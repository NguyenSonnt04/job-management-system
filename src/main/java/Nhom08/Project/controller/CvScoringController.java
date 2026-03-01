package Nhom08.Project.controller;

import Nhom08.Project.entity.*;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.CvScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/cv-scoring")
public class CvScoringController {

    @Autowired private CvScoringService scoringService;
    @Autowired private AuthService      authService;

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

    // ── Mappers ──────────────────────────────────────────
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
