package Nhom08.Project.dto;

public record HeroBannerView(
        Long id,
        String name,
        String imageUrl,
        String targetUrl,
        Integer displayOrder
) {
}
