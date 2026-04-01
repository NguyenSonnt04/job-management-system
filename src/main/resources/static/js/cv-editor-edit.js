// ═══════════════════════════════════════════════════════════
//  cv-editor-edit.js  —  Edit mode, DOM sync, contenteditable
// ═══════════════════════════════════════════════════════════

// ── Active Item Shell ─────────────────────────────────────────
function resolvePreviewItemShell(target) {
    if (!isEditMode || !(target instanceof HTMLElement)) return null;
    if (target.closest('.cv-item-controls')) return null;
    return target.closest('.cv-edit-shell, .cvm-exp-item, .cvm-edu-item, .cvm-skill-group, .cvm2-row, .cvc2-card, .cvh-item, .cvh-skill-group, .cvh-inline-item, .cv-topcv-row, .cvcl2-item, .cvcl2-r-item');
}

function setActiveItemShell(shell, preview = document.getElementById('cvPreview')) {
    if (!preview) return;
    preview.querySelectorAll('.cv-item-shell-active').forEach(el => el.classList.remove('cv-item-shell-active'));
    if (shell instanceof HTMLElement) shell.classList.add('cv-item-shell-active');
}

function handlePreviewShellFocus(event) {
    if (!isEditMode) return;
    const target = event.target instanceof HTMLElement ? event.target : null;
    if (!target || target.closest('.cv-item-controls')) return;
    const shell = resolvePreviewItemShell(target);
    setActiveItemShell(shell, document.getElementById('cvPreview'));
}

function handlePreviewShellPointerDown(event) {
    if (!isEditMode) return;
    const target = event.target instanceof HTMLElement ? event.target : null;
    if (!target || target.closest('.cv-item-controls')) return;
    const shell = resolvePreviewItemShell(target);
    if (shell) setActiveItemShell(shell, document.getElementById('cvPreview'));
}

function bindPreviewShellStateEvents(preview = document.getElementById('cvPreview')) {
    if (!preview || preview.dataset.itemShellBound === 'true') return;
    preview.addEventListener('pointerover', event => {
        if (!isEditMode) return;
        const shell = resolvePreviewItemShell(event.target instanceof HTMLElement ? event.target : null);
        if (shell) setActiveItemShell(shell, preview);
    });
    preview.addEventListener('focusin', event => {
        if (!isEditMode) return;
        const shell = resolvePreviewItemShell(event.target instanceof HTMLElement ? event.target : null);
        if (shell) setActiveItemShell(shell, preview);
    });
    preview.addEventListener('pointerleave', () => {
        if (!preview.contains(document.activeElement)) setActiveItemShell(null, preview);
    });
    preview.dataset.itemShellBound = 'true';
}

// ── Edit Mode Toggle ──────────────────────────────────────────
function toggleEditMode() {
    isEditMode   = !isEditMode;
    window.isEditMode = isEditMode;
    if (!isEditMode) syncPreviewToCurrentCv();
    refreshEditModeUI();
}

function refreshEditModeUI() {
    const preview = document.getElementById('cvPreview');
    const badge   = document.getElementById('editModeBadge');
    const btn     = document.getElementById('btnEditToggle');
    if (!preview) return;

    if (!preview.dataset.shellFocusBound) {
        preview.addEventListener('click', handlePreviewShellFocus);
        preview.dataset.shellFocusBound = 'true';
    }
    if (!preview.dataset.shellPointerBound) {
        preview.addEventListener('pointerdown', handlePreviewShellPointerDown, true);
        preview.dataset.shellPointerBound = 'true';
    }
    if (!preview.dataset.previewLinkGuardBound) {
        preview.addEventListener('click', preventPreviewLinkNavigation, true);
        preview.dataset.previewLinkGuardBound = 'true';
    }

    if (isEditMode) {
        preview.classList.add('cv-editable-mode');
        applyPreviewEditableState(preview);
        if (btn)   { btn.classList.add('active'); btn.innerHTML = '✅ Đang chỉnh sửa'; }
        if (badge)   badge.style.display = 'flex';
    } else {
        preview.classList.remove('cv-editable-mode');
        preview.querySelectorAll('[contenteditable]').forEach(el => el.removeAttribute('contenteditable'));
        preview.querySelectorAll('.cvh-editable-active').forEach(el => el.classList.remove('cvh-editable-active'));
        setActiveItemShell(null, preview);
        if (btn)   { btn.classList.remove('active'); btn.innerHTML = 'Chỉnh sửa'; }
        if (badge)   badge.style.display = 'none';
    }
    initSortable();
}

function applyPreviewEditableState(preview = document.getElementById('cvPreview')) {
    if (!preview || !isEditMode) return;
    preview.querySelectorAll('.cv-editable').forEach(el => el.setAttribute('contenteditable', 'true'));
}

// ── Link navigation prevention ────────────────────────────────
function preventPreviewLinkNavigation(event) {
    if (!isEditMode) return;
    const anchor = event.target instanceof Element ? event.target.closest('a[href]') : null;
    if (anchor) event.preventDefault();
}

// ── Sync DOM → JSON model ─────────────────────────────────────
function cleanPreviewText(text) {
    return String(text || '').replace(/\u00A0/g, ' ').replace(/\s{2,}/g,' ').trim();
}

function syncSectionTitleElementToDesignState(element) {
    if (!(element instanceof HTMLElement)) return;
    const sectionKey = element.dataset.sectionTitleKey;
    if (!sectionKey) return;
    designState.sectionTitles = { ...(designState.sectionTitles || {}) };
    const title = cleanPreviewText(element.innerText);
    if (title) designState.sectionTitles[sectionKey] = title;
    else delete designState.sectionTitles[sectionKey];
}

function syncPreviewSectionTitles(preview) {
    if (!preview) return;
    preview.querySelectorAll('[data-section-title-key]').forEach(syncSectionTitleElementToDesignState);
}

function parseBulletText(text) {
    return cleanPreviewText(text).replace(/^[•◈▪]\s*/u, '').trim();
}

function readPreviewDetailLines(container, bulletSelector) {
    if (!container) return [];
    // New: single bullet block per item
    const block = container.querySelector('[data-bullet-block="true"]');
    if (block) {
        // innerText preserves line breaks as \n
        return (block.innerText || '')
            .split(/\n/)
            .map(line => parseBulletText(line))
            .filter(Boolean);
    }
    // Fallback: individual bullet elements
    if (!bulletSelector) return [];
    return [...container.querySelectorAll(bulletSelector)]
        .flatMap(node => cleanPreviewText(node.innerText || '')
            .split(/\n+/).map(parseBulletText).filter(Boolean));
}

function syncPreviewToCurrentCv() {
    if (!currentCvJson) return;
    const preview = document.getElementById('cvPreview');
    if (!preview) return;
    syncPreviewSectionTitles(preview);

    const style = normalizeCvStyle(currentCvJson._styleTag || getCurrentPreviewStyle());

    if (style === 'harvard') {
        syncHarvardPreviewToJson(preview);
    } else {
        syncGenericPreviewToJson(preview, style);
    }
}

function syncGenericPreviewToJson(preview, style) {
    if (!preview || !currentCvJson) return;

    // Header fields
    const nameEl     = preview.querySelector('.cvm-name, .cvc2-name, .cvm2-name, .cvcl2-name, .cvh-name, .cv-topcv-root h1');
    const subtitleEl = preview.querySelector('.cvm-job-title, .cvc2-title, .cvm2-subtitle, .cvcl2-subtitle, .cvh-subtitle, .cv-topcv-root header > div.cv-editable');
    const summaryEl  = preview.querySelector('.cvm-summary-text-full, .cvc2-summary, .cvm2-summary, .cvcl2-summary-bar p, .cvh-summary, .cv-topcv-section .cv-editable');
    if (nameEl)     currentCvJson.name     = cleanPreviewText(nameEl.innerText);
    if (subtitleEl) currentCvJson.subtitle = cleanPreviewText(subtitleEl.innerText);
    if (summaryEl)  currentCvJson.summary  = cleanPreviewText(summaryEl.innerText.replace(/<br>/gi, '\n'));

    // Contact
    const contactContainer = preview.querySelector('[data-cv-section="contacts"]');
    if (contactContainer) {
        const contactEls = [...contactContainer.querySelectorAll('.cv-editable')];
        if (contactEls[0]) currentCvJson.email   = cleanPreviewText(contactEls[0].innerText);
        if (contactEls[1]) currentCvJson.phone   = cleanPreviewText(contactEls[1].innerText);
        if (contactEls[2]) currentCvJson.address = cleanPreviewText(contactEls[2].innerText);
    }

    // Experience
    const expShells = [...preview.querySelectorAll('[data-cv-section="experience"] .cvm-exp-item, [data-cv-section="experience"] .cvc2-card, [data-cv-section="experience"] .cvm2-row, [data-cv-section="experience"] .cvcl2-item, [data-cv-section="experience"] .cv-topcv-row')];
    if (expShells.length) {
        currentCvJson.experience = expShells.map(shell => {
            // Non-bullet editables only (exclude [data-bullet])
            const fields = [...shell.querySelectorAll('.cv-editable:not(.cv-item-controls *):not([data-bullet])')];
            const details = readPreviewDetailLines(shell);
            return {
                role:    cleanPreviewText(fields[0]?.innerText || ''),
                company: cleanPreviewText(fields[1]?.innerText || ''),
                period:  cleanPreviewText(fields[2]?.innerText || ''),
                details
            };
        });
    }

    // Education
    const eduShells = [...preview.querySelectorAll('[data-cv-section="education"] .cvm-edu-item, [data-cv-section="education"] .cvc2-card, [data-cv-section="education"] .cvm2-row, [data-cv-section="education"] .cvcl2-r-item, [data-cv-section="education"] .cv-topcv-row')];
    if (eduShells.length) {
        currentCvJson.education = eduShells.map(shell => {
            const fields = [...shell.querySelectorAll('.cv-editable:not(.cv-item-controls *):not([data-bullet])')];
            return {
                degree: cleanPreviewText(fields[0]?.innerText || ''),
                school: cleanPreviewText(fields[1]?.innerText || ''),
                period: cleanPreviewText(fields[2]?.innerText || ''),
                details: readPreviewDetailLines(shell)
            };
        });
    }

    // Skills
    const skillShells = [...preview.querySelectorAll('[data-cv-section="skills"] .cvm-skill-group, [data-cv-section="skills"] .cvc2-skill-group, [data-cv-section="skills"] .cvm2-row, [data-cv-section="skills"] .cvcl2-skill, [data-cv-section="skills"] .cv-topcv-row')];
    if (skillShells.length) {
        currentCvJson.skills = skillShells.map(shell => {
            const catEl   = shell.querySelector('.cvm-skill-cat strong, .cvc2-skill-cat, .cvm2-row-period, .cvcl2-skill-cat strong');
            const itemEls = shell.querySelectorAll('.cvm-skill-pill, .cvc2-tag');
            const itemsText = shell.querySelector('.cvm2-row-org, .cvcl2-skill-vals');
            return {
                category: cleanPreviewText(catEl?.innerText || ''),
                items: itemEls.length
                    ? [...itemEls].map(el => cleanPreviewText(el.innerText)).filter(Boolean)
                    : cleanPreviewText(itemsText?.innerText || '').split(',').map(s=>s.trim()).filter(Boolean)
            };
        });
    }

    // Projects
    const projShells = [...preview.querySelectorAll('[data-cv-section="projects"] .cvm-exp-item, [data-cv-section="projects"] .cvc2-card, [data-cv-section="projects"] .cvm2-row, [data-cv-section="projects"] .cvcl2-item, [data-cv-section="projects"] .cv-topcv-row')];
    if (projShells.length) {
        currentCvJson.projects = projShells.map(shell => {
            const fields  = [...shell.querySelectorAll('.cv-editable:not(.cv-item-controls *):not([data-bullet])')];
            const details = readPreviewDetailLines(shell);
            return {
                name:  cleanPreviewText(fields[0]?.innerText || ''),
                period:cleanPreviewText(fields[1]?.innerText || ''),
                tech:  cleanPreviewText(fields[2]?.innerText || ''),
                details
            };
        });
    }

    const referenceShells = [...preview.querySelectorAll('[data-cv-section="references"] .cv-reference-item')];
    if (referenceShells.length) {
        currentCvJson.references = referenceShells.map(shell => {
            const fields = [...shell.querySelectorAll('.cv-editable:not(.cv-item-controls *):not([data-bullet])')];
            return {
                name: cleanPreviewText(fields[0]?.innerText || ''),
                role: cleanPreviewText(fields[1]?.innerText || ''),
                company: '',
                contact: cleanPreviewText(fields[2]?.innerText || '')
            };
        });
    }

    const hobbyShells = [...preview.querySelectorAll('[data-cv-section="hobbies"] .cv-hobby-item')];
    if (hobbyShells.length) {
        currentCvJson.hobbies = hobbyShells.map(shell => {
            const fields = [...shell.querySelectorAll('.cv-editable:not(.cv-item-controls *):not([data-bullet])')];
            return {
                name: cleanPreviewText(fields[0]?.innerText || ''),
                description: cleanPreviewText(fields[1]?.innerText || '')
            };
        });
    }
}

function syncHarvardPreviewToJson(preview) {
    if (!preview || !currentCvJson) return;

    const nameEl    = preview.querySelector('.cvh-name');
    const subtitleEl= preview.querySelector('.cvh-subtitle');
    const summaryEl = preview.querySelector('.cvh-summary');
    if (nameEl)     currentCvJson.name     = cleanPreviewText(nameEl.innerText);
    if (subtitleEl) currentCvJson.subtitle = cleanPreviewText(subtitleEl.innerText);
    if (summaryEl)  currentCvJson.summary  = cleanPreviewText(summaryEl.innerText);

    preview.querySelectorAll('.cvh-contact-item[data-contact-key]').forEach(item => {
        const key = item.dataset.contactKey;
        if (!key) return;
        currentCvJson[key] = cleanPreviewText(item.innerText);
    });

    const getHarvardSection = (sectionKey, fallbackTitle = '') => {
        const byKey = preview.querySelector(`.cvh-section[data-cv-section="${sectionKey}"]`);
        if (byKey) return byKey;
        const normalized = fallbackTitle.toUpperCase();
        return [...preview.querySelectorAll('.cvh-section')]
            .find(section => (section.querySelector('.cvh-title')?.textContent || '').toUpperCase().includes(normalized)) || null;
    };

    const mapHarvardItems = (sectionKey, fallbackTitle, mapper) => {
        const section = getHarvardSection(sectionKey, fallbackTitle);
        if (!section) return [];
        return [...section.querySelectorAll('.cvh-item')].map(item => {
            const subLines = [...item.querySelectorAll('.cvh-item-sub')]
                .map(node => cleanPreviewText(node.innerText))
                .filter(Boolean);
            const details = readPreviewDetailLines(item, '.cvh-bullet:not(.cvh-github-bullet)');
            return mapper(item, subLines, details);
        });
    };

    const exp = mapHarvardItems('experience', 'EXPERIENCE', (item, subLines, details) => ({
        role: cleanPreviewText(item.querySelector('.cvh-item-title')?.innerText || ''),
        company: subLines[0] || '',
        period: cleanPreviewText(item.querySelector('.cvh-item-date')?.innerText || ''),
        details
    }));
    if (exp.length) currentCvJson.experience = exp;

    const edu = mapHarvardItems('education', 'EDUCATION', (item, subLines, details) => ({
        degree: cleanPreviewText(item.querySelector('.cvh-item-title')?.innerText || ''),
        school: subLines[0] || '',
        period: cleanPreviewText(item.querySelector('.cvh-item-date')?.innerText || ''),
        details
    }));
    if (edu.length) currentCvJson.education = edu;

    const projects = mapHarvardItems('projects', 'PROJECTS', (item, subLines, details) => {
        const techLine = subLines.find(line => /^Technologies\s*:/i.test(line)) || '';
        return {
            name: cleanPreviewText(item.querySelector('.cvh-item-title')?.innerText || ''),
            period: cleanPreviewText(item.querySelector('.cvh-item-date')?.innerText || ''),
            tech: techLine.replace(/^Technologies\s*:\s*/i, '').trim(),
            github: cleanPreviewText(item.querySelector('.cvh-github-bullet')?.innerText || ''),
            details
        };
    });
    if (projects.length) currentCvJson.projects = projects;

    const skillsSection = getHarvardSection('skills', 'SKILLS');
    if (skillsSection) {
        const skills = [...skillsSection.querySelectorAll('.cvh-skill-group')].map(group => ({
            category: cleanPreviewText(group.querySelector('.cvh-skill-cat')?.innerText || ''),
            items: cleanPreviewText(group.querySelector('.cvh-skill-items')?.innerText || '')
                .split(',')
                .map(item => item.trim())
                .filter(Boolean)
        }));
        if (skills.length) currentCvJson.skills = skills;
    }
}

// ── Harvard editable input ────────────────────────────────────
function handleHarvardEditableInput(event) {
    const target = event.target instanceof HTMLElement ? event.target : null;
    if (!target || !target.closest('.cvh-root')) return;
    if (!target.matches('.cv-editable')) return;
    queueEditorHistorySnapshot();
}

// ── bindPreviewEditableEvents ─────────────────────────────────
function bindPreviewEditableEvents() {
    const preview = document.getElementById('cvPreview');
    if (!preview) return;

    if (!preview.dataset.shellInputBound) {
        preview.addEventListener('input', event => {
            if (!isEditMode) return;
            const target = event.target instanceof HTMLElement ? event.target : null;
            if (target?.matches('.cv-editable')) {
                syncSectionTitleElementToDesignState(target);
                queueEditorHistorySnapshot();
            }
        });
        preview.addEventListener('input', handleHarvardEditableInput);
        preview.dataset.shellInputBound = 'true';
    }

    // ── Ensure item-control buttons work inside contenteditable shells ──
    if (!preview.dataset.itemCtrlBound) {
        // Capture phase: stop contenteditable from absorbing the pointerdown
        preview.addEventListener('pointerdown', event => {
            const btn = event.target instanceof Element
                ? event.target.closest('.cv-item-controls button, .cv-item-controls [role="button"]')
                : null;
            if (!btn) return;
            event.stopPropagation();
            // Don't preventDefault — we need the click to fire
        }, true);

        // Capture phase click: let button onclick run, stop propagation
        preview.addEventListener('click', event => {
            const btn = event.target instanceof Element
                ? event.target.closest('.cv-item-controls button')
                : null;
            if (!btn) return;
            event.stopPropagation();
            // execute the onclick manually if it exists (contenteditable may suppress it)
            const onclickAttr = btn.getAttribute('onclick');
            if (onclickAttr) {
                try { new Function(onclickAttr)(); } catch (e) { /* no-op */ }
            }
        }, true);

        // Also capture contact add/delete buttons
        preview.addEventListener('click', event => {
            const btn = event.target instanceof Element
                ? event.target.closest('.cv-contact-del-btn, .cv-contact-add-btn')
                : null;
            if (!btn) return;
            event.stopPropagation();
            event.preventDefault();
            const onclickAttr = btn.getAttribute('onclick');
            if (onclickAttr) {
                try { new Function(onclickAttr)(); } catch (e) { console.error('contact btn:', e); }
            }
        }, true);

        preview.dataset.itemCtrlBound = 'true';
    }

    bindPreviewShellStateEvents(preview);
    if (!preview.dataset.previewLinkGuardBound) {
        preview.addEventListener('click', preventPreviewLinkNavigation, true);
        preview.dataset.previewLinkGuardBound = 'true';
    }
}

// ── Contact field CRUD ────────────────────────────────────────
function removeContactField(key) {
    if (!currentCvJson) return;
    delete currentCvJson[key];
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

function addContactField(key) {
    if (!currentCvJson) return;
    currentCvJson[key] = '';          // empty string = show placeholder
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
    // Close any open menu
    document.getElementById('cv-contact-add-menu')?.remove();
}

function openAddContactMenu(triggerBtnEl) {
    // Remove existing menu
    document.getElementById('cv-contact-add-menu')?.remove();
    if (!currentCvJson) return;

    // The trigger may be called from onclick attribute (this = button element)
    const triggerBtn = triggerBtnEl instanceof Element ? triggerBtnEl : null;

    // Fields that are already present (key exists, might be empty string = placeholder)
    const available = (window.CONTACT_TYPES || []).filter(t => {
        return !(t.key in currentCvJson);   // only types completely absent
    });

    if (!available.length) {
        if (triggerBtn) triggerBtn.title = 'Đã thêm tất cả liên hệ';
        return;
    }

    const menu = document.createElement('div');
    menu.id = 'cv-contact-add-menu';
    menu.setAttribute('contenteditable', 'false');
    menu.innerHTML = available.map(t => `
        <button class="cv-contact-menu-item" type="button" data-key="${t.key}">
            <i data-lucide="${t.icon}" style="width:13px;height:13px;vertical-align:-2px;"></i>
            ${t.label}
        </button>`).join('');

    // Get position — fallback chain if button has no rect (hidden parent etc.)
    let rect = triggerBtn ? triggerBtn.getBoundingClientRect() : null;
    if (!rect || (rect.width === 0 && rect.height === 0)) {
        const bar = triggerBtn?.closest('.cv-contact-controls');
        rect = bar ? bar.getBoundingClientRect() : null;
    }
    if (!rect || (rect.width === 0 && rect.height === 0)) {
        rect = { bottom: window.innerHeight / 2, left: window.innerWidth / 2 - 80, top: 0 };
    }

    menu.style.cssText = `
        position:fixed;top:${rect.bottom + 6}px;left:${rect.left}px;
        z-index:9999;background:#fff;border:1px solid rgba(37,99,235,0.2);
        border-radius:10px;padding:6px;box-shadow:0 8px 24px rgba(26,36,86,0.14);
        display:flex;flex-direction:column;gap:2px;min-width:160px;
    `;

    menu.addEventListener('click', e => {
        const btn = e.target instanceof Element ? e.target.closest('[data-key]') : null;
        if (btn) {
            e.stopPropagation();
            addContactField(btn.dataset.key);
        }
    });

    document.body.appendChild(menu);
    if (window.lucide) lucide.createIcons({ el: menu });

    // Close on outside click
    const closeMenu = e => {
        if (!menu.contains(e.target) && e.target !== triggerBtn) {
            menu.remove();
            document.removeEventListener('click', closeMenu, true);
        }
    };
    setTimeout(() => document.addEventListener('click', closeMenu, true), 0);
}


// Expose for onclick attributes
window.removeContactField = removeContactField;
window.addContactField    = addContactField;
window.openAddContactMenu = openAddContactMenu;


// ── Keyboard shortcuts ────────────────────────────────────────
function handleEditorShortcuts(event) {
    if (!(event.ctrlKey || event.metaKey)) return;
    const target = event.target instanceof HTMLElement ? event.target : null;
    const isPreviewEditable = !!target?.closest('#cvPreview .cv-editable');
    const isFormField       = !!target?.closest('input, textarea, select');
    if (isFormField && !isPreviewEditable) return;

    const key = (event.key || '').toLowerCase();
    if (key === 's' && currentCvJson) {
        event.preventDefault();
        if (typeof openSaveModal === 'function') openSaveModal();
        return;
    }
    if (key === 'z') {
        event.preventDefault();
        if (event.shiftKey) redoCvChange(); else undoCvChange();
        return;
    }
    if (key === 'y') { event.preventDefault(); redoCvChange(); }
}

if (document.body) {
    if (!document.body.dataset.cvEditorShortcutsBound) {
        document.addEventListener('keydown', handleEditorShortcuts);
        document.body.dataset.cvEditorShortcutsBound = 'true';
    }
} else {
    document.addEventListener('DOMContentLoaded', () => {
        if (!document.body.dataset.cvEditorShortcutsBound) {
            document.addEventListener('keydown', handleEditorShortcuts);
            document.body.dataset.cvEditorShortcutsBound = 'true';
        }
    });
}

// ── Section tagging & ordering ────────────────────────────────
function getTaggedPreviewSection(sectionName) {
    return document.querySelector(`#cvPreview [data-cv-section="${sectionName}"]`);
}

function normalizePreviewSectionToken(value = '') {
    return String(value || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .replace(/&/g, ' and ')
        .replace(/[^a-z0-9]+/g, '_')
        .replace(/^_+|_+$/g, '');
}

function resolvePreviewSectionKey(rawKey = '', headingText = '') {
    const aliasMap = {
        contacts: 'contacts',
        thong_tin_lien_he: 'contacts',
        thong_tin_ca_nhan: 'contacts',
        contact: 'contacts',
        summary: 'summary',
        gioi_thieu: 'summary',
        professional_summary: 'summary',
        tom_tat: 'summary',
        muc_tieu_nghe_nghiep: 'summary',
        experience: 'experience',
        kinh_nghiem: 'experience',
        kinh_nghiem_lam_viec: 'experience',
        education: 'education',
        hoc_van: 'education',
        skills: 'skills',
        ky_nang: 'skills',
        skills_expertise: 'skills',
        projects: 'projects',
        du_an: 'projects',
        du_an_noi_bat: 'projects',
        certifications: 'certifications',
        chung_chi: 'certifications',
        awards: 'awards',
        awards_honors: 'awards',
        giai_thuong: 'awards',
        activities: 'activities',
        leadership_activities: 'activities',
        hoat_dong: 'activities',
        references: 'references',
        nguoi_tham_chieu: 'references',
        hobbies: 'hobbies',
        so_thich: 'hobbies'
    };

    const candidates = [rawKey, headingText]
        .map(normalizePreviewSectionToken)
        .filter(Boolean);
    for (const candidate of candidates) {
        if (aliasMap[candidate]) return aliasMap[candidate];
    }
    return '';
}

function tagPreviewSections(style = 'professional') {
    // Sections already have data-cv-section from renderers; this augments any missed ones.
    const preview = document.getElementById('cvPreview');
    if (!preview) return;

    const HEADING_MAP = {
        'MỤC TIÊU NGHỀ NGHIỆP': 'summary', 'GIỚI THIỆU': 'summary', 'PROFESSIONAL SUMMARY': 'summary', 'TÓM TẮT': 'summary',
        'KINH NGHIỆM LÀM VIỆC': 'experience', 'KINH NGHIỆM': 'experience', 'EXPERIENCE': 'experience',
        'HỌC VẤN': 'education', 'EDUCATION': 'education',
        'KỸ NĂNG': 'skills', 'SKILLS': 'skills', 'SKILLS & EXPERTISE': 'skills',
        'DỰ ÁN NỔI BẬT': 'projects', 'DỰ ÁN': 'projects', 'PROJECTS': 'projects',
        'CHỨNG CHỈ': 'certifications', 'CERTIFICATIONS': 'certifications',
        'GIẢI THƯỞNG': 'awards', 'AWARDS & HONORS': 'awards',
        'HOẠT ĐỘNG': 'activities', 'LEADERSHIP & ACTIVITIES': 'activities'
    };

    preview.querySelectorAll('section, .cvh-section, .cvm2-block, .cvc2-zone, .cvcl2-section, .cv-topcv-section').forEach(section => {
        const titleEl = section.querySelector('h1, h2, h3, h4, .cvm-section-title, .cvc2-zone-title, .cvm2-block-label, .cvh-title, .cvcl2-sec-title, .cvcl2-sec-title-r, .cv-topcv-title');
        const text    = (titleEl?.textContent || '').trim();
        const key     = resolvePreviewSectionKey(section.dataset.cvSection || '', text) || HEADING_MAP[text.toUpperCase()];
        if (key) section.setAttribute('data-cv-section', key);
    });
}

function applySectionVisibilityToPreview() {
    const preview = document.getElementById('cvPreview');
    if (!preview) return;
    sectionCatalog.forEach(sectionName => {
        const isEnabled = sectionEnabled(sectionName);
        preview.querySelectorAll(`[data-cv-section="${sectionName}"]`).forEach(el => {
            el.style.display = isEnabled ? '' : 'none';
        });
    });
}

function applySectionOrderToPreview(style = getCurrentPreviewStyle()) {
    const preview = document.getElementById('cvPreview');
    if (!preview) return;
    tagPreviewSections(style);
    const ordered = getOrderedUsedSections();

    // Single-column layout
    const singleBody = preview.querySelector('.cvm2-body, .cvh-body, .cv-topcv-body');
    if (singleBody) {
        const sections = [...singleBody.querySelectorAll('[data-cv-section]')];
        ordered.forEach(key => {
            const sec = sections.find(s => resolvePreviewSectionKey(s.dataset.cvSection || '') === key);
            if (sec) singleBody.appendChild(sec);
        });
        return;
    }

    // Two-column: distribute visible sections row-by-row so drag order matches the layout grid.
    const columns = [
        preview.querySelector('.cvm-left, .cvc2-left, .cvcl2-left'),
        preview.querySelector('.cvm-right, .cvc2-right, .cvcl2-right')
    ].filter(Boolean);
    if (!columns.length) return;

    const sectionMap = new Map();
    columns.forEach(col => {
        [...col.children].forEach(child => {
            const rawKey = child?.dataset?.cvSection || '';
            const titleText = child.querySelector('h1, h2, h3, h4, .cvm-section-title, .cvc2-zone-title, .cvm2-block-label, .cvh-title, .cvcl2-sec-title, .cvcl2-sec-title-r, .cv-topcv-title')?.textContent || '';
            const key = resolvePreviewSectionKey(rawKey, titleText);
            if (!key) return;
            child.dataset.cvSection = key;
            sectionMap.set(key, child);
        });
    });

    ordered
        .filter(key => sectionMap.has(key))
        .forEach((key, index) => {
            columns[index % columns.length].appendChild(sectionMap.get(key));
        });
}
