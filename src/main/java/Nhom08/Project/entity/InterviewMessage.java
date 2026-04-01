package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu từng tin nhắn trong phiên phỏng vấn AI chat.
 * Table: interview_messages
 *
 * Tách hoàn toàn khỏi bảng reference.
 */
@Entity
@Table(name = "interview_messages",
       indexes = {
           @Index(name = "idx_im_session_id", columnList = "session_id"),
           @Index(name = "idx_im_role",       columnList = "role"),
       })
public class InterviewMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    /**
     * "user" | "assistant"
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Thứ tự tin nhắn trong phiên (bắt đầu từ 1)
     */
    @Column(name = "message_order", nullable = false)
    private Integer messageOrder;

    /**
     * Thời gian người dùng mất để gõ (ms) — chỉ áp dụng role="user"
     */
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewMessage() {}

    public InterviewMessage(InterviewSession session, String role, String content, int messageOrder) {
        this.session      = session;
        this.role         = role;
        this.content      = content;
        this.messageOrder = messageOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InterviewSession getSession() { return session; }
    public void setSession(InterviewSession session) { this.session = session; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getMessageOrder() { return messageOrder; }
    public void setMessageOrder(Integer messageOrder) { this.messageOrder = messageOrder; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
