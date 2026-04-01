(function () {
    const listPage = document.getElementById('careermapListPage');

    const state = {
        paths: [],
        featuredPaths: [],
        industries: [],
        activeIndustry: 'all',
        query: ''
    };

    // ===== SCROLL-TRIGGERED ANIMATION =====
    function initScrollAnimation() {
        const stages = document.querySelectorAll('.path-stage-item');

        if (stages.length === 0) return;

        // Use Intersection Observer for better performance
        const observerOptions = {
            root: null,
            rootMargin: '-20% 0px -20% 0px',
            threshold: [0.1, 0.3, 0.5, 0.7, 0.9]
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const stage = entry.target;
                    const stageNumber = parseInt(stage.dataset.stage);

                    // Activate stage when it's significantly visible
                    if (entry.intersectionRatio > 0.3) {
                        activateStage(stageNumber);
                    }
                }
            });
        }, observerOptions);

        stages.forEach(stage => {
            observer.observe(stage);
        });

        // Also observe scroll position for smoother updates - Optimized
        let ticking = false;
        let lastScrollY = 0;

        window.addEventListener('scroll', () => {
            const currentScrollY = window.scrollY;

            // Only update if scroll position changed significantly
            if (!ticking && Math.abs(currentScrollY - lastScrollY) > 10) {
                ticking = true;
                window.requestAnimationFrame(() => {
                    updateTimelineProgress();
                    lastScrollY = currentScrollY;
                    ticking = false;
                });
            }
        }, { passive: true });

        // Don't auto-activate - wait for scroll
    }

    function activateStage(stageNumber) {
        const stages = document.querySelectorAll('.path-stage-item');

        if (!stages.length) return;

        stages.forEach(stage => {
            const currentStage = parseInt(stage.dataset.stage);

            stage.classList.remove('active', 'prev-stage');

            if (currentStage === stageNumber) {
                stage.classList.add('active');
            } else if (currentStage < stageNumber) {
                stage.classList.add('prev-stage');
            }
        });
    }

    function updateTimelineProgress() {
        const stages = document.querySelectorAll('.path-stage-item');

        if (stages.length === 0) return;

        let maxVisibleStage = 0;
        const viewportCenter = window.innerHeight / 2;

        stages.forEach(stage => {
            const rect = stage.getBoundingClientRect();
            const stageCenter = rect.top + rect.height / 2;
            const distanceFromCenter = Math.abs(stageCenter - viewportCenter);

            // Consider stage "visible" if it's within 60% of viewport center
            if (distanceFromCenter < viewportCenter * 0.7) {
                const stageNumber = parseInt(stage.dataset.stage);
                if (stageNumber > maxVisibleStage) {
                    maxVisibleStage = stageNumber;
                }
            }
        });

        // Only activate if we found a visible stage
        if (maxVisibleStage > 0) {
            activateStage(maxVisibleStage);
        }
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function formatCurrency(value) {
        if (!value) return '0đ';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(Number(value));
    }

    function formatNumber(value) {
        return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
    }

    function formatDuration(months) {
        if (!months) return '';
        const years = Math.floor(months / 12);
        const remainingMonths = months % 12;

        if (years > 0 && remainingMonths > 0) {
            return `${years} năm ${remainingMonths} tháng`;
        } else if (years > 0) {
            return `${years} năm`;
        } else {
            return `${remainingMonths} tháng`;
        }
    }

    function getDefaultIcon(iconUrl, accentColor) {
        if (iconUrl) return iconUrl;
        return `data:image/svg+xml,${encodeURIComponent(`
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="${accentColor || '#4f46e5'}">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
        `)}`;
    }

    function renderIndustryTabs(industries, activeIndustry) {
        const nav = document.getElementById('industryTabsNav');
        if (!nav) return;

        const tabs = [
            { slug: 'all', name: 'Tất cả', icon: 'fa-layer-group' },
            ...industries.map(industry => ({
                slug: industry,
                name: industry,
                icon: 'fa-briefcase'
            }))
        ];

        nav.innerHTML = tabs.map(tab => `
            <li class="${tab.slug === activeIndustry ? 'active' : ''}">
                <button class="nav-tab" data-industry="${escapeHtml(tab.slug)}">
                    <i class="fa-solid ${escapeHtml(tab.icon)}"></i>
                    ${escapeHtml(tab.name)}
                </button>
            </li>
        `).join('');

        nav.querySelectorAll('.nav-tab').forEach(tab => {
            tab.addEventListener('click', async (e) => {
                e.preventDefault();
                state.activeIndustry = tab.dataset.industry || 'all';
                updateUrl();
                await loadPaths();
                renderIndustryTabs(state.industries, state.activeIndustry);
            });
        });
    }

    function renderStats(homeData) {
        const statPaths = document.getElementById('stat-paths');
        const statIndustries = document.getElementById('stat-industries');
        const statStages = document.getElementById('stat-stages');

        if (statPaths) statPaths.textContent = formatNumber(homeData.allPaths?.length || 0);
        if (statIndustries) statIndustries.textContent = formatNumber(homeData.industries?.length || 0);

        // Calculate total stages
        let totalStages = 0;
        homeData.allPaths?.forEach(path => {
            totalStages += path.stageCount || 0;
        });
        if (statStages) statStages.textContent = formatNumber(totalStages);
    }

    function renderPathCard(path) {
        const accentColor = path.accentColor || '#4f46e5';
        const iconUrl = getDefaultIcon(path.iconUrl, accentColor);

        return `
            <div class="careermap-path-card" data-slug="${escapeHtml(path.slug)}" style="--card-accent: ${escapeHtml(accentColor)}">
                <div class="path-card-header">
                    <div class="path-card-icon" style="background: linear-gradient(135deg, ${escapeHtml(accentColor)}, ${escapeHtml(accentColor)}dd)">
                        <img src="${escapeHtml(iconUrl)}" alt="${escapeHtml(path.title)}" style="width: 32px; height: 32px; object-fit: contain;">
                    </div>
                    <div class="path-card-content">
                        <h3 class="path-card-title">${escapeHtml(path.title)}</h3>
                        <p class="path-card-description">${escapeHtml(path.description || '')}</p>
                    </div>
                </div>
                <div class="path-card-meta">
                    <div class="path-card-meta-item">
                        <i class="fa-solid fa-layer-group"></i>
                        <span>${formatNumber(path.stageCount || 0)} cấp độ</span>
                    </div>
                    <div class="path-card-meta-item">
                        <i class="fa-solid fa-clock"></i>
                        <span>${formatDuration(path.totalDurationMonths)}</span>
                    </div>
                    <div class="path-card-meta-item">
                        <i class="fa-solid fa-dollar-sign"></i>
                        <span>${formatCurrency(path.averageSalaryMin)} - ${formatCurrency(path.averageSalaryMax)}</span>
                    </div>
                </div>
            </div>
        `;
    }

    function renderPathDetail(detail) {
        const container = document.getElementById('pathDetailContent');
        if (!container) return;

        const accentColor = detail.accentColor || '#4f46e5';

        let stagesHtml = '';
        if (detail.stages && detail.stages.length > 0) {
            stagesHtml = detail.stages.map((stage, index) => `
                <div class="path-detail-stage" style="--stage-accent: ${escapeHtml(stage.iconColor || accentColor)}">
                    <div class="stage-header">
                        <div class="stage-number">${escapeHtml(String(index + 1))}</div>
                        <div class="stage-info">
                            <h4 class="stage-title">${escapeHtml(stage.title)}</h4>
                            <p class="stage-description">${escapeHtml(stage.description || '')}</p>
                        </div>
                    </div>
                    <div class="stage-details">
                        <div class="stage-detail-item">
                            <i class="fa-solid fa-briefcase"></i>
                            <span><strong>Vị trí:</strong> ${escapeHtml(stage.jobTitle || stage.title)}</span>
                        </div>
                        <div class="stage-detail-item">
                            <i class="fa-solid fa-chart-line"></i>
                            <span><strong>Kinh nghiệm:</strong> ${escapeHtml(stage.experienceLevel || 'Chưa xác định')}</span>
                        </div>
                        <div class="stage-detail-item">
                            <i class="fa-solid fa-clock"></i>
                            <span><strong>Thời gian:</strong> ${formatDuration(stage.durationMonths)}</span>
                        </div>
                        <div class="stage-detail-item">
                            <i class="fa-solid fa-dollar-sign"></i>
                            <span><strong>Mức lương:</strong> ${formatCurrency(stage.salaryMin)} - ${formatCurrency(stage.salaryMax)}</span>
                        </div>
                    </div>
                    ${stage.skills && stage.skills.length > 0 ? `
                        <div class="stage-skills">
                            <h5 class="skills-title">Kỹ năng cần thiết:</h5>
                            <div class="skills-list">
                                ${stage.skills.map(skill => `
                                    <div class="skill-tag ${skill.isRequired ? 'required' : 'optional'}">
                                        <i class="fa-solid ${skill.isRequired ? 'fa-check-circle' : 'fa-circle'}"></i>
                                        ${escapeHtml(skill.name)}
                                        ${skill.proficiencyLevel ? `<span class="skill-level">${escapeHtml(skill.proficiencyLevel)}</span>` : ''}
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}
                </div>
            `).join('');
        }

        container.innerHTML = `
            <div class="path-detail-header" style="--detail-accent: ${escapeHtml(accentColor)}">
                <div class="path-detail-icon" style="background: linear-gradient(135deg, ${escapeHtml(accentColor)}, ${escapeHtml(accentColor)}dd)">
                    <i class="fa-solid fa-route"></i>
                </div>
                <div class="path-detail-info">
                    <h2 class="path-detail-title">${escapeHtml(detail.title)}</h2>
                    <p class="path-detail-description">${escapeHtml(detail.description || '')}</p>
                    <div class="path-detail-stats">
                        <div class="detail-stat">
                            <i class="fa-solid fa-layer-group"></i>
                            <span>${formatNumber(detail.stageCount || 0)} cấp độ</span>
                        </div>
                        <div class="detail-stat">
                            <i class="fa-solid fa-clock"></i>
                            <span>${formatDuration(detail.totalDurationMonths)}</span>
                        </div>
                        <div class="detail-stat">
                            <i class="fa-solid fa-eye"></i>
                            <span>${formatNumber(detail.views || 0)} lượt xem</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="path-detail-stages">
                <h3 class="stages-section-title">Lộ trình chi tiết</h3>
                ${stagesHtml}
            </div>
        `;
    }

    async function loadHome() {
        try {
            const response = await fetch('/api/career-path/home');
            if (!response.ok) throw new Error('Failed to load home data');

            const homeData = await response.json();
            state.paths = homeData.allPaths || [];
            state.featuredPaths = homeData.featuredPaths || [];
            state.industries = homeData.industries || [];

            renderStats(homeData);
            renderIndustryTabs(state.industries, state.activeIndustry);
            renderFeaturedPaths(state.featuredPaths);
            renderAllPaths(state.paths);
        } catch (error) {
            console.error('Error loading home:', error);
        }
    }

    async function loadPaths() {
        try {
            let url = '/api/career-path/paths';
            const params = new URLSearchParams();

            if (state.activeIndustry && state.activeIndustry !== 'all') {
                params.append('industry', state.activeIndustry);
            }

            if (state.query) {
                params.append('q', state.query);
            }

            if (params.toString()) {
                url += '?' + params.toString();
            }

            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to load paths');

            const paths = await response.json();
            state.paths = paths || [];

            renderAllPaths(state.paths);
        } catch (error) {
            console.error('Error loading paths:', error);
        }
    }

    async function loadPathDetail(slug) {
        try {
            const response = await fetch(`/api/career-path/paths/${encodeURIComponent(slug)}`);
            if (!response.ok) throw new Error('Failed to load path detail');

            const detail = await response.json();
            renderPathDetail(detail);
            openPathDetail();
        } catch (error) {
            console.error('Error loading path detail:', error);
        }
    }

    function openPathDetail() {
        if (detailModal) {
            detailModal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }

    function closePathDetail() {
        if (detailModal) {
            detailModal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    function updateUrl() {
        const params = new URLSearchParams();

        if (state.activeIndustry && state.activeIndustry !== 'all') {
            params.set('industry', state.activeIndustry);
        }

        if (state.query) {
            params.set('q', state.query);
        }

        const url = params.toString() ? `?${params.toString()}` : '';
        window.history.replaceState({}, '', url || window.location.pathname);
    }

    function initFromUrl() {
        const params = new URLSearchParams(window.location.search);

        const industry = params.get('industry');
        if (industry) {
            state.activeIndustry = industry;
        }

        const query = params.get('q');
        if (query) {
            state.query = query;
        }
    }

    function initSearch() {
        const searchForm = document.getElementById('careermapSearchForm');
        const searchInput = document.getElementById('careermapSearchInput');

        if (searchForm && searchInput) {
            searchInput.value = state.query;

            searchForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                state.query = searchInput.value.trim();
                updateUrl();
                await loadPaths();
            });
        }
    }

    function init() {
        if (!listPage) return;

        initFromUrl();
        initScrollAnimation();
        // Not loading home data - only showing static career path

        // Close modal on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closePathDetail();
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose closePathDetail globally
    window.closePathDetail = closePathDetail;
})();
