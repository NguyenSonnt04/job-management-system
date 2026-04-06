package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "cv_type")
    private String cvType;

    @Column(name = "privacy")
    private String privacy;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "ai_compat_score")
    private Integer aiCompatScore;

    @Column(name = "ai_ranking", length = 50)
    private String aiRanking;

    @Column(name = "ai_experience", length = 200)
    private String aiExperience;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "cv_file_url")
    private String cvFileUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCvType() { return cvType; }
    public void setCvType(String cvType) { this.cvType = cvType; }
    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAiCompatScore() { return aiCompatScore; }
    public void setAiCompatScore(Integer aiCompatScore) { this.aiCompatScore = aiCompatScore; }
    public String getAiRanking() { return aiRanking; }
    public void setAiRanking(String aiRanking) { this.aiRanking = aiRanking; }
    public String getAiExperience() { return aiExperience; }
    public void setAiExperience(String aiExperience) { this.aiExperience = aiExperience; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public String getCvFileUrl() { return cvFileUrl; }
    public void setCvFileUrl(String cvFileUrl) { this.cvFileUrl = cvFileUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
