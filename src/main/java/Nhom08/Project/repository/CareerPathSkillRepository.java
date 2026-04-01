package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerPathSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerPathSkillRepository extends JpaRepository<CareerPathSkill, Long> {

    List<CareerPathSkill> findByStageIdOrderBySkillOrderAsc(Long stageId);

    List<CareerPathSkill> findByStageIdAndIsRequiredTrueOrderBySkillOrderAsc(Long stageId);
}
