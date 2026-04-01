package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_templates")
public class CvTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Preview background color (hex or css value)
    @Column(name = "preview_color", length = 50)
    private String previewColor;

    // Badge label: ATS-Friendly, Creative, Premium, Academic, Bold, Top Rated...
    @Column(name = "badge_label", length = 100)
    private String badgeLabel;

    // Badge style: background color (hex)
    @Column(name = "badge_bg_color", length = 50)
    private String badgeBgColor;

    // Badge text color (hex)
    @Column(name = "badge_text_color", length = 50)
    private String badgeTextColor;

    // Category/Industry filter
    @Column(name = "category", length = 100)
    private String category;

    // Style tag: professional, classic, modern, impactful, creative, ats, harvard
    @Column(name = "style_tag", length = 50)
    private String styleTag;

    // Full CV content stored as JSON string
    // Structure: { name, subtitle, phone, email, address, summary,
    //              education:[{school,degree,location,period,details:[]}],
    //              experience:[{company,role,location,period,details:[]}],
    //              skills:[{category,items:[]}] }
    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;

    // Sort order for display
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "latest_version_no")
    private Integer latestVersionNo = 1;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CvTemplate() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPreviewColor() { return previewColor; }
    public void setPreviewColor(String previewColor) { this.previewColor = previewColor; }

    public String getBadgeLabel() { return badgeLabel; }
    public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }

    public String getBadgeBgColor() { return badgeBgColor; }
    public void setBadgeBgColor(String badgeBgColor) { this.badgeBgColor = badgeBgColor; }

    public String getBadgeTextColor() { return badgeTextColor; }
    public void setBadgeTextColor(String badgeTextColor) { this.badgeTextColor = badgeTextColor; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStyleTag() { return styleTag; }
    public void setStyleTag(String styleTag) { this.styleTag = styleTag; }

    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getLatestVersionNo() { return latestVersionNo; }
    public void setLatestVersionNo(Integer latestVersionNo) { this.latestVersionNo = latestVersionNo; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
