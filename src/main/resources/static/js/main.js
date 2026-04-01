
async function loadHTML(elementId, filePath) {
    try {
        const response = await fetch(filePath, { credentials: 'include' });
        if (!response.ok) throw new Error(`Failed to load ${filePath}`);
        const html = await response.text();
        const container = document.getElementById(elementId);
        container.innerHTML = html;
        
        // Execute any script tags in the loaded HTML
        const scripts = container.querySelectorAll('script');
        scripts.forEach(oldScript => {
            const newScript = document.createElement('script');
            if (oldScript.src) {
                newScript.src = oldScript.src;
            } else {
                newScript.textContent = oldScript.textContent;
            }
            oldScript.parentNode.replaceChild(newScript, oldScript);
        });
    } catch (error) {
        console.error('Error loading HTML:', error);
    }
}

// Load header and footer when DOM is ready
document.addEventListener('DOMContentLoaded', async function() {
    // Load header and footer
    await loadHTML('header-placeholder', 'includes/header.html');
    await loadHTML('footer-placeholder', 'includes/footer.html');

    // Setup login dropdown after header is loaded (with small delay to ensure DOM is ready)
    wireToolsNavigation();
    setActiveNavLink();
    setTimeout(setupLoginDropdown, 100);
    setTimeout(setupMobileNavigation, 100);
});

function wireToolsNavigation() {
    const navLinks = document.querySelectorAll('.nav .nav-link');
    navLinks.forEach(link => {
        const label = (link.textContent || '').trim();
        if (label.includes('CÃ´ng Cá»¥') || label.includes('Công Cụ') || label.includes('Cong Cu')) {
            link.setAttribute('href', 'cong-cu.html');
        }
    });
}

// Set active nav-link based on current page
function setActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav .nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (!href) return;

        // Remove active class from all links first
        link.classList.remove('active');

        // Check if current path matches href
        // Handle both absolute and relative paths
        const linkPath = href.startsWith('/') ? href : '/' + href;

        // Exact match or contains match for sub-pages
        if (currentPath === linkPath ||
            currentPath === href ||
            (href !== 'index.html' && currentPath.includes(href.replace('.html', ''))) ||
            (href === '/cam-nang' && currentPath.startsWith('/cam-nang'))) {
            link.classList.add('active');
        }

        // Special case for index.html and root path
        if ((href === 'index.html' || href === '/index.html') &&
            (currentPath === '/' || currentPath === '/index.html' || currentPath.endsWith('/'))) {
            link.classList.add('active');
        }
    });
}

// Setup login dropdown toggle
function setupLoginDropdown() {
    const loginLink = document.querySelector('.login-link');
    const loginWrapper = document.querySelector('.login-dropdown-wrapper');
    
    if (!loginLink || !loginWrapper) return;
    
    // Toggle dropdown on click
    loginLink.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        loginWrapper.classList.toggle('active');
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!loginWrapper.contains(e.target)) {
            loginWrapper.classList.remove('active');
        }
    });
    
    // Prevent dropdown from closing when clicking inside it
    const loginDropdown = document.querySelector('.login-dropdown');
    if (loginDropdown) {
        loginDropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
        
        // Prevent closing when interacting with inputs, buttons, etc.
        const interactiveElements = loginDropdown.querySelectorAll('input, button, a');
        interactiveElements.forEach(element => {
            element.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        });
    }
}

function setupMobileNavigation() {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const nav = document.getElementById('mainNav');

    if (!mobileMenuBtn || !nav) return;

    const closeMenu = () => {
        nav.classList.remove('active');
        mobileMenuBtn.classList.remove('active');
        mobileMenuBtn.setAttribute('aria-expanded', 'false');
        document.body.classList.remove('menu-open');
        nav.querySelectorAll('.nav-dropdown.active').forEach(dropdown => {
            dropdown.classList.remove('active');
        });
    };

    mobileMenuBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        const shouldOpen = !nav.classList.contains('active');
        nav.classList.toggle('active', shouldOpen);
        mobileMenuBtn.classList.toggle('active', shouldOpen);
        mobileMenuBtn.setAttribute('aria-expanded', String(shouldOpen));
        document.body.classList.toggle('menu-open', shouldOpen && window.innerWidth <= 768);
    });

    nav.querySelectorAll('.nav-dropdown > .nav-link').forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            if (window.innerWidth > 768) return;

            e.preventDefault();
            const dropdown = trigger.closest('.nav-dropdown');
            const shouldOpen = !dropdown.classList.contains('active');

            nav.querySelectorAll('.nav-dropdown.active').forEach(item => {
                if (item !== dropdown) item.classList.remove('active');
            });

            dropdown.classList.toggle('active', shouldOpen);
        });
    });

    nav.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', function() {
            if (window.innerWidth > 768) return;

            const isDropdownTrigger = link.parentElement?.classList.contains('nav-dropdown') &&
                link.classList.contains('nav-link');

            if (!isDropdownTrigger) {
                closeMenu();
            }
        });
    });

    document.addEventListener('click', function(e) {
        if (window.innerWidth > 768) return;
        if (!nav.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
            closeMenu();
        }
    });

    window.addEventListener('resize', function() {
        if (window.innerWidth > 768) {
            closeMenu();
        }
    });
}

// ===== Main JavaScript for CoHoiViecLam =====

// Open Login Modal
function openLoginModal() {
    document.getElementById('loginModal').classList.add('active');
    document.body.style.overflow = 'hidden';
}

// Close Login Modal
function closeLoginModal() {
    document.getElementById('loginModal').classList.remove('active');
    document.body.style.overflow = 'auto';
}

// Toggle Password Visibility
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const button = event.currentTarget;
    
    if (input.type === 'password') {
        input.type = 'text';
        button.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
            </svg>
        `;
    } else {
        input.type = 'password';
        button.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
            </svg>
        `;
    }
}

// Handle Login
function handleLogin() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
    // TODO: Implement actual login logic with backend
    console.log('Login attempt:', { email, password });
    
    // Simulate login
    alert(`Đăng nhập thành công!\nEmail: ${email}`);
    closeLoginModal();
}

// Close modal when clicking outside
window.addEventListener('click', function(e) {
    const modal = document.getElementById('loginModal');
    if (e.target === modal) {
        closeLoginModal();
    }
});

// Add event listener to login button in header
document.addEventListener('DOMContentLoaded', function() {
    const loginLink = document.querySelector('.login-link');
    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            openLoginModal();
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const jobAlertForm = document.getElementById('jobAlertForm');
    const jobAlertEmail = document.getElementById('jobAlertEmail');
    const jobAlertStatus = document.getElementById('jobAlertStatus');

    if (!jobAlertForm || !jobAlertEmail || !jobAlertStatus) return;

    jobAlertForm.addEventListener('submit', function(event) {
        event.preventDefault();

        const email = jobAlertEmail.value.trim();
        if (!email) {
            jobAlertStatus.textContent = 'Vui lòng nhập email để nhận thông báo việc làm.';
            return;
        }

        localStorage.setItem('careerVietJobAlertEmail', email);
        jobAlertStatus.textContent = `Đã lưu ${email} để nhận cập nhật việc làm mới.`;
        jobAlertEmail.value = '';
    });
});

// ===== Inject Contact Widget =====
(function() {
    'use strict';

    const isPdfExportContext = function() {
        try {
            const params = new URLSearchParams(window.location.search);
            return params.get('download') === 'pdf'
                || document.body?.classList?.contains('pdf-export-mode');
        } catch {
            return false;
        }
    };

    const removeExistingWidget = function() {
        document.querySelectorAll('.contact-widget, .chatbot-modal-backdrop').forEach(node => node.remove());
    };

    // Inject CSS
    const injectCSS = function() {
        if (isPdfExportContext()) return;

        const links = [
            '/contact-widget/contact-widget.css',
            '/contact-widget/chatbot-modal.css'
        ];

        links.forEach(href => {
            // Check if already injected
            if (document.querySelector(`link[href^="${href}"]`)) return;

            const link = document.createElement('link');
            link.rel = 'stylesheet';
            // Add timestamp to bust cache
            link.href = href + '?v=' + new Date().getTime();
            document.head.appendChild(link);
        });
    };

    // Inject JavaScript
    const injectJS = function() {
        if (isPdfExportContext()) return;

        const src = '/contact-widget/contact-widget.js';

        // Check if already injected
        if (document.querySelector(`script[src^="${src}"]`)) return;

        const script = document.createElement('script');
        // Add timestamp to bust cache
        script.src = src + '?v=' + new Date().getTime();
        script.async = true;
        document.body.appendChild(script);
    };

    // Initialize widget when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            if (isPdfExportContext()) {
                removeExistingWidget();
                return;
            }
            injectCSS();
            injectJS();
        });
    } else {
        if (isPdfExportContext()) {
            removeExistingWidget();
            return;
        }
        injectCSS();
        injectJS();
    }

    console.log('Contact Widget injected successfully');
})();
