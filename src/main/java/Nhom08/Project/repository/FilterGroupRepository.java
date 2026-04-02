package Nhom08.Project.repository;

import Nhom08.Project.entity.FilterGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilterGroupRepository extends JpaRepository<FilterGroup, Long> {
    List<FilterGroup> findByActiveTrueOrderBySortOrderAsc();
    Optional<FilterGroup> findByCode(String code);
    boolean existsByCode(String code);
}
