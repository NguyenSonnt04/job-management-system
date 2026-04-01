package Nhom08.Project.service;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    @Autowired private InterviewSessionRepository    sessionRepo;
    @Autowired private InterviewMessageRepository    messageRepo;
    @Autowired private InterviewResultRepository     resultRepo;
    @Autowired private InterviewRoleRepository       roleRepo;
    @Autowired private InterviewLevelRepository      levelRepo;
    @Autowired private InterviewTypeRepository       typeRepo;
    @Autowired private InterviewQuestionBankRepository questionRepo;
    @Autowired private ClaudeService                 claudeService;

    private final ObjectMapper mapper = new ObjectMapper();

    // ================================================================
    // REFERENCE DATA
    // ================================================================

    public List<InterviewRole>  getAllActiveRoles()  { return roleRepo.findByActiveTrueOrderByDisplayOrderAsc(); }
    public List<InterviewLevel> getAllActiveLevels() { return levelRepo.findByActiveTrueOrderByDisplayOrderAsc(); }
    public List<InterviewType>  getAllActiveTypes()  { return typeRepo.findByActiveTrueOrderByDisplayOrderAsc(); }

    public List<InterviewQuestionBank> getQuestionsForSession(String typeKey, String roleKey, int limit) {
        return questionRepo.findRandomByTypeAndRole(typeKey, roleKey, limit);
    }

    // ================================================================
    // SESSION MANAGEMENT
    // ================================================================

    /**
     * Tạo phiên phỏng vấn mới, trả về sessionId.
     */
    @Transactional
    public InterviewSession createSession(User user, String mode, String roleKey,
                                          String levelKey, String typeKey,
                                          String cvFileName, boolean hasCv) {
        String roleName = roleRepo.findByRoleKey(roleKey)
                .map(InterviewRole::getRoleName)
                .orElse(roleKey);

        InterviewSession session = new InterviewSession(user, mode, roleName, roleKey, levelKey, typeKey);
        session.setCvFileName(cvFileName);
        session.setHasCv(hasCv);
        return sessionRepo.save(session);
    }

    /**
     * Đánh dấu phiên là completed, tính duration.
     */
    @Transactional
    public InterviewSession completeSession(Long sessionId) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session không tồn tại: " + sessionId));
        session.setStatus("completed");
        session.setCompletedAt(LocalDateTime.now());
        if (session.getCreatedAt() != null) {
            long seconds = java.time.Duration.between(session.getCreatedAt(), session.getCompletedAt()).getSeconds();
            session.setDurationSeconds((int) seconds);
        }
        return sessionRepo.save(session);
    }

    // ================================================================
    // AI CHAT — gọi Claude, lưu message, trả về reply
    // ================================================================

    /**
     * Gọi Claude AI với lịch sử hội thoại, lưu message mới vào DB.
     */
    @Transactional
    public String chat(Long sessionId, List<Map<String, String>> messages, String cvContext) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session không tồn tại: " + sessionId));

        String systemPrompt = buildSystemPrompt(session, cvContext);
        String reply = claudeService.chat(systemPrompt, messages, 800);

        // Lưu tin nhắn user (tin nhắn cuối trong danh sách)
        if (!messages.isEmpty()) {
            Map<String, String> lastUserMsg = messages.get(messages.size() - 1);
            if ("user".equals(lastUserMsg.get("role"))) {
                int order = (int) messageRepo.countBySessionId(sessionId) + 1;
                InterviewMessage userMsg = new InterviewMessage(
                        session, "user", lastUserMsg.get("content"), order);
                messageRepo.save(userMsg);
            }
        }

        // Lưu tin nhắn assistant
        int order = (int) messageRepo.countBySessionId(sessionId) + 1;
        InterviewMessage assistantMsg = new InterviewMessage(session, "assistant", reply, order);
        messageRepo.save(assistantMsg);

        return reply;
    }

    // ================================================================
    // KẾT THÚC PHIÊN — đánh giá + lưu kết quả
    // ================================================================

    /**
     * Gọi Claude đánh giá toàn bộ phiên, parse JSON, lưu InterviewResult.
     */
    @Transactional
    public InterviewResult endAndEvaluate(Long sessionId, List<Map<String, String>> messages) {
        InterviewSession session = completeSession(sessionId);

        String evalPrompt   = buildEvalSystemPrompt(session);
        String evalUserMsg  = buildEvalUserMessage(messages);
        List<Map<String, String>> evalMessages = List.of(
                Map.of("role", "user", "content", evalUserMsg));

        String raw = claudeService.chat(evalPrompt, evalMessages, 1200);

        // Parse JSON từ Claude
        InterviewResult result = new InterviewResult();
        result.setSession(session);
        result.setRawAiResponse(raw);

        try {
            // Xử lý trường hợp Claude bọc JSON trong markdown code block
            String json = raw.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode root = mapper.readTree(json);

            result.setOverallScore(root.path("overallScore").asInt(5));
            result.setScoreLabel(root.path("scoreLabel").asText("Trung bình"));
            result.setRecommendation(root.path("recommendation").asText(""));

            JsonNode cats = root.path("categories");
            result.setScoreCommunication(cats.path("communication").asInt(0));
            result.setScoreKnowledge(cats.path("knowledge").asInt(0));
            result.setScoreProblemSolving(cats.path("problemSolving").asInt(0));
            result.setScoreAttitude(cats.path("attitude").asInt(0));

            // Lưu strengths / improvements dạng JSON string
            result.setStrengths(mapper.writeValueAsString(
                    toStringList(root.path("strengths"))));
            result.setImprovements(mapper.writeValueAsString(
                    toStringList(root.path("improvements"))));

        } catch (Exception ex) {
            // Không parse được — giữ raw, đặt điểm mặc định
            result.setOverallScore(0);
            result.setScoreLabel("Lỗi phân tích");
        }

        return resultRepo.save(result);
    }

    // ================================================================
    // LỊCH SỬ PHỎNG VẤN
    // ================================================================

    public List<InterviewSession> getHistoryByUser(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<InterviewSession> getSessionById(Long sessionId) {
        return sessionRepo.findById(sessionId);
    }

    public Optional<InterviewResult> getResultBySession(Long sessionId) {
        return resultRepo.findBySessionId(sessionId);
    }

    public List<InterviewMessage> getMessagesBySession(Long sessionId) {
        return messageRepo.findBySessionIdOrderByMessageOrderAsc(sessionId);
    }

    /**
     * Điểm trung bình của user (dùng cho profile / dashboard).
     */
    public Double getAverageScoreByUser(Long userId) {
        return resultRepo.findAvgScoreByUserId(userId);
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private String buildSystemPrompt(InterviewSession session, String cvContext) {
        String typeDesc = switch (session.getTypeKey()) {
            case "hr"         -> "HR và soft skills (tính cách, thái độ, văn hóa công ty)";
            case "technical"  -> "chuyên môn kỹ thuật sâu theo vị trí";
            case "behavioral" -> "behavioral theo cấu trúc STAR (tình huống thực tế)";
            default           -> "tổng hợp gồm HR, chuyên môn và behavioral";
        };

        String cvSection = (session.getHasCv() && cvContext != null && !cvContext.isBlank())
                ? "\n\nThông tin CV ứng viên:\n" + cvContext +
                  "\nHãy cá nhân hóa câu hỏi dựa trên kinh nghiệm và kỹ năng trong CV này."
                : "";

        return """
            Bạn là một nhà tuyển dụng chuyên nghiệp, đang phỏng vấn ứng viên cho vị trí %s ở cấp độ %s.
            Phong cách phỏng vấn: %s.
            Nguyên tắc:
            - Mỗi lượt chỉ hỏi MỘT câu hỏi duy nhất, ngắn gọn và rõ ràng.
            - Sau khi ứng viên trả lời, đưa ra nhận xét ngắn (2-3 câu): điểm tốt, điểm cần cải thiện.
            - Sau nhận xét, hỏi câu tiếp theo có liên quan hoặc đi sâu hơn.
            - Sử dụng tiếng Việt xuyên suốt. Giọng điệu chuyên nghiệp nhưng thân thiện.
            - KHÔNG liệt kê nhiều câu hỏi cùng lúc. KHÔNG dùng markdown (không dùng **, ##).
            - Khi bắt đầu, hãy chào ứng viên và hỏi câu hỏi đầu tiên ngay.%s
            """.formatted(session.getRoleName(), session.getLevelKey(), typeDesc, cvSection);
    }

    private String buildEvalSystemPrompt(InterviewSession session) {
        return """
            Bạn là chuyên gia đánh giá phỏng vấn. Phân tích toàn bộ cuộc phỏng vấn và trả về JSON.
            Vị trí: %s — Cấp độ: %s.
            Trả về ĐÚNG format JSON sau (không markdown, không giải thích thêm):
            {
              "overallScore": <số từ 1-10>,
              "scoreLabel": "<Xuất sắc|Khá tốt|Trung bình|Cần cải thiện>",
              "strengths": ["<điểm mạnh 1>", "<điểm mạnh 2>", "<điểm mạnh 3>"],
              "improvements": ["<cần cải thiện 1>", "<cần cải thiện 2>"],
              "recommendation": "<1-2 câu nhận xét tổng thể và lời khuyên cụ thể>",
              "categories": {
                "communication": <1-10>,
                "knowledge": <1-10>,
                "problemSolving": <1-10>,
                "attitude": <1-10>
              }
            }
            """.formatted(session.getRoleName(), session.getLevelKey());
    }

    private String buildEvalUserMessage(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder("Đây là toàn bộ nội dung cuộc phỏng vấn:\n\n");
        for (Map<String, String> msg : messages) {
            String label = "user".equals(msg.get("role")) ? "Ứng viên" : "Interviewer";
            sb.append(label).append(": ").append(msg.get("content")).append("\n\n");
        }
        sb.append("Hãy đánh giá toàn bộ phần trả lời của ứng viên theo format JSON đã yêu cầu.");
        return sb.toString();
    }

    private List<String> toStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }
}
