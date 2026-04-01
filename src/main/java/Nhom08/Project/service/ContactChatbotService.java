package Nhom08.Project.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Nhom08.Project.entity.ChatMessage;
import Nhom08.Project.entity.ChatSession;
import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.ChatMessageRepository;
import Nhom08.Project.repository.ChatSessionRepository;
import Nhom08.Project.repository.JobRepository;

/**
 * Service xử lý chatbot với Gemini AI tích hợp
 * Hỗ trợ tìm việc, gợi ý job phù hợp
 */
@Service
public class ContactChatbotService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private JobRepository jobRepository;

    // ===== System Prompt cho Chatbot =====
    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI chuyên nghiệp cho JCO.

        VAI TRÒ CỦA BẠN:
        - Hỗ trợ người tìm việc: tìm kiếm công việc, viết CV, ứng tuyển, chuẩn bị phỏng vấn
        - Hỗ trợ nhà tuyển dụng: đăng tin, tìm ứng viên, quản lý tuyển dụng
        - Gợi ý việc làm phù hợp dựa trên kỹ năng, kinh nghiệm, mong muốn của người dùng
        - Cung cấp thông tin thị trường tuyển dụng, mức lương, xu hướng nghề nghiệp

        NGUYÊN TẮC TRẢU LỜI:
        - Trả lời ngắn gọn, thân thiện (dưới 120 từ)
        - Dùng ngôn ngữ tự nhiên, mình hỗ trợ như một người bạn
        - Nếu người dùng hỏi về việc làm -> Phân tích và gợi ý công việc phù hợp
        - Nếu người dùng hỏi những gì ngoài phạm vi -> Hướng dẫn liên hệ zalo, gọi điện số điện thoại
        - Không tiết lộ thông tin nội bộ của hệ thống
        - QUAN TRỌNG: KHÔNG TỰ THÊM LINK VÀO CÂU TRẢ LỜI (không thêm link tạo CV, không thêm link chấm điểm CV). Chỉ trả lời hướng dẫn, không tự động chèn link.
        Hãy trả lời một cách hữu ích và thiện chí!
        """;

    /**
     * Tạo session chat mới hoặc lấy session cũ
     * Ưu tiên: existingSessionId → session active gần nhất → session mới
     */
    @Transactional
    public ChatSession createOrGetSession(User user, String existingSessionId) {
        System.out.println("=== createOrGetSession Called ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "Guest"));
        System.out.println("Existing Session ID: " + existingSessionId);

        // 1. Nếu có existingSessionId, thử lấy session cũ
        if (existingSessionId != null && !existingSessionId.isEmpty()) {
            System.out.println("Looking for existing session...");
            Optional<ChatSession> existingSession = sessionRepository.findBySessionIdWithMessages(existingSessionId);

            if (existingSession.isPresent()) {
                ChatSession session = existingSession.get();

                // Kiểm tra user có quyền truy cập session này không
                if (!canUserAccessSession(existingSessionId, user)) {
                    System.out.println("❌ User does not have access to this session.");
                } else {
                    System.out.println("✅ Found existing session! Reusing it.");
                    System.out.println("Session messages count: " + (session.getMessages() != null ? session.getMessages().size() : "0"));

                    // Cập nhật updatedAt để đánh dấu session còn active
                    session.setUpdatedAt(LocalDateTime.now());
                    sessionRepository.save(session);
                    System.out.println("✅ Session updatedAt refreshed");

                    return session;
                }
            } else {
                System.out.println("❌ Existing session not found in database.");
            }
        }

        // 2. Nếu không tìm thấy existing session và user đã login, tìm session active gần nhất
        if (user != null) {
            System.out.println("Looking for most recent active session for user...");
            List<ChatSession> activeSessions = sessionRepository.findActiveSessionsByUserIdOrderByUpdatedAtDesc(user.getId());

            if (!activeSessions.isEmpty()) {
                ChatSession session = activeSessions.get(0);
                System.out.println("✅ Found active session: " + session.getSessionId());

                // Load lại với messages
                session = sessionRepository.findBySessionIdWithMessages(session.getSessionId()).orElse(session);

                // Cập nhật updatedAt
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepository.save(session);

                return session;
            } else {
                System.out.println("❌ No active session found for user.");
            }
        }

        // 3. Tạo session mới
        System.out.println("Creating new session...");
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = user != null ? new ChatSession(user, sessionId) : new ChatSession(sessionId);
        session = sessionRepository.save(session);
        System.out.println("✅ New session created with ID: " + sessionId);

        // Thêm tin nhắn chào mừng
        ChatMessage welcomeMsg = new ChatMessage(session, user, "ASSISTANT", getWelcomeMessage());
        messageRepository.save(welcomeMsg);
        System.out.println("✅ Welcome message saved");

        // KHÔNG thêm tin nhắn gợi ý nữa - chỉ chào thôi
        // ChatMessage suggestionsMsg = new ChatMessage(session, user, "SYSTEM", buildSuggestionsMessage());
        // messageRepository.save(suggestionsMsg);
        // System.out.println("✅ Suggestions message saved");

        // Load lại session với messages để tránh lazy loading issue
        session = sessionRepository.findBySessionIdWithMessages(sessionId).orElse(session);
        System.out.println("✅ Session reloaded with messages");

        return session;
    }

    /**
     * Gửi tin nhắn và nhận phản hồi từ Gemini AI
     */
    @Transactional
    public Map<String, Object> sendMessage(String sessionId, String userMessage, User currentUser) {
        System.out.println("\n=== sendMessage Called ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("User Message: " + userMessage);

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Lấy session
            ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

            System.out.println("✅ Session found");

            // 2. Kiểm tra user có quyền truy cập session này không
            if (!canUserAccessSession(sessionId, currentUser)) {
                throw new RuntimeException("Bạn không có quyền truy cập session này");
            }

            System.out.println("✅ User access verified");

            // Cập nhật updatedAt để đánh dấu session còn active
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
            System.out.println("✅ Session updatedAt refreshed");

            // 3. Lưu tin nhắn của user
            ChatMessage userMsg = new ChatMessage(session, currentUser, "USER", userMessage);
            messageRepository.save(userMsg);
            System.out.println("✅ User message saved");

            // 3. Lấy lịch sử chat gần nhất để có context
            List<ChatMessage> history = messageRepository.findMessagesBySessionId(sessionId);
            System.out.println("✅ Found " + history.size() + " messages in database");

            StringBuilder contextBuilder = new StringBuilder();

            // Chỉ lấy 10 tin nhắn gần nhất
            int recentMessages = Math.min(history.size(), 10);
            for (int i = Math.max(0, history.size() - recentMessages); i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                if (!"SYSTEM".equals(msg.getRole())) { // Bỏ qua system messages
                    contextBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
                }
            }

            // 4. Xác định xem có cần gợi ý jobs hoặc CV không
            String response;
            if (shouldRecommendJobs(userMessage)) {
                System.out.println("Triggering job recommendations...");
                response = recommendJobs(userMessage);
            } else {
                // 5. Gọi Gemini API với context
                System.out.println("Calling Gemini AI...");
                String fullPrompt = SYSTEM_PROMPT + "\n\nLỊCH SỬ ĐỐI THOẠI GẦN NHẤT:\n" +
                    contextBuilder.toString() + "\n\nCÂU HỎI HIỆN TẠI: " + userMessage;

                response = geminiService.callGeminiPlainText(fullPrompt);

                // Nếu người dùng hỏi về chấm điểm CV, thêm link vào phản hồi (ưu tiên check trước)
                if (isAskingAboutCVScoring(userMessage)) {
                    response += "\n\n**Chấm điểm CV với AI:** [Chấm điểm CV ở đây!!!](http://localhost:8083/cham-diem-cv.html)";
                }
                // Nếu người dùng hỏi về CV (tạo CV, làm CV...), thêm link vào phản hồi
                // Nhưng KHÔNG thêm nếu đã là câu hỏi về chấm điểm CV
                else if (isAskingAboutCV(userMessage)) {
                    response += "\n\n**Tạo CV với AI:** [Tạo CV ở đây!!!](http://localhost:8083/tao-cv-ai.html)";
                }

                System.out.println("✅ AI response received");
            }

            // 6. Lưu phản hồi
            ChatMessage botMsg = new ChatMessage(session, currentUser, "ASSISTANT", response);
            messageRepository.save(botMsg);
            System.out.println("✅ Assistant message saved");

            // 7. Trả về kết quả
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", response);
            result.put("role", "ASSISTANT");
            result.put("timestamp", botMsg.getCreatedAt().toString());

        } catch (Exception e) {
            System.err.println("❌ Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return result;
    }

    /**
     * Kiểm tra xem có nên gợi ý jobs không
     */
    private boolean shouldRecommendJobs(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("gợi ý") || lowerMessage.contains("recommend") ||
               lowerMessage.contains("việc làm") || lowerMessage.contains("job") ||
               lowerMessage.contains("tuyển dụng") || lowerMessage.contains("ứng tuyển");
    }

    /**
     * Kiểm tra xem người dùng có hỏi về CV không
     */
    private boolean isAskingAboutCV(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("cv") || lowerMessage.contains("resume") ||
               lowerMessage.contains("sơ yếu lý lịch") || lowerMessage.contains("tạo cv") ||
               lowerMessage.contains("viết cv") || lowerMessage.contains("làm cv");
    }

    /**
     * Kiểm tra xem người dùng có hỏi về chấm điểm CV không
     */
    private boolean isAskingAboutCVScoring(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("chấm điểm cv") || lowerMessage.contains("chấm cv") ||
               lowerMessage.contains("review cv") || lowerMessage.contains("đánh giá cv") ||
               lowerMessage.contains("kiểm tra cv") || lowerMessage.contains("scoring cv") ||
               lowerMessage.contains("chấm điểm resume") || lowerMessage.contains("cv scoring");
    }

    /**
     * Gợi ý jobs phù hợp
     */
    private String recommendJobs(String userQuery) {
        try {
            System.out.println("Fetching jobs from database...");
            // Lấy các jobs đang active
            List<Job> activeJobs = jobRepository.findByStatus("ACTIVE");

            if (activeJobs.isEmpty()) {
                return "Hiện tại chưa có công việc nào đang tuyển dụng. Vui lòng quay lại sau!\n\n**Xem tất cả việc làm:** [Tìm việc làm ở đây!!!](http://localhost:8083/tim-viec-lam.html)";
            }

            System.out.println("✅ Found " + activeJobs.size() + " active jobs");

            // Lấy ngẫu nhiên 5 jobs để gợi ý
            int sampleSize = Math.min(5, activeJobs.size());
            List<Job> suggestedJobs = activeJobs.subList(0, sampleSize);

            StringBuilder response = new StringBuilder();
            response.append("💼 **Dưới đây là một số công việc phù hợp:**\n\n");

            for (int i = 0; i < suggestedJobs.size(); i++) {
                Job job = suggestedJobs.get(i);
                response.append(String.format("**%d. %s**\n", (i + 1), job.getTitle()));
                response.append(String.format("🏢 %s\n", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty"));
                response.append(String.format("📍 %s\n", job.getLocation() != null ? job.getLocation() : "Toàn quốc"));
                response.append(String.format("💰 %s\n", job.getSalaryMin() != null ? job.getSalaryMin() + " - " + job.getSalaryMax() : "Thỏa thuận"));
                response.append(String.format("🔗 [Xem chi tiết và ứng tuyển](/job-detail?id=%d)\n\n", job.getId()));
            }

            response.append("\n👆 Click vào link để xem chi tiết và ứng tuyển ngay!");
            response.append("\n\n**Xem tất cả việc làm:** [Tìm việc ở đây!!!](http://localhost:8083/tim-viec-lam.html)");

            String result = response.toString();
            System.out.println("✅ Job recommendations generated: " + result.length() + " characters");

            return result;

        } catch (Exception e) {
            System.err.println("❌ Error recommending jobs: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, có lỗi khi tải danh sách việc làm. Vui lòng thử lại!";
        }
    }

    /**
     * Lấy câu chào mặc định
     */
    private String getWelcomeMessage() {
        return "Xin chào! Mình là trợ lý AI của JCO.\n\n" +
            "Mình có thể giúp bạn:\n" +
            "• Tìm kiếm công việc phù hợp\n" +
            "• Tư vấn viết CV chuyên nghiệp\n" +
            "• Hướng dẫn ứng tuyển hiệu quả\n" +
            "• Chuẩn bị cho phỏng vấn\n\n" +
            "Bạn cần hỗ trợ gì không?";
    }

    /**
     * Lấy lịch sử chat
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        System.out.println("=== getChatHistory Called ===");
        System.out.println("Session ID: " + sessionId);

        List<ChatMessage> messages = messageRepository.findMessagesBySessionId(sessionId);
        System.out.println("✅ Found " + messages.size() + " messages in database");

        // Log từng message để debug
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            System.out.println("  Message " + i + ": role=" + msg.getRole() +
                             ", content_length=" + (msg.getContent() != null ? msg.getContent().length() : 0));
        }

        return messages;
    }

    /**
     * Xóa sessions không hoạt động quá 24 giờ
     * Dựa trên updatedAt (thời gian cuối cùng tương tác) thay vì createdAt
     */
    @Transactional
    public int cleanupOldSessions() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        System.out.println("=== Cleanup Called ===");
        System.out.println("Looking for sessions inactive since: " + oneDayAgo);

        // Lấy tất cả sessions không hoạt động quá 24h (dựa trên updatedAt)
        List<ChatSession> oldSessions = sessionRepository.findAll().stream()
            .filter(s -> s.getUpdatedAt().isBefore(oneDayAgo))
            .toList();

        System.out.println("Found " + oldSessions.size() + " inactive sessions");

        int deletedCount = 0;
        for (ChatSession session : oldSessions) {
            try {
                // Xóa tất cả messages của session
                messageRepository.deleteBySessionId(session.getId());
                // Xóa session
                sessionRepository.delete(session);
                deletedCount++;
                System.out.println("✅ Deleted session: " + session.getSessionId() +
                                 " (last active: " + session.getUpdatedAt() + ")");
            } catch (Exception e) {
                // Log error but continue
                System.err.println("❌ Error deleting session: " + e.getMessage());
            }
        }

        System.out.println("=== Cleanup Complete ===");
        System.out.println("Total deleted: " + deletedCount + " sessions");

        return deletedCount;
    }

    /**
     * Xóa session cụ thể
     */
    @Transactional
    public void deleteSession(String sessionId) {
        System.out.println("=== deleteSession Called ===");
        System.out.println("Session ID: " + sessionId);

        ChatSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        System.out.println("Session found. Deleting...");

        // Xóa tất cả messages
        messageRepository.deleteBySessionId(session.getId());
        System.out.println("✅ Messages deleted");

        // Xóa session
        sessionRepository.delete(session);
        System.out.println("✅ Session deleted");
    }

    /**
     * Kiểm tra user có quyền truy cập session không
     * - Session của guest (không có user) -> chỉ guest dùng được, user đã login KHÔNG dùng lại
     * - Session của user đã đăng nhập -> chỉ user đó mới truy cập được
     */
    public boolean canUserAccessSession(String sessionId, User user) {
        try {
            System.out.println("=== canUserAccessSession Called ===");
            System.out.println("Session ID: " + sessionId);
            System.out.println("User: " + (user != null ? user.getEmail() + " (ID: " + user.getId() + ")" : "Guest"));

            Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isEmpty()) {
                System.out.println("❌ Session not found");
                return false;
            }

            ChatSession session = sessionOpt.get();

            // Nếu session là guest (không có user)
            if (session.getUser() == null) {
                // Chỉ guest user mới dùng được guest session
                // User đã login PHẢI tạo session riêng
                if (user == null) {
                    System.out.println("✅ Guest accessing guest session - OK");
                    return true;
                } else {
                    System.out.println("❌ Logged-in user trying to access guest session - MUST create new session");
                    return false;
                }
            }

            // Nếu session có user -> chỉ user đó mới truy cập được
            if (user == null) {
                // Guest user đang cố truy cập session của user đã login
                System.out.println("❌ Guest user trying to access session of user ID: " + session.getUser().getId());
                return false;
            }

            // Kiểm tra user ID có khớp không
            boolean hasAccess = session.getUser().getId().equals(user.getId());
            System.out.println("Session user ID: " + session.getUser().getId() + ", Current user ID: " + user.getId());
            System.out.println(hasAccess ? "✅ Access granted" : "❌ Access denied");

            return hasAccess;

        } catch (Exception e) {
            System.err.println("Error checking session access: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra session tồn tại
     */
    public boolean sessionExists(String sessionId) {
        boolean exists = sessionRepository.findBySessionId(sessionId).isPresent();
        System.out.println("Session exists check for " + sessionId + ": " + exists);
        return exists;
    }
}
