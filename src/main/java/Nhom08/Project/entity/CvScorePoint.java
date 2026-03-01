package Nhom08.Project.entity;

import jakarta.persistence.*;

/**
 * Stores individual strengths and weaknesses per session.
 * Table: cv_score_points
 */
@Entity
@Table(name = "cv_score_points")
public class CvScorePoint {

    public enum PointType { STRENGTH, WEAKNESS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CvScoreSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType pointType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Constructors
    public CvScorePoint() {}

    public CvScorePoint(CvScoreSession session, PointType pointType, String content, int displayOrder) {
        this.session      = session;
        this.pointType    = pointType;
        this.content      = content;
        this.displayOrder = displayOrder;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CvScoreSession getSession() { return session; }
    public void setSession(CvScoreSession session) { this.session = session; }

    public PointType getPointType() { return pointType; }
    public void setPointType(PointType pointType) { this.pointType = pointType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
