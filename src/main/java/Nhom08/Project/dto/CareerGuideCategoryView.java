package Nhom08.Project.dto;

public record CareerGuideCategoryView(
    Long id,
    String name,
    String slug,
    String description,
    String icon,
    String accentColor,
    long articleCount
) {}
