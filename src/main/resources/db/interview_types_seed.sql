-- ============================================================
-- SEED: interview_types
-- Loại phỏng vấn
-- ============================================================

DELETE FROM interview_types;

INSERT INTO interview_types (type_key, type_name, description, icon_class, display_order, active) VALUES
('mixed',      'Tổng hợp',         'Kết hợp cả 3 loại câu hỏi: HR, kỹ thuật và behavioral. Phù hợp với phỏng vấn thực tế nhất.', 'fa-solid fa-layer-group',      1, 1),
('hr',         'HR & Soft Skills', 'Tập trung vào tính cách, thái độ, văn hóa công ty, strengths/weaknesses và định hướng nghề nghiệp.', 'fa-solid fa-people-arrows',    2, 1),
('technical',  'Kỹ thuật chuyên môn', 'Câu hỏi chuyên sâu theo vị trí: ngôn ngữ lập trình, framework, system design, thuật toán...', 'fa-solid fa-code',             3, 1),
('behavioral', 'Behavioral (STAR)', 'Câu hỏi tình huống thực tế dùng cấu trúc STAR: Situation, Task, Action, Result.', 'fa-solid fa-comments',         4, 1);
