package Nhom08.Project.repository;

import Nhom08.Project.entity.TopEmployerLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TopEmployerLogoRepository extends JpaRepository<TopEmployerLogo, Long> {

    List<TopEmployerLogo> findAllByOrderByDisplayOrderAscUpdatedAtDesc();

    @Query("""
        select l
        from TopEmployerLogo l
        where l.active = true
          and (l.startAt is null or l.startAt <= :now)
          and (l.endAt is null or l.endAt >= :now)
        order by l.displayOrder asc, l.updatedAt desc, l.id desc
        """)
    List<TopEmployerLogo> findActiveLogos(@Param("now") LocalDateTime now);
}
