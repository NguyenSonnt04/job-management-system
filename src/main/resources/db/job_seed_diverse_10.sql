-- Seed 10 diverse jobs into `jobs`
-- Purpose:
--   - Manual import via phpMyAdmin or MySQL client
--   - Avoid duplicate inserts if the script is executed again
--   - Attach all jobs to the first employer found in the database
--
-- How to run in phpMyAdmin:
--   1. Open database `qltd_db`
--   2. Go to tab `Import`
--   3. Choose this file and execute

SET NAMES utf8mb4;

SET @seed_employer_id := (
    SELECT id
    FROM employers
    ORDER BY id
    LIMIT 1
);

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
    'Frontend Developer',
    'DIVERSE-2026-001',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển giao diện web với ReactJS, tối ưu hiệu năng, phối hợp với backend để tích hợp API cho sản phẩm tuyển dụng.',
    'Có kinh nghiệm ReactJS, JavaScript hoặc TypeScript, HTML/CSS, Git. Biết responsive design và làm việc nhóm tốt.',
    15000000,
    25000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 30 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Laptop","Đào tạo","Thưởng hiệu suất","Làm việc hybrid"]',
    'Nam/Nữ',
    22,
    32,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên có sản phẩm cá nhân hoặc đã từng làm ở startup.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Backend Developer',
    'DIVERSE-2026-002',
    'Công nghệ thông tin',
    'Hà Nội',
    0,
    'Xây dựng REST API bằng Java Spring Boot, làm việc với MySQL, Redis và xử lý các luồng nghiệp vụ của hệ thống.',
    'Thành thạo Java, Spring Boot, SQL. Có hiểu biết về bảo mật API, Docker và log monitoring là lợi thế.',
    18000000,
    32000000,
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
    '["Bảo hiểm","Laptop","Phụ cấp ăn trưa","Thưởng dự án"]',
    'Nam/Nữ',
    23,
    35,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm hệ thống có lượng truy cập lớn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Digital Marketing Specialist',
    'DIVERSE-2026-003',
    'Marketing',
    'Đà Nẵng',
    0,
    'Lên kế hoạch chạy quảng cáo Facebook và Google, tối ưu nội dung landing page và phối hợp với team thiết kế.',
    'Có kinh nghiệm performance marketing, đọc hiểu số liệu, biết sử dụng Google Analytics và Meta Ads Manager.',
    12000000,
    22000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 28 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    1,
    1,
    '["Thưởng KPI","Du lịch","Đào tạo","Làm việc linh hoạt"]',
    'Nam/Nữ',
    22,
    33,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên đã từng tối ưu ngân sách quảng cáo trên 100 triệu mỗi tháng.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-003');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Kế toán tổng hợp',
    'DIVERSE-2026-004',
    'Kế toán / Kiểm toán',
    'TP. Hồ Chí Minh',
    0,
    'Theo dõi thu chi, lập báo cáo tài chính nội bộ, kiểm tra chứng từ và phối hợp với các bộ phận liên quan.',
    'Tốt nghiệp chuyên ngành Kế toán hoặc Tài chính. Nắm vững nghiệp vụ kế toán, Excel tốt và sử dụng được phần mềm MISA.',
    11000000,
    18000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 25 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Bảo hiểm","Thưởng lễ","Hỗ trợ cơm trưa"]',
    'Nam/Nữ',
    23,
    35,
    '1-2 năm',
    'Đại học',
    'Có thể bắt đầu nhận việc trong vòng 2 tuần là lợi thế.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-004');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Chuyên viên tuyển dụng',
    'DIVERSE-2026-005',
    'Nhân sự / Hành chính',
    'Hà Nội',
    0,
    'Phụ trách đăng tin, lọc hồ sơ, liên hệ ứng viên và phối hợp phỏng vấn với quản lý các phòng ban.',
    'Giao tiếp tốt, có kinh nghiệm tuyển dụng khối văn phòng hoặc IT, thành thạo các nền tảng tuyển dụng phổ biến.',
    10000000,
    17000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 22 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Thưởng tuyển dụng","Đào tạo","Nghỉ phép năm"]',
    'Nam/Nữ',
    22,
    32,
    '1-2 năm',
    'Đại học',
    'Phù hợp với ứng viên thích môi trường năng động và làm việc nhiều với con người.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-005');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Nhân viên kinh doanh',
    'DIVERSE-2026-006',
    'Kinh doanh / Bán hàng',
    'Cần Thơ',
    0,
    'Tìm kiếm khách hàng mới, chăm sóc khách hàng hiện có, giới thiệu sản phẩm và chốt hợp đồng bán hàng.',
    'Có kỹ năng giao tiếp và đàm phán tốt. Ưu tiên ứng viên từng làm sales B2B hoặc B2C.',
    8000000,
    25000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 26 DAY),
    1,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Hoa hồng","Thưởng doanh số","Đào tạo sản phẩm"]',
    'Nam/Nữ',
    21,
    35,
    'Dưới 1 năm',
    'Cao đẳng',
    'Lương cứng cộng hoa hồng, thu nhập phụ thuộc kết quả kinh doanh.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-006');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Chăm sóc khách hàng',
    'DIVERSE-2026-007',
    'Kinh doanh / Bán hàng',
    'Hải Phòng',
    0,
    'Hỗ trợ khách hàng qua điện thoại và email, tiếp nhận phản hồi và phối hợp xử lý các vấn đề phát sinh.',
    'Giọng nói dễ nghe, giao tiếp lịch sự, có khả năng xử lý tình huống tốt. Biết dùng Excel và phần mềm CRM là lợi thế.',
    9000000,
    14000000,
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
    '["Bảo hiểm","Thưởng chất lượng dịch vụ","Đào tạo nội bộ"]',
    'Nam/Nữ',
    21,
    30,
    'Dưới 1 năm',
    'Cao đẳng',
    'Có thể làm việc theo ca xoay linh hoạt.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-007');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'UI UX Designer',
    'DIVERSE-2026-008',
    'Thiết kế / Sáng tạo',
    'TP. Hồ Chí Minh',
    0,
    'Thiết kế wireframe, prototype và cải tiến trải nghiệm người dùng cho website và ứng dụng di động.',
    'Thành thạo Figma, có tư duy thẩm mỹ tốt, hiểu về user flow và design system. Có portfolio thực tế.',
    14000000,
    24000000,
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
    '["Laptop","Đào tạo","Review lương","Hybrid"]',
    'Nam/Nữ',
    22,
    33,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm sản phẩm SaaS hoặc thương mại điện tử.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-008');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Data Analyst',
    'DIVERSE-2026-009',
    'Tài chính / Ngân hàng',
    'Hà Nội',
    0,
    'Phân tích dữ liệu vận hành, xây dựng dashboard, theo dõi KPI và hỗ trợ các phòng ban ra quyết định.',
    'Biết SQL, Excel tốt, ưu tiên ứng viên có kinh nghiệm với Power BI hoặc Tableau và tư duy phân tích số liệu.',
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
    1,
    1,
    '["Thưởng hiệu suất","Bảo hiểm","Đào tạo","Hybrid"]',
    'Nam/Nữ',
    23,
    34,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm về dữ liệu kinh doanh, vận hành hoặc tài chính.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-009');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'QA Engineer',
    'DIVERSE-2026-010',
    'Công nghệ thông tin',
    'Đà Nẵng',
    0,
    'Thiết kế test case, kiểm thử tính năng web, phối hợp với team dev để theo dõi và xác nhận lỗi đã được xử lý.',
    'Có kiến thức test manual, viết test case, biết sử dụng công cụ quản lý bug. Hiểu API testing là lợi thế.',
    13000000,
    22000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 31 DAY),
    0,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Bảo hiểm","Đào tạo","Thưởng dự án","Nghỉ phép năm"]',
    'Nam/Nữ',
    22,
    32,
    '1-2 năm',
    'Đại học',
    'Có thể tham gia kiểm thử API và regression test theo sprint.',
    'ACTIVE',
    NOW(),
    NOW(),
    @seed_employer_id
FROM DUAL
WHERE @seed_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'DIVERSE-2026-010');

SELECT
    COUNT(*) AS inserted_or_existing_jobs
FROM jobs
WHERE job_code IN (
    'DIVERSE-2026-001',
    'DIVERSE-2026-002',
    'DIVERSE-2026-003',
    'DIVERSE-2026-004',
    'DIVERSE-2026-005',
    'DIVERSE-2026-006',
    'DIVERSE-2026-007',
    'DIVERSE-2026-008',
    'DIVERSE-2026-009',
    'DIVERSE-2026-010'
);
