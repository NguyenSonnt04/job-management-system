// ═══════════════════════════════════════════════════════════
//  cv-editor-state.js
//  Global state, design state, undo/redo, and utility helpers
// ═══════════════════════════════════════════════════════════

// ── Global CV State ─────────────────────────────────────────
let currentCvJson = null;
let avatarDataUrl = null;
let isEditMode = false;

// Reference to templateData / cvData (may be set by outer page)
window.currentCvJson = null;
window.avatarDataUrl = null;
window.isEditMode = false;

const cvData = {};
const templateData = {};

// ── Section Catalog ──────────────────────────────────────────
const sectionCatalog = [
    'contacts',
    'summary',
    'experience',
    'education',
    'skills',
    'projects',
    'awards',
    'certifications',
    'activities',
    'references',
    'hobbies'
];

const sectionMeta = {
    contacts:       { icon: 'contact-round',     glyph: '', label: 'Th\u00f4ng tin li\u00ean h\u1ec7', layoutLabel: 'Th\u00f4ng tin c\u00e1 nh\u00e2n', desc: 'Email, s\u1ed1 \u0111i\u1ec7n tho\u1ea1i, \u0111\u1ecba ch\u1ec9 v\u00e0 li\u00ean k\u1ebft c\u1ee7a b\u1ea1n.' },
    profileCard:    { icon: 'id-card',           glyph: '', label: 'Danh thiếp',              layoutLabel: 'Danh thiếp',              desc: 'Khối họ tên, chức danh và liên hệ ở phần đầu CV.' },
    avatar:         { icon: 'image',             glyph: '', label: 'Ảnh đại diện',            layoutLabel: 'Ảnh đại diện',            desc: 'Ảnh hồ sơ ở phần đầu CV.' },
    summary:        { icon: 'file-pen-line',     glyph: '', label: 'Mục tiêu nghề nghiệp',    layoutLabel: 'Mục tiêu nghề nghiệp',    desc: 'Phần giới thiệu ngắn về định hướng.' },
    experience:     { icon: 'briefcase-business',glyph: '', label: 'Kinh nghiệm làm việc',    layoutLabel: 'Kinh nghiệm làm việc',    desc: 'Các vị trí và thành tựu nổi bật.' },
    education:      { icon: 'graduation-cap',    glyph: '', label: 'Học vấn',                 layoutLabel: 'Học vấn',                 desc: 'Trường học, bằng cấp và thời gian.' },
    skills:         { icon: 'sparkles',          glyph: '', label: 'Kỹ năng',                 layoutLabel: 'Kỹ năng',                 desc: 'Các nhóm kỹ năng chuyên môn.' },
    projects:       { icon: 'rocket',            glyph: '', label: 'Dự án',                   layoutLabel: 'Dự án',                   desc: 'Dự án cá nhân hoặc công việc nổi bật.' },
    certifications: { icon: 'scroll-text',       glyph: '', label: 'Chứng chỉ',               layoutLabel: 'Chứng chỉ',               desc: 'Chứng chỉ nghề nghiệp liên quan.' },
    awards:         { icon: 'trophy',            glyph: '', label: 'Giải thưởng',             layoutLabel: 'Giải thưởng',             desc: 'Thành tích, giải thưởng và ghi nhận.' },
    activities:     { icon: 'target',            glyph: '', label: 'Hoạt động',               layoutLabel: 'Hoạt động',               desc: 'CLB, tình nguyện hoặc ngoại khóa.' }
    , references:   { icon: 'users',             glyph: '', label: 'Ng\u01b0\u1eddi tham chi\u1ebfu', layoutLabel: 'Ng\u01b0\u1eddi tham chi\u1ebfu', desc: 'Ng\u01b0\u1eddi c\u00f3 th\u1ec3 x\u00e1c nh\u1eadn kinh nghi\u1ec7m ho\u1eb7c n\u0103ng l\u1ef1c c\u1ee7a b\u1ea1n.' },
    hobbies:        { icon: 'heart',             glyph: '', label: 'S\u1edf th\u00edch',      layoutLabel: 'S\u1edf th\u00edch',      desc: 'Nh\u1eefng s\u1edf th\u00edch c\u00e1 nh\u00e2n ph\u00f9 h\u1ee3p v\u1edbi h\u1ed3 s\u01a1.' }
};

const defaultSectionOrder = [...sectionCatalog];

// ── Design State ─────────────────────────────────────────────
const designState = {
    font: "'Inter', sans-serif",
    fontSize: 100,
    lineSpacing: 1.5,
    color: '#2E3B8E',
    background: 'solid',
    sections: {
        contacts: true,
        summary: true,
        experience: true,
        education: true,
        skills: true,
        projects: false,
        awards: false,
        certifications: false,
        activities: false,
        references: false,
        hobbies: false
    },
    order: [...defaultSectionOrder],
    sectionTitles: {}
};

// ── Editor Undo/Redo History ──────────────────────────────────
let editorHistory = [];
let editorHistoryIndex = -1;
let editorHistorySeeded = false;
let editorHistoryTimer = null;
let isRestoringEditorHistory = false;

// ── Layout Studio State ───────────────────────────────────────
let layoutSelectedSection = 'summary';

// ── Icon Picker State ─────────────────────────────────────────
let sectionIconPickerState = { sectionKey: null, selectedIcon: null };
let previewIconPickerState = { iconKey: null, selectedIcon: null, label: '' };

// ── Utility: escape HTML ──────────────────────────────────────
function esc(str) {
    return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ── Design State Helpers ──────────────────────────────────────
function cloneDesignState() {
    return JSON.parse(JSON.stringify(designState));
}

function hydrateDesignState(savedState = {}) {
    if (!savedState || typeof savedState !== 'object') return;
    designState.font        = savedState.font        || designState.font;
    designState.fontSize    = Number(savedState.fontSize    || designState.fontSize);
    designState.lineSpacing = Number(savedState.lineSpacing || designState.lineSpacing);
    designState.color       = savedState.color       || designState.color;
    designState.background  = savedState.background  || designState.background;
    designState.sections    = { ...designState.sections, ...(savedState.sections || {}) };
    designState.order       = sanitizeSectionOrder(savedState.order || designState.order);
    designState.sectionTitles = { ...(savedState.sectionTitles || {}) };
    if (savedState.sectionIcons)  designState.sectionIcons  = { ...savedState.sectionIcons };
    if (savedState.previewIcons)  designState.previewIcons  = { ...savedState.previewIcons };
}

function hasTextContent(value) {
    return String(value || '').trim().length > 0;
}

function arrayHasContent(items, fields = []) {
    if (!Array.isArray(items) || !items.length) return false;
    return items.some(item => {
        if (typeof item === 'string') return hasTextContent(item);
        if (!item || typeof item !== 'object') return false;
        return fields.some(field => {
            const value = item[field];
            if (Array.isArray(value)) return value.some(hasTextContent);
            return hasTextContent(value);
        });
    });
}

function inferSectionVisibilityFromCv(cv = currentCvJson || {}) {
    const customContacts = Array.isArray(cv?.customContacts) ? cv.customContacts : [];
    return {
        contacts: ['email', 'phone', 'address', 'linkedin', 'portfolio', 'github', 'website', 'zalo']
            .some(key => hasTextContent(cv?.[key])) || customContacts.length > 0,
        summary: hasTextContent(cv?.summary),
        experience: arrayHasContent(cv?.experience, ['role', 'company', 'period', 'details']),
        education: arrayHasContent(cv?.education, ['degree', 'school', 'location', 'period', 'details']),
        skills: arrayHasContent(cv?.skills, ['category', 'items']),
        projects: arrayHasContent(cv?.projects, ['name', 'period', 'tech', 'github', 'details']),
        awards: arrayHasContent(cv?.awards, ['name', 'raw', 'year']),
        certifications: arrayHasContent(cv?.certifications, ['name', 'raw', 'issuer', 'year']),
        activities: arrayHasContent(cv?.activities, ['name', 'raw', 'role', 'period', 'details']),
        references: arrayHasContent(cv?.references, ['name', 'role', 'company', 'contact']),
        hobbies: arrayHasContent(cv?.hobbies, ['name', 'description'])
    };
}

function syncSectionStateFromCv(cv = currentCvJson || {}) {
    const savedSections = cv?._designState?.sections || {};
    const inferred = inferSectionVisibilityFromCv(cv);
    sectionCatalog.forEach(sectionName => {
        if (Object.prototype.hasOwnProperty.call(savedSections, sectionName)) return;
        designState.sections[sectionName] = inferred[sectionName];
    });
}

function syncDesignStateToCurrentCv() {
    const nextState = cloneDesignState();
    const syncedTargets = new Set();

    [currentCvJson, templateData, cvData].forEach(target => {
        if (!target || typeof target !== 'object' || syncedTargets.has(target)) return;
        target._designState = JSON.parse(JSON.stringify(nextState));
        target._accent = designState.color;
        syncedTargets.add(target);
    });
}

// ── Section Order Helpers ─────────────────────────────────────
function sanitizeSectionOrder(order = []) {
    const seen = new Set();
    const normalized = [];
    (Array.isArray(order) ? order : []).forEach(sectionName => {
        if (sectionCatalog.includes(sectionName) && !seen.has(sectionName)) {
            seen.add(sectionName);
            normalized.push(sectionName);
        }
    });
    defaultSectionOrder.forEach(sectionName => {
        if (!seen.has(sectionName)) normalized.push(sectionName);
    });
    return normalized;
}

function getOrderedSections() {
    return sanitizeSectionOrder(designState.order);
}

function sectionEnabled(sectionName) {
    return designState.sections[sectionName] !== false;
}

function getOrderedUsedSections() {
    return getOrderedSections().filter(sectionEnabled);
}

function getOrderedUnusedSections() {
    return getOrderedSections().filter(sectionName => !sectionEnabled(sectionName));
}

function mergeVisibleSectionOrder(visibleOrder = []) {
    const fullOrder = getOrderedSections();
    const orderedVisible = sanitizeSectionOrder(visibleOrder).filter(sectionEnabled);
    const visibleSet = new Set(sectionCatalog.filter(sectionEnabled));
    let nextVisibleIndex = 0;
    return fullOrder.map(sectionName => {
        if (!visibleSet.has(sectionName)) return sectionName;
        const replacement = orderedVisible[nextVisibleIndex];
        nextVisibleIndex += 1;
        return replacement || sectionName;
    });
}

// ── Icon State Helpers ────────────────────────────────────────
function getSectionIconName(sectionKey) {
    if (!sectionKey || !sectionMeta[sectionKey]) return '';
    if (typeof designState === 'object' && designState?.sectionIcons?.[sectionKey]) return designState.sectionIcons[sectionKey];
    return sectionMeta[sectionKey].icon || '';
}

function getPreviewIconName(iconKey, fallbackIcon) {
    if (!iconKey) return fallbackIcon;
    if (typeof designState === 'object' && designState?.previewIcons?.[iconKey]) return designState.previewIcons[iconKey];
    return fallbackIcon;
}

// ── markCvDirty / Undo / Redo ─────────────────────────────────
function syncEditorHistoryButtons() {
    const undoBtn = document.getElementById('btnUndoCv');
    const redoBtn = document.getElementById('btnRedoCv');
    const canUndo = editorHistoryIndex > 0;
    const canRedo = editorHistoryIndex >= 0 && editorHistoryIndex < editorHistory.length - 1;
    if (undoBtn) undoBtn.disabled = !canUndo;
    if (redoBtn) redoBtn.disabled = !canRedo;
}

function snapshotEditorState() {
    if (!currentCvJson) return '';
    return JSON.stringify({ cvJson: currentCvJson, designState: cloneDesignState() });
}

function resetEditorHistory() {
    editorHistory = [];
    editorHistoryIndex = -1;
    editorHistorySeeded = false;
    clearTimeout(editorHistoryTimer);
    syncEditorHistoryButtons();
}

function captureEditorHistorySnapshot(force = false) {
    if (!currentCvJson || isRestoringEditorHistory) return;
    const snapshot = snapshotEditorState();
    if (!snapshot) return;
    if (!force && editorHistoryIndex >= 0 && editorHistory[editorHistoryIndex] === snapshot) {
        syncEditorHistoryButtons();
        return;
    }
    if (editorHistoryIndex < editorHistory.length - 1) {
        editorHistory = editorHistory.slice(0, editorHistoryIndex + 1);
    }
    editorHistory.push(snapshot);
    if (editorHistory.length > 60) editorHistory.shift();
    editorHistoryIndex = editorHistory.length - 1;
    editorHistorySeeded = true;
    syncEditorHistoryButtons();
}

function queueEditorHistorySnapshot() {
    clearTimeout(editorHistoryTimer);
    editorHistoryTimer = setTimeout(() => captureEditorHistorySnapshot(false), 240);
}

function restoreEditorHistorySnapshot(snapshot) {
    if (!snapshot) return;
    try {
        const parsed = JSON.parse(snapshot);
        isRestoringEditorHistory = true;
        currentCvJson = parsed.cvJson || currentCvJson;
        if (currentCvJson) Object.assign(cvData, parsed.cvJson || {});
        hydrateDesignState(parsed.designState || {});
        renderCvPreview(currentCvJson);
        syncDesignControls();
        markCvDirty({ history: false });
    } finally {
        isRestoringEditorHistory = false;
        syncEditorHistoryButtons();
    }
}

function undoCvChange() {
    if (editorHistoryIndex <= 0) return;
    editorHistoryIndex -= 1;
    restoreEditorHistorySnapshot(editorHistory[editorHistoryIndex]);
}

function redoCvChange() {
    if (editorHistoryIndex < 0 || editorHistoryIndex >= editorHistory.length - 1) return;
    editorHistoryIndex += 1;
    restoreEditorHistorySnapshot(editorHistory[editorHistoryIndex]);
}

function markCvDirty(options = {}) {
    const btnSave = document.getElementById('btnSaveCv');
    if (btnSave) {
        btnSave.textContent = '💾 Lưu CV';
        btnSave.classList.remove('saved');
        btnSave.disabled = false;
    }
    if (options.history === false || !currentCvJson) {
        syncEditorHistoryButtons();
        return;
    }
    if (options.immediate) captureEditorHistorySnapshot(false);
    else queueEditorHistorySnapshot();
}

// Expose to window for IIFEs that reference it
window.markCvDirty = markCvDirty;

markCvDirty = function markCvDirtyPatched(options = {}) {
    if (typeof setSaveButtonState === 'function') setSaveButtonState('default');
    else {
        const btnSave = document.getElementById('btnSaveCv');
        if (btnSave) {
            btnSave.textContent = 'Lưu CV';
            btnSave.classList.remove('saved');
            btnSave.disabled = false;
        }
    }

    if (options.history === false || !currentCvJson) {
        syncEditorHistoryButtons();
        return;
    }

    if (options.immediate) captureEditorHistorySnapshot(false);
    else queueEditorHistorySnapshot();
};

window.markCvDirty = markCvDirty;
