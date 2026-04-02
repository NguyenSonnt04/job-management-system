package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reference data: ngân hàng câu hỏi phỏng vấn tĩnh (dùng cho chế độ tự luyện).
 * Table: interview_question_bank
 * Seed: interview_question_bank_seed.sql
 */
@Entity
@Table(name = "interview_question_bank",
       indexes = {
           @Index(name = "idx_iqb_type",     columnList = "type_key"),
           @Index(name = "idx_iqb_category",  columnList = "category"),
           @Index(name = "idx_iqb_role_key",  columnList = "role_key"),
       })
public class InterviewQuestionBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Loại câu hỏi: "hr" | "technical" | "behavioral"
     */
    @Column(name = "type_key", nullable = false, length = 50)
    private String typeKey;

    /**
     * Danh mục để nhóm câu hỏi: "Giới thiệu bản thân", "Kỹ thuật Java", ...
     */
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    /**
     * role_key áp dụng — NULL = áp dụng cho tất cả vị trí.
     */
    @Column(name = "role_key", length = 100)
    private String roleKey;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;              // Gợi ý trả lời

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;       // Câu trả lời mẫu

    @Column(name = "difficulty", length = 20)
    private String difficulty;        // "easy" | "medium" | "hard"

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewQuestionBank() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeKey() { return typeKey; }
    public void setTypeKey(String typeKey) { this.typeKey = typeKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getModelAnswer() { return modelAnswer; }
    public void setModelAnswer(String modelAnswer) { this.modelAnswer = modelAnswer; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
