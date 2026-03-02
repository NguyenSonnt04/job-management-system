package Nhom08.Project.controller;

import Nhom08.Project.entity.Notification;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.NotificationRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationRepository notificationRepo;
    @Autowired private UserRepository userRepo;

    /**
     * GET /api/notifications
     * Lấy danh sách thông báo của user đang đăng nhập
     */
    @GetMapping
    public ResponseEntity<?> getMyNotifications(Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));

        List<Notification> notifications = notificationRepo.findByUserIdOrderByCreatedAtDesc(userOpt.get().getId());
        long unreadCount = notificationRepo.countByUserIdAndReadFalse(userOpt.get().getId());

        List<Map<String, Object>> result = notifications.stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",        n.getId());
            m.put("title",     n.getTitle());
            m.put("message",   n.getMessage());
            m.put("type",      n.getType());
            m.put("read",      n.isRead());
            m.put("jobId",     n.getJobId());
            m.put("jobTitle",  n.getJobTitle());
            m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "notifications", result,
            "unreadCount",   unreadCount
        ));
    }

    /**
     * PATCH /api/notifications/read-all
     * Đánh dấu tất cả đã đọc
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));

        notificationRepo.markAllReadByUserId(userOpt.get().getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Đánh dấu 1 notification đã đọc
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));

        return notificationRepo.findById(id).map(n -> {
            n.setRead(true);
            notificationRepo.save(n);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Optional<User> getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepo.findByEmail(auth.getName());
    }
}
