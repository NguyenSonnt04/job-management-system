// ═══════════════════════════════════════════════════════════
//  cv-editor-save.js  —  Save, PDF export, contacts modal
// ═══════════════════════════════════════════════════════════

let savedCvId         = null;
let templateName      = '';
let loadedCvName      = '';
let autoDownloadStarted = false;

// ── Save Modal ────────────────────────────────────────────────
function openSaveModal() {
    if (!currentCvJson) return;
    syncPreviewToCurrentCv();
    const modal   = document.getElementById('saveModal');
    const input   = document.getElementById('cvNameInput');
    const defaultName = currentCvJson.name ? `CV của ${currentCvJson.name}` : 'CV của tôi';
    if (input) input.value = savedCvId ? (currentCvJson._savedName || defaultName) : defaultName;
    if (modal) {
        if (modal.classList.contains !== undefined) modal.classList.add('show');
        else modal.style.display = 'flex';
    }
    setTimeout(() => input?.focus(), 80);
}

function closeSaveModal() {
    const modal = document.getElementById('saveModal');
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
            await html2pdf()
                .set({
                    margin:      0,
                    filename,
                    image:       { type: 'jpeg', quality: 0.98 },
                    html2canvas: { scale: 2, useCORS: true },
                    jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' }
                })
                .from(cvDoc)
                .save();
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

    saveModal?.addEventListener('click', e => { if (e.target === saveModal) closeSaveModal(); });
    cvNameInput?.addEventListener('keydown', e => {
        if (e.key === 'Enter')  confirmSave();
        if (e.key === 'Escape') closeSaveModal();
    });
});
