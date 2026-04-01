package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewLevelRepository extends JpaRepository<InterviewLevel, Long> {

    List<InterviewLevel> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<InterviewLevel> findByLevelKey(String levelKey);
}
