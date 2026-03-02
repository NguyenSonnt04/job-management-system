package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_job_matches")
public class CvJobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK den cv_score_sessions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CvScoreSession session;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_title", length = 200)
    private String jobTitle;

    @Column(name = "job_industry", length = 100)
    private String jobIndustry;

    @Column(name = "job_location", length = 200)
    private String jobLocation;

    @Column(name = "job_experience", length = 100)
    private String jobExperience;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }

    public CvScoreSession getSession() { return session; }
    public void setSession(CvScoreSession session) { this.session = session; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobIndustry() { return jobIndustry; }
    public void setJobIndustry(String jobIndustry) { this.jobIndustry = jobIndustry; }

    public String getJobLocation() { return jobLocation; }
    public void setJobLocation(String jobLocation) { this.jobLocation = jobLocation; }

    public String getJobExperience() { return jobExperience; }
    public void setJobExperience(String jobExperience) { this.jobExperience = jobExperience; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
