package Nhom08.Project.entity;

import jakarta.persistence.*;

/**
 * Score for one criteria in a session.
 * Table: cv_score_results
 */
@Entity
@Table(name = "cv_score_results")
public class CvScoreResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CvScoreSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criteria_id", nullable = false)
    private CvScoringCriteria criteria;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    // Constructors
    public CvScoreResult() {}

    public CvScoreResult(CvScoreSession session, CvScoringCriteria criteria, Integer score, String feedback) {
        this.session  = session;
        this.criteria = criteria;
        this.score    = score;
        this.feedback = feedback;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CvScoreSession getSession() { return session; }
    public void setSession(CvScoreSession session) { this.session = session; }

    public CvScoringCriteria getCriteria() { return criteria; }
    public void setCriteria(CvScoringCriteria criteria) { this.criteria = criteria; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
