-- ============================================================
-- SEED: interview_levels
-- Cấp độ kinh nghiệm trong phỏng vấn
-- ============================================================

DELETE FROM interview_levels;

INSERT INTO interview_levels (level_key, level_name, description, display_order, active) VALUES
('fresher', 'Fresher (0-1 năm)',  'Mới tốt nghiệp hoặc chưa có kinh nghiệm. Câu hỏi tập trung vào kiến thức nền, thái độ học hỏi và tiềm năng.', 1, 1),
('junior',  'Junior (1-2 năm)',   'Đã có kinh nghiệm cơ bản. Câu hỏi về kỹ năng thực tế và khả năng làm việc độc lập với task nhỏ.', 2, 1),
('middle',  'Middle (2-4 năm)',   'Thành thạo các công việc cơ bản. Câu hỏi về thiết kế giải pháp, xử lý vấn đề phức tạp, và mentoring junior.', 3, 1),
('senior',  'Senior (4-7 năm)',   'Chuyên gia kỹ thuật. Câu hỏi về kiến trúc hệ thống, best practices, leadership kỹ thuật và tác động tổ chức.', 4, 1),
('lead',    'Lead / Manager (7+)','Định hướng kỹ thuật/nhóm. Câu hỏi về chiến lược, quản lý team, roadmap sản phẩm và tầm nhìn dài hạn.', 5, 1);
