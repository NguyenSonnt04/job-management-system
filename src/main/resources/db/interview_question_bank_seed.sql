-- ============================================================
-- SEED: interview_question_bank
-- Ngân hàng câu hỏi cho chế độ tự luyện (static mode)
-- role_key = NULL → áp dụng cho mọi vị trí
-- ============================================================

DELETE FROM interview_question_bank;

-- ════════════════════════════════════════════════════════════
-- HR — Câu hỏi chung (role_key = NULL)
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('hr', 'Giới thiệu bản thân', NULL,
 'Hãy giới thiệu đôi nét về bản thân bạn.',
 'Trình bày ngắn gọn: tên, học vấn, kinh nghiệm và điểm nổi bật nhất phù hợp với vị trí.',
 'Giới thiệu 60-90 giây: tên, chuyên ngành, 1-2 kinh nghiệm liên quan, và lý do ứng tuyển. Kết bằng một câu nêu giá trị bạn mang lại.',
 'easy', 1, 1),

('hr', 'Giới thiệu bản thân', NULL,
 'Điểm mạnh lớn nhất của bạn là gì? Cho ví dụ cụ thể.',
 'Chọn điểm mạnh liên quan đến công việc, kèm ví dụ thực tế bằng cấu trúc STAR.',
 'Ví dụ: "Tôi có khả năng phân tích vấn đề tốt. Khi dự án gặp lỗi production, tôi đã... kết quả là giảm 40% downtime."',
 'easy', 2, 1),

('hr', 'Giới thiệu bản thân', NULL,
 'Điểm yếu của bạn là gì và bạn đang cải thiện như thế nào?',
 'Thành thật về điểm yếu thực sự, nhưng thể hiện bạn đang chủ động khắc phục.',
 'Tránh nói "tôi làm việc quá chăm chỉ". Thay vào đó: "Tôi từng khó từ chối yêu cầu, dẫn đến overcommit. Hiện tôi dùng time-blocking và học nói không có căn cứ."',
 'medium', 3, 1),

('hr', 'Động lực & Mục tiêu', NULL,
 'Tại sao bạn muốn ứng tuyển vào công ty chúng tôi?',
 'Nghiên cứu trước về công ty: sản phẩm, văn hóa, hướng phát triển. Kết nối với mục tiêu cá nhân.',
 'Đề cập cụ thể về sản phẩm/dự án của công ty, văn hóa, và cách vị trí này giúp bạn đạt mục tiêu nghề nghiệp.',
 'easy', 4, 1),

('hr', 'Động lực & Mục tiêu', NULL,
 'Bạn thấy mình ở đâu sau 3-5 năm nữa?',
 'Thể hiện tham vọng thực tế, gắn với lộ trình phát triển tại công ty.',
 'Nêu hướng phát triển chuyên môn và leadership, kết nối với cơ hội tại công ty. Tránh nói quá chung chung hoặc "tôi muốn làm CEO".',
 'easy', 5, 1),

('hr', 'Văn hóa & Làm việc nhóm', NULL,
 'Bạn làm việc tốt hơn khi làm độc lập hay trong nhóm?',
 'Không cần chọn một trong hai — hãy thể hiện bạn linh hoạt và có ví dụ ở cả hai tình huống.',
 'Nêu bạn thích gì ở mỗi cách, ví dụ thực tế khi làm nhóm hiệu quả và khi làm độc lập cần tập trung.',
 'easy', 6, 1),

('hr', 'Văn hóa & Làm việc nhóm', NULL,
 'Kể về một lần bạn xung đột với đồng nghiệp và cách bạn giải quyết.',
 'Dùng STAR. Tập trung vào cách giải quyết xung đột, không chỉ trích người khác.',
 'Mô tả tình huống trung thực, bước giải quyết: lắng nghe, tìm điểm chung, đề xuất giải pháp. Kết quả tích cực.',
 'medium', 7, 1),

('hr', 'Áp lực & Thích nghi', NULL,
 'Bạn xử lý deadline gấp và áp lực công việc như thế nào?',
 'Ví dụ cụ thể về cách bạn ưu tiên task, quản lý thời gian và giữ bình tĩnh dưới áp lực.',
 'Kể về lần làm overnight để deliver feature. Cách ưu tiên: critical path trước. Kết quả: ship đúng hạn + không bug major.',
 'medium', 8, 1),

('hr', 'Lương & Kỳ vọng', NULL,
 'Kỳ vọng lương của bạn là bao nhiêu?',
 'Nghiên cứu market rate trước. Đưa ra range thực tế, thể hiện bạn linh hoạt dựa trên tổng benefit.',
 'Nêu range dựa trên nghiên cứu thị trường và kinh nghiệm của bạn. Thể hiện bạn quan tâm cả cơ hội học hỏi, không chỉ lương.',
 'medium', 9, 1);

-- ════════════════════════════════════════════════════════════
-- BEHAVIORAL — Câu hỏi chung (role_key = NULL)
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('behavioral', 'Lãnh đạo & Chủ động', NULL,
 'Kể về một lần bạn đảm nhận trách nhiệm mà không ai yêu cầu.',
 'Thể hiện tính chủ động (proactivity) và ownership. Dùng cấu trúc STAR.',
 'Ví dụ: Nhận ra bug ảnh hưởng 20% user nhưng không ai assign. Tự phân tích, fix và document. Kết quả: được team lead ghi nhận, quy trình monitor được cải thiện.',
 'medium', 1, 1),

('behavioral', 'Lãnh đạo & Chủ động', NULL,
 'Mô tả một dự án bạn tự hào nhất. Bạn đóng góp gì?',
 'Chọn dự án có impact rõ ràng. Nêu cụ thể: bạn làm gì, kết quả đo được là gì.',
 'Nêu context dự án, vai trò của bạn, challenge chính, cách giải quyết và kết quả bằng số liệu cụ thể.',
 'medium', 2, 1),

('behavioral', 'Thất bại & Học hỏi', NULL,
 'Kể về một lần bạn thất bại. Bạn rút ra bài học gì?',
 'Thành thật, không đổ lỗi cho người khác. Tập trung vào bài học và cách bạn thay đổi sau đó.',
 'Mô tả tình huống thất bại thực sự (không quá nhỏ), bước phân tích nguyên nhân, bài học cụ thể và cách áp dụng sau này.',
 'medium', 3, 1),

('behavioral', 'Thích nghi & Thay đổi', NULL,
 'Kể về một lần bạn phải học công nghệ/kỹ năng mới trong thời gian ngắn.',
 'Thể hiện khả năng học nhanh, tự học và áp dụng ngay vào thực tế.',
 'Context: dự án đột ngột cần công nghệ mới. Cách học: docs + tutorial + practice project. Timeline. Kết quả: deliver được trong bao lâu.',
 'medium', 4, 1),

('behavioral', 'Phản hồi & Cải thiện', NULL,
 'Bạn phản ứng thế nào khi nhận phản hồi tiêu cực từ sếp hoặc đồng nghiệp?',
 'Thể hiện tư duy growth mindset — phản hồi là cơ hội, không phải công kích.',
 'Lắng nghe, xác nhận để hiểu đúng, cảm ơn. Phân tích xem phản hồi có căn cứ không. Nếu có: lên kế hoạch cải thiện. Kết quả tích cực.',
 'medium', 5, 1),

('behavioral', 'Deadline & Ưu tiên', NULL,
 'Kể về lần bạn phải quản lý nhiều task quan trọng cùng lúc.',
 'Thể hiện kỹ năng prioritization, time management và giao tiếp với stakeholder khi cần.',
 'List các task, cách phân tích priority (urgent vs important), cách delegate nếu có, cách giao tiếp với sếp khi cần điều chỉnh scope. Kết quả.',
 'hard', 6, 1);

-- ════════════════════════════════════════════════════════════
-- TECHNICAL — Frontend Developer
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('technical', 'HTML/CSS', 'frontend_developer',
 'Sự khác nhau giữa `display: none`, `visibility: hidden` và `opacity: 0` là gì?',
 'Xét về rendering, accessibility, và event handling.',
 'display:none xóa khỏi flow, không chiếm space, không đọc được bởi screen reader. visibility:hidden ẩn nhưng vẫn chiếm space. opacity:0 ẩn về mặt thị giác nhưng vẫn nhận events và được screen reader đọc.',
 'easy', 1, 1),

('technical', 'JavaScript', 'frontend_developer',
 'Giải thích Event Loop trong JavaScript. Microtask vs Macrotask khác gì nhau?',
 'Call stack, Web APIs, callback queue, microtask queue. Thứ tự thực thi.',
 'JS single-threaded, Event Loop liên tục check call stack. Microtask (Promise.then, queueMicrotask) được xử lý hết trước khi Macrotask (setTimeout, setInterval) tiếp theo chạy.',
 'hard', 2, 1),

('technical', 'React/Framework', 'frontend_developer',
 'React hooks `useEffect` dependency array hoạt động như thế nào? Khi nào gây vấn đề?',
 '[] vs [dep] vs không có deps. Stale closure. Cleanup function.',
 '[] chỉ chạy 1 lần sau mount. [dep] chạy lại khi dep thay đổi. Không có deps thì chạy mỗi render. Stale closure xảy ra khi closure capture giá trị cũ — fix bằng functional update hoặc useRef.',
 'medium', 3, 1),

('technical', 'Performance', 'frontend_developer',
 'Bạn sẽ tối ưu performance của một trang web nặng như thế nào?',
 'Nghĩ theo các chiều: network (bundle size, lazy loading), rendering (repaints, layout thrashing), caching.',
 'Code splitting + lazy loading, image optimization (WebP, lazy), CDN, HTTP caching headers, tree shaking, critical CSS inline, reduce CLS/LCP, virtual scrolling cho list dài.',
 'hard', 4, 1),

('technical', 'CSS Layout', 'frontend_developer',
 'Khi nào dùng Flexbox, khi nào dùng CSS Grid?',
 'Flexbox = 1 chiều. Grid = 2 chiều. Các use case cụ thể.',
 'Flexbox tốt cho navigation bar, centering, spacing các item theo 1 trục. Grid tốt cho page layout, card grid, bất kỳ layout 2 chiều phức tạp. Có thể kết hợp cả hai.',
 'easy', 5, 1);

-- ════════════════════════════════════════════════════════════
-- TECHNICAL — Backend Developer
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('technical', 'Database', 'backend_developer',
 'Sự khác nhau giữa `INNER JOIN`, `LEFT JOIN` và `FULL OUTER JOIN`?',
 'Vẽ Venn diagram trong đầu. Cho ví dụ kết quả với 2 bảng cụ thể.',
 'INNER: chỉ record khớp cả hai bảng. LEFT: tất cả từ bảng trái + record khớp từ bảng phải (NULL nếu không khớp). FULL OUTER: tất cả từ cả hai, NULL chỗ không khớp.',
 'easy', 1, 1),

('technical', 'API Design', 'backend_developer',
 'REST vs GraphQL: khi nào chọn cái nào?',
 'Trade-offs: over-fetching, under-fetching, caching, complexity, use case.',
 'REST: đơn giản, caching tốt, phù hợp public API, CRUD rõ ràng. GraphQL: khi client cần flexible query, mobile với network hạn chế, nhiều resource type liên kết. REST ưu tiên khi team nhỏ hoặc API đơn giản.',
 'medium', 2, 1),

('technical', 'Concurrency', 'backend_developer',
 'Giải thích race condition và cách phòng tránh trong backend.',
 'Ví dụ: đặt vé, trừ tiền. Các giải pháp: lock, transaction, optimistic locking.',
 'Race condition: 2 thread cùng đọc-write một resource. Phòng tránh: DB transaction với ISOLATION level, SELECT FOR UPDATE (pessimistic lock), version field (optimistic lock), Redis distributed lock.',
 'hard', 3, 1),

('technical', 'System Design', 'backend_developer',
 'Thiết kế một API endpoint POST /orders với rate limiting và idempotency.',
 'Xem xét: idempotency key, rate limiter (token bucket/leaky bucket), response khi duplicate request.',
 'Client gửi Idempotency-Key header. Server lưu key vào Redis với TTL. Rate limit bằng Redis sliding window hoặc token bucket per user. Duplicate request trả về response đã cache thay vì xử lý lại.',
 'hard', 4, 1),

('technical', 'Caching', 'backend_developer',
 'Cache invalidation strategies: bạn sử dụng chiến lược nào và khi nào?',
 'TTL, event-driven, write-through, write-around, cache-aside.',
 'Cache-aside: app quản lý cache, phù hợp read-heavy. Write-through: write vào cả cache và DB, consistent. TTL: đơn giản nhưng có thể stale. Event-driven: invalidate khi có event thay đổi data — mạnh nhất nhưng phức tạp.',
 'hard', 5, 1);

-- ════════════════════════════════════════════════════════════
-- TECHNICAL — Data Analyst / Data Scientist
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('technical', 'SQL & Data', 'data_analyst',
 'Bạn có dataset khách hàng với cột purchase_date và amount. Viết SQL tính retention rate tháng 2 (user mua lại sau tháng đầu tiên).',
 'CTE để tìm first purchase month, join để tìm ai mua tháng 2, tính tỉ lệ.',
 'WITH first_purchase AS (SELECT user_id, MIN(DATE_FORMAT(purchase_date,"%Y-%m")) as first_month FROM orders GROUP BY user_id) SELECT COUNT(DISTINCT o.user_id)/COUNT(DISTINCT fp.user_id) as retention FROM first_purchase fp LEFT JOIN orders o ON fp.user_id=o.user_id AND DATE_FORMAT(o.purchase_date,"%Y-%m") = DATE_FORMAT(DATE_ADD(STR_TO_DATE(CONCAT(fp.first_month,"-01"),"%Y-%m-%d"), INTERVAL 1 MONTH),"%Y-%m")',
 'hard', 1, 1),

('technical', 'Thống kê', 'data_analyst',
 'Giải thích sự khác nhau giữa correlation và causation. Cho ví dụ.',
 'Confounding variables. Spurious correlation. Cách kiểm tra causation.',
 'Correlation: 2 biến thay đổi cùng nhau. Causation: một biến gây ra biến kia. Ví dụ: kem và đuối nước tương quan cao nhưng do confounding variable (mùa hè). Kiểm tra causation: randomized experiment, A/B test.',
 'medium', 2, 1),

('technical', 'Machine Learning', 'data_scientist',
 'Giải thích sự khác nhau giữa overfitting và underfitting. Cách xử lý mỗi loại?',
 'Bias-variance tradeoff. Regularization. Cross-validation.',
 'Overfitting: model học quá sâu training data, kém generalize (high variance). Fix: regularization (L1/L2), dropout, more data, early stopping. Underfitting: model quá đơn giản (high bias). Fix: tăng model complexity, thêm features, giảm regularization.',
 'medium', 3, 1);

-- ════════════════════════════════════════════════════════════
-- TECHNICAL — DevOps Engineer
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('technical', 'CI/CD', 'devops_engineer',
 'Mô tả một CI/CD pipeline hoàn chỉnh từ code commit đến production.',
 'Các stage: test, build, staging deploy, smoke test, production deploy. Tools.',
 'Git push → trigger CI: unit test + lint + SAST scan → build Docker image → push to registry → deploy to staging → integration test + smoke test → manual approval (production) → blue-green hoặc canary deploy → monitor + rollback plan.',
 'medium', 1, 1),

('technical', 'Kubernetes', 'devops_engineer',
 'Sự khác nhau giữa Deployment, StatefulSet và DaemonSet trong Kubernetes?',
 'Use case của mỗi loại. State persistence. Pod identity.',
 'Deployment: stateless app, pods interchangeable, rolling update. StatefulSet: stateful app (DB, Kafka), pod có identity cố định, persistent volume riêng. DaemonSet: chạy 1 pod trên mỗi node (log collector, monitoring agent).',
 'hard', 2, 1);

-- ════════════════════════════════════════════════════════════
-- TECHNICAL — HR Generalist / Recruiter
-- ════════════════════════════════════════════════════════════
INSERT INTO interview_question_bank (type_key, category, role_key, question, hint, model_answer, difficulty, display_order, active) VALUES
('technical', 'Tuyển dụng', 'hr_recruiter',
 'Mô tả quy trình tuyển dụng end-to-end bạn đã thực hiện.',
 'Từ Job Requisition đến Onboarding. Metrics bạn track.',
 'JD → sourcing (LinkedIn, headhunt, referral) → screening CV → phone screen → technical/hiring manager interview → offer → onboarding. Metrics: time-to-hire, offer acceptance rate, quality of hire.',
 'medium', 1, 1),

('technical', 'Lương thưởng', 'hr_compensation',
 'Bạn xây dựng salary structure như thế nào cho một công ty 200 người?',
 'Job grading, market benchmarking, pay bands, equity principles.',
 'Job analysis → job grading (Hay/Mercer) → market survey (survey data 25th-75th percentile) → xác định pay philosophy (lag/lead/match market) → build salary bands → communicate to managers.',
 'hard', 1, 1);
