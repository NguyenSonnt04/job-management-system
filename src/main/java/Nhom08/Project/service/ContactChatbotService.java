package Nhom08.Project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Nhom08.Project.entity.ChatMessage;
import Nhom08.Project.entity.ChatSession;
import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.ChatMessageRepository;
import Nhom08.Project.repository.ChatSessionRepository;
import Nhom08.Project.repository.JobRepository;

/**
 * Chatbot service sử dụng RAG (Retrieval-Augmented Generation).
 * Gemini AI phân tích intent + trích xuất filter → query DB → Gemini format response.
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

    @Value("${app.public-base-url:http://localhost:8083}")
    private String publicBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== System Prompt cho chatbot conversation =====
    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI của website JCO (Job Connection Online) - hệ thống quản lý tuyển dụng.

        PHẠM VI HỖ TRỢ - CHỈ trả lời các câu hỏi liên quan đến:
        1. Tìm kiếm việc làm trên website JCO
        2. Xem và ứng tuyển vào các tin tuyển dụng
        3. Quản lý CV: tạo CV, tải CV lên, xem CV
        4. Quản lý hồ sơ ứng tuyển: xem trạng thái, lịch sử ứng tuyển
        5. Hỗ trợ nhà tuyển dụng: đăng tin tuyển dụng, quản lý ứng viên
        6. Hướng dẫn sử dụng các tính năng của website JCO

        NGUYÊN TẮC TỪ CHỐI:
        - Nếu câu hỏi KHÔNG liên quan đến JCO → Từ chối lịch sự
        - Khi từ chối: "Mình là trợ lý tuyển dụng của JCO. Mình sẽ hỗ trợ các vấn đề liên quan đến hệ thống này như tìm việc, quản lý CV và hồ sơ ứng tuyển. Bạn có câu hỏi nào về những chức năng này không?"

        NGUYÊN TẮC TRẢ LỜI:
        - Trả lời ngắn gọn, thân thiện (dưới 100 từ)
        - LUÔN xưng "mình", gọi người dùng là "bạn"
        - KHÔNG TỰ THÊM LINK vào câu trả lời
        """;

    // ===== RAG Prompt: Gemini tự suy nghĩ và quyết định toàn bộ =====
    private static final String RAG_PROMPT_TEMPLATE = """
        Bạn là Trợ lý AI tuyển dụng của JCO (Job Connection Online) — một người bạn thân thiện, vui vẻ, luôn sẵn lòng giúp đỡ.

        TÍNH CÁCH:
        - Xưng "mình", gọi người dùng là "bạn"
        - Nói chuyện tự nhiên, gần gũi như bạn bè (không robot, không cứng nhắc)
        - Dùng emoji vừa phải để tạo cảm giác thân thiện
        - Có thể dùng câu cảm thán, hỏi ngược, động viên
        - Trả lời ngắn gọn (dưới 80 từ), đi thẳng vào vấn đề

        DANH SÁCH CÔNG VIỆC ĐANG TUYỂN (format: ID | Chức danh | Địa điểm | Ngành | Kinh nghiệm | Lương):
        %s

        CÁC TRANG TRÊN JCO (chèn vào response khi phù hợp, dùng markdown link):
        - Tạo CV bằng AI: [Tạo CV ngay](%s)
        - Chấm điểm CV bằng AI: [Chấm điểm CV](%s)
        - Xem tất cả việc làm: [Xem việc làm](%s)

        Trả về JSON:
        {
          "response": "Câu trả lời hoàn chỉnh bằng tiếng Việt — bao gồm cả link nếu cần",
          "matchedJobIds": [id1, id2, ...]
        }

        CÁCH HOẠT ĐỘNG:
        - Bạn tự suy nghĩ và quyết định cách trả lời tốt nhất cho người dùng
        - Nếu họ tìm việc → chọn jobs phù hợp nhất vào matchedJobIds, viết lời giới thiệu
        - Nếu họ hỏi về CV → trả lời + chèn link tạo CV hoặc chấm điểm CV vào response
        - Nếu họ hỏi cách dùng website → hướng dẫn + chèn link liên quan
        - Nếu ngoài phạm vi → từ chối nhẹ nhàng, gợi ý quay lại chủ đề JCO
        - KHÔNG cần phân loại intent — hãy tự nhiên như đang nói chuyện

        QUY TẮC CHỌN JOBS (matchedJobIds):
        - Chọn tối đa 5 jobs PHÙ HỢP NHẤT từ danh sách, phù hợp nhất trước
        - KHỚP ĐÚNG cấp độ: intern/thực tập ≠ senior, junior ≠ manager
        - KHỚP ĐÚNG vị trí: frontend ≠ backend, kế toán ≠ nhân sự
        - KHÔNG có job phù hợp → trả [] (không ép chọn job lệch)
        - CHỈ dùng ID trong danh sách, KHÔNG bịa ID
        - Nếu không liên quan đến tìm việc → trả []

        PHẠM VI JCO: Tìm việc, ứng tuyển, CV, hồ sơ, đăng tin, hướng dẫn website JCO.
        NGOÀI PHẠM VI: Từ chối thân thiện, VD: "Câu này hơi ngoài chuyên môn của mình rồi 😅 Mình giỏi nhất là tìm việc và hỗ trợ CV — bạn cần gì không?"

        HIỂU TIẾNG VIỆT KHÔNG DẤU: "viec lam"="việc làm", "luong"="lương", "intern"="thực tập sinh"
        """;

    // =========================================================================
    // SESSION MANAGEMENT
    // =========================================================================

    @Transactional
    public ChatSession createOrGetSession(User user, String existingSessionId) {
        // 1. Nếu có existingSessionId, thử lấy session cũ
        if (existingSessionId != null && !existingSessionId.isEmpty()) {
            Optional<ChatSession> existingSession = sessionRepository.findBySessionIdWithMessages(existingSessionId);
            if (existingSession.isPresent() && canUserAccessSession(existingSessionId, user)) {
                ChatSession session = existingSession.get();
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepository.save(session);
                return session;
            }
        }

        // 2. Nếu user đã login, tìm session active gần nhất
        if (user != null) {
            List<ChatSession> activeSessions = sessionRepository.findActiveSessionsByUserIdOrderByUpdatedAtDesc(user.getId());
            if (!activeSessions.isEmpty()) {
                ChatSession session = activeSessions.get(0);
                session = sessionRepository.findBySessionIdWithMessages(session.getSessionId()).orElse(session);
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepository.save(session);
                return session;
            }
        }

        // 3. Tạo session mới
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = user != null ? new ChatSession(user, sessionId) : new ChatSession(sessionId);
        session = sessionRepository.save(session);

        ChatMessage welcomeMsg = new ChatMessage(session, user, "ASSISTANT", getWelcomeMessage());
        messageRepository.save(welcomeMsg);

        session = sessionRepository.findBySessionIdWithMessages(sessionId).orElse(session);
        return session;
    }

    // =========================================================================
    // CORE RAG LOGIC
    // =========================================================================

    @Transactional
    public Map<String, Object> sendMessage(String sessionId, String userMessage, User currentUser) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Lấy session + kiểm tra quyền
            ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

            if (!canUserAccessSession(sessionId, currentUser)) {
                throw new RuntimeException("Bạn không có quyền truy cập session này");
            }

            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);

            // 2. Lưu tin nhắn user
            ChatMessage userMsg = new ChatMessage(session, currentUser, "USER", userMessage);
            messageRepository.save(userMsg);

            // 3. Lấy context từ lịch sử chat (10 tin nhắn gần nhất)
            List<ChatMessage> history = messageRepository.findMessagesBySessionId(sessionId);
            String chatContext = buildChatContext(history);

            // 4. RAG: 1 Gemini call — AI tự suy nghĩ và quyết định toàn bộ
            JsonNode analysis = callRAG(userMessage, chatContext);

            String finalResponse = analysis.path("response").asText(
                "Mình có thể giúp bạn tìm việc, tạo CV, hoặc hướng dẫn sử dụng website JCO. Bạn cần hỗ trợ gì? 😊");

            // 5. Nếu AI chọn jobs → resolve thành card data và embed vào response
            List<Map<String, Object>> jobCards = resolveMatchedJobs(analysis.path("matchedJobIds"));
            if (!jobCards.isEmpty()) {
                finalResponse += "\n<!--JOBS" + objectMapper.writeValueAsString(jobCards) + "JOBS-->";
            }

            // 6. Lưu response vào DB
            ChatMessage botMsg = new ChatMessage(session, currentUser, "ASSISTANT", finalResponse);
            messageRepository.save(botMsg);

            // 7. Trả về kết quả
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", finalResponse);
            result.put("role", "ASSISTANT");
            result.put("timestamp", botMsg.getCreatedAt().toString());
            if (jobCards != null && !jobCards.isEmpty()) {
                result.put("jobs", jobCards);
            }

        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return result;
    }

    // =========================================================================
    // RAG: 1 Gemini call = intent + job matching + response
    // =========================================================================

    /**
     * Gọi Gemini 1 lần duy nhất: gửi user message + danh sách jobs.
     * Gemini trả JSON: { intent, response, matchedJobIds }
     */
    private JsonNode callRAG(String userMessage, String chatContext) {
        try {
            String jobListText = buildJobListForPrompt();

            String prompt = String.format(RAG_PROMPT_TEMPLATE,
                    jobListText,
                    publicUrl("/tao-cv-ai.html"),
                    publicUrl("/cham-diem-cv.html"),
                    publicUrl("/tim-viec-lam.html")) +
                "\n\nLỊCH SỬ ĐỐI THOẠI:\n" + chatContext +
                "\n\nTIN NHẮN HIỆN TẠI: " + userMessage;

            String jsonResponse = geminiService.callGeminiJson(prompt);

            // Dọn markdown wrapper nếu có
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            return objectMapper.readTree(jsonResponse);

        } catch (Exception e) {
            System.err.println("Error in RAG call: " + e.getMessage());
            // Fallback: gọi Gemini plain text
            try {
                String fallbackResponse = geminiService.callGeminiPlainText(
                    SYSTEM_PROMPT + "\n\nLỊCH SỬ:\n" + chatContext + "\n\nCÂU HỎI: " + userMessage);
                return objectMapper.createObjectNode()
                    .put("response", fallbackResponse);
            } catch (Exception ex) {
                return objectMapper.createObjectNode()
                    .put("response", "Xin lỗi, mình đang gặp sự cố. Bạn thử lại sau nhé! 😊");
            }
        }
    }

    /**
     * Build danh sách jobs active gửi cho Gemini (mới nhất trước, tối đa 50 jobs)
     */
    private String buildJobListForPrompt() {
        List<Job> activeJobs = jobRepository.findByStatusOrderByCreatedAtDesc("ACTIVE").stream()
            .filter(job -> job.getDeadline() == null || !job.getDeadline().isBefore(java.time.LocalDate.now()))
            .collect(Collectors.toList());
        if (activeJobs.isEmpty()) return "(Hiện tại không có công việc nào đang tuyển)";

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(50, activeJobs.size());
        for (int i = 0; i < limit; i++) {
            Job job = activeJobs.get(i);
            String salary = "Thỏa thuận";
            if (job.getShowSalary() != null && job.getShowSalary()
                    && job.getSalaryMin() != null && job.getSalaryMax() != null) {
                salary = formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax());
            }
            sb.append(String.format("ID:%d | %s | %s | %s | %s | %s\n",
                job.getId(),
                job.getTitle(),
                job.getLocation() != null ? job.getLocation() : "",
                job.getIndustry() != null ? job.getIndustry() : "",
                job.getExperience() != null ? job.getExperience() : "",
                salary));
        }
        return sb.toString();
    }

    /**
     * Map matchedJobIds từ Gemini về danh sách card data
     */
    private List<Map<String, Object>> resolveMatchedJobs(JsonNode matchedJobIds) {
        if (matchedJobIds == null || !matchedJobIds.isArray() || matchedJobIds.isEmpty()) {
            return List.of();
        }

        // Lấy IDs
        List<Long> ids = new ArrayList<>();
        for (JsonNode idNode : matchedJobIds) {
            ids.add(idNode.asLong());
        }

        // Query jobs từ DB
        List<Job> allActive = jobRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        Map<Long, Job> jobMap = new HashMap<>();
        for (Job job : allActive) {
            jobMap.put(job.getId(), job);
        }

        // Map theo thứ tự Gemini trả về
        return ids.stream()
            .filter(jobMap::containsKey)
            .limit(5)
            .map(id -> toJobCard(jobMap.get(id)))
            .collect(Collectors.toList());
    }

    /**
     * Chuyển Job entity thành card data cho frontend
     */
    private Map<String, Object> toJobCard(Job job) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", job.getId());
        card.put("title", job.getTitle());
        card.put("companyName", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty");
        card.put("logoUrl", job.getEmployer() != null ? job.getEmployer().getLogoUrl() : null);
        card.put("location", job.getLocation());
        card.put("employmentType", job.getEmploymentType());
        card.put("url", publicUrl("/job-detail.html?id=" + job.getId()));

        if (job.getShowSalary() != null && job.getShowSalary()
                && job.getSalaryMin() != null && job.getSalaryMax() != null) {
            card.put("salary", formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax()));
        } else {
            card.put("salary", "Thỏa thuận");
        }

        return card;
    }

    // =========================================================================
    // CHAT HISTORY & SESSION MANAGEMENT
    // =========================================================================

    public List<ChatMessage> getChatHistory(String sessionId) {
        return messageRepository.findMessagesBySessionId(sessionId);
    }

    @Transactional
    public int cleanupOldSessions() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<ChatSession> oldSessions = sessionRepository.findAll().stream()
            .filter(s -> s.getUpdatedAt().isBefore(oneDayAgo))
            .toList();

        int deletedCount = 0;
        for (ChatSession session : oldSessions) {
            try {
                messageRepository.deleteBySessionId(session.getId());
                sessionRepository.delete(session);
                deletedCount++;
            } catch (Exception e) {
                System.err.println("Error deleting session: " + e.getMessage());
            }
        }
        return deletedCount;
    }

    @Transactional
    public void deleteSession(String sessionId) {
        ChatSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session không tồn tại"));
        messageRepository.deleteBySessionId(session.getId());
        sessionRepository.delete(session);
    }

    public boolean canUserAccessSession(String sessionId, User user) {
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) return false;

        ChatSession session = sessionOpt.get();

        if (session.getUser() == null) {
            return user == null;
        }
        if (user == null) return false;
        return session.getUser().getId().equals(user.getId());
    }

    public boolean sessionExists(String sessionId) {
        return sessionRepository.findBySessionId(sessionId).isPresent();
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private String getWelcomeMessage() {
        return "Xin chào! Mình là trợ lý AI của JCO.\n\n" +
            "Mình có thể giúp bạn:\n" +
            "• Tìm kiếm công việc phù hợp\n" +
            "• Chấm điểm CV bằng AI\n" +
            "• Tạo CV mới\n" +
            "• Hướng dẫn sử dụng website\n\n" +
            "Bạn cần hỗ trợ gì không?";
    }

    private String buildChatContext(List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            if (!"SYSTEM".equals(msg.getRole())) {
                String content = msg.getContent();
                // Loại bỏ embedded jobs data khỏi context
                if (content.contains("<!--JOBS")) {
                    content = content.substring(0, content.indexOf("<!--JOBS")).trim();
                }
                sb.append(msg.getRole()).append(": ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    private String formatSalary(java.math.BigDecimal salary) {
        if (salary == null) return "Thỏa thuận";
        double millions = salary.doubleValue() / 1_000_000;
        return new java.text.DecimalFormat("#.#").format(millions) + " Tr";
    }

    private String publicUrl(String path) {
        String baseUrl = StringUtils.hasText(publicBaseUrl) ? publicBaseUrl.trim() : "http://localhost:8083";
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        if (!path.startsWith("/")) path = "/" + path;
        return baseUrl + path;
    }

}
