// ===== Employer Page JavaScript =====

// Load HTML and execute any <script> tags inside it
async function loadHTML(elementId, filePath) {
    try {
        const response = await fetch(filePath);
        if (!response.ok) throw new Error(`Failed to load ${filePath}`);
        const html     = await response.text();
        const element  = document.getElementById(elementId);
        if (!element) return;

        // Set HTML (scripts inside innerHTML do NOT run automatically)
        element.innerHTML = html;

        // Re-create and append each <script> so the browser executes them
        element.querySelectorAll('script').forEach(oldScript => {
            const newScript = document.createElement('script');
            // Copy attributes (type, src, etc.)
            Array.from(oldScript.attributes).forEach(attr =>
                newScript.setAttribute(attr.name, attr.value)
            );
            // Copy inline content
            if (oldScript.src) {
                // External script — set src triggers load & execute
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

// Load employer header and footer when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    Promise.all([
        loadHTML('header-employer-placeholder', 'includes/header-employer.html'),
        loadHTML('footer-placeholder', 'includes/footer.html')
    ]).then(() => {
        // Setup login dropdown after header is loaded
        setTimeout(setupLoginDropdown, 100);
    });
});

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

// Password toggle function
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        input.type = input.type === 'password' ? 'text' : 'password';
    }
}

// Handle login
function handleLogin() {
    alert('Chức năng đăng nhập sẽ được triển khai sau');
}
