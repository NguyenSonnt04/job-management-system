package Nhom08.Project.dto;

import java.util.List;

public record CareerGuideHomeView(
    List<CareerGuideCategoryView> categories,
    List<CareerGuideArticleSummaryView> articles
) {}
