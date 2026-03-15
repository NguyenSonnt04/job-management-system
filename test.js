// ═══════════════════════════════════════════════════════════
    //  CONSTANTS & STATE
    // ═══════════════════════════════════════════════════════════
    // ProgressBar is removed because flow is now dynamic
    let templateData = null;
    let templateName = '';
    let entryMode = null; // 'profile' | 'quick' | 'chat' | 'manual'

    const cvData = {
        name: '', subtitle: '', email: '', phone: '', address: '',
        linkedin: '', portfolio: '', summary: '',
        education: [], experience: [], projects: [], skills: [],
        certifications: [], awards: [], activities: []
    };

    let avatarDataUrl = null;
    let isEditMode = false;

    // ═══════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════
    async function init() {
        lucide.createIcons();

        const params = new URLSearchParams(window.location.search);
        const templateId = params.get('template');

        if (templateId) {
            try {
                const res = await fetch(`/api/cv-templates/${templateId}`);
                if (res.ok) {
                    const tpl = await res.json();
                    templateName = tpl.name || 'CV Mẫu';
                    window.currentTemplate = {
                        previewColor: tpl.previewColor || '#2E3B8E',
                        styleTag:     tpl.styleTag     || 'professional',
                        name:         tpl.name         || ''
                    };
                    if (tpl.templateContent) {
                        try { templateData = JSON.parse(tpl.templateContent); } catch {}
                    }
                }
            } catch {}
        }

        // Show template preview in background
        if (templateData && templateData.name) {
            renderCvPreview(templateData);
            const banner = document.createElement('div');
            banner.id = 'templateBanner';
            banner.style.cssText = `position:sticky;top:0;z-index:10;background:linear-gradient(135deg,#2e3b8e,#1a2456);color:white;text-align:center;padding:10px 16px;font-size:13px;font-weight:600;`;
            banner.innerHTML = `📋 Mẫu CV <strong>"${esc(templateName)}"</strong> — Chọn cách bắt đầu bên phải để điền thông tin của bạn`;
            document.getElementById('cvPreview').before(banner);
        } else {
            renderLiveSkeleton();
        }

        // Show onboarding overlay
        document.getElementById('onboardingOverlay').style.display = 'flex';

        document.getElementById('btnSend').addEventListener('click', handleSend);
        document.getElementById('chatInput').addEventListener('keydown', e => {
            if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  ONBOARDING FLOW — Entry point selection
    // ═══════════════════════════════════════════════════════════
    function showObEntries() {
        document.getElementById('obEntries').style.display = 'block';
        document.getElementById('quickAiPanel').classList.remove('show');
        document.getElementById('obLoading').classList.remove('show');
    }

    function showQuickAiForm() {
        document.getElementById('obEntries').style.display = 'none';
        document.getElementById('quickAiPanel').classList.add('show');
    }

    async function startWithProfile() {
        entryMode = 'profile';
        document.getElementById('obEntries').style.display = 'none';
        document.getElementById('obLoading').classList.add('show');

        try {
            const res = await fetch('/api/user/me');
            const user = await res.json();

            if (!user.authenticated) {
                document.getElementById('obLoading').classList.remove('show');
                document.getElementById('obEntries').style.display = 'block';
                const entryProfile = document.getElementById('entryProfile');
                entryProfile.style.borderColor = '#ef4444';
                entryProfile.querySelector('.ob-entry-desc').textContent = '⚠️ Bạn chưa đăng nhập. Vui lòng chọn cách khác.';
                return;
            }

            // Pre-fill cvData from profile
            cvData.name  = user.fullName  || user.displayName || '';
            cvData.email = user.email     || '';
            cvData.phone = user.phone     || '';

            // Build a minimal quick-generate payload to skip full chat
            const quickRaw = {
                name: cvData.name,
                email: cvData.email,
                phone: cvData.phone,
                subtitle: 'Ứng viên tìm việc',
                summary: `Tôi là ${cvData.name}, đang tìm kiếm cơ hội việc làm phù hợp.`,
                education: [], experience: [], projects: [], skills: [],
                certifications: [], awards: [], activities: []
            };

            await generateCvFromRaw(quickRaw);

        } catch (e) {
            document.getElementById('obLoading').classList.remove('show');
            document.getElementById('obEntries').style.display = 'block';
            addAiMsg('😔 Không thể đọc hồ sơ: ' + e.message);
            closeOnboarding();
        }
    }

    async function generateFromQuickForm() {
        const name     = document.getElementById('qaiName').value.trim();
        const role     = document.getElementById('qaiRole').value.trim();
        const industry = document.getElementById('qaiIndustry').value.trim();
        const exp      = document.getElementById('qaiExp').value.trim();
        const skills   = document.getElementById('qaiSkills').value.trim();

        if (!name) { document.getElementById('qaiName').focus(); return; }
        if (!role) { document.getElementById('qaiRole').focus(); return; }

        entryMode = 'quick';
        document.getElementById('quickAiPanel').classList.remove('show');
        document.getElementById('obLoading').classList.add('show');
        document.getElementById('obLoading').querySelector('.ob-loading-text').textContent = '✨ AI đang tạo CV của bạn...';

        const summary = `${name} là ${exp ? 'chuyên gia với ' + exp + ' kinh nghiệm' : 'ứng viên'} trong lĩnh vực ${industry || role}. Đang tìm kiếm vị trí ${role}.`;
        const skillList = skills ? skills.split(/[,;]+/).map(s => s.trim()).filter(Boolean) : [];

        const quickRaw = {
            name, subtitle: role,
            email: '', phone: '', address: '',
            summary,
            education: [], experience: [],
            projects: [],
            skills: skillList.length ? [{ category: 'Kỹ năng', items: skillList }] : [],
            certifications: [], awards: [], activities: []
        };

        Object.assign(cvData, { name, subtitle: role, summary });
        await generateCvFromRaw(quickRaw);
    }

    function startChatFlow() {
        entryMode = 'chat';
        closeOnboarding();
        document.getElementById('progressBar').innerHTML = ''; // Hide old progress bar
        
        const greeting = templateName
            ? `Xin chào! Tôi sẽ giúp bạn tạo CV theo mẫu <strong>"${esc(templateName)}"</strong>. Bạn có thể kể cho tôi nghe về bạn, bắt đầu từ tên và vị trí mong muốn nhé!`
            : 'Xin chào! Tôi là AI CV Assistant. Bạn muốn xây dựng CV cho vị trí nào? Hãy chia sẻ tên và vị trí bạn ứng tuyển nhé!';
        addAiMsg(greeting);
    }

    function startManualForm() {
        entryMode = 'manual';
        startChatFlow(); // Just use chat flow for now
    }

    function closeOnboarding() {
        const overlay = document.getElementById('onboardingOverlay');
        overlay.classList.add('hide');
        setTimeout(() => { overlay.style.display = 'none'; }, 280);
    }

    // ═══════════════════════════════════════════════════════════
    //  LIVE SKELETON PREVIEW
    // ═══════════════════════════════════════════════════════════
    function renderLiveSkeleton() {
        document.getElementById('cvPreview').innerHTML = `
            <div style="padding: 40px; background: white; min-height: 600px;">
                <div class="live-preview-hint">💡 CV của bạn sẽ được render ngay tại đây khi bạn cung cấp thông tin bên phải</div>
                <div style="display:flex; gap:20px; align-items:center; margin-bottom:30px;">
                    <div class="cv-skeleton-block" style="width:90px;height:90px;border-radius:50%;flex-shrink:0;"></div>
                    <div style="flex:1;">
                        <div class="cv-skeleton-block" style="height:24px;width:55%;margin-bottom:10px;"></div>
                        <div class="cv-skeleton-block" style="height:16px;width:35%;margin-bottom:10px;"></div>
                        <div class="cv-skeleton-block" style="height:12px;width:80%;"></div>
                    </div>
                </div>
                <div class="cv-skeleton-block" style="height:14px;width:30%;margin-bottom:16px;"></div>
                <div class="cv-skeleton-block" style="height:10px;width:100%;margin-bottom:8px;"></div>
                <div class="cv-skeleton-block" style="height:10px;width:90%;margin-bottom:8px;"></div>
                <div class="cv-skeleton-block" style="height:10px;width:75%;margin-bottom:28px;"></div>
                <div class="cv-skeleton-block" style="height:14px;width:25%;margin-bottom:16px;"></div>
                <div class="cv-skeleton-block" style="height:10px;width:100%;margin-bottom:8px;"></div>
                <div class="cv-skeleton-block" style="height:10px;width:85%;"></div>
            </div>`;
    }

    // Partial live preview — renders visible parts of CV as user fills in
    function renderPartialPreview() {
        if (!cvData.name && !cvData.subtitle) return;
        const accent = window.currentTemplate?.previewColor || '#2E3B8E';
        const partialData = Object.assign({}, cvData);
        // Render immediately using professional style
        const html = renderProfessionalStyle(partialData, accent);
        document.getElementById('cvPreview').innerHTML = html;
        const hint = document.createElement('div');
        hint.className = 'live-preview-hint';
        hint.style.margin = '8px 8px 0';
        hint.textContent = '✏️ Đây là bản nháp thời gian thực — tiếp tục cung cấp thông tin để hoàn thiện';
        document.getElementById('cvPreview').prepend(hint);
    }

    //  SEND HANDLER (AI DYNAMIC CHAT)
    // ═══════════════════════════════════════════════════════════
    async function handleSend() {
        const input = document.getElementById('chatInput');
        const text  = input.value.trim();
        if (!text) return;

        input.value = '';
        addUserMsg(text);
        clearQuickReplies();

        if (isPostGenMode) { await handleEditRequest(text); return; }

        // If user says done
        if (text.toLowerCase().includes('hoàn tất') || text.toLowerCase().includes('xong rồi')) {
            generateCv(); 
            return;
        }

        setAiTyping(true);

        try {
            const res = await fetch('/api/cv-ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text, currentCv: cvData })
            });
            const result = await res.json();
            setAiTyping(false);

            if (!res.ok || !result.success) throw new Error(result.error || 'Server error');

            const aiResp = result.data.ai_response;
            const extracted = result.data.extracted_data;

            // Merge extracted data carefully into cvData
            if (extracted) {
                for (const key in extracted) {
                    if (Array.isArray(cvData[key])) {
                        const newItems = Array.isArray(extracted[key]) ? extracted[key] : [extracted[key]];
                        cvData[key] = [...cvData[key], ...newItems];
                    } else {
                        cvData[key] = extracted[key];
                    }
                }
            }

            // Always render partial preview
            renderPartialPreview();

            // Display AI response
            addAiMsg(aiResp);
            
            // Add a hint to finish
            setQuickReplies(['Xong rồi, hãy tạo CV đầy đủ ➜']);

        } catch (e) {
            setAiTyping(false);
            addAiMsg('😔 Xin lỗi, tôi đang gặp sự cố kết nối: ' + e.message + '. Vui lòng thử lại.');
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  GENERATE CV (final step)
    // ═══════════════════════════════════════════════════════════
    async function generateCv() {
        addAiMsg('🎉 Tôi đã có cấu trúc cơ bản! Để tôi ráp lại thành CV hoàn chỉnh và làm đẹp câu chữ cho bạn nhé... <em>Quá trình này mất khoảng 10-20 giây.</em>');
        disableInput(true);

        document.getElementById('generatingOverlay').classList.add('show');

        try {
            const res = await fetch('/api/cv-ai/generate-cv', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ rawData: cvData })
            });

            document.getElementById('generatingOverlay').classList.remove('show');

            if (!res.ok) throw new Error('Server error');
            const data = await res.json();

            if (!data.success || !data.cvJson) {
                throw new Error(data.error || 'AI response invalid');
            }

            renderCvPreview(data.cvJson);

            // Remove the "this is a template" banner — CV is now personalized
            const banner = document.getElementById('templateBanner');
            if (banner) banner.remove();

            addAiMsg('✅ <strong>CV của bạn đã sẵn sàng!</strong> Bạn có thể xem bên trái. Nếu muốn thay đổi gì, hãy cho tôi biết nhé!');
            enablePostGenActions();

        } catch (e) {
            document.getElementById('generatingOverlay').classList.remove('show');
            addAiMsg('😔 Có lỗi khi tạo CV: ' + e.message + '. Vui lòng thử lại hoặc kiểm tra kết nối API Key.');
            disableInput(false);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  GENERATE CV FROM RAW (shared: quick/profile → no chat needed)
    // ═══════════════════════════════════════════════════════════
    async function generateCvFromRaw(rawData) {
        closeOnboarding();
        disableInput(true);
        document.getElementById('generatingOverlay').classList.add('show');

        try {
            const res = await fetch('/api/cv-ai/generate-cv', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ rawData })
            });

            document.getElementById('generatingOverlay').classList.remove('show');
            if (!res.ok) throw new Error('Server error');
            const data = await res.json();
            if (!data.success || !data.cvJson) throw new Error(data.error || 'AI response invalid');

            const banner = document.getElementById('templateBanner');
            if (banner) banner.remove();
            renderCvPreview(data.cvJson);

            addAiMsg(`✅ <strong>CV nháp đã sẵn sàng!</strong> Đây là bản CV được tạo từ thông tin của bạn. Hãy chọn một hướng tiếp theo để hoàn thiện hơn nhé!`);
            enablePostGenActions();

        } catch (e) {
            document.getElementById('generatingOverlay').classList.remove('show');
            addAiMsg('😔 Có lỗi khi tạo CV: ' + e.message + '. Vui lòng thử lại.');
            disableInput(false);
        }
    }

    function enablePostGenActions() {
        disableInput(false);
        isPostGenMode = true;
        document.getElementById('chatInput').placeholder = 'Yêu cầu chỉnh sửa CV (VD: "thêm kỹ năng Python", "viết lại summary")...';
        document.getElementById('aiStatus').textContent  = 'CV đã sẵn sàng — Chọn hướng chỉnh tiếp';
        document.getElementById('cvToolbar').classList.add('show');
        // Show branch cards instead of quick replies
        document.getElementById('postgenBranches').classList.add('show');
        clearQuickReplies();
    }

    // ── G1: Deep interview — AI fills missing sections ────────────────────────
    function branchDeepInterview() {
        document.getElementById('postgenBranches').classList.remove('show');
        addAiMsg('🎙️ <strong>Chế độ phỏng vấn sâu đã bật!</strong> Tôi sẽ hỏi về những phần còn thiếu trong CV để làm đầy đủ hơn. Hãy cùng bắt đầu!');

        const missing = [];
        if (!currentCvJson?.experience?.length)     missing.push('kinh nghiệm làm việc');
        if (!currentCvJson?.skills?.length)         missing.push('kỹ năng');
        if (!currentCvJson?.education?.length)      missing.push('học vấn');
        if (!currentCvJson?.certifications?.length) missing.push('chứng chỉ');

        if (missing.length === 0) {
            addAiMsg('✨ CV của bạn đã khá đầy đủ! Bạn muốn tôi điều chỉnh hoặc làm đẹp phần nào không? Hãy gõ yêu cầu.');
        } else {
            setTimeout(() => {
                addAiMsg(`📝 Tôi thấy CV còn thiếu <strong>${missing.join(', ')}</strong>. Hãy cho tôi biết thêm về ${missing[0]} của bạn nhé?`);
            }, 500);
        }
        setQuickReplies(['Quay lại chọn hướng']);
    }

    // ── G2: Block edit — Enable inline edit mode ──────────────────────────────
    function branchBlockEdit() {
        document.getElementById('postgenBranches').classList.remove('show');
        toggleEditMode();
        addAiMsg('✏️ <strong>Chế độ chỉnh sửa trực tiếp đã bật!</strong> Click vào bất kỳ phần nào trên CV bên trái để sửa text. Khi xong, nhấn nút "✅ Đang chỉnh sửa" để tắt.');
        setQuickReplies(['Xong, tắt chế độ sửa', 'Quay lại chọn hướng']);
    }

    // ── G3: Optimize by JD ────────────────────────────────────────────────────
    function branchOptimizeJD() {
        document.getElementById('jdPanel').classList.toggle('show');
        if (document.getElementById('jdPanel').classList.contains('show')) {
            document.getElementById('jdTextarea').focus();
        }
    }

    async function runJdOptimize() {
        const jd = document.getElementById('jdTextarea').value.trim();
        if (!jd) { addAiMsg('⚠️ Vui lòng dán nội dung JD vào ô trên trước nhé.'); return; }

        document.getElementById('jdPanel').classList.remove('show');
        document.getElementById('postgenBranches').classList.remove('show');
        setAiTyping(true);
        disableInput(true);

        addAiMsg('🎯 <strong>Đang phân tích JD và tối ưu CV...</strong> Tôi sẽ điều chỉnh từ ngữ và kỹ năng trên CV để phù hợp hơn với vị trí này.');

        const optimizeRequest = `Hãy tối ưu CV này theo Job Description sau đây. Điều chỉnh summary, experience bullets, và skills để match tốt hơn với JD. JD: ${jd}`;

        await handleEditRequest(optimizeRequest);
    }

    // ── G4: Change template ───────────────────────────────────────────────────
    function branchChangeTemplate() {
        document.getElementById('postgenBranches').classList.remove('show');
        addAiMsg('🎨 <strong>Đổi mẫu CV!</strong> Bạn sẽ được chuyển đến trang chọn mẫu. CV của bạn đã được lưu tạm — hãy chọn mẫu mới và thông tin sẽ được giữ nguyên.');
        setTimeout(() => {
            if (confirm('Bạn muốn đổi sang mẫu CV khác? Nội dung CV sẽ được giữ nguyên.')) {
                window.location.href = '/tao-cv-ai.html';
            } else {
                document.getElementById('postgenBranches').classList.add('show');
            }
        }, 800);
    }


    // ── Toggle inline edit mode ───────────────────────────────────────────────
    function toggleEditMode() {
        isEditMode = !isEditMode;
        const preview = document.getElementById('cvPreview');
        const badge   = document.getElementById('editModeBadge');
        const btn     = document.getElementById('btnEditToggle');
        if (isEditMode) {
            preview.classList.add('cv-editable-mode');
            preview.querySelectorAll('.cv-editable').forEach(el => el.setAttribute('contenteditable', 'true'));
            btn.classList.add('active');
            btn.textContent = '✅ Đang chỉnh sửa';
            badge.classList.add('show');
            addAiMsg('✏️ <strong>Chế độ chỉnh sửa thủ công đã bật.</strong> Bạn có thể click trực tiếp vào bất kỳ đoạn văn bản nào trên CV bên trái để sửa. Nhấn nút lần nữa để tắt.');
        } else {
            preview.classList.remove('cv-editable-mode');
            preview.querySelectorAll('[contenteditable]').forEach(el => el.removeAttribute('contenteditable'));
            btn.classList.remove('active');
            btn.textContent = '✏️ Chỉnh sửa thủ công';
            badge.classList.remove('show');
            addAiMsg('💾 Chỉnh sửa thủ công đã tắt. Mọi thay đổi đã được áp dụng vào CV.');
        }
    }

    // ── Avatar upload ─────────────────────────────────────────────────────────
    function triggerAvatarUpload() {
        document.getElementById('avatarFileInput').click();
    }

    function handleAvatarUpload(event) {
        const file = event.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function(e) {
            avatarDataUrl = e.target.result;
            // Update avatar in preview if it exists
            const avatarEl = document.getElementById('cvAvatarImg');
            if (avatarEl) {
                avatarEl.src = avatarDataUrl;
                avatarEl.style.display = 'block';
                const placeholder = document.getElementById('cvAvatarPlaceholder');
                if (placeholder) placeholder.style.display = 'none';
            }
            addAiMsg('🖼️ <strong>Ảnh đại diện đã được cập nhật!</strong> Ảnh sẽ xuất hiện trong CV của bạn.');
        };
        reader.readAsDataURL(file);
    }

    // ── Post-gen: AI edit mode ────────────────────────────────────────────────
    let isPostGenMode = false;
    let currentCvJson = null;  // tracks the live generated/edited CV

    async function handleEditRequest(text) {
        if (!currentCvJson) {
            addAiMsg('Chưa có CV nào để chỉnh sửa. Vui lòng hoàn thành quá trình tạo CV trước.');
            return;
        }

        setAiTyping(true);
        disableInput(true);
        clearQuickReplies();

        try {
            const res = await fetch('/api/cv-ai/edit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ request: text, currentCv: currentCvJson })
            });

            setAiTyping(false);
            disableInput(false);

            if (!res.ok) throw new Error('Server error');
            const data = await res.json();

            if (!data.success) throw new Error(data.message || 'Lỗi chỉnh sửa');

            // Update the live CV
            currentCvJson = data.cvJson;
            renderCvPreview(data.cvJson);

            addAiMsg((data.message || 'CV đã được cập nhật! ✨') + ' Bạn có muốn thay đổi gì thêm không?');

            // Reset save button (content changed, not yet saved)
            const btnSave = document.getElementById('btnSaveCv');
            if (btnSave) {
                btnSave.textContent = '💾 Lưu CV';
                btnSave.classList.remove('saved');
            }

            setQuickReplies([
                'Tiếp tục chỉnh sửa khác',
                'Viết lại phần tóm tắt',
                'Thêm kỹ năng mới'
            ]);

        } catch (e) {
            setAiTyping(false);
            disableInput(false);
            addAiMsg('😔 Không thể chỉnh sửa: ' + e.message);
        }
    }

    // ── Save CV to DB ─────────────────────────────────────────────────────────
    let savedCvId = null;

    function openSaveModal() {
        if (!currentCvJson) { addAiMsg('Chưa có CV để lưu.'); return; }
        const defaultName = currentCvJson.name
            ? `CV của ${currentCvJson.name}`
            : 'CV của tôi';
        document.getElementById('cvNameInput').value = savedCvId ? (currentCvJson._savedName || defaultName) : defaultName;
        document.getElementById('saveModal').classList.add('show');
        setTimeout(() => document.getElementById('cvNameInput').focus(), 80);
    }

    function closeSaveModal() {
        document.getElementById('saveModal').classList.remove('show');
    }

    // Close on backdrop click
    document.getElementById('saveModal').addEventListener('click', function(e) {
        if (e.target === this) closeSaveModal();
    });

    // Enter to confirm
    document.getElementById('cvNameInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter') confirmSave();
        if (e.key === 'Escape') closeSaveModal();
    });

    async function confirmSave() {
        const cvName = (document.getElementById('cvNameInput').value || '').trim()
            || (currentCvJson.name ? `CV của ${currentCvJson.name}` : 'CV của tôi');

        closeSaveModal();

        const btnSave = document.getElementById('btnSaveCv');
        btnSave.textContent = '⏳ Đang lưu...';
        btnSave.disabled = true;

        const params = new URLSearchParams(window.location.search);

        try {
            const url    = savedCvId ? `/api/user-cv/${savedCvId}` : '/api/user-cv/save';
            const method = savedCvId ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    cvName,
                    templateId:   params.get('template') || null,
                    templateName: templateName || '',
                    cvContent:    JSON.stringify(currentCvJson)
                })
            });

            const data = await res.json();

            if (res.status === 401) {
                addAiMsg('⚠️ Bạn cần <strong><a href="/login.html" style="color:#2e3b8e;">đăng nhập</a></strong> để lưu CV vào tài khoản.');
                btnSave.textContent = '💾 Lưu CV';
                btnSave.disabled = false;
                return;
            }

            if (!data.success) throw new Error(data.message);

            if (data.id) { savedCvId = data.id; }
            if (currentCvJson) currentCvJson._savedName = cvName;

            btnSave.textContent = '✅ Đã lưu';
            btnSave.classList.add('saved');
            btnSave.disabled = false;
            addAiMsg(`💾 CV <strong>"${cvName}"</strong> đã được lưu thành công! <a href="/my-cvs.html" style="color:#2e3b8e;font-weight:700;">Xem CV của tôi →</a>`);

        } catch (e) {
            btnSave.textContent = '💾 Lưu CV';
            btnSave.disabled = false;
            addAiMsg('Lỗi khi lưu CV: ' + e.message);
        }
    }

    // Keep backward-compat alias
    function saveCv() { openSaveModal(); }

    // ── Download PDF ─────────────────────────────────────────────────────────
    function downloadPdf() {
        if (isEditMode) toggleEditMode();
        // Update document title temporarily for the print dialog filename
        const name = currentCvJson?.name || 'CV';
        const orig = document.title;
        document.title = `CV_${name.replace(/\s+/g, '_')}`;
        setTimeout(() => {
            window.print();
            setTimeout(() => { document.title = orig; }, 500);
        }, 150);
    }

    // ═══════════════════════════════════════════════════════════
    //  CV RENDERER — Multi-Style Dispatcher
    // ═══════════════════════════════════════════════════════════
    function renderCvPreview(c) {
        currentCvJson = c;
        const accent = window.currentTemplate?.previewColor || c._accent || '#2E3B8E';
        c._accent = accent;

        // Route to style-specific renderer based on template styleTag
        const style = window.currentTemplate?.styleTag || c._styleTag || 'professional';
        c._styleTag = style;

        let html = '';
        if (style === 'creative')    html = renderCreativeStyle(c, accent);
        else if (style === 'minimalist') html = renderMinimalistStyle(c, accent);
        else if (style === 'classic')    html = renderClassicStyle(c, accent);
        else if (style === 'harvard')    html = renderHarvardStyle(c, accent);
        else                             html = renderProfessionalStyle(c, accent);

        document.getElementById('cvPreview').innerHTML = html;

        if (isEditMode) {
            document.getElementById('cvPreview').classList.add('cv-editable-mode');
            document.getElementById('cvPreview').querySelectorAll('.cv-editable').forEach(el => el.setAttribute('contenteditable', 'true'));
        }
    }

    // ── Common helpers ──────────────────────────────────────────
    function ed(text) { return `<span class="cv-editable">${esc(text)}</span>`; }

    function getAvatarHtml(borderColor = 'rgba(255,255,255,0.6)', size = 120, shape = 'circle') {
        const existing = avatarDataUrl;
        const radius = shape === 'square' ? '12px' : '50%';
        return `<div class="cvm-avatar-wrapper" style="width:${size}px;height:${size}px;">
            <div class="cvm-avatar" id="cvAvatarPlaceholder" style="border-radius:${radius};${existing ? 'display:none;' : ''}"></div>
            <img id="cvAvatarImg" src="${existing || ''}" alt="Avatar"
                style="width:${size}px;height:${size}px;border-radius:${radius};object-fit:cover;border:4px solid ${borderColor};${existing ? '' : 'display:none;'}">
            <div class="avatar-upload-overlay" onclick="triggerAvatarUpload()" title="Thay đổi ảnh" style="border-radius:${radius};">📷</div>
        </div>`;
    }

    // ── STYLE 1: Professional (2-column with color header) ──────
    function renderProfessionalStyle(c, accent) {
        // Pre-defined widths create a natural, varied look across all skill groups
        const SKILL_WIDTHS = [92, 78, 85, 65, 88, 72, 80, 68, 76, 70, 83, 60, 90, 75, 64];
        let skillWidthIdx = 0;

        const skillsHtml = (c.skills || []).map(sg => `
            <div class="cvm-skill-group">
                <div class="cvm-skill-cat">${ed(sg.category)}</div>
                <div class="cvm-skill-list">
                    ${(sg.items || []).map(item => ed(item)).join(', ')}
                </div>
            </div>`).join('');

        const eduHtml = (c.education || []).map(e => `
            <div class="cvm-edu-item">
                <div class="cvm-edu-degree">${ed(e.degree||'')}</div>
                <div class="cvm-edu-school">${ed(e.school||e.location||'')}</div>
                <div class="cvm-edu-year">${ed(e.period||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvm-edu-detail">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const certHtml = (c.certifications||[]).map(cert=>`
            <div class="cvm-edu-item">
                <div class="cvm-edu-degree">${ed(cert.name||cert.raw||'')}</div>
                ${cert.issuer?`<div class="cvm-edu-school">${ed(cert.issuer)}</div>`:''}
                ${cert.year?`<div class="cvm-edu-year">${ed(cert.year)}</div>`:''}
            </div>`).join('');

        const awardsHtml = (c.awards||[]).map(aw=>`
            <div class="cvm-edu-item">
                <div class="cvm-edu-degree">🏆 ${ed(aw.name||aw.raw||'')}</div>
                ${aw.year?`<div class="cvm-edu-year">${ed(aw.year)}</div>`:''}
            </div>`).join('');

        const expHtml = (c.experience||[]).map(e=>`
            <div class="cvm-exp-item">
                <div class="cvm-exp-role">${ed(e.role||'')}</div>
                <div class="cvm-exp-company">${ed(e.company||'')} &nbsp;|&nbsp; ${ed(e.period||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvm-exp-detail">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const projHtml = (c.projects||[]).map(p=>`
            <div class="cvm-exp-item">
                <div class="cvm-exp-role">${ed(p.name||'')}</div>
                <div class="cvm-exp-company">${p.period?`${ed(p.period)} <br>`:''} ${p.tech?`<strong>Tech:</strong> ${ed(p.tech)} <br>`:''} ${p.github?`<a href="${esc(p.github)}" target="_blank" style="color:${accent}">${ed(p.github)}</a>`:''}</div>
                ${(p.details||[]).map(d=>`<div class="cvm-exp-detail">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const actHtml = (c.activities||[]).map(a=>`
            <div class="cvm-exp-item">
                <div class="cvm-exp-role">${ed(a.name||a.raw||'')}</div>
                ${a.role?`<div class="cvm-exp-company">${ed(a.role)}</div>`:''}
                ${a.period?`<div class="cvm-exp-company">${ed(a.period)}</div>`:''}
                ${(a.details||[]).map(d=>`<div class="cvm-exp-detail">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const summaryHtml = c.summary ? `<div class="cvm-section cvm-summary-section">
            <div class="cvm-section-title" style="color:${accent};border-bottom:1.5px solid ${accent};">MỤC TIÊU NGHỀ NGHIỆP</div>
            <div class="cvm-summary-text-full cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</div></div>` : '';

        const linkedinHtml = c.linkedin ? `<span>🔗 <a href="${esc(c.linkedin)}" target="_blank" style="color:rgba(255,255,255,0.9);text-decoration:none;">${ed(c.linkedin)}</a></span>` : '';
        const portfolioHtml = c.portfolio ? `<span>🌐 <a href="${esc(c.portfolio)}" target="_blank" style="color:rgba(255,255,255,0.9);text-decoration:none;">${ed(c.portfolio)}</a></span>` : '';

        return `<div class="cv-full" style="--cv-accent:${accent};">
            <header class="cvm-header" style="background: linear-gradient(135deg, ${accent}, ${accent}dd);">
                ${getAvatarHtml()}
                <div class="cvm-header-info">
                    <h1 class="cvm-name cv-editable">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</h1>
                    <div class="cvm-job-title cv-editable">${esc(c.subtitle||'Vị trí công việc mong muốn')}</div>
                    <div class="cvm-contact-row">
                        ${c.email?`<span>📧 <span class="cv-editable">${esc(c.email)}</span></span>`:''}
                        ${c.phone?`<span>📱 <span class="cv-editable">${esc(c.phone)}</span></span>`:''}
                        ${c.address?`<span>📍 <span class="cv-editable">${esc(c.address)}</span></span>`:''}
                        ${linkedinHtml}
                        ${portfolioHtml}
                    </div>
                </div>
            </header>
            <div class="cvm-body">
                <aside class="cvm-left">
                    ${skillsHtml?`<section class="cvm-section"><h2 class="cvm-section-title" style="color:${accent};">KỸ NĂNG</h2>${skillsHtml}</section>`:''}
                    ${eduHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">HỌC VẤN</h2>${eduHtml}</section>`:''}
                    ${certHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">CHỨNG CHỈ</h2>${certHtml}</section>`:''}
                </aside>
                <main class="cvm-right">
                    ${summaryHtml}
                    ${expHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">KINH NGHIỆM LÀM VIỆC</h2>${expHtml}</section>`:''}
                    ${projHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">DỰ ÁN NỔI BẬT</h2>${projHtml}</section>`:''}
                    ${awardsHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">GIẢI THƯỞNG</h2>${awardsHtml}</section>`:''}
                    ${actHtml?`<section class="cvm-section" style="margin-top:20px;"><h2 class="cvm-section-title" style="color:${accent};">HOẠT ĐỘNG</h2>${actHtml}</section>`:''}
                </main>
            </div>
        </div>`;
    }

    // ── STYLE 2: Creative (Modern Card Layout) ──────
    function renderCreativeStyle(c, accent) {
        const skillGroupsHtml = (c.skills||[]).map(sg=>`
            <div class="cvc2-skill-group">
                <div class="cvc2-skill-cat">${ed(sg.category)}</div>
                <div class="cvc2-tags">${(sg.items||[]).map(i=>`<span class="cvc2-tag">${ed(i)}</span>`).join('')}</div>
            </div>`).join('');

        const expHtml = (c.experience||[]).map(e=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">${ed(e.role||'')}</div>
                <div class="cvc2-card-sub" style="color:${accent}">${ed(e.company||'')} &nbsp;|&nbsp; ${ed(e.period||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvc2-card-bullet">◈ ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const eduHtml = (c.education||[]).map(e=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">${ed(e.degree||'')}</div>
                <div class="cvc2-card-sub" style="color:${accent}">${ed(e.school||e.location||'')}</div>
                <div class="cvc2-card-sub">${ed(e.period||'')}</div>
            </div>`).join('');

        const projHtml = (c.projects||[]).map(p=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">${ed(p.name||'')}</div>
                <div class="cvc2-card-sub" style="color:${accent}">${ed(p.period||'')} &nbsp;|&nbsp; ${ed(p.tech||'')}</div>
                ${(p.details||[]).map(d=>`<div class="cvc2-card-bullet">◈ ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const awardsHtml = (c.awards||[]).map(aw=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">🏆 ${ed(aw.name||aw.raw||'')}</div>
                <div class="cvc2-card-sub">${ed(aw.year||'')}</div>
            </div>`).join('');

        const certHtml = (c.certifications||[]).map(cert=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">${ed(cert.name||cert.raw||'')}</div>
                <div class="cvc2-card-sub">${ed(cert.issuer||'')} &nbsp;|&nbsp; ${ed(cert.year||'')}</div>
            </div>`).join('');

        const actHtml = (c.activities||[]).map(a=>`
            <div class="cvc2-card">
                <div class="cvc2-card-title">${ed(a.name||a.raw||'')}</div>
                <div class="cvc2-card-sub">${ed(a.role||'')} &nbsp;|&nbsp; ${ed(a.period||'')}</div>
                ${(a.details||[]).map(d=>`<div class="cvc2-card-bullet">◈ ${ed(d)}</div>`).join('')}
            </div>`).join('');

        return `<div class="cv-full cvc2-root" style="--cv-accent:${accent};">
            <header class="cvc2-header" style="background: linear-gradient(45deg, ${accent}, ${accent}cc);">
                ${getAvatarHtml('rgba(255,255,255,0.8)', 110, 'square')}
                <div class="cvc2-header-right">
                    <h1 class="cvc2-name cv-editable">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</h1>
                    <div class="cvc2-title cv-editable">${esc(c.subtitle||'Vị trí / Chức danh')}</div>
                    <div class="cvc2-contacts">
                        ${c.email?`<span>📧 <span class="cv-editable">${esc(c.email)}</span></span>`:''}
                        ${c.phone?`<span>📱 <span class="cv-editable">${esc(c.phone)}</span></span>`:''}
                        ${c.address?`<span>📍 <span class="cv-editable">${esc(c.address)}</span></span>`:''}
                    </div>
                </div>
            </header>
            <div class="cvc2-body">
                <aside class="cvc2-left">
                    ${c.summary?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">GIỚI THIỆU</h3><div class="cvc2-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</div></section>`:''}
                    ${skillGroupsHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">KỸ NĂNG</h3>${skillGroupsHtml}</section>`:''}
                    ${eduHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">HỌC VẤN</h3>${eduHtml}</section>`:''}
                    ${certHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">CHỨNG CHỈ</h3>${certHtml}</section>`:''}
                    ${awardsHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">GIẢI THƯỞNG</h3>${awardsHtml}</section>`:''}
                </aside>
                <main class="cvc2-right">
                    ${expHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">KINH NGHIỆM</h3>${expHtml}</section>`:''}
                    ${projHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">DỰ ÁN PHÁT TRIỂN</h3>${projHtml}</section>`:''}
                    ${actHtml?`<section class="cvc2-zone"><h3 class="cvc2-zone-title" style="color:${accent}">HOẠT ĐỘNG</h3>${actHtml}</section>`:''}
                </main>
            </div>
        </div>`;
    }



    // ── STYLE 3: Minimalist (High-end Typography) ──
    function renderMinimalistStyle(c, accent) {
        const block = (title, content) => content ? `
            <div class="cvm2-block">
                <div class="cvm2-block-label" style="background:${accent};">${title}</div>
                <div class="cvm2-block-body">${content}</div>
            </div>` : '';

        const expHtml = (c.experience||[]).map(e=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(e.period||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">${esc(e.role||'')}</div>
                    <div class="cvm2-row-org">${esc(e.company||'')}</div>
                    ${(e.details||[]).map(d=>`<div class="cvm2-bullet">${ed(d)}</div>`).join('')}
                </div>
            </div>`).join('');

        const eduHtml = (c.education||[]).map(e=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(e.period||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">${esc(e.degree||'')}</div>
                    <div class="cvm2-row-org">${esc(e.school||e.location||'')}</div>
                </div>
            </div>`).join('');

        const projHtml = (c.projects||[]).map(p=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(p.period||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">${esc(p.name||'')}</div>
                    <div class="cvm2-row-org">${esc(p.tech||'')}</div>
                    ${(p.details||[]).map(d=>`<div class="cvm2-bullet">${ed(d)}</div>`).join('')}
                </div>
            </div>`).join('');

        const certHtml = (c.certifications||[]).map(cert=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(cert.year||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">${esc(cert.name||cert.raw||'')}</div>
                    <div class="cvm2-row-org">${esc(cert.issuer||'')}</div>
                </div>
            </div>`).join('');

        const awardsHtml = (c.awards||[]).map(aw=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(aw.year||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">🏆 ${esc(aw.name||aw.raw||'')}</div>
                </div>
            </div>`).join('');

        const actHtml = (c.activities||[]).map(a=>`
            <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(a.period||'')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-title">${esc(a.name||a.raw||'')}</div>
                    <div class="cvm2-row-org">${esc(a.role||'')}</div>
                    ${(a.details||[]).map(d=>`<div class="cvm2-bullet">${ed(d)}</div>`).join('')}
                </div>
            </div>`).join('');

        const skillHtml = (c.skills||[]).map(sg=>`
             <div class="cvm2-row">
                <div class="cvm2-row-period" style="color:${accent}">${esc(sg.category||'Kỹ năng')}</div>
                <div class="cvm2-row-content">
                    <div class="cvm2-row-org">${(sg.items||[]).join(', ')}</div>
                </div>
            </div>`).join('');

        return `<div class="cv-full cvm2-root">
            <header class="cvm2-header">
                <h1 class="cvm2-name cv-editable">${esc(c.name||'HỌ TÊN')}</h1>
                <div class="cvm2-subtitle cv-editable">${esc(c.subtitle||'')}</div>
                <div class="cvm2-contacts">
                    ${c.email?`<span>✉ ${esc(c.email)}</span>`:''}
                    ${c.phone?`<span>✆ ${esc(c.phone)}</span>`:''}
                    ${c.address?`<span>📍 ${esc(c.address)}</span>`:''}
                </div>
            </header>
            <div class="cvm2-body">
                ${c.summary?block('GIỚI THIỆU',`<p class="cvm2-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p>`)  :''}
                ${expHtml?block('KINH NGHIỆM',expHtml):''}
                ${projHtml?block('DỰ ÁN',projHtml):''}
                ${eduHtml?block('HỌC VẤN',eduHtml):''}
                ${skillHtml?block('KỸ NĂNG',skillHtml):''}
                ${certHtml?block('CHỨNG CHỈ',certHtml):''}
                ${awardsHtml?block('GIẢI THƯỞNG',awardsHtml):''}
                ${actHtml?block('HOẠT ĐỘNG',actHtml):''}
            </div>
        </div>`;
    }

    // ── STYLE 4: Classic (Center name + 2-col split body) ───────
    // Structure: Centered header with accent border + left=experience, right=edu+skills
    function renderClassicStyle(c, accent) {
        const hrColor = accent || '#1a1a1a';

        const contactBits = [
            c.email    ? `<span class="cv-editable">${esc(c.email)}</span>` : '',
            c.phone    ? `<span class="cv-editable">${esc(c.phone)}</span>` : '',
            c.address  ? `<span class="cv-editable">${esc(c.address)}</span>` : '',
            c.linkedin ? `<span class="cv-editable">${esc(c.linkedin)}</span>` : '',
            c.portfolio? `<span class="cv-editable">${esc(c.portfolio)}</span>` : ''
        ].filter(Boolean).join(' · ');

        const lsec = (title, content) => content ? `
            <div class="cvcl2-section">
                <div class="cvcl2-sec-title" style="color:${hrColor};border-left:4px solid ${hrColor};">${title}</div>
                ${content}
            </div>` : '';

        const rsec = (title, content) => content ? `
            <div class="cvcl2-section">
                <div class="cvcl2-sec-title-r" style="border-bottom:2px solid ${hrColor};color:${hrColor};">${title}</div>
                ${content}
            </div>` : '';

        const expHtml = (c.experience||[]).map(e=>`
            <div class="cvcl2-item">
                <div class="cvcl2-item-top">
                    <span class="cvcl2-role cv-editable">${esc(e.role||'')}</span>
                    <span class="cvcl2-date" style="color:${hrColor}">${ed(e.period||'')}</span>
                </div>
                <div class="cvcl2-company cv-editable">${esc(e.company||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvcl2-bullet">▪ ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const projHtml = (c.projects||[]).map(p=>`
            <div class="cvcl2-item">
                <div class="cvcl2-item-top">
                    <span class="cvcl2-role cv-editable">${esc(p.name||'')}</span>
                    <span class="cvcl2-date" style="color:${hrColor}">${ed(p.period||'')}</span>
                </div>
                ${p.tech?`<div class="cvcl2-company">Tech: ${ed(p.tech)}</div>`:''}
                ${(p.details||[]).map(d=>`<div class="cvcl2-bullet">▪ ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const eduHtml = (c.education||[]).map(e=>`
            <div class="cvcl2-r-item">
                <div class="cvcl2-r-degree cv-editable">${esc(e.degree||'')}</div>
                <div class="cvcl2-r-school cv-editable">${esc(e.school||e.location||'')}</div>
                <div class="cvcl2-r-period" style="color:${hrColor}">${ed(e.period||'')}</div>
            </div>`).join('');

        const skillHtml = (c.skills||[]).map(sg=>`
            <div class="cvcl2-skill">
                <div class="cvcl2-skill-cat">${ed(sg.category)}</div>
                <div class="cvcl2-skill-vals">${(sg.items||[]).map(i=>`<span class="cvcl2-pill" style="border-color:${hrColor}40;color:#333">${ed(i)}</span>`).join('')}</div>
            </div>`).join('');

        const certHtml = (c.certifications||[]).map(cert=>`
            <div class="cvcl2-r-item"><span class="cv-editable">${esc(cert.name||cert.raw||'')}</span>${cert.year?` <span style="color:${hrColor};font-size:11px">(${esc(cert.year)})</span>`:''}</div>`).join('');

        const awardsHtml = (c.awards||[]).map(aw=>`
            <div class="cvcl2-r-item">🏆 <span class="cv-editable">${esc(aw.name||aw.raw||'')}</span>${aw.year?` <span style="color:${hrColor};font-size:11px">(${esc(aw.year)})</span>`:''}</div>`).join('');

        const actHtml = (c.activities||[]).map(a=>`
            <div class="cvcl2-r-item"><span class="cv-editable">${esc(a.name||a.raw||'')}</span>${a.period?` <span style="color:${hrColor};font-size:11px">(${esc(a.period)})</span>`:''}</div>`).join('');

        return `<div class="cv-full cvcl2-root" style="--cv-accent:${hrColor};">
            <!-- Centered header with avatar -->
            <div class="cvcl2-header" style="border-top:5px solid ${hrColor};">
                <div class="cvcl2-header-left">
                    ${getAvatarHtml(hrColor+'80', 100, 'square')}
                </div>
                <div class="cvcl2-header-center">
                    <div class="cvcl2-name cv-editable">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</div>
                    <div class="cvcl2-subtitle cv-editable" style="color:${hrColor}">${esc(c.subtitle||'')}</div>
                    <div class="cvcl2-contact">${contactBits}</div>
                </div>
            </div>
            ${c.summary?`<div class="cvcl2-summary-bar" style="border-left:4px solid ${hrColor};">
                <p class="cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p>
            </div>`:''}
            <!-- 2-col body: left=exp+proj, right=edu+skills+certs -->
            <div class="cvcl2-body">
                <div class="cvcl2-left">
                    ${lsec('KINH NGHIỆM', expHtml)}
                    ${lsec('DỰ ÁN', projHtml)}
                </div>
                <div class="cvcl2-right">
                    ${rsec('HỌC VẤN', eduHtml)}
                    ${rsec('KỸ NĂNG', skillHtml)}
                    ${rsec('CHỨNG CHỈ', certHtml)}
                    ${rsec('THÀNH TÍCH', awardsHtml)}
                    ${rsec('HOẠT ĐỘNG', actHtml)}
                </div>
            </div>
        </div>`;
    }

    // ── STYLE 5: Harvard (Authentic Harvard resume format) ─────
    function renderHarvardStyle(c, accent) {
        // Harvard uses Times New Roman, very traditional formatting
        // Clean layout with left-aligned name, centered contact info

        const contactLine = [
            c.email    ? `${esc(c.email)}` : '',
            c.phone    ? `${esc(c.phone)}` : '',
            c.address  ? `${esc(c.address)}` : '',
            c.linkedin ? `${esc(c.linkedin)}` : ''
        ].filter(Boolean).join(' | ');

        const section = (title, content) => content ? `
            <div class="cvh-section">
                <div class="cvh-title">${title}</div>
                ${content}
            </div>` : '';

        const expHtml = (c.experience||[]).map(e=>`
            <div class="cvh-item">
                <div class="cvh-item-head">
                    <span class="cvh-item-title cv-editable">${esc(e.role||'')}</span>
                    <span class="cvh-item-date cv-editable">${esc(e.period||'')}</span>
                </div>
                <div class="cvh-item-sub">${esc(e.company||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvh-bullet">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const eduHtml = (c.education||[]).map(e=>`
            <div class="cvh-item">
                <div class="cvh-item-head">
                    <span class="cvh-item-title cv-editable">${esc(e.degree||'')}</span>
                    <span class="cvh-item-date cv-editable">${esc(e.period||'')}</span>
                </div>
                <div class="cvh-item-sub">${esc(e.school||'')}</div>
                ${(e.details||[]).map(d=>`<div class="cvh-bullet">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const projHtml = (c.projects||[]).map(p=>`
            <div class="cvh-item">
                <div class="cvh-item-head">
                    <span class="cvh-item-title cv-editable">${esc(p.name||'')}</span>
                    <span class="cvh-item-date">${esc(p.period||'')}</span>
                </div>
                ${p.tech?`<div class="cvh-item-sub">Technologies: ${ed(p.tech)}</div>`:''}
                ${p.github?`<div class="cvh-bullet"><a href="${esc(p.github)}" target="_blank">${ed(p.github)}</a></div>`:''}
                ${(p.details||[]).map(d=>`<div class="cvh-bullet">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        const skillHtml = (c.skills||[]).map(sg=>`
            <div class="cvh-skill-group">
                <span class="cvh-skill-cat">${ed(sg.category)}:</span>
                <span class="cvh-skill-items">${(sg.items||[]).map(i=>ed(i)).join(', ')}</span>
            </div>`).join('');

        const certHtml = (c.certifications||[]).map(cert=>`
            <div class="cvh-inline-item">
                <span class="cv-editable">${esc(cert.name||cert.raw||'')}</span>
                ${cert.issuer?`, <em>${esc(cert.issuer)}</em>`:''}
                ${cert.year?` (${ed(cert.year)})`:''}
            </div>`).join('');

        const awardsHtml = (c.awards||[]).map(aw=>`
            <div class="cvh-inline-item">
                <strong class="cv-editable">${esc(aw.name||aw.raw||'')}</strong>
                ${aw.year?`, ${ed(aw.year)}`:''}
            </div>`).join('');

        const actHtml = (c.activities||[]).map(a=>`
            <div class="cvh-item">
                <div class="cvh-item-head">
                    <span class="cvh-item-title cv-editable">${esc(a.name||a.raw||'')}</span>
                    ${a.period?`<span class="cvh-item-date">${ed(a.period)}</span>`:''}
                </div>
                ${a.role?`<div class="cvh-item-sub">${ed(a.role)}</div>`:''}
                ${(a.details||[]).map(d=>`<div class="cvh-bullet">• ${ed(d)}</div>`).join('')}
            </div>`).join('');

        return `<div class="cv-full cvh-root">
            <div class="cvh-header">
                <div class="cvh-name cv-editable">${esc(c.name||'YOUR NAME')}</div>
                <div class="cvh-subtitle cv-editable">${esc(c.subtitle||'')}</div>
                <div class="cvh-contact">${contactLine}</div>
            </div>
            <div class="cvh-body">
                ${c.summary ? section('PROFESSIONAL SUMMARY', `<p class="cvh-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p>`) : ''}
                ${section('EXPERIENCE', expHtml)}
                ${section('EDUCATION', eduHtml)}
                ${section('PROJECTS', projHtml)}
                ${section('SKILLS & EXPERTISE', skillHtml)}
                ${section('CERTIFICATIONS', certHtml)}
                ${section('AWARDS & HONORS', awardsHtml)}
                ${section('LEADERSHIP & ACTIVITIES', actHtml)}
            </div>
        </div>`;
    }


    // ═══════════════════════════════════════════════════════════
    //  CHAT UI HELPERS
    // ═══════════════════════════════════════════════════════════
    function addAiMsg(html) {
        const el = document.createElement('div');
        el.className = 'msg msg-ai';
        el.innerHTML = `<div class="msg-bubble">${html}</div><div class="msg-time">${nowTime()}</div>`;
        appendMsg(el);
    }

    function addUserMsg(text) {
        const el = document.createElement('div');
        el.className = 'msg msg-user';
        el.innerHTML = `<div class="msg-bubble">${esc(text)}</div><div class="msg-time">${nowTime()}</div>`;
        appendMsg(el);
    }

    function appendMsg(el) {
        const h = document.getElementById('chatHistory');
        h.appendChild(el);
        h.scrollTop = h.scrollHeight;
    }

    let typingEl = null;
    function setAiTyping(on) {
        if (on && !typingEl) {
            typingEl = document.createElement('div');
            typingEl.className = 'typing-indicator';
            typingEl.innerHTML = '<span class="dot-typing"></span><span class="dot-typing"></span><span class="dot-typing"></span>';
            appendMsg(typingEl);
        } else if (!on && typingEl) {
            typingEl.remove();
            typingEl = null;
        }
    }

    function setQuickReplies(chips) {
        const qr = document.getElementById('quickReplies');
        qr.innerHTML = chips.map(c =>
            `<button class="quick-reply" onclick="handleQuickReply('${c}')">${c}</button>`
        ).join('');
    }

    function clearQuickReplies() {
        document.getElementById('quickReplies').innerHTML = '';
    }

    function handleQuickReply(text) {
        document.getElementById('chatInput').value = text;
        handleSend();
    }

    function disableInput(disabled) {
        document.getElementById('chatInput').disabled = disabled;
        document.getElementById('btnSend').style.opacity = disabled ? '0.5' : '1';
        document.getElementById('btnSend').style.pointerEvents = disabled ? 'none' : 'auto';
    }

    function restartChat() {
        if (!confirm('Bắt đầu lại từ đầu? Dữ liệu hiện tại sẽ bị xóa.')) return;
        location.reload();
    }

    function nowTime() {
        return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    function esc(str) {
        return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    // ═══════════════════════════════════════════════════════════
    //  BOOT
    // ═══════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', init);
    