package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRoleRepository extends JpaRepository<InterviewRole, Long> {

    List<InterviewRole> findByActiveTrueOrderByDisplayOrderAsc();

    List<InterviewRole> findByCategoryKeyAndActiveTrueOrderByDisplayOrderAsc(String categoryKey);

    Optional<InterviewRole> findByRoleKey(String roleKey);
}
