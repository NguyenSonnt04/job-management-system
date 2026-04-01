package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerPathStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerPathStageRepository extends JpaRepository<CareerPathStage, Long> {

    List<CareerPathStage> findByCareerPathIdOrderByStageOrderAsc(Long careerPathId);

    List<CareerPathStage> findByCareerPathIdAndStageOrder(Long careerPathId, Integer stageOrder);
}
