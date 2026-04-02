(function () {
    // ── CSS ──────────────────────────────────────────────────────────────────
    const css = `
        .sidebar {
            width: 260px;
            background: #0f172a;
            color: white;
            display: flex;
            flex-direction: column;
            position: fixed;
            height: 100vh;
            left: 0;
            top: 0;
            z-index: 100;
            overflow-y: auto;
        }
        .sidebar-header {
            padding: 20px 16px;
            border-bottom: 1px solid rgba(255,255,255,0.07);
            flex-shrink: 0;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .sidebar-header:hover { opacity: 0.9; }
        .sidebar-logo-icon {
            width: 38px;
            height: 38px;
            border-radius: 10px;
            background: #2563eb;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            font-weight: 800;
            color: white;
            letter-spacing: -1px;
            flex-shrink: 0;
        }
        .sidebar-logo-text {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
        .sidebar-logo-name {
            font-size: 14px;
            font-weight: 700;
            color: #f1f5f9;
            letter-spacing: -0.3px;
            line-height: 1;
        }
        .admin-badge {
            display: inline-block;
            background: rgba(37,99,235,0.25);
            color: #93c5fd;
            font-size: 10px;
            padding: 2px 7px;
            border-radius: 4px;
            font-weight: 600;
            letter-spacing: 0.5px;
            border: 1px solid rgba(37,99,235,0.4);
            line-height: 1.4;
        }
        .sidebar-nav {
            padding: 20px 12px;
            flex-grow: 1;
            overflow-y: auto;
        }
        .nav-section {
            margin-bottom: 24px;
        }
        .nav-section-title {
            font-size: 11px;
            text-transform: uppercase;
            color: #94a3b8;
            padding: 0 12px;
            margin-bottom: 12px;
            font-weight: 600;
            letter-spacing: 0.5px;
        }
        .nav-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 12px;
            color: #f1f5f9;
            text-decoration: none;
            border-radius: 6px;
            margin-bottom: 4px;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.2s ease;
            cursor: pointer;
        }
        .nav-item:hover {
            background: #1e293b;
            color: #f1f5f9;
        }
        .nav-item.active {
            background: #2563eb;
            color: white;
        }
        .nav-item svg {
            width: 20px;
            height: 20px;
            stroke-width: 2;
            flex-shrink: 0;
        }
        /* Dropdown group */
        .nav-group {}
        .nav-group-toggle {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 12px;
            color: #f1f5f9;
            border-radius: 6px;
            margin-bottom: 2px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s ease;
            user-select: none;
        }
        .nav-group-toggle:hover { background: #1e293b; color: #f1f5f9; }
        .nav-group-toggle.open { color: #f1f5f9; }
        .nav-group-toggle.has-active { background: #2563eb; color: white; }
        .nav-group-toggle svg.icon { width: 20px; height: 20px; stroke-width: 2; flex-shrink: 0; }
        .nav-group-toggle .chevron {
            width: 14px; height: 14px; stroke-width: 2.5;
            margin-left: auto; flex-shrink: 0;
            transition: transform 0.2s ease;
        }
        .nav-group-toggle.open .chevron { transform: rotate(180deg); }
        .nav-group-children {
            display: none;
            padding-left: 16px;
            margin-bottom: 4px;
            padding-top: 4px;
            padding-bottom: 4px;
        }
        .nav-group-children.open { display: block; }
        .nav-sub-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 8px 12px;
            color: #f1f5f9;
            text-decoration: none;
            border-radius: 6px;
            margin-bottom: 4px;
            font-size: 13px;
            font-weight: 500;
            transition: all 0.2s ease;
            border-left: 2px solid #1e293b;
        }
        .nav-sub-item:hover { background: #1e293b; color: #f1f5f9; border-left-color: #334155; }
        .nav-sub-item.active { color: #93c5fd; border-left-color: #2563eb; background: rgba(37,99,235,0.1); }
        .nav-sub-item svg { width: 16px; height: 16px; stroke-width: 2; flex-shrink: 0; }
    `;

    const style = document.createElement('style');
    style.textContent = css;
    document.head.appendChild(style);

    const currentPath = window.location.pathname;

    // ── Nav config ────────────────────────────────────────────────────────────
    const navItems = [
        {
            section: 'Tổng quan',
            items: [
                {
                    href: '/admin/dashboard.html',
                    label: 'Dashboard',
                    svg: '<rect x="3" y="3" width="7" height="9" rx="1"></rect><rect x="14" y="3" width="7" height="5" rx="1"></rect><rect x="14" y="12" width="7" height="9" rx="1"></rect><rect x="3" y="16" width="7" height="5" rx="1"></rect>'
                },
                {
                    href: '/admin/analytics.html',
                    label: 'Thống kê',
                    svg: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>'
                }
            ]
        },
        {
            section: 'Quản lý',
            items: [
                {
                    href: '/admin/users.html',
                    label: 'Người dùng',
                    svg: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M22 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path>'
                },
                {
                    href: '/admin/employers.html',
                    label: 'Nhà tuyển dụng',
                    svg: '<rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>'
                },
                {
                    // Dropdown group
                    group: true,
                    label: 'Việc làm',
                    svg: '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line>',
                    children: [
                        {
                            href: '/admin/jobs.html',
                            label: 'Quản lý Jobs',
                            svg: '<rect x="2" y="7" width="20" height="14" rx="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>'
                        },
                        {
                            href: '/admin/applications.html',
                            label: 'Quản lý Applications',
                            svg: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline>'
                        },
                        {
                            href: '/admin/career-guide.html',
                            label: 'Career Guide',
                            svg: '<path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>'
                        }
                    ]
                },
                {
                    href: '/admin/cv-templates.html',
                    label: 'Mẫu CV',
                    svg: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline>'
                },
                {
                    href: '/admin/hero-banners.html',
                    label: 'Hero Banner',
                    svg: '<rect x="3" y="4" width="18" height="14" rx="2"></rect><path d="M7 15l3-3 3 2 4-5 2 3"></path><circle cx="8.5" cy="8.5" r="1.5"></circle>'
                },
                {
                    href: '/admin/top-employer-logos.html',
                    label: 'Top Employers',
                    svg: '<rect x="3" y="6" width="18" height="12" rx="2"></rect><path d="M7 12h10"></path><path d="M12 6v12"></path>'
                },
                {
                    href: '/admin/announcements.html',
                    label: 'Thông báo',
                    svg: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path>'
                }
            ]
        },
        {
            section: 'Hệ thống',
            items: [
                {
                    href: '/admin/settings.html',
                    label: 'Cài đặt',
                    svg: '<circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>'
                }
            ]
        }
    ];

    // ── Build sidebar ─────────────────────────────────────────────────────────
    function buildSidebar() {
        const aside = document.createElement('aside');
        aside.className = 'sidebar';

        aside.innerHTML = `
            <a href="/admin/dashboard.html" class="sidebar-header">
                <div class="sidebar-logo-icon">JC</div>
                <div class="sidebar-logo-text">
                    <div class="sidebar-logo-name">CoHoiViecLam</div>
                    <span class="admin-badge">ADMIN PANEL</span>
                </div>
            </a>
        `;

        const nav = document.createElement('nav');
        nav.className = 'sidebar-nav';

        navItems.forEach(({ section, items }) => {
            const sec = document.createElement('div');
            sec.className = 'nav-section';
            sec.innerHTML = `<div class="nav-section-title">${section}</div>`;

            items.forEach((item) => {
                if (item.group) {
                    // Kiểm tra có child nào đang active không
                    const hasActive = item.children.some(c =>
                        currentPath === c.href || currentPath.endsWith(c.href)
                    );

                    const group = document.createElement('div');
                    group.className = 'nav-group';

                    const toggle = document.createElement('div');
                    toggle.className = 'nav-group-toggle' + (hasActive ? ' has-active open' : '');
                    toggle.innerHTML = `
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" class="icon">${item.svg}</svg>
                        <span>${item.label}</span>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" class="chevron"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    `;

                    const children = document.createElement('div');
                    children.className = 'nav-group-children' + (hasActive ? ' open' : '');

                    item.children.forEach(({ href, label, svg }) => {
                        const isActive = currentPath === href || currentPath.endsWith(href);
                        const a = document.createElement('a');
                        a.href = href;
                        a.className = 'nav-sub-item' + (isActive ? ' active' : '');
                        a.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor">${svg}</svg>${label}`;
                        children.appendChild(a);
                    });

                    toggle.addEventListener('click', () => {
                        const isOpen = children.classList.contains('open');
                        children.classList.toggle('open', !isOpen);
                        toggle.classList.toggle('open', !isOpen);
                        if (hasActive) return; // giữ màu active nếu đang ở trang con
                        toggle.classList.toggle('has-active', false);
                    });

                    group.appendChild(toggle);
                    group.appendChild(children);
                    sec.appendChild(group);
                } else {
                    const isActive = currentPath === item.href || currentPath.endsWith(item.href);
                    const a = document.createElement('a');
                    a.href = item.href;
                    a.className = 'nav-item' + (isActive ? ' active' : '');
                    a.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor">${item.svg}</svg>${item.label}`;
                    sec.appendChild(a);
                }
            });

            nav.appendChild(sec);
        });

        aside.appendChild(nav);
        return aside;
    }

    // ── Mount ─────────────────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', function () {
        const old = document.querySelector('aside.sidebar');
        if (old) old.remove();
        document.body.insertBefore(buildSidebar(), document.body.firstChild);
    });
})();
