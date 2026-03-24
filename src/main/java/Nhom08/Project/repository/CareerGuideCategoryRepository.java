package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerGuideCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerGuideCategoryRepository extends JpaRepository<CareerGuideCategory, Long> {

    List<CareerGuideCategory> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<CareerGuideCategory> findBySlug(String slug);
}
