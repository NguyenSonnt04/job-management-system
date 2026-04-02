package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu các template prompt cho AI phỏng vấn.
 * Cho phép admin chỉnh sửa prompt từ DB mà không cần build lại code.
 *
 * promptKey format:
 *   - "system_main"           — prompt hệ thống chính
 *   - "style_techlead"        — phong cách Tech Lead
 *   - "style_startup"         — phong cách Startup
 *   - "style_strict"          — phong cách FAANG
 *   - "style_standard"        — phong cách chuẩn
 *   - "level_intern"          — hướng dẫn riêng cho Intern
 *   - "level_fresher"         — hướng dẫn riêng cho Fresher (nếu cần)
 *   - "eval_system"           — prompt đánh giá cuối phiên
 *
 * promptContent hỗ trợ placeholder:
 *   {{role}}, {{level}}, {{typeDesc}}, {{styleDesc}}, {{levelGuide}}, {{cvSection}}
 */
@Entity
@Table(name = "interview_prompt_templates",
       indexes = @Index(name = "idx_ipt_key", columnList = "prompt_key"))
public class InterviewPromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prompt_key", nullable = false, unique = true, length = 80)
    private String promptKey;

    @Column(name = "prompt_name", nullable = false, length = 150)
    private String promptName;

    @Column(name = "prompt_content", nullable = false, columnDefinition = "TEXT")
    private String promptContent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public InterviewPromptTemplate() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPromptKey() { return promptKey; }
    public void setPromptKey(String promptKey) { this.promptKey = promptKey; }

    public String getPromptName() { return promptName; }
    public void setPromptName(String promptName) { this.promptName = promptName; }

    public String getPromptContent() { return promptContent; }
    public void setPromptContent(String promptContent) { this.promptContent = promptContent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
