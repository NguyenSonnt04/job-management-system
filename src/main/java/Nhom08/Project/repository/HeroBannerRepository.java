package Nhom08.Project.repository;

import Nhom08.Project.entity.HeroBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HeroBannerRepository extends JpaRepository<HeroBanner, Long> {

    List<HeroBanner> findAllByOrderByDisplayOrderAscUpdatedAtDesc();

    @Query("""
        select b
        from HeroBanner b
        where b.active = true
          and (b.startAt is null or b.startAt <= :now)
          and (b.endAt is null or b.endAt >= :now)
        order by b.displayOrder asc, b.updatedAt desc, b.id desc
        """)
    List<HeroBanner> findActiveBanners(@Param("now") LocalDateTime now);
}
