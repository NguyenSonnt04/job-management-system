// ═══════════════════════════════════════════════════════════
//  cv-editor-design.js
//  Design panel controls, avatar, icon pickers, sortable layout
// ═══════════════════════════════════════════════════════════

// ── Lucide icon helper ────────────────────────────────────────
function libIcon(name, size = 16, style = '') {
    return `<i data-lucide="${name}" style="width:${size}px;height:${size}px;${style}"></i>`;
}

// ── Avatar ────────────────────────────────────────────────────
function getAvatarHtml(borderColor, size = 80, shape = 'circle') {
    const radius = shape === 'square' ? '12px' : '50%';
    const borderStyle = `border:3px solid ${borderColor || '#ccc'};`;
    if (avatarDataUrl) {
        return `<div class="cvm-avatar-wrapper" style="flex-shrink:0;">
            <img id="cvAvatarImg" src="${avatarDataUrl}" style="width:${size}px;height:${size}px;border-radius:${radius};object-fit:cover;${borderStyle}" alt="Avatar">
        </div>`;
    }
    return `<div class="cvm-avatar-wrapper" id="cvAvatarPlaceholder" onclick="triggerAvatarUpload()" style="width:${size}px;height:${size}px;border-radius:${radius};${borderStyle}background:#f1f5f9;display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0;" title="Tải ảnh đại diện">
        ${libIcon('camera', Math.round(size * 0.35), 'color:#94a3b8;')}
    </div>`;
}

function triggerAvatarUpload() {
    document.getElementById('avatarFileInput')?.click();
}

function handleAvatarUpload(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = e => {
        avatarDataUrl = e.target.result;
        window.avatarDataUrl = avatarDataUrl;
        if (currentCvJson) {
            currentCvJson.avatarUrl = avatarDataUrl;
            renderCvPreview(currentCvJson);
        }
        markCvDirty();
    };
    reader.readAsDataURL(file);
}

// ── Design Controls ───────────────────────────────────────────
function syncDesignControls() {
    const fontInput        = document.getElementById('cvFontFamily');
    const fontSizeInput    = document.getElementById('cvFontSize');
    const lineSpacingInput = document.getElementById('cvLineSpacing');
    const backgroundInput  = document.getElementById('cvBackground');

    if (fontInput)        fontInput.value        = designState.font;
    if (fontSizeInput)    fontSizeInput.value    = String(designState.fontSize);
    if (lineSpacingInput) lineSpacingInput.value = String(designState.lineSpacing);
    if (backgroundInput)  backgroundInput.value  = designState.background;

    const fontSizeLabel    = document.getElementById('cvFontSizeLabel');
    const lineSpacingLabel = document.getElementById('cvLineSpacingLabel');
    if (fontSizeLabel)    fontSizeLabel.textContent    = `${designState.fontSize}%`;
    if (lineSpacingLabel) lineSpacingLabel.textContent = String(designState.lineSpacing);

    function normalizeColor(color) {
        const probe = document.createElement('div');
        probe.style.color = color;
        return probe.style.color || color;
    }

    document.querySelectorAll('.color-dot').forEach(dot => {
        dot.classList.toggle('active',
            normalizeColor(getComputedStyle(dot).backgroundColor) === normalizeColor(designState.color));
    });

    document.querySelectorAll('[data-section-toggle]').forEach(input => {
        input.checked = designState.sections[input.dataset.sectionToggle] !== false;
    });
}

function applyDesignStateToPreview() {
    const cvDoc = document.getElementById('cvPreview');
    if (!cvDoc) return;

    cvDoc.style.setProperty('--cv-font-family', designState.font);
    cvDoc.style.setProperty('--cv-accent', designState.color);
    cvDoc.style.fontFamily  = designState.font;
    cvDoc.style.fontSize    = `${designState.fontSize}%`;
    cvDoc.style.lineHeight  = String(designState.lineSpacing);

    const cvRoot = cvDoc.querySelector('.cv-full');
    if (cvRoot) {
        cvRoot.style.setProperty('--font-main', designState.font);
        cvRoot.style.setProperty('--cv-accent', designState.color);
    }

    applyBackground(designState.background);
    syncDesignControls();
}

function updateDesign(prop, value, el) {
    if (prop === 'font') {
        designState.font = value;
    } else if (prop === 'fontSize') {
        designState.fontSize = Number(value);
    } else if (prop === 'lineSpacing') {
        designState.lineSpacing = Number(value);
    } else if (prop === 'color') {
        designState.color = value;
        window.currentTemplate = window.currentTemplate || {};
        window.currentTemplate.previewColor = value;
        if (el) {
            document.querySelectorAll('.color-dot').forEach(d => d.classList.remove('active'));
            el.classList.add('active');
        }
    } else if (prop === 'background') {
        designState.background = value;
    }

    syncDesignStateToCurrentCv();
    markCvDirty();

    if (prop === 'color') renderCurrentPreview();
    else applyDesignStateToPreview();
}

// ── Background Patterns ───────────────────────────────────────
function applyBackground(pattern) {
    const cvDoc  = document.getElementById('cvPreview');
    const cvRoot = cvDoc?.querySelector('.cv-full');
    if (!cvRoot) return;

    cvRoot.style.background      = '';
    cvRoot.style.backgroundImage = '';
    cvRoot.style.color           = '';

    switch (pattern) {
        case 'solid':
            cvRoot.style.background = '#ffffff';
            break;
        case 'gradient-light':
            cvRoot.style.background = 'linear-gradient(135deg, #ffffff 0%, #f1f5f9 100%)';
            break;
        case 'gradient-dark':
            cvRoot.style.background = 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)';
            cvRoot.style.color      = '#ffffff';
            break;
        case 'pattern-dots':
            cvRoot.style.backgroundImage = 'radial-gradient(circle, #e2e8f0 1px, transparent 1px)';
            cvRoot.style.backgroundSize  = '20px 20px';
            cvRoot.style.background      = '#ffffff';
            break;
        case 'pattern-lines':
            cvRoot.style.backgroundImage   = 'repeating-linear-gradient(45deg, #f1f5f9 0, #f1f5f9 1px, transparent 0, transparent 50%)';
            cvRoot.style.backgroundSize    = '14px 14px';
            cvRoot.style.backgroundColor   = '#ffffff';
            break;
        case 'pattern-subtle':
            cvRoot.style.backgroundImage = 'linear-gradient(120deg, #f1f5f9 0%, #e2e8f0 100%)';
            cvRoot.style.backgroundSize  = '100% 100%';
            break;
    }
}

// ── Design Panel Navigation ───────────────────────────────────
function switchDesignPanel(panelName) {
    document.querySelectorAll('.design-nav-item').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.panel === panelName);
    });
    document.querySelectorAll('.design-panel').forEach(panel => {
        panel.classList.toggle('active', panel.dataset.panel === panelName);
    });
    if (panelName === 'layout') renderLayoutManager();
    if (window.lucide) lucide.createIcons();
}

// ── Section Toggle ────────────────────────────────────────────
function toggleSectionByClass(selector, isVisible) {
    document.querySelectorAll(selector).forEach(el => {
        el.style.display = isVisible ? '' : 'none';
    });
}

function addSection(sectionName) {
    designState.sections[sectionName] = true;
    renderCvPreview(currentCvJson);
    renderSectionManager();
    markCvDirty();
}

function toggleSection(sectionName, isVisible) {
    designState.sections[sectionName] = isVisible;
    if (!isVisible) {
        applySectionVisibilityToPreview();
        renderSectionManager();
        markCvDirty();
        return;
    }
    renderCvPreview(currentCvJson);
    renderSectionManager();
    markCvDirty();
}

// ── Icon Choices ──────────────────────────────────────────────
const SECTION_ICON_CHOICES = [
    'id-card', 'image', 'file-pen-line', 'briefcase-business', 'graduation-cap',
    'sparkles', 'rocket', 'scroll-text', 'trophy', 'target',
    'folder-kanban', 'badge-check', 'star', 'layers-3', 'pen-tool'
];

const PREVIEW_ICON_CHOICES = [
    'mail', 'phone', 'map-pin', 'briefcase-business', 'badge-alert',
    'badge-check', 'sparkles', 'star', 'circle-dot', 'shield-check',
    'trophy', 'rocket', 'globe', 'link', 'folder-kanban', 'target'
];

// ── Section Icon Picker ───────────────────────────────────────
function ensureSectionIconPickerModal() {
    if (document.getElementById('sectionIconPickerModal')) return;
    const modal = document.createElement('div');
    modal.id        = 'sectionIconPickerModal';
    modal.className = 'cv-modal';
    modal.style.display = 'none';
    modal.innerHTML = `
        <div class="cv-modal-content section-icon-modal-content">
            <div class="cv-modal-title">Đổi icon mục</div>
            <div class="section-icon-modal-subtitle">Chọn icon hiển thị cho mục này trong giao diện bố cục.</div>
            <div class="section-icon-grid" id="sectionIconGrid"></div>
            <div class="cv-modal-actions">
                <button type="button" class="btn-modal-cancel" id="sectionIconCancelBtn">Hủy</button>
                <button type="button" class="btn-modal-confirm" id="sectionIconSaveBtn">Lưu</button>
            </div>
        </div>`;
    document.body.appendChild(modal);

    const grid = modal.querySelector('#sectionIconGrid');
    if (grid) {
        grid.innerHTML = SECTION_ICON_CHOICES.map(iconName =>
            `<button type="button" class="section-icon-option" data-icon-name="${iconName}" aria-label="${iconName}">
                ${libIcon(iconName, 18, 'color:currentColor;')}
            </button>`
        ).join('');
        grid.querySelectorAll('.section-icon-option').forEach(option => {
            option.addEventListener('click', () => {
                sectionIconPickerState.selectedIcon = option.dataset.iconName || '';
                syncSectionIconOptionState();
            });
        });
    }
    modal.querySelector('#sectionIconCancelBtn')?.addEventListener('click', closeSectionIconPickerModal);
    modal.querySelector('#sectionIconSaveBtn')?.addEventListener('click', saveSectionIconSelection);
    modal.addEventListener('click', event => { if (event.target === modal) closeSectionIconPickerModal(); });
}

function syncSectionIconOptionState() {
    const modal = document.getElementById('sectionIconPickerModal');
    if (!modal) return;
    modal.querySelectorAll('.section-icon-option').forEach(option => {
        option.classList.toggle('is-active', option.dataset.iconName === sectionIconPickerState.selectedIcon);
    });
}

function openSectionIconPickerModal(sectionKey) {
    if (!sectionMeta[sectionKey]) return;
    ensureSectionIconPickerModal();
    const modal = document.getElementById('sectionIconPickerModal');
    if (!modal) return;
    sectionIconPickerState.sectionKey    = sectionKey;
    sectionIconPickerState.selectedIcon  = getSectionIconName(sectionKey);
    syncSectionIconOptionState();
    modal.style.display = 'flex';
}

function closeSectionIconPickerModal() {
    const modal = document.getElementById('sectionIconPickerModal');
    if (modal) modal.style.display = 'none';
    sectionIconPickerState = { sectionKey: null, selectedIcon: null };
}

function saveSectionIconSelection() {
    const { sectionKey, selectedIcon } = sectionIconPickerState;
    if (!sectionKey || !selectedIcon) { closeSectionIconPickerModal(); return; }
    if (typeof designState === 'object') {
        designState.sectionIcons = designState.sectionIcons || {};
        designState.sectionIcons[sectionKey] = selectedIcon;
    }
    closeSectionIconPickerModal();
    refreshSectionIconUI();
}

function refreshSectionIconUI() {
    if (typeof renderSectionManager === 'function') renderSectionManager();
    if (typeof renderLayoutStudio   === 'function') renderLayoutStudio();
    normalizeSectionMetaIcons(document);
    enhanceSectionIconTriggers(document);
    if (window.lucide) lucide.createIcons();
}

// ── Preview Icon Picker ───────────────────────────────────────
function inferPreviewIconKeyFromNode(node, fallbackIcon) {
    if (!(node instanceof Element)) return `${fallbackIcon || 'icon'}-0`;
    const section = node.closest('[data-cv-section]')?.getAttribute('data-cv-section') || '';
    if (fallbackIcon === 'mail')               return 'contact-email';
    if (fallbackIcon === 'phone')              return 'contact-phone';
    if (fallbackIcon === 'map-pin')            return 'contact-address';
    if (fallbackIcon === 'briefcase-business') return 'contact-role';
    if (fallbackIcon === 'camera')             return 'avatar-camera';
    const item = node.closest('.cvm-exp-item, .cvm-edu-item, .cvc2-card, .cvm2-row, .cvcl2-item, .cvcl2-r-item, .cv-topcv-row, .cvh-item, .cvh-inline-item');
    const siblings = item?.parentElement
        ? [...item.parentElement.children].filter(child =>
            child.matches?.('.cvm-exp-item, .cvm-edu-item, .cvc2-card, .cvm2-row, .cvcl2-item, .cvcl2-r-item, .cv-topcv-row, .cvh-item, .cvh-inline-item'))
        : [];
    const index = item ? Math.max(0, siblings.indexOf(item)) : 0;
    return `${section || 'preview'}-${fallbackIcon}-${index}`;
}

function renderPreviewIconTrigger(iconKey, fallbackIcon, size = 14, style = '', label = 'icon') {
    const iconName  = getPreviewIconName(iconKey, fallbackIcon);
    const safeLabel = String(label || 'icon').replace(/"/g, '&quot;');
    return `<button type="button" class="preview-icon-trigger" data-preview-icon-key="${iconKey}" data-fallback-icon="${fallbackIcon}" data-label="${safeLabel}" title="Đổi icon ${safeLabel}" aria-label="Đổi icon ${safeLabel}" style="display:inline-flex;align-items:center;justify-content:center;width:${Math.max(size + 10, 24)}px;height:${Math.max(size + 10, 24)}px;padding:0;border:none;background:transparent;color:inherit;cursor:pointer;border-radius:8px;vertical-align:middle;${style}">${libIcon(iconName, size, 'color:currentColor;')}</button>`;
}

function ensurePreviewIconPickerModal() {
    if (document.getElementById('previewIconPickerModal')) return;
    const modal = document.createElement('div');
    modal.id        = 'previewIconPickerModal';
    modal.className = 'cv-modal';
    modal.style.display = 'none';
    modal.innerHTML = `
        <div class="cv-modal-content section-icon-modal-content">
            <div class="cv-modal-title">Đổi icon trong CV</div>
            <div class="section-icon-modal-subtitle" id="previewIconModalSubtitle">Chọn icon mới cho mục này trong bản xem trước CV.</div>
            <div class="section-icon-grid" id="previewIconGrid"></div>
            <div class="cv-modal-actions">
                <button type="button" class="btn-modal-cancel" id="previewIconCancelBtn">Hủy</button>
                <button type="button" class="btn-modal-confirm" id="previewIconSaveBtn">Lưu</button>
            </div>
        </div>`;
    document.body.appendChild(modal);

    const grid = modal.querySelector('#previewIconGrid');
    if (grid) {
        grid.innerHTML = PREVIEW_ICON_CHOICES.map(iconName =>
            `<button type="button" class="section-icon-option preview-icon-option" data-icon-name="${iconName}" aria-label="${iconName}">
                ${libIcon(iconName, 18, 'color:currentColor;')}
            </button>`
        ).join('');
        grid.querySelectorAll('.preview-icon-option').forEach(option => {
            option.addEventListener('click', () => {
                previewIconPickerState.selectedIcon = option.dataset.iconName || '';
                syncPreviewIconOptionState();
            });
        });
    }
    modal.querySelector('#previewIconCancelBtn')?.addEventListener('click', closePreviewIconPickerModal);
    modal.querySelector('#previewIconSaveBtn')?.addEventListener('click', savePreviewIconSelection);
    modal.addEventListener('click', event => { if (event.target === modal) closePreviewIconPickerModal(); });
}

function syncPreviewIconOptionState() {
    const modal = document.getElementById('previewIconPickerModal');
    if (!modal) return;
    modal.querySelectorAll('.preview-icon-option').forEach(option => {
        option.classList.toggle('is-active', option.dataset.iconName === previewIconPickerState.selectedIcon);
    });
}

function openPreviewIconPickerModal(iconKey, fallbackIcon, label = 'này') {
    ensurePreviewIconPickerModal();
    enhancePreviewIconTriggers(document.getElementById('cvPreview'));
    const modal = document.getElementById('previewIconPickerModal');
    if (!modal) return;
    previewIconPickerState.iconKey       = iconKey;
    previewIconPickerState.selectedIcon  = getPreviewIconName(iconKey, fallbackIcon);
    previewIconPickerState.label         = label;
    const subtitle = modal.querySelector('#previewIconModalSubtitle');
    if (subtitle) subtitle.textContent   = `Chọn icon mới cho ${label} trong bản xem trước CV.`;
    syncPreviewIconOptionState();
    modal.style.display = 'flex';
}

function closePreviewIconPickerModal() {
    const modal = document.getElementById('previewIconPickerModal');
    if (modal) modal.style.display = 'none';
    previewIconPickerState = { iconKey: null, selectedIcon: null, label: '' };
}

function savePreviewIconSelection() {
    const { iconKey, selectedIcon } = previewIconPickerState;
    if (!iconKey || !selectedIcon) { closePreviewIconPickerModal(); return; }
    if (typeof designState === 'object') {
        designState.previewIcons = designState.previewIcons || {};
        designState.previewIcons[iconKey] = selectedIcon;
    }
    closePreviewIconPickerModal();
    rerenderCurrentPreview();
}

function enhancePreviewIconTriggers(preview = document.getElementById('cvPreview')) {
    if (!preview) return;
    const supportedIcons = new Set([
        'mail', 'phone', 'map-pin', 'briefcase-business', 'trophy', 'camera',
        'link', 'rocket', 'target', 'star', 'sparkles', 'badge-check',
        'globe', 'folder-kanban', 'circle-dot', 'shield-check'
    ]);
    preview.querySelectorAll('[data-lucide], svg.lucide').forEach(iconNode => {
        const iconElement = iconNode instanceof SVGElement || iconNode instanceof HTMLElement ? iconNode : null;
        if (!iconElement) return;
        if (iconElement.closest('.preview-icon-trigger')) return;
        if (iconElement.closest('.cv-item-controls')) return;
        const iconName = iconElement.getAttribute('data-lucide')
            || Array.from(iconElement.classList || []).find(cls => cls.startsWith('lucide-'))?.replace('lucide-', '')
            || '';
        if (!supportedIcons.has(iconName)) return;
        const owner = iconElement.parentElement;
        if (!(owner instanceof HTMLElement)) return;
        const label   = owner.textContent?.trim() || 'icon';
        const iconKey = inferPreviewIconKeyFromNode(owner, iconName);
        const trigger = document.createElement('button');
        trigger.type                       = 'button';
        trigger.className                  = 'preview-icon-trigger';
        trigger.dataset.previewIconKey     = iconKey;
        trigger.dataset.fallbackIcon       = iconName;
        trigger.dataset.label              = label;
        trigger.title                      = `Đổi icon ${label}`;
        trigger.setAttribute('aria-label', `Đổi icon ${label}`);
        Object.assign(trigger.style, {
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: '24px', height: '24px', padding: '0', margin: '0',
            border: 'none', background: 'transparent', borderRadius: '8px',
            color: 'inherit', cursor: 'pointer'
        });
        owner.insertBefore(trigger, iconElement);
        trigger.appendChild(iconElement);
    });
    if (window.lucide?.createIcons) window.lucide.createIcons();
}

// ── Section Meta Icon Injection ───────────────────────────────
function renderSectionIconTrigger(sectionKey, size = 16, style = '') {
    const meta     = sectionMeta[sectionKey];
    const iconName = getSectionIconName(sectionKey);
    const label    = meta?.label || 'mục';
    return `<button type="button" class="section-meta-icon-trigger" data-section-key="${sectionKey}" title="Đổi icon cho ${label}" aria-label="Đổi icon cho ${label}" style="display:inline-flex;align-items:center;justify-content:center;width:${Math.max(size + 12, 28)}px;height:${Math.max(size + 12, 28)}px;padding:0;border:none;background:transparent;color:inherit;cursor:pointer;border-radius:10px;vertical-align:middle;${style}">${libIcon(iconName, size, 'color:currentColor;')}</button>`;
}

function renderSectionMetaIcon(sectionKey, size = 16, style = '') {
    const meta = sectionMeta[sectionKey];
    if (!meta?.icon) return meta?.glyph || '';
    return renderSectionIconTrigger(sectionKey, size, style);
}

function inferSectionKeyFromText(text = '') {
    const normalized = String(text || '').toLowerCase();
    return Object.entries(sectionMeta).find(([, meta]) =>
        normalized.includes((meta.label || '').toLowerCase()) ||
        normalized.includes((meta.layoutLabel || '').toLowerCase())
    )?.[0] || null;
}

function enhanceSectionIconTriggers(root = document) {
    root.querySelectorAll('.section-meta-icon-trigger, [data-section-meta-icon="true"]').forEach(node => {
        if (!(node instanceof HTMLElement)) return;
        const sectionKey = node.dataset.sectionKey || inferSectionKeyFromText(
            node.closest('.layout-section-card, .section-manager-item, li, button, div')?.textContent || ''
        );
        if (!sectionKey || !sectionMeta[sectionKey]) return;
        node.dataset.sectionKey = sectionKey;
        node.classList.add('section-meta-icon-trigger');
        node.setAttribute('title',       `Đổi icon cho ${sectionMeta[sectionKey].label}`);
        node.setAttribute('aria-label',  `Đổi icon cho ${sectionMeta[sectionKey].label}`);
        if (!node.hasAttribute('tabindex')) node.setAttribute('tabindex', '0');
    });
}

function resolveSectionIconTrigger(target) {
    if (!(target instanceof Element)) return null;
    const directTrigger = target.closest('.section-meta-icon-trigger, [data-section-meta-icon="true"]');
    if (directTrigger instanceof HTMLElement) return directTrigger;
    const iconNode = target.closest('[data-lucide]');
    if (!(iconNode instanceof HTMLElement)) return null;
    if (iconNode.closest('#cvPreview')) return null;
    const host = iconNode.closest('.layout-section-card, .section-manager-item, li, button, div');
    if (!(host instanceof HTMLElement)) return null;
    const sectionKey = host.dataset.sectionKey || inferSectionKeyFromText(host.textContent || '');
    if (!sectionKey || !sectionMeta[sectionKey]) return null;
    host.dataset.sectionKey = sectionKey;
    host.classList.add('section-meta-icon-trigger');
    host.setAttribute('title',       `Đổi icon cho ${sectionMeta[sectionKey].label}`);
    host.setAttribute('aria-label',  `Đổi icon cho ${sectionMeta[sectionKey].label}`);
    host.style.cursor = 'pointer';
    return host;
}

function normalizeSectionMetaIcons(root = document) {
    const iconMap = {
        '🎴': 'id-card', '🖼️': 'image', '📝': 'file-pen-line',
        '💼': 'briefcase-business', '🎓': 'graduation-cap', '⭐': 'sparkles',
        '🚀': 'rocket', '📜': 'scroll-text', '🏆': 'trophy', '🎯': 'target'
    };
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    const textNodes = [];
    while (walker.nextNode()) textNodes.push(walker.currentNode);
    textNodes.forEach(textNode => {
        const parent = textNode.parentElement;
        if (!parent || parent.closest('#cvPreview')) return;
        if (parent.querySelector(':scope > [data-section-meta-icon="true"]')) return;
        const raw     = textNode.textContent || '';
        const trimmed = raw.trimStart();
        const matched = Object.entries(iconMap).find(([emoji]) => trimmed.startsWith(emoji));
        if (!matched) return;
        const [emoji, iconName] = matched;
        textNode.textContent = raw.replace(emoji, '').trimStart();
        const sectionKey = inferSectionKeyFromText(parent.closest('.layout-section-card, .section-manager-item, li, button, div')?.textContent || parent.textContent || '');
        parent.insertAdjacentHTML('afterbegin', `<span data-section-meta-icon="true" data-section-key="${sectionKey || ''}" class="section-meta-icon-trigger" style="display:inline-flex;align-items:center;justify-content:center;width:28px;height:28px;margin-right:8px;vertical-align:middle;border-radius:10px;">${libIcon(iconName, 16, 'color:currentColor;')}</span>`);
    });
}

// ── Icon picker click/keyboard delegates ──────────────────────
document.addEventListener('click', event => {
    const trigger = resolveSectionIconTrigger(event.target);
    if (!trigger) return;
    event.preventDefault();
    event.stopPropagation();
    const sectionKey = trigger.getAttribute('data-section-key');
    if (sectionKey) openSectionIconPickerModal(sectionKey);
}, true);

document.addEventListener('pointerdown', event => {
    const trigger = event.target instanceof Element ? event.target.closest('.preview-icon-trigger') : null;
    if (!trigger) return;
    event.preventDefault();
    event.stopPropagation();
}, true);

document.addEventListener('click', event => {
    const trigger = event.target instanceof Element ? event.target.closest('.preview-icon-trigger') : null;
    if (!trigger) return;
    event.preventDefault();
    event.stopPropagation();
    const iconKey      = trigger.getAttribute('data-preview-icon-key');
    const fallbackIcon = trigger.getAttribute('data-fallback-icon') || 'star';
    const label        = trigger.getAttribute('data-label') || 'mục này';
    if (iconKey) openPreviewIconPickerModal(iconKey, fallbackIcon, label);
}, true);

document.addEventListener('keydown', event => {
    const trigger = resolveSectionIconTrigger(event.target);
    if (!trigger) return;
    if (event.key !== 'Enter' && event.key !== ' ') return;
    event.preventDefault();
    const sectionKey = trigger.getAttribute('data-section-key');
    if (sectionKey) openSectionIconPickerModal(sectionKey);
}, true);

document.addEventListener('keydown', event => {
    const trigger = event.target instanceof Element ? event.target.closest('.preview-icon-trigger') : null;
    if (!trigger) return;
    if (event.key !== 'Enter' && event.key !== ' ') return;
    event.preventDefault();
    const iconKey      = trigger.getAttribute('data-preview-icon-key');
    const fallbackIcon = trigger.getAttribute('data-fallback-icon') || 'star';
    const label        = trigger.getAttribute('data-label') || 'mục này';
    if (iconKey) openPreviewIconPickerModal(iconKey, fallbackIcon, label);
}, true);

// ── Layout / Sortable ─────────────────────────────────────────
function getDirectTaggedSections(container) {
    if (!container) return [];
    return [...container.children]
        .map(child => child?.dataset?.cvSection)
        .filter(Boolean);
}

function createLayoutUsedCard(sectionName) {
    const meta        = sectionMeta[sectionName];
    const isFixedCard = sectionName === 'avatar' || sectionName === 'profileCard';
    const selected    = layoutSelectedSection === sectionName ? ' selected' : '';
    if (isFixedCard) {
        return `<div class="layout-section-card layout-section-card-fixed${selected}" title="${meta.label}">
            <span class="layout-section-title">${meta.layoutLabel || meta.label}</span>
        </div>`;
    }
    return `<button type="button" class="layout-section-card${selected}" data-layout-section="${sectionName}" draggable="true" onclick="selectLayoutSection('${sectionName}')" title="${meta.label}">
        <span class="layout-section-grip" aria-hidden="true"><i data-lucide="grip-vertical"></i></span>
        <span class="layout-section-title">${meta.layoutLabel || meta.label}</span>
        <span class="layout-section-action" onclick="event.stopPropagation(); toggleSection('${sectionName}', false)" aria-label="Ẩn ${meta.label}">
            <i data-lucide="minus"></i>
        </span>
    </button>`;
}

function createLayoutUnusedCard(sectionName) {
    const meta = sectionMeta[sectionName];
    return `<button type="button" class="layout-section-card layout-section-card-unused" onclick="addSection('${sectionName}')" title="Thêm ${meta.label}">
        <span class="layout-section-title">${meta.layoutLabel || meta.label}</span>
        <span class="layout-section-action" aria-label="Thêm ${meta.label}"><i data-lucide="plus"></i></span>
    </button>`;
}

function ensureLayoutSelection() {
    const used       = getOrderedUsedSections();
    const selectable = used.filter(sectionName => sectionName !== 'avatar');
    if (selectable.includes(layoutSelectedSection)) return;
    layoutSelectedSection = selectable[0] || used[0] || sectionCatalog[0];
}

function selectLayoutSection(sectionName) {
    layoutSelectedSection = sectionName;
    renderLayoutManager();
}

function getLayoutStudioFixedCards(style = getCurrentPreviewStyle()) {
    style = normalizeCvStyle(style);
    const fixedCards     = [];
    const hasProfileCard = ['professional', 'creative', 'minimalist', 'classic', 'harvard'].includes(style);
    if (hasProfileCard) fixedCards.push('profileCard');
    if (sectionEnabled('avatar') && document.querySelector('.cvm-avatar-wrapper')) fixedCards.unshift('avatar');
    return fixedCards;
}

function getLayoutStudioColumns(style = getCurrentPreviewStyle()) {
    style   = normalizeCvStyle(style);
    let columns = [];
    if (style === 'creative') {
        columns = [
            getDirectTaggedSections(document.querySelector('.cvc2-left')),
            getDirectTaggedSections(document.querySelector('.cvc2-right'))
        ];
    } else if (style === 'classic') {
        const leftColumn  = getDirectTaggedSections(document.querySelector('.cvcl2-left'));
        const rightColumn = getDirectTaggedSections(document.querySelector('.cvcl2-right'));
        if (sectionEnabled('summary') && getTaggedPreviewSection('summary')) leftColumn.unshift('summary');
        columns = [leftColumn, rightColumn];
    } else if (style === 'harvard') {
        columns = [getDirectTaggedSections(document.querySelector('.cvh-body'))];
    } else if (style === 'minimalist') {
        columns = [getDirectTaggedSections(document.querySelector('.cvm2-body'))];
    } else {
        columns = [
            getDirectTaggedSections(document.querySelector('.cvm-left')),
            getDirectTaggedSections(document.querySelector('.cvm-right'))
        ];
    }
    if (!columns.some(column => column.length)) {
        return (style === 'harvard' || style === 'minimalist')
            ? [getOrderedUsedSections()]
            : [getOrderedUsedSections(), []];
    }
    return columns;
}

function flattenLayoutColumns(columns) {
    const maxLength = columns.reduce((max, col) => Math.max(max, col.length), 0);
    const ordered   = [];
    for (let i = 0; i < maxLength; i++) {
        columns.forEach(col => {
            const s = col[i];
            if (s && !ordered.includes(s)) ordered.push(s);
        });
    }
    return ordered;
}

function syncLayoutOrderFromGrid() {
    const usedContainer = document.getElementById('layoutUsedGrid');
    if (!usedContainer) return;
    const fixedOrder = getLayoutStudioFixedCards(getCurrentPreviewStyle())
        .filter(s => sectionCatalog.includes(s) && sectionEnabled(s));
    const usedOrder  = [
        ...fixedOrder,
        ...[...usedContainer.querySelectorAll('[data-layout-section]')]
            .map(card => card.dataset.layoutSection)
            .filter(s => sectionCatalog.includes(s))
    ];
    designState.order = mergeVisibleSectionOrder(usedOrder);
    applySectionOrderToPreview();
    syncDesignStateToCurrentCv();
    markCvDirty({ immediate: true });
}

function renderLayoutManager() {
    const fixedContainer  = document.getElementById('layoutFixedGrid');
    const usedContainer   = document.getElementById('layoutUsedGrid');
    const unusedContainer = document.getElementById('layoutUnusedGrid');
    if (!fixedContainer || !usedContainer || !unusedContainer) return;

    ensureLayoutSelection();
    const style          = getCurrentPreviewStyle();
    const fixedCards     = getLayoutStudioFixedCards(style);
    const layoutColumns  = getLayoutStudioColumns(style);
    const used           = flattenLayoutColumns(layoutColumns);
    const unused         = getOrderedUnusedSections();
    const isSingleColumn = layoutColumns.filter(c => c.length).length <= 1;
    const boardScroll    = usedContainer.closest('.layout-board-scroll');

    fixedContainer.style.display = fixedCards.length ? 'grid' : 'none';
    fixedContainer.classList.toggle('layout-grid-single', fixedCards.length <= 1);
    fixedContainer.innerHTML = fixedCards.map(createLayoutUsedCard).join('');
    usedContainer.classList.toggle('layout-grid-single', isSingleColumn);
    if (boardScroll) boardScroll.classList.toggle('layout-board-scroll-single', isSingleColumn);

    usedContainer.innerHTML   = used.length
        ? used.map(createLayoutUsedCard).join('')
        : '<div class="layout-empty-state">Chưa có mục nào đang hiển thị.</div>';
    unusedContainer.innerHTML = unused.length
        ? unused.map(createLayoutUnusedCard).join('')
        : '<div class="layout-empty-state">Tất cả mục đang được dùng.</div>';

    if (window.lucide) lucide.createIcons();
    initLayoutSortable();
}

function initLayoutSortable() {
    const usedContainer = document.getElementById('layoutUsedGrid');
    if (!usedContainer || !window.Sortable) return;
    if (usedContainer._sortable) usedContainer._sortable.destroy();
    usedContainer._sortable = Sortable.create(usedContainer, {
        animation: 150,
        handle:    '.layout-section-grip',
        ghostClass:'layout-sortable-ghost',
        onEnd:     () => syncLayoutOrderFromGrid()
    });
}

// ── Section Manager (panel) ───────────────────────────────────
function createSectionCard(sectionName, isUsed) {
    const meta   = sectionMeta[sectionName];
    const action = isUsed
        ? `<button type="button" class="section-card-action secondary" onclick="toggleSection('${sectionName}', false)">Ẩn</button>`
        : `<button type="button" class="section-card-action" onclick="addSection('${sectionName}')">Thêm</button>`;
    return `<div class="section-card ${isUsed ? 'used' : ''}">
        <div class="section-card-main">
            <span class="section-card-glyph">${meta.glyph}</span>
            <div class="section-card-copy">
                <div class="section-card-title">${meta.label}</div>
                <div class="section-card-desc">${meta.desc}</div>
            </div>
        </div>
        ${action}
    </div>`;
}

function renderSectionManager() {
    const unusedContainer = document.getElementById('unusedSectionList');
    const usedContainer   = document.getElementById('usedSectionList');
    if (!unusedContainer || !usedContainer) return;

    const unused = getOrderedUnusedSections();
    const used   = getOrderedUsedSections();

    unusedContainer.innerHTML = unused.length
        ? unused.map(key => createSectionCard(key, false)).join('')
        : '<div class="section-empty-state">Tất cả mục đang được dùng.</div>';
    usedContainer.innerHTML   = used.length
        ? used.map(key => createSectionCard(key, true)).join('')
        : '<div class="section-empty-state">Chưa có mục nào được bật.</div>';

    renderLayoutManager();
}

// ── Save / Open Modal ─────────────────────────────────────────
function openSaveModal() {
    const modal = document.getElementById('saveModal');
    if (!modal) return;
    const input = document.getElementById('cvNameInput');
    if (input && currentCvJson?.name) input.value = currentCvJson.name;
    modal.style.display = 'flex';
}

function closeSaveModal() {
    const modal = document.getElementById('saveModal');
    if (modal) modal.style.display = 'none';
}

function confirmSave() {
    const name = document.getElementById('cvNameInput')?.value?.trim() || 'CV của tôi';
    saveCvToServer(name);
}

async function saveCvToServer(name) {
    if (!currentCvJson) return;
    try {
        syncPreviewToCurrentCv();
        const payload = { ...currentCvJson, _name: name };
        const resp = await fetch('/api/cv/save', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        });
        if (!resp.ok) throw new Error('Save failed');
        closeSaveModal();
        const btnSave = document.getElementById('btnSaveCv');
        if (btnSave) { btnSave.textContent = '✅ Đã lưu'; btnSave.classList.add('saved'); }
    } catch (err) {
        console.error('saveCvToServer:', err);
    }
}

// ── PDF Export ────────────────────────────────────────────────
function downloadPdf() {
    const el = document.getElementById('cvPreview');
    if (!el || !window.html2pdf) return;
    const name = currentCvJson?.name || 'CV';
    const opt  = {
        margin:      [10, 10],
        filename:    `${name}.pdf`,
        image:       { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(el).save();
}

// ── AI panel toggle ───────────────────────────────────────────
function toggleAssistant() {
    const aiCol   = document.querySelector('.assistant-column');
    const textSpan = document.getElementById('toggleAiText');
    aiCol?.classList.toggle('collapsed');
    if (!aiCol || !textSpan) return;
    textSpan.textContent = aiCol.classList.contains('collapsed') ? 'Mở AI' : 'Đóng AI';
}

function primeAssistantSuggestion(text) {
    const input = document.getElementById('chatInput');
    if (input) input.value = text;
}

function openLayoutGuide() {
    alert('Kéo thả các mục trong danh sách để sắp xếp thứ tự xuất hiện trong CV.');
}

function branchChangeTemplate() {
    window.location.href = '/cv-templates.html';
}

// ── Sortable for CV items ─────────────────────────────────────
function initSortable() {
    if (!window.Sortable) return;
    const sectionSelectors = [
        '.cvm-left', '.cvm-right',
        '.cvc2-left', '.cvc2-right',
        '.cvm2-body',
        '.cvcl2-left', '.cvcl2-right',
        '.cvh-body',
        '.cv-topcv-body'
    ];
    sectionSelectors.forEach(selector => {
        const container = document.querySelector(`#cvPreview ${selector}`);
        if (!container || container._sortable) return;
        container._sortable = Sortable.create(container, {
            animation:  150,
            ghostClass: 'sortable-ghost',
            handle:     '.cv-drag-handle',
            onEnd(evt) {
                const orderedSectionNames = [...evt.to.children]
                    .map(child => child?.dataset?.cvSection)
                    .filter(Boolean);
                if (orderedSectionNames.length) {
                    designState.order = mergeVisibleSectionOrder(orderedSectionNames);
                    syncDesignStateToCurrentCv();
                    markCvDirty({ immediate: true });
                }
            }
        });
    });
}
