package Nhom08.Project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Key định danh: INTERVIEW_TITLE, INTERVIEW_MESSAGE, ...
    @Column(name = "`key`", unique = true, nullable = false, length = 100)
    private String key;

    // Noi dung co the dung placeholder: {jobTitle}, {companyName}, ...
    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(length = 255)
    private String description;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
