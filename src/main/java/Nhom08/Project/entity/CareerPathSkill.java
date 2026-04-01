package Nhom08.Project.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "career_path_skills",
    indexes = {
        @Index(name = "idx_career_path_skill_stage", columnList = "stage_id"),
        @Index(name = "idx_career_path_skill_order", columnList = "stage_id, skill_order")
    }
)
public class CareerPathSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private CareerPathStage stage;

    @Column(name = "skill_order", nullable = false)
    private Integer skillOrder;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Column(name = "proficiency_level", length = 20)
    private String proficiencyLevel;

    @Column(length = 500)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (skillOrder == null) skillOrder = 1;
        if (isRequired == null) isRequired = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CareerPathStage getStage() { return stage; }
    public void setStage(CareerPathStage stage) { this.stage = stage; }
    public Integer getSkillOrder() { return skillOrder; }
    public void setSkillOrder(Integer skillOrder) { this.skillOrder = skillOrder; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
