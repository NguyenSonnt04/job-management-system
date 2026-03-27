package Nhom08.Project.controller;

import Nhom08.Project.dto.AdminHeroBannerView;
import Nhom08.Project.service.HeroBannerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hero-banners")
public class AdminHeroBannerController {

    private final HeroBannerService heroBannerService;

    public AdminHeroBannerController(HeroBannerService heroBannerService) {
        this.heroBannerService = heroBannerService;
    }

    @GetMapping
    public ResponseEntity<List<AdminHeroBannerView>> list() {
        return ResponseEntity.ok(heroBannerService.getAdminBanners());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam("name") String name,
            @RequestParam(value = "targetUrl", required = false) String targetUrl,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            AdminHeroBannerView banner = heroBannerService.createBanner(
                    name, targetUrl, displayOrder, active, startAt, endAt, imageFile);
            return ResponseEntity.ok(Map.of("success", true, "banner", banner));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Không thể tải ảnh banner lên"));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "targetUrl", required = false) String targetUrl,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            AdminHeroBannerView banner = heroBannerService.updateBanner(
                    id, name, targetUrl, displayOrder, active, startAt, endAt, imageFile);
            return ResponseEntity.ok(Map.of("success", true, "banner", banner));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Không thể tải ảnh banner lên"));
        }
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> updateActive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            AdminHeroBannerView banner = heroBannerService.updateActive(id, active);
            return ResponseEntity.ok(Map.of("success", true, "banner", banner));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            heroBannerService.deleteBanner(id);
            return ResponseEntity.ok(Map.of("success", true, "deletedId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
