package Nhom08.Project.controller;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.UserCvRepository;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API cho tính năng Mock Interview.
 *
 * Endpoints:
 *   GET  /api/interview/config          — roles, levels, types (reference data)
 *   POST /api/interview/session/start   — tạo phiên mới, trả sessionId
 *   POST /api/interview/session/{id}/chat  — gửi message, nhận reply Claude
 *   POST /api/interview/session/{id}/end   — kết thúc + đánh giá
 *   GET  /api/interview/session/{id}    — chi tiết phiên + messages + result
 *   GET  /api/interview/history         — lịch sử phỏng vấn của user
 *   GET  /api/interview/questions       — câu hỏi ngân hàng (chế độ tự luyện)
 */
@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @Autowired private InterviewService  interviewService;
    @Autowired private AuthService       authService;
    @Autowired private UserCvRepository  userCvRepository;

    // ── Helper ───────────────────────────────────────────────────────
    private Optional<User> currentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        return authService.findByEmail(auth.getName());
    }

    // ================================================================
    // GET /api/interview/config
    // Trả về toàn bộ reference data để frontend render form
    // ================================================================
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        List<Map<String, Object>> roles = interviewService.getAllActiveRoles().stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",           r.getId());
                    m.put("roleKey",      r.getRoleKey());
                    m.put("roleName",     r.getRoleName());
                    m.put("category",     r.getCategory());
                    m.put("categoryKey",  r.getCategoryKey());
                    m.put("iconClass",    r.getIconClass());
                    return m;
                }).toList();

        List<Map<String, Object>> levels = interviewService.getAllActiveLevels().stream()
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("levelKey",  l.getLevelKey());
                    m.put("levelName", l.getLevelName());
                    return m;
                }).toList();

        List<Map<String, Object>> types = interviewService.getAllActiveTypes().stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("typeKey",     t.getTypeKey());
                    m.put("typeName",    t.getTypeName());
                    m.put("description", t.getDescription());
                    m.put("iconClass",   t.getIconClass());
                    return m;
                }).toList();

        return ResponseEntity.ok(Map.of(
                "roles",  roles,
                "levels", levels,
                "types",  types
        ));
    }

    // ================================================================
    // POST /api/interview/session/start
    // Body: { mode, roleKey, levelKey, typeKey, cvFileName?, hasCv? }
    // ================================================================
    @PostMapping("/session/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @RequestBody Map<String, Object> req,
            Authentication auth) {
        try {
            String mode      = str(req, "mode",      "ai");
            String roleKey   = str(req, "roleKey",   "");
            String levelKey  = str(req, "levelKey",  "fresher");
            String typeKey   = str(req, "typeKey",   "mixed");
            String cvFileName= str(req, "cvFileName","");
            boolean hasCv    = Boolean.TRUE.equals(req.get("hasCv"));

            if (roleKey.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "roleKey là bắt buộc"));
            }

            User user = currentUser(auth).orElse(null);
            InterviewSession session = interviewService.createSession(
                    user, mode, roleKey, levelKey, typeKey, cvFileName, hasCv);

            return ResponseEntity.ok(Map.of(
                    "sessionId", session.getId(),
                    "roleName",  session.getRoleName(),
                    "mode",      session.getMode()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================================================
    // POST /api/interview/session/{id}/chat
    // Body: { messages: [{role,content}], cvContext? }
    // ================================================================
    @PostMapping("/session/{sessionId}/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> req) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages =
                    (List<Map<String, String>>) req.getOrDefault("messages", List.of());
            String cvContext = str(req, "cvContext", "");

            String reply = interviewService.chat(sessionId, messages, cvContext);
            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================================================
    // POST /api/interview/session/{id}/end
    // Body: { messages: [{role,content}] }
    // ================================================================
    @PostMapping("/session/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> req) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages =
                    (List<Map<String, String>>) req.getOrDefault("messages", List.of());

            InterviewResult result = interviewService.endAndEvaluate(sessionId, messages);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("sessionId",          result.getSession().getId());
            resp.put("overallScore",        result.getOverallScore());
            resp.put("scoreLabel",          result.getScoreLabel());
            resp.put("recommendation",      result.getRecommendation());
            resp.put("strengths",           parseJsonArray(result.getStrengths()));
            resp.put("improvements",        parseJsonArray(result.getImprovements()));
            resp.put("categories", Map.of(
                    "communication",  nullToZero(result.getScoreCommunication()),
                    "knowledge",      nullToZero(result.getScoreKnowledge()),
                    "problemSolving", nullToZero(result.getScoreProblemSolving()),
                    "attitude",       nullToZero(result.getScoreAttitude())
            ));
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================================================
    // GET /api/interview/session/{id}
    // Chi tiết phiên + tin nhắn + kết quả
    // ================================================================
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(
            @PathVariable Long sessionId,
            Authentication auth) {
        return interviewService.getSessionById(sessionId)
                .map(session -> {
                    // Kiểm tra quyền: chỉ chủ phiên hoặc guest (user null) mới xem được
                    Optional<User> caller = currentUser(auth);
                    if (session.getUser() != null && caller.isPresent()) {
                        if (!session.getUser().getId().equals(caller.get().getId())) {
                            return ResponseEntity.<Map<String, Object>>status(403)
                                    .body(Map.of("error", "Không có quyền xem phiên này"));
                        }
                    }

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("sessionId",  session.getId());
                    resp.put("roleName",   session.getRoleName());
                    resp.put("levelKey",   session.getLevelKey());
                    resp.put("typeKey",    session.getTypeKey());
                    resp.put("mode",       session.getMode());
                    resp.put("status",     session.getStatus());
                    resp.put("createdAt",  session.getCreatedAt());
                    resp.put("completedAt",session.getCompletedAt());

                    // Messages
                    List<Map<String, String>> msgs = interviewService
                            .getMessagesBySession(sessionId).stream()
                            .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                            .collect(Collectors.toList());
                    resp.put("messages", msgs);

                    // Result (nếu có)
                    interviewService.getResultBySession(sessionId).ifPresent(r -> {
                        resp.put("result", Map.of(
                                "overallScore",  r.getOverallScore(),
                                "scoreLabel",    r.getScoreLabel(),
                                "recommendation",r.getRecommendation() != null ? r.getRecommendation() : "",
                                "strengths",     parseJsonArray(r.getStrengths()),
                                "improvements",  parseJsonArray(r.getImprovements()),
                                "categories", Map.of(
                                        "communication",  nullToZero(r.getScoreCommunication()),
                                        "knowledge",      nullToZero(r.getScoreKnowledge()),
                                        "problemSolving", nullToZero(r.getScoreProblemSolving()),
                                        "attitude",       nullToZero(r.getScoreAttitude())
                                )
                        ));
                    });

                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ================================================================
    // GET /api/interview/my-cvs
    // Danh sách CV đã lưu của user (từ CV editor)
    // ================================================================
    @GetMapping("/my-cvs")
    public ResponseEntity<?> getMyCvs(Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("cvs", List.of()));
        }

        List<Map<String, Object>> cvs = userCvRepository
                .findByUserIdOrderByUpdatedAtDesc(userOpt.get().getId())
                .stream()
                .map(cv -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          cv.getId());
                    m.put("cvName",      cv.getCvName() != null ? cv.getCvName() : "CV chưa đặt tên");
                    m.put("templateName",cv.getTemplateName());
                    m.put("updatedAt",   cv.getUpdatedAt());
                    return m;
                }).toList();

        return ResponseEntity.ok(Map.of("cvs", cvs));
    }

    // ================================================================
    // GET /api/interview/history
    // Lịch sử phỏng vấn của user đang đăng nhập
    // ================================================================
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication auth) {
        Optional<User> userOpt = currentUser(auth);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để xem lịch sử"));
        }

        List<Map<String, Object>> history = interviewService
                .getHistoryByUser(userOpt.get().getId()).stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("sessionId",  s.getId());
                    m.put("roleName",   s.getRoleName());
                    m.put("levelKey",   s.getLevelKey());
                    m.put("typeKey",    s.getTypeKey());
                    m.put("mode",       s.getMode());
                    m.put("status",     s.getStatus());
                    m.put("createdAt",  s.getCreatedAt());
                    m.put("completedAt",s.getCompletedAt());
                    // Kèm điểm nếu đã có kết quả
                    interviewService.getResultBySession(s.getId()).ifPresent(r ->
                            m.put("overallScore", r.getOverallScore()));
                    return m;
                }).toList();

        return ResponseEntity.ok(Map.of(
                "history",       history,
                "totalSessions", history.size()
        ));
    }

    // ================================================================
    // GET /api/interview/questions?typeKey=hr&roleKey=frontend_developer&limit=10
    // Lấy câu hỏi cho chế độ tự luyện
    // ================================================================
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(
            @RequestParam(defaultValue = "mixed") String typeKey,
            @RequestParam(defaultValue = "")      String roleKey,
            @RequestParam(defaultValue = "10")    int limit) {
        try {
            List<InterviewQuestionBank> questions =
                    interviewService.getQuestionsForSession(typeKey, roleKey, Math.min(limit, 30));

            List<Map<String, Object>> result = questions.stream().map(q -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",          q.getId());
                m.put("typeKey",     q.getTypeKey());
                m.put("category",    q.getCategory());
                m.put("question",    q.getQuestion());
                m.put("hint",        q.getHint());
                m.put("modelAnswer", q.getModelAnswer());
                m.put("difficulty",  q.getDifficulty());
                return m;
            }).toList();

            return ResponseEntity.ok(Map.of("questions", result, "total", result.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── Utilities ──────────────────────────────────────────────────────
    private String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return (v instanceof String s && !s.isBlank()) ? s : def;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private int nullToZero(Integer v) { return v != null ? v : 0; }
}
