package Nhom08.Project.dto;

import java.util.List;

public record CareerGuideArticleDetailView(
    CareerGuideArticleSummaryView article,
    String contentHtml,
    List<CareerGuideTagView> tags,
    List<CareerGuideArticleSummaryView> relatedArticles
) {}
