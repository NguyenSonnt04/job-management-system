package Nhom08.Project.controller;

import Nhom08.Project.entity.User;
import Nhom08.Project.entity.UserCv;
import Nhom08.Project.entity.UserCvVersion;
import Nhom08.Project.repository.JobApplicationRepository;
import Nhom08.Project.repository.UserCvRepository;
import Nhom08.Project.repository.UserCvVersionRepository;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.CvVersioningService;
import Nhom08.Project.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private final UserCvVersionRepository userCvVersionRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthService authService;
    private final CvVersioningService cvVersioningService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserCvController(
            UserCvRepository userCvRepository,
            UserCvVersionRepository userCvVersionRepository,
            JobApplicationRepository jobApplicationRepository,
            AuthService authService,
            CvVersioningService cvVersioningService,
            GeminiService geminiService) {
        this.userCvRepository = userCvRepository;
        this.userCvVersionRepository = userCvVersionRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.authService = authService;
        this.cvVersioningService = cvVersioningService;
        this.geminiService = geminiService;
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
            Object sourceTemplateVersionNoObj = body.get("sourceTemplateVersionNo");
            if (sourceTemplateVersionNoObj != null) {
                try { cv.setSourceTemplateVersionNo(Integer.parseInt(String.valueOf(sourceTemplateVersionNoObj))); } catch (Exception ignored) {}
            }

            // cvContent may arrive as a JSON string OR as an object — serialize to string
            Object cvContent = body.get("cvContent");
            if (cvContent instanceof String s) {
                cv.setCvContent(s);
            } else {
                cv.setCvContent(objectMapper.writeValueAsString(cvContent));
            }

            UserCv saved = cvVersioningService.createUserCv(cv, "USER");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id",      saved.getId());
            result.put("currentVersionNo", saved.getCurrentVersionNo());
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
                String previousName = cv.getCvName();
                String previousContent = cv.getCvContent();

                if (body.containsKey("cvName"))
                    cv.setCvName(String.valueOf(body.get("cvName")));

                if (body.containsKey("templateId") && body.get("templateId") != null) {
                    try { cv.setTemplateId(Long.parseLong(String.valueOf(body.get("templateId")))); } catch (Exception ignored) {}
                }
                
                if (body.containsKey("templateName") && body.get("templateName") != null) {
                    cv.setTemplateName(String.valueOf(body.get("templateName")));
                }

                if (body.containsKey("sourceTemplateVersionNo") && body.get("sourceTemplateVersionNo") != null) {
                    try { cv.setSourceTemplateVersionNo(Integer.parseInt(String.valueOf(body.get("sourceTemplateVersionNo")))); } catch (Exception ignored) {}
                }

                Object cvContent = body.get("cvContent");
                if (cvContent != null) {
                    cv.setCvContent(cvContent instanceof String s
                        ? s
                        : objectMapper.writeValueAsString(cvContent));
                }
                UserCv saved = cvVersioningService.updateUserCv(cv, previousName, previousContent, "USER");

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("currentVersionNo", saved.getCurrentVersionNo());
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
            m.put("sourceTemplateVersionNo", cv.getSourceTemplateVersionNo());
            m.put("currentVersionNo", cv.getCurrentVersionNo());
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
            result.put("sourceTemplateVersionNo", cv.getSourceTemplateVersionNo());
            result.put("currentVersionNo", cv.getCurrentVersionNo());
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

    @GetMapping("/{id}/versions")
    public ResponseEntity<?> versions(@PathVariable Long id, Authentication auth) {
        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();

        return userCvRepository.findById(id).map(cv -> {
            if (!cv.getUser().getId().equals(userOpt.get().getId())) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "KhÃ´ng cÃ³ quyá»n xem lá»‹ch sá»­ CV nÃ y.");
                return ResponseEntity.status(403).body(err);
            }

            List<Map<String, Object>> result = userCvVersionRepository.findByUserCvIdOrderByVersionNoDesc(id)
                .stream()
                .map(this::toVersionSummary)
                .toList();
            return ResponseEntity.ok(result);
        }).orElseGet(() -> {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "KhÃ´ng tÃ¬m tháº¥y CV.");
            return ResponseEntity.status(404).body(err);
        });
    }

    @Transactional
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

            // Check if user has pending applications and this is their last CV
            List<UserCv> userCvs = userCvRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
            boolean isLastCv = userCvs.size() <= 1;
            if (isLastCv) {
                List<Nhom08.Project.entity.JobApplication> apps = jobApplicationRepository.findByUserId(user.getId());
                boolean hasPendingApps = apps.stream()
                        .anyMatch(a -> "PENDING".equalsIgnoreCase(a.getStatus()));
                if (hasPendingApps) {
                    result.put("success", false);
                    result.put("message", "Không thể xóa CV cuối cùng khi bạn còn đơn ứng tuyển đang chờ xử lý. Nhà tuyển dụng cần xem CV của bạn.");
                    return ResponseEntity.status(409).body(result);
                }
            }

            userCvVersionRepository.deleteByUserCvId(id);
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
    // ── Upload PDF file ──────────────────────────────────────────────────────

    /**
     * POST /api/user-cv/upload-pdf
     * Nhận file PDF, lưu vào uploads/cv/, tạo bản ghi UserCv với _pdfUrl trong cvContent.
     */
    @PostMapping("/upload-pdf")
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cvName", defaultValue = "CV tải lên") String cvName,
            Authentication auth) {

        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();
        User user = userOpt.get();

        Map<String, Object> result = new HashMap<>();
        try {
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf")
                    && !contentType.equals("application/msword")
                    && !contentType.contains("wordprocessingml"))) {
                result.put("success", false);
                result.put("message", "Chỉ hỗ trợ file PDF, DOC, DOCX");
                return ResponseEntity.badRequest().body(result);
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "File vượt quá 10MB");
                return ResponseEntity.badRequest().body(result);
            }

            // Lưu file vào uploads/cv/
            Path uploadDir = Paths.get("uploads", "cv");
            Files.createDirectories(uploadDir);
            String ext      = file.getOriginalFilename() != null
                    && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                    : ".pdf";
            String fileName = UUID.randomUUID() + ext;
            Path   filePath = uploadDir.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String pdfUrl = "/cv-files/" + fileName;

            // Lưu vào user_cvs với cvContent chứa _pdfUrl
            UserCv cv = new UserCv();
            cv.setUser(user);
            cv.setCvName(cvName);
            cv.setTemplateName("CV tải lên");
            cv.setCvContent(objectMapper.writeValueAsString(Map.of("_pdfUrl", pdfUrl)));
            UserCv saved = userCvRepository.save(cv);

            result.put("success", true);
            result.put("id",     saved.getId());
            result.put("pdfUrl", pdfUrl);
            result.put("message", "Đã tải CV lên thành công!");
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    // ── Translate CV content via Gemini ─────────────────────────────────────

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translateCv(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) return unauthorized();

        Map<String, Object> result = new HashMap<>();
        try {
            Object cvContentObj = body.get("cvContent");
            String targetLang   = String.valueOf(body.getOrDefault("targetLang", "en"));

            if (cvContentObj == null) {
                result.put("success", false);
                result.put("message", "Thiếu nội dung CV.");
                return ResponseEntity.badRequest().body(result);
            }

            String cvJsonStr = cvContentObj instanceof String
                    ? (String) cvContentObj
                    : objectMapper.writeValueAsString(cvContentObj);

            String translated = geminiService.translateCv(cvJsonStr, targetLang);
            Object parsedCv   = objectMapper.readValue(translated, Object.class);

            result.put("success", true);
            result.put("data", parsedCv);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi dịch CV: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private Map<String, Object> toVersionSummary(UserCvVersion version) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", version.getId());
        item.put("versionNo", version.getVersionNo());
        item.put("cvName", version.getCvName());
        item.put("savedBy", version.getSavedBy());
        item.put("createdAt", version.getCreatedAt());
        return item;
    }
}
