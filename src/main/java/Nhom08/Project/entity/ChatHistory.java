package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ lịch sử chat với Chatbot
 */
@Entity
@Table(name = "chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @Column(name = "matched_keyword")
    private String matchedKeyword;

    @Column(name = "prompt_id")
    private Integer promptId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Constructors ---
    public ChatHistory() {}

    public ChatHistory(String sessionId, String userMessage, String botResponse, String matchedKeyword, Integer promptId) {
        this.sessionId = sessionId;
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.matchedKeyword = matchedKeyword;
        this.promptId = promptId;
    }

    // --- Getters & Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public String getBotResponse() { return botResponse; }
    public void setBotResponse(String botResponse) { this.botResponse = botResponse; }

    public String getMatchedKeyword() { return matchedKeyword; }
    public void setMatchedKeyword(String matchedKeyword) { this.matchedKeyword = matchedKeyword; }

    public Integer getPromptId() { return promptId; }
    public void setPromptId(Integer promptId) { this.promptId = promptId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}