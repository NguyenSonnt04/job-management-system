/**
 * Chatbot AI - Main Logic
 * Xử lý tương tác với chatbot
 */

(function() {
    'use strict';

    // DOM Elements
    const chatbotPopup = document.getElementById('chatbotPopup');
    const chatbotToggle = document.getElementById('chatbotToggle');
    const chatbotMinimize = document.getElementById('chatbotMinimize');
    const chatbotClose = document.getElementById('chatbotClose');
    const chatbotMinimizedBtn = document.getElementById('chatbotMinimizedBtn');
    const chatbotMessages = document.getElementById('chatbotMessages');
    const chatbotInput = document.getElementById('chatbotInput');
    const chatbotSend = document.getElementById('chatbotSend');
    const chatbotQuickQuestions = document.getElementById('chatbotQuickQuestions');

    // State
    let isOpen = false;
    let isMinimized = false;
    let unreadCount = 0;

    // Initialize
    function init() {
        if (!chatbotPopup || !chatbotToggle) {
            console.warn('Chatbot elements not found');
            return;
        }

        // Event Listeners
        chatbotToggle.addEventListener('click', openChatbot);
        chatbotMinimize.addEventListener('click', minimizeChatbot);
        chatbotClose.addEventListener('click', closeChatbot);
        chatbotMinimizedBtn.addEventListener('click', openChatbot);
        chatbotSend.addEventListener('click', sendMessage);

        chatbotInput.addEventListener('input', handleInputChange);
        chatbotInput.addEventListener('keydown', handleKeyPress);

        // Quick questions
        const quickButtons = chatbotQuickQuestions?.querySelectorAll('.quick-question');
        quickButtons?.forEach(btn => {
            btn.addEventListener('click', () => {
                const question = btn.dataset.question;
                if (question) {
                    chatbotInput.value = question;
                    sendMessage();
                }
            });
        });

        // Auto-resize textarea
        chatbotInput.addEventListener('input', autoResizeTextarea);

        // Load state from localStorage
        loadChatState();
    }

    // Open chatbot
    function openChatbot() {
        isOpen = true;
        isMinimized = false;
        chatbotPopup.classList.add('active');
        chatbotPopup.classList.remove('minimized');
        chatbotToggle.style.display = 'none';
        chatbotMinimizedBtn.style.display = 'none';

        // Clear unread count
        unreadCount = 0;
        updateMinimizedBadge();

        // Focus input
        setTimeout(() => chatbotInput.focus(), 300);

        saveChatState();
    }

    // Minimize chatbot
    function minimizeChatbot() {
        isMinimized = true;
        chatbotPopup.classList.add('minimized');
        chatbotPopup.classList.remove('active');
        chatbotMinimizedBtn.style.display = 'flex';

        saveChatState();
    }

    // Close chatbot
    function closeChatbot() {
        isOpen = false;
        isMinimized = false;
        chatbotPopup.classList.remove('active');
        chatbotPopup.classList.remove('minimized');
        chatbotToggle.style.display = 'flex';
        chatbotMinimizedBtn.style.display = 'none';

        // Reset unread count
        unreadCount = 0;
        updateMinimizedBadge();

        saveChatState();
    }

    // Handle input change
    function handleInputChange() {
        const hasValue = chatbotInput.value.trim().length > 0;
        chatbotSend.disabled = !hasValue;
    }

    // Handle key press
    function handleKeyPress(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    }

    // Auto resize textarea
    function autoResizeTextarea() {
        chatbotInput.style.height = 'auto';
        chatbotInput.style.height = Math.min(chatbotInput.scrollHeight, 100) + 'px';
    }

    // Send message
    async function sendMessage() {
        const message = chatbotInput.value.trim();
        if (!message) return;

        // Add user message
        addMessage(message, 'user');

        // Clear input
        chatbotInput.value = '';
        chatbotInput.style.height = 'auto';
        chatbotSend.disabled = true;

        // Show typing indicator
        showTypingIndicator();

        // Call API Backend
        try {
            if (window.ChatbotAPI) {
                const result = await window.ChatbotAPI.ask(message);
                hideTypingIndicator();
                addMessage(result.answer, 'bot');
            } else {
                // Fallback nếu ChatbotAPI không tồn tại
                hideTypingIndicator();
                addMessage(getDefaultResponse(message), 'bot');
            }
        } catch (error) {
            hideTypingIndicator();
            console.error('Chatbot error:', error);
            addMessage(getDefaultResponse(message), 'bot');
        }
    }

    // Add message to chat
    function addMessage(content, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `chatbot-message ${sender}`;

        const avatarDiv = document.createElement('div');
        avatarDiv.className = sender === 'bot' ? 'chatbot-avatar bot-avatar' : '';

        if (sender === 'bot') {
            avatarDiv.innerHTML = '<img src="images/logo_chatbot.png" alt="AI">';
        }

        const messageContent = document.createElement('div');
        messageContent.className = 'chatbot-message-content';

        const bubble = document.createElement('div');
        bubble.className = `chatbot-bubble ${sender}-bubble`;

        // Convert markdown-like syntax to HTML
        bubble.innerHTML = formatMessage(content);

        const time = document.createElement('span');
        time.className = 'chatbot-time';
        time.textContent = getCurrentTime();

        messageContent.appendChild(bubble);
        messageContent.appendChild(time);

        if (sender === 'bot') {
            messageDiv.appendChild(avatarDiv);
        }

        messageDiv.appendChild(messageContent);
        chatbotMessages.appendChild(messageDiv);

        // Scroll to bottom
        scrollToBottom();

        // Update unread if minimized
        if (sender === 'bot' && (!isOpen || isMinimized)) {
            unreadCount++;
            updateMinimizedBadge();
        }
    }

    // Format message (convert markdown to HTML)
    function formatMessage(content) {
        return content
            // Bold
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            // Italic
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            // Code
            .replace(/`(.*?)`/g, '<code>$1</code>')
            // Line breaks
            .replace(/\n\n/g, '</p><p>')
            .replace(/\n/g, '<br>')
            // Wrap in p tags if not already
            .replace(/^(?!<)/, '<p>')
            .replace(/(?<!>)$/, '</p>');
    }

    // Show typing indicator
    function showTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chatbot-message bot typing-message';
        typingDiv.id = 'typingIndicator';

        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'chatbot-avatar bot-avatar';
        avatarDiv.innerHTML = '<img src="images/logo_chatbot.png" alt="AI">';

        const typingBubble = document.createElement('div');
        typingBubble.className = 'chatbot-typing';
        typingBubble.innerHTML = `
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
        `;

        typingDiv.appendChild(avatarDiv);
        typingDiv.appendChild(typingBubble);
        chatbotMessages.appendChild(typingDiv);

        scrollToBottom();
    }

    // Hide typing indicator
    function hideTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    // Get current time
    function getCurrentTime() {
        const now = new Date();
        const hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    // Scroll to bottom
    function scrollToBottom() {
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
    }

    // Update minimized badge
    function updateMinimizedBadge() {
        const notification = chatbotMinimizedBtn.querySelector('.chatbot-notification');
        if (notification) {
            if (unreadCount > 0) {
                notification.style.display = 'block';
            } else {
                notification.style.display = 'none';
            }
        }
    }

    // Get default response
    function getDefaultResponse(message) {
        return `🤔 Xin lỗi, tôi chưa hiểu câu hỏi "${message}" của bạn.

**Bạn có thể hỏi về:**
• Các chức năng của hệ thống
• Cách tạo công việc mới
• Vai trò người dùng
• Bảo mật và đăng nhập
• Công nghệ sử dụng

Hoặc chọn một trong các câu hỏi gợi ý bên dưới! 😊`;
    }

    // Save chat state
    function saveChatState() {
        const state = { isOpen, isMinimized, unreadCount };
        localStorage.setItem('chatbotState', JSON.stringify(state));
    }

    // Load chat state
    function loadChatState() {
        try {
            const savedState = localStorage.getItem('chatbotState');
            if (savedState) {
                const state = JSON.parse(savedState);
                if (state.isOpen && !state.isMinimized) {
                    openChatbot();
                } else if (state.isMinimized) {
                    minimizeChatbot();
                }
            }
        } catch (e) {
            console.warn('Failed to load chat state:', e);
        }
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
