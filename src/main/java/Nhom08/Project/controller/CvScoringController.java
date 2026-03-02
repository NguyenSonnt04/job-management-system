package Nhom08.Project.controller;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.JobRepository;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.CvScoringService;
import Nhom08.Project.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cv-scoring")
public class CvScoringController {

    @Autowired private CvScoringService scoringService;
    @Autowired private AuthService      authService;
    @Autowired private GeminiService    geminiService;
    @Autowired private JobRepository    jobRepo;

    // ── Helper: get current user from session ──────────────
    private Optional<User> currentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        return authService.findByEmail(auth.getName());
    }

    /**
     * GET /api/cv-scoring/criteria
     * Returns list of active scoring criteria (for frontend display).
     */
    @GetMapping("/criteria")
    public ResponseEntity<?> getCriteria() {
        List<CvScoringCriteria> list = scoringService.getCriteria();
        List<Map<String, Object>> result = list.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          c.getId());
            m.put("name",        c.getName());
            m.put("description", c.getDescription());
            m.put("maxScore",    c.getMaxScore());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/cv-scoring/score
     * Upload CV file → score with Gemini → save → return result.
     */
    @PostMapping("/score")
    public ResponseEntity<Map<String, Object>> score(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();

        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Vui lòng đăng nhập để sử dụng tính năng chấm điểm CV");
            return ResponseEntity.status(401).body(resp);
        }

        try {
            CvScoreSession session = scoringService.scoreAndSave(userOpt.get(), file);
            resp.put("success",   true);
            resp.put("sessionId", session.getId());
            resp.put("data",      toSessionMap(session));
            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        } catch (IllegalStateException e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Lỗi phân tích CV: " + e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    /**
     * GET /api/cv-scoring/history
     * Returns scoring history for the logged-in user.
     */
    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        List<CvScoreSession> sessions = scoringService.getUserHistory(userOpt.get().getId());
        List<Map<String, Object>> list = sessions.stream()
            .map(this::toSessionSummaryMap)
            .toList();
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/cv-scoring/{id}
     * Returns full session detail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSession(@PathVariable Long id, Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        try {
            CvScoreSession session = scoringService.getSession(id, userOpt.get().getId());
            return ResponseEntity.ok(toSessionMap(session));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/cv-scoring/match-jobs
     * Uses existing session data to match CV profile against active jobs in same industry.
     * Body: { "sessionId": 123 }
     */
    @PostMapping("/match-jobs")
    public ResponseEntity<?> matchJobs(@RequestBody Map<String, Object> body, Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Long sessionId = Long.valueOf(body.get("sessionId").toString());

        CvScoreSession session;
        try {
            session = scoringService.getSession(sessionId, userOpt.get().getId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }

        // Build CV profile text from session data
        StringBuilder cvProfile = new StringBuilder();
        cvProfile.append("Overall feedback: ").append(session.getOverallFeedback()).append("\n");
        cvProfile.append("Strengths: ");
        session.getPoints().stream()
            .filter(p -> p.getPointType() == CvScorePoint.PointType.STRENGTH)
            .forEach(p -> cvProfile.append(p.getContent()).append("; "));
        cvProfile.append("\nWeaknesses: ");
        session.getPoints().stream()
            .filter(p -> p.getPointType() == CvScorePoint.PointType.WEAKNESS)
            .forEach(p -> cvProfile.append(p.getContent()).append("; "));
        session.getResults().forEach(r ->
            cvProfile.append("\n").append(r.getCriteria().getName())
                     .append(": ").append(r.getFeedback()));

        // Ask Gemini to extract the industry keyword from CV profile
        String industryPrompt =
            "Based on this CV analysis summary, respond with ONLY a JSON object containing one field 'industry' " +
            "with the main industry/domain keyword in Vietnamese (e.g. 'Công nghệ thông tin', 'Kế toán', 'Marketing'). " +
            "CV Summary: " + cvProfile;

        String industryJson;
        String detectedIndustry = null;
        try {
            industryJson = geminiService.scoreCvText(industryPrompt, List.of());
            // Parse industry from JSON
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(industryJson);
            if (node.has("industry")) {
                detectedIndustry = node.get("industry").asText();
            }
        } catch (Exception e) {
            // Fallback: use top 20 active jobs
        }

        // Fetch candidate jobs
        List<Job> candidateJobs;
        if (detectedIndustry != null && !detectedIndustry.isBlank()) {
            candidateJobs = jobRepo.findByStatusAndIndustryContainingIgnoreCase("ACTIVE", detectedIndustry);
            if (candidateJobs.isEmpty()) {
                candidateJobs = jobRepo.findTop20ByStatus("ACTIVE");
            }
        } else {
            candidateJobs = jobRepo.findTop20ByStatus("ACTIVE");
        }

        if (candidateJobs.isEmpty()) {
            return ResponseEntity.ok(Map.of("matches", List.of(), "industry", ""));
        }

        // Build job data list for Gemini
        List<Map<String, Object>> jobData = candidateJobs.stream().map(j -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          j.getId());
            m.put("title",       j.getTitle());
            m.put("industry",    j.getIndustry());
            m.put("experience",  j.getExperience());
            m.put("description", j.getDescription() != null
                ? j.getDescription().substring(0, Math.min(500, j.getDescription().length())) : "");
            m.put("requirements", j.getRequirements() != null
                ? j.getRequirements().substring(0, Math.min(500, j.getRequirements().length())) : "");
            return m;
        }).collect(Collectors.toList());

        // Ask Gemini to match (text-based using CV profile)
        String matchPrompt =
            "You are a job-matching AI. Based on this candidate profile, match and score each job listing (0-100).\n" +
            "Candidate Profile:\n" + cvProfile + "\n\n" +
            "Score based on: skill alignment (40%), experience (30%), industry fit (20%), education (10%).\n\n" +
            "Jobs to match:\n";
        StringBuilder jobsText = new StringBuilder(matchPrompt);
        for (Map<String, Object> jd : jobData) {
            jobsText.append("---\nJob ID: ").append(jd.get("id"))
                .append("\nTitle: ").append(jd.get("title"))
                .append("\nIndustry: ").append(jd.get("industry"))
                .append("\nExperience: ").append(jd.get("experience"))
                .append("\nRequirements: ").append(jd.get("requirements")).append("\n");
        }
        jobsText.append("\nReturn ONLY a JSON array (no markdown): [{\"jobId\": <number>, \"matchScore\": <0-100>, \"reason\": \"<1 sentence in Vietnamese>\"}]\nSort descending. Only include jobs with matchScore >= 40.");

        try {
            String matchJson = geminiService.scoreCvText(jobsText.toString(), List.of());
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode matchArray = om.readTree(matchJson);

            // Enrich with job details
            Map<Long, Job> jobMap = candidateJobs.stream().collect(Collectors.toMap(Job::getId, j -> j));
            List<Map<String, Object>> result = new ArrayList<>();

            for (com.fasterxml.jackson.databind.JsonNode item : matchArray) {
                long jid  = item.path("jobId").asLong();
                int  score = item.path("matchScore").asInt(0);
                String reason = item.path("reason").asText("");

                Job job = jobMap.get(jid);
                if (job == null) continue;

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("jobId",      jid);
                m.put("matchScore", score);
                m.put("reason",     reason);
                m.put("title",      job.getTitle());
                m.put("industry",   job.getIndustry());
                m.put("location",   job.getLocation());
                m.put("experience", job.getExperience());
                m.put("company",    job.getEmployer() != null ? job.getEmployer().getCompanyName() : "");
                result.add(m);
            }

            return ResponseEntity.ok(Map.of(
                "matches", result,
                "industry", detectedIndustry != null ? detectedIndustry : ""
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Lỗi phân tích: " + e.getMessage()));
        }
    }


    private Map<String, Object> toSessionMap(CvScoreSession s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              s.getId());
        m.put("fileName",        s.getFileName());
        m.put("fileType",        s.getFileType());
        m.put("totalScore",      s.getTotalScore());
        m.put("maxTotalScore",   s.getMaxTotalScore());
        m.put("overallFeedback", s.getOverallFeedback());
        m.put("createdAt",       s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);

        // Per-criteria results
        List<Map<String, Object>> results = s.getResults().stream().map(r -> {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("criteriaId",   r.getCriteria().getId());
            rm.put("criteriaName", r.getCriteria().getName());
            rm.put("score",        r.getScore());
            rm.put("maxScore",     r.getCriteria().getMaxScore());
            rm.put("feedback",     r.getFeedback());
            return rm;
        }).toList();
        m.put("results", results);

        // Strengths & weaknesses
        List<String> strengths = s.getPoints().stream()
            .filter(p -> p.getPointType() == CvScorePoint.PointType.STRENGTH)
            .sorted(Comparator.comparing(CvScorePoint::getDisplayOrder))
            .map(CvScorePoint::getContent)
            .toList();
        List<String> weaknesses = s.getPoints().stream()
            .filter(p -> p.getPointType() == CvScorePoint.PointType.WEAKNESS)
            .sorted(Comparator.comparing(CvScorePoint::getDisplayOrder))
            .map(CvScorePoint::getContent)
            .toList();
        m.put("strengths",  strengths);
        m.put("weaknesses", weaknesses);

        return m;
    }

    private Map<String, Object> toSessionSummaryMap(CvScoreSession s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            s.getId());
        m.put("fileName",      s.getFileName());
        m.put("totalScore",    s.getTotalScore());
        m.put("maxTotalScore", s.getMaxTotalScore());
        m.put("createdAt",     s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
        return m;
    }
}
