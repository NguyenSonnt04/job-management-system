package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerGuideTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerGuideTagRepository extends JpaRepository<CareerGuideTag, Long> {

    List<CareerGuideTag> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<CareerGuideTag> findBySlug(String slug);
}
