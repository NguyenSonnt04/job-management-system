// ===================================================
// JOB SEARCH PAGE - Full API Integration
// ===================================================

let allJobs = [];
let searchDebounceTimer = null;
let currentFilteredJobs = [];
let visibleJobsCount = 5;
const JOBS_PER_PAGE = 5;

// Map filter group code → select element id
const FILTER_CODE_TO_ID = {
    salary:          'filterSalary',
    level:           'filterLevel',
    posted_within:   'filterPosted',
    employment_type: 'filterType',
    experience:      'filterExp',
    job_rank:        'filterRank'
};

async function loadFilterOptions() {
    try {
        const res = await fetch('/api/filters');
        if (!res.ok) return;
        const groups = await res.json();
        groups.forEach(group => {
            const selectId = FILTER_CODE_TO_ID[group.code];
            if (!selectId) return;
            const select = document.getElementById(selectId);
            if (!select) return;
            // Keep "Tất cả" option, append the rest
            group.options.forEach(opt => {
                const option = document.createElement('option');
                option.value = opt.value;
                option.textContent = opt.label;
                select.appendChild(option);
            });
        });
    } catch (e) {
        console.warn('Không thể tải filter options:', e);
    }
}

document.addEventListener('DOMContentLoaded', function () {

    // Read keyword from URL params (e.g. from homepage search)
    const urlParams = new URLSearchParams(window.location.search);
    const initKeyword  = urlParams.get('keyword')  || '';
    const initLocation = urlParams.get('location') || '';

    const keywordInput  = document.getElementById('searchKeyword');
    const locationInput = document.getElementById('searchLocation');

    if (keywordInput  && initKeyword)  keywordInput.value  = initKeyword;
    if (locationInput && initLocation) locationInput.value = initLocation;

    // Load filter options from DB, then load jobs
    loadFilterOptions().then(() => loadJobs(initKeyword, initLocation));

    // ── Search button + Enter ──────────────────────────
    const searchBtn = document.querySelector('.btn-search-main');
    if (searchBtn) searchBtn.addEventListener('click', triggerSearch);

    [keywordInput, locationInput].forEach(inp => {
        if (inp) inp.addEventListener('keypress', e => { if (e.key === 'Enter') triggerSearch(); });
    });

    // ── Filters ──────────────────────────────────────
    ['filterSalary', 'filterLevel', 'filterPosted', 'filterType', 'filterExp', 'filterRank'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', () => applyClientFilters());
    });

    // ── Clear filters ─────────────────────────────────
    const clearBtn = document.querySelector('.btn-clear-filters');
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            ['filterSalary', 'filterLevel', 'filterPosted', 'filterType', 'filterExp', 'filterRank'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.selectedIndex = 0;
            });
            if (keywordInput)  keywordInput.value  = '';
            if (locationInput) locationInput.value = '';
            loadJobs('', '');
        });
    }

    // ── Sort ──────────────────────────────────────────
    const sortSelect = document.querySelector('.sort-dropdown select');
    if (sortSelect) sortSelect.addEventListener('change', () => applyClientFilters());
});

// ─────────────────────────────────────────────────────
// TRIGGER SEARCH
// ─────────────────────────────────────────────────────
function triggerSearch() {
    const keyword  = (document.getElementById('searchKeyword')?.value  || '').trim();
    const location = (document.getElementById('searchLocation')?.value || '').trim();
    loadJobs(keyword, location);
}

// ─────────────────────────────────────────────────────
// LOAD JOBS FROM API
// ─────────────────────────────────────────────────────
async function loadJobs(keyword, location) {
    const listEl = document.getElementById('jobList');
    if (!listEl) return;

    listEl.innerHTML = `<div style="text-align:center;padding:60px 20px;color:#999;font-size:15px;">
        <div style="font-size:32px;margin-bottom:12px;">⏳</div>
        Đang tìm kiếm việc làm phù hợp...
    </div>`;

    try {
        let url = '/api/jobs/active';
        if (keyword) {
            url = `/api/jobs/search?keyword=${encodeURIComponent(keyword)}`;
        }

        const resp = await fetch(url);
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

        allJobs = await resp.json();

        // Client-side location filter (API search chỉ có keyword)
        if (location) {
            const loc = location.toLowerCase();
            allJobs = allJobs.filter(j => (j.location || '').toLowerCase().includes(loc));
        }

        applyClientFilters();

    } catch (err) {
        console.error('Error loading jobs:', err);
        listEl.innerHTML = `<div style="text-align:center;padding:60px 20px;color:#dc3545;">
            <div style="font-size:32px;margin-bottom:12px;">❌</div>
            Không thể tải danh sách việc làm. 
            <button onclick="triggerSearch()" style="margin-top:12px;padding:8px 20px;background:#e84141;
            color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:600;">Thử lại</button>
        </div>`;
    }
}

// ─────────────────────────────────────────────────────
// CLIENT-SIDE FILTERS + SORT
// ─────────────────────────────────────────────────────
function applyClientFilters() {
    let filtered = [...allJobs];
    const today  = new Date();

    // Filter: Mức lương
    const salary = document.getElementById('filterSalary')?.value || '';
    if (salary) {
        filtered = filtered.filter(j => {
            const min = j.salaryMin || 0;
            const max = j.salaryMax || 0;
            const avg = (min + max) / 2 || min || max;
            const m   = avg / 1_000_000; // in triệu
            if (salary === 'Dưới 10 triệu')  return m > 0 && m < 10;
            if (salary === '10-15 triệu')    return m >= 10 && m <= 15;
            if (salary === '15-20 triệu')    return m > 15 && m <= 20;
            if (salary === '20-30 triệu')    return m > 20 && m <= 30;
            if (salary === 'Trên 30 triệu')  return m > 30;
            return true;
        });
    }

    // Filter: Hình thức
    const type = document.getElementById('filterType')?.value || '';
    if (type) {
        const typeMap = { 'Toàn thời gian': 'FULL_TIME', 'Bán thời gian': 'PART_TIME', 'Remote': 'REMOTE' };
        filtered = filtered.filter(j => j.employmentType === typeMap[type] || (j.employmentType || '').includes(type));
    }

    // Filter: Kinh nghiệm
    const exp = document.getElementById('filterExp')?.value || '';
    if (exp) {
        filtered = filtered.filter(j => {
            const e = (j.experience || '').toLowerCase();
            if (exp === 'Chưa có kinh nghiệm') return e.includes('chưa') || e.includes('không') || e === '';
            if (exp === 'Dưới 1 năm')           return e.includes('dưới 1') || e.includes('< 1');
            if (exp === '1-2 năm')               return e.includes('1') || e.includes('2');
            if (exp === '3-5 năm')               return e.includes('3') || e.includes('4') || e.includes('5');
            if (exp === 'Trên 5 năm')            return e.includes('5+') || e.includes('trên 5');
            return true;
        });
    }

    // Filter: Đăng trong vòng
    const posted = document.getElementById('filterPosted')?.value || '';
    if (posted) {
        const days = posted === '24 giờ qua' ? 1 : posted === '7 ngày qua' ? 7 : 30;
        const cutoff = new Date(today - days * 86400000);
        filtered = filtered.filter(j => j.createdAt && new Date(j.createdAt) >= cutoff);
    }

    // Sort
    const sortVal = document.querySelector('.sort-dropdown select')?.value || 'Ngày cập nhật';
    if (sortVal === 'Ngày cập nhật') {
        filtered.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    } else if (sortVal === 'Mức lương') {
        filtered.sort((a, b) => (b.salaryMax || b.salaryMin || 0) - (a.salaryMax || a.salaryMin || 0));
    }

    renderJobs(filtered, true);
}

// ─────────────────────────────────────────────────────
// RENDER JOB CARDS - CareerViet style
// ─────────────────────────────────────────────────────
function renderJobs(jobs, resetVisibleCount = false) {
    const listEl  = document.getElementById('jobList');
    const titleEl = document.getElementById('resultsTitle');
    const descriptionMeta = document.querySelector('meta[name="description"]');
    const kw = (document.getElementById('searchKeyword')?.value || '').trim();
    const formattedCount = new Intl.NumberFormat('vi-VN').format(jobs?.length || 0);

    currentFilteredJobs = Array.isArray(jobs) ? [...jobs] : [];
    if (resetVisibleCount) {
        visibleJobsCount = JOBS_PER_PAGE;
    }

    if (titleEl) {
        titleEl.textContent = kw
            ? `${jobs.length} việc làm cho "${kw}"`
            : `${jobs.length} việc làm đang tuyển dụng`;
    }

    document.title = kw
        ? `${formattedCount} việc làm cho "${kw}" | CareerViet`
        : `${formattedCount} việc làm theo ngày cập nhật mới nhất | CareerViet`;

    if (descriptionMeta) {
        descriptionMeta.setAttribute(
            'content',
            kw
                ? `Tìm thấy ${formattedCount} việc làm phù hợp với từ khóa "${kw}" trên CareerViet.`
                : `Tìm kiếm trong ${formattedCount} việc làm đang tuyển dụng. Cập nhật hàng ngày với mức lương hấp dẫn.`
        );
    }

    if (!jobs || jobs.length === 0) {
        listEl.innerHTML = `
            <div class="jc-empty">
                <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                    <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                </svg>
                <p style="font-size:16px;font-weight:600;color:#555;margin:12px 0 6px;">Không tìm thấy việc làm phù hợp</p>
                <p style="font-size:14px;color:#999;">Hãy thử thay đổi từ khóa hoặc xóa bộ lọc</p>
                <button onclick="document.querySelector('.btn-clear-filters')?.click()"
                    style="margin-top:16px;padding:10px 28px;background:#e84141;color:#fff;
                    border:none;border-radius:8px;cursor:pointer;font-weight:700;font-size:14px;">
                    Xóa bộ lọc
                </button>
            </div>`;
        return;
    }

    const visibleJobs = currentFilteredJobs.slice(0, visibleJobsCount);

    listEl.innerHTML = visibleJobs.map((job, idx) => {
        const cleanTitle = normalizeJobTitle(job.title);
        const salary   = formatSalaryCard(job.salaryMin, job.salaryMax, job.currency);
        const company  = job.employer?.companyName || 'Công ty đang ẩn tên';
        const logoUrl  = normalizeLogoUrl(job.employer?.logoUrl);
        const isUrgent = job.urgentRecruitment;
        const isNew    = job.createdAt && (Date.now() - new Date(job.createdAt)) < 3 * 86400000;

        // Format deadline & createdAt
        const deadlineStr = job.deadline ? formatDate(job.deadline) : null;
        const updatedStr  = job.createdAt ? formatDate(job.createdAt.split('T')[0]) : null;

        // Benefits from description keywords (fallback)
        const benefits = parseBenefits(job.benefits, job.urgentRecruitment);

        return `
        <div class="job-card-search jcs-card" data-id="${job.id}"
             style="animation: jcFadeUp .3s ease ${Math.min(idx * 0.06, 0.5)}s both;">
            <div class="job-card-header-search">

                <!-- Logo -->
                <div class="jcs-logo">
                    ${logoUrl
                        ? `<img src="${escapeAttr(logoUrl)}" alt="${escapeAttr(company)}" class="jcs-logo-image" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline-flex';">
                           <span class="jcs-logo-fallback" style="display:none;"><i class="fa-regular fa-building"></i></span>`
                        : `<span class="jcs-logo-fallback"><i class="fa-regular fa-building"></i></span>`}
                </div>

                <!-- Content -->
                <div class="job-card-content">
                    <h3 class="jcs-title">
                        <a href="job-detail.html?id=${job.id}" class="jcs-title-link">${escapeHtml(cleanTitle)}</a>
                        ${isUrgent ? '<span class="badge-hot">URGENT</span>' : isNew ? '<span class="badge-new">MỚI</span>' : ''}
                    </h3>
                    <p class="company-name-search">${company}</p>

                    <div class="jcs-meta">
                        <span class="jcs-salary">
                            <i class="fa-solid fa-wallet"></i> ${salary}
                        </span>
                        <span class="jcs-location">
                            <i class="fa-solid fa-location-dot"></i> ${job.location || 'Chưa xác định'}
                        </span>
                        ${job.employmentType ? `<span class="jcs-type">
                            <i class="fa-regular fa-clock"></i> ${formatType(job.employmentType)}
                        </span>` : ''}
                    </div>

                    <div class="job-footer">
                        <div class="deadline">
                            ${deadlineStr ? `<span class="jcs-info-pill"><i class="fa-regular fa-clock"></i> Hạn nộp: ${deadlineStr}</span>` : ''}
                            ${updatedStr ? `<span class="jcs-info-pill"><i class="fa-regular fa-calendar-days"></i> Cập nhật: ${updatedStr}</span>` : ''}
                        </div>
                        <div class="job-bottom-row">
                            ${benefits.length > 0 ? `<div class="job-badges">
                                ${benefits.slice(0,4).map(b => `<span class="badge-icon">
                                    <i class="${getBenefitIcon(b)}"></i> ${b}
                                </span>`).join('')}
                            </div>` : '<div class="job-badges"></div>'}
                            <div class="job-actions">
                                <button class="btn-favorite jcs-fav" onclick="toggleFav(this,${job.id})" title="Lưu">
                                    <i class="fa-regular fa-heart" style="font-size:16px;color:#999;"></i>
                                </button>
                                <a href="job-detail.html?id=${job.id}" class="btn-apply">Xem chi tiết</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');

    // Inject styles once
    if (!document.getElementById('jcsStyles')) {
        const s = document.createElement('style');
        s.id = 'jcsStyles';
        s.textContent = `
            @keyframes jcFadeUp { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }

            .jcs-card {
                border: 1px solid #e7edf5;
                border-radius: 16px;
                box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
                transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
            }
            .jcs-card:hover {
                transform: translateY(-1px);
                border-color: #d8e2f0;
                box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
            }

            .jcs-logo {
                width: 76px; height: 76px; border: 1px solid #edf2f7; border-radius: 12px;
                display: flex; align-items: center; justify-content: center;
                background: #fff; flex-shrink: 0;
                overflow: hidden;
            }

            .jcs-logo-image {
                width: 100%;
                height: 100%;
                object-fit: contain;
                display: block;
                background: #fff;
            }

            .jcs-logo-fallback {
                width: 100%;
                height: 100%;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                color: #ced4da;
                font-size: 28px;
            }

            .jcs-title {
                font-size: 17px;
                font-weight: 700;
                margin-bottom: 4px;
                display: flex;
                align-items: center;
                gap: 8px;
                flex-wrap: wrap;
                line-height: 1.25;
            }
            .jcs-title-link {
                color: #1a2456;
                text-decoration: none;
                line-height: 1.35;
            }
            .jcs-title-link:hover {
                color: #22326f;
            }

            .company-name-search {
                font-size: 13px;
                color: #5b667a;
                margin-bottom: 10px;
                font-weight: 500;
            }

            .badge-new,
            .badge-hot {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                min-height: 22px;
                padding: 0 9px;
                border-radius: 999px;
                font-size: 10px;
                font-weight: 700;
                letter-spacing: 0.04em;
                flex-shrink: 0;
            }

            .badge-new {
                background: #eef2ff;
                color: #2e3b8e;
                border: 1px solid #c7d2fe;
            }

            .badge-hot {
                background: #fff1f2;
                color: #d03a52;
                border: 1px solid #fecdd3;
            }

            .jcs-meta {
                display: flex;
                flex-wrap: wrap;
                gap: 8px 14px;
                margin-bottom: 10px;
            }
            .jcs-salary,
            .jcs-location,
            .jcs-type {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                font-size: 13px;
            }
            .jcs-salary {
                color: #1f9d8f;
                font-weight: 700;
            }
            .jcs-location,
            .jcs-type {
                color: #5f6b7c;
                font-weight: 500;
            }

            .deadline {
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
            }

            .jcs-info-pill {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                min-height: 28px;
                padding: 0 9px;
                border-radius: 999px;
                background: #f7f9fc;
                border: 1px solid #e8eef5;
                color: #6b7280;
                font-size: 11px;
                font-weight: 600;
            }

            .badge-icon {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                font-size: 11px;
                color: #4d5768;
                background: #f8fafc;
                padding: 2px 6px;
                border-radius: 999px;
                border: 1px solid #e6ebf2;
                font-weight: 600;
                line-height: 1.2;
            }

            .job-bottom-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 12px;
                width: 100%;
            }

            .job-badges {
                display: flex;
                flex-wrap: wrap;
                gap: 8px;
                min-width: 0;
                flex: 1;
            }

            .job-actions {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 8px;
                flex-shrink: 0;
            }

            .jcs-fav {
                border: 1px solid #d9e2ec;
                border-radius: 8px;
                background: #fff;
                width: 34px;
                height: 34px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: all .18s ease;
            }
            .jcs-fav:hover {
                border-color: #b7c4d6;
                background: #f8fafc;
            }
            .jcs-fav.saved svg { fill: #e84141; stroke: #e84141; }

            .jc-empty { text-align: center; padding: 80px 20px; }

            .job-load-more-wrap {
                display: flex;
                justify-content: center;
                padding-top: 10px;
            }

            .job-load-more-btn {
                min-width: 180px;
                min-height: 42px;
                padding: 0 18px;
                border: 1px solid #d7e1ee;
                border-radius: 999px;
                background: #fff;
                color: #1a2456;
                font-size: 13px;
                font-weight: 700;
                cursor: pointer;
                transition: all .18s ease;
                box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
            }

            .job-load-more-btn:hover {
                border-color: #bcc9db;
                background: #f8fafc;
                transform: translateY(-1px);
            }

            .btn-apply {
                display: inline-flex !important;
                align-items: center;
                justify-content: center;
                min-height: 36px;
                padding: 0 14px;
                border-radius: 8px;
                background: #1a2456;
                color: #fff !important;
                text-decoration: none !important;
                text-align: center;
                font-size: 12px;
                font-weight: 700;
                letter-spacing: 0.01em;
                box-shadow: none;
            }
            .btn-apply:hover {
                background: #22326f;
                transform: none;
                box-shadow: none;
            }
        `;
        document.head.appendChild(s);
    }

    renderLoadMore();
}

function renderLoadMore() {
    const listEl = document.getElementById('jobList');
    if (!listEl) return;

    const oldControl = document.getElementById('jobLoadMoreWrap');
    if (oldControl) oldControl.remove();

    if (currentFilteredJobs.length <= visibleJobsCount) return;

    const remaining = currentFilteredJobs.length - visibleJobsCount;
    const wrap = document.createElement('div');
    wrap.id = 'jobLoadMoreWrap';
    wrap.className = 'job-load-more-wrap';
    wrap.innerHTML = `
        <button type="button" class="job-load-more-btn" id="jobLoadMoreBtn">
            Xem thêm ${Math.min(JOBS_PER_PAGE, remaining)} việc làm
        </button>
    `;

    listEl.appendChild(wrap);

    const btn = document.getElementById('jobLoadMoreBtn');
    if (btn) {
        btn.addEventListener('click', function () {
            visibleJobsCount += JOBS_PER_PAGE;
            renderJobs(currentFilteredJobs, false);
        });
    }
}

// ─────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────
function formatSalaryCard(min, max, currency) {
    if (!min && !max) return 'Thỏa thuận';
    const fmt = n => (n / 1_000_000).toFixed(0) + ' Tr';
    const cur = currency === 'USD' ? 'USD' : 'VNĐ';
    if (min && max) return `${fmt(min)} - ${fmt(max)} ${cur}`;
    if (min) return `Từ ${fmt(min)} ${cur}`;
    return `Đến ${fmt(max)} ${cur}`;
}

function formatDate(str) {
    if (!str) return '';
    const [y, m, d] = str.split('-');
    return `${d}/${m}/${y}`;
}

function formatType(type) {
    return { FULL_TIME: 'Toàn thời gian', PART_TIME: 'Bán thời gian', REMOTE: 'Remote', CONTRACT: 'Hợp đồng' }[type] || type;
}

function toggleFav(btn, jobId) {
    btn.classList.toggle('saved');
    const favs = JSON.parse(localStorage.getItem('favJobs') || '[]');
    const i = favs.indexOf(jobId);
    if (btn.classList.contains('saved')) { if (i < 0) favs.push(jobId); }
    else { if (i > -1) favs.splice(i, 1); }
    localStorage.setItem('favJobs', JSON.stringify(favs));
}

// Map benefit keyword → Font Awesome icon class
function getBenefitIcon(benefit) {
    const b = benefit.toLowerCase();
    if (b.includes('bảo hiểm') || b.includes('bao hiem') || b.includes('insurance')) return 'fa-solid fa-shield-halved';
    if (b.includes('du lịch') || b.includes('du lich') || b.includes('travel'))       return 'fa-solid fa-plane';
    if (b.includes('laptop') || b.includes('máy tính') || b.includes('computer'))     return 'fa-solid fa-laptop';
    if (b.includes('thưởng') || b.includes('bonus') || b.includes('kpi'))              return 'fa-solid fa-dollar-sign';
    if (b.includes('xe') || b.includes('ô tô') || b.includes('car'))                   return 'fa-solid fa-car';
    if (b.includes('ăn') || b.includes('bữa') || b.includes('meal'))                   return 'fa-solid fa-utensils';
    if (b.includes('nhà ở') || b.includes('housing'))                                  return 'fa-solid fa-house';
    if (b.includes('y tế') || b.includes('sức khỏe') || b.includes('health'))          return 'fa-solid fa-heart-pulse';
    if (b.includes('đào tạo') || b.includes('training') || b.includes('học'))          return 'fa-solid fa-graduation-cap';
    if (b.includes('phụ cấp') || b.includes('phu cap') || b.includes('allowance'))     return 'fa-solid fa-hand-holding-dollar';
    if (b.includes('remote') || b.includes('từ xa'))                                   return 'fa-solid fa-house-laptop';
    if (b.includes('thể thao') || b.includes('gym') || b.includes('sport'))            return 'fa-solid fa-dumbbell';
    return 'fa-solid fa-circle-check'; // default
}

function normalizeLogoUrl(value) {
    const url = (value || '').trim();
    return url || '';
}

function parseBenefits(rawBenefits, urgentRecruitment) {
    if (!rawBenefits) {
        return urgentRecruitment ? ['Thưởng KPI', 'Bảo hiểm'] : [];
    }

    const normalized = String(rawBenefits).trim();
    if (!normalized) {
        return urgentRecruitment ? ['Thưởng KPI', 'Bảo hiểm'] : [];
    }

    try {
        const parsed = JSON.parse(normalized);
        if (Array.isArray(parsed)) {
            return parsed.map(item => String(item).trim()).filter(Boolean);
        }
    } catch (_) {
        // Fall back to comma-separated parsing.
    }

    return normalized
        .split(',')
        .map(item => item.replace(/^[\[\]"]+|[\[\]"]+$/g, '').trim())
        .filter(Boolean);
}

function normalizeJobTitle(value) {
    return String(value ?? '')
        .replace(/\(\s*mới\s*\)/gi, '')
        .replace(/\[\s*mới\s*\]/gi, '')
        .replace(/^\s*mới\s*[:-]\s*/gi, '')
        .replace(/\s{2,}/g, ' ')
        .trim();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeAttr(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}
