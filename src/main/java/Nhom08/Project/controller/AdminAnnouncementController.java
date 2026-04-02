package Nhom08.Project.controller;

import Nhom08.Project.entity.SiteAnnouncement;
import Nhom08.Project.repository.SiteAnnouncementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    private final SiteAnnouncementRepository repo;

    public AdminAnnouncementController(SiteAnnouncementRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<SiteAnnouncement>> list() {
        return ResponseEntity.ok(repo.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String title = (String) body.get("title");
            String message = (String) body.get("message");
            if (title == null || title.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tiêu đề không được để trống"));
            }
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nội dung không được để trống"));
            }

            SiteAnnouncement a = new SiteAnnouncement();
            a.setTitle(title.trim());
            a.setMessage(message.trim());

            String type = (String) body.getOrDefault("type", "INFO");
            a.setType(type != null ? type : "INFO");

            Object activeVal = body.get("active");
            a.setActive(activeVal == null || Boolean.TRUE.equals(activeVal));

            String startAtStr = (String) body.get("startAt");
            String endAtStr = (String) body.get("endAt");
            if (startAtStr != null && !startAtStr.isBlank()) {
                a.setStartAt(LocalDateTime.parse(startAtStr));
            }
            if (endAtStr != null && !endAtStr.isBlank()) {
                a.setEndAt(LocalDateTime.parse(endAtStr));
            }

            SiteAnnouncement saved = repo.save(a);
            return ResponseEntity.ok(Map.of("success", true, "announcement", saved));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Không thể tạo thông báo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(a -> {
            try {
                String title = (String) body.get("title");
                String message = (String) body.get("message");
                if (title == null || title.isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tiêu đề không được để trống"));
                }
                if (message == null || message.isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nội dung không được để trống"));
                }
                a.setTitle(title.trim());
                a.setMessage(message.trim());

                String type = (String) body.getOrDefault("type", "INFO");
                a.setType(type != null ? type : "INFO");

                Object activeVal = body.get("active");
                a.setActive(activeVal == null || Boolean.TRUE.equals(activeVal));

                String startAtStr = (String) body.get("startAt");
                String endAtStr   = (String) body.get("endAt");
                a.setStartAt(startAtStr != null && !startAtStr.isBlank() ? LocalDateTime.parse(startAtStr) : null);
                a.setEndAt(endAtStr != null && !endAtStr.isBlank() ? LocalDateTime.parse(endAtStr) : null);

                SiteAnnouncement saved = repo.save(a);
                return ResponseEntity.ok(Map.of("success", true, "announcement", saved));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Không thể cập nhật: " + e.getMessage()));
            }
        }).orElseGet(() ->
            ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông báo"))
        );
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(a -> {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            a.setActive(active);
            repo.save(a);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "active", active));
        }).orElseGet(() ->
            ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông báo"))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông báo"));
        }
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
