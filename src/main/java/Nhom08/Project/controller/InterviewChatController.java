package Nhom08.Project.controller;

import Nhom08.Project.entity.InterviewPromptTemplate;
import Nhom08.Project.repository.InterviewPromptTemplateRepository;
import Nhom08.Project.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for AI mock interview chat (stateless mode — no session).
 * POST /api/interview/chat  — sends conversation history, returns AI's next message.
 * POST /api/interview/end   — sends full conversation, returns structured final feedback.
 */
@RestController
@RequestMapping("/api/interview")
public class InterviewChatController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private InterviewPromptTemplateRepository promptRepo;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/interview/chat
    // Body: { role, level, type, hasCV, cvContext, messages: [{role,content}] }
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> req) {
        try {
            String role           = str(req, "role",           "Vị trí chưa xác định");
            String level          = str(req, "level",          "Fresher");
            String type           = str(req, "type",           "mixed");
            String cvContext      = str(req, "cvContext",      "");
            String interviewStyle = str(req, "interviewStyle", "standard");
            boolean hasCV         = Boolean.TRUE.equals(req.get("hasCV"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages =
                (List<Map<String, String>>) req.getOrDefault("messages", List.of());

            String system = buildSystemPrompt(role, level, type, hasCV, cvContext, interviewStyle);
            List<Map<String, String>> effectiveMessages = messages.isEmpty()
                    ? List.of(Map.of("role", "user", "content", "Bắt đầu phỏng vấn"))
                    : messages;
            String reply  = geminiService.chat(system, effectiveMessages);

            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/interview/end
    // Body: same as /chat — returns structured JSON feedback for the session
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endSession(@RequestBody Map<String, Object> req) {
        try {
            String role  = str(req, "role",  "Vị trí chưa xác định");
            String level = str(req, "level", "Fresher");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages =
                (List<Map<String, String>>) req.getOrDefault("messages", List.of());

            String evalPrompt = buildEvalSystemPrompt(role, level);
            String evalMsg    = buildEvalUserMessage(messages);

            List<Map<String, String>> evalMessages =
                List.of(Map.of("role", "user", "content", evalMsg));

            String raw = geminiService.chat(evalPrompt, evalMessages);

            // Try to parse JSON; if not valid, wrap as raw text
            Map<String, Object> result;
            try {
                result = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(raw, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            } catch (Exception ex) {
                result = Map.of("raw", raw);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ── Prompt helpers — đọc từ DB, fallback hardcode ──────────────────────

    private String getPrompt(String key) {
        return promptRepo.findByPromptKeyAndActiveTrue(key)
                .map(InterviewPromptTemplate::getPromptContent)
                .orElse(null);
    }

    private String buildSystemPrompt(String role, String level, String type,
                                      boolean hasCV, String cvContext, String interviewStyle) {
        String typeDesc = getPrompt("type_" + type);
        if (typeDesc == null) {
            typeDesc = switch (type) {
                case "hr"         -> "HR và soft skills (tính cách, thái độ, văn hóa công ty)";
                case "technical"  -> "chuyên môn kỹ thuật sâu theo vị trí";
                case "behavioral" -> "behavioral theo cấu trúc STAR (tình huống thực tế)";
                default           -> "tổng hợp gồm HR, chuyên môn và behavioral";
            };
        }

        String styleDesc = getPrompt("style_" + interviewStyle);
        if (styleDesc == null) {
            styleDesc = switch (interviewStyle) {
                case "techlead" -> "Bạn là Tech Lead / Senior Engineer 10+ năm kinh nghiệm. Hỏi sâu vào kỹ thuật, " +
                    "phản biện câu trả lời chưa chính xác, đặt follow-up để kiểm tra hiểu biết thực sự. " +
                    "Giọng nghiêm túc, ít khen chung chung.";
                case "startup"  -> "Bạn là Founder/CTO startup đang scale nhanh. Hỏi thẳng vào thực tế, " +
                    "kiểm tra tự học, chịu áp lực. Đánh giá mindset hơn bằng cấp. Giọng nhanh, thực dụng.";
                case "strict"   -> "Bạn là interviewer chuẩn FAANG. Nghiêm khắc, ít gợi ý, yêu cầu chính xác. " +
                    "Không chấp nhận câu trả lời mơ hồ. Không khen nếu chưa xứng đáng.";
                default         -> "Bạn là nhà tuyển dụng chuyên nghiệp, cân bằng và thân thiện. " +
                    "Đặt câu hỏi rõ ràng, nhận xét mang tính xây dựng.";
            };
        }

        String levelGuide = getPrompt("level_" + level.toLowerCase());
        if (levelGuide == null) {
            levelGuide = switch (level.toLowerCase()) {
                case "intern" -> "\n\nHướng dẫn đặc biệt cho Intern:\n" +
                    "- Bắt đầu bằng câu hỏi giới thiệu bản thân.\n" +
                    "- Hỏi 1-2 câu OOP cơ bản, 1-2 câu DSA cơ bản.\n" +
                    "- Nếu có CV/project, hỏi sâu vào project đã làm.\n" +
                    "- Giọng nhẹ nhàng, khuyến khích.";
                default -> "";
            };
        }

        String cvSection = hasCV && !cvContext.isBlank()
            ? "\n\nThông tin CV ứng viên:\n" + cvContext +
              "\nHãy cá nhân hóa câu hỏi dựa trên kinh nghiệm và kỹ năng trong CV này."
            : "";

        String mainTemplate = getPrompt("system_main");
        if (mainTemplate != null) {
            return mainTemplate
                    .replace("{{role}}", role)
                    .replace("{{level}}", level)
                    .replace("{{typeDesc}}", typeDesc)
                    .replace("{{styleDesc}}", styleDesc)
                    .replace("{{levelGuide}}", levelGuide)
                    .replace("{{cvSection}}", cvSection);
        }

        return """
            Bạn là AI Interviewer của JCO (Job Connection Online). Bạn đang phỏng vấn ứng viên cho vị trí %s ở cấp độ %s.
            Loại phỏng vấn: %s.

            Phong cách của bạn: %s

            Nguyên tắc bất biến:
            - Khi bắt đầu, hãy giới thiệu: "Xin chào, tôi là AI Interviewer của JCO." rồi giới thiệu ngắn gọn cuộc phỏng vấn và hỏi câu đầu tiên.
            - KHÔNG bao giờ tự đặt tên người thật cho mình (ví dụ: [Tên của tôi], Minh, Hùng...). Luôn xưng là "tôi" hoặc "AI Interviewer của JCO".
            - Mỗi lượt chỉ hỏi MỘT câu hỏi duy nhất.
            - Sau khi ứng viên trả lời, đưa ra nhận xét ngắn (2-3 câu) rồi hỏi tiếp.
            - Sử dụng tiếng Việt xuyên suốt.
            - KHÔNG liệt kê nhiều câu hỏi cùng lúc. KHÔNG dùng markdown (không dùng **, ##).%s%s
            """.formatted(role, level, typeDesc, styleDesc, levelGuide, cvSection);
    }

    private String buildEvalSystemPrompt(String role, String level) {
        String evalTemplate = getPrompt("eval_system");
        if (evalTemplate != null) {
            return evalTemplate
                    .replace("{{role}}", role)
                    .replace("{{level}}", level);
        }

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
            """.formatted(role, level);
    }

    private String buildEvalUserMessage(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder("Đây là toàn bộ nội dung cuộc phỏng vấn:\n\n");
        for (Map<String, String> msg : messages) {
            String roleLabel = "user".equals(msg.get("role")) ? "Ứng viên" : "Interviewer";
            sb.append(roleLabel).append(": ").append(msg.get("content")).append("\n\n");
        }
        sb.append("Hãy đánh giá toàn bộ phần trả lời của ứng viên theo format JSON đã yêu cầu.");
        return sb.toString();
    }

    private String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return (v instanceof String s && !s.isBlank()) ? s : def;
    }
}
