-- ============================================================
-- CV SCORING CRITERIA SEED DATA
-- Chạy script này trong MySQL sau khi Hibernate tạo bảng
-- ============================================================

-- Xóa dữ liệu cũ nếu có
DELETE FROM cv_scoring_criteria;

-- Thêm 4 tiêu chí theo yêu cầu
INSERT INTO cv_scoring_criteria (name, description, max_score, display_order, active) VALUES
(
    'Chỉ số Tác động',
    'Đánh giá mức độ các thành tựu và kinh nghiệm trong CV thể hiện được tác động rõ ràng và đo lường được (con số, %, kết quả cụ thể). CV có số liệu cụ thể và kết quả định lượng sẽ được điểm cao.',
    25,
    1,
    true
),
(
    'Độ sâu Kỹ thuật',
    'Đánh giá mức độ chuyên sâu về kỹ năng kỹ thuật, công nghệ và kiến thức chuyên môn. Bao gồm sự đa dạng, độ phù hợp và cập nhật của stack công nghệ, cũng như các dự án kỹ thuật đã thực hiện.',
    25,
    2,
    true
),
(
    'Khả năng mở rộng và Tư duy hệ thống',
    'Đánh giá khả năng thiết kế, xây dựng và vận hành hệ thống quy mô lớn. Tìm kiếm bằng chứng về kinh nghiệm kiến trúc hệ thống, microservices, distributed systems, và tư duy về scalability.',
    25,
    3,
    true
),
(
    'Khả năng đọc hiểu của Máy',
    'Đánh giá cấu trúc và định dạng CV có thân thiện với ATS (Applicant Tracking System) không. Bao gồm: từ khóa liên quan, định dạng rõ ràng, heading chuẩn, không dùng bảng/hình ảnh phức tạp và thông tin đầy đủ.',
    25,
    4,
    true
);

-- Verify
SELECT id, name, max_score, display_order FROM cv_scoring_criteria ORDER BY display_order;
