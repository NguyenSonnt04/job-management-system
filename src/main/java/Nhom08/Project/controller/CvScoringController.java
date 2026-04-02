package Nhom08.Project.controller;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.CvJobMatchRepository;
import Nhom08.Project.repository.JobRepository;
import Nhom08.Project.repository.UserCvRepository;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.CvScoringService;
import Nhom08.Project.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cv-scoring")
public class CvScoringController {

    @Autowired private CvScoringService    scoringService;
    @Autowired private AuthService         authService;
    @Autowired private GeminiService       geminiService;
    @Autowired private JobRepository       jobRepo;
    @Autowired private CvJobMatchRepository matchRepo;
    @Autowired private UserCvRepository    userCvRepo;

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
     * POST /api/cv-scoring/score-saved/{cvId}
     * Score an already-saved CV by its ID (uses cvContent text, no file upload).
     */
    @PostMapping("/score-saved/{cvId}")
    public ResponseEntity<Map<String, Object>> scoreSaved(
            @PathVariable Long cvId,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();

        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Vui lòng đăng nhập để sử dụng tính năng chấm điểm CV");
            return ResponseEntity.status(401).body(resp);
        }

        User user = userOpt.get();

        Optional<UserCv> cvOpt = userCvRepo.findById(cvId);
        if (cvOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Không tìm thấy CV với ID: " + cvId);
            return ResponseEntity.status(404).body(resp);
        }

        UserCv userCv = cvOpt.get();
        if (!userCv.getUser().getId().equals(user.getId())) {
            resp.put("success", false);
            resp.put("message", "Bạn không có quyền chấm điểm CV này");
            return ResponseEntity.status(403).body(resp);
        }

        String cvContent = userCv.getCvContent();
        if (cvContent == null || cvContent.isBlank()) {
            resp.put("success", false);
            resp.put("message", "CV chưa có nội dung để chấm điểm");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            String fileName = userCv.getCvName() != null ? userCv.getCvName() : "CV #" + cvId;
            CvScoreSession session = scoringService.scoreFromText(user, cvContent, fileName);
            resp.put("success",   true);
            resp.put("sessionId", session.getId());
            resp.put("data",      toSessionMap(session));
            return ResponseEntity.ok(resp);

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
     * DELETE /api/cv-scoring/history/{id}
     * Deletes one scoring session and its cached matches.
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id, Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Chưa đăng nhập"
            ));
        }

        try {
            scoringService.deleteSession(id, userOpt.get().getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "deletedId", id
            ));
        } catch (RuntimeException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Không thể xóa phiên chấm điểm";
            int status = message.startsWith("Không tìm thấy") ? 404 : 403;
            return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "message", message
            ));
        }
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

        // ── Cache hit: return saved matches from DB ────────────────────────────
        if (matchRepo.existsBySessionId(sessionId)) {
            List<CvJobMatch> cached = matchRepo.findBySessionIdOrderByMatchScoreDesc(sessionId);
            List<Map<String, Object>> cachedResult = cached.stream().map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("jobId",      c.getJobId());
                m.put("matchScore", c.getMatchScore());
                m.put("reason",     c.getReason());
                m.put("title",      c.getJobTitle());
                m.put("industry",   c.getJobIndustry());
                m.put("location",   c.getJobLocation());
                m.put("experience", c.getJobExperience());
                m.put("company",    c.getCompanyName());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("matches", cachedResult, "industry", "", "cached", true));
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

        // --- Step 1: Extract industry keyword from CV profile via Gemini ---
        String industryPrompt =
            "Based on this CV analysis, reply with ONLY a JSON object: {\"industry\": \"<keyword>\"}\n" +
            "The keyword must be one of the common Vietnamese industry names used in job listings, " +
            "for example: 'C\u00f4ng ngh\u1ec7 th\u00f4ng tin', 'K\u1ebf to\u00e1n', 'Marketing', 'T\u00e0i ch\u00ednh', 'X\u00e2y d\u1ef1ng'.\n" +
            "Use EXACTLY the same short keyword that would appear in job industry fields.\n\n" +
            "CV Summary:\n" + cvProfile;

        String detectedIndustry = null;
        try {
            String industryJson = geminiService.callGeminiWithText(industryPrompt);
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            // Strip markdown code fences if present
            String cleanJson = industryJson.replaceAll("(?s)```json|```", "").trim();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(cleanJson);
            if (node.has("industry")) {
                detectedIndustry = node.get("industry").asText().trim();
            }
        } catch (Exception ignored) { /* fallback below */ }

        // --- Step 2: Fetch candidate jobs ---
        // Always include all ACTIVE jobs; if DB is small just take all of them
        List<Job> activeSameIndustry = (detectedIndustry != null && !detectedIndustry.isBlank())
            ? jobRepo.findByStatusAndIndustryContainingIgnoreCase("ACTIVE", detectedIndustry)
            : List.of();

        List<Job> fallbackJobs = jobRepo.findTop20ByStatus("ACTIVE");

        // Merge: same-industry first, then add remaining from fallback (avoid duplicates)
        java.util.Set<Long> seen = new java.util.LinkedHashSet<>();
        List<Job> candidateJobs = new ArrayList<>();
        for (Job j : activeSameIndustry) { if (seen.add(j.getId())) candidateJobs.add(j); }
        for (Job j : fallbackJobs)       { if (seen.add(j.getId())) candidateJobs.add(j); }

        if (candidateJobs.isEmpty()) {
            return ResponseEntity.ok(Map.of("matches", List.of(), "industry", ""));
        }

        // --- Step 3: Build job descriptions for matching prompt ---
        StringBuilder jobsBlock = new StringBuilder();
        for (Job j : candidateJobs) {
            jobsBlock.append("---\nJob ID: ").append(j.getId())
                .append("\nTitle: ").append(j.getTitle())
                .append("\nIndustry: ").append(j.getIndustry())
                .append("\nExperience: ").append(j.getExperience())
                .append("\nDescription: ").append(
                    j.getDescription() != null
                        ? j.getDescription().substring(0, Math.min(400, j.getDescription().length())) : "")
                .append("\nRequirements: ").append(
                    j.getRequirements() != null
                        ? j.getRequirements().substring(0, Math.min(400, j.getRequirements().length())) : "")
                .append("\n\n");
        }

        // --- Step 4: Ask Gemini to match using callGeminiWithText ---
        String matchPrompt =
            "You are a job-matching AI. Score how well this candidate matches each job listing (0-100).\n\n" +
            "CANDIDATE PROFILE:\n" + cvProfile + "\n\n" +
            "Scoring weights: skill alignment 40%, experience 30%, industry fit 20%, education 10%.\n\n" +
            "JOB LISTINGS:\n" + jobsBlock +
            "Return ONLY a JSON array, no markdown:\n" +
            "[{\"jobId\": <number>, \"matchScore\": <0-100>, \"reason\": \"<1 sentence in Vietnamese>\"}]\n" +
            "Sort by matchScore descending. Include ALL jobs (even low scores).";

        try {
            String matchJson = geminiService.callGeminiWithText(matchPrompt);
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String cleanMatch = matchJson.replaceAll("(?s)```json|```", "").trim();
            com.fasterxml.jackson.databind.JsonNode matchArray = om.readTree(cleanMatch);

            Map<Long, Job> jobMap = candidateJobs.stream().collect(Collectors.toMap(Job::getId, j -> j));
            List<Map<String, Object>> result = new ArrayList<>();

            for (com.fasterxml.jackson.databind.JsonNode item : matchArray) {
                long   jid    = item.path("jobId").asLong();
                int    score  = item.path("matchScore").asInt(0);
                String reason = item.path("reason").asText("");
                if (score < 30) continue;  // Filter out very poor matches

                Job job = jobMap.get(jid);
                if (job == null) continue;

                // Build result map
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

                // Persist to DB cache
                CvJobMatch cache = new CvJobMatch();
                cache.setSession(session);
                cache.setJobId(jid);
                cache.setMatchScore(score);
                cache.setReason(reason);
                cache.setJobTitle(job.getTitle());
                cache.setJobIndustry(job.getIndustry());
                cache.setJobLocation(job.getLocation());
                cache.setJobExperience(job.getExperience());
                cache.setCompanyName(job.getEmployer() != null ? job.getEmployer().getCompanyName() : "");
                matchRepo.save(cache);
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

    /**
     * DELETE /api/cv-scoring/match-jobs/{sessionId}
     * Xoa cache match de cho phep tai lai.
     */
    @Transactional
    @DeleteMapping("/match-jobs/{sessionId}")
    public ResponseEntity<?> clearMatchCache(@PathVariable Long sessionId, Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();
        try {
            scoringService.getSession(sessionId, userOpt.get().getId());
            matchRepo.deleteBySessionId(sessionId);
            return ResponseEntity.ok(Map.of("cleared", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
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

    /**
     * POST /api/cv-scoring/compare-jd
     * Compare uploaded CV against a specific Job Description (JD).
     * FormData: file (MultipartFile) + jdText (String)
     */
    @PostMapping("/compare-jd")
    public ResponseEntity<?> compareWithJD(@RequestParam("file") MultipartFile file,
                                          @RequestParam("jdText") String jdText,
                                          Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }

        User user = userOpt.get();

        // Validate file
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv";
        String ext = originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
            : "pdf";

        if (!ext.equals("pdf") && !ext.equals("docx") && !ext.equals("doc")) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Chỉ hỗ trợ file PDF và DOCX"));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "File quá lớn, tối đa 5MB"));
        }

        if (jdText == null || jdText.trim().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Vui lòng nhập mô tả công việc (JD)"));
        }

        try {
            // Prepare JD comparison prompt
            String mimeType = ext.equals("pdf") ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            String comparisonPrompt =
                "You are an expert HR consultant. Compare this CV against the Job Description and provide a detailed analysis.\n\n" +
                "JOB DESCRIPTION:\n" + jdText.trim() + "\n\n" +
                "Analyze and return ONLY a JSON object (no markdown):\n" +
                "{\n" +
                "  \"matchScore\": <0-100 overall match percentage>,\n" +
                "  \"skillScore\": <0-100 how well candidate skills match job requirements>,\n" +
                "  \"experienceScore\": <0-100 how well experience level matches>,\n" +
                "  \"educationScore\": <0-100 how well education matches>,\n" +
                "  \"languageScore\": <0-100 language and communication fit>,\n" +
                "  \"overallFitScore\": <0-100 overall cultural and role fit>,\n" +
                "  \"summary\": \"<2-3 sentences summary in Vietnamese about overall fit>\",\n" +
                "  \"matchedSkills\": [\"<skill1>\", \"<skill2>\"],\n" +
                "  \"missingSkills\": [\"<missing skill1>\", \"<missing skill2>\"],\n" +
                "  \"experienceMatch\": \"<comment on experience alignment in Vietnamese>\",\n" +
                "  \"educationMatch\": \"<comment on education alignment in Vietnamese>\",\n" +
                "  \"suggestions\": [\n" +
                "    \"<suggestion1>\",\n" +
                "    \"<suggestion2>\",\n" +
                "    \"<suggestion3>\"\n" +
                "  ],\n" +
                "  \"keywordsToAdd\": [\"<keyword1>\", \"<keyword2>\", \"<keyword3>\"]\n" +
                "}\n\n" +
                "Be specific, constructive, and professional. Use Vietnamese for all text fields.";

            // Call Gemini with CV file and comparison prompt
            String geminiResponse = geminiService.scoreCvWithPrompt(
                file.getBytes(),
                mimeType,
                comparisonPrompt
            );

            // Parse response
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String cleanJson = geminiResponse.replaceAll("(?s)```json|```", "").trim();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(cleanJson);

            // Build result map
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("fileName", originalName);
            result.put("jdText", jdText.trim());
            result.put("matchScore", root.path("matchScore").asInt(0));
            result.put("skillScore", root.path("skillScore").asInt(0));
            result.put("experienceScore", root.path("experienceScore").asInt(0));
            result.put("educationScore", root.path("educationScore").asInt(0));
            result.put("languageScore", root.path("languageScore").asInt(0));
            result.put("overallFitScore", root.path("overallFitScore").asInt(0));
            result.put("summary", root.path("summary").asText("Không có tóm tắt"));
            result.put("matchedSkills", parseStringArray(root, "matchedSkills"));
            result.put("missingSkills", parseStringArray(root, "missingSkills"));
            result.put("experienceMatch", root.path("experienceMatch").asText(""));
            result.put("educationMatch", root.path("educationMatch").asText(""));
            result.put("suggestions", parseStringArray(root, "suggestions"));
            result.put("keywordsToAdd", parseStringArray(root, "keywordsToAdd"));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Lỗi khi phân tích: " + e.getMessage()));
        }
    }

    private List<String> parseStringArray(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        com.fasterxml.jackson.databind.JsonNode arr = node.path(fieldName);
        if (arr.isArray()) {
            List<String> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode item : arr) {
                String val = item.asText();
                if (val != null && !val.isBlank()) {
                    list.add(val);
                }
            }
            return list;
        }
        return List.of();
    }
}
