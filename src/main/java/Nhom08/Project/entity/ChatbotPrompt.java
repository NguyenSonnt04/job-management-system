package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ Prompt cho Chatbot
 * Hỗ trợ keyword matching để trả lời tự động
 */
@Entity
@Table(name = "chatbot_prompts")
public class ChatbotPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String promptName;

    @Column(nullable = false, length = 500)
    private String keywords;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    private String category = "general";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Constructors ---
    public ChatbotPrompt() {}

    public ChatbotPrompt(String promptName, String keywords, String response, String category) {
        this.promptName = promptName;
        this.keywords = keywords;
        this.response = response;
        this.category = category;
    }

    // --- Getters & Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPromptName() { return promptName; }
    public void setPromptName(String promptName) { this.promptName = promptName; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}