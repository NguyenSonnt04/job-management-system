package Nhom08.Project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_statistics")
public class JobStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "application_count", nullable = false)
    private Long applicationCount = 0L;

    public JobStatistics() {
    }

    public JobStatistics(Job job) {
        this.job = job;
        this.viewCount = 0L;
        this.applicationCount = 0L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public Long getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(Long applicationCount) {
        this.applicationCount = applicationCount;
    }

    public void incrementApplicationCount() {
        this.applicationCount++;
    }
}
