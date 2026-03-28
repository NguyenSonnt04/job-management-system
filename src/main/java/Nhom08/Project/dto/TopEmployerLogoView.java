package Nhom08.Project.dto;

public record TopEmployerLogoView(
        Long id,
        String name,
        String imageUrl,
        String targetUrl,
        Integer displayOrder
) {
}
