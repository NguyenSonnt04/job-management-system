package Nhom08.Project.repository;

import Nhom08.Project.entity.CvScoringCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CvScoringCriteriaRepository extends JpaRepository<CvScoringCriteria, Long> {
    List<CvScoringCriteria> findByActiveTrueOrderByDisplayOrderAsc();
}
