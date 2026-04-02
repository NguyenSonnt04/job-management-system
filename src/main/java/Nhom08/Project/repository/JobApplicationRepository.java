package Nhom08.Project.repository;

import Nhom08.Project.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByUserId(Long userId);
    List<JobApplication> findByJobIdIn(List<Long> jobIds);
    boolean existsByJobIdAndEmail(Long jobId, String email);

    @Query("SELECT CAST(a.createdAt AS date), COUNT(a) FROM JobApplication a WHERE a.createdAt >= :from GROUP BY CAST(a.createdAt AS date) ORDER BY CAST(a.createdAt AS date)")
    List<Object[]> countByDay(@Param("from") java.time.LocalDateTime from);
}
