// ═══════════════════════════════════════════════════════════
//  cv-editor-render.js  —  Style-specific CV renderers
// ═══════════════════════════════════════════════════════════

// ── Shared: normalize cv style tag ───────────────────────────
function normalizeCvStyle(style) {
    const s = String(style || '').trim().toLowerCase();
    if (s === 'minimalist') return 'modern';
    if (s === 'topcv')      return 'ats';
    if (s === 'impressive') return 'impactful';
    if (['professional', 'classic', 'modern', 'impactful', 'creative', 'ats', 'harvard'].includes(s)) return s;
    return 'professional';
}

function getCurrentPreviewStyle() {
    return normalizeCvStyle(
        window.currentTemplate?.styleTag ||
        window.currentCvJson?._styleTag  ||
        'professional'
    );
}

// ── Shared render helpers ─────────────────────────────────────
function esc(str) {
    return String(str || '')
        .replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function cleanPreviewText(text) {
    return String(text || '').replace(/\u00A0/g,' ').replace(/\s{2,}/g,' ').trim();
}

function editableField(tag, text, path, className = '', multiline = false) {
    const cls   = [className, 'cv-editable'].filter(Boolean).join(' ');
    const multi = multiline ? ' data-multiline="true"' : '';
    const val   = multiline ? esc(text || '').replace(/\n/g, '<br>') : esc(text || '');
    return `<${tag} class="${cls}" data-path="${path}"${multi}>${val}</${tag}>`;
}

// ed() — inline editable span with optional placeholder
function ed(text, placeholder = '') {
    const ph = placeholder ? ` data-placeholder="${placeholder}"` : '';
    return `<span class="cv-editable"${ph}>${esc(text || '')}</span>`;
}

// detailsBlock — all bullets as one editable block with placeholder
function detailsBlock(details, className = '', placeholder = '• Nhập nội dung...') {
    const cls  = ['cv-details-block', 'cv-editable', className].filter(Boolean).join(' ');
    const ph   = ` data-placeholder="${placeholder}" data-bullet-block="true" data-multiline="true"`;
    if (!details || details.length === 0) {
        return `<div class="${cls}"${ph}></div>`;
    }
    const text = details.map(d => `\u2022 ${esc(String(d || ''))}`).join('\n');
    return `<div class="${cls}"${ph}>${text.replace(/\n/g, '<br>')}</div>`;
}

function libIcon(name, size = 14, style = '') {
    return `<i data-lucide="${name}" aria-hidden="true" style="width:${size}px;height:${size}px;${style}"></i>`;
}

function getSectionTitleText(sectionKey, fallbackTitle = '') {
    const title = designState?.sectionTitles?.[sectionKey];
    return title == null ? fallbackTitle : String(title);
}

function renderSectionTitle(tagName, sectionKey, fallbackTitle, className = '', attrs = '') {
    const title = getSectionTitleText(sectionKey, fallbackTitle);
    const cls = [className, 'cv-editable', 'cv-section-heading'].filter(Boolean).join(' ');
    const extraAttrs = attrs ? ` ${attrs}` : '';
    return `<${tagName} class="${cls}" data-section-title-key="${sectionKey}" data-placeholder="Tiêu đề mục"${extraAttrs}>${esc(title)}</${tagName}>`;
}

// ── Contact row helpers ───────────────────────────────────────
// All defined contact types (order = display order preference)
const CONTACT_TYPES = [
    { key: 'email',     icon: 'mail',      label: 'Email',          placeholder: 'Email' },
    { key: 'phone',     icon: 'phone',     label: 'Điện thoại',     placeholder: 'Số điện thoại' },
    { key: 'address',   icon: 'map-pin',   label: 'Địa chỉ',        placeholder: 'Địa chỉ' },
    { key: 'linkedin',  icon: 'linkedin',  label: 'LinkedIn',        placeholder: 'linkedin.com/in/...' },
    { key: 'portfolio', icon: 'globe',     label: 'Portfolio',       placeholder: 'portfolio.com' },
    { key: 'github',    icon: 'github',    label: 'GitHub',          placeholder: 'github.com/...' },
    { key: 'website',   icon: 'link',      label: 'Website',         placeholder: 'Website URL' },
    { key: 'zalo',      icon: 'message-circle', label: 'Zalo',      placeholder: 'Zalo số' },
];

// Expose so edit.js can access
window.CONTACT_TYPES = CONTACT_TYPES;

function renderContactItem(key, value, iconKey, iconName, label, placeholder) {
    const iconHtml = typeof renderPreviewIconTrigger === 'function'
        ? renderPreviewIconTrigger(`contact-${key}`, iconName, 14, 'margin-right:4px;', label)
        : libIcon(iconName, 14, 'margin-right:4px;vertical-align:-2px;');
    return `<span class="cv-contact-item" data-contact-key="${key}" contenteditable="false">` +
        // Floating control bar: Xóa + Thêm together
        `<div class="cv-contact-controls" contenteditable="false">` +
        `<button class="btn-item-ctrl btn-item-del cv-contact-del-btn" type="button" onclick="removeContactField('${key}')" title="Xóa ${label}">Xóa</button>` +
        `<button class="btn-item-ctrl btn-item-add cv-contact-add-btn" type="button" onclick="openAddContactMenu(this)" title="Thêm liên hệ"><i data-lucide="plus"></i> Thêm</button>` +
        `</div>` +
        `${iconHtml}` +
        `<span class="cv-editable" data-placeholder="${placeholder}">${esc(value)}</span>` +
        `</span>`;
}

function renderContactRow(c, containerClass = 'cvm-contact-row') {
    const items = CONTACT_TYPES
        .filter(t => c[t.key] !== undefined && c[t.key] !== null)
        .map(t => renderContactItem(t.key, c[t.key] || '', `contact-${t.key}`, t.icon, t.label, t.placeholder))
        .join('');

    // If no contacts at all, show a standalone + Thêm button
    const fallbackAdd = !items
        ? `<button class="btn-item-ctrl btn-item-add cv-contact-add-btn cv-contact-add-standalone" type="button" onclick="openAddContactMenu(this)" contenteditable="false"><i data-lucide="plus"></i> Thêm liên hệ</button>`
        : '';

    return `<div class="${containerClass}" data-cv-section="contacts">${items}${fallbackAdd}</div>`;
}



function getItemControls(type, index) {
    // Always render controls; CSS hides them outside edit mode
    const arr   = Array.isArray(currentCvJson?.[type]) ? currentCvJson[type] : [];
    const first = index === 0;
    const last  = index === arr.length - 1;
    return `<div class="cv-item-controls" data-type="${type}" data-index="${index}" contenteditable="false">
        <button class="btn-item-ctrl btn-item-up"  type="button" onclick="moveItem('${type}',${index},-1)"      ${first?'disabled':''} title="Lên"><i data-lucide="arrow-up"></i></button>
        <button class="btn-item-ctrl btn-item-down" type="button" onclick="moveItem('${type}',${index},1)"       ${last?'disabled':''} title="Xuống"><i data-lucide="arrow-down"></i></button>
        <button class="btn-item-ctrl btn-item-dup"  type="button" onclick="duplicateItem('${type}',${index})" title="Nhân đôi"><i data-lucide="copy"></i></button>
        <button class="btn-item-ctrl btn-item-del"  type="button" onclick="removeItem('${type}',${index})"    title="Xóa">Xóa</button>
        <button class="btn-item-ctrl btn-item-add"  type="button" onclick="addItem('${type}',${index+1})"     title="Thêm"><i data-lucide="plus"></i> Thêm</button>
    </div>`;
}

function getAvatarHtml(borderColor = 'rgba(255,255,255,0.6)', size = 120, shape = 'circle') {
    if (!designState.sections.avatar) return '';
    const r  = shape === 'square' ? '12px' : '50%';
    const ex = avatarDataUrl;
    return `<div class="cvm-avatar-wrapper" style="width:${size}px;height:${size}px;">
        <div class="cvm-avatar" id="cvAvatarPlaceholder" style="border-radius:${r};${ex?'display:none;':''}"></div>
        <img id="cvAvatarImg" src="${ex||''}" alt="Avatar"
            style="width:${size}px;height:${size}px;border-radius:${r};object-fit:cover;border:4px solid ${borderColor};${ex?'':'display:none;'}">
        <div class="avatar-upload-overlay" onclick="triggerAvatarUpload()" title="Thay đổi ảnh" style="border-radius:${r};">${libIcon('camera',18,'color:#fff;')}</div>
    </div>`;
}

// ── STYLE 1: Professional ─────────────────────────────────────
function renderProfessionalStyle(c, accent) {
    const skillsHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvm-skill-group cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvm-skill-cat"><strong>${ed(sg.category||'','Nhóm kỹ năng')}</strong></div>
            <div class="cvm-skill-list">${(sg.items||[]).map(i=>`<span class="cvm-skill-pill">${ed(i,'Kỹ năng')}</span>`).join('')}</div>
        </div>`).join('');

    const eduHtml = (c.education||[]).map((e,idx)=>`
        <div class="cvm-edu-item cv-edit-shell">
            ${getItemControls('education',idx)}
            <div class="cvm-edu-degree">${ed(e.degree||'','Bằng cấp / Chuyên ngành')}</div>
            <div class="cvm-edu-school">${ed(e.school||e.location||'','Tên trường')}</div>
            <div class="cvm-edu-year">${ed(e.period||'','Thời gian (VD: 2020 - 2024)')}</div>
            ${detailsBlock(e.details, 'cvm-edu-detail', '• Thành tích học tập...')}
        </div>`).join('');

    const certHtml = (c.certifications||[]).map((cert,idx)=>`
        <div class="cvm-edu-item cv-edit-shell">
            ${getItemControls('certifications',idx)}
            <div class="cvm-edu-degree">${ed(cert.name||cert.raw||'','Tên chứng chỉ')}</div>
            ${cert.issuer?`<div class="cvm-edu-school">${ed(cert.issuer,'Tổ chức cấp')}</div>`:
                `<div class="cvm-edu-school">${ed('','Tổ chức cấp')}</div>`}
            ${cert.year?`<div class="cvm-edu-year">${ed(cert.year,'Năm')}</div>`:
                `<div class="cvm-edu-year">${ed('','Năm')}</div>`}
        </div>`).join('');

    const awardsHtml = (c.awards||[]).map((aw,idx)=>`
        <div class="cvm-edu-item cv-edit-shell">
            ${getItemControls('awards',idx)}
            <div class="cvm-edu-degree">${libIcon('trophy',14,'vertical-align:-2px;margin-right:6px;')} ${ed(aw.name||aw.raw||'','Tên giải thưởng')}</div>
            <div class="cvm-edu-year">${ed(aw.year||'','Năm')}</div>
        </div>`).join('');

    const expHtml = (c.experience||[]).map((e,idx)=>`
        <div class="cvm-exp-item cv-edit-shell">
            ${getItemControls('experience',idx)}
            <div class="cvm-exp-role">${ed(e.role||'','Chức danh / Vị trí')}</div>
            <div class="cvm-exp-company">${ed(e.company||'','Tên công ty')}</div>
            <div class="cvm-exp-company">${ed(e.period||'','Thời gian (VD: 01/2023 - Hiện tại)')}</div>
            ${detailsBlock(e.details, 'cvm-exp-detail', '• Mô tả công việc, thành tựu và đóng góp nổi bật...')}
        </div>`).join('');

    const projHtml = (c.projects||[]).map((p,idx)=>`
        <div class="cvm-exp-item cv-edit-shell">
            ${getItemControls('projects',idx)}
            <div class="cvm-exp-role">${ed(p.name||'','Tên dự án')}</div>
            <div class="cvm-exp-company">${ed(p.period||'','Thời gian')}</div>
            <div class="cvm-exp-company">${p.tech?`<strong>Tech:</strong> `:''}${ed(p.tech||'','Công nghệ sử dụng')}</div>
            ${detailsBlock(p.details, 'cvm-exp-detail', '• Mô tả dự án, kết quả đạt được...')}
        </div>`).join('');

    const actHtml = (c.activities||[]).map((a,idx)=>`
        <div class="cvm-exp-item cv-edit-shell">
            ${getItemControls('activities',idx)}
            <div class="cvm-exp-role">${ed(a.name||a.raw||'','Tên hoạt động')}</div>
            <div class="cvm-exp-company">${ed(a.role||'','Vai trò của bạn')}</div>
            <div class="cvm-exp-company">${ed(a.period||'','Thời gian')}</div>
            ${detailsBlock(a.details, 'cvm-exp-detail', '• Mô tả hoạt động và đóng góp...')}
        </div>`).join('');

    const refHtml = (c.references||[]).map((ref,idx)=>`
        <div class="cvm-edu-item cv-edit-shell cv-reference-item">
            ${getItemControls('references',idx)}
            <div class="cvm-edu-degree">${ed(ref.name||'','Nguoi tham chieu')}</div>
            <div class="cvm-edu-school">${ed(ref.role||ref.company||'','Chuc danh / Cong ty')}</div>
            <div class="cvm-edu-year">${ed(ref.contact||'','Email / So dien thoai')}</div>
        </div>`).join('');

    const hobbyHtml = (c.hobbies||[]).map((hobby,idx)=>`
        <div class="cvm-edu-item cv-edit-shell cv-hobby-item">
            ${getItemControls('hobbies',idx)}
            <div class="cvm-edu-degree">${ed(hobby.name||'','So thich')}</div>
            <div class="cvm-edu-school">${ed(hobby.description||'','Mo ta ngan')}</div>
        </div>`).join('');

    const summaryHtml = `
        <section class="cvm-section cvm-summary-section" data-cv-section="summary">
            ${renderSectionTitle('h2', 'summary', 'MỤC TIÊU NGHỀ NGHIỆP', 'cvm-section-title', `style="color:${accent};border-bottom:1.5px solid ${accent};"`)}
            <div class="cvm-summary-text-full cv-editable" data-placeholder="Giới thiệu về bản thân, mục tiêu nghề nghiệp và thế mạnh của bạn...">${c.summary?esc(c.summary).replace(/\n/g,'<br>'):''}</div>
        </section>`;

    return `<div class="cv-full" style="--cv-accent:${accent};">
        <header class="cvm-header" style="background:linear-gradient(135deg,${accent},${accent}dd);">
            ${getAvatarHtml()}
            <div class="cvm-header-info">
                <h1 class="cvm-name cv-editable" data-placeholder="Họ và tên ứng viên">${esc(c.name||'')}</h1>
                <div class="cvm-job-title cv-editable" data-placeholder="Vị trí công việc mong muốn">${esc(c.subtitle||'')}</div>
                ${renderContactRow(c, 'cvm-contact-row')}
            </div>
        </header>
        <div class="cvm-body">
            <aside class="cvm-left">
                ${skillsHtml?`<section class="cvm-section" data-cv-section="skills">${renderSectionTitle('h2', 'skills', 'KỸ NĂNG', 'cvm-section-title', `style="color:${accent};"`)}${skillsHtml}</section>`:''}
                ${eduHtml?`<section class="cvm-section" data-cv-section="education">${renderSectionTitle('h2', 'education', 'HỌC VẤN', 'cvm-section-title', `style="color:${accent};"`)}${eduHtml}</section>`:''}
                ${certHtml?`<section class="cvm-section" data-cv-section="certifications">${renderSectionTitle('h2', 'certifications', 'CHỨNG CHỈ', 'cvm-section-title', `style="color:${accent};"`)}${certHtml}</section>`:''}
                ${refHtml?`<section class="cvm-section" data-cv-section="references">${renderSectionTitle('h2', 'references', 'NGUOI THAM CHIEU', 'cvm-section-title', `style="color:${accent};"`)}${refHtml}</section>`:''}
                ${hobbyHtml?`<section class="cvm-section" data-cv-section="hobbies">${renderSectionTitle('h2', 'hobbies', 'SO THICH', 'cvm-section-title', `style="color:${accent};"`)}${hobbyHtml}</section>`:''}
            </aside>
            <main class="cvm-right">
                ${summaryHtml}
                ${expHtml?`<section class="cvm-section" data-cv-section="experience">${renderSectionTitle('h2', 'experience', 'KINH NGHIỆM LÀM VIỆC', 'cvm-section-title', `style="color:${accent};"`)}${expHtml}</section>`:''}
                ${projHtml?`<section class="cvm-section" data-cv-section="projects">${renderSectionTitle('h2', 'projects', 'DỰ ÁN NỔI BẬT', 'cvm-section-title', `style="color:${accent};"`)}${projHtml}</section>`:''}
                ${awardsHtml?`<section class="cvm-section" data-cv-section="awards">${renderSectionTitle('h2', 'awards', 'GIẢI THƯỞNG', 'cvm-section-title', `style="color:${accent};"`)}${awardsHtml}</section>`:''}
                ${actHtml?`<section class="cvm-section" data-cv-section="activities">${renderSectionTitle('h2', 'activities', 'HOẠT ĐỘNG', 'cvm-section-title', `style="color:${accent};"`)}${actHtml}</section>`:''}
            </main>
        </div>
    </div>`;
}

// ── STYLE 2: Creative ─────────────────────────────────────────

function renderCreativeStyle(c, accent) {
    const skillsHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvc2-skill-group cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvc2-skill-cat">${esc(sg.category)}</div>
            <div class="cvc2-tags">${(sg.items||[]).map(i=>`<span class="cvc2-tag">${esc(i)}</span>`).join('')}</div>
        </div>`).join('');

    const cardSection = (type, items) => items.map((item, idx) => {
        const title  = item.role || item.degree || item.name || '';
        const sub    = item.company || item.school || item.period || '';
        const period = item.period || item.year || '';
        const details= item.details || [];
        return `<div class="cvc2-card cv-edit-shell">
            ${getItemControls(type,idx)}
            <div class="cvc2-card-title">${ed(title)}</div>
            <div class="cvc2-card-sub" style="color:${accent}">${ed(sub)} &nbsp;|&nbsp; ${ed(period)}</div>
            ${detailsBlock(details, 'cvc2-card-bullet')}
        </div>`;
    }).join('');

    return `<div class="cv-full cvc2-root" style="--cv-accent:${accent};">
        <header class="cvc2-header" style="background:linear-gradient(45deg,${accent},${accent}cc);">
            ${getAvatarHtml('rgba(255,255,255,0.8)',110,'square')}
            <div class="cvc2-header-right">
                <h1 class="cvc2-name cv-editable">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</h1>
                <div class="cvc2-title cv-editable">${esc(c.subtitle||'Vị trí / Chức danh')}</div>
                ${renderContactRow(c, 'cvc2-contacts')}
            </div>
        </header>
        <div class="cvc2-body">
            <aside class="cvc2-left">
                ${c.summary?`<section class="cvc2-zone" data-cv-section="summary">${renderSectionTitle('h3', 'summary', 'GIỚI THIỆU', 'cvc2-zone-title', `style="color:${accent}"`)}<div class="cvc2-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</div></section>`:''}
                ${skillsHtml?`<section class="cvc2-zone" data-cv-section="skills">${renderSectionTitle('h3', 'skills', 'KỸ NĂNG', 'cvc2-zone-title', `style="color:${accent}"`)}${skillsHtml}</section>`:''}
                ${cardSection('education',c.education||[])?`<section class="cvc2-zone" data-cv-section="education">${renderSectionTitle('h3', 'education', 'HỌC VẤN', 'cvc2-zone-title', `style="color:${accent}"`)}${cardSection('education',c.education||[])}</section>`:''}
                ${cardSection('certifications',c.certifications||[])?`<section class="cvc2-zone" data-cv-section="certifications">${renderSectionTitle('h3', 'certifications', 'CHỨNG CHỈ', 'cvc2-zone-title', `style="color:${accent}"`)}${cardSection('certifications',c.certifications||[])}</section>`:''}
            </aside>
            <main class="cvc2-right">
                ${cardSection('experience',c.experience||[])?`<section class="cvc2-zone" data-cv-section="experience">${renderSectionTitle('h3', 'experience', 'KINH NGHIỆM', 'cvc2-zone-title', `style="color:${accent}"`)}${cardSection('experience',c.experience||[])}</section>`:''}
                ${cardSection('projects',c.projects||[])?`<section class="cvc2-zone" data-cv-section="projects">${renderSectionTitle('h3', 'projects', 'DỰ ÁN', 'cvc2-zone-title', `style="color:${accent}"`)}${cardSection('projects',c.projects||[])}</section>`:''}
                ${cardSection('activities',c.activities||[])?`<section class="cvc2-zone" data-cv-section="activities">${renderSectionTitle('h3', 'activities', 'HOẠT ĐỘNG', 'cvc2-zone-title', `style="color:${accent}"`)}${cardSection('activities',c.activities||[])}</section>`:''}
            </main>
        </div>
    </div>`;
}

// ── STYLE 2b: Impactful (Bold Executive) ─────────────────────
function renderImpactfulStyle(c, accent) {
    const darkBg = '#0f172a';

    const skillsHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvi-skill-group cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvi-skill-cat">${ed(sg.category||'','Nhóm kỹ năng')}</div>
            <div class="cvi-skill-items">${(sg.items||[]).map(i=>`<span class="cvi-skill-pill" style="background:${accent}18;color:${accent};border:1px solid ${accent}30;">${ed(i,'Kỹ năng')}</span>`).join('')}</div>
        </div>`).join('');

    const eduHtml = (c.education||[]).map((e,idx)=>`
        <div class="cvi-side-item cv-edit-shell">
            ${getItemControls('education',idx)}
            <div class="cvi-side-title">${ed(e.degree||'','Bằng cấp')}</div>
            <div class="cvi-side-sub">${ed(e.school||e.location||'','Tên trường')}</div>
            <div class="cvi-side-date">${ed(e.period||'','Thời gian')}</div>
        </div>`).join('');

    const certHtml = (c.certifications||[]).map((cert,idx)=>`
        <div class="cvi-side-item cv-edit-shell">
            ${getItemControls('certifications',idx)}
            <div class="cvi-side-title">${ed(cert.name||cert.raw||'','Tên chứng chỉ')}</div>
            <div class="cvi-side-sub">${ed(cert.issuer||'','Tổ chức cấp')}</div>
            <div class="cvi-side-date">${ed(cert.year||'','Năm')}</div>
        </div>`).join('');

    const expHtml = (c.experience||[]).map((e,idx)=>`
        <div class="cvi-timeline-item cv-edit-shell">
            ${getItemControls('experience',idx)}
            <div class="cvi-timeline-dot" style="background:${accent};"></div>
            <div class="cvi-timeline-content">
                <div class="cvi-timeline-role">${ed(e.role||'','Chức danh')}</div>
                <div class="cvi-timeline-meta">
                    <span class="cvi-timeline-company">${ed(e.company||'','Công ty')}</span>
                    <span class="cvi-timeline-period" style="color:${accent};">${ed(e.period||'','Thời gian')}</span>
                </div>
                ${detailsBlock(e.details, 'cvi-timeline-detail', '• Mô tả công việc...')}
            </div>
        </div>`).join('');

    const projHtml = (c.projects||[]).map((p,idx)=>`
        <div class="cvi-timeline-item cv-edit-shell">
            ${getItemControls('projects',idx)}
            <div class="cvi-timeline-dot" style="background:${accent};"></div>
            <div class="cvi-timeline-content">
                <div class="cvi-timeline-role">${ed(p.name||'','Tên dự án')}</div>
                <div class="cvi-timeline-meta">
                    <span class="cvi-timeline-company">${ed(p.period||'','Thời gian')}</span>
                    ${p.tech?`<span class="cvi-timeline-period">Tech: ${ed(p.tech)}</span>`:''}
                </div>
                ${detailsBlock(p.details, 'cvi-timeline-detail', '• Mô tả dự án...')}
            </div>
        </div>`).join('');

    const actHtml = (c.activities||[]).map((a,idx)=>`
        <div class="cvi-timeline-item cv-edit-shell">
            ${getItemControls('activities',idx)}
            <div class="cvi-timeline-dot" style="background:${accent};"></div>
            <div class="cvi-timeline-content">
                <div class="cvi-timeline-role">${ed(a.name||a.raw||'','Tên hoạt động')}</div>
                <div class="cvi-timeline-meta"><span class="cvi-timeline-company">${ed(a.period||'','Thời gian')}</span></div>
                ${detailsBlock(a.details, 'cvi-timeline-detail', '• Mô tả hoạt động...')}
            </div>
        </div>`).join('');

    const awardsHtml = (c.awards||[]).map((aw,idx)=>`
        <div class="cvi-side-item cv-edit-shell">
            ${getItemControls('awards',idx)}
            <div class="cvi-side-title">${libIcon('trophy',14,'vertical-align:-2px;margin-right:6px;color:'+accent)} ${ed(aw.name||aw.raw||'','Giải thưởng')}</div>
            <div class="cvi-side-date">${ed(aw.year||'','Năm')}</div>
        </div>`).join('');

    const refHtml = (c.references||[]).map((ref,idx)=>`
        <div class="cvi-side-item cv-edit-shell">
            ${getItemControls('references',idx)}
            <div class="cvi-side-title">${ed(ref.name||'','Người tham chiếu')}</div>
            <div class="cvi-side-sub">${ed(ref.role||ref.company||'','Chức danh / Công ty')}</div>
            <div class="cvi-side-date">${ed(ref.contact||'','Email / SĐT')}</div>
        </div>`).join('');

    const hobbyHtml = (c.hobbies||[]).map((hobby,idx)=>`
        <div class="cvi-side-item cv-edit-shell">
            ${getItemControls('hobbies',idx)}
            <div class="cvi-side-title">${ed(hobby.name||'','Sở thích')}</div>
            <div class="cvi-side-sub">${ed(hobby.description||'','Mô tả')}</div>
        </div>`).join('');

    const contactLine = CONTACT_TYPES
        .filter(t => c[t.key] !== undefined && c[t.key] !== null)
        .map(t => `<span class="cvi-contact-item">${libIcon(t.icon,13,'vertical-align:-2px;margin-right:5px;opacity:0.7;')} <span class="cv-editable" data-placeholder="${t.placeholder}">${esc(c[t.key]||'')}</span></span>`)
        .join('');

    return `<div class="cv-full cvi-root" style="--cv-accent:${accent};">
        <div class="cvi-layout">
            <aside class="cvi-sidebar" style="background:${darkBg};">
                <div class="cvi-sidebar-header">
                    ${getAvatarHtml(accent+'80', 100, 'square')}
                    <h1 class="cvi-name cv-editable" data-placeholder="Họ và tên">${esc(c.name||'')}</h1>
                    <div class="cvi-subtitle cv-editable" style="color:${accent};" data-placeholder="Vị trí công việc">${esc(c.subtitle||'')}</div>
                </div>
                <div class="cvi-sidebar-contacts" data-cv-section="contacts">${contactLine||`<button class="btn-item-ctrl btn-item-add cv-contact-add-btn cv-contact-add-standalone" type="button" onclick="openAddContactMenu(this)" contenteditable="false"><i data-lucide="plus"></i> Thêm liên hệ</button>`}</div>
                ${skillsHtml?`<div class="cvi-sidebar-section" data-cv-section="skills"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('skills','KỸ NĂNG')}</div>${skillsHtml}</div>`:''}
                ${eduHtml?`<div class="cvi-sidebar-section" data-cv-section="education"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('education','HỌC VẤN')}</div>${eduHtml}</div>`:''}
                ${certHtml?`<div class="cvi-sidebar-section" data-cv-section="certifications"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('certifications','CHỨNG CHỈ')}</div>${certHtml}</div>`:''}
                ${awardsHtml?`<div class="cvi-sidebar-section" data-cv-section="awards"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('awards','GIẢI THƯỞNG')}</div>${awardsHtml}</div>`:''}
                ${refHtml?`<div class="cvi-sidebar-section" data-cv-section="references"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('references','NGƯỜI THAM CHIẾU')}</div>${refHtml}</div>`:''}
                ${hobbyHtml?`<div class="cvi-sidebar-section" data-cv-section="hobbies"><div class="cvi-sidebar-title" style="color:${accent};">${getSectionTitleText('hobbies','SỞ THÍCH')}</div>${hobbyHtml}</div>`:''}
            </aside>
            <main class="cvi-main">
                ${c.summary?`<section class="cvi-section" data-cv-section="summary">
                    <div class="cvi-section-title" style="border-left:4px solid ${accent};"><span class="cv-editable cv-section-heading" data-section-title-key="summary" data-placeholder="Tiêu đề mục">${getSectionTitleText('summary','GIỚI THIỆU BẢN THÂN')}</span></div>
                    <div class="cvi-summary cv-editable" data-placeholder="Giới thiệu bản thân...">${esc(c.summary).replace(/\n/g,'<br>')}</div>
                </section>`:''}
                ${expHtml?`<section class="cvi-section" data-cv-section="experience">
                    <div class="cvi-section-title" style="border-left:4px solid ${accent};"><span class="cv-editable cv-section-heading" data-section-title-key="experience" data-placeholder="Tiêu đề mục">${getSectionTitleText('experience','KINH NGHIỆM LÀM VIỆC')}</span></div>
                    <div class="cvi-timeline" style="border-left:2px solid ${accent}22;">${expHtml}</div>
                </section>`:''}
                ${projHtml?`<section class="cvi-section" data-cv-section="projects">
                    <div class="cvi-section-title" style="border-left:4px solid ${accent};"><span class="cv-editable cv-section-heading" data-section-title-key="projects" data-placeholder="Tiêu đề mục">${getSectionTitleText('projects','DỰ ÁN NỔI BẬT')}</span></div>
                    <div class="cvi-timeline" style="border-left:2px solid ${accent}22;">${projHtml}</div>
                </section>`:''}
                ${actHtml?`<section class="cvi-section" data-cv-section="activities">
                    <div class="cvi-section-title" style="border-left:4px solid ${accent};"><span class="cv-editable cv-section-heading" data-section-title-key="activities" data-placeholder="Tiêu đề mục">${getSectionTitleText('activities','HOẠT ĐỘNG')}</span></div>
                    <div class="cvi-timeline" style="border-left:2px solid ${accent}22;">${actHtml}</div>
                </section>`:''}
            </main>
        </div>
    </div>`;
}

// ── STYLE 3: Modern (Minimalist Typography) ───────────────────
function renderModernStyle(c, accent) {
    const block = (sectionKey, title, content) => content ? `
        <div class="cvm2-block" data-cv-section="${sectionKey}">
            ${renderSectionTitle('div', sectionKey, title, 'cvm2-block-label', `style="background:${accent};"`)}
            <div class="cvm2-block-body">${content}</div>
        </div>` : '';

    const row = (type, items) => items.map((item, idx) => {
        const period  = item.period || item.year || '';
        const org     = item.company || item.school || item.location || item.issuer || '';
        const title   = item.role || item.degree || item.name || '';
        const details = item.details || [];
        return `<div class="cvm2-row cv-edit-shell">
            ${getItemControls(type,idx)}
            <div class="cvm2-row-meta">
                <div class="cvm2-row-period" style="color:${accent}">${esc(period)}</div>
                <div class="cvm2-row-org">${esc(org)}</div>
            </div>
            <div class="cvm2-row-content">
                <div class="cvm2-row-title">${esc(title)}</div>
                ${detailsBlock(details, 'cvm2-bullet')}
            </div>
        </div>`;
    }).join('');

    const skillHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvm2-row cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvm2-row-period" style="color:${accent}">${esc(sg.category||'')}</div>
            <div class="cvm2-row-content"><div class="cvm2-row-org">${esc((sg.items||[]).join(', '))}</div></div>
        </div>`).join('');

    return `<div class="cv-full cvm2-root" style="--cv-accent:${accent};">
        <header class="cvm2-header">
            <div class="cvm2-header-bar" style="background:${accent};"></div>
            <div class="cvm2-header-content">
                <h1 class="cvm2-name cv-editable">${esc(c.name||'HỌ TÊN')}</h1>
                <div class="cvm2-subtitle cv-editable">${esc(c.subtitle||'')}</div>
                <div class="cvm2-contacts" data-cv-section="contacts">
                    ${c.email?`<span>${libIcon('mail',14,'vertical-align:-2px;margin-right:6px;')} ${esc(c.email)}</span>`:''}
                    ${c.phone?`<span class="cvm2-sep">•</span><span>${libIcon('phone',14,'vertical-align:-2px;margin-right:6px;')} ${esc(c.phone)}</span>`:''}
                    ${c.address?`<span class="cvm2-sep">•</span><span>${libIcon('map-pin',14,'vertical-align:-2px;margin-right:6px;')} ${esc(c.address)}</span>`:''}
                </div>
            </div>
        </header>
        <div class="cvm2-body">
            ${c.summary?block('summary', 'GIỚI THIỆU', `<p class="cvm2-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p>`):''}
            ${row('experience',c.experience||[])?block('experience', 'KINH NGHIỆM', row('experience',c.experience||[])):''}
            ${row('projects',c.projects||[])?block('projects', 'DỰ ÁN', row('projects',c.projects||[])):''}
            ${row('education',c.education||[])?block('education', 'HỌC VẤN', row('education',c.education||[])):''}
            ${skillHtml?block('skills', 'KỸ NĂNG', skillHtml):''}
            ${row('certifications',c.certifications||[])?block('certifications', 'CHỨNG CHỈ', row('certifications',c.certifications||[])):''}
            ${row('awards',c.awards||[])?block('awards', 'GIẢI THƯỞNG', row('awards',c.awards||[])):''}
            ${row('activities',c.activities||[])?block('activities', 'HOẠT ĐỘNG', row('activities',c.activities||[])):''}
        </div>
    </div>`;
}

// ── STYLE 4: Classic ──────────────────────────────────────────
function renderClassicStyle(c, accent) {
    const hr = accent || '#1a1a1a';

    const lsec = (sectionKey, title, content) => content ? `
        <div class="cvcl2-section" data-cv-section="${sectionKey}">
            ${renderSectionTitle('div', sectionKey, title, 'cvcl2-sec-title', `style="color:${hr};border-left:4px solid ${hr};"`)}
            ${content}
        </div>` : '';

    const rsec = (sectionKey, title, content) => content ? `
        <div class="cvcl2-section" data-cv-section="${sectionKey}">
            ${renderSectionTitle('div', sectionKey, title, 'cvcl2-sec-title-r', `style="border-bottom:2px solid ${hr};color:${hr};"`)}
            ${content}
        </div>` : '';

    const expHtml = (c.experience||[]).map((e,idx)=>`
        <div class="cvcl2-item cv-edit-shell">
            ${getItemControls('experience',idx)}
            <div class="cvcl2-item-top">
                <span class="cvcl2-role cv-editable">${esc(e.role||'')}</span>
                <span class="cvcl2-date" style="color:${hr}">${ed(e.period||'')}</span>
            </div>
            <div class="cvcl2-company cv-editable">${esc(e.company||'')}</div>
            ${detailsBlock(e.details, 'cvcl2-bullet')}
        </div>`).join('');

    const projHtml = (c.projects||[]).map((p,idx)=>`
        <div class="cvcl2-item cv-edit-shell">
            ${getItemControls('projects',idx)}
            <div class="cvcl2-item-top">
                <span class="cvcl2-role cv-editable">${esc(p.name||'')}</span>
                <span class="cvcl2-date" style="color:${hr}">${ed(p.period||'')}</span>
            </div>
            ${p.tech?`<div class="cvcl2-company">Tech: ${ed(p.tech)}</div>`:''}
            ${detailsBlock(p.details, 'cvcl2-bullet')}
        </div>`).join('');

    const eduHtml = (c.education||[]).map((e,idx)=>`
        <div class="cvcl2-r-item cv-edit-shell">
            ${getItemControls('education',idx)}
            <div class="cvcl2-r-degree cv-editable">${esc(e.degree||'')}</div>
            <div class="cvcl2-r-school cv-editable">${esc(e.school||e.location||'')}</div>
            <div class="cvcl2-r-period" style="color:${hr}">${ed(e.period||'')}</div>
        </div>`).join('');

    const skillHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvcl2-skill cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvcl2-skill-cat"><strong>${ed(sg.category||'')}</strong></div>
            <div class="cvcl2-skill-vals">${(sg.items||[]).map(i=>`<span class="cvcl2-pill" style="border-color:${hr}22;background:${hr}0d;color:${hr};">${ed(i)}</span>`).join('')}</div>
        </div>`).join('');

    const contactBits = [
        c.email?`<span class="cv-editable">${esc(c.email)}</span>`:'',
        c.phone?`<span class="cv-editable">${esc(c.phone)}</span>`:'',
        c.address?`<span class="cv-editable">${esc(c.address)}</span>`:'',
    ].filter(Boolean).join(' · ');

    return `<div class="cv-full cvcl2-root" style="--cv-accent:${hr};">
        <div class="cvcl2-header" style="border-top:5px solid ${hr};">
            <div class="cvcl2-header-left">${getAvatarHtml(hr+'80',100,'square')}</div>
            <div class="cvcl2-header-center">
                <div class="cvcl2-name cv-editable">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</div>
                <div class="cvcl2-subtitle cv-editable" style="color:${hr}">${esc(c.subtitle||'')}</div>
                <div class="cvcl2-contact" data-cv-section="contacts">${contactBits}</div>
            </div>
        </div>
        ${c.summary?`<div class="cvcl2-summary-bar" data-cv-section="summary" style="border-left:4px solid ${hr};"><p class="cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p></div>`:''}
        <div class="cvcl2-body">
            <div class="cvcl2-left">
                ${lsec('experience', 'KINH NGHIỆM', expHtml)}
                ${lsec('projects', 'DỰ ÁN', projHtml)}
            </div>
            <div class="cvcl2-right">
                ${rsec('education', 'HỌC VẤN', eduHtml)}
                ${rsec('skills', 'KỸ NĂNG', skillHtml)}
            </div>
        </div>
    </div>`;
}

// ── STYLE 5: Harvard ──────────────────────────────────────────
function renderHarvardStyle(c, accent) {
    const section = (sectionKey, title, content) => content ? `
        <div class="cvh-section" data-cv-section="${sectionKey}">
            ${renderSectionTitle('div', sectionKey, title, 'cvh-title')}
            ${content}
        </div>` : '';

    const itemRow = (type, items) => items.map((item, idx) => {
        const title  = item.role || item.degree || item.name || '';
        const sub    = item.company || item.school || '';
        const period = item.period || '';
        const details= item.details || [];
        return `<div class="cvh-item cvh-edit-shell">
            ${getItemControls(type,idx)}
            <div class="cvh-item-head">
                <span class="cvh-item-title cv-editable">${esc(title)}</span>
                <span class="cvh-item-date cv-editable">${esc(period)}</span>
            </div>
            ${sub?`<div class="cvh-item-sub cv-editable">${esc(sub)}</div>`:''}
            ${item.tech?`<div class="cvh-item-tech cv-editable">${esc(item.tech)}</div>`:''}
            ${item.github?`<div class="cvh-bullet cvh-github-bullet cv-editable">${esc(item.github)}</div>`:''}
            ${detailsBlock(details, 'cvh-bullet')}
        </div>`;
    }).join('');

    const skillHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cvh-skill-row cvh-edit-shell">
            ${getItemControls('skills',idx)}
            <div class="cvh-skill-cat cv-editable">${esc(sg.category||'')}</div>
            <div class="cvh-skill-items cv-editable">${esc((sg.items||[]).join(', '))}</div>
        </div>`).join('');

    // Contact line with icons matching the PDF layout
    const contactItems = CONTACT_TYPES
        .filter(t => c[t.key] !== undefined && c[t.key] !== null && c[t.key] !== '')
        .map(t => `<span class="cvh-contact-item" data-contact-key="${t.key}">${libIcon(t.icon, 13, 'vertical-align:-2px;margin-right:4px;')} <span class="cv-editable" data-placeholder="${t.placeholder}">${esc(c[t.key]||'')}</span></span>`)
        .join('');

    return `<div class="cv-full cvh-root">
        <div class="cvh-header">
            <div class="cvh-name cv-editable" data-placeholder="HỌ VÀ TÊN">${esc(c.name||'')}</div>
            <div class="cvh-subtitle cv-editable" data-placeholder="VỊ TRÍ CÔNG VIỆC">${esc(c.subtitle||'')}</div>
            <div class="cvh-contact" data-cv-section="contacts">${contactItems||`<button class="btn-item-ctrl btn-item-add cv-contact-add-btn cv-contact-add-standalone" type="button" onclick="openAddContactMenu(this)" contenteditable="false"><i data-lucide="plus"></i> Thêm liên hệ</button>`}</div>
        </div>
        <div class="cvh-body">
            ${skillHtml?section('skills', 'SKILLS', `<div class="cvh-skill-table">${skillHtml}</div>`):''}
            ${section('education', 'EDUCATION', itemRow('education',c.education||[]))}
            ${section('projects', 'PROJECTS', itemRow('projects',c.projects||[]))}
            ${section('experience', 'EXPERIENCE', itemRow('experience',c.experience||[]))}
            ${c.summary?section('summary', 'PROFESSIONAL SUMMARY', `<p class="cvh-summary cv-editable">${esc(c.summary).replace(/\n/g,'<br>')}</p>`):''}
            ${itemRow('certifications',c.certifications||[])?section('certifications', 'CERTIFICATIONS', itemRow('certifications',c.certifications||[])):''}
            ${itemRow('awards',c.awards||[])?section('awards', 'AWARDS', itemRow('awards',c.awards||[])):''}
            ${itemRow('activities',c.activities||[])?section('activities', 'ACTIVITIES', itemRow('activities',c.activities||[])):''}
        </div>
    </div>`;
}

// ── STYLE 6: ATS (TopCV-style clean single column) ────────────
function renderAtsStyle(c, accent) {
    const sec = (sectionKey, title, content) => content ? `
        <div class="cv-topcv-section" data-cv-section="${sectionKey}">
            ${renderSectionTitle('h2', sectionKey, title, 'cv-topcv-title', 'style="color:#000;border-bottom:2px solid #333;"')}
            <div class="cv-topcv-content">${content}</div>
        </div>` : '';

    const rowHtml = (type, items) => items.map((item, idx) => {
        const title  = item.role || item.degree || item.name || '';
        const org    = item.company || item.school || item.location || '';
        const period = item.period || item.year || '';
        const tech   = item.tech || '';
        const details= item.details || [];
        return `<div class="cv-topcv-row cv-edit-shell">
            ${getItemControls(type,idx)}
            <div style="display:flex;justify-content:space-between;">
                <strong>${ed(org || title)}</strong>
                <span style="font-style:italic;color:#777;">${ed(period)}</span>
            </div>
            ${org&&title?`<div style="margin-top:4px;font-weight:500;">${ed(title)}</div>`:''}
            ${tech?`<div style="margin-top:4px;">Tech: ${ed(tech)}</div>`:''}
            ${detailsBlock(details, 'cv-ats-detail')}
        </div>`;
    }).join('');

    const skillHtml = (c.skills||[]).map((sg,idx)=>`
        <div class="cv-topcv-row cv-edit-shell">
            ${getItemControls('skills',idx)}
            <div style="flex:0 0 30%;font-style:italic;color:#555;">${ed(sg.category||'')}</div>
            <div style="flex:1;color:#777;">${ed((sg.items||[]).join(', '))}</div>
        </div>`).join('');

    const contactHtml = [
        c.phone?`<span>${libIcon('phone',14,'vertical-align:-2px;')} <span class="cv-editable">${esc(c.phone)}</span></span>`:'',
        c.email?`<span>${libIcon('mail',14,'vertical-align:-2px;')} <span class="cv-editable">${esc(c.email)}</span></span>`:'',
        c.address?`<span>${libIcon('map-pin',14,'vertical-align:-2px;')} <span class="cv-editable">${esc(c.address)}</span></span>`:'',
    ].filter(Boolean).join(' &nbsp;|&nbsp; ');

    return `<div class="cv-full cv-topcv-root" style="--cv-accent:${accent};">
        <header style="text-align:center;margin-bottom:30px;">
            <h1 class="cv-editable" style="font-size:36px;font-weight:bold;margin:0 0 10px;">${esc(c.name||'HỌ TÊN ỨNG VIÊN')}</h1>
            <div class="cv-editable" style="font-size:16px;font-weight:bold;text-transform:uppercase;margin-bottom:15px;">${esc(c.subtitle||'VỊ TRÍ CÔNG VIỆC')}</div>
            <div style="display:flex;justify-content:center;gap:15px;flex-wrap:wrap;" data-cv-section="contacts">${contactHtml}</div>
        </header>
        <div class="cv-topcv-body">
            ${c.summary?sec('summary', 'TÓM TẮT', `<div class="cv-editable" style="font-style:italic;color:#555;">${esc(c.summary).replace(/\n/g,'<br>')}</div>`):''}
            ${skillHtml?sec('skills', 'KỸ NĂNG', skillHtml):''}
            ${rowHtml('education',c.education||[])?sec('education', 'HỌC VẤN', rowHtml('education',c.education||[])):''}
            ${rowHtml('experience',c.experience||[])?sec('experience', 'KINH NGHIỆM LÀM VIỆC', rowHtml('experience',c.experience||[])):''}
            ${rowHtml('projects',c.projects||[])?sec('projects', 'DỰ ÁN', rowHtml('projects',c.projects||[])):''}
            ${rowHtml('certifications',c.certifications||[])?sec('certifications', 'CHỨNG CHỈ', rowHtml('certifications',c.certifications||[])):''}
        </div>
    </div>`;
}

// ── MAIN DISPATCHER ───────────────────────────────────────────
function renderCvPreview(c) {
    currentCvJson = c;
    window.currentCvJson = c;
    avatarDataUrl = c.avatarUrl || c.avatarDataUrl || avatarDataUrl || null;
    if (avatarDataUrl) c.avatarUrl = avatarDataUrl;
    else delete c.avatarUrl;
    delete c.avatarDataUrl;

    hydrateDesignState(c._designState || {});
    if (typeof syncSectionStateFromCv === 'function') syncSectionStateFromCv(c);
    const accent = c._accent || c._designState?.color || window.currentTemplate?.previewColor || designState.color || '#1f4b99';
    designState.color = accent;
    window.currentTemplate = window.currentTemplate || {};
    window.currentTemplate.previewColor = accent;
    c._accent    = accent;
    c._designState = cloneDesignState();

    const style  = normalizeCvStyle(window.currentTemplate?.styleTag || c._styleTag || 'professional');
    c._styleTag  = style;

    let html = '';
    if      (style === 'creative')   html = renderCreativeStyle(c, accent).replace('class="cv-full cvc2-root"','class="cv-full cvc2-root cv-creative-root"');
    else if (style === 'impactful')  html = renderImpactfulStyle(c, accent).replace('class="cv-full cvi-root"','class="cv-full cvi-root cv-impactful-root"');
    else if (style === 'modern')     html = renderModernStyle(c, accent);
    else if (style === 'classic')    html = renderClassicStyle(c, accent).replace('class="cv-full cvcl2-root"','class="cv-full cvcl2-root cv-classic-root"');
    else if (style === 'harvard')    html = renderHarvardStyle(c, accent).replace('class="cv-full cvh-root"','class="cv-full cvh-root cv-harvard-root"');
    else if (style === 'ats')        html = renderAtsStyle(c, accent);
    else                             html = renderProfessionalStyle(c, accent).replace('class="cv-full"','class="cv-full cv-professional-root"');

    document.getElementById('cvPreview').innerHTML = html;

    if (isEditMode) document.getElementById('cvPreview').classList.add('cv-editable-mode');

    applyDesignStateToPreview();
    tagPreviewSections(style);
    applySectionVisibilityToPreview();
    applySectionOrderToPreview(style);
    normalizePreviewIcons(document.getElementById('cvPreview'));
    enhancePreviewIconTriggers(document.getElementById('cvPreview'));
    normalizeSectionMetaIcons(document);
    renderSectionManager();
    refreshEditModeUI();
    initSortable();
    if (window.lucide) lucide.createIcons();
    setActiveItemShell(null, document.getElementById('cvPreview'));
    syncEditorHistoryButtons();
}

// Expose globally so IIFE wrappers can wrap it
window.renderCvPreview = renderCvPreview;

// Helper for current preview/section rendering
function renderCurrentPreview() {
    if (currentCvJson) { renderCvPreview(currentCvJson); return; }
    if (typeof cvData === 'object' && cvData?.name) { renderCvPreview(cvData); return; }
    if (typeof templateData === 'object' && templateData?.name) { renderCvPreview(templateData); }
}

function rerenderCurrentPreview() { renderCurrentPreview(); }

// Item CRUD helpers
function moveItem(type, index, direction) {
    if (!currentCvJson || !Array.isArray(currentCvJson[type])) return;
    const arr = currentCvJson[type];
    const ni  = index + direction;
    if (index < 0 || index >= arr.length || ni < 0 || ni >= arr.length) return;
    [arr[index], arr[ni]] = [arr[ni], arr[index]];
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

function removeItem(type, index) {
    if (!currentCvJson || !Array.isArray(currentCvJson[type])) return;
    if (!confirm('Bạn có chắc muốn xóa mục này?')) return;
    currentCvJson[type].splice(index, 1);
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

function duplicateItem(type, index) {
    if (!currentCvJson || !Array.isArray(currentCvJson[type])) return;
    const clone = JSON.parse(JSON.stringify(currentCvJson[type][index]));
    currentCvJson[type].splice(index + 1, 0, clone);
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

function addItem(type, insertIndex = null) {
    if (!currentCvJson) return;
    if (!currentCvJson[type]) currentCvJson[type] = [];
    const defaults = {
        skills:         { category: '', items: [''] },
        experience:     { role: '', company: '', period: '', details: [''] },
        education:      { degree: '', school: '', period: '', details: [''] },
        projects:       { name: '', period: '', tech: '', github: '', details: [''] },
        certifications: { name: '', issuer: '', year: '' },
        awards:         { name: '', year: '' },
        activities:     { name: '', role: '', period: '', details: [''] },
        references:     { name: '', role: '', company: '', contact: '' },
        hobbies:        { name: '', description: '' }
    };
    const newItem = defaults[type] || {};
    if (Number.isInteger(insertIndex) && insertIndex >= 0 && insertIndex <= currentCvJson[type].length) {
        currentCvJson[type].splice(insertIndex, 0, newItem);
    } else {
        currentCvJson[type].push(newItem);
    }
    renderCvPreview(currentCvJson);
    markCvDirty({ immediate: true });
}

// Normalize preview emoji → lucide icons
function normalizePreviewIcons(preview = document.getElementById('cvPreview')) {
    if (!preview) return;
    const iconDefs = [
        { chars: ['📧','✉','✉️'], icon: 'mail' },
        { chars: ['📱','☎','☏','✆','📞'], icon: 'phone' },
        { chars: ['📍','📌'], icon: 'map-pin' },
        { chars: ['💼'], icon: 'briefcase' },
        { chars: ['🏆'], icon: 'trophy' },
        { chars: ['📷'], icon: 'camera' }
    ];
    const escaped = iconDefs.map(({chars})=>chars.map(c=>c.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')).join('|'));
    const anyRegex = new RegExp(`^(?:${escaped.join('|')})\\s*`,'u');

    const walker = document.createTreeWalker(preview, NodeFilter.SHOW_TEXT);
    const nodes  = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);
    nodes.forEach(textNode => {
        const parent  = textNode.parentElement;
        if (!parent || parent.closest('.cv-item-controls')) return;
        const raw     = textNode.textContent || '';
        const matched = iconDefs.find(({chars})=>chars.some(c=>raw.trimStart().startsWith(c)));
        if (matched && !parent.querySelector(':scope > [data-lucide]')) {
            const iconKey = inferPreviewIconKeyFromNode(parent, matched.icon);
            parent.insertAdjacentHTML('afterbegin', renderPreviewIconTrigger(iconKey, matched.icon, 14, 'margin-right:6px;', parent.textContent||'icon'));
        }
        textNode.textContent = raw.replace(anyRegex, '');
    });
}
