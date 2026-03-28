document.addEventListener('DOMContentLoaded', function () {
    const grid = document.getElementById('topEmployerGrid');
    if (!grid) return;

    const fallbackMarkup = grid.innerHTML;

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function escapeAttribute(value) {
        return String(value ?? '').replace(/"/g, '&quot;');
    }

    function normalizeTarget(url) {
        return url && url.trim() ? url.trim() : '';
    }

    function render(items) {
        if (!Array.isArray(items) || items.length === 0) {
            grid.innerHTML = fallbackMarkup;
            return;
        }

        grid.innerHTML = items.map((item) => {
            const name = escapeHtml(item.name || 'Nhà tuyển dụng');
            const image = escapeAttribute(item.imageUrl || '');
            const href = normalizeTarget(item.targetUrl);
            const tag = href ? 'a' : 'div';
            const attrs = href
                ? `href="${escapeAttribute(href)}" target="_blank" rel="noopener noreferrer"`
                : 'aria-disabled="true"';

            return `
                <${tag} class="top-employer-tile" ${attrs}>
                    <img src="${image}" alt="${name}" loading="lazy" onerror="this.closest('.top-employer-tile').classList.add('is-fallback');this.remove();">
                    <span class="top-employer-name">${name}</span>
                </${tag}>
            `;
        }).join('');
    }

    async function load() {
        try {
            const response = await fetch('/api/top-employer-logos', { credentials: 'include' });
            if (!response.ok) throw new Error('Không tải được logo nhà tuyển dụng');
            const items = await response.json();
            render(items);
        } catch (error) {
            grid.innerHTML = fallbackMarkup;
        }
    }

    load();
});
