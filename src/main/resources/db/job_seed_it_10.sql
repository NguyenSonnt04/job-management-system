-- Seed 10 realistic IT jobs into `jobs`
-- Purpose:
--   - Manual import into an existing database
--   - Keep data realistic across multiple seniority levels
--   - Avoid duplicate inserts when the script is executed more than once
--
-- How to run:
--   SOURCE src/main/resources/db/job_seed_it_10.sql;
-- Or paste this file into phpMyAdmin / MySQL client and execute.

SET NAMES utf8mb4;

SET @fallback_employer_id := (
    SELECT id
    FROM employers
    ORDER BY id
    LIMIT 1
);

SET @emp_fpt := COALESCE((
    SELECT id
    FROM employers
    WHERE company_name = 'FPT Software'
    LIMIT 1
), @fallback_employer_id);

SET @emp_vng := COALESCE((
    SELECT id
    FROM employers
    WHERE company_name = 'VNG Corporation'
    LIMIT 1
), @fallback_employer_id);

SET @emp_momo := COALESCE((
    SELECT id
    FROM employers
    WHERE company_name = 'MoMo'
    LIMIT 1
), @fallback_employer_id);

SET @emp_tiki := COALESCE((
    SELECT id
    FROM employers
    WHERE company_name = 'Tiki Corporation'
    LIMIT 1
), @fallback_employer_id);

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
    'Software Engineer Intern',
    'ITSEED-2026-001',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Tham gia phát triển các module nội bộ dưới sự hướng dẫn của mentor. Hỗ trợ viết tính năng nhỏ bằng Java/Spring Boot hoặc React, fix bug, viết unit test cơ bản và tham gia daily scrum cùng team sản phẩm.',
    'Sinh viên năm 3-4 ngành CNTT hoặc Khoa học máy tính. Có kiến thức nền tảng về OOP, Git, SQL và một trong các ngôn ngữ Java/JavaScript. Có dự án cá nhân hoặc đồ án trên GitHub là lợi thế.',
    4000000,
    7000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 21 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Internship',
    0,
    1,
    '["Phụ cấp thực tập","Mentor 1:1","Cơ hội lên Fresher","Gửi xe miễn phí"]',
    'Nam/Nữ',
    NULL,
    NULL,
    'Chưa có kinh nghiệm',
    'Đại học',
    'Làm việc tối thiểu 4 ngày/tuần. Ưu tiên ứng viên có thể onsite tại văn phòng Quận 9.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE @emp_fpt IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Fresher QA Engineer',
    'ITSEED-2026-002',
    'Công nghệ thông tin',
    'Hà Nội',
    0,
    'Tham gia kiểm thử chức năng cho web app và mobile app, viết test case, log bug, phối hợp với dev để reproduce issue và hỗ trợ regression testing trước mỗi đợt release.',
    'Tốt nghiệp Cao đẳng/Đại học ngành CNTT hoặc tương đương. Hiểu quy trình test, biết viết test case, có tư duy logic tốt. Ưu tiên ứng viên từng dùng Postman, Jira, SQL hoặc có kinh nghiệm test ở dự án thực tập.',
    8000000,
    12000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 24 DAY),
    1,
    'Tiếng Việt',
    'Full-time',
    0,
    1,
    '["Bảo hiểm sức khỏe","Lương tháng 13","Review lương 2 lần/năm","Đào tạo ISTQB nội bộ"]',
    'Nam/Nữ',
    NULL,
    NULL,
    'Dưới 1 năm',
    'Cao đẳng',
    'Có lộ trình lên QA Automation sau 6-12 tháng nếu đáp ứng tốt năng lực.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE @emp_vng IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Junior Frontend Developer (ReactJS)',
    'ITSEED-2026-003',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển giao diện cho hệ thống tuyển dụng và dashboard nội bộ bằng ReactJS/TypeScript. Làm việc cùng UI/UX designer để hiện thực hóa thiết kế, tối ưu performance và đảm bảo responsive trên desktop/mobile.',
    'Có từ 1 năm kinh nghiệm với ReactJS, JavaScript/TypeScript, HTML/CSS. Hiểu component-based architecture, state management cơ bản, REST API integration và responsive design. Ưu tiên ứng viên có kinh nghiệm với TailwindCSS hoặc Ant Design.',
    14000000,
    20000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 28 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Hybrid 2 ngày/tuần","MacBook cấp phát","Khám sức khỏe định kỳ","Teambuilding hàng quý"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '1-2 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm sản phẩm có lượng người dùng lớn hoặc có portfolio UI đã deploy.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE @emp_tiki IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-003');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Junior Backend Developer (Java Spring Boot)',
    'ITSEED-2026-004',
    'Công nghệ thông tin',
    'Đà Nẵng',
    0,
    'Phát triển API cho hệ thống quản lý người dùng và tuyển dụng. Tham gia thiết kế database, xử lý business logic, viết unit/integration test và hỗ trợ debug production issue dưới sự review của senior engineer.',
    'Có 1-2 năm kinh nghiệm với Java, Spring Boot, JPA/Hibernate, MySQL hoặc PostgreSQL. Hiểu RESTful API, authentication cơ bản, transaction và tối ưu query. Biết Docker là lợi thế.',
    15000000,
    22000000,
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
    '["Bảo hiểm full lương","Lương tháng 13","Thưởng dự án","Hỗ trợ chứng chỉ kỹ thuật"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '1-2 năm',
    'Đại học',
    'Có thể tham gia trực release ngoài giờ theo lịch luân phiên, có phụ cấp riêng.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE @emp_fpt IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-004');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Middle Fullstack Developer (.NET + React)',
    'ITSEED-2026-005',
    'Công nghệ thông tin',
    'Hà Nội',
    0,
    'Phụ trách phát triển end-to-end cho các tính năng trên nền tảng seller portal. Làm việc trực tiếp với PM, UI/UX và QA để thiết kế giải pháp, estimate task, code review chéo và tối ưu trải nghiệm người dùng.',
    'Có tối thiểu 3 năm kinh nghiệm phát triển phần mềm, mạnh ở .NET Core/C# và ReactJS. Hiểu kiến trúc web application, message queue, caching, CI/CD và có khả năng tự breakdown task độc lập.',
    25000000,
    35000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 30 DAY),
    0,
    'Tiếng Anh hoặc Tiếng Việt',
    'Hybrid',
    1,
    1,
    '["Hybrid linh hoạt","Stock review","Bảo hiểm sức khỏe nâng cao","Phụ cấp ăn trưa"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm product company hoặc hệ thống e-commerce / marketplace.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE @emp_tiki IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-005');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Data Engineer',
    'ITSEED-2026-006',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Xây dựng và vận hành data pipeline cho hệ thống phân tích hành vi người dùng và báo cáo vận hành. Làm việc với nguồn dữ liệu từ transaction, event tracking và đối tác thứ ba để đảm bảo dữ liệu sạch, đúng và sẵn sàng cho BI/ML.',
    'Có 2-4 năm kinh nghiệm với SQL, Python hoặc Scala, ETL/ELT pipeline, Airflow, Spark hoặc tương đương. Hiểu data warehouse, data modeling và tối ưu hiệu năng xử lý dữ liệu lớn. Kinh nghiệm với BigQuery hoặc Snowflake là lợi thế.',
    28000000,
    40000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 32 DAY),
    1,
    'Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Thưởng hiệu suất","Bảo hiểm premium","Hỗ trợ khóa học online","Làm việc với data quy mô lớn"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng làm fintech, e-wallet, banking hoặc data-intensive platform.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE @emp_momo IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-006');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Senior Backend Developer (Microservices)',
    'ITSEED-2026-007',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Thiết kế và phát triển các service cốt lõi cho nền tảng thanh toán, đảm bảo hiệu năng, tính ổn định và khả năng mở rộng. Chủ động review thiết kế, hướng dẫn thành viên junior và tham gia xử lý sự cố ở production.',
    'Có từ 5 năm kinh nghiệm backend với Java hoặc Kotlin, thành thạo Spring Boot, event-driven architecture, Redis, Kafka/RabbitMQ và observability. Hiểu rõ microservices, transaction patterns, distributed tracing và secure coding.',
    38000000,
    55000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 35 DAY),
    1,
    'Tiếng Anh hoặc Tiếng Việt',
    'Full-time',
    1,
    1,
    '["ESOP","Bảo hiểm cho người thân","Review lương 2 lần/năm","Thưởng dự án lớn"]',
    'Nam/Nữ',
    NULL,
    NULL,
    'Trên 5 năm',
    'Đại học',
    'Yêu cầu sẵn sàng tham gia on-call theo lịch luân phiên cùng team platform.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE @emp_momo IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-007');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Senior DevOps Engineer',
    'ITSEED-2026-008',
    'Công nghệ thông tin',
    'Hà Nội',
    0,
    'Xây dựng và tối ưu hạ tầng cloud, CI/CD pipeline, monitoring và incident response cho các hệ thống có traffic cao. Phối hợp cùng backend team để tối ưu deployment pipeline, rollback strategy và release cadence.',
    'Có 4-6 năm kinh nghiệm với Linux, Docker, Kubernetes, CI/CD, Terraform, AWS hoặc Azure. Hiểu networking, security hardening, logging/monitoring stack và có kinh nghiệm triển khai production system quy mô lớn.',
    35000000,
    50000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 29 DAY),
    0,
    'Tiếng Anh',
    'Hybrid',
    1,
    1,
    '["Phụ cấp chứng chỉ cloud","Workation","Bảo hiểm mở rộng","Ngân sách học tập hàng năm"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng vận hành Kubernetes cluster hoặc nền tảng game / social / fintech.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE @emp_vng IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-008');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Machine Learning Engineer',
    'ITSEED-2026-009',
    'Công nghệ thông tin',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển và triển khai mô hình ML phục vụ ranking, fraud detection và cá nhân hóa trải nghiệm người dùng. Phối hợp với data scientist và product team để đưa mô hình vào production, theo dõi drift và tối ưu inference cost.',
    'Có 3-5 năm kinh nghiệm với Python, machine learning pipeline, feature engineering, model deployment và MLOps. Thành thạo một trong các framework như PyTorch, TensorFlow, XGBoost; hiểu Docker, API serving và monitoring model sau triển khai.',
    32000000,
    48000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 33 DAY),
    0,
    'Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Bonus theo performance","Bảo hiểm sức khỏe quốc tế","Laptop cấu hình cao","Làm việc với bài toán AI thực tế"]',
    'Nam/Nữ',
    NULL,
    NULL,
    '2-5 năm',
    'Đại học',
    'Ưu tiên ứng viên từng triển khai mô hình trên dữ liệu transaction, recommendation hoặc NLP tiếng Việt.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE @emp_momo IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-009');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Technical Lead (Platform Engineering)',
    'ITSEED-2026-010',
    'Công nghệ thông tin',
    'Hà Nội',
    0,
    'Dẫn dắt team platform engineering từ 6-8 thành viên, chịu trách nhiệm technical direction, architecture review, delivery quality và nâng cao engineering standards. Là đầu mối phối hợp giữa product, security, devops và engineering manager.',
    'Có tối thiểu 7 năm kinh nghiệm phát triển phần mềm, trong đó ít nhất 2 năm ở vai trò lead. Mạnh về system design, microservices, cloud-native architecture, code review, mentoring và quản trị rủi ro kỹ thuật. Có khả năng giao tiếp tốt với stakeholder kỹ thuật và phi kỹ thuật.',
    50000000,
    70000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 40 DAY),
    1,
    'Tiếng Anh',
    'Full-time',
    1,
    1,
    '["ESOP","Thưởng leadership","Bảo hiểm cao cấp","Ngân sách hội thảo/chứng chỉ"]',
    'Nam/Nữ',
    NULL,
    NULL,
    'Trên 5 năm',
    'Đại học',
    'Vị trí phù hợp với ứng viên từng lead team product hoặc platform trong công ty công nghệ quy mô vừa và lớn.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE @emp_vng IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'ITSEED-2026-010');

INSERT INTO job_statistics (job_id, view_count, application_count)
SELECT j.id, 0, 0
FROM jobs j
LEFT JOIN job_statistics js ON js.job_id = j.id
WHERE j.job_code LIKE 'ITSEED-2026-%'
  AND js.job_id IS NULL;

SELECT
    COUNT(*) AS seeded_it_jobs
FROM jobs
WHERE job_code LIKE 'ITSEED-2026-%';
