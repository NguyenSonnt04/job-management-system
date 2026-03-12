package Nhom08.Project.repository;

import Nhom08.Project.entity.CvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvTemplateRepository extends JpaRepository<CvTemplate, Long> {
    List<CvTemplate> findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
    List<CvTemplate> findAllByOrderBySortOrderAscCreatedAtDesc();
    List<CvTemplate> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);
    Optional<CvTemplate> findByName(String name);
}
