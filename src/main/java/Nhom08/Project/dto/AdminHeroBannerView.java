package Nhom08.Project.dto;

import java.time.LocalDateTime;

public record AdminHeroBannerView(
        Long id,
        String name,
        String imageUrl,
        String targetUrl,
        Integer displayOrder,
        Boolean active,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
