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
    let currentUserId = null;

    // Get user ID from backend
    async function getUserId() {
        if (currentUserId) return currentUserId;

        try {
            const response = await fetch('/api/user/me', { credentials: 'include' });
            const data = await response.json();

            if (data.authenticated && data.id) {
                // Đã đăng nhập - dùng user ID
                currentUserId = 'user_' + data.id;
                localStorage.setItem('chatbot_user_id', currentUserId);
                return currentUserId;
            } else {
                // Chưa đăng nhập - xóa old user ID nếu có, tạo guest mới
                localStorage.removeItem('chatbot_user_id');
                currentUserId = 'guest_' + Date.now();
                localStorage.setItem('chatbot_user_id', currentUserId);
                return currentUserId;
            }
        } catch (e) {
            console.error('Error getting user ID:', e);
            // Lỗi - tạo guest session mới
            localStorage.removeItem('chatbot_user_id');
            currentUserId = 'guest_' + Date.now();
            localStorage.setItem('chatbot_user_id', currentUserId);
            return currentUserId;
        }
    }

    // Tạo chatbot wrapper
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

    // Tạo history container
    function createHistoryWrapper() {
        if (document.getElementById('historyWrapper')) {
            return document.getElementById('historyWrapper');
        }

        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from { opacity: 0; transform: translateX(20px); }
                to { opacity: 1; transform: translateX(0); }
            }
            @keyframes fadeOut {
                0% { opacity: 1; }
                100% { opacity: 0; }
            }
            .history-container {
                background: white;
                border-radius: 16px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
                width: 100%;
                max-height: 70vh;
                overflow: hidden;
                display: flex;
                flex-direction: column;
                animation: slideIn 0.3s ease;
            }
            .history-header {
                background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
                color: white;
                padding: 15px 20px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-radius: 16px 16px 0 0;
            }
            .history-header h2 {
                margin: 0;
                font-size: 16px;
                font-weight: 600;
            }
            .history-close {
                background: rgba(255, 255, 255, 0.2);
                border: none;
                color: white;
                width: 28px;
                height: 28px;
                border-radius: 50%;
                cursor: pointer;
                font-size: 16px;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s ease;
            }
            .history-close:hover {
                background: rgba(255, 255, 255, 0.3);
            }
            .history-list {
                max-height: 400px;
                overflow-y: auto;
                padding: 10px;
            }
            .history-list::-webkit-scrollbar {
                width: 6px;
            }
            .history-list::-webkit-scrollbar-thumb {
                background: #e2e8f0;
                border-radius: 3px;
            }
            .history-item {
                padding: 12px;
                border-bottom: 1px solid #e2e8f0;
                cursor: pointer;
                transition: background 0.2s ease;
            }
            .history-item:hover {
                background: #f8fafc;
            }
            .history-question {
                font-weight: 600;
                color: #1e293b;
                margin-bottom: 4px;
                font-size: 13px;
            }
            .history-answer {
                color: #64748b;
                font-size: 12px;
                white-space: pre-wrap;
                margin-bottom: 4px;
            }
            .history-time {
                font-size: 10px;
                color: #999;
            }
            .empty-state {
                text-align: center;
                color: #999;
                padding: 40px 20px;
            }
            .empty-state-icon {
                font-size: 48px;
                margin-bottom: 10px;
            }
            .history-actions {
                padding: 15px 20px;
                border-top: 1px solid #e2e8f0;
                display: flex;
                justify-content: flex-end;
                gap: 10px;
            }
            .btn {
                padding: 8px 16px;
                border: none;
                border-radius: 8px;
                font-size: 12px;
                cursor: pointer;
                transition: all 0.2s ease;
            }
            .btn-primary {
                background: #6366f1;
                color: white;
            }
            .btn-primary:hover {
                background: #4f46e5;
            }
            .btn-danger {
                background: #ef4444;
                color: white;
            }
            .btn-danger:hover {
                background: #dc2626;
            }
            .btn-secondary {
                background: #f8fafc;
                color: #1e293b;
                border: 1px solid #e2e8f0;
            }
            .btn-secondary:hover {
                background: #e2e8f0;
            }
        `;

        const wrapper = document.createElement('div');
        wrapper.id = 'historyWrapper';
        wrapper.style.cssText = `
            position: fixed;
            top: 100px;
            right: 30px;
            width: 400px;
            max-height: 500px;
            z-index: 10001;
            display: none;
        `;

        wrapper.innerHTML = `
            <div class="history-container">
                <div class="history-header">
                    <h2>📜 Lịch sử Chat</h2>
                    <div style="display: flex; gap: 5px;">
                        <small id="currentSessionDisplay" style="opacity: 0.8; font-size: 10px; align-self: center;"></small>
                        <button class="history-close" onclick="window.ChatbotInclude.closeHistory()">✕</button>
                    </div>
                </div>
                <div class="history-list" id="historyList"></div>
                <div class="history-actions">
                    <button class="btn btn-secondary" onclick="showCurrentSession()">🔍 Session ID</button>
                    <button class="btn btn-primary" onclick="window.ChatbotInclude.newSession()">🔄 New Session</button>
                    <button class="btn btn-secondary" onclick="window.ChatbotInclude.exportHistory()">📥 Export</button>
                    <button class="btn btn-danger" onclick="window.ChatbotInclude.clearHistory()">🗑️ Xóa</button>
                </div>
            </div>
        `;

        document.head.appendChild(style);
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
        const wrapper = document.getElementById('historyWrapper');
        if (!wrapper) {
            createHistoryWrapper();
        }

        wrapper.style.display = 'block';
        await loadHistoryData();

        // Display current user ID
        const userId = await getUserId();
        const sessionDisplay = document.getElementById('currentSessionDisplay');
        if (sessionDisplay && userId) {
            sessionDisplay.textContent = userId.substring(0, 20) + '...';
        }

        // Add click outside to close
        setTimeout(() => {
            document.addEventListener('click', handleHistoryClickOutside);
        }, 100);
    }

    async function showCurrentSession() {
        const userId = await getUserId();
        alert('Current User ID:\n' + userId);
    }

    function handleHistoryClickOutside(e) {
        const wrapper = document.getElementById('historyWrapper');
        if (wrapper && !wrapper.contains(e.target)) {
            // Check if click is not on the chatbot either
            const chatbotWrapper = document.getElementById('chatbotWrapper');
            if (!chatbotWrapper || !chatbotWrapper.contains(e.target)) {
                hideHistory();
                document.removeEventListener('click', handleHistoryClickOutside);
            }
        }
    }

    async function loadHistoryData() {
        const list = document.getElementById('historyList');
        if (!list) return;

        const userId = await getUserId();
        const history = getHistory(userId);

        if (history.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">💬</div>
                    <p>Chưa có lịch sử chat</p>
                    <small>Bắt đầu chat với AI Assistant ngay!</small>
                </div>
            `;
        } else {
            list.innerHTML = '';
            history.slice().reverse().forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'history-item';
                itemDiv.innerHTML = `
                    <div class="history-question">Q: ${escapeHtml(item.question)}</div>
                    <div class="history-answer">A: ${escapeHtml(item.answer)}</div>
                    <div class="history-time">${new Date(item.timestamp).toLocaleString('vi-VN')}</div>
                `;
                itemDiv.onclick = () => copyToClipboard(item.answer);
                list.appendChild(itemDiv);
            });
        }
    }

    function getHistory(userId) {
        try {
            const historyKey = 'chatbot_history_' + userId;
            const history = localStorage.getItem(historyKey);
            return history ? JSON.parse(history) : [];
        } catch (e) {
            console.error('Error loading chat history:', e);
            return [];
        }
    }

    async function clearHistoryData() {
        if (confirm('Xóa toàn bộ lịch sử chat của user này?')) {
            const userId = await getUserId();
            if (userId) {
                const historyKey = 'chatbot_history_' + userId;
                localStorage.removeItem(historyKey);
                await loadHistoryData();
            }
        }
    }

    async function exportHistoryData() {
        const userId = await getUserId();
        const history = getHistory(userId);
        if (history.length === 0) {
            alert('Không có lịch sử để export!');
            return;
        }

        let text = 'Lịch sử Chat - AI Assistant\n';
        text += `User: ${userId}\n`;
        text += '='.repeat(40) + '\n\n';

        history.forEach(item => {
            text += `Q: ${item.question}\n`;
            text += `A: ${item.answer}\n`;
            text += `Thời gian: ${new Date(item.timestamp).toLocaleString('vi-VN')}\n`;
            text += '-'.repeat(40) + '\n\n';
        });

        const blob = new Blob([text], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `chat-history-${userId ? userId.substring(5, 15) : Date.now()}.txt`;
        a.click();
        URL.revokeObjectURL(url);
    }

    function copyToClipboard(text) {
        navigator.clipboard.writeText(text).then(() => {
            const tooltip = document.createElement('div');
            tooltip.textContent = '✓ Đã copy!';
            tooltip.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: #10b981;
                color: white;
                padding: 8px 16px;
                border-radius: 8px;
                font-size: 12px;
                z-index: 10002;
                animation: fadeOut 2s forwards;
            `;
            document.body.appendChild(tooltip);
            setTimeout(() => tooltip.remove(), 2000);
        });
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function hideHistory() {
        const wrapper = document.getElementById('historyWrapper');
        if (wrapper) {
            wrapper.style.display = 'none';
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
        exportHistory: async function() {
            await exportHistoryData();
        },
        newSession: function() {
            // Xóa cache user ID và reload để lấy user mới từ API
            localStorage.removeItem('chatbot_user_id');
            currentUserId = null;
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
        // Verify origin for security (optional, for development allow all)
        if (event.data.action === 'closeChatbot') {
            hideChatbot();
        }
    });

    // ESC để đóng
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            hideChatbot();
            hideHistory();
        }
    });

    // Cleanup old history data (chạy 1 lần)
    (async function cleanupOldHistory() {
        try {
            // Xóa key cũ không có userId
            const oldKeys = ['chatbot_history', 'chatbot_history_all', 'chatbot_session_id'];
            oldKeys.forEach(key => {
                if (localStorage.getItem(key)) {
                    console.log('Removing old key:', key);
                    localStorage.removeItem(key);
                }
            });

            // Log current user info
            const userId = await getUserId();
            console.log('Current user:', userId);
            console.log('History keys:', Object.keys(localStorage).filter(k => k.includes('chatbot_history')));
        } catch (e) {
            console.error('Cleanup error:', e);
        }
    })();

    console.log('Chatbot Include loaded! ✨');
})();
