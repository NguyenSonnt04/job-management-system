package Nhom08.Project.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "career_paths",
    indexes = {
        @Index(name = "idx_career_path_slug", columnList = "slug", unique = true),
        @Index(name = "idx_career_path_active", columnList = "active")
    }
)
public class CareerPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "accent_color", length = 20)
    private String accentColor;

    @Column(name = "industry_field", length = 100)
    private String industryField;

    @Column(name = "average_salary_min")
    private Integer averageSalaryMin;

    @Column(name = "average_salary_max")
    private Integer averageSalaryMax;

    @Column(name = "total_duration_months")
    private Integer totalDurationMonths;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "views")
    private Long views = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "careerPath", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CareerPathStage> stages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (featured == null) featured = false;
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 0;
        if (views == null) views = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public String getIndustryField() { return industryField; }
    public void setIndustryField(String industryField) { this.industryField = industryField; }
    public Integer getAverageSalaryMin() { return averageSalaryMin; }
    public void setAverageSalaryMin(Integer averageSalaryMin) { this.averageSalaryMin = averageSalaryMin; }
    public Integer getAverageSalaryMax() { return averageSalaryMax; }
    public void setAverageSalaryMax(Integer averageSalaryMax) { this.averageSalaryMax = averageSalaryMax; }
    public Integer getTotalDurationMonths() { return totalDurationMonths; }
    public void setTotalDurationMonths(Integer totalDurationMonths) { this.totalDurationMonths = totalDurationMonths; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<CareerPathStage> getStages() { return stages; }
    public void setStages(List<CareerPathStage> stages) { this.stages = stages; }
}
