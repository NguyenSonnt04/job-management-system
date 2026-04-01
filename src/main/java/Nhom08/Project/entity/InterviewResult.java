package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu kết quả đánh giá cuối phiên phỏng vấn (do Claude AI trả về).
 * Table: interview_results
 *
 * Quan hệ 1-1 với interview_sessions.
 * Tách hoàn toàn khỏi bảng reference.
 */
@Entity
@Table(name = "interview_results",
       indexes = {
           @Index(name = "idx_ir_session_id",    columnList = "session_id"),
           @Index(name = "idx_ir_overall_score",  columnList = "overall_score"),
       })
public class InterviewResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private InterviewSession session;

    /** Điểm tổng (1-10) */
    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    /** Nhãn xếp loại: "Xuất sắc" | "Khá tốt" | "Trung bình" | "Cần cải thiện" */
    @Column(name = "score_label", length = 50)
    private String scoreLabel;

    /** Điểm giao tiếp (1-10) */
    @Column(name = "score_communication")
    private Integer scoreCommunication;

    /** Điểm kiến thức chuyên môn (1-10) */
    @Column(name = "score_knowledge")
    private Integer scoreKnowledge;

    /** Điểm giải quyết vấn đề (1-10) */
    @Column(name = "score_problem_solving")
    private Integer scoreProblemSolving;

    /** Điểm thái độ (1-10) */
    @Column(name = "score_attitude")
    private Integer scoreAttitude;

    /** Điểm mạnh — lưu dạng JSON array ["...","..."] */
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    /** Điểm cần cải thiện — lưu dạng JSON array */
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;

    /** Nhận xét tổng thể và lời khuyên */
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    /** Raw JSON từ Claude (backup để debug / re-render) */
    @Column(name = "raw_ai_response", columnDefinition = "MEDIUMTEXT")
    private String rawAiResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewResult() {}

    public InterviewResult(InterviewSession session, int overallScore, String scoreLabel) {
        this.session      = session;
        this.overallScore = overallScore;
        this.scoreLabel   = scoreLabel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InterviewSession getSession() { return session; }
    public void setSession(InterviewSession session) { this.session = session; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public String getScoreLabel() { return scoreLabel; }
    public void setScoreLabel(String scoreLabel) { this.scoreLabel = scoreLabel; }

    public Integer getScoreCommunication() { return scoreCommunication; }
    public void setScoreCommunication(Integer scoreCommunication) { this.scoreCommunication = scoreCommunication; }

    public Integer getScoreKnowledge() { return scoreKnowledge; }
    public void setScoreKnowledge(Integer scoreKnowledge) { this.scoreKnowledge = scoreKnowledge; }

    public Integer getScoreProblemSolving() { return scoreProblemSolving; }
    public void setScoreProblemSolving(Integer scoreProblemSolving) { this.scoreProblemSolving = scoreProblemSolving; }

    public Integer getScoreAttitude() { return scoreAttitude; }
    public void setScoreAttitude(Integer scoreAttitude) { this.scoreAttitude = scoreAttitude; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getRawAiResponse() { return rawAiResponse; }
    public void setRawAiResponse(String rawAiResponse) { this.rawAiResponse = rawAiResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
