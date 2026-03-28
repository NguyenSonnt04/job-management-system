(function () {
    const listPage = document.getElementById('guideListPage');
    const detailPage = document.getElementById('guideDetailPage');

    const state = {
        categories: [],
        articles: [],
        activeCategory: '',
        query: ''
    };

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function formatDate(iso) {
        if (!iso) return '';
        try {
            return new Intl.DateTimeFormat('vi-VN', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            }).format(new Date(iso));
        } catch {
            return iso;
        }
    }

    function formatNumber(value) {
        return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
    }

    function articleHref(slug) {
        return `/cam-nang/${encodeURIComponent(slug)}`;
    }

    function getLeadArticle(articles) {
        if (!articles.length) return null;
        return articles.find(article => article.pinned || article.featured) || articles[0];
    }

    function buildArticleList(articles, lead) {
        const leadId = lead ? lead.id : null;
        const railIds = new Set();
        if (leadId != null) railIds.add(leadId);

        const rail = articles.filter(article => article.id !== leadId).slice(0, 4);
        rail.forEach(article => railIds.add(article.id));

        const grid = articles.filter(article => !railIds.has(article.id)).slice(0, 12);
        return { rail, grid };
    }

    function renderTabs(categories, activeSlug, totalCount) {
        const nav = document.getElementById('guideTabsNav');
        if (!nav) return;

        nav.innerHTML = [
            `<li class="${!activeSlug ? 'active' : ''}">
                <a href="#" data-category="">Mới nhất</a>
            </li>`,
            ...categories.map(category => `
                <li class="${category.slug === activeSlug ? 'active' : ''}">
                    <a href="#" data-category="${escapeHtml(category.slug)}">${escapeHtml(category.name)}</a>
                </li>
            `)
        ].join('');

        nav.querySelectorAll('a[data-category]').forEach(link => {
            link.addEventListener('click', async (e) => {
                e.preventDefault();
                state.activeCategory = link.dataset.category || '';
                updateUrl();

                // Thêm class category-active cho search panel khi có category được chọn
                const searchPanel = document.querySelector('.guide-search-panel');
                if (searchPanel) {
                    if (state.activeCategory) {
                        searchPanel.classList.add('category-active');
                    } else {
                        searchPanel.classList.remove('category-active');
                    }
                }

                await loadArticles();
            });
        });
    }

    function renderStats(articles) {
        const articleCount = document.getElementById('guideHeroArticleCount');
        const categoryCount = document.getElementById('guideHeroCategoryCount');
        const featuredCount = document.getElementById('guideHeroFeaturedCount');
        const resultCount = document.getElementById('guideResultCount');

        const featuredTotal = articles.filter(article => article.featured || article.pinned).length;

        if (articleCount) articleCount.textContent = formatNumber(articles.length);
        if (categoryCount) categoryCount.textContent = formatNumber(state.categories.length);
        if (featuredCount) featuredCount.textContent = formatNumber(featuredTotal);
        if (resultCount) resultCount.textContent = `${formatNumber(articles.length)} bài viết`;
    }

    function renderFeatured(articles) {
        const container = document.getElementById('guideFeatured');
        if (!container) return;

        const lead = getLeadArticle(articles);
        if (!lead) {
            container.innerHTML = '';
            return;
        }

        const { rail } = buildArticleList(articles, lead);
        container.innerHTML = `
            <article class="guide-lead-card" style="--article-accent:${escapeHtml(lead.categoryAccentColor || '#1d4ed8')}">
                <a href="${articleHref(lead.slug)}" class="guide-lead-media">
                    ${lead.coverImageUrl ? `<img src="${escapeHtml(lead.coverImageUrl)}" alt="${escapeHtml(lead.coverImageAlt || lead.title)}">` : ''}
                </a>
                <div class="guide-lead-body">
                    <div class="guide-lead-kicker">${escapeHtml(lead.categoryName || 'Cẩm nang')}</div>
                    <h2 class="guide-lead-title"><a href="${articleHref(lead.slug)}">${escapeHtml(lead.title)}</a></h2>
                    <p class="guide-lead-excerpt">${escapeHtml(lead.excerpt || '')}</p>
                    <div class="guide-lead-meta">
                        <span><i class="fa-regular fa-calendar"></i> ${formatDate(lead.publishedAt)}</span>
                        <span><i class="fa-regular fa-clock"></i> ${escapeHtml(lead.readTimeMinutes || 5)} phút đọc</span>
                        <span><i class="fa-regular fa-user"></i> ${escapeHtml(lead.authorName || 'CareerViet')}</span>
                    </div>
                    <a href="${articleHref(lead.slug)}" class="guide-lead-cta">Đọc bài viết <i class="fa-solid fa-arrow-right"></i></a>
                </div>
            </article>
            <div class="guide-rail">
                ${rail.map(article => `
                    <article class="guide-rail-card" style="--article-accent:${escapeHtml(article.categoryAccentColor || '#1d4ed8')}">
                        <a href="${articleHref(article.slug)}" class="guide-rail-media">
                            ${article.coverImageUrl ? `<img src="${escapeHtml(article.coverImageUrl)}" alt="${escapeHtml(article.coverImageAlt || article.title)}">` : ''}
                        </a>
                        <div class="guide-rail-body">
                            <div class="guide-rail-kicker">${escapeHtml(article.categoryName || 'Cẩm nang')}</div>
                            <h3 class="guide-rail-title"><a href="${articleHref(article.slug)}">${escapeHtml(article.title)}</a></h3>
                            <div class="guide-card-meta">
                                <span><i class="fa-regular fa-calendar"></i> ${formatDate(article.publishedAt)}</span>
                                <span><i class="fa-regular fa-clock"></i> ${escapeHtml(article.readTimeMinutes || 5)} phút</span>
                            </div>
                        </div>
                    </article>
                `).join('')}
            </div>
        `;
    }

    function renderGrid(articles) {
        const grid = document.getElementById('guideGrid');
        const empty = document.getElementById('guideEmpty');
        if (!grid || !empty) return;

        const lead = getLeadArticle(articles);
        const { grid: items } = buildArticleList(articles, lead);

        if (!articles.length) {
            grid.innerHTML = '';
            empty.hidden = false;
            return;
        }

        empty.hidden = true;
        if (!items.length) {
            grid.innerHTML = `
                <div class="guide-empty" style="grid-column: 1 / -1; margin-top: 0;">
                    <strong>Đã hiển thị hết bài phù hợp</strong>
                    <div>Các bài khớp nhất đã nằm ở phần nổi bật phía trên.</div>
                </div>
            `;
            return;
        }

        grid.innerHTML = items.map(article => `
            <article class="guide-card" style="--article-accent:${escapeHtml(article.categoryAccentColor || '#1d4ed8')}">
                <a href="${articleHref(article.slug)}" class="guide-card-media">
                    ${article.coverImageUrl ? `<img src="${escapeHtml(article.coverImageUrl)}" alt="${escapeHtml(article.coverImageAlt || article.title)}">` : ''}
                </a>
                <div class="guide-card-body">
                    <div class="guide-card-kicker">${escapeHtml(article.categoryName || 'Cẩm nang')}</div>
                    <h3 class="guide-card-title"><a href="${articleHref(article.slug)}">${escapeHtml(article.title)}</a></h3>
                    <p class="guide-card-excerpt">${escapeHtml(article.excerpt || '')}</p>
                    <div class="guide-card-meta">
                        <span><i class="fa-regular fa-calendar"></i> ${formatDate(article.publishedAt)}</span>
                        <span><i class="fa-regular fa-clock"></i> ${escapeHtml(article.readTimeMinutes || 5)} phút</span>
                        <span><i class="fa-regular fa-user"></i> ${escapeHtml(article.authorName || 'CareerViet')}</span>
                    </div>
                </div>
            </article>
        `).join('');
    }

    function updateUrl() {
        const url = new URL(window.location.href);
        if (state.activeCategory) {
            url.searchParams.set('category', state.activeCategory);
        } else {
            url.searchParams.delete('category');
        }
        if (state.query) {
            url.searchParams.set('q', state.query);
        } else {
            url.searchParams.delete('q');
        }
        window.history.replaceState({}, '', url);
    }

    async function loadArticles() {
        const params = new URLSearchParams();
        if (state.activeCategory) params.set('category', state.activeCategory);
        if (state.query) params.set('q', state.query);
        params.set('limit', '24');

        const loading = document.getElementById('guideLoading');
        const featured = document.getElementById('guideFeatured');
        const grid = document.getElementById('guideGrid');
        if (loading) loading.hidden = false;
        if (featured) featured.style.opacity = '0.55';
        if (grid) grid.style.opacity = '0.55';

        try {
            const response = await fetch(`/api/career-guide/articles?${params.toString()}`, {
                credentials: 'include'
            });
            const articles = response.ok ? await response.json() : [];
            state.articles = Array.isArray(articles) ? articles : [];
        } catch {
            state.articles = [];
        } finally {
            if (loading) loading.hidden = true;
            if (featured) featured.style.opacity = '1';
            if (grid) grid.style.opacity = '1';
        }

        renderStats(state.articles);
        renderTabs(state.categories, state.activeCategory, state.articles.length);
        renderFeatured(state.articles);
        renderGrid(state.articles);

        // Update search panel state
        const searchPanel = document.querySelector('.guide-search-panel');
        if (searchPanel) {
            if (state.activeCategory) {
                searchPanel.classList.add('category-active');
            } else {
                searchPanel.classList.remove('category-active');
            }
        }
    }

    function bindListPage(home) {
        state.categories = home.categories || [];
        state.articles = home.articles || [];

        const url = new URL(window.location.href);
        state.activeCategory = url.searchParams.get('category') || '';
        state.query = url.searchParams.get('q') || '';

        const searchForm = document.getElementById('guideSearchForm');
        const searchInput = document.getElementById('guideSearchInput');
        const resetBtn = document.getElementById('guideResetBtn');

        if (searchInput && state.query) {
            searchInput.value = state.query;
        }

        renderStats(state.articles);
        renderTabs(state.categories, state.activeCategory, state.articles.length);
        renderFeatured(state.articles);
        renderGrid(state.articles);

        // Update search panel state
        const searchPanel = document.querySelector('.guide-search-panel');
        if (searchPanel) {
            if (state.activeCategory) {
                searchPanel.classList.add('category-active');
            } else {
                searchPanel.classList.remove('category-active');
            }
        }

        if (searchForm) {
            searchForm.addEventListener('submit', async event => {
                event.preventDefault();
                state.query = searchInput ? searchInput.value.trim() : '';
                updateUrl();
                await loadArticles();
            });
        }

        if (resetBtn) {
            resetBtn.addEventListener('click', async event => {
                event.preventDefault();
                state.activeCategory = '';
                state.query = '';
                if (searchInput) searchInput.value = '';
                updateUrl();
                await loadArticles();
            });
        }
    }

    async function initListPage() {
        const loading = document.getElementById('guideLoading');
        if (loading) loading.hidden = false;

        try {
            const response = await fetch('/api/career-guide/home', { credentials: 'include' });
            const home = response.ok ? await response.json() : { categories: [], articles: [] };
            bindListPage(home);
            await loadArticles();
        } catch {
            const empty = document.getElementById('guideEmpty');
            if (empty) {
                empty.hidden = false;
                empty.innerHTML = '<strong>Không thể tải cẩm nang</strong><div>Vui lòng thử lại sau.</div>';
            }
        } finally {
            if (loading) loading.hidden = true;
        }
    }

    function renderRelatedItem(article) {
        return `
            <a class="guide-related-item" href="${articleHref(article.slug)}">
                <span class="guide-related-thumb">
                    ${article.coverImageUrl ? `<img src="${escapeHtml(article.coverImageUrl)}" alt="${escapeHtml(article.coverImageAlt || article.title)}">` : ''}
                </span>
                <span>
                    <span class="guide-related-title">${escapeHtml(article.title)}</span>
                    <span class="guide-related-meta">${formatDate(article.publishedAt)} • ${escapeHtml(article.readTimeMinutes || 5)} phút đọc</span>
                </span>
            </a>
        `;
    }

    function renderDetailNotFound(message) {
        const container = document.getElementById('guideDetailRoot');
        if (!container) return;
        container.innerHTML = `
            <div class="guide-not-found">
                <h2>Không tìm thấy bài viết</h2>
                <p>${escapeHtml(message || 'Bài viết này không còn tồn tại hoặc đã được ẩn.')}</p>
                <p style="margin-top:16px;">
                    <a href="/cam-nang" class="guide-share-link"><i class="fa-solid fa-arrow-left"></i> Quay lại cẩm nang</a>
                </p>
            </div>
        `;
    }

    function bindDetailPage() {
        const params = new URLSearchParams(window.location.search);
        const slug = params.get('slug');
        const container = document.getElementById('guideDetailRoot');

        if (!slug) {
            renderDetailNotFound('Thiếu slug bài viết.');
            return;
        }

        if (container) {
            container.innerHTML = '<div class="guide-loading">Đang tải bài viết...</div>';
        }

        fetch(`/api/career-guide/articles/${encodeURIComponent(slug)}`, {
            credentials: 'include'
        })
            .then(async response => {
                if (!response.ok) {
                    throw new Error(response.status === 404 ? 'Bài viết không tồn tại.' : 'Không thể tải bài viết.');
                }
                return response.json();
            })
            .then(detail => {
                const article = detail.article || {};
                const root = document.getElementById('guideDetailRoot');
                if (!root) return;

                document.title = `${article.title || 'Cẩm nang'} | CareerViet`;
                const metaDescription = document.querySelector('meta[name="description"]');
                if (metaDescription && article.excerpt) {
                    metaDescription.setAttribute('content', article.excerpt);
                }

                const tags = (detail.tags || article.tags || []).map(tag => `
                    <span class="guide-tag-pill">${escapeHtml(tag.name)}</span>
                `).join('');
                const related = detail.relatedArticles || [];
                const relatedListHtml = related.length
                    ? related.map(renderRelatedItem).join('')
                    : '<div class="guide-empty" style="padding:20px; margin-top:0;">Chưa có bài liên quan.</div>';
                const relatedGridHtml = related.length
                    ? related.map(article => `
                        <article class="guide-card" style="--article-accent:${escapeHtml(article.categoryAccentColor || '#1d4ed8')}">
                            <a href="${articleHref(article.slug)}" class="guide-card-media">
                                ${article.coverImageUrl ? `<img src="${escapeHtml(article.coverImageUrl)}" alt="${escapeHtml(article.coverImageAlt || article.title)}">` : ''}
                                <span class="guide-card-badge">${escapeHtml(article.categoryName || 'Cẩm nang')}</span>
                            </a>
                            <div class="guide-card-body">
                                <div class="guide-card-kicker">${escapeHtml(article.authorName || 'CareerViet')}</div>
                                <h3 class="guide-card-title"><a href="${articleHref(article.slug)}">${escapeHtml(article.title)}</a></h3>
                                <p class="guide-card-excerpt">${escapeHtml(article.excerpt || '')}</p>
                                <div class="guide-card-meta">
                                    <span><i class="fa-regular fa-calendar"></i> ${formatDate(article.publishedAt)}</span>
                                    <span><i class="fa-regular fa-clock"></i> ${escapeHtml(article.readTimeMinutes || 5)} phút</span>
                                </div>
                            </div>
                        </article>
                    `).join('')
                    : '<div class="guide-empty" style="grid-column: 1 / -1; margin-top: 0;">Chưa có bài liên quan.</div>';

                root.innerHTML = `
                    <section class="guide-detail-hero">
                        <div class="guide-breadcrumbs">
                            <a href="/cam-nang">Cẩm nang</a>
                            <span>/</span>
                            <span>${escapeHtml(article.categoryName || '')}</span>
                        </div>
                        <h1 class="guide-detail-title">${escapeHtml(article.title || '')}</h1>
                        <p class="guide-detail-lead">${escapeHtml(article.excerpt || '')}</p>
                        <div class="guide-detail-meta">
                            <span><i class="fa-regular fa-calendar"></i> ${formatDate(article.publishedAt)}</span>
                            <span><i class="fa-regular fa-clock"></i> ${escapeHtml(article.readTimeMinutes || 5)} phút đọc</span>
                            <span><i class="fa-regular fa-eye"></i> ${formatNumber(article.views)}</span>
                            <span><i class="fa-solid fa-user"></i> ${escapeHtml(article.authorName || 'CareerViet')}</span>
                        </div>
                        <div class="guide-tag-list">${tags}</div>
                    </section>

                    <section class="guide-detail-layout">
                        <article class="guide-article-card">
                            <figure class="guide-cover">
                                ${article.coverImageUrl ? `<img src="${escapeHtml(article.coverImageUrl)}" alt="${escapeHtml(article.coverImageAlt || article.title || '')}">` : ''}
                            </figure>
                            <div class="guide-article-body">
                                ${detail.contentHtml || ''}
                            </div>
                        </article>

                        <aside class="guide-sidebar">
                            <div class="guide-sidebar-card">
                                <h3>Thông tin bài viết</h3>
                                <div class="guide-info-list">
                                    <div class="guide-info-row">
                                        <span>Danh mục</span>
                                        <strong>${escapeHtml(article.categoryName || '')}</strong>
                                    </div>
                                    <div class="guide-info-row">
                                        <span>Tác giả</span>
                                        <strong>${escapeHtml(article.authorName || 'CareerViet')}</strong>
                                    </div>
                                    <div class="guide-info-row">
                                        <span>Đọc lúc</span>
                                        <strong>${formatDate(article.publishedAt)}</strong>
                                    </div>
                                    <div class="guide-info-row">
                                        <span>Lượt xem</span>
                                        <strong>${formatNumber(article.views)}</strong>
                                    </div>
                                </div>
                                <div style="margin-top:18px;" class="guide-share">
                                    <button type="button" class="guide-copy-btn" id="guideCopyBtn">
                                        <i class="fa-regular fa-copy"></i>
                                        Sao chép link
                                    </button>
                                    <a href="/cam-nang" class="guide-share-link">
                                        <i class="fa-solid fa-arrow-left"></i>
                                        Về cẩm nang
                                    </a>
                                </div>
                            </div>

                            <div class="guide-sidebar-card">
                                <h3>Bài liên quan</h3>
                                <div class="guide-related-list">
                                    ${relatedListHtml}
                                </div>
                            </div>
                        </aside>
                    </section>

                    <section class="guide-related-section">
                        <div class="guide-section-head">
                            <div>
                                <div class="guide-overline">Đọc tiếp</div>
                                <h2>Chủ đề liên quan</h2>
                            </div>
                        </div>
                        <div class="guide-grid">
                            ${relatedGridHtml}
                        </div>
                    </section>
                `;

                const copyBtn = document.getElementById('guideCopyBtn');
                if (copyBtn) {
                    copyBtn.addEventListener('click', async () => {
                        try {
                            await navigator.clipboard.writeText(window.location.href);
                            copyBtn.innerHTML = '<i class="fa-solid fa-check"></i> Đã sao chép';
                            setTimeout(() => {
                                copyBtn.innerHTML = '<i class="fa-regular fa-copy"></i> Sao chép link';
                            }, 1800);
                        } catch {
                            copyBtn.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> Không thể sao chép';
                        }
                    });
                }
            })
            .catch(error => {
                renderDetailNotFound(error.message);
            });
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (listPage) {
            initListPage();
        } else if (detailPage) {
            bindDetailPage();
        }
    });
})();
