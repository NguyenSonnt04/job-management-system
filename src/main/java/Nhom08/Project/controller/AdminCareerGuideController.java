package Nhom08.Project.controller;

import Nhom08.Project.dto.CareerGuideArticleSummaryView;
import Nhom08.Project.dto.CareerGuideArticleUpsertRequest;
import Nhom08.Project.service.CareerGuideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/career-guide")
public class AdminCareerGuideController {

    private final CareerGuideService careerGuideService;

    public AdminCareerGuideController(CareerGuideService careerGuideService) {
        this.careerGuideService = careerGuideService;
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
    public ResponseEntity<List<CareerGuideArticleSummaryView>> articles() {
        return ResponseEntity.ok(careerGuideService.getAdminArticles());
    }

    @PostMapping("/articles")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CareerGuideArticleUpsertRequest request) {
        CareerGuideArticleSummaryView saved = careerGuideService.createArticle(request);
        return ResponseEntity.ok(Map.of("success", true, "article", saved));
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody CareerGuideArticleUpsertRequest request
    ) {
        CareerGuideArticleSummaryView saved = careerGuideService.updateArticle(id, request);
        return ResponseEntity.ok(Map.of("success", true, "article", saved));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        careerGuideService.deleteArticle(id);
        return ResponseEntity.ok(Map.of("success", true, "deletedId", id));
    }
}
