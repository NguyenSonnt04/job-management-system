package Nhom08.Project.repository;

import Nhom08.Project.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // Find jobs by employer ID
    List<Job> findByEmployerId(Long employerId);
    
    // Find active jobs
    List<Job> findByStatus(String status);
    
    // Find jobs by employer ID and status
    List<Job> findByEmployerIdAndStatus(Long employerId, String status);
    
    // Find jobs by location
    List<Job> findByLocationContainingIgnoreCase(String location);
    
    // Find jobs by title (excluding soft-deleted)
    List<Job> findByTitleContainingIgnoreCaseAndStatusNot(String title, String excludedStatus);

    // Find active jobs by industry (for CV matching)
    List<Job> findByStatusAndIndustryContainingIgnoreCase(String status, String industry);

    // Find top active jobs (when industry not detected)
    List<Job> findTop20ByStatus(String status);
}
