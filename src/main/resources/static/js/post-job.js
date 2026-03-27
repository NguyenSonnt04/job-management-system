// ===== TOAST NOTIFICATION =====
function showToast(message, type = 'success') {
    const existing = document.getElementById('job-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.id = 'job-toast';
    toast.style.cssText = `
        position: fixed; top: 24px; right: 24px; z-index: 9999;
        padding: 14px 20px; border-radius: 10px; font-size: 15px;
        font-weight: 600; display: flex; align-items: center; gap: 10px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.18); max-width: 380px;
        animation: slideInRight 0.3s ease;
        ${type === 'success'
            ? 'background: #1a8c5b; color: #fff;'
            : 'background: #dc3545; color: #fff;'}
    `;

    const icon = type === 'success' ? '✅' : '❌';
    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, type === 'success' ? 2000 : 4000);
}

// Inject toast animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { opacity: 0; transform: translateX(40px); }
        to   { opacity: 1; transform: translateX(0); }
    }
    @keyframes slideOutRight {
        from { opacity: 1; transform: translateX(0); }
        to   { opacity: 0; transform: translateX(40px); }
    }
    .btn-submit-loading {
        opacity: 0.7; cursor: not-allowed; position: relative;
    }
    .btn-submit-loading::after {
        content: '';
        display: inline-block; width: 14px; height: 14px;
        border: 2px solid #fff; border-top-color: transparent;
        border-radius: 50%; animation: spin 0.7s linear infinite;
        margin-left: 8px; vertical-align: middle;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
`;
document.head.appendChild(style);


// ===== AUTO-FILL EMPLOYER INFO =====
async function loadEmployerInfo() {
    try {
        const response = await fetch('/api/jobs/employer-info', { credentials: 'include' });

        if (response.status === 403) {
            showToast('Vui lòng đăng nhập với tài khoản nhà tuyển dụng.', 'error');
            setTimeout(() => { window.location.href = '/employer-login.html'; }, 2000);
            return;
        }

        if (response.ok) {
            const info = await response.json();
            window.employerInfo = info;
            showCompanyInfo(info);
        } else {
            console.warn('Không tải được thông tin công ty:', response.status);
        }
    } catch (error) {
        console.error('Lỗi kết nối khi tải thông tin công ty:', error);
    }
}


// ===== LOAD FORM OPTIONS FROM API =====
async function loadFormOptions() {
    try {
        const res = await fetch('/api/jobs/form-options');
        if (!res.ok) throw new Error('Cannot load form options');
        const opts = await res.json();

        const benefitIconMap = {
            'che-do-bao-hiem': 'shield',
            'bao-hiem': 'shield',
            'cham-soc-suc-khoe': 'heart',
            'suc-khoe': 'heart',
            'du-lich': 'plane',
            'du-lich-nuoc-ngoai': 'globe',
            'dao-tao': 'book',
            'tang-luong': 'arrow-up',
            'laptop': 'laptop',
            'cong-tac-phi': 'wallet',
            'nghi-phep-nam': 'calendar',
            'thuong-kpi': 'award',
            'xe-dua-don': 'car',
            'an-trua': 'utensils',
            'che-do-thuong': 'gift'
        };

        function normalizeKey(value) {
            return String(value || '')
                .normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
                .toLowerCase()
                .replace(/đ/g, 'd')
                .replace(/[^a-z0-9]+/g, '-')
                .replace(/^-+|-+$/g, '');
        }

        function getMonoIcon(name) {
            const icons = {
                shield: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M10 2.2 15.4 4.3V8.8c0 3.2-2.1 6-5.4 7.5-3.3-1.5-5.4-4.3-5.4-7.5V4.3L10 2.2Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>`,
                heart: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M10 16.1 3.8 10.3a3.9 3.9 0 0 1 5.5-5.6L10 5.4l.7-.7a3.9 3.9 0 1 1 5.5 5.6L10 16.1Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>`,
                plane: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="m17 3-6.7 14-1.9-6.1L2.3 8.9 17 3Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><path d="m8.4 10.9 3.1-3.1" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
                globe: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.6"/><path d="M3 10h14M10 3a12 12 0 0 1 0 14M10 3a12 12 0 0 0 0 14" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>`,
                book: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M4.5 4.2h8.6a2 2 0 0 1 2 2v9.6H6.5a2 2 0 0 0-2 2V4.2Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><path d="M6.5 15.8h9" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
                'arrow-up': `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M10 15V5M10 5 6.6 8.4M10 5l3.4 3.4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
                laptop: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><rect x="4.2" y="4" width="11.6" height="8" rx="1.2" stroke="currentColor" stroke-width="1.6"/><path d="M2.8 14.5h14.4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
                wallet: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M4 6.2h11a1.8 1.8 0 0 1 1.8 1.8v6a1.8 1.8 0 0 1-1.8 1.8H4a1.8 1.8 0 0 1-1.8-1.8V8A1.8 1.8 0 0 1 4 6.2Z" stroke="currentColor" stroke-width="1.6"/><path d="M13.3 10h3.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
                calendar: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><rect x="3.2" y="4.2" width="13.6" height="12.2" rx="1.8" stroke="currentColor" stroke-width="1.6"/><path d="M6.5 2.8v2.7M13.5 2.8v2.7M3.2 7.3h13.6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
                award: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="10" cy="7" r="3.6" stroke="currentColor" stroke-width="1.6"/><path d="M7.7 10.1 6.4 16l3.6-2.1 3.6 2.1-1.3-5.9" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>`,
                car: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M4.4 12.8 5.6 8.7A1.8 1.8 0 0 1 7.3 7.4h5.4a1.8 1.8 0 0 1 1.7 1.3l1.2 4.1" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><path d="M3.5 12.8h13v2.1a1 1 0 0 1-1 1h-1.1v-1.8H5.6v1.8H4.5a1 1 0 0 1-1-1v-2.1Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><circle cx="6.3" cy="13.1" r="1" fill="currentColor"/><circle cx="13.7" cy="13.1" r="1" fill="currentColor"/></svg>`,
                utensils: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M5.2 3.2v5.1M7.6 3.2v5.1M5.2 5.8h2.4M6.4 8.3V16M12.4 3.2c1.6 1.3 1.6 4.6 0 5.9V16" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
                gift: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M3.4 8.1h13.2v8.5H3.4z" stroke="currentColor" stroke-width="1.6"/><path d="M2.6 5.4h14.8v2.7H2.6zM10 5.4v11.2M7.3 5.4c-1.5 0-2.4-.8-2.4-1.9 0-1 .8-1.7 1.8-1.7 1.5 0 2.5 1.5 3.3 3.6M12.7 5.4c1.5 0 2.4-.8 2.4-1.9 0-1-.8-1.7-1.8-1.7-1.5 0-2.5 1.5-3.3 3.6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
                home: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M3.3 8.7 10 3.3l6.7 5.4v7.5H3.3V8.7Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><path d="M8 16.2v-4.6h4v4.6" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>`,
                office: `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M4 16.4V4.8c0-.7.6-1.2 1.2-1.2h6.3c.7 0 1.2.5 1.2 1.2v11.6M2.8 16.4h14.4M7 7.1h2M7 9.9h2M7 12.7h2M12.7 16.4V8.6h2.1c.7 0 1.2.5 1.2 1.2v6.6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`
            };

            return icons[name] || icons.gift;
        }

        // Populate select helper
        function populateSelect(id, items, placeholder) {
            const sel = document.getElementById(id);
            if (!sel) return;
            sel.innerHTML = `<option value="">${placeholder}</option>` +
                items.map(i => `<option value="${i.value}">${i.label}</option>`).join('');
        }

        populateSelect('industry',       opts.industries,      'Chọn ngành nghề');
        populateSelect('experience',     opts.experiences,     'Chọn kinh nghiệm');
        populateSelect('educationLevel', opts.educationLevels, 'Chọn cấp bậc');
        populateSelect('degreeLevel',    opts.degreeLevels,    'Không yêu cầu bằng cấp');

        // Populate datalist for location
        const dl = document.getElementById('provinceList');
        if (dl && opts.provinces) {
            dl.innerHTML = opts.provinces.map(p => `<option value="${p}">`).join('');
        }

        // Render benefits checkboxes dynamically
        const grid = document.getElementById('benefitsGrid');
        if (grid && opts.benefits) {
            grid.innerHTML = opts.benefits.map(b => `
                <label class="checkbox-label">
                    <input type="checkbox" name="benefits" value="${b.value}">
                    <span class="option-icon" aria-hidden="true">${getMonoIcon(benefitIconMap[normalizeKey(b.value)] || benefitIconMap[normalizeKey(b.label)] || 'gift')}</span>
                    <span>${b.label}</span>
                </label>
            `).join('');
        }

    } catch (err) {
        console.warn('Không tải được form-options, dùng fallback:', err);
        // Fallback nếu API lỗi
        const sel = document.getElementById('industry');
        if (sel) sel.innerHTML = `
            <option value="">Chọn ngành nghề</option>
            <option value="CNTT">Công nghệ thông tin</option>
            <option value="Marketing">Marketing</option>
            <option value="Kinh doanh">Kinh doanh</option>
        `;
    }
}


// ===== SHOW COMPANY INFO =====
function renderCompanyLogo(info) {
    const logoImage = document.getElementById('companyLogoImage');
    const logoFallback = document.getElementById('companyLogoFallback');
    if (!logoImage || !logoFallback) return;

    const initials = String(info?.companyName || 'CT')
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map(part => part.charAt(0))
        .join('')
        .toUpperCase() || 'CT';
    logoFallback.textContent = initials;

    if (info?.logoUrl) {
        logoImage.src = info.logoUrl;
        logoImage.style.display = 'block';
        logoFallback.style.display = 'none';
        logoImage.onerror = () => {
            logoImage.style.display = 'none';
            logoFallback.style.display = 'inline-flex';
        };
        return;
    }

    logoImage.removeAttribute('src');
    logoImage.style.display = 'none';
    logoFallback.style.display = 'inline-flex';
}

async function uploadCompanyLogo(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/jobs/employer-logo', {
        method: 'POST',
        credentials: 'include',
        body: formData
    });

    const result = await response.json().catch(() => ({}));
    if (!response.ok || !result.success) {
        throw new Error(result.message || 'Không thể tải logo công ty lên');
    }

    window.employerInfo = {
        ...(window.employerInfo || {}),
        logoUrl: result.logoUrl
    };
    renderCompanyLogo(window.employerInfo);
    showToast(result.message || 'Cập nhật logo công ty thành công', 'success');
}

function showCompanyInfo(info) {
    const section = document.getElementById('companyInfoSection');
    const sidebar = document.getElementById('companyInfoSidebar');
    const formLayout = document.getElementById('jobPostForm');
    if (!section) return;

    document.getElementById('displayCompanyName').textContent = info.companyName || '—';
    document.getElementById('displayAddress').textContent     = info.address     || '—';
    document.getElementById('displayContactName').textContent = info.contactName || '—';
    document.getElementById('displayContactPhone').textContent = info.contactPhone || '—';
    document.getElementById('displayEmail').textContent       = info.email       || '—';
    renderCompanyLogo(info);

    if (sidebar) {
        sidebar.style.display = 'block';
    }
    if (formLayout) {
        formLayout.classList.add('has-company-sidebar');
    }
    section.style.display = 'block';
}


// ===== COLLECT FORM DATA =====
function collectFormData() {
    const employmentTypes = Array.from(
        document.querySelectorAll('input[name="employmentType"]:checked')
    ).map(cb => cb.value).join(', ');

    const benefits = JSON.stringify(
        Array.from(document.querySelectorAll('input[name="benefits"]:checked'))
            .map(cb => cb.value)
    );

    const resumeLanguage = Array.from(
        document.querySelectorAll('input[name="languages"]:checked')
    ).map(cb => cb.value).join(', ') || 'tieng-viet';

    return {
        title:              document.getElementById('title').value.trim(),
        jobCode:            document.getElementById('jobCode').value.trim() || null,
        industry:           document.getElementById('industry').value || null,
        location:           document.getElementById('location').value.trim(),
        hideLocation:       document.getElementById('hideLocation').checked,
        description:        document.getElementById('description').value.trim(),
        requirements:       document.getElementById('requirements').value.trim(),
        videoUrl1:          document.getElementById('videoUrl1').value.trim() || null,
        videoUrl2:          document.getElementById('videoUrl2').value.trim() || null,
        currency:           document.getElementById('currency').value,
        salaryMin:          parseSalary(document.getElementById('salaryMin').value),
        salaryMax:          parseSalary(document.getElementById('salaryMax').value),
        showSalary:         !document.getElementById('salaryDisplayText').textContent.includes('Ẩn'),
        deadline:           document.getElementById('deadline').value,
        urgentRecruitment:  document.querySelector('input[name="urgentRecruitment"]:checked')?.value === 'true',
        workFromHome:       document.getElementById('workFromHome').checked,
        workAtOffice:       document.getElementById('workAtOffice').checked,
        gender:             document.querySelector('input[name="gender"]:checked')?.value || 'Nam/Nữ',
        ageMin:             parseAge(document.getElementById('ageMin').value),
        ageMax:             parseAge(document.getElementById('ageMax').value),
        experience:         document.getElementById('experience').value || null,
        educationLevel:     document.getElementById('educationLevel').value || null,
        degreeLevel:        document.getElementById('degreeLevel').value || null,
        additionalInfo:     document.getElementById('additionalInfo').value.trim() || null,
        employmentType:     employmentTypes,
        benefits:           benefits,
        resumeLanguage:     resumeLanguage,
    };
}


// ===== HELPERS =====
function parseSalary(value) {
    const num = parseFloat(value);
    return isNaN(num) ? null : num;
}

function parseAge(value) {
    const num = parseInt(value, 10);
    return isNaN(num) ? null : num;
}


// ===== VALIDATION =====
function validateForm() {
    const fields = [
        { id: 'title',        msg: 'Vui lòng nhập chức danh tuyển dụng' },
        { id: 'location',     msg: 'Vui lòng nhập nơi làm việc' },
        { id: 'deadline',     msg: 'Vui lòng chọn hạn nộp hồ sơ' },
        { id: 'description',  msg: 'Mô tả công việc phải có ít nhất 50 ký tự', minLen: 50 },
        { id: 'requirements', msg: 'Kỹ năng yêu cầu phải có ít nhất 50 ký tự',  minLen: 50 },
    ];

    for (const { id, msg, minLen } of fields) {
        const el = document.getElementById(id);
        const value = el?.value?.trim() || '';
        if (!value || (minLen && value.length < minLen)) {
            showToast(msg, 'error');
            el?.focus();
            return false;
        }
    }

    const deadline = new Date(document.getElementById('deadline').value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (deadline < today) {
        showToast('Hạn nộp hồ sơ phải là ngày trong tương lai', 'error');
        document.getElementById('deadline')?.focus();
        return false;
    }

    return true;
}


// ===== SALARY TOGGLE =====
document.addEventListener('DOMContentLoaded', () => {
    const logoUploadBtn = document.getElementById('companyLogoUploadBtn');
    const logoInput = document.getElementById('companyLogoInput');

    if (logoUploadBtn && logoInput) {
        logoUploadBtn.addEventListener('click', () => logoInput.click());
        logoInput.addEventListener('change', async () => {
            const file = logoInput.files?.[0];
            if (!file) return;

            logoUploadBtn.disabled = true;
            const originalText = logoUploadBtn.textContent;
            logoUploadBtn.textContent = 'Đang tải...';

            try {
                await uploadCompanyLogo(file);
            } catch (error) {
                showToast(error.message || 'Không thể tải logo công ty lên', 'error');
            } finally {
                logoUploadBtn.disabled = false;
                logoUploadBtn.textContent = originalText;
                logoInput.value = '';
            }
        });
    }

    // Salary display toggle
    const salaryBtn = document.querySelector('.btn-salary-display');
    if (salaryBtn) {
        salaryBtn.addEventListener('click', () => {
            const text = document.getElementById('salaryDisplayText');
            const isShowing = text.textContent === 'Hiện thị';
            text.textContent = isShowing ? 'Ẩn' : 'Hiện thị';
            salaryBtn.style.background = isShowing ? '#dc3545' : '#2c3e8f';
        });
    }

    // Set min deadline = today
    const deadlineInput = document.getElementById('deadline');
    if (deadlineInput) {
        deadlineInput.min = new Date().toISOString().split('T')[0];
    }

    // Load employer info & form options concurrently
    loadEmployerInfo();
    loadFormOptions();

    // Editor toolbar (simple)
    document.querySelectorAll('.btn-editor').forEach(btn => {
        btn.addEventListener('click', () => {
            // Find textarea sibling
            const toolbar = btn.closest('.editor-toolbar');
            const textarea = toolbar?.nextElementSibling;
            if (!textarea || textarea.tagName !== 'TEXTAREA') return;

            const start = textarea.selectionStart;
            const end   = textarea.selectionEnd;
            const selected = textarea.value.substring(start, end);
            const action = btn.dataset.editorAction || btn.textContent.trim();

            let wrapped = selected;
            if (action === 'bold' || action === 'B') wrapped = `**${selected}**`;
            else if (action === 'italic' || action === 'I') wrapped = `_${selected}_`;
            else if (action === 'bullet' || action === '• List') wrapped = `• ${selected}`;
            else if (action === 'number' || action === '1. List') wrapped = `1. ${selected}`;

            textarea.value =
                textarea.value.substring(0, start) + wrapped + textarea.value.substring(end);
            textarea.focus();
        });
    });

    // Video preview
    document.querySelectorAll('.btn-preview').forEach(btn => {
        btn.addEventListener('click', () => {
            const url = btn.previousElementSibling?.value?.trim();
            if (!url) { showToast('Vui lòng nhập link video', 'error'); return; }
            if (!url.includes('youtube.com') && !url.includes('youtu.be')) {
                showToast('Chỉ hỗ trợ link YouTube', 'error'); return;
            }
            window.open(url, '_blank');
        });
    });

    // ===== FORM SUBMIT =====
    const form = document.getElementById('jobPostForm');
    if (!form) { console.error('Form #jobPostForm không tìm thấy!'); return; }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        const submitBtn = form.querySelector('.btn-submit');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.classList.add('btn-submit-loading');
        submitBtn.innerHTML = 'Đang đăng tuyển...';

        const formData = collectFormData();

        try {
            const response = await fetch('/api/jobs/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(formData),
            });

            const result = await response.json();

            if (response.ok && result.success) {
                showToast(`✅ Đăng tin tuyển dụng thành công! (Job #${result.jobId})`, 'success');
                setTimeout(() => {
                    window.location.href = '/quan-ly-dang-tuyen.html';
                }, 2000);
            } else if (response.status === 403) {
                showToast('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.', 'error');
                setTimeout(() => { window.location.href = '/employer-login.html'; }, 2000);
            } else {
                const errorMsg = result.error || result.message || 'Đã xảy ra lỗi. Vui lòng thử lại.';
                showToast(errorMsg, 'error');
                submitBtn.disabled = false;
                submitBtn.classList.remove('btn-submit-loading');
                submitBtn.innerHTML = originalHTML;
            }
        } catch (err) {
            console.error('Lỗi khi gửi form:', err);
            showToast('Không thể kết nối đến server. Vui lòng thử lại.', 'error');
            submitBtn.disabled = false;
            submitBtn.classList.remove('btn-submit-loading');
            submitBtn.innerHTML = originalHTML;
        }
    });
});
