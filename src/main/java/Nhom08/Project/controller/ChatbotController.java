package Nhom08.Project.controller;

import Nhom08.Project.entity.ChatHistory;
import Nhom08.Project.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho Chatbot
 * Frontend gọi: /api/chatbot/ask
 */
@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    /**
     * POST /api/chatbot/ask
     * Xử lý câu hỏi từ user
     * Body: { "question": "text", "sessionId": "optional" }
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String question = request.get("question");
        String sessionId = request.get("sessionId");

        // Validate
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Question cannot be empty"
            ));
        }

        // Process
        Map<String, Object> response = chatbotService.processQuestion(question, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/chatbot/quick-questions
     * Lấy danh sách câu hỏi nhanh
     */
    @GetMapping("/quick-questions")
    public ResponseEntity<List<Map<String, Object>>> getQuickQuestions() {
        return ResponseEntity.ok(chatbotService.getQuickQuestions());
    }

    /**
     * GET /api/chatbot/categories
     * Lấy danh sách categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(chatbotService.getCategories());
    }

    /**
     * GET /api/chatbot/history/{sessionId}
     * Lấy lịch sử chat của session
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatHistory>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getSessionHistory(sessionId));
    }

    /**
     * GET /api/chatbot/health
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ChatbotAPI",
            "version", "1.0"
        ));
    }

    /**
     * POST /api/chatbot/feedback
     * Gửi feedback (để lưu nếu muốn mở rộng)
     */
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> feedback(@RequestBody Map<String, Object> request) {
        // Có thể mở rộng để lưu feedback vào database
        Integer conversationId = (Integer) request.get("conversationId");
        Boolean isHelpful = (Boolean) request.get("isHelpful");
        String feedback = (String) request.get("feedback");

        // TODO: Lưu feedback vào database nếu cần

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Thank you for your feedback!"
        ));
    }
}
