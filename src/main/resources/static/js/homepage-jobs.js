const HOMEPAGE_JOBS_PER_PAGE = 8;
const HOMEPAGE_JOB_TABS = ['featured', 'vip', 'headhunter'];

const homepageJobsState = {
    jobsByTab: {
        featured: [],
        vip: [],
        headhunter: []
    },
    pageByTab: {
        featured: 1,
        vip: 1,
        headhunter: 1
    },
    activeTab: 'featured'
};

document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('.job-listings .tab');
    if (!tabs.length) return;

    bindHomepageJobTabs(tabs);
    loadHomepageJobs();
});

function bindHomepageJobTabs(tabs) {
    tabs.forEach((tab) => {
        tab.addEventListener('click', function () {
            const nextTab = tab.dataset.tab;
            if (!nextTab || nextTab === homepageJobsState.activeTab) return;
            homepageJobsState.activeTab = nextTab;
            updateHomepageJobTabUi();
            renderHomepageJobTab(nextTab);
        });
    });
}

async function loadHomepageJobs() {
    showHomepageJobLoading();

    try {
        const response = await fetch('/api/jobs/active');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const jobs = await response.json();
        const collections = buildHomepageJobCollections(Array.isArray(jobs) ? jobs : []);

        homepageJobsState.jobsByTab = collections;
        updateHomepageJobTabUi();
        HOMEPAGE_JOB_TABS.forEach((tabKey) => renderHomepageJobTab(tabKey));
    } catch (error) {
        console.error('Failed to load homepage jobs:', error);
        HOMEPAGE_JOB_TABS.forEach((tabKey) => {
            const grid = getHomepageJobGrid(tabKey);
            const pagination = getHomepageJobPagination(tabKey);
            if (grid) {
                grid.innerHTML = '<div class="job-card-empty">Không thể tải danh sách việc làm lúc này.</div>';
            }
            if (pagination) {
                pagination.innerHTML = '';
            }
        });
    }
}

function buildHomepageJobCollections(rawJobs) {
    const baseJobs = [...rawJobs]
        .filter((job) => Boolean(job?.id))
        .sort((left, right) => {
            const urgentDelta = Number(Boolean(right?.urgentRecruitment)) - Number(Boolean(left?.urgentRecruitment));
            if (urgentDelta !== 0) return urgentDelta;
            return getJobTimestamp(right) - getJobTimestamp(left);
        });

    return {
        featured: baseJobs,
        vip: buildVipJobs(baseJobs),
        headhunter: buildHeadhunterJobs(baseJobs)
    };
}

function buildVipJobs(jobs) {
    const seeded = [];
    const seenIds = new Set();

    jobs
        .filter((job) => isVipJob(job))
        .sort((left, right) => getSalaryUpper(right) - getSalaryUpper(left) || getJobTimestamp(right) - getJobTimestamp(left))
        .forEach((job) => pushUniqueJob(seeded, seenIds, job));

    jobs
        .slice()
        .sort((left, right) => getSalaryUpper(right) - getSalaryUpper(left) || getJobTimestamp(right) - getJobTimestamp(left))
        .forEach((job) => {
            if (seeded.length >= 24) return;
            pushUniqueJob(seeded, seenIds, job);
        });

    return seeded;
}

function buildHeadhunterJobs(jobs) {
    const seeded = [];
    const seenIds = new Set();

    jobs
        .filter((job) => isHeadhunterJob(job))
        .sort((left, right) => Number(Boolean(right?.urgentRecruitment)) - Number(Boolean(left?.urgentRecruitment))
            || getSalaryUpper(right) - getSalaryUpper(left)
            || getJobTimestamp(right) - getJobTimestamp(left))
        .forEach((job) => pushUniqueJob(seeded, seenIds, job));

    jobs
        .filter((job) => job?.urgentRecruitment || getSalaryUpper(job) >= 18000000)
        .sort((left, right) => Number(Boolean(right?.urgentRecruitment)) - Number(Boolean(left?.urgentRecruitment))
            || getSalaryUpper(right) - getSalaryUpper(left)
            || getJobTimestamp(right) - getJobTimestamp(left))
        .forEach((job) => {
            if (seeded.length >= 24) return;
            pushUniqueJob(seeded, seenIds, job);
        });

    return seeded;
}

function pushUniqueJob(target, seenIds, job) {
    if (!job?.id || seenIds.has(job.id)) return;
    target.push(job);
    seenIds.add(job.id);
}

function showHomepageJobLoading() {
    const labels = {
        featured: 'Đang tải việc làm nổi bật...',
        vip: 'Đang tải việc làm VIP...',
        headhunter: 'Đang tải việc làm từ Top Headhunter...'
    };

    HOMEPAGE_JOB_TABS.forEach((tabKey) => {
        const grid = getHomepageJobGrid(tabKey);
        const pagination = getHomepageJobPagination(tabKey);
        if (grid) {
            grid.innerHTML = `<div class="job-card-empty">${labels[tabKey]}</div>`;
        }
        if (pagination) {
            pagination.innerHTML = '';
        }
    });
}

function updateHomepageJobTabUi() {
    document.querySelectorAll('.job-listings .tab').forEach((tabButton) => {
        tabButton.classList.toggle('active', tabButton.dataset.tab === homepageJobsState.activeTab);
    });

    document.querySelectorAll('.job-listings .tab-content').forEach((tabPanel) => {
        tabPanel.classList.toggle('active', tabPanel.id === homepageJobsState.activeTab);
    });
}

function renderHomepageJobTab(tabKey) {
    const jobs = homepageJobsState.jobsByTab[tabKey] || [];
    const grid = getHomepageJobGrid(tabKey);
    const pagination = getHomepageJobPagination(tabKey);
    if (!grid || !pagination) return;

    if (!jobs.length) {
        grid.innerHTML = '<div class="job-card-empty">Chưa có việc làm phù hợp trong mục này.</div>';
        pagination.innerHTML = '';
        return;
    }

    const totalPages = Math.max(1, Math.ceil(jobs.length / HOMEPAGE_JOBS_PER_PAGE));
    const currentPage = Math.min(homepageJobsState.pageByTab[tabKey] || 1, totalPages);
    const pageStart = (currentPage - 1) * HOMEPAGE_JOBS_PER_PAGE;
    const pageJobs = jobs.slice(pageStart, pageStart + HOMEPAGE_JOBS_PER_PAGE);

    homepageJobsState.pageByTab[tabKey] = currentPage;
    grid.innerHTML = pageJobs.map((job) => renderHomepageJobCard(job, tabKey)).join('');
    renderHomepageJobPagination(tabKey, totalPages, currentPage);
}

function renderHomepageJobPagination(tabKey, totalPages, currentPage) {
    const pagination = getHomepageJobPagination(tabKey);
    if (!pagination) return;

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    const items = buildHomepagePaginationItems(totalPages, currentPage);
    const buttons = items.map((item) => {
        if (item === 'ellipsis') {
            return '<span class="job-page-ellipsis">...</span>';
        }

        if (item === 'next') {
            return `
                <button
                    type="button"
                    class="job-page-btn job-page-btn-next"
                    data-page="${Math.min(totalPages, currentPage + 1)}"
                    data-tab="${tabKey}"
                    aria-label="Trang tiếp theo"
                >&gt;</button>
            `;
        }

        return `
            <button
                type="button"
                class="job-page-btn${item === currentPage ? ' active' : ''}"
                data-page="${item}"
                data-tab="${tabKey}"
                ${item === currentPage ? 'aria-current="page"' : ''}
            >${item}</button>
        `;
    });

    pagination.innerHTML = buttons.join('');
    pagination.querySelectorAll('.job-page-btn').forEach((button) => {
        button.addEventListener('click', function () {
            const nextPage = Number(button.dataset.page || 1);
            const nextTab = button.dataset.tab;
            if (!nextTab) return;
            homepageJobsState.pageByTab[nextTab] = nextPage;
            renderHomepageJobTab(nextTab);
        });
    });
}

function buildHomepagePaginationItems(totalPages, currentPage) {
    if (totalPages <= 6) {
        return [...Array(totalPages)].map((_, index) => index + 1);
    }

    const items = [];
    const leadingPages = [1, 2, 3, 4, 5].filter((page) => page <= totalPages);
    const trailingStart = Math.max(totalPages - 2, 6);

    if (currentPage <= 5) {
        items.push(...leadingPages);
        if (totalPages > 6) items.push('ellipsis');
        items.push(totalPages);
        items.push('next');
        return items;
    }

    items.push(1);

    if (currentPage > 4) {
        items.push('ellipsis');
    }

    const middlePages = [currentPage - 1, currentPage, currentPage + 1]
        .filter((page, index, array) => page > 1 && page < totalPages && array.indexOf(page) === index);
    items.push(...middlePages);

    if (currentPage < totalPages - 3) {
        items.push('ellipsis');
    }

    for (let page = trailingStart; page <= totalPages; page += 1) {
        if (!items.includes(page)) {
            items.push(page);
        }
    }

    if (currentPage < totalPages) {
        items.push('next');
    }

    return items;
}

function renderHomepageJobCard(job, tabKey) {
    const detailUrl = `job-detail.html?id=${job.id}`;
    const title = escapeHtml(normalizeHomepageJobTitle(job.title));
    const companyName = escapeHtml(job.employer?.companyName || 'Công ty đang cập nhật');
    const salaryLabel = escapeHtml(formatHomepageSalary(job));
    const locationLabel = escapeHtml(job.hideLocation ? 'Địa điểm bảo mật' : (job.location || 'Đang cập nhật'));
    const logoUrl = normalizeHomepageLogoUrl(job.employer?.logoUrl);
    const topBadge = tabKey === 'vip' || getSalaryUpper(job) >= 25000000;
    const urgentBadge = Boolean(job.urgentRecruitment);

    return `
        <a class="job-card homepage-job-card" href="${detailUrl}">
            <div class="job-card-header">
                ${logoUrl
                    ? `<img src="${escapeHtml(logoUrl)}" alt="${companyName}" class="company-logo" loading="lazy" onerror="this.replaceWith(createHomepageLogoFallback('${escapeJs(companyName)}'));">`
                    : createHomepageLogoFallback(companyName, true)}
                <div class="job-info">
                    <h3 class="job-title">${title}</h3>
                    <p class="company-name">${companyName}</p>
                    <p class="salary">$ Lương : ${salaryLabel}</p>
                    <p class="location">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
                            <path d="M7 7.58333C8.10457 7.58333 9 6.68791 9 5.58333C9 4.47876 8.10457 3.58333 7 3.58333C5.89543 3.58333 5 4.47876 5 5.58333C5 6.68791 5.89543 7.58333 7 7.58333Z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M7 12.25C9.33333 10.5 11.6667 8.47917 11.6667 5.83333C11.6667 3.16667 9.66667 1.16667 7 1.16667C4.33333 1.16667 2.33333 3.16667 2.33333 5.83333C2.33333 8.47917 4.66667 10.5 7 12.25Z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        ${locationLabel}
                    </p>
                </div>
                ${urgentBadge ? '<span class="badge-urgent">URGENT</span>' : ''}
                ${topBadge ? '<span class="badge-top">TOP</span>' : ''}
            </div>
        </a>
    `;
}

function getHomepageJobGrid(tabKey) {
    return document.getElementById(`homepage${capitalizeHomepageTab(tabKey)}Grid`);
}

function getHomepageJobPagination(tabKey) {
    return document.getElementById(`homepage${capitalizeHomepageTab(tabKey)}Pagination`);
}

function capitalizeHomepageTab(tabKey) {
    return tabKey.charAt(0).toUpperCase() + tabKey.slice(1);
}

function getJobTimestamp(job) {
    return job?.createdAt ? new Date(job.createdAt).getTime() : 0;
}

function getSalaryUpper(job) {
    return Number(job?.salaryMax ?? job?.salaryMin ?? 0);
}

function isVipJob(job) {
    return (job?.currency || '').toUpperCase() === 'USD'
        || getSalaryUpper(job) >= 25000000
        || Number(job?.salaryMin || 0) >= 18000000;
}

function isHeadhunterJob(job) {
    const title = String(job?.title || '');
    const experience = String(job?.experience || '');
    return Boolean(job?.urgentRecruitment)
        || /(senior|lead|manager|trưởng|giám đốc|director|head|executive)/i.test(title)
        || /(3|4|5|\+|trên)/i.test(experience);
}

function formatHomepageSalary(job) {
    if (job?.showSalary === false) {
        return 'Cạnh tranh';
    }

    const min = Number(job?.salaryMin || 0);
    const max = Number(job?.salaryMax || 0);
    const currency = (job?.currency || 'VND').toUpperCase();

    if (!min && !max) {
        return 'Cạnh tranh';
    }

    const unit = currency === 'USD' ? 'USD' : 'VNĐ';
    const formatValue = (value) => `${Math.round(value / 1000000)} Tr`;

    if (min && max) {
        return `${formatValue(min)} - ${formatValue(max)} ${unit}`;
    }
    if (min) {
        return `Từ ${formatValue(min)} ${unit}`;
    }
    return `Đến ${formatValue(max)} ${unit}`;
}

function normalizeHomepageLogoUrl(value) {
    return String(value || '').trim();
}

function normalizeHomepageJobTitle(value) {
    return String(value || '')
        .replace(/\(\s*mới\s*\)/gi, '')
        .replace(/\[\s*mới\s*\]/gi, '')
        .replace(/^\s*mới\s*[:-]\s*/gi, '')
        .replace(/\s{2,}/g, ' ')
        .trim();
}

function createHomepageLogoFallback(label, asMarkup = false) {
    const initials = String(label || 'CV')
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part.charAt(0).toUpperCase())
        .join('') || 'CV';

    const markup = `<div class="company-logo company-logo-fallback">${escapeHtml(initials)}</div>`;
    return asMarkup ? markup : (() => {
        const wrapper = document.createElement('div');
        wrapper.innerHTML = markup;
        return wrapper.firstElementChild;
    })();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeJs(value) {
    return String(value ?? '')
        .replace(/\\/g, '\\\\')
        .replace(/'/g, "\\'");
}
