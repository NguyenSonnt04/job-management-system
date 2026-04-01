package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trữ một phiên phỏng vấn của người dùng.
 * Table: interview_sessions
 *
 * Tách hoàn toàn khỏi bảng reference — chỉ lưu dữ liệu runtime.
 */
@Entity
@Table(name = "interview_sessions",
       indexes = {
           @Index(name = "idx_is_user_id",    columnList = "user_id"),
           @Index(name = "idx_is_created_at", columnList = "created_at"),
           @Index(name = "idx_is_mode",       columnList = "mode"),
       })
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NULL được phép — khách chưa đăng nhập vẫn có thể luyện.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /** "ai" | "static" */
    @Column(name = "mode", nullable = false, length = 20)
    private String mode;

    /** Tên vị trí ứng tuyển (lưu denormalized để report dễ) */
    @Column(name = "role_name", nullable = false, length = 150)
    private String roleName;

    /** role_key tham chiếu interview_roles.role_key */
    @Column(name = "role_key", nullable = false, length = 100)
    private String roleKey;

    /** level_key tham chiếu interview_levels.level_key */
    @Column(name = "level_key", nullable = false, length = 50)
    private String levelKey;

    /** type_key tham chiếu interview_types.type_key */
    @Column(name = "type_key", nullable = false, length = 50)
    private String typeKey;

    /** Tên file CV đã dùng (nếu có) */
    @Column(name = "cv_file_name", length = 255)
    private String cvFileName;

    /** Có đính kèm CV không */
    @Column(name = "has_cv", nullable = false)
    private Boolean hasCv = false;

    /** Trạng thái: "in_progress" | "completed" | "abandoned" */
    @Column(name = "status", nullable = false, length = 30)
    private String status = "in_progress";

    /** Thời gian thực tế của phiên (giây) */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewMessage> messages = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewResult result;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewSession() {}

    public InterviewSession(User user, String mode, String roleName, String roleKey,
                             String levelKey, String typeKey) {
        this.user     = user;
        this.mode     = mode;
        this.roleName = roleName;
        this.roleKey  = roleKey;
        this.levelKey = levelKey;
        this.typeKey  = typeKey;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }

    public String getLevelKey() { return levelKey; }
    public void setLevelKey(String levelKey) { this.levelKey = levelKey; }

    public String getTypeKey() { return typeKey; }
    public void setTypeKey(String typeKey) { this.typeKey = typeKey; }

    public String getCvFileName() { return cvFileName; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }

    public Boolean getHasCv() { return hasCv; }
    public void setHasCv(Boolean hasCv) { this.hasCv = hasCv; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<InterviewMessage> getMessages() { return messages; }
    public void setMessages(List<InterviewMessage> messages) { this.messages = messages; }

    public InterviewResult getResult() { return result; }
    public void setResult(InterviewResult result) { this.result = result; }
}
