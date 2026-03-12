package Nhom08.Project.controller;

import Nhom08.Project.entity.CvTemplate;
import Nhom08.Project.repository.CvTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cv-templates")
public class CvTemplateController {

    @Autowired
    private CvTemplateRepository cvTemplateRepository;

    /** Public: get all active templates (for tao-cv-ai.html) */
    @GetMapping("/public")
    public ResponseEntity<List<CvTemplate>> getPublicTemplates(
            @RequestParam(required = false) String category) {
        List<CvTemplate> templates;
        if (category != null && !category.isBlank()) {
            templates = cvTemplateRepository.findByCategoryAndActiveTrueOrderBySortOrderAsc(category);
        } else {
            templates = cvTemplateRepository.findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
        }
        return ResponseEntity.ok(templates);
    }

    /** Admin: get all templates (including inactive) */
    @GetMapping
    public ResponseEntity<List<CvTemplate>> getAllTemplates() {
        return ResponseEntity.ok(cvTemplateRepository.findAllByOrderBySortOrderAscCreatedAtDesc());
    }

    /** Admin: get single template */
    @GetMapping("/{id}")
    public ResponseEntity<CvTemplate> getTemplate(@PathVariable Long id) {
        return cvTemplateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Admin: create template */
    @PostMapping
    public ResponseEntity<CvTemplate> createTemplate(@RequestBody CvTemplate template) {
        CvTemplate saved = cvTemplateRepository.save(template);
        return ResponseEntity.ok(saved);
    }

    /** Admin: update template */
    @PutMapping("/{id}")
    public ResponseEntity<CvTemplate> updateTemplate(
            @PathVariable Long id, @RequestBody CvTemplate body) {
        Optional<CvTemplate> opt = cvTemplateRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CvTemplate template = opt.get();
        template.setName(body.getName());
        template.setDescription(body.getDescription());
        template.setPreviewColor(body.getPreviewColor());
        template.setBadgeLabel(body.getBadgeLabel());
        template.setBadgeBgColor(body.getBadgeBgColor());
        template.setBadgeTextColor(body.getBadgeTextColor());
        template.setCategory(body.getCategory());
        template.setStyleTag(body.getStyleTag());
        template.setSortOrder(body.getSortOrder());
        template.setActive(body.getActive());
        template.setTemplateContent(body.getTemplateContent());

        return ResponseEntity.ok(cvTemplateRepository.save(template));
    }

    /** Admin: toggle active/inactive */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CvTemplate> toggleActive(@PathVariable Long id) {
        Optional<CvTemplate> opt = cvTemplateRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        CvTemplate template = opt.get();
        template.setActive(!template.getActive());
        return ResponseEntity.ok(cvTemplateRepository.save(template));
    }

    /** Admin: delete template */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable Long id) {
        if (!cvTemplateRepository.existsById(id)) return ResponseEntity.notFound().build();
        cvTemplateRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa mẫu CV thành công"));
    }
}
