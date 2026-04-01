package Nhom08.Project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_cv_versions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_cv_versions_cv_version",
        columnNames = {"user_cv_id", "version_no"}
    )
)
public class UserCvVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_cv_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private UserCv userCv;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "cv_name", length = 200)
    private String cvName;

    @Column(name = "cv_content", columnDefinition = "TEXT", nullable = false)
    private String cvContent;

    @Column(name = "saved_by", length = 30, nullable = false)
    private String savedBy = "USER";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserCv getUserCv() { return userCv; }
    public void setUserCv(UserCv userCv) { this.userCv = userCv; }

    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }

    public String getCvName() { return cvName; }
    public void setCvName(String cvName) { this.cvName = cvName; }

    public String getCvContent() { return cvContent; }
    public void setCvContent(String cvContent) { this.cvContent = cvContent; }

    public String getSavedBy() { return savedBy; }
    public void setSavedBy(String savedBy) { this.savedBy = savedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
