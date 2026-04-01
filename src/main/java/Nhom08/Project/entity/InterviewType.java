package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reference data: loại phỏng vấn.
 * Table: interview_types
 * Seed: interview_types_seed.sql
 */
@Entity
@Table(name = "interview_types")
public class InterviewType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_key", nullable = false, unique = true, length = 50)
    private String typeKey;           // "hr" | "technical" | "behavioral" | "mixed"

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;          // "HR & Soft Skills"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_class", length = 80)
    private String iconClass;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeKey() { return typeKey; }
    public void setTypeKey(String typeKey) { this.typeKey = typeKey; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
