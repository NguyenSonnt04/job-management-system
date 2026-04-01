-- ============================================================
-- CV SCORING CRITERIA SEED DATA
-- Chạy script này trong MySQL sau khi Hibernate tạo bảng
-- ============================================================

-- Xóa dữ liệu cũ nếu có
DELETE FROM cv_scoring_criteria;

-- Thêm 7 tiêu chí đánh giá CV toàn diện
INSERT INTO cv_scoring_criteria (name, description, max_score, display_order, active) VALUES
(
    'Thông tin cá nhân',
    'Đánh giá tính đầy đủ và chuyên nghiệp của thông tin cá nhân. Bao gồm: họ tên, số điện thoại, email, địa chỉ, link LinkedIn/portfolio. Đảm bảo thông tin liên lạc chính xác và dễ nhìn thấy.',
    10,
    1,
    true
),
(
    'Mục tiêu nghề nghiệp',
    'Đánh giá sự rõ ràng, tính thực tế và mức độ phù hợp của mục tiêu nghề nghiệp. Mục tiêu cần cụ thể, có định hướng rõ ràng, phù hợp với vị trí ứng tuyển và thể hiện động lực phát triển.',
    15,
    2,
    true
),
(
    'Kỹ năng chuyên môn',
    'Đánh giá độ phong phú và phù hợp của kỹ năng chuyên môn. Bao gồm: kỹ năng cứng (hard skills), kỹ năng mềm (soft skills), kỹ năng tin học, ngoại ngữ. Kỹ năng cần được phân loại rõ và liên quan đến vị trí.',
    20,
    3,
    true
),
(
    'Kinh nghiệm làm việc',
    'Đánh giá tính liên quan, chiều sâu và thành tựu trong kinh nghiệm làm việc. Ưu tiên kinh nghiệm có số liệu định lượng, kết quả cụ thể, và tiến trình thăng tiến. Tính liên quan với vị trí ứng tuyển là quan trọng.',
    20,
    4,
    true
),
(
    'Học vấn / Chứng chỉ',
    'Đánh giá trình độ học vấn, bằng cấp và chứng chỉ chuyên môn. Bao gồm: chuyên ngành phù hợp, kết quả học tập tốt, các khóa học bổ sung, chứng chỉ quốc tế. Tính cập nhật và liên quan đến vị trí được ưu tiên.',
    15,
    5,
    true
),
(
    'Bố cục / Trình bày',
    'Đánh giá tính chuyên nghiệp, rõ ràng và dễ đọc của bố cục CV. Bao gồm: trình bày logic, font size phù hợp, không lỗi chính tả, sử dụng khoảng trắng hợp lý, heading rõ ràng, không dùng bảng/hình ảnh phức tạp.',
    10,
    6,
    true
),
(
    'Mức độ phù hợp với vị trí',
    'Đánh giá tổng hợp mức độ phù hợp của CV với yêu cầu vị trí. Bao gồm: khớp kỹ năng, kinh nghiệm, học vấn với JD (Job Description). Đây là tiêu chí quan trọng nhất để đánh giá khả năng đạt được việc.',
    10,
    7,
    true
);

-- Verify
SELECT id, name, max_score, display_order FROM cv_scoring_criteria ORDER BY display_order;
