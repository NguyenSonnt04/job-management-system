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

    async function showChatbot() {
        const wrapper = document.getElementById('chatbotWrapper');
        if (wrapper) {
            wrapper.style.opacity = '1';
            wrapper.style.visibility = 'visible';
            wrapper.style.transform = 'translateY(0)';

            // Gửi userId hiện tại đến popup iframe
            const userId = await getUserId();
            const iframe = document.getElementById('chatbotIframe');
            if (iframe && iframe.contentWindow) {
                iframe.contentWindow.postMessage({ action: 'setUserId', userId: userId }, '*');
                console.log('Parent: Sent userId to popup:', userId);
            }
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

        // Gửi userId hiện tại vào iframe
        const userId = await getUserId();
        console.log('Parent: showHistory - userId:', userId);
        const iframe = document.getElementById('historyIframe');
        if (iframe && iframe.contentWindow) {
            iframe.contentWindow.postMessage({ action: 'setUserId', userId: userId }, '*');
            console.log('Parent: Sent userId to history iframe:', userId);
        }

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

    // Get user ID - luôn kiểm tra với API để đảm bảo đúng user hiện tại
    async function getUserId(forceRefresh = false) {
        try {
            const response = await fetch('/api/user/me', { credentials: 'include' });
            const data = await response.json();
            const apiUserId = data.authenticated && data.id ? 'user_' + data.id : null;

            // Nếu API trả về user ID khác với localStorage → update
            const localUserId = localStorage.getItem('chatbot_user_id');

            if (apiUserId) {
                // Đã đăng nhập - dùng API user ID
                if (localUserId !== apiUserId) {
                    console.log('Parent: User changed from', localUserId, 'to', apiUserId);
                    localStorage.setItem('chatbot_user_id', apiUserId);
                    // Reset cache và thông báo cho iframes
                    notifyUserChanged(apiUserId);
                }
                return apiUserId;
            } else {
                // Chưa đăng nhập (logout) → tạo guest ID mới
                // Nếu localUserId là user_xxx (đã login trước đó) → logout detected!
                if (localUserId && localUserId.startsWith('user_')) {
                    console.log('Parent: Logout detected! Creating new guest session...');
                    const guestId = 'guest_' + Date.now();
                    localStorage.setItem('chatbot_user_id', guestId);
                    notifyUserChanged(guestId);
                    return guestId;
                }
                // Vẫn là guest, dùng existing hoặc tạo mới nếu forceRefresh
                if (!localUserId || forceRefresh) {
                    const guestId = 'guest_' + Date.now();
                    localStorage.setItem('chatbot_user_id', guestId);
                    return guestId;
                }
                return localUserId;
            }
        } catch (e) {
            console.error('Parent: Error getting user ID:', e);
            // Fallback - dùng guest ID
            let userId = localStorage.getItem('chatbot_user_id');
            if (!userId) {
                userId = 'guest_' + Date.now();
                localStorage.setItem('chatbot_user_id', userId);
            }
            return userId;
        }
    }

    // Thông báo cho các iframe rằng user đã thay đổi
    function notifyUserChanged(newUserId) {
        console.log('Parent: Notifying iframes of user change:', newUserId);

        // Thông báo popup iframe
        const chatbotIframe = document.getElementById('chatbotIframe');
        if (chatbotIframe && chatbotIframe.contentWindow) {
            chatbotIframe.contentWindow.postMessage({
                action: 'setUserId',
                userId: newUserId
            }, '*');
            // Clear chat
            chatbotIframe.contentWindow.postMessage({ action: 'clearChat' }, '*');
        }

        // Thông báo history iframe
        const historyIframe = document.getElementById('historyIframe');
        if (historyIframe && historyIframe.contentWindow) {
            historyIframe.contentWindow.postMessage({
                action: 'setUserId',
                userId: newUserId
            }, '*');
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
        newSession: async function() {
            // Xóa user ID hiện tại trong localStorage
            localStorage.removeItem('chatbot_user_id');
            console.log('New session started - userId cleared!');

            // Lấy userId mới
            const newUserId = await getUserId();
            console.log('New userId:', newUserId);

            // Reset currentUserId và gửi đến popup iframe
            const chatbotIframe = document.getElementById('chatbotIframe');
            if (chatbotIframe && chatbotIframe.contentWindow) {
                chatbotIframe.contentWindow.postMessage({
                    action: 'setUserId',
                    userId: newUserId
                }, '*');
                // Clear chat trong popup
                chatbotIframe.contentWindow.postMessage({ action: 'clearChat' }, '*');
            }

            // Gửi userId mới đến history iframe và reload
            const historyIframe = document.getElementById('historyIframe');
            if (historyIframe && historyIframe.contentWindow) {
                historyIframe.contentWindow.postMessage({
                    action: 'setUserId',
                    userId: newUserId
                }, '*');
            }
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
    window.addEventListener('message', async (event) => {
        const { action, userId, question, answer } = event.data;

        if (action === 'closeChatbot') {
            hideChatbot();
        }
        if (action === 'closeHistory') {
            hideHistory();
        }

        // Lưu history vào localStorage của parent (nơi duy nhất chia sẻ)
        if (action === 'saveHistory') {
            try {
                const historyKey = 'chatbot_history_' + userId;
                const history = JSON.parse(localStorage.getItem(historyKey) || '[]');
                history.push({
                    question: question,
                    answer: answer,
                    timestamp: Date.now()
                });
                // Giữ tối đa 50 tin nhắn
                if (history.length > 50) {
                    history.splice(0, history.length - 50);
                }
                localStorage.setItem(historyKey, JSON.stringify(history));
            } catch (e) {
                console.error('Error saving chat history:', e);
            }
        }

        // Load history từ localStorage của parent
        if (action === 'loadHistory') {
            try {
                const historyKey = 'chatbot_history_' + userId;
                const history = JSON.parse(localStorage.getItem(historyKey) || '[]');
                // Gửi lại history cho iframe
                const historyIframe = document.getElementById('historyIframe');
                if (historyIframe && historyIframe.contentWindow) {
                    historyIframe.contentWindow.postMessage({
                        action: 'historyData',
                        history: history
                    }, '*');
                }
            } catch (e) {
                console.error('Error loading chat history:', e);
            }
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
