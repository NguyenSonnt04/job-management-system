package Nhom08.Project.repository;

import Nhom08.Project.entity.FilterOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilterOptionRepository extends JpaRepository<FilterOption, Long> {
    List<FilterOption> findByGroupCodeAndActiveTrueOrderBySortOrderAsc(String groupCode);
}
