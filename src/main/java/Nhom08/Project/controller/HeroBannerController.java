package Nhom08.Project.controller;

import Nhom08.Project.dto.HeroBannerView;
import Nhom08.Project.service.HeroBannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hero-banners")
public class HeroBannerController {

    private final HeroBannerService heroBannerService;

    public HeroBannerController(HeroBannerService heroBannerService) {
        this.heroBannerService = heroBannerService;
    }

    @GetMapping
    public ResponseEntity<List<HeroBannerView>> listActiveBanners() {
        return ResponseEntity.ok(heroBannerService.getActiveBanners());
    }
}
