// ═══════════════════════════════════════════════════════════
//  cv-editor-save.js  —  Save, PDF export, contacts modal
// ═══════════════════════════════════════════════════════════

let savedCvId         = null;
let templateName      = '';
let loadedCvName      = '';
let autoDownloadStarted = false;

function setSaveButtonState(state = 'default') {
    const btnSave = document.getElementById('btnSaveCv');
    if (!btnSave) return;

    btnSave.classList.remove('saved');
    btnSave.disabled = false;

    if (state === 'saving') {
        btnSave.textContent = 'Đang lưu...';
        btnSave.disabled = true;
        return;
    }

    if (state === 'saved') {
        btnSave.textContent = 'Đã lưu';
        btnSave.classList.add('saved');
        return;
    }

    btnSave.textContent = 'Lưu CV';
}

// ── Save Modal ────────────────────────────────────────────────
function mountSaveModalToBody() {
    const modal = document.getElementById('saveModal');
    if (modal && modal.parentElement !== document.body) {
        document.body.appendChild(modal);
    }
}

function openSaveModal() {
    if (!currentCvJson) return;
    mountSaveModalToBody();
    syncPreviewToCurrentCv();
    const modal   = document.getElementById('saveModal');
    const input   = document.getElementById('cvNameInput');
    const defaultName = currentCvJson.name ? `CV của ${currentCvJson.name}` : 'CV của tôi';
    if (input) input.value = savedCvId ? (currentCvJson._savedName || defaultName) : defaultName;
    if (modal) {
        document.body.classList.add('save-modal-open');
        if (modal.classList.contains !== undefined) modal.classList.add('show');
        else modal.style.display = 'flex';
    }
    setTimeout(() => input?.focus(), 80);
}

function closeSaveModal() {
    const modal = document.getElementById('saveModal');
    document.body.classList.remove('save-modal-open');
    if (!modal) return;
    if (modal.classList.contains('show')) modal.classList.remove('show');
    else modal.style.display = 'none';
}

async function confirmSave() {
    const cvName = (document.getElementById('cvNameInput')?.value || '').trim()
        || (currentCvJson?.name ? `CV của ${currentCvJson.name}` : 'CV của tôi');

    syncPreviewToCurrentCv();
    closeSaveModal();
    syncDesignStateToCurrentCv();

    const btnSave = document.getElementById('btnSaveCv');
    if (btnSave) { btnSave.textContent = '⏳ Đang lưu...'; btnSave.disabled = true; }

    const params = new URLSearchParams(window.location.search);

    try {
        const url    = savedCvId ? `/api/user-cv/${savedCvId}` : '/api/user-cv/save';
        const method = savedCvId ? 'PUT' : 'POST';

        if (currentCvJson) {
            if (avatarDataUrl) currentCvJson.avatarUrl = avatarDataUrl;
            else delete currentCvJson.avatarUrl;
            delete currentCvJson.avatarDataUrl;
        }

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
            alert('Bạn cần đăng nhập để lưu CV.');
            if (btnSave) { btnSave.textContent = '💾 Lưu CV'; btnSave.disabled = false; }
            return;
        }

        if (!data.success) throw new Error(data.message || 'Lỗi lưu CV');

        if (data.id) savedCvId = data.id;
        loadedCvName = cvName;
        if (currentCvJson) currentCvJson._savedName = cvName;

        if (btnSave) { btnSave.textContent = '✅ Đã lưu'; btnSave.classList.add('saved'); btnSave.disabled = false; }
        captureEditorHistorySnapshot(true);

    } catch (e) {
        console.error('confirmSave:', e);
        if (btnSave) { btnSave.textContent = '💾 Lưu CV'; btnSave.disabled = false; }
        alert('Lỗi khi lưu CV: ' + e.message);
    }
}

// Alias
function saveCv() { openSaveModal(); }

// ── PDF Export ────────────────────────────────────────────────
function queueAutoPdfDownload() {
    const params = new URLSearchParams(window.location.search);
    if (autoDownloadStarted || params.get('download') !== 'pdf') return;
    autoDownloadStarted = true;

    window.setTimeout(async () => {
        const ok = await downloadPdf();
        if (ok && window.opener && params.get('source') === 'my-cvs') {
            window.setTimeout(() => window.close(), 900);
        }
    }, 700);
}

async function downloadPdf() {
    const wasEditMode = isEditMode;
    if (wasEditMode) { isEditMode = false; refreshEditModeUI(); }

    const preview = document.getElementById('cvPreview');
    const cvDoc   = preview?.firstElementChild;
    if (!cvDoc) { if (wasEditMode) { isEditMode = true; refreshEditModeUI(); } return false; }

    const name     = currentCvJson?.name || loadedCvName || 'CV';
    const filename = `CV_${name.replace(/\s+/g, '_')}.pdf`;
    const btn      = document.querySelector('.btn-pdf');
    const oldText  = btn?.innerHTML;
    if (btn) { btn.innerHTML = '⏳ Đang tạo...'; btn.disabled = true; }

    if (typeof html2pdf !== 'undefined') {
        try {
            // Sanitize unsupported CSS color functions for html2canvas
            const sanitized = [];
            cvDoc.querySelectorAll('*').forEach(el => {
                const cs = getComputedStyle(el);
                ['color', 'backgroundColor', 'borderColor', 'borderTopColor', 'borderRightColor', 'borderBottomColor', 'borderLeftColor'].forEach(prop => {
                    const val = cs[prop];
                    if (val && /^color\(/.test(val)) {
                        sanitized.push({ el, prop, original: el.style[prop] });
                        el.style[prop] = '#000000';
                    }
                });
            });

            await html2pdf()
                .set({
                    margin:      0,
                    filename,
                    image:       { type: 'jpeg', quality: 0.98 },
                    html2canvas: { scale: 2, useCORS: true, removeContainer: true },
                    jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' }
                })
                .from(cvDoc)
                .save();

            // Restore original styles
            sanitized.forEach(({ el, prop, original }) => { el.style[prop] = original; });
            return true;
        } catch (error) {
            console.error('downloadPdf:', error);
            alert('Có lỗi khi tải PDF. Vui lòng thử lại!');
            return false;
        } finally {
            if (btn) { btn.innerHTML = oldText; btn.disabled = false; }
            if (wasEditMode) { isEditMode = true; refreshEditModeUI(); }
        }
    } else {
        document.title = filename;
        window.print();
        if (btn) { btn.innerHTML = oldText; btn.disabled = false; }
        if (wasEditMode) { isEditMode = true; refreshEditModeUI(); }
        return true;
    }
}

// ── Custom Contacts Editor ────────────────────────────────────
const DEFAULT_CONTACT_ICONS = ['mail','phone','map-pin','globe','linkedin','github','twitter','facebook','instagram'];

function openContactsModal() {
    const modal = document.getElementById('contactsModal');
    if (!modal) return;
    renderContactsList();
    modal.style.display = 'flex';
}

function closeContactsModal() {
    const modal = document.getElementById('contactsModal');
    if (modal) modal.style.display = 'none';
}

function renderContactsList() {
    const container = document.getElementById('contactsListContainer');
    if (!container) return;
    const contacts = currentCvJson?.customContacts || [];

    container.innerHTML = contacts.length ? contacts.map((contact, idx) => `
        <div class="contact-item" data-index="${idx}">
            <select class="contact-icon-select" onchange="updateContact(${idx}, 'icon', this.value)">
                ${DEFAULT_CONTACT_ICONS.map(ic => `<option value="${ic}"${contact.icon===ic?' selected':''}>${ic}</option>`).join('')}
            </select>
            <input type="text" class="contact-label-input" value="${esc(contact.label||'')}" placeholder="Nhãn" oninput="updateContact(${idx},'label',this.value)">
            <input type="text" class="contact-value-input" value="${esc(contact.value||'')}" placeholder="Giá trị" oninput="updateContact(${idx},'value',this.value)">
            <button type="button" onclick="removeContact(${idx})" title="Xóa">✕</button>
        </div>`).join('') : '<div class="contacts-empty">Chưa có liên hệ tuỳ chỉnh.</div>';
}

function updateContact(idx, field, value) {
    if (!currentCvJson) return;
    if (!Array.isArray(currentCvJson.customContacts)) currentCvJson.customContacts = [];
    if (currentCvJson.customContacts[idx]) {
        currentCvJson.customContacts[idx][field] = value;
        markCvDirty();
    }
}

function addContact() {
    if (!currentCvJson) return;
    if (!Array.isArray(currentCvJson.customContacts)) currentCvJson.customContacts = [];
    currentCvJson.customContacts.push({ icon: 'globe', label: '', value: '' });
    renderContactsList();
    markCvDirty();
}

function removeContact(idx) {
    if (!currentCvJson?.customContacts) return;
    currentCvJson.customContacts.splice(idx, 1);
    renderContactsList();
    markCvDirty();
}

function saveContacts() {
    closeContactsModal();
    if (currentCvJson) renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

// ── Template switching ────────────────────────────────────────
function branchChangeTemplate() {
    if (currentCvJson) syncPreviewToCurrentCv();
    const params = new URLSearchParams(window.location.search);
    const cvId   = savedCvId || params.get('cvId') || '';
    window.location.href = `/cv-templates.html${cvId ? `?cvId=${cvId}` : ''}`;
}

// ── Wire save modal events (once DOM ready) ───────────────────
document.addEventListener('DOMContentLoaded', () => {
    const saveModal   = document.getElementById('saveModal');
    const cvNameInput = document.getElementById('cvNameInput');

    mountSaveModalToBody();

    saveModal?.addEventListener('click', e => { if (e.target === saveModal) closeSaveModal(); });
    cvNameInput?.addEventListener('keydown', e => {
        if (e.key === 'Enter')  confirmSave();
        if (e.key === 'Escape') closeSaveModal();
    });
});

window.setSaveButtonState = setSaveButtonState;

function persistSavedCvIdToUrl(cvId) {
    if (!cvId) return;
    const url = new URL(window.location.href);
    url.searchParams.set('cvId', cvId);
    if (!url.searchParams.get('template')) url.searchParams.delete('template');
    window.history.replaceState({}, '', url.toString());
}

function cacheSaveSuccessPayload(payload) {
    try {
        sessionStorage.setItem('cvSaveSuccessPayload', JSON.stringify(payload));
    } catch (error) {
        console.warn('cacheSaveSuccessPayload:', error);
    }
}

async function createCvPdfBlobForScoring() {
    const wasEditMode = isEditMode;
    if (wasEditMode) {
        isEditMode = false;
        refreshEditModeUI();
    }

    try {
        const preview = document.getElementById('cvPreview');
        const cvDoc = preview?.firstElementChild;
        if (!cvDoc || typeof html2pdf === 'undefined') return null;

        const worker = html2pdf()
            .set({
                margin: 0,
                image: { type: 'jpeg', quality: 0.98 },
                html2canvas: { scale: 2, useCORS: true },
                jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
            })
            .from(cvDoc)
            .toPdf();

        const pdf = await worker.get('pdf');
        return pdf.output('blob');
    } finally {
        if (wasEditMode) {
            isEditMode = true;
            refreshEditModeUI();
        }
    }
}

async function scoreCurrentCvForSuccessPage(cvId, cvName) {
    const pdfBlob = await createCvPdfBlobForScoring();
    if (!pdfBlob) return null;

    const safeName = String(cvName || currentCvJson?.name || 'CV')
        .replace(/[^\w\s-]+/g, '')
        .trim()
        .replace(/\s+/g, '_') || 'CV';

    const formData = new FormData();
    formData.append('file', new File([pdfBlob], `${safeName}.pdf`, { type: 'application/pdf' }));

    const res = await fetch('/api/cv-scoring/score', {
        method: 'POST',
        credentials: 'include',
        body: formData
    });

    const data = await res.json().catch(() => ({}));
    if (!res.ok || !data.success) {
        throw new Error(data.message || 'Không thể chấm điểm CV vừa lưu');
    }

    cacheSaveSuccessPayload({
        cvId,
        sessionId: data.sessionId || data.data?.id || null,
        cvName,
        templateName: templateName || '',
        scoreData: data.data || null,
        savedAt: new Date().toISOString()
    });

    return data;
}

function redirectToSaveSuccessPage(cvId, sessionId = '') {
    const url = new URL('/save-cv-success.html', window.location.origin);
    if (cvId) url.searchParams.set('cvId', cvId);
    if (sessionId) url.searchParams.set('sessionId', sessionId);
    window.location.href = url.toString();
}

confirmSave = async function confirmSavePatched() {
    const cvName = (document.getElementById('cvNameInput')?.value || '').trim()
        || (currentCvJson?.name ? `CV của ${currentCvJson.name}` : 'CV của tôi');

    if (!currentCvJson) return;

    syncPreviewToCurrentCv();
    closeSaveModal();
    if (typeof syncDesignStateToCurrentCv === 'function') syncDesignStateToCurrentCv();
    setSaveButtonState('saving');

    const params = new URLSearchParams(window.location.search);

    try {
        const url = savedCvId ? `/api/user-cv/${savedCvId}` : '/api/user-cv/save';
        const method = savedCvId ? 'PUT' : 'POST';

        if (avatarDataUrl) currentCvJson.avatarUrl = avatarDataUrl;
        else delete currentCvJson.avatarUrl;
        delete currentCvJson.avatarDataUrl;

        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                cvName,
                templateId: params.get('template') || null,
                templateName: templateName || '',
                cvContent: JSON.stringify(currentCvJson)
            })
        });

        const data = await res.json().catch(() => ({}));

        if (res.status === 401) {
            setSaveButtonState('default');
            alert('Bạn cần đăng nhập để lưu CV.');
            return;
        }

        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Lỗi lưu CV');
        }

        if (data.id) {
            savedCvId = data.id;
            persistSavedCvIdToUrl(data.id);
        }

        loadedCvName = cvName;
        currentCvJson._savedName = cvName;
        captureEditorHistorySnapshot(true);

        let scoreSessionId = null;

        try {
            const btnSave = document.getElementById('btnSaveCv');
            if (btnSave) {
                btnSave.textContent = 'Đang chấm điểm...';
                btnSave.disabled = true;
                btnSave.classList.remove('saved');
            }

            const scoring = await scoreCurrentCvForSuccessPage(savedCvId, cvName);
            scoreSessionId = scoring?.sessionId || scoring?.data?.id || null;
        } catch (scoreError) {
            console.error('scoreCurrentCvForSuccessPage:', scoreError);
            cacheSaveSuccessPayload({
                cvId: savedCvId,
                sessionId: null,
                cvName,
                templateName: templateName || '',
                scoreData: null,
                scoreError: scoreError.message || 'Không thể chấm điểm CV',
                savedAt: new Date().toISOString()
            });
        }

        setSaveButtonState('saved');
        redirectToSaveSuccessPage(savedCvId, scoreSessionId);
    } catch (error) {
        console.error('confirmSave:', error);
        setSaveButtonState('default');
        alert('Lỗi khi lưu CV: ' + error.message);
    }
};

window.confirmSave = confirmSave;
