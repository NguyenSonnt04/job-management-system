package Nhom08.Project.controller;

import Nhom08.Project.entity.SiteAnnouncement;
import Nhom08.Project.repository.SiteAnnouncementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final SiteAnnouncementRepository repo;

    public AnnouncementController(SiteAnnouncementRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/active")
    public ResponseEntity<List<SiteAnnouncement>> getActive() {
        return ResponseEntity.ok(repo.findActive(LocalDateTime.now()));
    }
}
