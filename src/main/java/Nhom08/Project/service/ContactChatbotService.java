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

    // ===== RAG Prompt: phân tích intent + chọn jobs phù hợp trong 1 lần gọi =====
    private static final String RAG_PROMPT_TEMPLATE = """
        Bạn là Trợ lý AI tuyển dụng của JCO (Job Connection Online) — một người bạn thân thiện, vui vẻ, luôn sẵn lòng giúp đỡ.

        TÍNH CÁCH:
        - Xưng "mình", gọi người dùng là "bạn"
        - Nói chuyện tự nhiên, gần gũi như bạn bè (không robot, không cứng nhắc)
        - Dùng emoji vừa phải để tạo cảm giác thân thiện
        - Có thể dùng câu cảm thán, hỏi ngược, động viên
        - Trả lời ngắn gọn (dưới 80 từ), đi thẳng vào vấn đề

        VÍ DỤ GIỌNG NÓI TỰ NHIÊN:
        - "Ồ, bạn đang tìm việc Backend Intern à? Để mình xem nhé! 🔍"
        - "Mình tìm được mấy vị trí khá phù hợp với bạn nè 👇"
        - "Tiếc quá, hiện tại chưa có vị trí nào khớp. Bạn thử mở rộng tìm kiếm xem sao? 😊"
        - "Chấm điểm CV hả? JCO có tính năng này luôn nè, hay lắm! ✨"

        DANH SÁCH CÔNG VIỆC ĐANG TUYỂN (format: ID | Chức danh | Địa điểm | Ngành | Kinh nghiệm | Lương):
        %s

        Trả về JSON:
        {
          "intent": "FIND_JOB | ASK_CV | ASK_CV_SCORING | GENERAL",
          "response": "Câu trả lời tự nhiên, thân thiện bằng tiếng Việt",
          "matchedJobIds": [id1, id2, ...]
        }

        PHÂN LOẠI INTENT:
        - FIND_JOB: Muốn tìm/gợi ý việc làm, nhắc đến nghề/vị trí/địa điểm/lương
        - ASK_CV: Hỏi về tạo CV, viết CV (KHÔNG phải chấm điểm)
        - ASK_CV_SCORING: Hỏi về chấm điểm/đánh giá/review CV
        - GENERAL: Câu hỏi chung, hướng dẫn, hoặc ngoài phạm vi

        QUY TẮC CHỌN JOBS (matchedJobIds):
        - CHỈ dùng khi intent = FIND_JOB, còn lại trả mảng rỗng []
        - Chọn tối đa 5 jobs PHÙ HỢP NHẤT, sắp xếp phù hợp nhất trước
        - PHẢI KHỚP ĐÚNG cấp độ: intern/thực tập ≠ senior, junior ≠ manager
        - PHẢI KHỚP ĐÚNG vị trí: frontend ≠ backend, kế toán ≠ nhân sự
        - Nếu KHÔNG có job nào thực sự phù hợp → trả [] (KHÔNG ép chọn job lệch)
        - CHỈ dùng ID có trong danh sách trên, KHÔNG bịa ID

        PHẠM VI (CHỈ hỗ trợ):
        - Tìm việc, ứng tuyển, xem tin tuyển dụng trên JCO
        - Tạo CV, chấm điểm CV, quản lý hồ sơ trên JCO
        - Đăng tin tuyển dụng, quản lý ứng viên trên JCO
        - Hướng dẫn sử dụng website JCO

        NGOÀI PHẠM VI → PHẢI từ chối nhẹ nhàng:
        - Kiến thức chung, lập trình, học tập, khoa học, giải trí...
        - Khi từ chối vẫn phải thân thiện, VD: "Ôi câu này hay nhưng hơi ngoài chuyên môn của mình rồi 😅 Mình giỏi nhất là tìm việc và hỗ trợ CV trên JCO — bạn cần mình giúp gì không?"

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

            // 4. RAG: 1 Gemini call duy nhất — intent + chọn jobs + response
            JsonNode analysis = callRAG(userMessage, chatContext);

            String intent = analysis.path("intent").asText("GENERAL");
            String aiResponse = analysis.path("response").asText("");

            // 5. Xử lý theo intent
            String finalResponse;
            List<Map<String, Object>> jobCards = null;

            switch (intent) {
                case "FIND_JOB":
                    // Lấy matched job IDs từ Gemini, map về card data
                    jobCards = resolveMatchedJobs(analysis.path("matchedJobIds"));

                    if (jobCards.isEmpty()) {
                        finalResponse = !aiResponse.isEmpty() ? aiResponse
                            : "Xin lỗi, mình không tìm thấy công việc phù hợp. Bạn thử từ khóa khác nhé!";
                        finalResponse += "\n\n**Xem tất cả việc làm:** [Tìm việc làm ở đây!!!](" + publicUrl("/tim-viec-lam.html") + ")";
                    } else {
                        String intro = !aiResponse.isEmpty() ? aiResponse
                            : "Mình tìm thấy một số công việc phù hợp cho bạn:";
                        finalResponse = intro + "\n<!--JOBS" + objectMapper.writeValueAsString(jobCards) + "JOBS-->";
                    }
                    break;

                case "ASK_CV":
                    finalResponse = !aiResponse.isEmpty() ? aiResponse
                        : "Bạn có thể tạo CV chuyên nghiệp với JCO!";
                    finalResponse += "\n\n**Tạo CV:** [Tạo CV ở đây!!!](" + publicUrl("/tao-cv-ai.html") + ")";
                    break;

                case "ASK_CV_SCORING":
                    finalResponse = !aiResponse.isEmpty() ? aiResponse
                        : "Bạn có thể chấm điểm CV của mình bằng AI trên JCO!";
                    finalResponse += "\n\n**Chấm điểm CV với AI:** [Chấm điểm CV ở đây!!!](" + publicUrl("/cham-diem-cv.html") + ")";
                    break;

                default: // GENERAL
                    finalResponse = !aiResponse.isEmpty() ? aiResponse
                        : "Mình có thể giúp bạn tìm việc, tạo CV, hoặc hướng dẫn sử dụng website JCO. Bạn cần hỗ trợ gì?";
                    break;
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
            // Build danh sách jobs cho prompt
            String jobListText = buildJobListForPrompt();
            System.out.println("=== RAG DEBUG ===");
            System.out.println("Job list for prompt (" + jobListText.split("\n").length + " jobs):");
            System.out.println(jobListText);

            String prompt = String.format(RAG_PROMPT_TEMPLATE, jobListText) +
                "\n\nLỊCH SỬ ĐỐI THOẠI:\n" + chatContext +
                "\n\nTIN NHẮN HIỆN TẠI: " + userMessage;

            System.out.println("Calling Gemini with prompt length: " + prompt.length());
            String jsonResponse = geminiService.callGeminiJson(prompt);
            System.out.println("Gemini RAG response: " + jsonResponse);

            // Dọn markdown wrapper nếu có
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            JsonNode result = objectMapper.readTree(jsonResponse);
            System.out.println("Parsed intent: " + result.path("intent").asText());
            System.out.println("Parsed matchedJobIds: " + result.path("matchedJobIds"));
            System.out.println("=== END RAG DEBUG ===");
            return result;

        } catch (Exception e) {
            System.err.println("Error in RAG call: " + e.getMessage());
            // Fallback: gọi Gemini plain text
            try {
                String fallbackResponse = geminiService.callGeminiPlainText(
                    SYSTEM_PROMPT + "\n\nLỊCH SỬ:\n" + chatContext + "\n\nCÂU HỎI: " + userMessage);
                return objectMapper.createObjectNode()
                    .put("intent", "GENERAL")
                    .put("response", fallbackResponse);
            } catch (Exception ex) {
                return objectMapper.createObjectNode()
                    .put("intent", "GENERAL")
                    .put("response", "Xin lỗi, mình đang gặp sự cố. Bạn vui lòng thử lại sau nhé!");
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
