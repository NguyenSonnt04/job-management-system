# Kế hoạch Triển khai Trải nghiệm Tạo CV (Giống TopCV)

## Tổng quan Kiến trúc (Layout 3 khu vực)
1. **Left Panel (Design & Structure):** Khung điều khiển thiết kế bên trái.
2. **Center Preview (Direct Edit):** Bản CV xem trước ở giữa, cho phép click vào text để sửa trực tiếp.
3. **Right Panel (AI Assistant):** Khung chat AI hỗ trợ, có thể thu hồi (collapse/expand) để nhường không gian cho CV.

## Chi tiết Triển khai

### Giai đoạn 1: Xây dựng Layout & Toggles (HTML/CSS)
- Bổ sung cấu trúc CSS Grid/Flexbox đa cột vào `cv-editor.css` và `cv-editor.html`.
- **Cột trái (Design Form):** Dựng UI các tabs:
  - **Khởi tạo/Thiết kế:** Font chữ (Dropdown), Cỡ chữ (Slider), Khoảng cách dòng (Slider), Màu CV (Presets), Nền (Background pattern).
  - **Cấu trúc/Bố cục:** Thêm/Ẩn các section (Dự án, Chứng chỉ, Hoạt động...).
- **Cột phải (AI Chat):** Thêm nút "Đóng/Mở" (Collapse/Expand toggle) và hiệu ứng transition trơn tru.

### Giai đoạn 2: Tính năng Tuỳ chỉnh Thiết kế (JavaScript)
- Viết hàm `updateCvDesign(type, value)` trong `js/main.js` để cập nhật CSS Variables (ví dụ: `--cv-font-family`, `--cv-font-size`, `--cv-line-height`, `--cv-accent-color`).
- Đảm bảo khi thao tác trên Design Panel, CV ở giữa render lại kiểu dáng ngay lập tức.

### Giai đoạn 3: Tính năng Chỉnh sửa Trực tiếp (Direct Edit)
- Kích hoạt chế độ `contenteditable` theo chuẩn. 
- Đảm bảo khi người dùng gõ trực tiếp lên Preview, dữ liệu JSON của CV (`cvData`) tự động được đồng bộ lại để phục vụ cho việc Lưu và Xuất file.
- Tuỳ chọn Kéo thả (Drag & Drop) mục lục CV (sẽ được bổ sung nếu cần).

### Giai đoạn 4: Validation & Hoàn thiện
- Đảm bảo thiết kế responsive và có tính thẩm mỹ "wow" theo tiêu chuẩn hiện đại, áp dụng glassmorphism/shadows mượt mà cho các Panels.
- Kiểm tra tính tương thích lúc bấm In PDF sau khi đã đổi Font/Style.
