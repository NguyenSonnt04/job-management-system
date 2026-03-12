package Nhom08.Project.controller;

import Nhom08.Project.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints for the AI-powered CV Builder chat in cv-editor.html.
 *
 * POST /api/cv-ai/acknowledge   – Gemini acknowledges a user's answer naturally
 * POST /api/cv-ai/generate-cv   – Gemini formats all collected data into a polished CV JSON
 */
@RestController
@RequestMapping("/api/cv-ai")
public class CvAiController {

    private final GeminiService geminiService;
    private final ObjectMapper  objectMapper = new ObjectMapper();

    public CvAiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Acknowledges a single user answer conversationally.
     * Body: { "step": "name", "answer": "Nguyễn Văn A" }
     * Returns: { "message": "Xin chào Nguyễn Văn A! ..." }
     */
    @PostMapping("/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledge(@RequestBody Map<String, String> body) {
        String step   = body.getOrDefault("step", "");
        String answer = body.getOrDefault("answer", "");

        String prompt = """
            Bạn là trợ lý AI thân thiện giúp người dùng tạo CV chuyên nghiệp bằng tiếng Việt.
            Người dùng vừa trả lời câu hỏi về "%s" với nội dung: "%s"
            
            Hãy:
            1. Xác nhận thông tin đã nhận một cách tự nhiên, thân thiện (1-2 câu ngắn)
            2. Không cần hỏi thêm gì
            3. Trả lời THUẦN TIẾNG VIỆT, ngắn gọn, không dùng markdown
            
            Ví dụ tốt: "Tuyệt! Tôi đã ghi nhận thông tin của bạn."
            """.formatted(step, answer);

        try {
            String reply = geminiService.callGeminiPlainText(prompt);
            // Clean up potential markdown formatting
            reply = reply.replaceAll("\\*\\*", "").replaceAll("\\*", "").trim();
            if (reply.length() > 200) reply = reply.substring(0, 200);

            Map<String, Object> result = new HashMap<>();
            result.put("message", reply);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("message", "Đã ghi nhận! Tiếp tục nhé.");
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Generates a polished, professional CV JSON from all collected raw data.
     * Body: { rawData: { name, subtitle, email, phone, address, summary,
     *                    education[], experience[], projects[], skills[] } }
     * Returns: { cvJson: { ...structured CV... } }
     */
    @PostMapping("/generate-cv")
    public ResponseEntity<Map<String, Object>> generateCv(@RequestBody Map<String, Object> body) {
        Object rawData = body.get("rawData");

        String prompt = """
            Bạn là chuyên gia viết CV hàng đầu cho MỌI ngành nghề (Y tế, Giáo dục, Luật, IT, Nghệ thuật, Kinh doanh, v.v.).
            Dựa trên thông tin thô dưới đây của người dùng, hãy tạo ra một CV chuyên nghiệp, ấn tượng.
            
            THÔNG TIN NGƯỜI DÙNG:
            %s
            
            NHIỆM VỤ:
            1. Viết lại phần "summary" thành đoạn văn chuyên nghiệp 3-5 câu, nêu bật điểm mạnh và phù hợp với ngành nghề.
            2. Chuyển các mô tả kinh nghiệm/dự án thành bullet point cụ thể, có thể thêm số liệu nếu phù hợp.
            3. Phân tích thông tin chứng chỉ/giải thưởng/hoạt động từ dữ liệu thô và điền vào đúng field.
            4. GIỮ NGUYÊN tất cả trường dữ liệu (không bỏ trường nào, kể cả trường rỗng).
            5. Ngôn ngữ nhất quán (tiếng Việt).
            
            TRẢ VỀ ĐÚNG format JSON sau (không markdown, không giải thích):
            {
              "name": "string",
              "subtitle": "string",
              "email": "string",
              "phone": "string",
              "address": "string",
              "linkedin": "string",
              "portfolio": "string",
              "summary": "string",
              "education": [
                { "school": "string", "location": "string", "degree": "string", "period": "string", "details": ["string"] }
              ],
              "experience": [
                { "company": "string", "location": "string", "role": "string", "period": "string", "details": ["string"] }
              ],
              "projects": [
                { "name": "string", "period": "string", "tech": "string", "github": "string", "details": ["string"] }
              ],
              "skills": [
                { "category": "string", "items": ["string"] }
              ],
              "certifications": [
                { "name": "string", "issuer": "string", "year": "string" }
              ],
              "awards": [
                { "name": "string", "year": "string" }
              ],
              "activities": [
                { "name": "string", "role": "string", "period": "string", "details": ["string"] }
              ]
            }
            """.formatted(rawDataToString(rawData));

        try {
            String jsonStr = geminiService.callGeminiJson(prompt);
            // Parse and re-serialize to validate JSON
            Object parsed = objectMapper.readValue(jsonStr, Object.class);

            Map<String, Object> result = new HashMap<>();
            result.put("cvJson", parsed);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Không thể tạo CV: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    private String rawDataToString(Object rawData) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
        } catch (Exception e) {
            return String.valueOf(rawData);
        }
    }

    /**
     * AI-powered targeted edit on the existing CV.
     * Body: { "request": "thêm kỹ năng Python", "currentCv": { ...cvJson... } }
     * Returns: { "message": "...", "cvJson": { ...updated cv... } }
     */
    @PostMapping("/edit")
    public ResponseEntity<Map<String, Object>> editCv(@RequestBody Map<String, Object> body) {
        String userRequest = String.valueOf(body.getOrDefault("request", ""));
        Object currentCv   = body.get("currentCv");

        String cvStr;
        try { cvStr = objectMapper.writeValueAsString(currentCv); }
        catch (Exception e) { cvStr = String.valueOf(currentCv); }

        // Step 1: Gemini confirms the edit naturally
        String ackPrompt = """
            Bạn là trợ lý AI đang giúp người dùng chỉnh sửa CV.
            Người dùng yêu cầu: "%s"
            Xác nhận sẽ thực hiện yêu cầu này một cách tự nhiên, thân thiện (1–2 câu, tiếng Việt, không dùng markdown).
            """.formatted(userRequest);

        String ackMessage = "Đang cập nhật CV của bạn...";
        try {
            ackMessage = geminiService.callGeminiPlainText(ackPrompt)
                .replaceAll("\\*\\*", "").replaceAll("\\*", "").trim();
            if (ackMessage.length() > 200) ackMessage = ackMessage.substring(0, 200);
        } catch (Exception ignored) {}

        // Step 2: Gemini applies the edit to the CV JSON
        String editPrompt = """
            Bạn là chuyên gia chỉnh sửa CV. Đây là CV hiện tại dưới dạng JSON:
            %s
            
            Yêu cầu chỉnh sửa của người dùng: "%s"
            
            Hãy áp dụng thay đổi đúng theo yêu cầu và trả về TOÀN BỘ CV JSON đã được cập nhật.
            Chỉ thay đổi phần liên quan đến yêu cầu, giữ nguyên mọi trường khác.
            Trả về ĐÚNG format JSON gốc, không markdown, không giải thích.
            """.formatted(cvStr, userRequest);

        try {
            String updatedJson = geminiService.callGeminiJson(editPrompt);
            Object parsed = objectMapper.readValue(updatedJson, Object.class);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", ackMessage);
            result.put("cvJson",  parsed);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không thể chỉnh sửa CV: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
