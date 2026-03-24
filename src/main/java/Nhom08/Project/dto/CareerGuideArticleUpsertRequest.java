package Nhom08.Project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CareerGuideArticleUpsertRequest(
    @NotBlank String title,
    String slug,
    @NotNull Long categoryId,
    @NotBlank String excerpt,
    @NotBlank String contentHtml,
    String coverImageUrl,
    String coverImageAlt,
    String authorName,
    Boolean featured,
    Boolean pinned,
    Integer readTimeMinutes,
    String status,
    List<Long> tagIds
) {}
