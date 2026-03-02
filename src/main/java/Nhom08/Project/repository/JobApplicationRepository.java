package Nhom08.Project.repository;

import Nhom08.Project.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByUserId(Long userId);
    List<JobApplication> findByJobIdIn(List<Long> jobIds);
    boolean existsByJobIdAndEmail(Long jobId, String email);
}
