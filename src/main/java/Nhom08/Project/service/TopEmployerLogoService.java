package Nhom08.Project.service;

import Nhom08.Project.dto.AdminTopEmployerLogoView;
import Nhom08.Project.dto.TopEmployerLogoView;
import Nhom08.Project.entity.TopEmployerLogo;
import Nhom08.Project.repository.TopEmployerLogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class TopEmployerLogoService {

    private final TopEmployerLogoRepository topEmployerLogoRepository;
    private final FirebaseImageStorageService imageStorageService;

    public TopEmployerLogoService(
            TopEmployerLogoRepository topEmployerLogoRepository,
            FirebaseImageStorageService imageStorageService) {
        this.topEmployerLogoRepository = topEmployerLogoRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<AdminTopEmployerLogoView> getAdminLogos() {
        return topEmployerLogoRepository.findAllByOrderByDisplayOrderAscUpdatedAtDesc()
                .stream()
                .map(this::toAdminView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopEmployerLogoView> getActiveLogos() {
        return topEmployerLogoRepository.findActiveLogos(LocalDateTime.now())
                .stream()
                .map(this::toPublicView)
                .toList();
    }

    @Transactional
    public AdminTopEmployerLogoView createLogo(
            String name,
            String targetUrl,
            Integer displayOrder,
            Boolean active,
            LocalDateTime startAt,
            LocalDateTime endAt,
            MultipartFile imageFile) throws IOException {

        validateSchedule(startAt, endAt);
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn logo nhà tuyển dụng");
        }

        TopEmployerLogo logo = new TopEmployerLogo();
        logo.setName(requireName(name));
        logo.setTargetUrl(normalizeTargetUrl(targetUrl));
        logo.setDisplayOrder(normalizeDisplayOrder(displayOrder));
        logo.setActive(active == null || active);
        logo.setStartAt(startAt);
        logo.setEndAt(endAt);
        logo.setImageUrl(imageStorageService.uploadImage(imageFile, "top-employer-logos"));

        return toAdminView(topEmployerLogoRepository.save(logo));
    }

    @Transactional
    public AdminTopEmployerLogoView updateLogo(
            Long id,
            String name,
            String targetUrl,
            Integer displayOrder,
            Boolean active,
            LocalDateTime startAt,
            LocalDateTime endAt,
            MultipartFile imageFile) throws IOException {

        validateSchedule(startAt, endAt);

        TopEmployerLogo logo = topEmployerLogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy logo nhà tuyển dụng"));

        logo.setName(requireName(name));
        logo.setTargetUrl(normalizeTargetUrl(targetUrl));
        logo.setDisplayOrder(normalizeDisplayOrder(displayOrder));
        logo.setActive(active == null || active);
        logo.setStartAt(startAt);
        logo.setEndAt(endAt);

        if (imageFile != null && !imageFile.isEmpty()) {
            logo.setImageUrl(imageStorageService.uploadImage(imageFile, "top-employer-logos"));
        }

        return toAdminView(topEmployerLogoRepository.save(logo));
    }

    @Transactional
    public AdminTopEmployerLogoView updateActive(Long id, boolean active) {
        TopEmployerLogo logo = topEmployerLogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy logo nhà tuyển dụng"));
        logo.setActive(active);
        return toAdminView(topEmployerLogoRepository.save(logo));
    }

    @Transactional
    public void deleteLogo(Long id) {
        if (!topEmployerLogoRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy logo nhà tuyển dụng");
        }
        topEmployerLogoRepository.deleteById(id);
    }

    private String requireName(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Tên nhà tuyển dụng không được để trống");
        }
        return normalized;
    }

    private Integer normalizeDisplayOrder(Integer displayOrder) {
        return displayOrder == null ? 0 : displayOrder;
    }

    private String normalizeTargetUrl(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        if (normalized.startsWith("/") || normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        if (normalized.matches("^[\\w.-]+\\.[a-zA-Z]{2,}.*$")) {
            return "https://" + normalized;
        }
        throw new IllegalArgumentException("Link nhà tuyển dụng không hợp lệ");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateSchedule(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }

    private AdminTopEmployerLogoView toAdminView(TopEmployerLogo logo) {
        return new AdminTopEmployerLogoView(
                logo.getId(),
                logo.getName(),
                toImageUrl(logo.getImageUrl()),
                logo.getTargetUrl(),
                logo.getDisplayOrder(),
                logo.getActive(),
                logo.getStartAt(),
                logo.getEndAt(),
                logo.getCreatedAt(),
                logo.getUpdatedAt()
        );
    }

    private TopEmployerLogoView toPublicView(TopEmployerLogo logo) {
        return new TopEmployerLogoView(
                logo.getId(),
                logo.getName(),
                toImageUrl(logo.getImageUrl()),
                logo.getTargetUrl(),
                logo.getDisplayOrder()
        );
    }

    private String toImageUrl(String storedValue) {
        if (!StringUtils.hasText(storedValue)) {
            return storedValue;
        }
        String normalized = storedValue.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("/api/uploads/image?")) {
            return storedValue;
        }
        return "/api/uploads/image?path=" + java.net.URLEncoder.encode(storedValue, StandardCharsets.UTF_8);
    }
}
