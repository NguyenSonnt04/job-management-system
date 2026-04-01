package Nhom08.Project.dto;

import java.util.List;

public record CareerPathHomeView(
    List<CareerPathSummaryView> featuredPaths,
    List<CareerPathSummaryView> allPaths,
    List<String> industries
) {}
