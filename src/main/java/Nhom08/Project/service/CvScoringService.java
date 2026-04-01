package Nhom08.Project.service;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.CvJobMatchRepository;
import Nhom08.Project.repository.CvScoringCriteriaRepository;
import Nhom08.Project.repository.CvScoreSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class CvScoringService {

    @Autowired private CvScoringCriteriaRepository criteriaRepo;
    @Autowired private CvScoreSessionRepository    sessionRepo;
    @Autowired private CvJobMatchRepository        matchRepo;
    @Autowired private GeminiService               geminiService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Upload CV file → call Gemini → parse → save to DB → return session ID.
     */
    @Transactional
    public CvScoreSession scoreAndSave(User user, MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename() != null
            ? file.getOriginalFilename() : "cv";
        String ext = originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
            : "pdf";

        // Validate file type
        if (!ext.equals("pdf") && !ext.equals("docx") && !ext.equals("doc")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file PDF và DOCX");
        }

        // Max 5 MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File quá lớn, tối đa 5MB");
        }

        // Get active criteria from DB
        List<CvScoringCriteria> criteriaList = criteriaRepo.findByActiveTrueOrderByDisplayOrderAsc();
        if (criteriaList.isEmpty()) {
            throw new IllegalStateException("Chưa có tiêu chí chấm điểm trong DB. Vui lòng thêm vào bảng cv_scoring_criteria.");
        }

        // Call Gemini
        String mimeType = ext.equals("pdf") ? "application/pdf"
            : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        String geminiJson = geminiService.scoreCvFile(file.getBytes(), mimeType, criteriaList);

        // Parse Gemini response
        JsonNode root = mapper.readTree(geminiJson);

        // Create session
        CvScoreSession session = new CvScoreSession(user, originalName, ext);
        session.setTotalScore(root.path("totalScore").asInt(0));
        session.setMaxTotalScore(root.path("maxTotalScore").asInt(100));
        session.setOverallFeedback(root.path("overallFeedback").asText(""));
        sessionRepo.save(session);

        // Parse per-criteria results
        JsonNode criteriaNode = root.path("criteria");
        List<CvScoreResult> results = new ArrayList<>();
        for (CvScoringCriteria c : criteriaList) {
            JsonNode cNode = criteriaNode.path(c.getName());
            int score = cNode.path("score").asInt(0);
            String feedback = cNode.path("feedback").asText("");
            results.add(new CvScoreResult(session, c, score, feedback));
        }
        session.setResults(results);

        // Parse strengths
        List<CvScorePoint> points = new ArrayList<>();
        JsonNode strengthsNode = root.path("strengths");
        if (strengthsNode.isArray()) {
            for (int i = 0; i < strengthsNode.size(); i++) {
                points.add(new CvScorePoint(session, CvScorePoint.PointType.STRENGTH,
                    strengthsNode.get(i).asText(), i + 1));
            }
        }

        // Parse weaknesses
        JsonNode weaknessesNode = root.path("weaknesses");
        if (weaknessesNode.isArray()) {
            for (int i = 0; i < weaknessesNode.size(); i++) {
                points.add(new CvScorePoint(session, CvScorePoint.PointType.WEAKNESS,
                    weaknessesNode.get(i).asText(), i + 1));
            }
        }
        session.setPoints(points);

        return sessionRepo.save(session);
    }

    /**
     * Score a saved CV from its JSON content text (no file upload needed).
     */
    @Transactional
    public CvScoreSession scoreFromText(User user, String cvText, String fileName) throws Exception {
        List<CvScoringCriteria> criteriaList = criteriaRepo.findByActiveTrueOrderByDisplayOrderAsc();
        if (criteriaList.isEmpty()) {
            throw new IllegalStateException("Chưa có tiêu chí chấm điểm trong DB. Vui lòng thêm vào bảng cv_scoring_criteria.");
        }

        String geminiJson = geminiService.scoreCvText(cvText, criteriaList);
        JsonNode root = mapper.readTree(geminiJson);

        CvScoreSession session = new CvScoreSession(user, fileName, "json");
        session.setTotalScore(root.path("totalScore").asInt(0));
        session.setMaxTotalScore(root.path("maxTotalScore").asInt(100));
        session.setOverallFeedback(root.path("overallFeedback").asText(""));
        sessionRepo.save(session);

        JsonNode criteriaNode = root.path("criteria");
        List<CvScoreResult> results = new ArrayList<>();
        for (CvScoringCriteria c : criteriaList) {
            JsonNode cNode = criteriaNode.path(c.getName());
            int score = cNode.path("score").asInt(0);
            String feedback = cNode.path("feedback").asText("");
            results.add(new CvScoreResult(session, c, score, feedback));
        }
        session.setResults(results);

        List<CvScorePoint> points = new ArrayList<>();
        JsonNode strengthsNode = root.path("strengths");
        if (strengthsNode.isArray()) {
            for (int i = 0; i < strengthsNode.size(); i++) {
                points.add(new CvScorePoint(session, CvScorePoint.PointType.STRENGTH,
                    strengthsNode.get(i).asText(), i + 1));
            }
        }
        JsonNode weaknessesNode = root.path("weaknesses");
        if (weaknessesNode.isArray()) {
            for (int i = 0; i < weaknessesNode.size(); i++) {
                points.add(new CvScorePoint(session, CvScorePoint.PointType.WEAKNESS,
                    weaknessesNode.get(i).asText(), i + 1));
            }
        }
        session.setPoints(points);

        return sessionRepo.save(session);
    }

    /** Get all sessions for a user. */
    public List<CvScoreSession> getUserHistory(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Get one session by ID (with auth check). */
    public CvScoreSession getSession(Long sessionId, Long userId) {
        CvScoreSession session = sessionRepo.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chấm điểm"));
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem phiên này");
        }
        return session;
    }

    /** Delete one session by ID (with auth check). */
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        CvScoreSession session = sessionRepo.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chấm điểm"));
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa phiên này");
        }

        // Match cache is stored in a separate table, so remove it first.
        matchRepo.deleteBySessionId(sessionId);
        sessionRepo.delete(session);
    }

    /** Get all active criteria. */
    public List<CvScoringCriteria> getCriteria() {
        return criteriaRepo.findByActiveTrueOrderByDisplayOrderAsc();
    }
}
