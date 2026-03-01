package Nhom08.Project.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thông tin tuyển dụng
    @NotBlank(message = "Chức danh không được để trống")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "job_code", length = 50)
    private String jobCode;

    @Column(name = "industry", length = 100)
    private String industry;

    @NotBlank(message = "Nơi làm việc không được để trống")
    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Column(name = "hide_location")
    private Boolean hideLocation = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // Lương
    @Column(name = "salary_min", precision = 10, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 10, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @Column(name = "show_salary")
    private Boolean showSalary = true;

    // Video giới thiệu
    @Column(name = "video_url_1", length = 500)
    private String videoUrl1;

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2;

    // Hạn nộp hồ sơ
    @NotNull(message = "Hạn nộp hồ sơ không được để trống")
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    // Yêu cầu thu thập thiểu
    @Column(name = "urgent_recruitment")
    private Boolean urgentRecruitment = false;

    // Ngôn ngữ trình bày hồ sơ
    @Column(name = "resume_language", length = 20)
    private String resumeLanguage = "Tiếng Việt";

    // Hình thức làm việc
    @Column(name = "employment_type", length = 50)
    private String employmentType; // Full-time, Part-time, Contract

    @Column(name = "work_from_home")
    private Boolean workFromHome = false;

    @Column(name = "work_at_office")
    private Boolean workAtOffice = true;

    // Phúc lợi (stored as JSON or comma-separated)
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits; // JSON: ["Chế độ bảo hiểm", "Đào tạo", ...]

    // Yêu cầu chung
    @Column(name = "gender", length = 20)
    private String gender; // Nam/Nữ/Nam/Nữ

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Column(name = "experience", length = 100)
    private String experience; // "Chưa có kinh nghiệm", "1-2 năm"

    @Column(name = "education_level", length = 100)
    private String educationLevel; // "Không yêu cầu", "Đại học"

    // Thông tin khác
    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    // Status
    @Column(name = "status", length = 20)
    private String status = "DRAFT"; // DRAFT, ACTIVE, CLOSED

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ với Employer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    @JsonIgnoreProperties({"user", "createdAt", "updatedAt", "taxCode", "description"})
    private Employer employer;

    // Constructors
    public Job() {}

    public Job(String title, String location, LocalDate deadline, Employer employer) {
        this.title = title;
        this.location = location;
        this.deadline = deadline;
        this.employer = employer;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getHideLocation() {
        return hideLocation;
    }

    public void setHideLocation(Boolean hideLocation) {
        this.hideLocation = hideLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public BigDecimal getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(BigDecimal salaryMin) {
        this.salaryMin = salaryMin;
    }

    public BigDecimal getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(BigDecimal salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getShowSalary() {
        return showSalary;
    }

    public void setShowSalary(Boolean showSalary) {
        this.showSalary = showSalary;
    }

    public String getVideoUrl1() {
        return videoUrl1;
    }

    public void setVideoUrl1(String videoUrl1) {
        this.videoUrl1 = videoUrl1;
    }

    public String getVideoUrl2() {
        return videoUrl2;
    }

    public void setVideoUrl2(String videoUrl2) {
        this.videoUrl2 = videoUrl2;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Boolean getUrgentRecruitment() {
        return urgentRecruitment;
    }

    public void setUrgentRecruitment(Boolean urgentRecruitment) {
        this.urgentRecruitment = urgentRecruitment;
    }

    public String getResumeLanguage() {
        return resumeLanguage;
    }

    public void setResumeLanguage(String resumeLanguage) {
        this.resumeLanguage = resumeLanguage;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Boolean getWorkFromHome() {
        return workFromHome;
    }

    public void setWorkFromHome(Boolean workFromHome) {
        this.workFromHome = workFromHome;
    }

    public Boolean getWorkAtOffice() {
        return workAtOffice;
    }

    public void setWorkAtOffice(Boolean workAtOffice) {
        this.workAtOffice = workAtOffice;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Employer getEmployer() {
        return employer;
    }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }
}
