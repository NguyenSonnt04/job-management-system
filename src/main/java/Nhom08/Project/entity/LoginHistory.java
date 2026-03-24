package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ lịch sử đăng nhập
 */
@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String username;

    private String email;

    @Column(name = "login_time")
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    private String status = "success";

    private String note;

    // --- Constructors ---
    public LoginHistory() {}

    public LoginHistory(String username, String email, String ipAddress, String userAgent, String status) {
        this.username = username;
        this.email = email;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
    }

    // --- Getters & Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
