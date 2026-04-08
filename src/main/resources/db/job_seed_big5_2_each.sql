-- Seed 10 realistic jobs for 5 employers
-- Employer IDs are fixed according to current production data:
--   1 = FPT Software
--   2 = VNG Corporation
--   3 = Tiki Corporation
--   4 = Vingroup
--   5 = MoMo
--
-- Safe to import multiple times because each job uses a unique job_code.

SET NAMES utf8mb4;

SET @emp_fpt := 1;
SET @emp_vng := 2;
SET @emp_tiki := 3;
SET @emp_vingroup := 4;
SET @emp_momo := 5;

INSERT INTO jobs (
    title,
    job_code,
    industry,
    location,
    hide_location,
    description,
    requirements,
    salary_min,
    salary_max,
    currency,
    show_salary,
    video_url_1,
    video_url_2,
    deadline,
    urgent_recruitment,
    resume_language,
    employment_type,
    work_from_home,
    work_at_office,
    benefits,
    gender,
    age_min,
    age_max,
    experience,
    education_level,
    additional_info,
    status,
    created_at,
    updated_at,
    employer_id
)
SELECT
    'Java Backend Developer',
    'BIG5-2026-FPT-001',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển các dịch vụ backend cho hệ thống doanh nghiệp và khách hàng quốc tế bằng Java Spring Boot, tích hợp API và tối ưu hiệu năng xử lý.',
    'Có kinh nghiệm Java, Spring Boot, MySQL hoặc PostgreSQL. Hiểu REST API, Git, Docker và quy trình làm việc Agile.',
    18000000,
    32000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 30 DAY),
    1,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Laptop","Bảo hiểm","Đào tạo","Thưởng dự án","Hybrid"]',
    'Nam/Nữ',
    22,
    35,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng tham gia dự án outsource hoặc product quy mô vừa và lớn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_fpt)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-FPT-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'QA Engineer',
    'BIG5-2026-FPT-002',
    'Công nghệ thông tin',
    'Đà Nẵng',
    0,
    'Thiết kế test case, kiểm thử chức năng web, API và phối hợp với đội phát triển để đảm bảo chất lượng phần mềm trước khi release.',
    'Có kinh nghiệm test manual từ 1 năm trở lên, biết viết test case và sử dụng công cụ quản lý lỗi. Biết API testing là lợi thế.',
    13000000,
    22000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 28 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Thưởng dự án","Bảo hiểm","Đào tạo nội bộ","Nghỉ phép năm"]',
    'Nam/Nữ',
    22,
    32,
    '1-2 năm',
    'Đại học',
    'Ứng viên có kinh nghiệm test API và regression test sẽ được ưu tiên.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_fpt)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-FPT-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Product Data Analyst',
    'BIG5-2026-VNG-001',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phân tích dữ liệu hành vi người dùng cho sản phẩm số, xây dựng dashboard và đề xuất insight hỗ trợ tăng trưởng sản phẩm.',
    'Thành thạo SQL, Excel và một công cụ trực quan hóa như Power BI hoặc Tableau. Có tư duy phân tích tốt và khả năng làm việc với product team.',
    18000000,
    30000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 27 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Thưởng hiệu suất","Bảo hiểm","Du lịch","Hybrid"]',
    'Nam/Nữ',
    23,
    34,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm trong mảng app, game hoặc social platform.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vng)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-VNG-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Senior Android Developer',
    'BIG5-2026-VNG-002',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển ứng dụng Android cho sản phẩm có lượng người dùng lớn, tối ưu hiệu năng, bảo trì tính ổn định và phối hợp chặt với team backend.',
    'Có từ 3 năm kinh nghiệm Android, thành thạo Kotlin, hiểu MVVM, REST API, tối ưu hiệu năng và phát hành ứng dụng trên Google Play.',
    25000000,
    40000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 35 DAY),
    1,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    0,
    1,
    '["Laptop","Thưởng năm","Bảo hiểm","Chăm sóc sức khỏe"]',
    'Nam/Nữ',
    24,
    36,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm sản phẩm mobile có quy mô người dùng lớn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vng)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-VNG-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'E-commerce Operations Executive',
    'BIG5-2026-TIKI-001',
    'Thương mại điện tử',
    'TP. Hồ Chí Minh',
    0,
    'Theo dõi vận hành gian hàng, xử lý tồn kho, phối hợp với đội kho vận và chăm sóc khách hàng để đảm bảo tỷ lệ giao hàng đúng hạn.',
    'Có kinh nghiệm vận hành sàn thương mại điện tử, sử dụng Excel tốt, làm việc cẩn thận và có khả năng phối hợp nhiều bộ phận.',
    12000000,
    18000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 24 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Bảo hiểm","Thưởng KPI","Đào tạo","Nghỉ phép năm"]',
    'Nam/Nữ',
    22,
    33,
    '1-2 năm',
    'Đại học',
    'Phù hợp ứng viên từng làm e-commerce, retail hoặc logistics vận hành.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_tiki)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-TIKI-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Performance Marketing Specialist',
    'BIG5-2026-TIKI-002',
    'Marketing',
    'TP. Hồ Chí Minh',
    0,
    'Triển khai và tối ưu các chiến dịch quảng cáo digital cho ngành hàng thương mại điện tử, theo dõi CPA, ROAS và tăng trưởng traffic chất lượng.',
    'Có kinh nghiệm Facebook Ads, Google Ads, đọc hiểu dữ liệu marketing và từng làm việc với landing page hoặc CRM là lợi thế.',
    15000000,
    25000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 26 DAY),
    1,
    'Tiếng Việt',
    'Full-time',
    1,
    1,
    '["Thưởng KPI","Du lịch","Đào tạo","Hybrid"]',
    'Nam/Nữ',
    22,
    34,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm tại sàn thương mại điện tử hoặc agency lớn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_tiki)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-TIKI-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Chuyên viên phân tích kinh doanh',
    'BIG5-2026-VIN-001',
    'Kinh doanh / Bán hàng',
    'Hà Nội',
    0,
    'Phân tích số liệu kinh doanh, xây dựng báo cáo quản trị và hỗ trợ ban điều hành theo dõi hiệu quả hoạt động của đơn vị thành viên.',
    'Thành thạo Excel, PowerPoint và có khả năng tư duy logic tốt. Ưu tiên ứng viên từng làm phân tích kinh doanh, vận hành hoặc tài chính doanh nghiệp.',
    16000000,
    28000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 29 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    0,
    1,
    '["Bảo hiểm","Thưởng năm","Đào tạo","Ăn trưa"]',
    'Nam/Nữ',
    23,
    35,
    '1-2 năm',
    'Đại học',
    'Ứng viên có kinh nghiệm ở tập đoàn hoặc mô hình đa ngành sẽ phù hợp hơn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vingroup
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vingroup)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-VIN-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Project Coordinator',
    'BIG5-2026-VIN-002',
    'Xây dựng / Kiến trúc',
    'Hà Nội',
    0,
    'Theo dõi tiến độ dự án, tổng hợp báo cáo giữa các phòng ban, hỗ trợ điều phối kế hoạch triển khai và các đầu việc hành chính dự án.',
    'Có khả năng tổ chức công việc tốt, giao tiếp tốt, sử dụng thành thạo Excel và PowerPoint. Kinh nghiệm làm PMO hoặc coordinator là lợi thế.',
    14000000,
    22000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 23 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Bảo hiểm","Thưởng lễ","Đào tạo","Nghỉ phép năm"]',
    'Nam/Nữ',
    22,
    32,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm trong lĩnh vực bất động sản, xây dựng hoặc quản trị dự án.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vingroup
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vingroup)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-VIN-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Backend Engineer',
    'BIG5-2026-MOMO-001',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển dịch vụ backend cho nền tảng thanh toán số, xây dựng API ổn định, tối ưu xử lý giao dịch và phối hợp với các team sản phẩm.',
    'Thành thạo Java hoặc Golang, hiểu về microservices, database quan hệ, message queue và có tư duy xử lý hệ thống lớn.',
    22000000,
    38000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 32 DAY),
    1,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Bảo hiểm","Chăm sóc sức khỏe","Laptop","Hybrid","Thưởng hiệu suất"]',
    'Nam/Nữ',
    23,
    35,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm fintech, ngân hàng số hoặc hệ thống giao dịch thời gian thực.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_momo)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-MOMO-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Fraud Risk Analyst',
    'BIG5-2026-MOMO-002',
    'Tài chính / Ngân hàng',
    'TP. Hồ Chí Minh',
    0,
    'Theo dõi giao dịch bất thường, phân tích hành vi rủi ro, phối hợp với các đội vận hành và sản phẩm để giảm gian lận trên nền tảng.',
    'Có kinh nghiệm phân tích dữ liệu, hiểu mô hình kiểm soát rủi ro hoặc fraud detection. Biết SQL là lợi thế lớn.',
    18000000,
    30000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 25 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    0,
    1,
    '["Thưởng hiệu suất","Bảo hiểm","Đào tạo","Chăm sóc sức khỏe"]',
    'Nam/Nữ',
    23,
    34,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm tại ví điện tử, ngân hàng hoặc công ty dữ liệu.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_momo)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5-2026-MOMO-002');

SELECT employer_id, COUNT(*) AS total_seeded_jobs
FROM jobs
WHERE job_code IN (
    'BIG5-2026-FPT-001',
    'BIG5-2026-FPT-002',
    'BIG5-2026-VNG-001',
    'BIG5-2026-VNG-002',
    'BIG5-2026-TIKI-001',
    'BIG5-2026-TIKI-002',
    'BIG5-2026-VIN-001',
    'BIG5-2026-VIN-002',
    'BIG5-2026-MOMO-001',
    'BIG5-2026-MOMO-002'
)
GROUP BY employer_id
ORDER BY employer_id;
