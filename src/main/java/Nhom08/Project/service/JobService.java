package Nhom08.Project.service;

import Nhom08.Project.dto.JobCreateDTO;
import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.Job;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private Nhom08.Project.repository.JobStatisticsRepository jobStatisticsRepository;

    /**
     * Create a new job posting
     */
    @Transactional
    public Job createJob(JobCreateDTO dto, Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhà tuyển dụng"));

        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setJobCode(dto.getJobCode());
        job.setIndustry(dto.getIndustry());
        job.setLocation(dto.getLocation());
        job.setHideLocation(dto.getHideLocation());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setCurrency(dto.getCurrency());
        job.setShowSalary(dto.getShowSalary());
        job.setVideoUrl1(dto.getVideoUrl1());
        job.setVideoUrl2(dto.getVideoUrl2());
        job.setDeadline(dto.getDeadline());
        job.setUrgentRecruitment(dto.getUrgentRecruitment());
        job.setResumeLanguage(dto.getResumeLanguage());
        job.setEmploymentType(dto.getEmploymentType());
        job.setWorkFromHome(dto.getWorkFromHome());
        job.setWorkAtOffice(dto.getWorkAtOffice());
        job.setBenefits(dto.getBenefits());
        job.setGender(dto.getGender());
        job.setAgeMin(dto.getAgeMin());
        job.setAgeMax(dto.getAgeMax());
        job.setExperience(dto.getExperience());
        job.setEducationLevel(dto.getEducationLevel());
        job.setAdditionalInfo(dto.getAdditionalInfo());
        job.setEmployer(employer);
        job.setStatus("PENDING");

        Job savedJob = jobRepository.save(job);
        jobStatisticsRepository.save(new Nhom08.Project.entity.JobStatistics(savedJob));

        return savedJob;
    }

    /**
     * Get all jobs by employer
     */
    public List<Job> getJobsByEmployer(Long employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

    /**
     * Get all active jobs
     */
    public List<Job> getAllActiveJobs() {
        return jobRepository.findByStatus("ACTIVE");
    }

    /**
     * Get job by ID
     */
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    /**
     * Update job
     */
    @Transactional
    public Job updateJob(Long jobId, JobCreateDTO dto, Long employerId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));

        // Check ownership
        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng này");
        }

        // Update fields
        job.setTitle(dto.getTitle());
        job.setJobCode(dto.getJobCode());
        job.setIndustry(dto.getIndustry());
        job.setLocation(dto.getLocation());
        job.setHideLocation(dto.getHideLocation());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setCurrency(dto.getCurrency());
        job.setShowSalary(dto.getShowSalary());
        job.setVideoUrl1(dto.getVideoUrl1());
        job.setVideoUrl2(dto.getVideoUrl2());
        job.setDeadline(dto.getDeadline());
        job.setUrgentRecruitment(dto.getUrgentRecruitment());
        job.setResumeLanguage(dto.getResumeLanguage());
        job.setEmploymentType(dto.getEmploymentType());
        job.setWorkFromHome(dto.getWorkFromHome());
        job.setWorkAtOffice(dto.getWorkAtOffice());
        job.setBenefits(dto.getBenefits());
        job.setGender(dto.getGender());
        job.setAgeMin(dto.getAgeMin());
        job.setAgeMax(dto.getAgeMax());
        job.setExperience(dto.getExperience());
        job.setEducationLevel(dto.getEducationLevel());
        job.setAdditionalInfo(dto.getAdditionalInfo());

        return jobRepository.save(job);
    }

    /**
     * Soft delete job — mark as DELETED instead of removing from DB
     */
    @Transactional
    public void deleteJob(Long jobId, Long employerId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));

        // Check ownership
        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Bạn không có quyền xóa tin tuyển dụng này");
        }

        // Soft delete: just mark as DELETED, keep record in DB
        job.setStatus("DELETED");
        jobRepository.save(job);
    }

    /**
     * Search jobs
     */
    public List<Job> searchJobs(String keyword) {
        // Simple search by title for now
        return jobRepository.findByTitleContainingIgnoreCaseAndStatusNot(keyword, "DELETED");
    }
}
