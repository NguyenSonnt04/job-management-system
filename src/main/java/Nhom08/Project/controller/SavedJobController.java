package Nhom08.Project.controller;

import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.SavedJob;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.JobRepository;
import Nhom08.Project.repository.SavedJobRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-jobs")
public class SavedJobController {

    @Autowired private SavedJobRepository savedJobRepo;
    @Autowired private JobRepository      jobRepo;
    @Autowired private UserRepository     userRepo;

    /**
     * GET /api/saved-jobs
     * List all saved jobs for the current user.
     */
    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        List<SavedJob> savedJobs = savedJobRepo.findByUserIdOrderByCreatedAtDesc(userOpt.get().getId());

        List<Map<String, Object>> result = savedJobs.stream().map(sv -> {
            Map<String, Object> m = new LinkedHashMap<>();
            Job job = sv.getJob();
            m.put("id", job.getId());
            m.put("title", job.getTitle());
            m.put("location", job.getLocation());
            m.put("salaryMin", job.getSalaryMin());
            m.put("salaryMax", job.getSalaryMax());
            m.put("currency", job.getCurrency());
            m.put("showSalary", job.getShowSalary());
            m.put("employmentType", job.getEmploymentType());
            m.put("deadline", job.getDeadline() != null ? job.getDeadline().toString() : null);
            m.put("status", job.getStatus());
            m.put("savedAt", sv.getCreatedAt() != null ? sv.getCreatedAt().toString() : null);
            if (job.getEmployer() != null) {
                m.put("companyName", job.getEmployer().getCompanyName());
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("savedJobs", result, "total", result.size()));
    }

    /**
     * POST /api/saved-jobs/{jobId}
     * Save a job.
     */
    @PostMapping("/{jobId}")
    public ResponseEntity<?> save(@PathVariable Long jobId, Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        User user = userOpt.get();

        Optional<Job> jobOpt = jobRepo.findById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy việc làm"));
        }

        if (savedJobRepo.existsByUserIdAndJobId(user.getId(), jobId)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã lưu trước đó"));
        }

        SavedJob savedJob = new SavedJob(user, jobOpt.get());
        savedJobRepo.save(savedJob);

        return ResponseEntity.ok(Map.of("success", true, "message", "Đã lưu việc làm"));
    }

    /**
     * DELETE /api/saved-jobs/{jobId}
     * Unsave a job.
     */
    @DeleteMapping("/{jobId}")
    @Transactional
    public ResponseEntity<?> unsave(@PathVariable Long jobId, Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        savedJobRepo.deleteByUserIdAndJobId(userOpt.get().getId(), jobId);

        return ResponseEntity.ok(Map.of("success", true, "message", "Đã bỏ lưu việc làm"));
    }

    /**
     * GET /api/saved-jobs/check/{jobId}
     * Check if a job is saved by the current user.
     */
    @GetMapping("/check/{jobId}")
    public ResponseEntity<?> check(@PathVariable Long jobId, Authentication auth) {
        Optional<User> userOpt = getUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("saved", false));
        }

        boolean saved = savedJobRepo.existsByUserIdAndJobId(userOpt.get().getId(), jobId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    private Optional<User> getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepo.findByEmail(auth.getName());
    }
}
