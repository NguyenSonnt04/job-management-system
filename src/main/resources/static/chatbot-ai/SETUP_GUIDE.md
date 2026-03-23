# Chatbot AI - Hướng dẫn cài đặt Database Backend

## 📦 Tổng quan

Chatbot AI hỗ trợ 2 chế độ hoạt động:
1. **Client-side only** - Sử dụng knowledge base cứng trong JS
2. **Database Backend** - Kết nối với API backend để lấy prompts từ database

## 🗄️ Cài đặt Database

### Bước 1: Chạy SQL script

Chạy file `schema.sql` trong database của bạn:

```bash
psql -U your_user -d your_database -f schema.sql
```

Hoặc copy nội dung `schema.sql` và chạy trong SQL client.

### Bước 2: Copy Java files

Copy các file Java vào đúng vị trí trong project:

```
src/main/java/com/chatbot/
├── entity/
│   ├── ChatbotCategory.java
│   ├── ChatbotPrompt.java
│   ├── ChatbotQuickQuestion.java
│   └── ChatbotConversation.java
├── repository/
│   ├── ChatbotCategoryRepository.java
│   ├── ChatbotPromptRepository.java
│   ├── ChatbotQuickQuestionRepository.java
│   └── ChatbotConversationRepository.java
├── dto/
│   ├── ChatbotRequest.java
│   └── ChatbotResponse.java
├── service/
│   └── ChatbotService.java
└── controller/
    └── ChatbotController.java
```

### Bước 3: Add dependencies (pom.xml)

Đảm bảo có các dependencies sau:

```xml
<!-- JPA & Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL (hoặc MySQL) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Bước 4: Cấu hình application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## 🚀 Cách sử dụng

### Client-side (không cần backend)

Thêm vào HTML:

```html
<link rel="stylesheet" href="chatbot-ai/chatbot.css">
<script src="chatbot-ai/chatbot-knowledge.js"></script>
<script src="chatbot-ai/chatbot.js"></script>
```

### Database Backend

Thêm vào HTML:

```html
<link rel="stylesheet" href="chatbot-ai/chatbot.css">
<script src="chatbot-ai/chatbot-api.js"></script>
<script src="chatbot-ai/chatbot-db.js"></script>
```

Hoặc dùng init script (tự động chọn mode):

```html
<script src="chatbot-ai/chatbot-init.js"></script>
```

## 📝 Quản lý Prompts

### Thêm prompt mới

**Cách 1: SQL trực tiếp**
```sql
INSERT INTO chatbot_prompt (category_id, title, keywords, answer)
VALUES (
    (SELECT id FROM chatbot_category WHERE code = 'FEATURES'),
    'Tính năng mới',
    ARRAY['tính năng mới', 'new feature', 'mới'],
    'Đây là tính năng mới của hệ thống...'
);
```

**Cách 2: Thêm vào database qua UI** (nếu có admin panel)

**Cách 3: Sửa file knowledge.js** (cho client-side mode)

### Sửa prompt

```sql
UPDATE chatbot_prompt
SET answer = 'Nội dung mới...'
WHERE id = 123;
```

### Xóa prompt

```sql
UPDATE chatbot_prompt
SET is_active = false
WHERE id = 123;
```

## 🔌 API Endpoints

```
POST   /api/chatbot/ask              # Đặt câu hỏi
POST   /api/chatbot/feedback         # Gửi feedback
GET    /api/chatbot/quick-questions  # Lấy câu hỏi nhanh
GET    /api/chatbot/categories       # Lấy danh mục
GET    /api/chatbot/prompts          # Lấy prompts theo category
GET    /api/chatbot/statistics       # Thống kê (Admin)
GET    /api/chatbot/health           # Health check
```

## 📊 Cấu trúc Database

```
chatbot_category          # Danh mục prompts
├── id
├── code (UNIQUE)
├── name
├── description
├── icon
└── display_order

chatbot_prompt            # Prompts và câu trả lời
├── id
├── category_id (FK)
├── title
├── keywords (ARRAY)
├── question
├── answer (TEXT)
├── prompt_type
├── priority
├── tags (ARRAY)
└── view_count

chatbot_quick_question    # Câu hỏi nhanh
├── id
├── question
├── icon
└── display_order

chatbot_conversation      # Lịch sử chat
├── id
├── session_id
├── user_question
├── bot_answer
├── prompt_id (FK)
├── confidence_score
└── is_helpful
```

## 🔧 Troubleshooting

**API không hoạt động?**
- Kiểm tra backend có chạy không
- Test endpoint `/api/chatbot/health`
- Kiểm tra console browser

**Prompt không tìm thấy?**
- Kiểm tra keywords trong database
- Test function `chatbot_find_answer()`

**Feedback không lưu?**
- Kiểm tra `conversation_id` có hợp lệ không
- Xem log backend

## 📈 Thống kê

Xem thống kê sử dụng:

```sql
SELECT * FROM v_chatbot_stats;
```

Hoặc gọi API:
```
GET /api/chatbot/statistics
```

---

Created for Job Management System 🚀
