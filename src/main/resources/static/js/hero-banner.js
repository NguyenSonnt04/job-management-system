document.addEventListener('DOMContentLoaded', function () {
    const slider = document.getElementById('heroBannerSlider');
    const dotsContainer = document.getElementById('heroBannerDots');
    const controls = document.getElementById('heroBannerControls');
    const prevButton = document.getElementById('heroBannerPrev');
    const nextButton = document.getElementById('heroBannerNext');

    if (!slider || !dotsContainer) return;

    const fallbackSlides = [...slider.querySelectorAll('.hero-banner-slide')];
    let slides = [];
    let currentIndex = 0;
    let autoplayId = null;
    const autoplayDelay = 3000;

    function normalizeTarget(url) {
        return url && url.trim() ? url.trim() : '';
    }

    function renderSlides(items) {
        slider.innerHTML = items.map((item, index) => {
            const href = normalizeTarget(item.targetUrl);
            const disabledAttrs = href ? '' : ' data-disabled="true"';
            const safeName = escapeHtml(item.name || 'Banner nổi bật');
            const safeImage = escapeAttribute(item.imageUrl || '');

            return `
                <a class="hero-banner-slide${index === 0 ? ' is-active' : ''}" href="${href || '#'}" aria-label="${safeName}" style="background-image: linear-gradient(120deg, rgba(13, 29, 73, 0.38), rgba(13, 29, 73, 0.18)), url('${safeImage}');"${disabledAttrs}>
                    <span class="hero-banner-meta">${safeName}</span>
                </a>
            `;
        }).join('');

        slides = [...slider.querySelectorAll('.hero-banner-slide')];
        slides.forEach((slide) => {
            if (slide.dataset.disabled === 'true') {
                slide.addEventListener('click', function (event) {
                    event.preventDefault();
                });
            }
        });
    }

    function renderDots(count) {
        dotsContainer.innerHTML = '';
        if (count <= 1) {
            dotsContainer.style.display = 'none';
            if (controls) controls.hidden = true;
            return;
        }

        dotsContainer.style.display = 'flex';
        if (controls) controls.hidden = false;

        for (let index = 0; index < count; index += 1) {
            const dot = document.createElement('button');
            dot.type = 'button';
            dot.className = 'dot' + (index === 0 ? ' active' : '');
            dot.setAttribute('aria-label', `Chuyển đến banner ${index + 1}`);
            dot.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                goTo(index);
                restartAutoplay();
            });
            dotsContainer.appendChild(dot);
        }
    }

    function setActive(index) {
        currentIndex = index;
        slides.forEach((slide, slideIndex) => {
            slide.classList.toggle('is-active', slideIndex === index);
        });

        [...dotsContainer.querySelectorAll('.dot')].forEach((dot, dotIndex) => {
            dot.classList.toggle('active', dotIndex === index);
        });
    }

    function goTo(index) {
        if (!slides.length) return;
        const normalizedIndex = (index + slides.length) % slides.length;
        setActive(normalizedIndex);
    }

    function next() {
        goTo(currentIndex + 1);
    }

    function prev() {
        goTo(currentIndex - 1);
    }

    function stopAutoplay() {
        if (autoplayId) {
            window.clearInterval(autoplayId);
            autoplayId = null;
        }
    }

    function startAutoplay() {
        stopAutoplay();
        if (slides.length <= 1) return;
        autoplayId = window.setInterval(next, autoplayDelay);
    }

    function restartAutoplay() {
        startAutoplay();
    }

    function hydrateFallback() {
        slides = fallbackSlides;
        renderDots(slides.length);
        setActive(0);
        startAutoplay();
    }

    function bindHoverPause() {
        if (!window.matchMedia('(hover: hover)').matches) return;

        const heroStage = slider.closest('.hero-stage');
        if (!heroStage) return;

        heroStage.addEventListener('mouseenter', stopAutoplay);
        heroStage.addEventListener('mouseleave', startAutoplay);
    }

    function bindControls() {
        if (prevButton) {
            prevButton.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                prev();
                restartAutoplay();
            });
        }

        if (nextButton) {
            nextButton.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                next();
                restartAutoplay();
            });
        }
    }

    async function loadBanners() {
        try {
            const response = await fetch('/api/hero-banners', { credentials: 'include' });
            if (!response.ok) throw new Error('Không tải được banner');
            const items = await response.json();

            if (!Array.isArray(items) || items.length === 0) {
                hydrateFallback();
                return;
            }

            renderSlides(items);
            renderDots(items.length);
            setActive(0);
            startAutoplay();
        } catch (error) {
            hydrateFallback();
        }
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function escapeAttribute(value) {
        return String(value).replace(/'/g, '%27');
    }

    bindHoverPause();
    bindControls();
    loadBanners();
});
