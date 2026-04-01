/**
 * Contact Widget - Premium Version
 * Floating widget with AI chatbot integration
 * Enhanced UI with smooth animations
 */

(function() {
    'use strict';

    // ===== Configuration =====
    const CONFIG = {
        phone: '0918846349',    
        zalo: '0918846349',         
        apiBase: '/api/contact-chat',
        localStorageKey: 'chatbot_session_id',
        hoverDelay: 200 // ms before showing sub-buttons on hover
    };

    // ===== State Management =====
    let state = {
        isOpen: false,
        isClickedOpen: false, // Track if opened by click
        sessionId: null,
        isChatbotOpen: false,
        hoverTimer: null
    };

    // ===== Initialize Widget =====
    function initWidget() {
        // Load existing session ID from localStorage
        const savedSessionId = localStorage.getItem(CONFIG.localStorageKey);
        if (savedSessionId) {
            state.sessionId = savedSessionId;
        }

        // Create widget HTML
        createWidgetHTML();

        // Attach event listeners
        attachEventListeners();

        console.log('Contact Widget Premium initialized');
    }

    // ===== Create Widget HTML =====
    function createWidgetHTML() {
        const widgetHTML = `
            <div class="contact-widget">
                <button class="contact-widget__main-btn" aria-label="Liên hệ">
                    <span class="icon-message">💬</span>
                    <div class="ring-indicator"></div>
                </button>

                <div class="contact-widget__sub-buttons">
                    <a href="tel:${CONFIG.phone}"
                       class="contact-widget__btn phone-btn"
                       aria-label="Điện thoại">
                        <svg class="icon-phone" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M20.01 15.38c-1.23 0-2.42-.2-3.53-.56a.977.977 0 00-1.01.24l-1.57 1.97c-2.83-1.35-5.48-3.9-6.89-6.83l1.95-1.66c.27-.28.35-.67.24-1.02-.37-1.11-.56-2.3-.56-3.53 0-.54-.45-.99-.99-.99H4.19C3.65 3 3 3.24 3 3.99 3 13.28 10.73 21 20.01 21c.71 0 .99-.63.99-1.18v-3.45c0-.54-.45-.99-.99-.99z"/>
                        </svg>
                    </a>

                    <button class="contact-widget__btn chatbot-btn"
                            aria-label="Chatbot AI">
                        <svg class="icon-ai" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                        </svg>
                    </button>

                    <a href="https://zalo.me/${CONFIG.zalo}"
                       target="_blank"
                       rel="noopener"
                       class="contact-widget__btn zalo-btn"
                       aria-label="Zalo">
                        <svg class="icon-zalo" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <rect x="1" y="4" width="22" height="16" rx="3" fill="#0068FF"/>
                            <text x="12" y="15" text-anchor="middle" font-size="7" font-weight="bold" fill="currentColor">Zalo</text>
                        </svg>
                    </a>
                </div>
            </div>
        `;

        // Insert widget at the end of body
        document.body.insertAdjacentHTML('beforeend', widgetHTML);
    }

    // ===== Attach Event Listeners =====
    function attachEventListeners() {
        const mainBtn = document.querySelector('.contact-widget__main-btn');
        const subButtons = document.querySelector('.contact-widget__sub-buttons');
        const chatbotBtn = document.querySelector('.chatbot-btn');

        // Click to toggle widget (keep open when clicked)
        mainBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            // Clear any pending hover timers
            clearTimeout(state.hoverTimer);
            toggleWidgetClick();
        });

        // Hover to show sub-buttons (only if not clicked open)
        mainBtn.addEventListener('mouseenter', function() {
            // Don't auto-open if user clicked to open
            if (state.isClickedOpen) return;

            clearTimeout(state.hoverTimer);
            state.hoverTimer = setTimeout(() => {
                if (!state.isOpen) {
                    openWidget();
                }
            }, CONFIG.hoverDelay);
        });

        // Mouse leave main button - only close if NOT clicked open
        mainBtn.addEventListener('mouseleave', function() {
            // Don't auto-close if user clicked to open
            if (state.isClickedOpen) return;

            clearTimeout(state.hoverTimer);
            state.hoverTimer = setTimeout(() => {
                if (!isMouseOverSubButtons() && !state.isClickedOpen) {
                    closeWidget();
                }
            }, 100);
        });

        // Keep open when hovering over sub-buttons (only for hover mode)
        subButtons.addEventListener('mouseenter', function() {
            if (state.isClickedOpen) return;
            clearTimeout(state.hoverTimer);
        });

        // Close when leaving sub-buttons (only for hover mode)
        subButtons.addEventListener('mouseleave', function() {
            if (state.isClickedOpen) return;

            clearTimeout(state.hoverTimer);
            state.hoverTimer = setTimeout(() => {
                if (!state.isClickedOpen) {
                    closeWidget();
                }
            }, 100);
        });

        // Close widget when clicking outside
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.contact-widget')) {
                closeWidget();
                state.isClickedOpen = false; // Reset click state
            }
        });

        // Close widget when pressing Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && state.isOpen) {
                closeWidget();
                state.isClickedOpen = false; // Reset click state
            }
        });

        // Open chatbot modal - FIXED VERSION
        if (chatbotBtn) {
            // Remove existing event listener if any (prevent double attachment)
            const newChatbotBtn = chatbotBtn.cloneNode(true);
            chatbotBtn.parentNode.replaceChild(newChatbotBtn, chatbotBtn);

            newChatbotBtn.addEventListener('click', function(e) {
                console.log('🤖 Chatbot button clicked!'); // Debug log

                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation(); // Prevent other handlers

                // Force close widget and reset ALL states
                const widgetMainBtn = document.querySelector('.contact-widget__main-btn');
                const widgetSubButtons = document.querySelector('.contact-widget__sub-buttons');

                if (widgetMainBtn) widgetMainBtn.classList.remove('active');
                if (widgetSubButtons) widgetSubButtons.classList.remove('active');

                state.isOpen = false;
                state.isClickedOpen = false;
                clearTimeout(state.hoverTimer);

                console.log('✅ Widget closed, opening modal...'); // Debug log

                // Open modal immediately
                openChatbotModal();
            });
        }
    }

    // ===== Helper: Check if mouse is over sub-buttons =====
    function isMouseOverSubButtons() {
        const subButtons = document.querySelector('.contact-widget__sub-buttons');
        if (!subButtons) return false;

        return subButtons.matches(':hover');
    }

    // ===== Toggle Widget (Click) =====
    function toggleWidgetClick() {
        const mainBtn = document.querySelector('.contact-widget__main-btn');
        const subButtons = document.querySelector('.contact-widget__sub-buttons');

        if (state.isOpen && state.isClickedOpen) {
            // Widget is open from click - close it
            state.isOpen = false;
            state.isClickedOpen = false;
            closeWidget();
        } else if (state.isOpen && !state.isClickedOpen) {
            // Widget is open from hover - just mark as clicked open (don't close)
            state.isClickedOpen = true;
        } else {
            // Widget is closed - open it
            state.isOpen = true;
            state.isClickedOpen = true;
            mainBtn.classList.add('active');
            subButtons.classList.add('active');

            // Hide ring indicator when opened
            const ringIndicator = mainBtn.querySelector('.ring-indicator');
            if (ringIndicator) {
                ringIndicator.style.display = 'none';
            }
        }
    }

    // ===== Open Widget (Hover) =====
    function openWidget() {
        state.isOpen = true;

        const mainBtn = document.querySelector('.contact-widget__main-btn');
        const subButtons = document.querySelector('.contact-widget__sub-buttons');

        mainBtn.classList.add('active');
        subButtons.classList.add('active');

        // Hide ring indicator when opened
        const ringIndicator = mainBtn.querySelector('.ring-indicator');
        if (ringIndicator) {
            ringIndicator.style.display = 'none';
        }
    }

    // ===== Close Widget =====
    function closeWidget() {
        state.isOpen = false;
        state.isClickedOpen = false; // Always reset click state

        const mainBtn = document.querySelector('.contact-widget__main-btn');
        const subButtons = document.querySelector('.contact-widget__sub-buttons');

        if (mainBtn) mainBtn.classList.remove('active');
        if (subButtons) subButtons.classList.remove('active');

        // Clear hover timer
        clearTimeout(state.hoverTimer);
    }

    // ===== Open Chatbot Modal =====
    function openChatbotModal() {
        console.log('🔓 openChatbotModal called'); // Debug log

        // Create and show chatbot modal immediately
        createChatbotModal();

        console.log('✅ Modal created'); // Debug log

        // Load chat history immediately after modal is rendered
        // Sử dụng requestAnimationFrame để đảm bảo DOM đã được cập nhật
        requestAnimationFrame(() => {
            console.log('📝 Loading chat history...'); // Debug log
            loadChatHistory();
        });
    }

    // ===== Create Chatbot Modal =====
    function createChatbotModal() {
        console.log('📱 createChatbotModal called');

        // Check if modal already exists
        const existingBackdrop = document.querySelector('.chatbot-modal-backdrop');
        if (existingBackdrop) {
            console.log('♻️ Modal already exists, removing old one...');
            existingBackdrop.remove();
        }

        console.log('🆕 Creating new modal...');

        const modalHTML = `
            <div class="chatbot-modal-backdrop">
                <div class="chatbot-modal">
                    <div class="chatbot-modal__header">
                        <div class="chatbot-modal__header-info">
                            <div class="chatbot-modal__avatar">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                </svg>
                            </div>
                            <div class="chatbot-modal__header-text">
                                <h3>Trợ lý AI JCO</h3>
                                <div class="chatbot-modal__status-info">
                                    <span class="chatbot-modal__status-dot"></span>
                                    <span class="chatbot-modal__status-text">Online</span>
                                </div>
                            </div>
                        </div>
                        <button class="chatbot-modal__close" aria-label="Đóng">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    </div>

                    <div class="chatbot-modal__messages">
                        <div class="chatbot-modal__loading">
                            <div class="spinner"></div>
                            <p>Đang tải...</p>
                        </div>
                    </div>

                    <div class="chatbot-modal__input">
                        <div class="chatbot-modal__input-wrapper">
                            <textarea
                                class="chatbot-modal__textarea"
                                placeholder="Nhập câu hỏi của bạn..."
                                rows="1"></textarea>
                            <button class="chatbot-modal__send" disabled>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <line x1="22" y1="2" x2="11" y2="13"></line>
                                    <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                                </svg>
                            </button>
                        </div>
                        <div class="chatbot-modal__hint">Nhấn Enter để gửi, Shift + Enter để xuống dòng</div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);

        console.log('✅ Modal HTML inserted into DOM');

        // CRITICAL: Wait for DOM to update before attaching event listeners
        // Use setTimeout with longer delay to ensure DOM is ready
        setTimeout(() => {
            console.log('🔄 Checking DOM...');

            // Debug: Check all elements
            const backdropCheck = document.querySelector('.chatbot-modal-backdrop');
            const modalCheck = document.querySelector('.chatbot-modal');
            const closeBtnCheck = document.querySelector('.chatbot-modal__close');
            const textareaCheck = document.querySelector('.chatbot-modal__textarea');
            const sendBtnCheck = document.querySelector('.chatbot-modal__send');

            console.log('Elements after insertion:');
            console.log('  backdrop:', !!backdropCheck, backdropCheck);
            console.log('  modal:', !!modalCheck, modalCheck);
            console.log('  closeBtn:', !!closeBtnCheck);
            console.log('  textarea:', !!textareaCheck);
            console.log('  sendBtn:', !!sendBtnCheck);

            if (!backdropCheck) {
                console.error('❌ Backdrop not found after insertion!');
                return;
            }

            // Attach modal event listeners AFTER DOM is ready
            const success = attachModalEventListeners();

            if (success) {
                // Add 'show' class to display modal
                const backdrop = document.querySelector('.chatbot-modal-backdrop');
                if (backdrop) {
                    backdrop.classList.add('show');
                    console.log('✅ Modal backdrop shown');
                    console.log('Backdrop classes:', backdrop.className);
                    console.log('Backdrop styles:', window.getComputedStyle(backdrop).display);
                } else {
                    console.error('❌ Backdrop not found!');
                }
            } else {
                console.error('❌ Failed to attach event listeners');
            }
        }, 100); // Tăng lên 100ms
    }

    // ===== Attach Modal Event Listeners =====
    function attachModalEventListeners() {
        console.log('🔍 attachModalEventListeners called');

        const backdrop = document.querySelector('.chatbot-modal-backdrop');
        const closeBtn = document.querySelector('.chatbot-modal__close');
        const textarea = document.querySelector('.chatbot-modal__textarea');
        const sendBtn = document.querySelector('.chatbot-modal__send');

        console.log('Elements found:');
        console.log('  - backdrop:', !!backdrop);
        console.log('  - closeBtn:', !!closeBtn);
        console.log('  - textarea:', !!textarea);
        console.log('  - sendBtn:', !!sendBtn);

        if (!backdrop || !closeBtn || !textarea || !sendBtn) {
            console.error('❌ Modal elements not found!');
            console.error('Missing:', {
                backdrop: !backdrop,
                closeBtn: !closeBtn,
                textarea: !textarea,
                sendBtn: !sendBtn
            });
            return false;
        }

        console.log('✅ All elements found, attaching events...');

        // Close modal
        closeBtn.addEventListener('click', closeChatbotModal);
        backdrop.addEventListener('click', function(e) {
            // Click vào backdrop (không phải modal) thì đóng
            if (e.target === backdrop) {
                console.log('🖱️ Clicked outside modal - closing...');
                closeChatbotModal();
            } else {
                console.log('🖱️ Clicked inside modal - keeping open');
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && backdrop.classList.contains('show')) {
                closeChatbotModal();
            }
        });

        // Textarea auto-resize
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 120) + 'px';

            // Enable/disable send button
            sendBtn.disabled = this.value.trim().length === 0;
        });

        // Send message on Enter (without Shift)
        textarea.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (this.value.trim().length > 0) {
                    sendMessage();
                }
            }
        });

        // Send button click
        sendBtn.addEventListener('click', sendMessage);

        console.log('✅ All event listeners attached successfully!');

        return true;
    }

    // ===== Close Chatbot Modal =====
    function closeChatbotModal() {
        const backdrop = document.querySelector('.chatbot-modal-backdrop');
        if (backdrop) {
            backdrop.classList.remove('show');
            setTimeout(() => {
                backdrop.remove();
            }, 300);
        }

        // KHÔNG xóa sessionId khỏi localStorage - giữ lại để lần sau còn dùng
    }

    // ===== Load Chat History =====
    async function loadChatHistory() {
        try {
            // Tạo hoặc lấy session cũ (truyền sessionId nếu có trong localStorage)
            const existingSessionId = localStorage.getItem(CONFIG.localStorageKey) || '';
            console.log('Loading chat history with sessionId:', existingSessionId);

            const response = await fetch(`${CONFIG.apiBase}/session?existingSessionId=${encodeURIComponent(existingSessionId)}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            console.log('Session API response status:', response.status);

            if (response.ok) {
                const data = await response.json();
                console.log('Session API response:', data);

                if (data.success && data.sessionId) {
                    // Lưu sessionId mới (hoặc cũ)
                    state.sessionId = data.sessionId;
                    localStorage.setItem(CONFIG.localStorageKey, data.sessionId);

                    console.log('Session ID saved, loading messages...');

                    // Load lịch sử chat
                    await loadMessagesFromDB();
                } else {
                    console.error('Session API returned success=false:', data);
                    showError('Không thể tạo session chat: ' + (data.error || 'Unknown error'));
                }
            } else if (response.status === 403) {
                // Forbidden - sessionId không thuộc về user hiện tại
                console.warn('⚠️ Session không thuộc về user hiện tại. Đang xóa và tạo mới...');

                // Xóa sessionId cũ và thử lại
                localStorage.removeItem(CONFIG.localStorageKey);
                state.sessionId = null;

                // Gọi lại để tạo session mới (recursive call nhưng không có existingSessionId)
                await loadChatHistory();
            } else {
                console.error('Session API failed with status:', response.status);
                const errorText = await response.text();
                console.error('Error response:', errorText);
                showError(`Lỗi kết nối server (${response.status}). Vui lòng thử lại.`);
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
            showError('Lỗi kết nối mạng: ' + error.message);
        }
    }

    // ===== Show Error Message =====
    function showError(message) {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');
        if (!messagesContainer) return;

        messagesContainer.innerHTML = `
            <div class="chatbot-modal__error">
                <div class="error-icon">⚠️</div>
                <p>${message}</p>
                <button class="retry-btn">Thử lại</button>
            </div>
        `;

        // Add click handler to retry button
        const retryBtn = messagesContainer.querySelector('.retry-btn');
        if (retryBtn) {
            retryBtn.addEventListener('click', loadChatHistory);
        }
    }

    // ===== Load Messages from Database =====
    async function loadMessagesFromDB() {
        if (!state.sessionId) {
            console.log('No sessionId to load messages');
            return;
        }

        const messagesContainer = document.querySelector('.chatbot-modal__messages');

        try {
            console.log('Fetching messages for sessionId:', state.sessionId);
            const response = await fetch(`${CONFIG.apiBase}/history/${state.sessionId}`, {
                credentials: 'include'
            });

            console.log('History API response status:', response.status);

            if (response.ok) {
                const data = await response.json();
                console.log('History API response:', data);

                if (data.success && data.messages && data.messages.length > 0) {
                    console.log('Found', data.messages.length, 'messages');
                    // Log each message to debug
                    data.messages.forEach((msg, i) => {
                        console.log(`Message ${i}:`, JSON.stringify(msg));
                    });
                    displayMessages(data.messages);

                    // Show notification if loading old chat
                    const nonSystemMessages = data.messages.filter(m => m.role && m.role !== 'SYSTEM');
                    if (nonSystemMessages.length > 2) {
                        showRestoreNotification(nonSystemMessages.length);
                    }
                } else {
                    console.log('No messages found or error:', data);
                    // Clear loading state if no messages
                    messagesContainer.innerHTML = '<p class="text-center text-muted">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</p>';
                }
            } else if (response.status === 403) {
                // Forbidden - sessionId không thuộc về user hiện tại
                console.warn('⚠️ Session không thuộc về user hiện tại. Đang xóa và tạo mới...');

                // Xóa sessionId cũ
                localStorage.removeItem(CONFIG.localStorageKey);
                state.sessionId = null;

                // Hiển thị thông báo
                showError('Phiên chat cũ không còn hợp lệ. Đang tạo phiên mới...');

                // Đợi 1 giây rồi reload lại
                setTimeout(() => {
                    loadChatHistory();
                }, 1000);
            } else {
                console.log('History API failed:', response.status);
                messagesContainer.innerHTML = '<p class="text-center text-danger">Không thể tải lịch sử chat.</p>';
            }
        } catch (error) {
            console.error('Error loading messages:', error);
            messagesContainer.innerHTML = '<p class="text-center text-danger">Lỗi kết nối. Vui lòng thử lại.</p>';
        }
    }

    // ===== Show Restore Notification =====
    function showRestoreNotification(messageCount) {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');

        const notificationDiv = document.createElement('div');
        notificationDiv.className = 'chatbot-modal__notification';
        notificationDiv.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">📜</span>
                <span class="notification-text">Đã khôi phục ${messageCount} tin nhắn từ phiên chat trước</span>
            </div>
        `;

        // Insert at the beginning
        messagesContainer.insertBefore(notificationDiv, messagesContainer.firstChild);

        // Auto-remove after 3 seconds
        setTimeout(() => {
            notificationDiv.classList.add('fade-out');
            setTimeout(() => notificationDiv.remove(), 300);
        }, 3000);
    }

    // ===== Create Session =====
    async function createSession() {
        try {
            const response = await fetch(`${CONFIG.apiBase}/session`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success && data.sessionId) {
                    state.sessionId = data.sessionId;
                    localStorage.setItem(CONFIG.localStorageKey, data.sessionId);
                }
            }
        } catch (error) {
            console.error('Error creating session:', error);
        }
    }

    // ===== Display Messages =====
    function displayMessages(messages) {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');
        if (!messagesContainer) {
            console.error('Messages container not found!');
            return;
        }
        messagesContainer.innerHTML = '';

        console.log('Displaying messages:', messages);

        messages.forEach((msg, index) => {
            console.log(`Message ${index}:`, msg);
            // Validate message object
            if (!msg || !msg.role) {
                console.error('Invalid message object:', msg);
                return;
            }
            if (!msg.content) {
                console.error('Message has no content:', msg);
                return;
            }
            addMessageToUI(msg.role, msg.content);
        });

        scrollToBottom();
    }

    // ===== Add Message to UI =====
    function addMessageToUI(role, content) {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');

        // Validate role
        if (!role || typeof role !== 'string') {
            console.error('Invalid role:', role);
            role = 'ASSISTANT'; // Default fallback
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `chatbot-modal__message chatbot-modal__message--${role.toLowerCase()}`;

        const avatar = role === 'USER'
            ? '<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>'
            : '<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>';

        // Check if system message with suggestions
        if (role === 'SYSTEM') {
            messageDiv.innerHTML = formatSystemMessage(content);
        } else {
            messageDiv.innerHTML = `
                <div class="chatbot-modal__message-avatar">${avatar}</div>
                <div class="chatbot-modal__message-content">
                    <div class="chatbot-modal__message-bubble">${formatMessage(content)}</div>
                    <div class="chatbot-modal__message-time">${getCurrentTime()}</div>
                </div>
            `;
        }

        messagesContainer.appendChild(messageDiv);

        // Add click handlers for suggestions
        if (role === 'SYSTEM') {
            attachSuggestionClickHandlers();
        }

        scrollToBottom();
    }

    // ===== Format Message (support markdown-like syntax) =====
    function formatMessage(text) {
        // 1. Escape toàn bộ text trước
        let formatted = escapeHtml(text);

        // 2. Convert markdown links [text](url) thành HTML links
        // Sau khi escape, cú pháp markdown vẫn giữ nguyên
        const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
        formatted = formatted.replace(linkRegex, '<a href="$2" target="_blank" rel="noopener noreferrer" style="color: #0066cc; text-decoration: underline; cursor: pointer;">$1</a>');

        // 3. Convert **bold**
        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        // 4. Convert line breaks
        formatted = formatted.replace(/\n/g, '<br>');

        return formatted;
    }

    // ===== Format System Message with Suggestions =====
    function formatSystemMessage(content) {
        // Chỉ hiển thị lời chào, bỏ qua các gợi ý
        const lines = content.split('\n');
        let formatted = '<div class="chatbot-modal__system-content">';

        for (let line of lines) {
            // Bỏ qua numbered suggestions: "1. 🔍 Cách tìm việc làm?"
            const match = line.match(/^(\d+)\.\s+(🔍|📝|📤|👤|🏢|💰|🎯|🎤|📞)?\s*(.+)$/);
            if (match) {
                // Skip numbered suggestions - không hiển thị
                continue;
            } else if (line.trim()) {
                // Chỉ hiển thị các dòng văn bản thường (lời chào)
                if (line.includes('**')) {
                    line = line.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
                }
                formatted += `<div class="system-line">${line}</div>`;
            }
        }

        formatted += '</div>';
        return formatted;
    }

    // ===== Send Message =====
    async function sendMessage() {
        const textarea = document.querySelector('.chatbot-modal__textarea');
        const sendBtn = document.querySelector('.chatbot-modal__send');
        const message = textarea.value.trim();

        if (!message || !state.sessionId) return;

        // Disable input while sending
        textarea.disabled = true;
        sendBtn.disabled = true;

        // Add user message to UI
        addMessageToUI('USER', message);

        // Show typing indicator
        showTypingIndicator();

        // Clear textarea
        textarea.value = '';
        textarea.style.height = 'auto';

        try {
            const response = await fetch(`${CONFIG.apiBase}/message`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    sessionId: state.sessionId,
                    message: message
                })
            });

            // Remove typing indicator
            hideTypingIndicator();

            if (response.ok) {
                const data = await response.json();
                if (data.success && data.message) {
                    addMessageToUI('ASSISTANT', data.message);
                } else {
                    addMessageToUI('SYSTEM', 'Xin lỗi, có lỗi xảy ra. Vui lòng thử lại.');
                }
            } else {
                addMessageToUI('SYSTEM', 'Không thể gửi tin nhắn. Vui lòng thử lại.');
            }
        } catch (error) {
            console.error('Error sending message:', error);
            hideTypingIndicator();
            addMessageToUI('SYSTEM', 'Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.');
        } finally {
            // Re-enable input
            textarea.disabled = false;
            sendBtn.disabled = true;
            textarea.focus();
        }
    }

    // ===== Show Typing Indicator =====
    function showTypingIndicator() {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');

        const typingDiv = document.createElement('div');
        typingDiv.className = 'chatbot-modal__message chatbot-modal__message--assistant';
        typingDiv.id = 'typing-indicator';

        typingDiv.innerHTML = `
            <div class="chatbot-modal__message-avatar">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
            </div>
            <div class="chatbot-modal__message-content">
                <div class="chatbot-modal__typing">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
        `;

        messagesContainer.appendChild(typingDiv);
        scrollToBottom();
    }

    // ===== Hide Typing Indicator =====
    function hideTypingIndicator() {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    // ===== Scroll to Bottom =====
    function scrollToBottom() {
        const messagesContainer = document.querySelector('.chatbot-modal__messages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // ===== Attach Click Handlers for Suggestions =====
    function attachSuggestionClickHandlers() {
        const suggestions = document.querySelectorAll('.suggestion-item');
        suggestions.forEach(item => {
            item.addEventListener('click', function() {
                const question = this.getAttribute('data-question');
                if (question) {
                    // Auto-send the suggested question
                    const textarea = document.querySelector('.chatbot-modal__textarea');
                    if (textarea) {
                        textarea.value = question;
                        sendMessage();
                    }
                }
            });
        });
    }

    // ===== Get Current Time =====
    function getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }

    // ===== Escape HTML =====
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // ===== Auto-initialize on DOM ready =====
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWidget);
    } else {
        initWidget();
    }

    // ===== Expose public API =====
    window.ContactWidget = {
        open: () => document.querySelector('.contact-widget__main-btn')?.click(),
        close: () => closeWidget(),
        openChatbot: () => openChatbotModal(),
        closeChatbot: () => closeChatbotModal(),
        isOpen: () => state.isOpen,
        isChatbotOpen: () => state.isChatbotOpen
    };

})();
