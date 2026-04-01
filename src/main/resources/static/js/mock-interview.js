/* ============================================================
   MOCK INTERVIEW AI — Frontend Logic
   Data thật từ /api/interview/config và /api/interview/questions
   ============================================================ */

/* =====================================================
   STATE
   ===================================================== */
let session = {
    role: '',
    roleKey: '',
    level: 'fresher',
    type: 'mixed',
    typeLabel: 'Tổng hợp',
    totalQ: 5,
    questions: [],   // câu hỏi load từ API (static mode)
    answers: [],
    feedbacks: [],
    current: 0,
    timer: null,
    timerSec: 180,
};

let cvState = { hasCV: false, cvName: '', cvSize: '', source: '' };

// Reference data từ API
let refRoles  = [];   // [{ roleKey, roleName, category, categoryKey, iconClass }]
let refLevels = [];   // [{ levelKey, levelName }]
let refTypes  = [];   // [{ typeKey, typeName, description, iconClass }]

/* =====================================================
   TIPS (tĩnh — không phải data nghiệp vụ)
   ===================================================== */
const TIPS = [
    'Hãy trả lời bằng kinh nghiệm thực tế. AI đánh giá cao câu trả lời có dẫn chứng cụ thể.',
    'Dùng cấu trúc STAR: Situation → Task → Action → Result để trả lời behavioral questions.',
    'Đưa con số cụ thể vào câu trả lời: "% tăng trưởng", "X người dùng", "giảm Y giây".',
    'Câu trả lời lý tưởng từ 150-300 từ — đủ chi tiết nhưng không dài dòng.',
    'Liên kết kinh nghiệm của bạn trực tiếp với yêu cầu của vị trí đang ứng tuyển.',
    'Thể hiện growth mindset: kể cả khi mô tả thất bại, hãy nêu bài học rút ra.',
    'Technical questions: giải thích như bạn đang nói chuyện với người không chuyên.',
];

/* =====================================================
   INIT — load config từ API rồi render UI
   ===================================================== */
async function initPage() {
    try {
        const res  = await fetch('/api/interview/config');
        const data = await res.json();
        refRoles  = data.roles  || [];
        refLevels = data.levels || [];
        refTypes  = data.types  || [];
    } catch (e) {
        console.error('Không load được config:', e);
        showConfigError();
        return;
    }

    renderRoleGrid();
    renderLevelButtons();
    renderTypeCards();
    initSetupEvents();
    updateRangeStyle();
    updateModeCards();
    loadSavedCvs();
}

function showConfigError() {
    const grid = document.getElementById('roleGrid');
    if (grid) grid.innerHTML = '<p style="color:#e74c3c;padding:12px">Không thể tải danh sách vị trí. Vui lòng tải lại trang.</p>';
}

/* =====================================================
   RENDER ROLE GRID (từ refRoles)
   ===================================================== */
let activeCat = 'all', roleSearch = '';

function renderRoleGrid(filter = activeCat, search = roleSearch) {
    const grid = document.getElementById('roleGrid');
    if (!grid) return;

    let filtered = refRoles;
    if (filter !== 'all') filtered = filtered.filter(r => r.categoryKey === filter);
    if (search.trim()) {
        const q = search.toLowerCase();
        filtered = filtered.filter(r =>
            r.roleName.toLowerCase().includes(q) ||
            r.category.toLowerCase().includes(q)
        );
    }

    if (filtered.length === 0) {
        grid.innerHTML = '<p style="color:#9ba8c0;padding:12px;font-size:13px">Không tìm thấy vị trí phù hợp.</p>';
        return;
    }

    grid.innerHTML = filtered.map(r => `
        <button class="mi-role-btn${session.roleKey === r.roleKey ? ' active' : ''}"
                data-role="${r.roleName}"
                data-role-key="${r.roleKey}">
            <i class="${r.iconClass || 'fa-solid fa-briefcase'}"></i> ${r.roleName}
        </button>
    `).join('');

    grid.querySelectorAll('.mi-role-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.mi-role-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            session.role    = btn.dataset.role;
            session.roleKey = btn.dataset.roleKey;
            const customInput = document.getElementById('customRole');
            if (customInput) customInput.value = '';
        });
    });
}

/* =====================================================
   RENDER LEVEL BUTTONS (từ refLevels)
   ===================================================== */
function renderLevelButtons() {
    const wrap = document.getElementById('levelButtons');
    if (!wrap || refLevels.length === 0) return;

    wrap.innerHTML = refLevels.map((l, i) => `
        <button class="mi-level-btn${i === 0 ? ' active' : ''}"
                data-level="${l.levelKey}">
            ${l.levelName}
        </button>
    `).join('');

    // Set default
    if (refLevels.length > 0) session.level = refLevels[0].levelKey;

    wrap.querySelectorAll('.mi-level-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            wrap.querySelectorAll('.mi-level-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            session.level = btn.dataset.level;
        });
    });
}

/* =====================================================
   RENDER TYPE CARDS (từ refTypes)
   ===================================================== */
function renderTypeCards() {
    const wrap = document.getElementById('typeCards');
    if (!wrap || refTypes.length === 0) return;

    wrap.innerHTML = refTypes.map((t, i) => `
        <label class="mi-type-card${i === 0 ? ' active' : ''}">
            <input type="radio" name="itype" value="${t.typeKey}"${i === 0 ? ' checked' : ''}>
            <div class="mi-type-icon"><i class="${t.iconClass || 'fa-solid fa-list'}"></i></div>
            <div>
                <strong>${t.typeName}</strong>
                <span>${t.description || ''}</span>
            </div>
        </label>
    `).join('');

    // Set default
    if (refTypes.length > 0) {
        session.type     = refTypes[0].typeKey;
        session.typeLabel= refTypes[0].typeName;
    }

    wrap.querySelectorAll('.mi-type-card').forEach(card => {
        card.addEventListener('click', () => {
            wrap.querySelectorAll('.mi-type-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            const radio = card.querySelector('input[name="itype"]');
            if (radio) {
                session.type      = radio.value;
                const found = refTypes.find(t => t.typeKey === radio.value);
                session.typeLabel = found ? found.typeName : radio.value;
            }
        });
    });
}

/* =====================================================
   SETUP EVENTS
   ===================================================== */
function initSetupEvents() {
    // Category tabs
    document.querySelectorAll('.mi-cat-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.mi-cat-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            activeCat = tab.dataset.cat;
            renderRoleGrid(activeCat, roleSearch);
        });
    });

    const roleSearchEl = document.getElementById('roleSearch');
    if (roleSearchEl) {
        roleSearchEl.addEventListener('input', e => {
            roleSearch = e.target.value;
            renderRoleGrid(activeCat, roleSearch);
        });
    }

    // Custom role input
    const customRole = document.getElementById('customRole');
    if (customRole) {
        customRole.addEventListener('input', e => {
            if (e.target.value.trim()) {
                document.querySelectorAll('.mi-role-btn').forEach(b => b.classList.remove('active'));
                session.role    = e.target.value.trim();
                session.roleKey = toRoleKey(session.role);
            }
        });
    }

    // Interviewer style selector
    document.querySelectorAll('.mi-style-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.mi-style-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            session.interviewStyle = btn.dataset.style;
        });
    });
    session.interviewStyle = 'standard'; // default

    // Range (số câu hỏi)
    const qCount = document.getElementById('qCount');
    const qCountLabel = document.getElementById('qCountLabel');
    if (qCount) {
        qCount.addEventListener('input', () => {
            if (qCountLabel) qCountLabel.textContent = qCount.value;
            session.totalQ = parseInt(qCount.value);
            updateRangeStyle();
        });
    }

    // CV upload
    initCvUpload();

    // Start button
    const startBtn = document.getElementById('startBtn');
    if (startBtn) {
        startBtn.addEventListener('click', () => {
            if (!session.role && !session.roleKey) {
                alert('Vui lòng chọn vị trí ứng tuyển trước khi bắt đầu.');
                return;
            }
            const mode = getSelectedMode();
            if (mode === 'ai') {
                startChatInterview();
            } else if (mode === 'voice') {
                startVoiceInterview();
            } else {
                startStaticInterview();
            }
        });
    }
}

function updateRangeStyle() {
    const qCount = document.getElementById('qCount');
    if (!qCount) return;
    const pct = ((qCount.value - qCount.min) / (qCount.max - qCount.min)) * 100;
    qCount.style.background = `linear-gradient(to right, #00BFA5 ${pct}%, #e4ecf5 ${pct}%)`;
}

/* =====================================================
   SAVED CVs — load từ API
   ===================================================== */
async function loadSavedCvs() {
    const container = document.getElementById('savedCvs');
    if (!container) return;

    try {
        const res  = await fetch('/api/interview/my-cvs');
        const data = await res.json();
        const cvs  = data.cvs || [];

        const noSaved = document.getElementById('noSavedCv');

        if (cvs.length === 0) {
            if (noSaved) noSaved.style.display = 'flex';
            return;
        }

        if (noSaved) noSaved.style.display = 'none';

        cvs.forEach(cv => {
            const updatedLabel = formatRelativeTime(cv.updatedAt);
            const templateInfo = cv.templateName ? cv.templateName + ' · ' : '';

            const item = document.createElement('div');
            item.className = 'mi-saved-cv-item';
            item.dataset.cvId = cv.id;
            item.innerHTML = `
                <div class="mi-scv-icon"><i class="fa-solid fa-file-lines"></i></div>
                <div class="mi-scv-info">
                    <strong>${escapeHtml(cv.cvName)}</strong>
                    <span>${templateInfo}Cập nhật ${updatedLabel}</span>
                </div>
                <button type="button" class="mi-scv-select">Dùng CV này</button>
            `;
            container.insertBefore(item, noSaved);

            item.querySelector('.mi-scv-select').addEventListener('click', () => {
                selectSavedCv(item, cv.cvName, templateInfo + 'Cập nhật ' + updatedLabel, cv.id);
            });
        });
    } catch (e) {
        // Không đăng nhập hoặc lỗi → hiện trạng thái trống bình thường
        const noSaved = document.getElementById('noSavedCv');
        if (noSaved) noSaved.style.display = 'flex';
    }
}

function selectSavedCv(item, cvName, cvDesc, cvId) {
    const cvUploadZone = document.getElementById('cvUploadZone');
    cvState = { hasCV: true, cvName, cvSize: 'Đang phân tích...', source: 'saved' };

    document.getElementById('cvUploadInner').style.display   = 'none';
    document.getElementById('cvSelectedState').style.display = 'flex';
    cvUploadZone?.classList.add('has-file');
    document.getElementById('cvFileName').textContent = cvName;
    document.getElementById('cvFileSize').innerHTML = '<span class="mi-cv-analyzing"><span class="mi-cv-spinner"></span>AI đang phân tích CV...</span>';

    document.querySelectorAll('.mi-saved-cv-item').forEach(i => i.classList.remove('selected'));
    item.classList.add('selected');
    const cvAiNote = document.getElementById('cvAiNote');
    if (cvAiNote) cvAiNote.style.display = 'flex';

    if (cvId) analyzeSavedCvAndLockForm(cvId, cvName, cvDesc);
}

async function analyzeSavedCvAndLockForm(cvId, cvName, cvDesc) {
    const sizeEl = document.getElementById('cvFileSize');
    try {
        const res  = await fetch(`/api/interview/analyze-cv/${cvId}`, { credentials: 'include' });
        if (!res.ok) throw new Error('API error');
        const data = await res.json();
        if (data.error) throw new Error(data.error);

        session.roleKey = data.roleKey || '';
        session.role    = data.roleName || '';
        session.level   = data.levelKey || 'fresher';
        session.type    = data.typeKey  || 'mixed';
        cvState.cvSummary = data.cvSummary || '';
        cvState.cvSize    = '✓ Đã chọn · ' + cvDesc;
        if (sizeEl) sizeEl.textContent = cvState.cvSize;

        // Auto-select role
        let matched = false;
        document.querySelectorAll('.mi-role-btn').forEach(btn => {
            const active = btn.dataset.roleKey === data.roleKey;
            btn.classList.toggle('active', active);
            if (active) matched = true;
        });
        if (!matched && data.roleName) {
            const customInput = document.getElementById('customRole');
            if (customInput) customInput.value = data.roleName;
        }

        // Auto-select level
        document.querySelectorAll('.mi-level-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.level === data.levelKey);
        });

        // Auto-select type
        document.querySelectorAll('.mi-type-card').forEach(card => {
            card.classList.toggle('active', card.dataset.type === data.typeKey);
            const radio = card.querySelector('input[type=radio]');
            if (radio) radio.checked = card.dataset.type === data.typeKey;
        });

        lockFormFields(true);

        const cvAiNote = document.getElementById('cvAiNote');
        if (cvAiNote) {
            cvAiNote.innerHTML = `<i class="fa-solid fa-wand-magic-sparkles"></i>
                <span>AI đã phân tích CV: <strong>${escapeHtml(data.roleName || data.roleKey)}</strong> · ${getLevelName(data.levelKey)} · Các trường đã được tự động điền. <button type="button" class="mi-unlock-btn" onclick="lockFormFields(false)">Chỉnh sửa</button></span>`;
            cvAiNote.style.display = 'flex';
        }
    } catch (e) {
        if (sizeEl) sizeEl.textContent = '✓ Đã chọn · ' + cvDesc;
        console.warn('Không thể phân tích CV đã lưu:', e);
    }
}

function formatRelativeTime(isoString) {
    if (!isoString) return 'vừa rồi';
    const diff = Date.now() - new Date(isoString).getTime();
    const mins  = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days  = Math.floor(diff / 86400000);
    if (mins  < 2)  return 'vừa rồi';
    if (mins  < 60) return `${mins} phút trước`;
    if (hours < 24) return `${hours} giờ trước`;
    if (days  < 7)  return `${days} ngày trước`;
    if (days  < 30) return `${Math.floor(days/7)} tuần trước`;
    return `${Math.floor(days/30)} tháng trước`;
}

function escapeHtml(str) {
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

/* =====================================================
   CV UPLOAD
   ===================================================== */
function initCvUpload() {
    const cvUploadZone = document.getElementById('cvUploadZone');
    const cvFileInput  = document.getElementById('cvFileInput');
    const cvBrowseBtn  = document.getElementById('cvBrowseBtn');
    const cvRemoveBtn  = document.getElementById('cvRemoveBtn');

    if (!cvUploadZone) return;

    cvBrowseBtn?.addEventListener('click', () => cvFileInput?.click());

    cvFileInput?.addEventListener('change', e => {
        if (e.target.files[0]) setCVFile(e.target.files[0]);
    });

    cvUploadZone.addEventListener('dragover', e => {
        e.preventDefault();
        cvUploadZone.classList.add('dragover');
    });
    cvUploadZone.addEventListener('dragleave', () => cvUploadZone.classList.remove('dragover'));
    cvUploadZone.addEventListener('drop', e => {
        e.preventDefault();
        cvUploadZone.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        if (file && /\.(pdf|doc|docx)$/i.test(file.name)) setCVFile(file);
    });

    cvRemoveBtn?.addEventListener('click', () => {
        cvState = { hasCV: false, cvName: '', cvSize: '', source: '' };
        document.getElementById('cvUploadInner').style.display  = 'flex';
        document.getElementById('cvSelectedState').style.display= 'none';
        cvUploadZone.classList.remove('has-file');
        const cvAiNote = document.getElementById('cvAiNote');
        if (cvAiNote) {
            cvAiNote.style.display = 'none';
            cvAiNote.innerHTML = `<i class="fa-solid fa-wand-magic-sparkles"></i>
                <span>AI đã đọc CV của bạn và sẽ tạo câu hỏi phù hợp với kinh nghiệm, kỹ năng và vị trí mong muốn.</span>`;
        }
        if (cvFileInput) cvFileInput.value = '';
        document.querySelectorAll('.mi-saved-cv-item').forEach(i => i.classList.remove('selected'));
        lockFormFields(false);
    });
}

function setCVFile(file) {
    const kb   = Math.round(file.size / 1024);
    const size = kb > 1024 ? `${(kb/1024).toFixed(1)} MB` : `${kb} KB`;
    cvState = { hasCV: true, cvName: file.name, cvSize: size + ' · Đang phân tích...', source: 'upload' };

    document.getElementById('cvFileName').textContent = cvState.cvName;
    document.getElementById('cvFileSize').innerHTML = '<span class="mi-cv-analyzing"><span class="mi-cv-spinner"></span>AI đang phân tích CV...</span>';
    document.getElementById('cvUploadInner').style.display   = 'none';
    document.getElementById('cvSelectedState').style.display = 'flex';
    document.getElementById('cvUploadZone').classList.add('has-file');
    const cvAiNote = document.getElementById('cvAiNote');
    if (cvAiNote) cvAiNote.style.display = 'none';
    document.querySelectorAll('.mi-saved-cv-item').forEach(i => i.classList.remove('selected'));

    // Gọi Gemini phân tích CV → auto-fill + lock form
    analyzeCvAndLockForm(file);
}

async function analyzeCvAndLockForm(file) {
    const sizeEl = document.getElementById('cvFileSize');
    try {
        const fd = new FormData();
        fd.append('file', file);
        const res  = await fetch('/api/interview/analyze-cv', { method: 'POST', body: fd });
        if (!res.ok) throw new Error('API error');
        const data = await res.json();

        if (data.error) throw new Error(data.error);

        // Cập nhật session state từ kết quả Gemini
        session.roleKey = data.roleKey || '';
        session.role    = data.roleName || '';
        session.level   = data.levelKey || 'fresher';
        session.type    = data.typeKey  || 'mixed';

        // Lưu context CV vào cvState
        cvState.cvSummary = data.cvSummary || '';
        cvState.cvSize = file.size > 1024*1024
            ? `${(file.size/1024/1024).toFixed(1)} MB · Đã phân tích`
            : `${Math.round(file.size/1024)} KB · Đã phân tích`;
        if (sizeEl) sizeEl.textContent = cvState.cvSize;

        // Auto-select role button
        let matched = false;
        document.querySelectorAll('.mi-role-btn').forEach(btn => {
            const active = btn.dataset.roleKey === data.roleKey;
            btn.classList.toggle('active', active);
            if (active) matched = true;
        });
        // Nếu không khớp roleKey nào → hiện trong custom input
        if (!matched && data.roleName) {
            const customInput = document.getElementById('customRole');
            if (customInput) customInput.value = data.roleName;
        }

        // Auto-select level
        document.querySelectorAll('.mi-level-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.level === data.levelKey);
        });

        // Auto-select type
        document.querySelectorAll('.mi-type-card').forEach(card => {
            card.classList.toggle('active', card.dataset.type === data.typeKey);
            const radio = card.querySelector('input[type=radio]');
            if (radio) radio.checked = card.dataset.type === data.typeKey;
        });

        // Lock tất cả các field (role, level, type)
        lockFormFields(true);

        // Hiện thông báo đã phân tích
        const cvAiNote = document.getElementById('cvAiNote');
        if (cvAiNote) {
            cvAiNote.innerHTML = `<i class="fa-solid fa-wand-magic-sparkles"></i>
                <span>AI đã phân tích CV: <strong>${escapeHtml(data.roleName || data.roleKey)}</strong> · ${getLevelName(data.levelKey)} · Các trường đã được tự động điền. <button type="button" class="mi-unlock-btn" onclick="lockFormFields(false)">Chỉnh sửa</button></span>`;
            cvAiNote.style.display = 'flex';
        }

    } catch (e) {
        if (sizeEl) sizeEl.textContent = cvState.cvName ? `${Math.round(file.size/1024)} KB · Đã sẵn sàng` : '';
        console.warn('Không thể phân tích CV:', e);
    }
}

function lockFormFields(locked) {
    // Chỉ lock phần Vị trí ứng tuyển (role grid, category tabs, role search, custom role)
    ['#catTabs', '#roleGrid', '#customRole'].forEach(sel => {
        const el = document.querySelector(sel);
        if (el) {
            el.style.pointerEvents = locked ? 'none' : '';
            el.style.opacity       = locked ? '0.5'  : '';
        }
    });
    const roleSearchEl = document.querySelector('#roleSearch');
    if (roleSearchEl) {
        roleSearchEl.disabled      = locked;
        roleSearchEl.style.opacity = locked ? '0.5' : '';
    }
}

function getLevelName(key) {
    const map = { fresher: 'Fresher', junior: 'Junior', middle: 'Middle', senior: 'Senior', lead: 'Lead/Manager' };
    return map[key] || key;
}

/* =====================================================
   MODE SELECTOR
   ===================================================== */
function updateModeCards() {
    document.querySelectorAll('input[name="mode"]').forEach(r => {
        const card = r.closest('.mi-mode-opt') || r.closest('.mi-mode-card');
        if (card) card.classList.toggle('selected', r.checked);
    });
}

document.querySelectorAll('input[name="mode"]').forEach(r => {
    r.addEventListener('change', updateModeCards);
});

function getSelectedMode() {
    const checked = document.querySelector('input[name="mode"]:checked');
    return checked ? checked.value : 'ai';
}

/* =====================================================
   STATIC MODE — load câu hỏi từ API
   ===================================================== */
async function startStaticInterview() {
    const loadingBtn = document.getElementById('startBtn');
    if (loadingBtn) loadingBtn.textContent = 'Đang tải...';

    try {
        const params = new URLSearchParams({
            typeKey:  session.type    || 'mixed',
            roleKey:  session.roleKey || '',
            limit:    session.totalQ  || 5,
        });
        const res  = await fetch(`/api/interview/questions?${params}`);
        const data = await res.json();

        session.questions = (data.questions || []).map(q => ({
            q:    q.question,
            hint: q.hint        || '',
            diff: q.difficulty  || 'medium',
            cat:  mapTypeToCat(q.typeKey),
            model:q.modelAnswer || '',
        }));

        if (session.questions.length === 0) {
            alert('Không có câu hỏi cho vị trí và loại này. Vui lòng chọn lại.');
            if (loadingBtn) loadingBtn.textContent = 'Bắt đầu phỏng vấn';
            return;
        }

    } catch (e) {
        console.error('Load questions error:', e);
        alert('Không thể tải câu hỏi. Vui lòng thử lại.');
        if (loadingBtn) loadingBtn.textContent = 'Bắt đầu phỏng vấn';
        return;
    }

    if (loadingBtn) loadingBtn.textContent = 'Bắt đầu phỏng vấn';

    session.answers   = [];
    session.feedbacks = [];
    session.current   = 0;

    document.getElementById('sidebarRole').textContent  = session.role;
    document.getElementById('sidebarLevel').textContent = session.level;
    document.getElementById('sidebarType').textContent  = session.typeLabel;

    buildQMap();
    showStep('stepInterview');
    renderQuestion();
}

function mapTypeToCat(typeKey) {
    const map = { hr: 'HR', technical: 'Kỹ thuật', behavioral: 'Behavioral', mixed: 'Tổng hợp' };
    return map[typeKey] || typeKey;
}

function buildQMap() {
    const map = document.getElementById('qMap');
    if (!map) return;
    map.innerHTML = '';
    session.questions.forEach((_, i) => {
        const dot = document.createElement('div');
        dot.className = 'mi-qmap-dot' + (i === 0 ? ' current' : '');
        dot.id = `qDot${i}`;
        dot.textContent = i + 1;
        map.appendChild(dot);
    });
}

/* =====================================================
   INTERVIEW STEP
   ===================================================== */
function renderQuestion() {
    const q     = session.questions[session.current];
    const idx   = session.current;
    const total = session.questions.length;

    document.getElementById('progressLabel').textContent = `Câu ${idx + 1} / ${total}`;
    document.getElementById('progressRole').textContent  = session.role;
    document.getElementById('progressFill').style.width  = `${((idx + 1) / total) * 100}%`;

    document.getElementById('qNumber').textContent    = `Câu hỏi ${String(idx + 1).padStart(2, '0')}`;
    document.getElementById('questionText').textContent= q.q;
    document.getElementById('qHint').textContent      = q.hint ? `Gợi ý: ${q.hint}` : '';
    document.getElementById('qBadge').textContent     = q.cat;
    document.getElementById('qDiff').textContent      = q.diff;

    const badge = document.getElementById('qBadge');
    badge.style.cssText = '';
    if (q.cat === 'Kỹ thuật') {
        badge.style.background = 'rgba(26,36,86,0.08)';
        badge.style.color = '#1a2456';
        badge.style.borderColor = 'rgba(26,36,86,0.15)';
    } else if (q.cat === 'Behavioral') {
        badge.style.background = 'rgba(139,92,246,0.08)';
        badge.style.color = '#6d28d9';
        badge.style.borderColor = 'rgba(139,92,246,0.18)';
    }

    document.getElementById('answerInput').value = '';
    document.getElementById('charCount').textContent = '0';
    document.getElementById('feedbackCard').style.display  = 'none';
    document.getElementById('loadingCard').style.display   = 'none';
    document.getElementById('submitBtn').disabled = false;

    document.querySelectorAll('.mi-qmap-dot').forEach((d, i) => {
        d.className = 'mi-qmap-dot';
        if (i < idx) d.className += ' done';
        if (i === idx) d.className += ' current';
    });

    document.getElementById('liveTipText').textContent = TIPS[idx % TIPS.length];
    startTimer();
}

document.getElementById('answerInput')?.addEventListener('input', function () {
    document.getElementById('charCount').textContent = this.value.length;
});

document.getElementById('clearBtn')?.addEventListener('click', () => {
    document.getElementById('answerInput').value = '';
    document.getElementById('charCount').textContent = '0';
});

document.getElementById('submitBtn')?.addEventListener('click', submitAnswer);

async function submitAnswer() {
    const answer = document.getElementById('answerInput').value.trim();
    if (!answer) {
        const input = document.getElementById('answerInput');
        input.style.borderColor = '#ff4444';
        setTimeout(() => input.style.borderColor = '', 1500);
        return;
    }

    stopTimer();
    document.getElementById('submitBtn').disabled = true;
    document.getElementById('loadingCard').style.display  = 'block';
    document.getElementById('feedbackCard').style.display = 'none';

    const q  = session.questions[session.current];
    const fb = scoreFeedback(answer, q);

    session.answers.push(answer);
    session.feedbacks.push(fb);

    setTimeout(() => {
        document.getElementById('loadingCard').style.display = 'none';
        renderFeedback(fb, q);
    }, 900);
}

/** Chấm điểm phía client cho static mode (không gọi AI) */
function scoreFeedback(answer, q) {
    const words     = answer.trim().split(/\s+/).filter(w => w).length;
    const hasStar   = /situation|task|action|result|tình huống|nhiệm vụ|kết quả/i.test(answer);
    const hasNumbers= /\d+/.test(answer);

    let score = 5;
    const good    = [];
    const improve = [];

    if (words >= 50)  { score += 1;   good.push('Câu trả lời có độ dài phù hợp.'); }
    if (hasStar)      { score += 1.5; good.push('Sử dụng cấu trúc rõ ràng (STAR).'); }
    if (hasNumbers)   { score += 0.5; good.push('Có số liệu / dẫn chứng cụ thể.'); }
    if (words < 30)   { score -= 1.5; improve.push('Câu trả lời còn quá ngắn, hãy mở rộng với ví dụ.'); }
    if (!hasStar && q.cat !== 'Kỹ thuật') improve.push('Thử dùng cấu trúc STAR để trả lời rõ hơn.');
    if (!hasNumbers)  improve.push('Bổ sung số liệu cụ thể để tăng độ thuyết phục.');
    if (words > 250)  improve.push('Câu trả lời hơi dài, hãy tóm gọn lại điểm chính.');
    if (good.length === 0) good.push('Đã đề cập được ý chính của câu hỏi.');

    score = Math.min(10, Math.max(3, Math.round(score * 10) / 10));
    return { score, good, improve, model: q.model || '' };
}

function renderFeedback(fb, q) {
    const scoreEl  = document.getElementById('scoreNum');
    const circle   = document.getElementById('scoreCircle');
    scoreEl.textContent = fb.score;

    if (fb.score >= 8)      { circle.style.borderColor = '#00BFA5'; scoreEl.style.color = '#00BFA5'; }
    else if (fb.score >= 6) { circle.style.borderColor = '#FF8C00'; scoreEl.style.color = '#FF8C00'; }
    else                    { circle.style.borderColor = '#ff4444'; scoreEl.style.color = '#ff4444'; }

    document.getElementById('feedbackGood').innerHTML    = fb.good.map(g => `<li>${g}</li>`).join('');
    document.getElementById('feedbackImprove').innerHTML = fb.improve.length
        ? fb.improve.map(i => `<li>${i}</li>`).join('')
        : '<li>Không có điểm cần cải thiện đặc biệt.</li>';

    const modelEl = document.getElementById('modelAnswer');
    if (modelEl) modelEl.textContent = fb.model || '';

    document.getElementById('feedbackCard').style.display = 'block';

    const dot = document.getElementById(`qDot${session.current}`);
    if (dot) { dot.className = 'mi-qmap-dot done'; dot.textContent = ''; }

    const nextBtn = document.getElementById('nextBtn');
    const isLast  = session.current === session.questions.length - 1;
    nextBtn.innerHTML = isLast
        ? 'Xem kết quả <i class="fa-solid fa-flag-checkered"></i>'
        : 'Câu tiếp theo <i class="fa-solid fa-arrow-right"></i>';
}

document.getElementById('nextBtn')?.addEventListener('click', () => {
    if (session.current === session.questions.length - 1) {
        showStaticResults();
    } else {
        session.current++;
        renderQuestion();
    }
});

// Timer
function startTimer() {
    stopTimer();
    session.timerSec = 180;
    updateTimerDisplay();
    session.timer = setInterval(() => {
        session.timerSec--;
        updateTimerDisplay();
        if (session.timerSec <= 0) stopTimer();
    }, 1000);
}

function stopTimer() {
    if (session.timer) { clearInterval(session.timer); session.timer = null; }
}

function updateTimerDisplay() {
    const m = Math.floor(session.timerSec / 60);
    const s = session.timerSec % 60;
    document.getElementById('timerVal').textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    document.getElementById('qTimer').className = 'mi-q-timer' + (session.timerSec <= 30 ? ' warning' : '');
}

document.getElementById('quitBtn')?.addEventListener('click', () => {
    if (confirm('Bạn có chắc muốn kết thúc phiên phỏng vấn không?')) {
        stopTimer();
        if (session.answers.length > 0) showStaticResults();
        else showStep('stepSetup');
    }
});

/* =====================================================
   STATIC RESULTS
   ===================================================== */
function showStaticResults() {
    stopTimer();

    const fbs        = session.feedbacks;
    const totalScore = fbs.length
        ? (fbs.reduce((s, f) => s + f.score, 0) / fbs.length).toFixed(1)
        : 0;
    const score = parseFloat(totalScore);

    document.getElementById('finalScore').textContent     = totalScore;
    document.getElementById('resultSubtitle').textContent = `${session.role} · ${session.level} · ${session.typeLabel}`;

    let tag = 'Cần cải thiện', tagBg = 'rgba(255,68,68,0.10)', tagColor = '#cc0000', tagBorder = 'rgba(255,68,68,0.2)';
    if (score >= 8.5) { tag = 'Xuất sắc';   tagBg = 'rgba(0,191,165,0.12)'; tagColor = '#007d6d'; tagBorder = 'rgba(0,191,165,0.25)'; }
    else if (score >= 7) { tag = 'Khá tốt'; tagBg = 'rgba(0,191,165,0.10)'; tagColor = '#007d6d'; tagBorder = 'rgba(0,191,165,0.22)'; }
    else if (score >= 5) { tag = 'Trung bình'; tagBg = 'rgba(255,140,0,0.10)'; tagColor = '#c95500'; tagBorder = 'rgba(255,140,0,0.22)'; }

    const tagEl = document.getElementById('scoreTag');
    tagEl.textContent = tag;
    tagEl.style.cssText = `background:${tagBg};color:${tagColor};border:1px solid ${tagBorder};
        display:inline-block;margin-top:6px;padding:4px 12px;border-radius:999px;font-size:13px;font-weight:700;`;

    // Category breakdown
    const catScores = {};
    session.questions.forEach((q, i) => {
        if (!catScores[q.cat]) catScores[q.cat] = [];
        if (fbs[i]) catScores[q.cat].push(fbs[i].score);
    });

    const colors    = { 'HR': '#4A7FFF', 'Kỹ thuật': '#1a2456', 'Behavioral': '#8b5cf6', 'Tổng hợp': '#00BFA5' };
    const breakdown = document.getElementById('breakdownList');
    if (breakdown) {
        breakdown.innerHTML = Object.entries(catScores).map(([cat, scores]) => {
            const avg   = (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1);
            const pct   = (avg / 10 * 100).toFixed(0);
            const color = colors[cat] || '#00BFA5';
            return `
                <div class="mi-breakdown-item">
                    <div class="mi-breakdown-label">
                        <span>${cat}</span>
                        <span style="color:${color};font-weight:800">${avg}/10</span>
                    </div>
                    <div class="mi-breakdown-bar">
                        <div class="mi-breakdown-fill" style="width:${pct}%;background:${color}"></div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // Summary stats
    const highScore = Math.max(...fbs.map(f => f.score));
    const lowScore  = Math.min(...fbs.map(f => f.score));
    const goodCount = fbs.filter(f => f.score >= 7).length;
    const summaryEl = document.getElementById('summaryStats');
    if (summaryEl) {
        summaryEl.innerHTML = `
            <div class="mi-stat-row"><span>Số câu trả lời</span><strong>${fbs.length} câu</strong></div>
            <div class="mi-stat-row"><span>Câu tốt nhất</span><strong style="color:#00BFA5">${highScore}/10</strong></div>
            <div class="mi-stat-row"><span>Câu thấp nhất</span><strong style="color:${lowScore < 5 ? '#cc0000' : '#FF8C00'}">${lowScore}/10</strong></div>
            <div class="mi-stat-row"><span>Câu đạt (≥7)</span><strong>${goodCount}/${fbs.length} câu</strong></div>
        `;
    }

    const rec = score >= 7
        ? 'Bạn đã thể hiện tốt! Hãy tập trung cải thiện các câu có điểm thấp hơn và luyện thêm kỹ thuật trả lời theo cấu trúc STAR.'
        : score >= 5
        ? 'Bạn có nền tảng tốt nhưng cần luyện thêm. Chú ý bổ sung số liệu cụ thể và mở rộng câu trả lời với ví dụ thực tế.'
        : 'Hãy luyện tập thêm! Tập trung vào cấu trúc STAR, bổ sung kinh nghiệm thực tế và các dẫn chứng cụ thể vào câu trả lời.';

    const recEl = document.getElementById('recommendation');
    if (recEl) recEl.innerHTML = `<strong><i class="fa-solid fa-wand-magic-sparkles"></i> Gợi ý:</strong> ${rec}`;

    // Q&A Review
    const reviewList = document.getElementById('reviewList');
    if (reviewList) {
        reviewList.innerHTML = session.questions.map((q, i) => {
            const fb  = fbs[i];
            if (!fb) return '';
            const cls = fb.score >= 7 ? 'score-good' : fb.score >= 5 ? 'score-mid' : 'score-low';
            return `
                <div class="mi-review-item">
                    <div class="mi-review-header" onclick="toggleReview(${i})">
                        <span class="mi-review-qnum">${i + 1}</span>
                        <span class="mi-review-qtxt">${q.q}</span>
                        <span class="mi-review-score ${cls}">${fb.score}/10</span>
                        <i class="fa-solid fa-chevron-down" style="color:#9aabb8;margin-left:8px;font-size:12px"></i>
                    </div>
                    <div class="mi-review-body" id="reviewBody${i}">
                        <div class="mi-review-answer">
                            <strong>Câu trả lời của bạn:</strong><br>
                            ${session.answers[i] || '(Không có câu trả lời)'}
                        </div>
                        <div class="mi-review-fb">
                            <strong>Điểm mạnh:</strong> ${fb.good.join(' • ')}<br>
                            <strong>Cần cải thiện:</strong> ${fb.improve.join(' • ') || 'Không có.'}
                        </div>
                        ${fb.model ? `<div class="mi-review-model"><strong>Câu trả lời mẫu:</strong><br>${fb.model}</div>` : ''}
                    </div>
                </div>
            `;
        }).join('');
    }

    showStep('stepResults');
}

function toggleReview(idx) {
    const body = document.getElementById(`reviewBody${idx}`);
    if (body) body.classList.toggle('open');
}

/* =====================================================
   RESULTS ACTIONS
   ===================================================== */
document.getElementById('retryBtn')?.addEventListener('click', () => {
    const mode = getSelectedMode();
    if (mode === 'ai') startChatInterview();
    else if (mode === 'voice') startVoiceInterview();
    else startStaticInterview();
});

document.getElementById('newSetupBtn')?.addEventListener('click', () => showStep('stepSetup'));

document.getElementById('saveBtn')?.addEventListener('click', () => {
    const score = document.getElementById('finalScore').textContent;
    alert(`✓ Đã lưu kết quả phiên phỏng vấn!\nVị trí: ${session.role}\nĐiểm tổng: ${score}/10`);
});

/* =====================================================
   HELPERS
   ===================================================== */
function showStep(stepId) {
    document.querySelectorAll('.mi-step').forEach(s => s.classList.remove('active'));
    document.getElementById(stepId)?.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toRoleKey(roleName) {
    return roleName
        .toLowerCase()
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9\s]/g, '')
        .trim()
        .replace(/\s+/g, '_');
}

/* =====================================================
   AI CHAT MODE (Claude API)
   ===================================================== */
const chatState = {
    sessionId: null,
    messages:  [],
    role: '', level: '', type: '',
    hasCV: false, cvContext: '',
    ttsEnabled: true, sttEnabled: false,
    recognition: null, isRecording: false, isSending: false,
    synth: window.speechSynthesis || null,
};

/* Vấn đáp = AI chat nhưng voice-first: TTS bật, STT bật tự động sau mỗi câu AI */
async function startVoiceInterview() {
    // Force-check toggles
    const ttsEl = document.getElementById('ttsEnabled');
    const sttEl = document.getElementById('sttEnabled');
    if (ttsEl) ttsEl.checked = true;
    if (sttEl) sttEl.checked = true;
    await startChatInterview();
    // Sau khi AI nói xong → tự bật mic
    const origSpeak = speak;
    window._voiceInterviewSpeak = function(text) {
        origSpeak(text);
        if (chatState.synth) {
            chatState.synth.addEventListener('voiceschanged', () => {}, { once: true });
            const check = setInterval(() => {
                if (!chatState.synth.speaking) {
                    clearInterval(check);
                    if (getSelectedMode() === 'voice' && !chatState.isSending) {
                        toggleVoiceInput();
                    }
                }
            }, 300);
        }
    };
}

async function startChatInterview() {
    const role           = session.role  || 'Vị trí chưa xác định';
    const roleKey        = session.roleKey || toRoleKey(role);
    const level          = session.level || 'fresher';
    const type           = session.type  || 'mixed';
    const interviewStyle = session.interviewStyle || 'standard';
    const hasCV          = cvState.hasCV || false;
    const cvName         = cvState.cvName || '';

    // Tạo session trước, rồi redirect
    const startBtn = document.getElementById('startBtn');
    if (startBtn) { startBtn.disabled = true; startBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang khởi tạo...'; }

    try {
        const startRes = await fetch('/api/interview/session/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ mode: 'ai', roleKey, levelKey: level, typeKey: type, interviewStyle, cvFileName: cvName, hasCv: hasCV }),
        });
        const d = startRes.ok ? await startRes.json() : {};
        const sessionId = d.sessionId || '';

        // Lưu cvSummary vào sessionStorage (quá dài cho URL param)
        if (cvState.cvSummary) {
            sessionStorage.setItem('interviewCvContext', cvState.cvSummary);
        }

        const params = new URLSearchParams({
            sessionId,
            role,
            roleKey,
            level,
            type,
            interviewStyle,
            hasCV: hasCV ? '1' : '0',
            cvName,
        });
        window.location.href = `/interview-chat.html?${params}`;
    } catch (err) {
        if (startBtn) { startBtn.disabled = false; startBtn.innerHTML = '<i class="fa-solid fa-play"></i> Bắt đầu phỏng vấn'; }
        alert('Không thể khởi tạo phiên. Vui lòng thử lại.');
    }
}

async function callChatAPI(messages) {
    const url  = chatState.sessionId
        ? `/api/interview/session/${chatState.sessionId}/chat`
        : '/api/interview/chat';
    const body = chatState.sessionId
        ? { messages, cvContext: chatState.cvContext }
        : { role: chatState.role, level: chatState.level, type: chatState.type,
            hasCV: chatState.hasCV, cvContext: chatState.cvContext, messages };

    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `HTTP ${res.status}`);
    }
    return (await res.json()).reply || '';
}

async function sendChatMessage() {
    if (chatState.isSending) return;
    const input = document.getElementById('chatInput');
    const text  = input?.value.trim();
    if (!text) return;

    chatState.isSending = true;
    document.getElementById('chatSendBtn').disabled = true;

    appendMessage('user', text);
    chatState.messages.push({ role: 'user', content: text });
    input.value = '';
    updateChatCharCount(0);
    showTyping(true);

    try {
        const reply = await callChatAPI(chatState.messages);
        chatState.messages.push({ role: 'assistant', content: reply });
        showTyping(false);
        appendMessage('assistant', reply);
        if (chatState.ttsEnabled) speak(reply);
    } catch (err) {
        showTyping(false);
        appendMessage('assistant', '⚠ Không thể kết nối AI. Kiểm tra API key và thử lại.');
        console.error(err);
    } finally {
        chatState.isSending = false;
        document.getElementById('chatSendBtn').disabled = false;
    }
}

function appendMessage(role, content) {
    const container = document.getElementById('chatMessages');
    if (!container) return;
    const isAI = role === 'assistant';
    const now  = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    const div  = document.createElement('div');
    div.className = `mi-msg ${isAI ? 'ai' : 'user'}`;
    div.innerHTML = `
        <div class="mi-msg-avatar"><i class="fa-solid ${isAI ? 'fa-brain' : 'fa-user'}"></i></div>
        <div>
            <div class="mi-msg-bubble">${escapeHtml(content)}</div>
            <div class="mi-msg-time">${now}</div>
        </div>`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function escapeHtml(str) {
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g,'<br>');
}

function showTyping(show) {
    const el = document.getElementById('typingIndicator');
    if (el) el.style.display = show ? 'flex' : 'none';
    if (show) {
        const c = document.getElementById('chatMessages');
        if (c) c.scrollTop = c.scrollHeight;
    }
}

function speak(text) {
    if (!chatState.synth) return;
    chatState.synth.cancel();
    const utter = new SpeechSynthesisUtterance(text);
    utter.lang = 'vi-VN';
    const viVoice = chatState.synth.getVoices().find(v => v.lang.startsWith('vi'));
    if (viVoice) utter.voice = viVoice;
    chatState.synth.speak(utter);
}

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

function initRecognition() {
    if (!SpeechRecognition) return null;
    const rec = new SpeechRecognition();
    rec.lang = 'vi-VN'; rec.continuous = false; rec.interimResults = true;
    rec.onresult = e => {
        let final = '', interim = '';
        for (let i = e.resultIndex; i < e.results.length; i++) {
            const t = e.results[i][0].transcript;
            if (e.results[i].isFinal) final += t; else interim += t;
        }
        const input = document.getElementById('chatInput');
        if (input) { input.value = final || interim; updateChatCharCount(input.value.length); }
    };
    rec.onend   = () => setRecordingState(false);
    rec.onerror = () => setRecordingState(false);
    return rec;
}

function toggleVoiceInput() {
    if (!SpeechRecognition) { alert('Trình duyệt không hỗ trợ nhận dạng giọng nói.'); return; }
    if (chatState.isRecording) {
        chatState.recognition?.stop();
        setRecordingState(false);
    } else {
        chatState.recognition = initRecognition();
        chatState.recognition?.start();
        setRecordingState(true);
    }
}

function setRecordingState(recording) {
    chatState.isRecording = recording;
    document.getElementById('voiceBtn')?.classList.toggle('recording', recording);
    const icon = document.getElementById('voiceIcon');
    if (icon) icon.className = recording ? 'fa-solid fa-stop' : 'fa-solid fa-microphone';
    document.getElementById('voiceStatus')?.classList.toggle('active', recording);
}

document.getElementById('voiceStopBtn')?.addEventListener('click', () => {
    chatState.recognition?.stop();
    setRecordingState(false);
});

async function endChatSession() {
    if (chatState.messages.length < 2) {
        alert('Hãy trả lời ít nhất một câu hỏi trước khi kết thúc phỏng vấn.');
        return;
    }
    chatState.synth?.cancel();
    if (chatState.isRecording) { chatState.recognition?.stop(); setRecordingState(false); }

    showStep('stepResults');
    const reviewList = document.getElementById('reviewList');
    if (reviewList) reviewList.innerHTML = '<p style="text-align:center;color:#6b7a99;padding:20px">Đang phân tích...</p>';

    try {
        const url  = chatState.sessionId
            ? `/api/interview/session/${chatState.sessionId}/end`
            : '/api/interview/end';
        const body = chatState.sessionId
            ? { messages: chatState.messages }
            : { role: chatState.role, level: chatState.level, messages: chatState.messages };

        const res  = await fetch(url, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
        const data = await res.json();

        if (data.raw) renderRawResults(data.raw);
        else          renderChatResults(data);
    } catch (err) {
        console.error(err);
        renderRawResults('Không thể lấy đánh giá từ AI. Vui lòng thử lại.');
    }
}

function renderChatResults(d) {
    const score = d.overallScore ?? 7;
    const label = d.scoreLabel  ?? 'Khá tốt';

    document.getElementById('finalScore') && (document.getElementById('finalScore').textContent = score);
    document.getElementById('scoreTag')   && (document.getElementById('scoreTag').textContent   = label);

    const subtitle = document.getElementById('resultSubtitle');
    if (subtitle) subtitle.textContent = `${chatState.role} · ${chatState.level} · AI Chat`;

    const breakdownList = document.getElementById('breakdownList');
    if (breakdownList && d.categories) {
        const cats = [
            { key:'communication',  label:'Giao tiếp',            icon:'fa-comments'  },
            { key:'knowledge',      label:'Kiến thức chuyên môn',  icon:'fa-book'      },
            { key:'problemSolving', label:'Giải quyết vấn đề',     icon:'fa-lightbulb' },
            { key:'attitude',       label:'Thái độ & văn hóa',     icon:'fa-star'      },
        ];
        breakdownList.innerHTML = cats.map(c => {
            const val   = d.categories[c.key] ?? 0;
            const color = val >= 8 ? '#00BFA5' : val >= 6 ? '#f59e0b' : '#e74c3c';
            return `
                <div class="mi-breakdown-row">
                    <span class="mi-bd-label"><i class="fa-solid ${c.icon}"></i> ${c.label}</span>
                    <div class="mi-bd-bar-wrap"><div class="mi-bd-bar" style="width:${val*10}%;background:${color}"></div></div>
                    <span class="mi-bd-score">${val}/10</span>
                </div>`;
        }).join('');
    }

    const summaryStats = document.getElementById('summaryStats');
    if (summaryStats) {
        const st = (d.strengths    || []).map(s => `<li><i class="fa-solid fa-check-circle" style="color:#00BFA5;margin-right:6px"></i>${escapeHtml(s)}</li>`).join('');
        const im = (d.improvements || []).map(s => `<li><i class="fa-solid fa-arrow-trend-up" style="color:#f59e0b;margin-right:6px"></i>${escapeHtml(s)}</li>`).join('');
        summaryStats.innerHTML =
            (st ? `<div class="mi-ss-section"><strong style="color:#0f1f3d;font-size:13px">Điểm mạnh</strong><ul style="margin:8px 0 0;padding-left:0;list-style:none;display:flex;flex-direction:column;gap:6px;font-size:13px">${st}</ul></div>` : '') +
            (im ? `<div class="mi-ss-section" style="margin-top:14px"><strong style="color:#0f1f3d;font-size:13px">Cần cải thiện</strong><ul style="margin:8px 0 0;padding-left:0;list-style:none;display:flex;flex-direction:column;gap:6px;font-size:13px">${im}</ul></div>` : '');
    }

    const recEl = document.getElementById('recommendation');
    if (recEl && d.recommendation) recEl.textContent = d.recommendation;

    const reviewList = document.getElementById('reviewList');
    if (reviewList) {
        let html = '', qCount = 0;
        chatState.messages.forEach((msg, i) => {
            if (msg.role === 'assistant') {
                qCount++;
                const answer = chatState.messages[i + 1];
                html += `
                    <div class="mi-review-item">
                        <div class="mi-review-header" onclick="toggleReview(${qCount})">
                            <span class="mi-review-num">Câu ${qCount}</span>
                            <span class="mi-review-q">${escapeHtml(msg.content.substring(0, 120))}${msg.content.length > 120 ? '...' : ''}</span>
                            <i class="fa-solid fa-chevron-down mi-review-chevron"></i>
                        </div>
                        <div class="mi-review-body" id="reviewBody${qCount}">
                            <div class="mi-review-section">
                                <strong>Câu trả lời của bạn:</strong>
                                <p>${answer ? escapeHtml(answer.content) : '<em>Chưa trả lời</em>'}</p>
                            </div>
                        </div>
                    </div>`;
            }
        });
        reviewList.innerHTML = html || '<p style="text-align:center;color:#6b7a99;padding:20px">Chưa có câu hỏi nào.</p>';
    }
}

function renderRawResults(text) {
    const reviewList = document.getElementById('reviewList');
    if (reviewList) reviewList.innerHTML = `<div style="padding:20px;background:#f8faff;border-radius:12px;line-height:1.7;white-space:pre-wrap;font-size:14px;color:#1a2456">${escapeHtml(text)}</div>`;
    document.getElementById('finalScore') && (document.getElementById('finalScore').textContent = '—');
}

// Chat input bindings
document.getElementById('chatSendBtn')?.addEventListener('click', sendChatMessage);
document.getElementById('chatInput')?.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendChatMessage(); }
});
document.getElementById('chatInput')?.addEventListener('input', e => updateChatCharCount(e.target.value.length));
document.getElementById('voiceBtn')?.addEventListener('click', toggleVoiceInput);
document.getElementById('chatEndBtn')?.addEventListener('click', () => {
    if (confirm('Kết thúc phỏng vấn và xem đánh giá?')) endChatSession();
});
document.getElementById('ttsEnabled')?.addEventListener('change', e => {
    chatState.ttsEnabled = e.target.checked;
    if (!e.target.checked) chatState.synth?.cancel();
});
document.getElementById('sttEnabled')?.addEventListener('change', e => { chatState.sttEnabled = e.target.checked; });

function updateChatCharCount(len) {
    const el = document.getElementById('chatCharCount');
    if (el) el.textContent = len;
}

/* =====================================================
   BOOT
   ===================================================== */
initPage();
