package Nhom08.Project.controller;

import Nhom08.Project.entity.CvTemplate;
import Nhom08.Project.entity.CvTemplateVersion;
import Nhom08.Project.repository.CvTemplateRepository;
import Nhom08.Project.repository.CvTemplateVersionRepository;
import Nhom08.Project.service.CvVersioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cv-templates")
public class CvTemplateController {

    private final CvTemplateRepository cvTemplateRepository;
    private final CvTemplateVersionRepository cvTemplateVersionRepository;
    private final CvVersioningService cvVersioningService;

    public CvTemplateController(
            CvTemplateRepository cvTemplateRepository,
            CvTemplateVersionRepository cvTemplateVersionRepository,
            CvVersioningService cvVersioningService) {
        this.cvTemplateRepository = cvTemplateRepository;
        this.cvTemplateVersionRepository = cvTemplateVersionRepository;
        this.cvVersioningService = cvVersioningService;
    }

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
        CvTemplate saved = cvVersioningService.createTemplate(template, "Admin created initial template version");
        return ResponseEntity.ok(saved);
    }

    /** Admin: update template */
    @PutMapping("/{id}")
    public ResponseEntity<CvTemplate> updateTemplate(
            @PathVariable Long id, @RequestBody CvTemplate body) {
        Optional<CvTemplate> opt = cvTemplateRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CvTemplate template = opt.get();
        String previousContent = template.getTemplateContent();
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

        return ResponseEntity.ok(
            cvVersioningService.updateTemplate(template, previousContent, "Admin updated template content")
        );
    }

    /** Admin: version history for one template */
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<CvTemplateVersion>> getTemplateVersions(@PathVariable Long id) {
        if (!cvTemplateRepository.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cvTemplateVersionRepository.findByTemplateIdOrderByVersionNoDesc(id));
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
    @Transactional
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable Long id) {
        if (!cvTemplateRepository.existsById(id)) return ResponseEntity.notFound().build();
        cvTemplateVersionRepository.deleteByTemplateId(id);
        cvTemplateRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa mẫu CV thành công"));
    }
}
