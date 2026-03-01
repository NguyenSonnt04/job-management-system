package Nhom08.Project.repository;

import Nhom08.Project.entity.JobStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobStatisticsRepository extends JpaRepository<JobStatistics, Long> {
    Optional<JobStatistics> findByJobId(Long jobId);
    List<JobStatistics> findByJobIdIn(List<Long> jobIds);
}
