/**
 * Chatbot AI Integration Script
 * Include this script in any page to add the chatbot
 *
 * Usage: Add <script src="chatbot-ai/chatbot-init.js"></script> before closing </body> tag
 */

(function() {
    'use strict';

    // Configuration
    const CONFIG = {
        position: 'right', // 'left' or 'right'
        bottom: '30px',
        autoOpen: false,
        showWelcome: true,
        buttonText: 'Hỏi AI'
    };

    // Inject styles
    function injectStyles() {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = 'chatbot-ai/chatbot.css';
        document.head.appendChild(link);
    }

    // Create chatbot elements
    function createChatbotElements() {
        // Create floating button if not exists
        if (!document.getElementById('chatbotToggle')) {
            const toggle = document.createElement('div');
            toggle.id = 'chatbotToggle';
            toggle.className = 'chatbot-toggle';
            toggle.title = 'Chat với AI';
            toggle.innerHTML = `
                <img src="images/logo_chatbot.png" alt="AI Chatbot">
                <span class="chatbot-badge">?</span>
            `;
            document.body.appendChild(toggle);
        }

        // Check if popup already exists
        if (!document.getElementById('chatbotPopup')) {
            // Fetch popup content
            fetch('chatbot-ai/chatbot-popup.html')
                .then(response => response.text())
                .then(html => {
                    const div = document.createElement('div');
                    div.innerHTML = html;
                    document.body.appendChild(div);

                    // Initialize scripts
                    initScripts();
                })
                .catch(err => {
                    console.error('Failed to load chatbot:', err);
                    createInlineChatbot();
                });
        }
    }

    // Create inline chatbot (fallback)
    function createInlineChatbot() {
        const popup = document.createElement('div');
        popup.id = 'chatbotPopup';
        popup.className = 'chatbot-popup';
        popup.innerHTML = `
            <div class="chatbot-header">
                <div class="chatbot-header-left">
                    <div class="chatbot-avatar">
                        <img src="images/logo_chatbot.png" alt="AI">
                    </div>
                    <div class="chatbot-title">
                        <h3>AI Assistant</h3>
                        <span class="chatbot-status online">Online</span>
                    </div>
                </div>
                <div class="chatbot-header-right">
                    <button class="chatbot-minimize" id="chatbotMinimize" title="Thu nhỏ">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                        </svg>
                    </button>
                    <button class="chatbot-close" id="chatbotClose" title="Đóng">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                    </button>
                </div>
            </div>
            <div class="chatbot-messages" id="chatbotMessages">
                <div class="chatbot-message bot">
                    <div class="chatbot-avatar bot-avatar">
                        <img src="images/logo_chatbot.png" alt="AI">
                    </div>
                    <div class="chatbot-message-content">
                        <div class="chatbot-bubble bot-bubble">
                            <p>Xin chào! Tôi là AI Assistant. Hãy hỏi tôi về dự án của bạn!</p>
                        </div>
                        <span class="chatbot-time">Bây giờ</span>
                    </div>
                </div>
            </div>
            <div class="chatbot-quick-questions" id="chatbotQuickQuestions">
                <button class="quick-question" data-question="Hệ thống có những chức năng chính nào?">🔍 Chức năng chính</button>
                <button class="quick-question" data-question="Làm thế nào để tạo công việc mới?">📝 Tạo công việc</button>
            </div>
            <div class="chatbot-input-area">
                <div class="chatbot-input-container">
                    <textarea id="chatbotInput" class="chatbot-input" placeholder="Đặt câu hỏi..." rows="1"></textarea>
                    <button class="chatbot-send" id="chatbotSend" disabled>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                    </button>
                </div>
            </div>
        `;

        // Create minimized button
        const minimizedBtn = document.createElement('div');
        minimizedBtn.id = 'chatbotMinimizedBtn';
        minimizedBtn.className = 'chatbot-minimized-btn';
        minimizedBtn.style.display = 'none';
        minimizedBtn.innerHTML = `
            <img src="images/logo_chatbot.png" alt="AI">
            <span class="chatbot-notification"></span>
        `;

        document.body.appendChild(popup);
        document.body.appendChild(minimizedBtn);

        // Initialize scripts
        initScripts();
    }

    // Initialize scripts - ĐÚNG THỨ TỰ: API trước, Main sau
    function initScripts() {
        // Load API Client trước (cần thiết để gọi backend)
        const apiScript = document.createElement('script');
        apiScript.src = 'chatbot-ai/chatbot-api.js';
        apiScript.onload = function() {
            console.log('Chatbot API loaded!');

            // Sau đó load Main script (phụ thuộc vào API)
            const mainScript = document.createElement('script');
            mainScript.src = 'chatbot-ai/chatbot.js';
            mainScript.onload = function() {
                console.log('Chatbot AI initialized successfully!');
            };
            document.body.appendChild(mainScript);
        };
        document.body.appendChild(apiScript);
    }

    // Make chatbot API available globally
    window.ChatbotAI = {
        open: function() {
            const toggle = document.getElementById('chatbotToggle');
            if (toggle) toggle.click();
        },
        close: function() {
            const close = document.getElementById('chatbotClose');
            if (close) close.click();
        },
        ask: function(question) {
            const input = document.getElementById('chatbotInput');
            const send = document.getElementById('chatbotSend');
            if (input && send) {
                this.open();
                setTimeout(() => {
                    input.value = question;
                    send.click();
                }, 300);
            }
        }
    };

    // Initialize
    function init() {
        injectStyles();
        createChatbotElements();
    }

    // Auto-initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
