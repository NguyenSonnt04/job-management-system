package Nhom08.Project.service;

import Nhom08.Project.dto.AdminHeroBannerView;
import Nhom08.Project.dto.HeroBannerView;
import Nhom08.Project.entity.HeroBanner;
import Nhom08.Project.repository.HeroBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class HeroBannerService {

    private final HeroBannerRepository heroBannerRepository;
    private final FirebaseImageStorageService imageStorageService;

    public HeroBannerService(
            HeroBannerRepository heroBannerRepository,
            FirebaseImageStorageService imageStorageService) {
        this.heroBannerRepository = heroBannerRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<AdminHeroBannerView> getAdminBanners() {
        return heroBannerRepository.findAllByOrderByDisplayOrderAscUpdatedAtDesc()
                .stream()
                .map(this::toAdminView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HeroBannerView> getActiveBanners() {
        return heroBannerRepository.findActiveBanners(LocalDateTime.now())
                .stream()
                .map(this::toPublicView)
                .toList();
    }

    @Transactional
    public AdminHeroBannerView createBanner(
            String name,
            String targetUrl,
            Integer displayOrder,
            Boolean active,
            LocalDateTime startAt,
            LocalDateTime endAt,
            MultipartFile imageFile) throws IOException {

        validateSchedule(startAt, endAt);
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh banner");
        }

        HeroBanner banner = new HeroBanner();
        banner.setName(requireName(name));
        banner.setTargetUrl(normalizeTargetUrl(targetUrl));
        banner.setDisplayOrder(normalizeDisplayOrder(displayOrder));
        banner.setActive(active == null || active);
        banner.setStartAt(startAt);
        banner.setEndAt(endAt);
        banner.setImageUrl(imageStorageService.uploadImage(imageFile, "hero-banners"));

        return toAdminView(heroBannerRepository.save(banner));
    }

    @Transactional
    public AdminHeroBannerView updateBanner(
            Long id,
            String name,
            String targetUrl,
            Integer displayOrder,
            Boolean active,
            LocalDateTime startAt,
            LocalDateTime endAt,
            MultipartFile imageFile) throws IOException {

        validateSchedule(startAt, endAt);

        HeroBanner banner = heroBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner"));

        banner.setName(requireName(name));
        banner.setTargetUrl(normalizeTargetUrl(targetUrl));
        banner.setDisplayOrder(normalizeDisplayOrder(displayOrder));
        banner.setActive(active == null || active);
        banner.setStartAt(startAt);
        banner.setEndAt(endAt);

        if (imageFile != null && !imageFile.isEmpty()) {
            banner.setImageUrl(imageStorageService.uploadImage(imageFile, "hero-banners"));
        }

        return toAdminView(heroBannerRepository.save(banner));
    }

    @Transactional
    public AdminHeroBannerView updateActive(Long id, boolean active) {
        HeroBanner banner = heroBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner"));
        banner.setActive(active);
        return toAdminView(heroBannerRepository.save(banner));
    }

    @Transactional
    public void deleteBanner(Long id) {
        if (!heroBannerRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy banner");
        }
        heroBannerRepository.deleteById(id);
    }

    private String requireName(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Tên banner không được để trống");
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
        throw new IllegalArgumentException("Link banner không hợp lệ");
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

    private AdminHeroBannerView toAdminView(HeroBanner banner) {
        return new AdminHeroBannerView(
                banner.getId(),
                banner.getName(),
                toImageUrl(banner.getImageUrl()),
                banner.getTargetUrl(),
                banner.getDisplayOrder(),
                banner.getActive(),
                banner.getStartAt(),
                banner.getEndAt(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }

    private HeroBannerView toPublicView(HeroBanner banner) {
        return new HeroBannerView(
                banner.getId(),
                banner.getName(),
                toImageUrl(banner.getImageUrl()),
                banner.getTargetUrl(),
                banner.getDisplayOrder()
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
        return "/api/uploads/image?path=" + java.net.URLEncoder.encode(storedValue, java.nio.charset.StandardCharsets.UTF_8);
    }
}
