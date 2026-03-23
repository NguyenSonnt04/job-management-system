/**
 * Chatbot AI - Include Script
 * Chỉ cần thêm <script src="chatbot-ai/chatbot-include.js"></script> vào trang
 * Tự động load chatbot popup từ file HTML riêng
 */

(function() {
    'use strict';

    const CONFIG = {
        popupUrl: 'chatbot-ai/chatbot-popup.html',
        historyUrl: 'chatbot-ai/chatbot-history.html'
    };

    let isPopupLoaded = false;

    // Tạo chatbot wrapper với iframe
    function createChatbotWrapper() {
        if (document.getElementById('chatbotWrapper')) {
            return document.getElementById('chatbotWrapper');
        }

        const wrapper = document.createElement('div');
        wrapper.id = 'chatbotWrapper';
        wrapper.style.cssText = `
            position: fixed;
            bottom: 100px;
            right: 30px;
            width: 400px;
            height: 600px;
            max-height: calc(100vh - 120px);
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            z-index: 10000;
            opacity: 0;
            visibility: hidden;
            transform: translateY(20px);
            transition: all 0.3s ease;
            overflow: hidden;
        `;

        const iframe = document.createElement('iframe');
        iframe.src = CONFIG.popupUrl;
        iframe.style.cssText = 'width:100%;height:100%;border:none;';
        iframe.id = 'chatbotIframe';

        wrapper.appendChild(iframe);
        document.body.appendChild(wrapper);

        // Load xong thì hiện popup
        iframe.onload = function() {
            setTimeout(() => showChatbot(), 200);
        };

        return wrapper;
    }

    // Tạo history wrapper với iframe load file HTML riêng
    function createHistoryWrapper() {
        if (document.getElementById('historyWrapper')) {
            return document.getElementById('historyWrapper');
        }

        const wrapper = document.createElement('div');
        wrapper.id = 'historyWrapper';
        // Vị trí: bên trái chatbot popup (right: 30px + 400px + 10px gap)
        wrapper.style.cssText = `
            position: fixed;
            bottom: 100px;
            right: 440px;
            width: 400px;
            height: 600px;
            max-height: calc(100vh - 120px);
            z-index: 10001;
            opacity: 0;
            visibility: hidden;
            transform: translateX(20px);
            transition: all 0.3s ease;
            display: block;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
            overflow: hidden;
        `;

        const iframe = document.createElement('iframe');
        iframe.src = CONFIG.historyUrl;
        iframe.style.cssText = 'width:100%;height:100%;border:none;';
        iframe.id = 'historyIframe';

        wrapper.appendChild(iframe);
        document.body.appendChild(wrapper);

        return wrapper;
    }

    function showChatbot() {
        const wrapper = document.getElementById('chatbotWrapper');
        if (wrapper) {
            wrapper.style.opacity = '1';
            wrapper.style.visibility = 'visible';
            wrapper.style.transform = 'translateY(0)';
        }
    }

    function hideChatbot() {
        const wrapper = document.getElementById('chatbotWrapper');
        if (wrapper) {
            wrapper.style.opacity = '0';
            wrapper.style.visibility = 'hidden';
            wrapper.style.transform = 'translateY(20px)';
        }
    }

    async function showHistory() {
        let wrapper = document.getElementById('historyWrapper');
        if (!wrapper) {
            wrapper = createHistoryWrapper();
        }

        wrapper.style.opacity = '1';
        wrapper.style.visibility = 'visible';
        wrapper.style.transform = 'translateX(0)';

        // Add click outside to close
        setTimeout(() => {
            document.addEventListener('click', handleHistoryClickOutside);
        }, 100);
    }

    function handleHistoryClickOutside(e) {
        const wrapper = document.getElementById('historyWrapper');
        if (wrapper && !wrapper.contains(e.target)) {
            const chatbotWrapper = document.getElementById('chatbotWrapper');
            if (!chatbotWrapper || !chatbotWrapper.contains(e.target)) {
                hideHistory();
                document.removeEventListener('click', handleHistoryClickOutside);
            }
        }
    }

    function hideHistory() {
        const wrapper = document.getElementById('historyWrapper');
        if (wrapper) {
            wrapper.style.opacity = '0';
            wrapper.style.visibility = 'hidden';
            wrapper.style.transform = 'translateX(20px)';
        }
    }

    // Get user ID từ localStorage hoặc tạo mới
    async function getUserId() {
        let userId = localStorage.getItem('chatbot_user_id');
        if (!userId) {
            try {
                const response = await fetch('/api/user/me', { credentials: 'include' });
                const data = await response.json();
                userId = data.authenticated && data.id ? 'user_' + data.id : 'guest_' + Date.now();
            } catch {
                userId = 'guest_' + Date.now();
            }
            localStorage.setItem('chatbot_user_id', userId);
        }
        return userId;
    }

    async function clearHistoryData() {
        if (confirm('Xóa toàn bộ lịch sử chat của user này?')) {
            const userId = await getUserId();
            if (userId) {
                const historyKey = 'chatbot_history_' + userId;
                localStorage.removeItem(historyKey);
                // Reload iframe để cập nhật UI
                const iframe = document.getElementById('historyIframe');
                if (iframe) iframe.src = iframe.src;
            }
        }
    }

    // Export API global
    window.ChatbotInclude = {
        toggle: function() {
            if (!isPopupLoaded) {
                createChatbotWrapper();
                isPopupLoaded = true;
            } else {
                const wrapper = document.getElementById('chatbotWrapper');
                if (wrapper.style.visibility === 'visible') {
                    hideChatbot();
                } else {
                    showChatbot();
                }
            }
        },
        openHistory: async function() {
            await showHistory();
        },
        closeHistory: function() {
            hideHistory();
        },
        clearHistory: async function() {
            await clearHistoryData();
        },
        newSession: function() {
            localStorage.removeItem('chatbot_user_id');
            console.log('Refreshing user session...');
            location.reload();
        },
        getCurrentSession: async function() {
            return await getUserId();
        }
    };

    // Override nút button trong index.html
    window.toggleChatbotPopup = function() {
        window.ChatbotInclude.toggle();
    };

    // Open history (gọi từ popup chatbot)
    window.openChatHistory = function() {
        window.ChatbotInclude.openHistory();
    };

    window.closeChatHistory = function() {
        window.ChatbotInclude.closeHistory();
    };

    // Listen for messages from iframe
    window.addEventListener('message', (event) => {
        if (event.data.action === 'closeChatbot') {
            hideChatbot();
        }
        if (event.data.action === 'closeHistory') {
            hideHistory();
        }
    });

    // ESC để đóng
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            hideChatbot();
            hideHistory();
        }
    });

    console.log('Chatbot Include loaded! ✨');
})();
