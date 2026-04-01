// ═══════════════════════════════════════════════════════════
//  cv-editor-init.js  —  Bootstrap: DOM wiring & URL routing
// ═══════════════════════════════════════════════════════════

function cloneCvEditorData(value, fallback = {}) {
    if (value == null) {
        return Array.isArray(fallback) ? [...fallback] : { ...fallback };
    }
    try {
        return JSON.parse(JSON.stringify(value));
    } catch (error) {
        console.warn('[cv-editor-init] clone data error:', error);
        return Array.isArray(fallback) ? [...fallback] : { ...fallback };
    }
}

function parseCvEditorJson(value, fallback = null) {
    if (!value) return fallback;
    if (typeof value === 'object') return cloneCvEditorData(value, fallback || {});
    try {
        return JSON.parse(value);
    } catch (error) {
        console.warn('[cv-editor-init] parse JSON error:', error);
        return fallback;
    }
}

function buildTemplateAppliedCv(sourceCv, templateRecord) {
    const parsedSource = parseCvEditorJson(sourceCv?.cvContent, {});
    if (!parsedSource || typeof parsedSource !== 'object') return null;

    const accent = templateRecord?.previewColor
        || parsedSource._accent
        || parsedSource._designState?.color
        || '#1f4b99';
    const styleTag = normalizeCvStyle(templateRecord?.styleTag || parsedSource._styleTag || 'professional');
    const sourceDesignState = parsedSource._designState || {};
    const mergedDesignState = { color: accent };

    if (sourceDesignState.sections && typeof sourceDesignState.sections === 'object') {
        mergedDesignState.sections = cloneCvEditorData(sourceDesignState.sections, {});
    }
    if (Array.isArray(sourceDesignState.order)) {
        mergedDesignState.order = [...sourceDesignState.order];
    }
    if (sourceDesignState.sectionIcons && typeof sourceDesignState.sectionIcons === 'object') {
        mergedDesignState.sectionIcons = cloneCvEditorData(sourceDesignState.sectionIcons, {});
    }
    if (sourceDesignState.previewIcons && typeof sourceDesignState.previewIcons === 'object') {
        mergedDesignState.previewIcons = cloneCvEditorData(sourceDesignState.previewIcons, {});
    }

    return {
        ...parsedSource,
        _styleTag: styleTag,
        _accent: accent,
        _designState: mergedDesignState
    };
}

async function initCvEditor() {
    // Icons
    if (window.lucide) lucide.createIcons();

    // Bind editable events
    bindPreviewEditableEvents();

    // Default panel
    switchDesignPanel('design');
    renderSectionManager();
    syncDesignControls();

    const params          = new URLSearchParams(window.location.search);
    const templateId      = params.get('template');
    const loadCvId        = params.get('cvId');
    const autoDownloadPdf = params.get('download') === 'pdf';
    const isPdfExportMode = autoDownloadPdf;
    const creationMethod  = params.get('method') || '';
    const sourceCvId      = params.get('sourceCvId');

    if (isPdfExportMode) {
        document.body.classList.add('pdf-export-mode');
        document.title = 'CV Export';
        const previewColumn = document.querySelector('.preview-column');
        if (previewColumn) {
            previewColumn.classList.remove('preview-column');
            previewColumn.classList.add('pdf-export-column');
        }
    }

    // ── Load existing saved CV ────────────────────────────────
    if (loadCvId) {
        try {
            const res = await fetch(`/api/user-cv/${loadCvId}`);
            if (res.ok) {
                const data = await res.json();
                if (data.cvContent) {
                    const parsed = JSON.parse(data.cvContent);
                    savedCvId    = data.id;
                    loadedCvName = data.cvName || '';
                    parsed.name  = parsed.name || loadedCvName;
                    avatarDataUrl = parsed.avatarUrl || parsed.avatarDataUrl || null;
                    window.avatarDataUrl = avatarDataUrl;
                    hydrateDesignState(parsed._designState || { color: parsed._accent });
                    window.currentTemplate = {
                        previewColor: parsed._accent || parsed._designState?.color || designState.color || '#1f4b99',
                        styleTag:     normalizeCvStyle(parsed._styleTag || 'professional'),
                        name:         data.templateName || ''
                    };
                    parsed._styleTag = normalizeCvStyle(parsed._styleTag || 'professional');
                    templateName     = data.templateName || '';
                    resetEditorHistory();
                    renderCvPreview(parsed);
                    captureEditorHistorySnapshot(true);
                    enableEditorToolbar();
                    if (typeof setSaveButtonState === 'function') setSaveButtonState('saved');
                    if (autoDownloadPdf) queueAutoPdfDownload();
                    return;
                }
            }
        } catch (e) {
            console.error('[cv-editor-init] load existing CV error:', e);
        }
    }

    // ── Fetch template ────────────────────────────────────────
    if (templateId && creationMethod === 'existing_cv') {
        try {
            const [templateRes, userCvListRes] = await Promise.all([
                fetch(`/api/cv-templates/${templateId}`),
                fetch('/api/user-cv')
            ]);

            if (templateRes.ok) {
                const tpl = await templateRes.json();
                templateName = tpl.name || '';
                window.currentTemplate = {
                    previewColor: tpl.previewColor || '#1f4b99',
                    styleTag: normalizeCvStyle(tpl.styleTag || 'professional'),
                    name: tpl.name || ''
                };

                if (userCvListRes.ok) {
                    const userCvList = await userCvListRes.json();
                    const selectedSourceCv = Array.isArray(userCvList)
                        ? (userCvList.find(item => String(item.id) === String(sourceCvId)) || userCvList[0])
                        : null;

                    if (selectedSourceCv?.id) {
                        const sourceCvRes = await fetch(`/api/user-cv/${selectedSourceCv.id}`);
                        if (sourceCvRes.ok) {
                            const sourceCv = await sourceCvRes.json();
                            const parsed = buildTemplateAppliedCv(sourceCv, tpl);

                            if (parsed) {
                                savedCvId = null;
                                loadedCvName = sourceCv.cvName || '';
                                avatarDataUrl = parsed.avatarUrl || parsed.avatarDataUrl || null;
                                window.avatarDataUrl = avatarDataUrl;
                                hydrateDesignState(parsed._designState || { color: parsed._accent });
                                resetEditorHistory();
                                renderCvPreview(parsed);
                                captureEditorHistorySnapshot(true);
                                enableEditorToolbar();
                                if (typeof setSaveButtonState === 'function') setSaveButtonState('default');
                                return;
                            }
                        }
                    }
                }
            }
        } catch (e) {
            console.error('[cv-editor-init] apply existing CV template error:', e);
        }
    }

    if (templateId) {
        try {
            const res = await fetch(`/api/cv-templates/${templateId}`);
            if (res.ok) {
                const tpl = await res.json();
                templateName = tpl.name || '';
                window.currentTemplate = {
                    previewColor: tpl.previewColor || '#1f4b99',
                    styleTag:     normalizeCvStyle(tpl.styleTag || 'professional'),
                    name:         tpl.name || ''
                };
                if (tpl.templateContent) {
                    try {
                        const parsed = JSON.parse(tpl.templateContent);
                        resetEditorHistory();
                        renderCvPreview(parsed);
                        captureEditorHistorySnapshot(true);
                        enableEditorToolbar();
                        if (typeof setSaveButtonState === 'function') setSaveButtonState('default');
                        return;
                    } catch {}
                }
            }
        } catch (e) {
            console.error('[cv-editor-init] fetch template error:', e);
        }
    }

    // ── Fallback skeleton if no data ──────────────────────────
    const skeletonData = {
        name: 'HỌ TÊN ỨNG VIÊN', subtitle: 'Vị trí công việc mong muốn',
        email: 'email@example.com', phone: '0901 234 567', address: 'Hà Nội',
        summary: 'Nhấn để nhập mục tiêu nghề nghiệp của bạn.',
        experience: [{ role: 'Vị trí', company: 'Công ty', period: '2024 - Hiện tại', details: ['Mô tả công việc'] }],
        education:  [{ degree: 'Bằng cấp', school: 'Trường', period: '2020 - 2024' }],
        skills:     [{ category: 'Kỹ năng', items: ['Kỹ năng 1', 'Kỹ năng 2'] }],
        projects: [], certifications: [], awards: [], activities: []
    };
    window.currentTemplate = window.currentTemplate || { previewColor: '#1f4b99', styleTag: 'professional', name: '' };
    resetEditorHistory();
    renderCvPreview(skeletonData);
    captureEditorHistorySnapshot(true);
    enableEditorToolbar();
    if (typeof setSaveButtonState === 'function') setSaveButtonState('default');
}

function enableEditorToolbar() {
    const toolbar = document.getElementById('cvToolbar');
    if (toolbar && !document.body.classList.contains('pdf-export-mode')) {
        toolbar.classList.add('show');
    }
    // Auto-enable edit mode
    if (!document.body.classList.contains('pdf-export-mode') && !isEditMode) {
        isEditMode = true;
        window.isEditMode = true;
    }
    refreshEditModeUI();
    syncDesignControls();
    renderSectionManager();
}

// ── Design control event wiring ───────────────────────────────
function wireDesignControlEvents() {
    // Font
    document.getElementById('cvFontFamily')?.addEventListener('change', e =>
        updateDesign('font', e.target.value, null));

    // Font size
    const fsInput = document.getElementById('cvFontSize');
    const fsLabel = document.getElementById('cvFontSizeLabel');
    fsInput?.addEventListener('input', e => {
        if (fsLabel) fsLabel.textContent = `${e.target.value}%`;
        updateDesign('fontSize', e.target.value, null);
    });

    // Line spacing
    const lsInput = document.getElementById('cvLineSpacing');
    const lsLabel = document.getElementById('cvLineSpacingLabel');
    lsInput?.addEventListener('input', e => {
        if (lsLabel) lsLabel.textContent = e.target.value;
        updateDesign('lineSpacing', e.target.value, null);
    });

    // Background
    document.getElementById('cvBackground')?.addEventListener('change', e =>
        updateDesign('background', e.target.value, null));

    // Color dots
    document.querySelectorAll('.color-dot').forEach(dot => {
        dot.addEventListener('click', () => {
            const color = dot.dataset.color || getComputedStyle(dot).backgroundColor;
            updateDesign('color', color, dot);
        });
    });

    // Section toggles
    document.querySelectorAll('[data-section-toggle]').forEach(input => {
        input.addEventListener('change', () => {
            toggleSection(input.dataset.sectionToggle, input.checked);
        });
    });

    // Undo / Redo
    document.getElementById('btnUndoCv')?.addEventListener('click', undoCvChange);
    document.getElementById('btnRedoCv')?.addEventListener('click', redoCvChange);

    // Edit toggle
    document.getElementById('btnEditToggle')?.addEventListener('click', toggleEditMode);

    // Save
    document.getElementById('btnSaveCv')?.addEventListener('click', openSaveModal);

    // PDF
    document.querySelector('.btn-pdf')?.addEventListener('click', downloadPdf);

    // Avatar
    document.getElementById('avatarFileInput')?.addEventListener('change', handleAvatarUpload);

    // Design nav tabs
    document.querySelectorAll('.design-nav-item').forEach(btn => {
        btn.addEventListener('click', () => switchDesignPanel(btn.dataset.panel));
    });

    // Save modal
    document.getElementById('btnSaveConfirm')?.addEventListener('click', confirmSave);
    document.getElementById('btnSaveCancel')?.addEventListener('click', closeSaveModal);
}

// ── DOM ready ─────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    wireDesignControlEvents();
    initCvEditor();
});
