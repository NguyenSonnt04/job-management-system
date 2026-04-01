package Nhom08.Project.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "career_path_stages",
    indexes = {
        @Index(name = "idx_career_path_stage_career_path", columnList = "career_path_id"),
        @Index(name = "idx_career_path_stage_order", columnList = "career_path_id, stage_order")
    }
)
public class CareerPathStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_path_id", nullable = false)
    private CareerPath careerPath;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "icon_color", length = 20)
    private String iconColor;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CareerPathSkill> skills = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (stageOrder == null) stageOrder = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CareerPath getCareerPath() { return careerPath; }
    public void setCareerPath(CareerPath careerPath) { this.careerPath = careerPath; }
    public Integer getStageOrder() { return stageOrder; }
    public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }
    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }
    public List<CareerPathSkill> getSkills() { return skills; }
    public void setSkills(List<CareerPathSkill> skills) { this.skills = skills; }
}
