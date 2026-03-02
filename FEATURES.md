# 📋 FEATURES.md — Theo Dõi Tiến Độ Tính Năng

> **Dự án:** CareerViet — Hệ Thống Quản Lý Tuyển Dụng (Nhóm 08)  
> **Cập nhật lần cuối:** 2026-03-02  
> **Cách dùng:** Khi hoàn thành một tính năng, cập nhật trạng thái từ `🔲 Chưa làm` → `✅ Hoàn thành` và ghi ngày hoàn thành.

---

## Bảng Trạng Thái Tổng Quan

| Ký hiệu | Ý nghĩa |
|---------|---------|
| ✅ | Đã hoàn thành — có backend + frontend |
| 🚧 | Đang làm — UI có nhưng backend chưa xong / ngược lại |
| ⬜ | Chưa làm |
| ❌ | Dừng lại / Không làm |

---

## 1. 🔐 Xác Thực & Phân Quyền (Authentication)

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 1.1 | Đăng ký ứng viên (Candidate) | ✅ | 2026-02 | `POST /api/auth/register/user` — validation đủ, lưu DB |
| 1.2 | Đăng ký NTD Bước 1 (email + mật khẩu) | ✅ | 2026-02 | `POST /api/auth/register/employer/step1` — lưu session |
| 1.3 | Đăng ký NTD Bước 2 (thông tin công ty) | ✅ | 2026-02 | `POST /api/auth/register/employer/step2` — tạo Employer entity |
| 1.4 | Đăng nhập (tất cả roles) | ✅ | 2026-02 | `POST /api/auth/login` — Spring Security session |
| 1.5 | Kiểm tra email tồn tại | ✅ | 2026-02 | `GET /api/auth/check-email` — realtime validate |
| 1.6 | Đăng xuất | ✅ | 2026-02 | `GET /logout` |
| 1.7 | Trang 403 Forbidden | ✅ | 2026-02 | `/templates/403.html` — Thymeleaf |
| 1.8 | Phân quyền 3 role (ADMIN/EMPLOYER/CANDIDATE) | ✅ | 2026-02 | Spring Security config hoàn chỉnh |
| 1.9 | Redirect sau login theo role | ✅ | 2026-02 | Admin→`/admin/dashboard`, Employer→`/dashboard`, User→home |

---

## 2. 👤 Quản Lý Người Dùng (User)

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 2.1 | Lấy thông tin user đang đăng nhập | ✅ | 2026-02 | `GET /api/user/me` — trả về role, displayName, employer info |
| 2.2 | Hiển thị header động (login/logout state) | ✅ | 2026-02 | `main.js` — gọi `/api/user/me` để cập nhật header |
| 2.3 | Cập nhật hồ sơ cá nhân | ⬜ | — | Chưa có endpoint PUT |
| 2.4 | Đổi mật khẩu | ⬜ | — | Chưa làm |
| 2.5 | Tạo tài khoản admin mặc định | ✅ | 2026-02 | `DataInitializer.java` — seed `admin@careerviet.vn` / `admin123` |

---

## 3. 🏢 Nhà Tuyển Dụng (Employer)

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 3.1 | Landing page NTD | ✅ | 2026-02 | `/nha-tuyen-dung.html` — UI hoàn chỉnh |
| 3.2 | Dashboard NTD | 🚧 | — | `/dashboard.html` — UI có, data chưa kết nối đầy đủ |
| 3.3 | Lấy thông tin công ty (auto-fill form) | ✅ | 2026-02 | `GET /api/jobs/employer-info` — điền sẵn khi đăng tin |
| 3.4 | Đăng tin tuyển dụng mới | ✅ | 2026-02 | `POST /api/jobs/create` + `/post-job.html` + `post-job.js` |
| 3.5 | Xem danh sách tin đã đăng | ✅ | 2026-02 | `GET /api/jobs/my-jobs` — trả về jobs của employer |
| 3.6 | Cập nhật tin tuyển dụng | ✅ | 2026-02 | `PUT /api/jobs/{id}` — kiểm tra quyền sở hữu |
| 3.7 | Xóa tin tuyển dụng | ✅ | 2026-02 | `DELETE /api/jobs/{id}` — kiểm tra quyền sở hữu |
| 3.8 | Quản lý tin tuyển dụng (UI + Excel export) | ✅ | 2026-03 | Quản lý view count, applicant count, export JS |
| 3.9 | Quản lý ứng viên (UI) | 🚧 | — | `/quan-ly-ung-vien.html` — UI có, backend mở rộng thêm sau |
| 3.10 | Quản lý Config Options (Industry/Exp/Location/Benefits) | ✅ | 2026-02 | Database-driven select options thay vì hardcode |

---

## 4. 💼 Tin Tuyển Dụng / Việc Làm (Jobs)

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 4.1 | Xem tất cả tin active (public) | ✅ | 2026-02 | `GET /api/jobs/active` |
| 4.2 | Xem chi tiết tin tuyển dụng | ✅ | 2026-02 | `job-detail.html` |
| 4.3 | Tìm kiếm việc làm (backend) | ✅ | 2026-02 | `GET /api/jobs/search?keyword=` |
| 4.4 | Tìm kiếm việc làm (UI + filter) | ✅ | 2026-02 | Lọc hoạt động hiệu quả |
| 4.5 | Lọc theo ngành, lương, địa điểm | ✅ | 2026-02 | Filters API fully functional |
| 4.6 | Đánh dấu tin tuyển dụng khẩn | ✅ | 2026-02 | Field `urgentRecruitment` trong Job entity |
| 4.7 | Job status: DRAFT / ACTIVE / CLOSED | ✅ | 2026-02 | Field `status` trong Job entity |
| 4.8 | Nộp hồ sơ ứng tuyển | 🚧 | — | Có frontend form, backend sắp hoàn thiện logic workflow |

---

## 5. 🤖 AI Features (Gemini Integration)

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 5.1 | Trang chấm điểm CV (UI) | ✅ | 2026-03 | `/cham-diem-cv.html` — UI hoàn chỉnh (Drag drop, status indicator) |
| 5.2 | API Chấm điểm CV (Backend Gemini) | ✅ | 2026-03 | Gọi text/file API chấm điểm CV qua Gemini AI |
| 5.3 | Quản lý Tiêu chí chấm điểm | ✅ | 2026-03 | DB table `cv_scoring_criteria` với 4 tiêu chí mặc định |
| 5.4 | AI Job Matching từ điểm CV | ✅ | 2026-03 | Load DB cache, detect industry, cross-match > 30% score |
| 5.5 | Lưu Lịch sử Chấm Điểm | ✅ | 2026-03 | DB table `cv_score_sessions` |
| 5.6 | Tạo CV bằng AI (UI) | 🚧 | — | `/tao-cv-ai.html` — có giao diện chọn template |
| 5.7 | CV Editor & Harvard Template | ✅ | 2026-02 | Design Harvard chuẩn, wysiwyg editor |
| 5.8 | Xuất CV dạng PDF | ⬜ | — | Chưa làm |

---

## 6. 🛡️ Admin

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 6.1 | Admin Dashboard (UI) | ✅ | 2026-02 | `/admin/dashboard.html` — Thymeleaf fragment |
| 6.2 | Thống kê số lượng user | ✅ | 2026-02 | `GET /api/admin/stats` |
| 6.3 | Quản lý tài khoản người dùng | ⬜ | — | Backend chưa có endpoint CRUD user |
| 6.4 | Duyệt / khóa nhà tuyển dụng | ⬜ | — | Chưa làm |
| 6.5 | Quản lý tin tuyển dụng (admin) | ⬜ | — | Chưa làm |

---

## 7. 🎨 Giao Diện & Frontend

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 7.1 | Trang chủ (`index.html`) | ✅ | 2026-02 | Banner, search bar, danh sách việc làm nổi bật |
| 7.2 | Header component (includes) | ✅ | 2026-02 | `/includes/` — reusable header/footer |
| 7.3 | Header cập nhật theo trạng thái login | ✅ | 2026-02 | `main.js` xử lý dynamic header quá trình đăng nhập |
| 7.4 | Trang đăng ký ứng viên | ✅ | 2026-02 | `/candidate-register.html` |
| 7.5 | Trang đăng ký NTD (2 step) | ✅ | 2026-02 | `/employer-register.html` + `/employer-register-step-2.html` |
| 7.6 | Trang đăng nhập NTD | ✅ | 2026-02 | `/employer-login.html` |
| 7.7 | Trang đăng tin tuyển dụng | ✅ | 2026-02 | Cấp nhật logic form, xử lý select DB driven |
| 7.8 | Responsive design | 🚧 | 2026-03 | Liên tục được cập nhật theo từng UI mới |

---

## 8. ⚙️ Hạ Tầng, Cấu Hình & Bảo Mật

| # | Tính năng | Trạng thái | Ngày hoàn thành | Ghi chú |
|---|-----------|-----------|-----------------|---------|
| 8.1 | Docker Compose (MySQL + PhpMyAdmin) | ✅ | 2026-02 | `docker-compose.yml` — port 8085/8086 |
| 8.2 | Spring Security config | ✅ | 2026-02 | Phân quyền API endpoints |
| 8.3 | Maven build (Spring Boot 3 + Java 17) | ✅ | 2026-02 | Project Structure |
| 8.4 | Seed data khi khởi chạy | ✅ | 2026-02 | Tạo data mặc định cho UI forms, Admin, Job Attributes |
| 8.5 | Bảo mật API Key | ✅ | 2026-03 | Tách key Gemini ra `application-local.properties` & gitignore |
| 8.6 | Quản lý Notification Templates linh hoạt | ✅ | 2026-03 | DB table `notification_templates` |

---

## 📊 Tổng Kết Tiến Độ

```
✅ Hoàn thành : 43 tính năng
🚧 Đang làm   :  6 tính năng
⬜ Chưa làm   :  9 tính năng
─────────────────────────────
   Tổng        : 58 tính năng
```

> **Tiến độ ước tính:** ~74% hoàn thành

---

## 📝 Nhật Ký Cập Nhật

| Ngày | Người cập nhật | Nội dung |
|------|---------------|---------|
| 2026-02-22 | AI (Antigravity) | Khởi tạo file, phân tích toàn bộ codebase và ghi nhận trạng thái hiện tại |
| 2026-03-02 | AI (Antigravity) | Cập nhật trạng thái hoàn thiện CV Scoring, AI Job Matching, Security API Keys, Database Seeding, và các UI/UX bug fixes |

---

> 🔔 **Hướng dẫn cập nhật:** Khi hoàn thành một tính năng, nhắn với AI: *"cập nhật docs, đã xong [tên tính năng]"* để AI sửa file này tự động.
