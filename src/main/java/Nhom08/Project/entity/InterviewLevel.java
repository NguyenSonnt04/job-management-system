package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reference data: cấp độ kinh nghiệm trong phỏng vấn.
 * Table: interview_levels
 * Seed: interview_levels_seed.sql
 */
@Entity
@Table(name = "interview_levels")
public class InterviewLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_key", nullable = false, unique = true, length = 50)
    private String levelKey;          // "fresher" | "junior" | "middle" | "senior" | "lead"

    @Column(name = "level_name", nullable = false, length = 80)
    private String levelName;         // "Fresher (0-1 năm)"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewLevel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLevelKey() { return levelKey; }
    public void setLevelKey(String levelKey) { this.levelKey = levelKey; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
