package Nhom08.Project.controller;

import Nhom08.Project.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * POST /api/cv/parse-profile
 * Accepts a CV file (PDF/image), sends to Gemini AI for structured extraction.
 * Returns: name, email, phone, dob, occupation, skills (categorized JSON), education, experience.
 */
@RestController
@RequestMapping("/api/cv")
public class CvParseController {

    private final GeminiService geminiService;
    private final ObjectMapper  objectMapper = new ObjectMapper();

    public CvParseController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/parse-profile")
    public ResponseEntity<Map<String, Object>> parseProfile(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống."));
        }

        String mimeType = file.getContentType();
        if (mimeType == null) mimeType = "application/pdf";
        if (!mimeType.startsWith("application/pdf") && !mimeType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ hỗ trợ file PDF hoặc ảnh CV."));
        }

        String prompt = """
            Bạn là chuyên gia phân tích CV chuyên sâu. Đọc kỹ tài liệu CV đính kèm, trích xuất thông tin một cách đầy đủ và chính xác.
            
            Trả về ĐÚNG định dạng JSON dưới đây, không markdown, không giải thích thêm:
            {
              "name": "<họ và tên đầy đủ, Title Case>",
              "email": "<địa chỉ email cá nhân>",
              "phone": "<số điện thoại, chỉ số và dấu + nếu có>",
              "dob": "<ngày sinh DD/MM/YYYY hoặc chuỗi rỗng>",
              "occupation": "<vị trí/chức danh ứng viên, ví dụ: Backend Developer Intern>",
              "skills": {
                "<Tên nhóm viết hoa, ví dụ: BACKEND>": ["<skill 1>", "<skill 2>"],
                "<Tên nhóm 2, ví dụ: FRONTEND>": ["<skill 1>", "<skill 2>"]
              },
              "education": "<tên trường, chuyên ngành, năm, GPA nếu có>",
              "experience": "<kinh nghiệm LÀM VIỆC THỰC TẾ: tên công ty, vị trí, thời gian, mô tả. Chuỗi rỗng nếu không có>"
            }
            
            QUY TẮC QUAN TRỌNG:
            - "skills" là object JSON, key là tên nhóm kỹ năng VIẾT HOA (BACKEND, FRONTEND, DATABASE, DEVOPS & TOOLS, NGÔN NGỮ...), value là mảng tên kỹ năng ngắn gọn.
            - Phân loại: Java, Spring Boot → BACKEND; React, HTML/CSS → FRONTEND; MySQL, MongoDB → DATABASE; Docker, Git → DEVOPS & TOOLS; Tiếng Anh → NGÔN NGỮ.
            - Mỗi kỹ năng chỉ là tên (Java, Spring Boot...), KHÔNG viết mô tả.
            - "experience" chỉ chứa kinh nghiệm làm việc tại công ty thực tế. Chuỗi rỗng nếu không có.
            - Nếu không có kỹ năng nào, skills = {}.
            - Chuyển "name" sang Title Case.
            - Chỉ trả về đúng 7 trường trên, không thêm bất kỳ trường nào khác.
            """;

        try {
            byte[] fileBytes = file.getBytes();
            String rawJson   = geminiService.scoreCvWithPrompt(fileBytes, mimeType, prompt);


            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(rawJson, Map.class);

            // Serialize skills object → JSON string for DB storage
            Object skillsRaw = parsed.get("skills");
            if (skillsRaw instanceof Map || skillsRaw instanceof java.util.List) {
                parsed.put("skills", objectMapper.writeValueAsString(skillsRaw));
            } else if (skillsRaw == null) {
                parsed.put("skills", "{}");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", parsed);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Không thể phân tích CV: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
