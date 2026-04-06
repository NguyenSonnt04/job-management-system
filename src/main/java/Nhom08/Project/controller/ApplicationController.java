package Nhom08.Project.controller;

import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.JobApplication;
import Nhom08.Project.entity.Notification;
import Nhom08.Project.entity.NotificationTemplate;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.JobApplicationRepository;
import Nhom08.Project.repository.JobRepository;
import Nhom08.Project.repository.NotificationRepository;
import Nhom08.Project.repository.NotificationTemplateRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired private JobApplicationRepository applicationRepo;
    @Autowired private JobRepository            jobRepo;
    @Autowired private UserRepository           userRepo;
    @Autowired private EmployerRepository       employerRepo;
    @Autowired private Nhom08.Project.repository.JobStatisticsRepository jobStatsRepo;
    @Autowired private NotificationRepository         notificationRepo;
    @Autowired private NotificationTemplateRepository templateRepo;
    @Autowired private Nhom08.Project.service.GeminiService geminiService;
    @Autowired private Nhom08.Project.service.FirebaseImageStorageService firebaseStorageService;
    @Autowired private Nhom08.Project.repository.UserCvRepository userCvRepo;

    /**
     * POST /api/applications/apply
     * Submit a job application.
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();

        // Get jobId
        Object jobIdObj = body.get("jobId");
        if (jobIdObj == null) {
            resp.put("success", false);
            resp.put("message", "Thiếu thông tin jobId");
            return ResponseEntity.badRequest().body(resp);
        }
        Long jobId = Long.parseLong(jobIdObj.toString());

        // Validate job exists
        Optional<Job> jobOpt = jobRepo.findById(jobId);
        if (jobOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Không tìm thấy việc làm");
            return ResponseEntity.badRequest().body(resp);
        }

        // Required fields
        String fullName = (String) body.get("fullName");
        String email    = (String) body.get("email");
        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()) {
            resp.put("success", false);
            resp.put("message", "Họ tên và email là bắt buộc");
            return ResponseEntity.badRequest().body(resp);
        }

        // Check duplicate application by same email for same job
        if (applicationRepo.existsByJobIdAndEmail(jobId, email)) {
            resp.put("success", false);
            resp.put("message", "Bạn đã ứng tuyển vị trí này rồi");
            return ResponseEntity.badRequest().body(resp);
        }

        // Build entity
        JobApplication application = new JobApplication();
        application.setJob(jobOpt.get());
        application.setFullName(fullName);
        application.setEmail(email);
        application.setPhone((String) body.getOrDefault("phone", ""));
        application.setCvType((String) body.getOrDefault("cvType", "existing"));
        application.setPrivacy((String) body.getOrDefault("privacy", "lock"));
        application.setCoverLetter((String) body.getOrDefault("coverLetter", ""));
        if (body.containsKey("cvFileUrl")) {
            application.setCvFileUrl((String) body.get("cvFileUrl"));
        }

        // Link to user account if logged in
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userRepo.findByEmail(auth.getName()).ifPresent(application::setUser);
        }

        applicationRepo.save(application);

        // Update applicationCount
        jobStatsRepo.findByJobId(jobId).ifPresentOrElse(stats -> {
            stats.incrementApplicationCount();
            jobStatsRepo.save(stats);
        }, () -> {
            Nhom08.Project.entity.JobStatistics newStats = new Nhom08.Project.entity.JobStatistics(jobOpt.get());
            // Since this is the first application, counts may not be accurate if there are older ones.
            // A better way is to count all existing applications for this job.
            long count = applicationRepo.findByJobId(jobId).size();
            newStats.setApplicationCount(count);
            jobStatsRepo.save(newStats);
        });

        resp.put("success", true);
        resp.put("success", true);
        resp.put("message", "Ứng tuyển thành công!");
        resp.put("applicationId", application.getId());
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /api/applications/upload-cv
     * Upload file CV (PDF/DOC/DOCX).
     * Ưu tiên Firebase Storage, fallback sang local storage nếu Firebase lỗi.
     */
    @PostMapping("/upload-cv")
    public ResponseEntity<Map<String, Object>> uploadCvFile(
            @RequestParam("file") MultipartFile file) {

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
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "File vượt quá 5MB");
                return ResponseEntity.badRequest().body(result);
            }

            String cvFileUrl;
            // Thử Firebase trước
            try {
                String objectPath = firebaseStorageService.uploadFile(file, "cv-applications", 5 * 1024 * 1024);
                cvFileUrl = "/api/uploads/file?path=" + java.net.URLEncoder.encode(objectPath, java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("[CV-UPLOAD] Firebase OK: " + cvFileUrl);
            } catch (Exception firebaseEx) {
                System.err.println("[CV-UPLOAD] Firebase FAILED: " + firebaseEx.getMessage());
                firebaseEx.printStackTrace();
                // Fallback: lưu local
                java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "cv");
                java.nio.file.Files.createDirectories(uploadDir);
                String originalName = file.getOriginalFilename();
                String ext = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf('.'))
                        : ".pdf";
                String fileName = java.util.UUID.randomUUID() + ext;
                java.nio.file.Files.write(uploadDir.resolve(fileName), file.getBytes());
                cvFileUrl = "/cv-files/" + fileName;
                System.out.println("[CV-UPLOAD] Fallback local: " + cvFileUrl);
            }

            result.put("success", true);
            result.put("cvFileUrl", cvFileUrl);
            result.put("fileName", file.getOriginalFilename());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * GET /api/applications/employer
     * Get all applications for jobs belonging to the logged-in employer.
     */
    @GetMapping("/employer")
    public ResponseEntity<?> employerApplications(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User not found"));

        // Get employer's jobs
        Optional<Employer> employerOpt = employerRepo.findByUserId(userOpt.get().getId());
        if (employerOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Không phải nhà tuyển dụng"));
        }

        List<Job> jobs = jobRepo.findByEmployerId(employerOpt.get().getId());
        if (jobs.isEmpty()) {
            return ResponseEntity.ok(Map.of("applications", List.of(), "total", 0));
        }

        List<Long> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        List<JobApplication> applications = applicationRepo.findByJobIdIn(jobIds);

        // Map to summary objects for frontend table
        List<Map<String, Object>> result = applications.stream().map(app -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          app.getId());
            m.put("fullName",    app.getFullName());
            m.put("email",       app.getEmail());
            m.put("phone",       app.getPhone());
            m.put("status",      app.getStatus());
            m.put("cvType",      app.getCvType());
            m.put("cvFileUrl",   app.getCvFileUrl());
            m.put("coverLetter", app.getCoverLetter());
            m.put("createdAt",   app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
            // AI analysis fields
            m.put("aiCompatScore", app.getAiCompatScore());
            m.put("aiRanking",     app.getAiRanking());
            m.put("aiExperience",  app.getAiExperience());
            m.put("aiSummary",     app.getAiSummary());
            // Job info
            if (app.getJob() != null) {
                m.put("jobId",    app.getJob().getId());
                m.put("jobTitle", app.getJob().getTitle());
            }
            // CV info – expose the latest CV id so employer can view it
            if (app.getUser() != null) {
                var cvs = userCvRepo.findByUserIdOrderByUpdatedAtDesc(app.getUser().getId());
                if (!cvs.isEmpty()) {
                    m.put("userCvId", cvs.get(0).getId());
                    m.put("userCvName", cvs.get(0).getCvName());
                }
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("applications", result, "total", result.size()));
    }

    /**
     * GET /api/applications/my
     * Get all applications for the logged-in candidate (user).
     */
    @GetMapping("/my")
    public ResponseEntity<?> myApplications(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        }

        List<JobApplication> applications = applicationRepo.findByUserId(userOpt.get().getId());

        List<Map<String, Object>> result = applications.stream().map(app -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", app.getId());
            m.put("status", app.getStatus());
            m.put("createdAt", app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
            if (app.getJob() != null) {
                m.put("jobId", app.getJob().getId());
                m.put("jobTitle", app.getJob().getTitle());
                if (app.getJob().getEmployer() != null) {
                    m.put("companyName", app.getJob().getEmployer().getCompanyName());
                }
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("applications", result, "total", result.size()));
    }

    /**
     * PATCH /api/applications/{id}/status
     * Update application status (employer action).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<JobApplication> appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();

        JobApplication application = appOpt.get();
        String newStatus = body.getOrDefault("status", "PENDING");
        application.setStatus(newStatus);
        applicationRepo.save(application);

        // Doc template tu DB, fallback ve gia tri mac dinh
        if ("INTERVIEW".equals(newStatus) && application.getUser() != null) {
            Notification notification = new Notification();
            notification.setUser(application.getUser());
            notification.setType("INTERVIEW");

            String jobTitle = application.getJob() != null ? application.getJob().getTitle() : "vị trí này";

            String title = templateRepo.findByKey("INTERVIEW_TITLE")
                .map(NotificationTemplate::getValue)
                .orElse("Bạn được mời phỏng vấn!")
                .replace("{jobTitle}", jobTitle);

            String message = templateRepo.findByKey("INTERVIEW_MESSAGE")
                .map(NotificationTemplate::getValue)
                .orElse("Chúc mừng! Bạn đã được mời phỏng vấn cho vị trí '{jobTitle}'. Nhà tuyển dụng sẽ liên hệ với bạn sớm!")
                .replace("{jobTitle}", jobTitle);

            notification.setTitle(title);
            notification.setMessage(message);

            if (application.getJob() != null) {
                notification.setJobId(application.getJob().getId());
                notification.setJobTitle(jobTitle);
            }
            notificationRepo.save(notification);
        }

        return ResponseEntity.ok(Map.of("success", true, "status", newStatus));
    }

    /**
     * GET /api/applications/{id}/cv
     * Employer xem CV của ứng viên (qua application id).
     */
    @GetMapping("/{id}/cv")
    public ResponseEntity<?> getApplicantCv(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<JobApplication> appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();

        JobApplication application = appOpt.get();

        // Verify caller is the employer who owns this job
        Optional<User> callerOpt = userRepo.findByEmail(auth.getName());
        if (callerOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        Optional<Employer> empOpt = employerRepo.findByUserId(callerOpt.get().getId());
        if (empOpt.isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Không phải NTD"));

        Job job = application.getJob();
        if (job == null || !job.getEmployer().getId().equals(empOpt.get().getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền xem CV này"));
        }

        // Get the applicant's latest CV
        if (application.getUser() == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Ứng viên không có tài khoản trong hệ thống"));
        }

        var cvs = userCvRepo.findByUserIdOrderByUpdatedAtDesc(application.getUser().getId());
        if (cvs.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Ứng viên chưa có CV trong hệ thống"));
        }

        var cv = cvs.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", cv.getId());
        result.put("cvName", cv.getCvName());
        result.put("templateId", cv.getTemplateId());
        result.put("templateName", cv.getTemplateName());
        result.put("cvContent", cv.getCvContent());
        result.put("updatedAt", cv.getUpdatedAt() != null ? cv.getUpdatedAt().toString() : null);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/applications/{id}/analyze
     * AI phân tích ứng viên: mức tương thích, xếp loại, kinh nghiệm
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeApplication(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<JobApplication> appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();

        JobApplication app = appOpt.get();
        Job job = app.getJob();
        if (job == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy job"));

        // Lấy CV content nếu ứng viên có CV trong hệ thống
        String cvContent = "";
        if (app.getUser() != null) {
            var cvs = userCvRepo.findByUserIdOrderByUpdatedAtDesc(app.getUser().getId());
            if (!cvs.isEmpty()) {
                cvContent = cvs.get(0).getCvContent() != null ? cvs.get(0).getCvContent() : "";
            }
        }
        if (cvContent.isEmpty() && app.getCoverLetter() != null) {
            cvContent = app.getCoverLetter();
        }

        // Build prompt cho Gemini
        String prompt = buildAnalyzePrompt(job, app, cvContent);

        try {
            String raw = geminiService.callGeminiJson(prompt);
            // Parse JSON response
            String json = raw.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }
            com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);

            int score = node.path("compatScore").asInt(0);
            String ranking = node.path("ranking").asText("Chưa xếp loại");
            String experience = node.path("experience").asText("Chưa xác định");
            String summary = node.path("summary").asText("");

            app.setAiCompatScore(score);
            app.setAiRanking(ranking);
            app.setAiExperience(experience);
            app.setAiSummary(summary);
            applicationRepo.save(app);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("aiCompatScore", score);
            result.put("aiRanking", ranking);
            result.put("aiExperience", experience);
            result.put("aiSummary", summary);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Lỗi phân tích AI: " + e.getMessage()));
        }
    }

    /**
     * POST /api/applications/analyze-all
     * Phân tích AI tất cả ứng viên chưa được phân tích của employer
     */
    @PostMapping("/analyze-all")
    public ResponseEntity<?> analyzeAll(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User not found"));

        Optional<Employer> empOpt = employerRepo.findByUserId(userOpt.get().getId());
        if (empOpt.isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Không phải NTD"));

        List<Job> jobs = jobRepo.findByEmployerId(empOpt.get().getId());
        List<Long> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        List<JobApplication> apps = applicationRepo.findByJobIdIn(jobIds);

        int analyzed = 0;
        for (JobApplication app : apps) {
            if (app.getAiCompatScore() != null) continue; // Đã phân tích

            Job job = app.getJob();
            if (job == null) continue;

            String cvContent = "";
            if (app.getUser() != null) {
                var cvs = userCvRepo.findByUserIdOrderByUpdatedAtDesc(app.getUser().getId());
                if (!cvs.isEmpty() && cvs.get(0).getCvContent() != null) {
                    cvContent = cvs.get(0).getCvContent();
                }
            }
            if (cvContent.isEmpty() && app.getCoverLetter() != null) {
                cvContent = app.getCoverLetter();
            }

            try {
                String raw = geminiService.callGeminiJson(buildAnalyzePrompt(job, app, cvContent));
                String json = raw.trim();
                if (json.startsWith("```")) json = json.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
                com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);

                app.setAiCompatScore(node.path("compatScore").asInt(0));
                app.setAiRanking(node.path("ranking").asText("Chưa xếp loại"));
                app.setAiExperience(node.path("experience").asText("Chưa xác định"));
                app.setAiSummary(node.path("summary").asText(""));
                applicationRepo.save(app);
                analyzed++;
            } catch (Exception e) {
                // Skip và tiếp tục ứng viên khác
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "analyzed", analyzed, "total", apps.size()));
    }

    private String buildAnalyzePrompt(Job job, JobApplication app, String cvContent) {
        return """
            Phân tích ứng viên cho vị trí tuyển dụng và trả về JSON, KHÔNG giải thích thêm.

            THÔNG TIN VIỆC LÀM:
            - Chức danh: %s
            - Ngành: %s
            - Mô tả: %s
            - Yêu cầu: %s
            - Kinh nghiệm yêu cầu: %s

            THÔNG TIN ỨNG VIÊN:
            - Họ tên: %s
            - Email: %s
            - Cover letter: %s
            - CV content: %s

            Trả về JSON:
            {
              "compatScore": <số từ 0-100, mức tương thích giữa ứng viên và JD>,
              "ranking": "<Xuất sắc|Tốt|Khá|Trung bình|Yếu>",
              "experience": "<tóm tắt kinh nghiệm liên quan trong 1 câu ngắn, VD: 2 năm Java, 1 năm React>",
              "summary": "<nhận xét tổng thể 1-2 câu về ứng viên so với vị trí>"
            }
            Nếu không đủ thông tin, ước lượng dựa trên những gì có. compatScore tối thiểu 10.
            """.formatted(
                job.getTitle() != null ? job.getTitle() : "",
                job.getIndustry() != null ? job.getIndustry() : "",
                job.getDescription() != null ? limitText(job.getDescription(), 500) : "",
                job.getRequirements() != null ? limitText(job.getRequirements(), 500) : "",
                job.getExperience() != null ? job.getExperience() : "",
                app.getFullName() != null ? app.getFullName() : "",
                app.getEmail() != null ? app.getEmail() : "",
                app.getCoverLetter() != null ? limitText(app.getCoverLetter(), 300) : "Không có",
                !cvContent.isEmpty() ? limitText(cvContent, 800) : "Không có CV"
        );
    }

    private String limitText(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }
}
