package Nhom08.Project.controller;

import Nhom08.Project.entity.User;
import Nhom08.Project.entity.UserCv;
import Nhom08.Project.repository.UserCvRepository;
import Nhom08.Project.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CRUD endpoints for a user's saved CVs.
 *
 * POST   /api/user-cv/save      – Save a CV to the logged-in user's account
 * GET    /api/user-cv           – List all CVs for the logged-in user
 * DELETE /api/user-cv/{id}      – Delete a CV (must belong to the user)
 * PUT    /api/user-cv/{id}      – Update an existing saved CV
 */
@RestController
@RequestMapping("/api/user-cv")
public class UserCvController {

    private final UserCvRepository userCvRepository;
    private final AuthService      authService;
    private final ObjectMapper     objectMapper = new ObjectMapper();

    public UserCvController(UserCvRepository userCvRepository, AuthService authService) {
        this.userCvRepository = userCvRepository;
        this.authService      = authService;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Optional<User> resolveUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        return authService.findByEmail(auth.getName());
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", "Bạn cần đăng nhập để thực hiện thao tác này.");
        return ResponseEntity.status(401).body(err);
    }

    // ── Save CV ──────────────────────────────────────────────────────────────

    /**
     * Body: { cvName, templateId, templateName, cvContent (JSON string or object) }
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();
        User user = userOpt.get();

        try {
            UserCv cv = new UserCv();
            cv.setUser(user);
            cv.setCvName(String.valueOf(body.getOrDefault("cvName", "CV của tôi")));

            Object templateIdObj = body.get("templateId");
            if (templateIdObj != null) {
                try { cv.setTemplateId(Long.parseLong(String.valueOf(templateIdObj))); } catch (Exception ignored) {}
            }
            cv.setTemplateName(String.valueOf(body.getOrDefault("templateName", "")));

            // cvContent may arrive as a JSON string OR as an object — serialize to string
            Object cvContent = body.get("cvContent");
            if (cvContent instanceof String s) {
                cv.setCvContent(s);
            } else {
                cv.setCvContent(objectMapper.writeValueAsString(cvContent));
            }

            UserCv saved = userCvRepository.save(cv);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id",      saved.getId());
            result.put("message", "CV đã được lưu thành công!");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi lưu CV: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ── Update existing CV ───────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();
        User user = userOpt.get();

        return userCvRepository.findById(id).map(cv -> {
            if (!cv.getUser().getId().equals(user.getId())) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Không có quyền cập nhật CV này.");
                return ResponseEntity.status(403).body(err);
            }

            try {
                if (body.containsKey("cvName"))
                    cv.setCvName(String.valueOf(body.get("cvName")));

                Object cvContent = body.get("cvContent");
                if (cvContent != null) {
                    cv.setCvContent(cvContent instanceof String s
                        ? s
                        : objectMapper.writeValueAsString(cvContent));
                }
                userCvRepository.save(cv);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "CV đã được cập nhật!");
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Lỗi cập nhật: " + e.getMessage());
                return ResponseEntity.status(500).body(err);
            }
        }).orElseGet(() -> {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Không tìm thấy CV.");
            return ResponseEntity.status(404).body(err);
        });
    }

    // ── List user's CVs ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();

        List<UserCv> cvs = userCvRepository.findByUserIdOrderByUpdatedAtDesc(userOpt.get().getId());

        List<Map<String, Object>> result = cvs.stream().map(cv -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",           cv.getId());
            m.put("cvName",       cv.getCvName());
            m.put("templateId",   cv.getTemplateId());
            m.put("templateName", cv.getTemplateName());
            m.put("updatedAt",    cv.getUpdatedAt());
            m.put("createdAt",    cv.getCreatedAt());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // ── Get specific CV (with content) ───────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, Authentication auth) {
        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();

        return userCvRepository.findById(id).map(cv -> {
            if (!cv.getUser().getId().equals(userOpt.get().getId())) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Không có quyền xem CV này.");
                return ResponseEntity.status(403).body(err);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id",           cv.getId());
            result.put("cvName",       cv.getCvName());
            result.put("templateId",   cv.getTemplateId());
            result.put("templateName", cv.getTemplateName());
            result.put("cvContent",    cv.getCvContent());
            result.put("updatedAt",    cv.getUpdatedAt());
            return ResponseEntity.ok(result);
        }).orElseGet(() -> {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Không tìm thấy CV.");
            return ResponseEntity.status(404).body(err);
        });
    }

    // ── Delete CV ────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id, Authentication auth) {

        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();
        User user = userOpt.get();

        Map<String, Object> result = new HashMap<>();
        return userCvRepository.findById(id).map(cv -> {
            if (!cv.getUser().getId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "Không có quyền xóa CV này.");
                return ResponseEntity.status(403).body(result);
            }
            userCvRepository.delete(cv);
            result.put("success", true);
            result.put("message", "Đã xóa CV thành công.");
            return ResponseEntity.ok(result);
        }).orElseGet(() -> {
            result.put("success", false);
            result.put("message", "Không tìm thấy CV.");
            return ResponseEntity.status(404).body(result);
        });
    }
}
