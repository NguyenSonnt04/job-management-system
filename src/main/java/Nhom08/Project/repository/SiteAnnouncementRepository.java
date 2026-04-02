package Nhom08.Project.repository;

import Nhom08.Project.entity.SiteAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SiteAnnouncementRepository extends JpaRepository<SiteAnnouncement, Long> {

    List<SiteAnnouncement> findAllByOrderByCreatedAtDesc();

    @Query("SELECT a FROM SiteAnnouncement a WHERE a.active = true AND (a.startAt IS NULL OR a.startAt <= :now) AND (a.endAt IS NULL OR a.endAt >= :now) ORDER BY a.createdAt DESC")
    List<SiteAnnouncement> findActive(@Param("now") LocalDateTime now);
}
