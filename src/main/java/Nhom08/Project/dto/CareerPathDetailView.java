package Nhom08.Project.dto;

import java.util.List;

public record CareerPathDetailView(
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
    Boolean featured,
    Long views,
    List<CareerPathStageView> stages
) {}
