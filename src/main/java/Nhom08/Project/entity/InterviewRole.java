package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reference data: danh sách vị trí/ngành nghề hỗ trợ phỏng vấn.
 * Table: interview_roles
 * Seed: interview_roles_seed.sql — KHÔNG gộp dữ liệu vào đây.
 */
@Entity
@Table(name = "interview_roles")
public class InterviewRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key", nullable = false, unique = true, length = 100)
    private String roleKey;           // e.g. "frontend_developer"

    @Column(name = "role_name", nullable = false, length = 150)
    private String roleName;          // e.g. "Frontend Developer"

    @Column(name = "category", nullable = false, length = 80)
    private String category;          // e.g. "Công nghệ thông tin"

    @Column(name = "category_key", nullable = false, length = 80)
    private String categoryKey;       // e.g. "tech"

    @Column(name = "icon_class", length = 80)
    private String iconClass;         // Font Awesome class

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public InterviewRole() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryKey() { return categoryKey; }
    public void setCategoryKey(String categoryKey) { this.categoryKey = categoryKey; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
