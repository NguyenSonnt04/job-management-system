# QLTD Job Management System - Agent Guidelines

Hướng dẫn cho AI Agent khi làm việc với dự án QLTD (Quản Lý Tuyển Dụng).

---

## 1. THÔNG TIN DỰ ÁN

**Tên dự án:** QLTD - Job Management System
**Mã dự án:** Nhom08.Project
**Tech Stack:**
- Backend: Spring Boot 4.1.0-M1, Java 17
- Frontend: HTML5, CSS3, Vanilla JavaScript
- Database: MySQL 8.0 (Docker port 8085)
- AI: Google Gemini API
- Build: Maven

**Ports:**
- Application: 8083
- MySQL: 8085
- phpMyAdmin: 8086

---

## 2. QUY ƯỚC CODING BẮT BUỘC

### 2.1. Java Backend

**Cấu trúc package:**
```
Nhom08/Project/
├── config/          # Configuration classes
├── controller/      # @RestController
├── dto/            # Data Transfer Objects
├── entity/         # @Entity JPA classes
├── repository/     # @Repository interfaces
└── service/        # @Service classes
```

**Luồng dữ liệu:**
```
Controller → Service → Repository
```
❌ KHÔNG BAO GIỜ bỏ qua Service layer
❌ KHÔNG BAO GIỜ gọi Repository từ Controller

**Naming Conventions:**
- Class: PascalCase → `JobController`, `UserRepository`
- Method: camelCase, verb-first → `getJobById()`, `createJob()`
- Variable: camelCase → `currentUser`, `jobList`
- Constant: SCREAMING_SNAKE_CASE → `ROLE_ADMIN`, `MAX_FILE_SIZE`

**Dependency Injection:**
```java
// ✅ CORRECT - Constructor injection
@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
}

// ❌ WRONG - Field injection
@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;
}
```

**Transaction:**
```java
@Transactional              // For write operations
@Transactional(readOnly = true)  // For read operations
```

### 2.2. Frontend JavaScript

**API Call Pattern:**
```javascript
fetch('/api/endpoint', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',  // BẮT BUỘC cho authenticated requests
    body: JSON.stringify(data)
})
```

**Naming:**
- File: kebab-case → `main.js`, `job-detail.js`
- Function: camelCase → `loadJobs()`, `handleSubmit()`
- Variable: camelCase → `currentUser`, `jobList`

### 2.3. API Endpoints

**Pattern:** `/api/{resource}/{action}`

```javascript
GET    /api/jobs/active           # Lấy danh sách việc làm
GET    /api/jobs/{id}             # Chi tiết việc làm
POST   /api/jobs/create           # Tạo việc làm mới
PUT    /api/jobs/{id}             # Cập nhật việc làm
DELETE /api/jobs/{id}             # Xóa việc làm
```

**Response Format:**
```json
{
  "success": true,
  "message": "Vietnamese message",
  "data": { ... }
}
```

---

## 3. ENTITY & DATABASE

### 3.1. Base Entity Pattern

Tất cả entities PHẢI có:
```java
@Entity
@Table(name = "table_name")
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 3.2. Dropdown Options

Kế thừa từ `BaseOption`:
```java
@Entity
@Table(name = "industries")
public class Industry extends BaseOption {
    // Kế thừa: id, value, label, sortOrder, active
}
```

### 3.3. Relationships

```java
// Many-to-One
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "employer_id")
private Employer employer;

// One-to-Many
@OneToMany(mappedBy = "job")
private List<JobApplication> applications;

// One-to-One
@OneToOne(mappedBy = "user")
private Employer employer;
```

---

## 4. AUTHENTICATION & AUTHORIZATION

### 4.1. Roles

- `ROLE_ADMIN` - Quản trị viên
- `ROLE_EMPLOYER` - Nhà tuyển dụng
- `ROLE_CANDIDATE` - Người tìm việc

### 4.2. Protected Endpoints

```java
@PreAuthorize("hasRole('EMPLOYER')")
@PostMapping("/api/jobs/create")
public ResponseEntity<?> createJob(...) { }

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/admin/users")
public ResponseEntity<?> getAllUsers() { }
```

### 4.3. Get Current User

```java
@GetMapping("/api/user/me")
public ResponseEntity<?> getCurrentUser(Principal principal) {
    String email = principal.getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
    return ResponseEntity.ok(user);
}
```

---

## 5. FRONTEND RULES

### 5.1. Vietnamese Language

**TẤT CẢ text hiển thị cho user PHẢI là tiếng Việt:**
```javascript
// ✅ CORRECT
showError('Email không được để trống');
showSuccess('Đăng tuyển thành công');

// ❌ WRONG
showError('Email is required');
showSuccess('Job posted successfully');
```

### 5.2. Page Load Order

```javascript
document.addEventListener('DOMContentLoaded', async function() {
    // 1. Load shared components
    await loadHTML('header', '/includes/header.html');
    await loadHTML('footer', '/includes/footer.html');

    // 2. Check auth (cho protected pages)
    await checkAuth();

    // 3. Load page data
    await loadPageData();

    // 4. Setup event listeners
    setupEventListeners();
});
```

### 5.3. Form Handling

```javascript
async function submitForm(formId, submitUrl) {
    const form = document.getElementById(formId);
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    // Client-side validation
    if (!validateForm(data)) return;

    // Show loading
    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        const response = await fetch(submitUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            showSuccess(result.message);
        } else {
            showError(result.message);
        }
    } finally {
        btn.disabled = false;
    }
}
```

### 5.4. CSS Variables

```css
:root {
    --primary-color: #0066cc;
    --secondary-color: #f8f9fa;
    --success-color: #28a745;
    --danger-color: #dc3545;
    --text-color: #333;
    --border-color: #dee2e6;
    --border-radius: 8px;
}
```

---

## 6. VALIDATION & ERROR HANDLING

### 6.1. DTO Validation

```java
public class JobCreateDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không quá 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Ngành nghề không được để trống")
    private Long industryId;
}
```

### 6.2. Controller Error Handling

```java
@PostMapping("/create")
public ResponseEntity<?> create(@Valid @RequestBody DTO dto,
                                BindingResult result) {
    if (result.hasErrors()) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(e ->
            errors.put(e.getField(), e.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "errors", errors));
    }
    // Process valid DTO
}
```

---

## 7. FILE UPLOAD

### 7.1. Configuration

```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

### 7.2. Upload Handler

```java
@PostMapping("/upload")
public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    // Validate file type
    if (!file.getContentType().equals("application/pdf")) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false,
                "message", "Chỉ chấp nhận file PDF"));
    }

    // Validate file size
    if (file.getSize() > 5 * 1024 * 1024) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false,
                "message", "File quá lớn (tối đa 5MB)"));
    }

    // Process file
}
```

---

## 8. AI INTEGRATION (GEMINI)

### 8.1. GeminiService

Located at: `src/main/java/Nhom08/Project/service/GeminiService.java`

**Usage:**
- CV scoring against job criteria
- Skill extraction from CVs
- Job matching recommendations

**Best Practices:**
- Handle API errors gracefully
- Cache results when appropriate
- Validate API responses
- Never expose API keys in frontend

---

## 9. REFERENCE FILES

**Documentation:**
- `SKILL.md` - Main development rules
- `references/ARCHITECTURE.md` - Architecture patterns
- `references/API-REFERENCE.md` - API endpoints
- `references/ENTITY-RELATIONSHIPS.md` - Database schema
- `references/FRONTEND-PATTERNS.md` - Frontend patterns

**Key Files:**
```
src/main/java/Nhom08/Project/
├── QltdApplication.java              # Entry point
├── config/SecurityConfig.java        # Security config
├── controller/
│   ├── AuthController.java
│   ├── JobController.java
│   └── ...
├── service/GeminiService.java
└── entity/
    ├── User.java
    ├── Job.java
    └── ...

src/main/resources/
├── application.properties            # Main config
├── static/
│   ├── index.html                    # Homepage
│   ├── css/style.css
│   ├── js/main.js
│   └── includes/
│       ├── header.html
│       └── footer.html
└── templates/
    └── 403.html
```

---

## 10. BUILD & RUN

```bash
# Development
mvn spring-boot:run
docker-compose up -d

# Build
mvn clean package

# Production
java -jar target/Project-0.0.1-SNAPSHOT.jar
```

---

## 11. COMMON TASKS

### Tạo mới Entity:

```java
// 1. Entity
@Entity
@Table(name = "table_name")
public class EntityName {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
    // fields...
}

// 2. Repository
@Repository
public interface EntityRepository extends JpaRepository<EntityName, Long> { }

// 3. Service
@Service
public class EntityService {
    private final EntityRepository repository;
    public EntityService(EntityRepository repository) {
        this.repository = repository;
    }
}

// 4. Controller
@RestController
@RequestMapping("/api/entities")
public class EntityController {
    private final EntityService service;
    public EntityController(EntityService service) {
        this.service = service;
    }
}
```

### Tạo mới API Endpoint:

```java
@GetMapping("/api/resource/{id}")
public ResponseEntity<?> getById(@PathVariable Long id) {
    try {
        Data data = service.findById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", data
        ));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", e.getMessage()
        ));
    }
}
```

### Tạo mới Frontend Page:

```html
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tên trang - QLTD</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <div id="header"></div>
    <main class="container">
        <!-- Content -->
    </main>
    <div id="footer"></div>
    <script>
        loadHTML('header', '/includes/header.html');
        loadHTML('footer', '/includes/footer.html');
        checkAuth();
    </script>
</body>
</html>
```

---

## 12. IMPORTANT NOTES

1. ✅ **Luôn dùng tiếng Việt** cho user-facing messages
2. ✅ **Luôn include `credentials: 'include'`** cho authenticated API calls
3. ✅ **Luôn validate data** ở cả client và server side
4. ✅ **Luôn xử lý errors gracefully** với user-friendly messages
5. ✅ **Luôn follow Controller→Service→Repository** pattern
6. ✅ **Luôn dùng constructor injection** thay vì field injection
7. ✅ **Luôn dùng @Transactional** cho database operations
8. ❌ **KHÔNG bypass Service layer**
9. ❌ **KHÔNG hardcode sensitive data**
10. ❌ **KHÔNG skip authentication checks** cho protected endpoints

---

## 13. ACCESS URLS

| Service | URL |
|---------|-----|
| Application | http://localhost:8083 |
| phpMyAdmin | http://localhost:8086 |
| MySQL | localhost:8085 |

---

**Document Version:** 1.0
**Last Updated:** 2025-03-16
**Project:** QLTD - Job Management System
