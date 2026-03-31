package Nhom08.Project.controller;

import Nhom08.Project.entity.ChatMessage;
import Nhom08.Project.entity.ChatSession;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.UserRepository;
import Nhom08.Project.service.ContactChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho contact widget chatbot
 * Cung cấp API endpoints cho frontend
 */
@RestController
@RequestMapping("/api/contact-chat")
public class ContactWidgetController {

    @Autowired
    private ContactChatbotService chatbotService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper: Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("=== getCurrentUser Debug ===");
        System.out.println("Auth object: " + (auth != null ? auth.getClass().getName() : "null"));
        System.out.println("Authenticated: " + (auth != null && auth.isAuthenticated()));
        System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));
        System.out.println("Name: " + (auth != null ? auth.getName() : "null"));

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Not authenticated");
        }

        String email = auth.getName();
        System.out.println("Looking for user with email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Tạo hoặc lấy session chat hiện có
     * POST /api/contact-chat/session
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createOrGetSession(
            @RequestParam(required = false) String existingSessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy user hiện tại (có thể null cho guest)
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
                System.out.println("✅ Successfully got current user: " + currentUser.getEmail());
            } catch (Exception e) {
                // Guest user - log the error
                System.out.println("⚠️ No authenticated user found (Guest): " + e.getMessage());
            }

            System.out.println("API: Creating/getting session for user: " +
                (currentUser != null ? currentUser.getEmail() + " (ID: " + currentUser.getId() + ")" : "Guest"));

            // Tạo session mới hoặc lấy session cũ
            ChatSession session = chatbotService.createOrGetSession(currentUser, existingSessionId);

            response.put("success", true);
            response.put("sessionId", session.getSessionId());
            response.put("status", session.getStatus());
            response.put("createdAt", session.getCreatedAt());
            response.put("messageCount", session.getMessages() != null ? session.getMessages().size() : 0);

            // Thêm user info vào response để debug
            response.put("userId", currentUser != null ? currentUser.getId() : null);
            response.put("userEmail", currentUser != null ? currentUser.getEmail() : null);

            System.out.println("✅ Session created/retrieved. Session ID: " + session.getSessionId() +
                             ", User: " + (session.getUser() != null ? session.getUser().getEmail() : "Guest"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in createOrGetSession: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Gửi tin nhắn
     * POST /api/contact-chat/message
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = request.get("sessionId");
            String message = request.get("message");

            if (sessionId == null || message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "sessionId và message không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Lấy user hiện tại (có thể null cho guest)
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (Exception e) {
                // Guest user - ignore error
            }

            Map<String, Object> result = chatbotService.sendMessage(sessionId, message, currentUser);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lấy lịch sử chat
     * GET /api/contact-chat/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> getChatHistory(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== Controller: getChatHistory Called ===");
            System.out.println("Session ID from path: " + sessionId);

            // Lấy user hiện tại (có thể null cho guest)
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
                System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "Guest"));
            } catch (Exception e) {
                // Guest user - ignore error
                System.out.println("Current user: Guest (exception)");
            }

            // Kiểm tra session tồn tại và user có quyền truy cập không
            if (!chatbotService.canUserAccessSession(sessionId, currentUser)) {
                System.out.println("❌ Access denied for session " + sessionId);
                response.put("success", false);
                response.put("error", "Bạn không có quyền truy cập session này");
                return ResponseEntity.status(403).body(response);
            }

            List<ChatMessage> messages = chatbotService.getChatHistory(sessionId);

            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("messages", messages);

            System.out.println("✅ Controller returning " + messages.size() + " messages");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in getChatHistory: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/contact-chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Contact Widget Chatbot");

        // Tự động cleanup sessions cũ (gọi ngẫu nhiên, khoảng 10%)
        if (Math.random() < 0.1) {
            int deleted = chatbotService.cleanupOldSessions();
            response.put("cleaned", String.valueOf(deleted) + " sessions");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa session hiện tại
     * DELETE /api/contact-chat/session
     */
    @DeleteMapping("/session")
    public ResponseEntity<Map<String, Object>> deleteSession(
            @RequestParam String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy user hiện tại (có thể null cho guest)
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (Exception e) {
                // Guest user - ignore error
            }

            // Kiểm tra user có quyền xóa session này không
            if (!chatbotService.canUserAccessSession(sessionId, currentUser)) {
                response.put("success", false);
                response.put("error", "Bạn không có quyền xóa session này");
                return ResponseEntity.status(403).body(response);
            }

            chatbotService.deleteSession(sessionId);

            response.put("success", true);
            response.put("message", "Đã xóa lịch sử chat thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
