package Nhom08.Project.controller;

import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.JobApplication;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.JobApplicationRepository;
import Nhom08.Project.repository.JobRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        resp.put("message", "Ứng tuyển thành công!");
        resp.put("applicationId", application.getId());
        return ResponseEntity.ok(resp);
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
            m.put("coverLetter", app.getCoverLetter());
            m.put("createdAt",   app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
            // Job info
            if (app.getJob() != null) {
                m.put("jobId",    app.getJob().getId());
                m.put("jobTitle", app.getJob().getTitle());
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

        return ResponseEntity.ok(Map.of("success", true, "status", newStatus));
    }
}
