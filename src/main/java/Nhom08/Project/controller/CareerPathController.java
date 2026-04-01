package Nhom08.Project.controller;

import Nhom08.Project.dto.CareerPathDetailView;
import Nhom08.Project.dto.CareerPathHomeView;
import Nhom08.Project.dto.CareerPathSummaryView;
import Nhom08.Project.service.CareerPathService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career-path")
public class CareerPathController {

    private final CareerPathService careerPathService;

    public CareerPathController(CareerPathService careerPathService) {
        this.careerPathService = careerPathService;
    }

    @GetMapping("/home")
    public ResponseEntity<CareerPathHomeView> home() {
        return ResponseEntity.ok(careerPathService.getHomeView());
    }

    @GetMapping("/paths")
    public ResponseEntity<List<CareerPathSummaryView>> paths(
            @RequestParam(required = false) String industry
    ) {
        if (industry != null && !industry.isEmpty()) {
            return ResponseEntity.ok(careerPathService.getPathsByIndustry(industry));
        }
        return ResponseEntity.ok(careerPathService.getAllPaths());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<CareerPathSummaryView>> featured() {
        return ResponseEntity.ok(careerPathService.getFeaturedPaths());
    }

    @GetMapping("/paths/{slug}")
    public ResponseEntity<CareerPathDetailView> path(@PathVariable String slug) {
        return ResponseEntity.ok(careerPathService.getPathDetail(slug));
    }
}
