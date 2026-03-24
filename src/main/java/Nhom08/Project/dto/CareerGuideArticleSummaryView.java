package Nhom08.Project.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CareerGuideArticleSummaryView(
    Long id,
    String title,
    String slug,
    String excerpt,
    String coverImageUrl,
    String coverImageAlt,
    Long categoryId,
    String categoryName,
    String categorySlug,
    String categoryIcon,
    String categoryAccentColor,
    String authorName,
    LocalDateTime publishedAt,
    Integer readTimeMinutes,
    boolean featured,
    boolean pinned,
    long views,
    String status,
    List<CareerGuideTagView> tags
) {}
