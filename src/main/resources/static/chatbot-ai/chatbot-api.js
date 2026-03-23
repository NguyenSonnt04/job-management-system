/**
 * Chatbot API Client
 * Kết nối với backend API để lấy câu trả lời từ database
 */

(function(window) {
    'use strict';

    // Configuration
    const CONFIG = {
        apiBaseUrl: '/api/chatbot',
        timeout: 10000,
        retries: 2
    };

    // Session Management
    const SessionManager = {
        getUserId: async function() {
            // Try to get from API first
            try {
                const response = await fetch('/api/user/me', { credentials: 'include' });
                const data = await response.json();

                if (data.authenticated && data.id) {
                    // Đã đăng nhập - dùng user ID
                    const userId = 'user_' + data.id;
                    localStorage.setItem('chatbot_user_id', userId);
                    return userId;
                } else {
                    // Chưa đăng nhập - xóa old user ID, tạo guest mới
                    localStorage.removeItem('chatbot_user_id');
                    const guestId = 'guest_' + Date.now();
                    localStorage.setItem('chatbot_user_id', guestId);
                    return guestId;
                }
            } catch (e) {
                console.error('Error getting user ID:', e);
                // Lỗi - tạo guest session mới
                localStorage.removeItem('chatbot_user_id');
                const guestId = 'guest_' + Date.now();
                localStorage.setItem('chatbot_user_id', guestId);
                return guestId;
            }
        },

        getSessionId: async function() {
            return await this.getUserId();
        },

        resetSession: function() {
            localStorage.removeItem('chatbot_user_id');
        }
    };

    // API Client
    const ChatbotAPI = {
        /**
         * Đặt câu hỏi cho chatbot
         * @param {string} question - Câu hỏi
         * @returns {Promise<Object>} Response từ API
         */
        ask: async function(question) {
            const sessionId = await SessionManager.getSessionId();

            try {
                const response = await fetch(`${CONFIG.apiBaseUrl}/ask`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        question: question,
                        sessionId: sessionId
                    }),
                    signal: AbortSignal.timeout(CONFIG.timeout)
                });

                if (!response.ok) {
                    throw new Error(`API error: ${response.status}`);
                }

                const data = await response.json();
                return this.normalizeResponse(data);

            } catch (error) {
                console.error('Chatbot API error:', error);
                return this.getErrorResponse(error);
            }
        },

        /**
         * Gửi feedback
         * @param {number} conversationId - ID cuộc hội thoại
         * @param {boolean} isHelpful - Có hữu ích không
         * @param {string} feedback - Nội dung feedback
         */
        sendFeedback: async function(conversationId, isHelpful, feedback) {
            try {
                const response = await fetch(`${CONFIG.apiBaseUrl}/feedback`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        conversationId: conversationId,
                        isHelpful: isHelpful,
                        feedback: feedback || ''
                    })
                });

                return response.ok;

            } catch (error) {
                console.error('Feedback error:', error);
                return false;
            }
        },

        /**
         * Lấy danh sách câu hỏi nhanh
         */
        getQuickQuestions: async function() {
            try {
                const response = await fetch(`${CONFIG.apiBaseUrl}/quick-questions`);
                if (response.ok) {
                    return await response.json();
                }
            } catch (error) {
                console.error('Quick questions error:', error);
            }
            return this.getDefaultQuickQuestions();
        },

        /**
         * Lấy danh sách categories
         */
        getCategories: async function() {
            try {
                const response = await fetch(`${CONFIG.apiBaseUrl}/categories`);
                if (response.ok) {
                    return await response.json();
                }
            } catch (error) {
                console.error('Categories error:', error);
            }
            return [];
        },

        /**
         * Health check
         */
        healthCheck: async function() {
            try {
                const response = await fetch(`${CONFIG.apiBaseUrl}/health`);
                return response.ok;
            } catch {
                return false;
            }
        },

        /**
         * Normalize response from API
         */
        normalizeResponse: function(data) {
            return {
                success: true,
                conversationId: data.conversationId,
                promptId: data.promptId,
                answer: data.answer,
                title: data.title,
                confidence: data.confidence,
                category: data.category,
                found: data.found,
                suggestions: data.suggestions || []
            };
        },

        /**
         * Get error response
         */
        getErrorResponse: function(error) {
            return {
                success: false,
                error: error.message,
                answer: this.getDefaultAnswer()
            };
        },

        /**
         * Default quick questions (fallback)
         */
        getDefaultQuickQuestions: function() {
            return [
                { id: 1, question: 'Hệ thống có những chức năng chính nào?', icon: '🔍' },
                { id: 2, question: 'Làm thế nào để tạo công việc mới?', icon: '📝' },
                { id: 3, question: 'Các vai trò người dùng là gì?', icon: '👤' },
                { id: 4, question: 'Hệ thống bảo mật như thế nào?', icon: '🔐' }
            ];
        },

        /**
         * Default answer (fallback)
         */
        getDefaultAnswer: function() {
            return `🤔 Xin lỗi, tôi đang gặp sự cố kết nối.

**Bạn có thể:**
• Thử hỏi lại sau giây lát
• Kiểm tra kết nối internet
• Liên hệ với admin nếu vấn đề kéo dài

Xin lỗi vì sự bất tiện này! 😔`;
        }
    };

    // Knowledge Base Fallback (sử dụng khi API không khả dụng)
    const KnowledgeBase = {
        data: {
            greetings: {
                keywords: ['xin chào', 'chào', 'hello', 'hi', 'hey'],
                response: '👋 Xin chào! Tôi là AI Assistant. Tôi có thể giúp gì cho bạn?'
            },
            thanks: {
                keywords: ['cảm ơn', 'thank', 'thanks'],
                response: '😊 Rất vui được giúp đỡ bạn!'
            },
            bye: {
                keywords: ['tạm biệt', 'bye', 'goodbye'],
                response: '👋 Tạm biệt! Chúc bạn một ngày tốt lành!'
            }
        },

        find: function(question) {
            const q = question.toLowerCase().trim();

            for (const category in this.data) {
                const item = this.data[category];
                for (const keyword of item.keywords) {
                    if (q.includes(keyword)) {
                        return {
                            success: true,
                            answer: item.response,
                            found: true,
                            fromCache: true
                        };
                    }
                }
            }

            return {
                success: true,
                answer: ChatbotAPI.getDefaultAnswer(),
                found: false
            };
        }
    };

    // Export
    window.ChatbotAPI = ChatbotAPI;
    window.ChatbotSession = SessionManager;
    window.ChatbotKnowledge = KnowledgeBase;

})(window);
