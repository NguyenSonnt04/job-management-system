package Nhom08.Project.dto;

public record CareerPathSummaryView(
    Long id,
    String title,
    String slug,
    String description,
    String iconUrl,
    String accentColor,
    String industryField,
    Integer averageSalaryMin,
    Integer averageSalaryMax,
    Integer totalDurationMonths,
    Integer stageCount,
    Boolean featured,
    Long views
) {}
