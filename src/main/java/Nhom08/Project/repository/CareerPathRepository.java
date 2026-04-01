package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerPathRepository extends JpaRepository<CareerPath, Long> {

    Optional<CareerPath> findBySlug(String slug);

    List<CareerPath> findByActiveTrueOrderByDisplayOrderAscTitleAsc();

    List<CareerPath> findByActiveTrueAndFeaturedTrueOrderByDisplayOrderAscTitleAsc();

    List<CareerPath> findByIndustryField(String industryField);

    boolean existsBySlug(String slug);
}
