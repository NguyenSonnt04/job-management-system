package Nhom08.Project.controller;

import Nhom08.Project.dto.CareerGuideArticleDetailView;
import Nhom08.Project.dto.CareerGuideArticleSummaryView;
import Nhom08.Project.dto.CareerGuideHomeView;
import Nhom08.Project.service.CareerGuideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/career-guide")
public class CareerGuideController {

    private final CareerGuideService careerGuideService;

    public CareerGuideController(CareerGuideService careerGuideService) {
        this.careerGuideService = careerGuideService;
    }

    @GetMapping("/home")
    public ResponseEntity<CareerGuideHomeView> home() {
        return ResponseEntity.ok(careerGuideService.getHomeView());
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories() {
        return ResponseEntity.ok(careerGuideService.getCategories());
    }

    @GetMapping("/tags")
    public ResponseEntity<?> tags() {
        return ResponseEntity.ok(careerGuideService.getTags());
    }

    @GetMapping("/articles")
    public ResponseEntity<List<CareerGuideArticleSummaryView>> articles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(careerGuideService.getPublishedArticles(category, tag, q, limit));
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<CareerGuideArticleDetailView> article(@PathVariable String slug) {
        return ResponseEntity.ok(careerGuideService.getArticleDetail(slug));
    }
}
