package Nhom08.Project.controller;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private JobApplicationRepository applicationRepository;

    // ── Dashboard Stats ──
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalEmployers", employerRepository.count());
        stats.put("totalJobs", jobRepository.count());
        stats.put("totalApplications", applicationRepository.count());
        stats.put("adminCount", userRepository.countByRoleName(Role.ADMIN));
        stats.put("candidateCount", userRepository.countByRoleName(Role.CANDIDATE));
        stats.put("employerUserCount", userRepository.countByRoleName(Role.EMPLOYER));

        // Active jobs
        long activeJobs = jobRepository.findByStatus("ACTIVE").size();
        stats.put("activeJobs", activeJobs);

        return ResponseEntity.ok(stats);
    }

    // ── Users List ──
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("email", u.getEmail());
            m.put("fullName", u.getFullName());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole() != null ? u.getRole().getName() : "N/A");
            m.put("enabled", u.getEnabled());
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Change user role ──
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                        org.springframework.security.core.Authentication auth) {
        if (auth != null) {
            User self = userRepository.findByEmail(auth.getName()).orElse(null);
            if (self != null && self.getId().equals(id))
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể đổi role của chính mình"));
        }
        String roleName = body.get("role");
        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Role không hợp lệ"));
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "role", role.getName()));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Toggle user enabled ──
    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUser(@PathVariable Long id,
                                        org.springframework.security.core.Authentication auth) {
        if (auth != null) {
            User self = userRepository.findByEmail(auth.getName()).orElse(null);
            if (self != null && self.getId().equals(id))
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể khóa chính mình"));
        }
        return userRepository.findById(id).map(user -> {
            user.setEnabled(!user.getEnabled());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "enabled", user.getEnabled()));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Delete user ──
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Employers List ──
    @GetMapping("/employers")
    public ResponseEntity<?> getEmployers() {
        List<Employer> employers = employerRepository.findAll();
        List<Map<String, Object>> result = employers.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("companyName", e.getCompanyName());
            m.put("businessType", e.getBusinessType());
            m.put("employeeCount", e.getEmployeeCount());
            m.put("province", e.getProvince());
            m.put("contactName", e.getContactName());
            m.put("contactPhone", e.getContactPhone());
            m.put("taxCode", e.getTaxCode());
            m.put("website", e.getWebsite());
            m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
            if (e.getUser() != null) {
                m.put("userEmail", e.getUser().getEmail());
                m.put("userId", e.getUser().getId());
                m.put("userEnabled", e.getUser().getEnabled());
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Jobs List ──
    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs() {
        List<Job> jobs = jobRepository.findAll();
        List<Map<String, Object>> result = jobs.stream().map(j -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", j.getId());
            m.put("title", j.getTitle());
            m.put("location", j.getLocation());
            m.put("status", j.getStatus());
            m.put("employmentType", j.getEmploymentType());
            m.put("deadline", j.getDeadline() != null ? j.getDeadline().toString() : null);
            m.put("createdAt", j.getCreatedAt() != null ? j.getCreatedAt().toString() : null);
            if (j.getEmployer() != null) {
                m.put("companyName", j.getEmployer().getCompanyName());
                m.put("employerId", j.getEmployer().getId());
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Toggle job status ──
    @PatchMapping("/jobs/{id}/toggle")
    public ResponseEntity<?> toggleJob(@PathVariable Long id) {
        return jobRepository.findById(id).map(job -> {
            String newStatus = "ACTIVE".equals(job.getStatus()) ? "PAUSED" : "ACTIVE";
            job.setStatus(newStatus);
            jobRepository.save(job);
            return ResponseEntity.ok(Map.of("success", true, "status", newStatus));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Approve job ──
    @PatchMapping("/jobs/{id}/approve")
    public ResponseEntity<?> approveJob(@PathVariable Long id) {
        return jobRepository.findById(id).map(job -> {
            job.setStatus("ACTIVE");
            jobRepository.save(job);
            return ResponseEntity.ok(Map.of("success", true, "status", "ACTIVE"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Reject job ──
    @PatchMapping("/jobs/{id}/reject")
    public ResponseEntity<?> rejectJob(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return jobRepository.findById(id).map(job -> {
            job.setStatus("REJECTED");
            jobRepository.save(job);
            return ResponseEntity.ok(Map.of("success", true, "status", "REJECTED"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Analytics ──
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        LocalDateTime from = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Object[]> userRows = userRepository.countByDay(from);
        List<Object[]> appRows = applicationRepository.countByDay(from);

        Map<String, Long> userMap = new LinkedHashMap<>();
        for (Object[] row : userRows) {
            userMap.put(row[0].toString().substring(0, 10), ((Number) row[1]).longValue());
        }
        Map<String, Long> appMap = new LinkedHashMap<>();
        for (Object[] row : appRows) {
            appMap.put(row[0].toString().substring(0, 10), ((Number) row[1]).longValue());
        }

        List<String> labels = new ArrayList<>();
        List<Long> userData = new ArrayList<>();
        List<Long> appData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            String day = LocalDateTime.now().minusDays(i).format(fmt);
            labels.add(day);
            userData.add(userMap.getOrDefault(day, 0L));
            appData.add(appMap.getOrDefault(day, 0L));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("newUsers", userData);
        result.put("newApplications", appData);
        result.put("totalUsers", userRepository.count());
        result.put("totalJobs", jobRepository.count());
        result.put("totalApplications", applicationRepository.count());
        result.put("totalEmployers", employerRepository.count());
        return ResponseEntity.ok(result);
    }

    // ── Applications List ──
    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        List<JobApplication> apps = applicationRepository.findAll();
        List<Map<String, Object>> result = apps.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("fullName", a.getFullName());
            m.put("email", a.getEmail());
            m.put("status", a.getStatus());
            m.put("aiCompatScore", a.getAiCompatScore());
            m.put("aiRanking", a.getAiRanking());
            m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
            if (a.getJob() != null) {
                m.put("jobTitle", a.getJob().getTitle());
                m.put("jobId", a.getJob().getId());
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
