package Nhom08.Project.dto;

import java.util.List;

public record CareerPathStageView(
    Long id,
    Integer stageOrder,
    String title,
    String description,
    String jobTitle,
    String experienceLevel,
    Integer durationMonths,
    Integer salaryMin,
    Integer salaryMax,
    String iconName,
    String iconColor,
    List<CareerPathSkillView> skills
) {}
