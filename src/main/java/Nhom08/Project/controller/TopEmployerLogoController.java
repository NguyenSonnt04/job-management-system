package Nhom08.Project.controller;

import Nhom08.Project.dto.TopEmployerLogoView;
import Nhom08.Project.service.TopEmployerLogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/top-employer-logos")
public class TopEmployerLogoController {

    private final TopEmployerLogoService topEmployerLogoService;

    public TopEmployerLogoController(TopEmployerLogoService topEmployerLogoService) {
        this.topEmployerLogoService = topEmployerLogoService;
    }

    @GetMapping
    public ResponseEntity<List<TopEmployerLogoView>> listActiveLogos() {
        return ResponseEntity.ok(topEmployerLogoService.getActiveLogos());
    }
}
