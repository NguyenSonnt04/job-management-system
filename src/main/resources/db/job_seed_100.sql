-- Seed 100 job records into `jobs`
-- Assumption:
--   - Database is MySQL/MariaDB
--   - Table `employers` already has at least 1 row
--   - All seeded jobs will be attached to the first employer found
--
-- How to run:
--   SOURCE src/main/resources/db/job_seed_100.sql;
-- Or paste this file into phpMyAdmin SQL tab and execute.

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
    CONCAT(
        CASE ((seq.n - 1) MOD 12)
            WHEN 0 THEN 'Nhân viên kinh doanh'
            WHEN 1 THEN 'Chuyên viên marketing'
            WHEN 2 THEN 'Kế toán tổng hợp'
            WHEN 3 THEN 'Nhân viên chăm sóc khách hàng'
            WHEN 4 THEN 'Frontend Developer'
            WHEN 5 THEN 'Backend Developer'
            WHEN 6 THEN 'Nhân sự tổng hợp'
            WHEN 7 THEN 'Chuyên viên tuyển dụng'
            WHEN 8 THEN 'Nhân viên hành chính'
            WHEN 9 THEN 'Thiết kế đồ họa'
            WHEN 10 THEN 'Data Analyst'
            ELSE 'Trưởng nhóm kinh doanh'
        END,
        ' ',
        LPAD(seq.n, 3, '0')
    ) AS title,
    CONCAT('SEEDJOB-', DATE_FORMAT(CURDATE(), '%Y%m'), '-', LPAD(seq.n, 3, '0')) AS job_code,
    CASE ((seq.n - 1) MOD 8)
        WHEN 0 THEN 'Kinh doanh'
        WHEN 1 THEN 'Marketing'
        WHEN 2 THEN 'Kế toán / Kiểm toán'
        WHEN 3 THEN 'Dịch vụ khách hàng'
        WHEN 4 THEN 'Công nghệ thông tin'
        WHEN 5 THEN 'Nhân sự'
        WHEN 6 THEN 'Hành chính / Văn phòng'
        ELSE 'Phân tích dữ liệu'
    END AS industry,
    CASE ((seq.n - 1) MOD 10)
        WHEN 0 THEN 'Hồ Chí Minh'
        WHEN 1 THEN 'Hà Nội'
        WHEN 2 THEN 'Đà Nẵng'
        WHEN 3 THEN 'Bình Dương'
        WHEN 4 THEN 'Đồng Nai'
        WHEN 5 THEN 'Cần Thơ'
        WHEN 6 THEN 'Hải Phòng'
        WHEN 7 THEN 'Vũng Tàu'
        WHEN 8 THEN 'Bắc Ninh'
        ELSE 'Long An'
    END AS location,
    CASE WHEN seq.n MOD 9 = 0 THEN 1 ELSE 0 END AS hide_location,
    CONCAT(
        'Mô tả công việc cho vị trí số ', seq.n, ': ',
        'chịu trách nhiệm thực hiện kế hoạch công việc, phối hợp với các phòng ban liên quan, ',
        'báo cáo tiến độ định kỳ và đề xuất cải tiến để nâng cao hiệu quả vận hành.'
    ) AS description,
    CONCAT(
        'Yêu cầu cho vị trí số ', seq.n, ': ',
        'có tinh thần trách nhiệm, kỹ năng giao tiếp tốt, sử dụng thành thạo tin học văn phòng ',
        'và sẵn sàng học hỏi trong môi trường làm việc chuyên nghiệp.'
    ) AS requirements,
    (6000000 + (seq.n * 180000)) AS salary_min,
    (9500000 + (seq.n * 230000)) AS salary_max,
    'VND' AS currency,
    1 AS show_salary,
    NULL AS video_url_1,
    NULL AS video_url_2,
    DATE_ADD(CURDATE(), INTERVAL (15 + (seq.n MOD 35)) DAY) AS deadline,
    CASE WHEN seq.n MOD 5 = 0 THEN 1 ELSE 0 END AS urgent_recruitment,
    'Tiếng Việt' AS resume_language,
    CASE
        WHEN seq.n MOD 7 = 0 THEN 'Hybrid'
        WHEN seq.n MOD 6 = 0 THEN 'Part-time'
        WHEN seq.n MOD 4 = 0 THEN 'Remote'
        ELSE 'Full-time'
    END AS employment_type,
    CASE WHEN seq.n MOD 7 = 0 OR seq.n MOD 4 = 0 THEN 1 ELSE 0 END AS work_from_home,
    CASE WHEN seq.n MOD 4 = 0 THEN 0 ELSE 1 END AS work_at_office,
    CASE ((seq.n - 1) MOD 6)
        WHEN 0 THEN '["Chế độ bảo hiểm","Thưởng KPI","Đào tạo"]'
        WHEN 1 THEN '["Chăm sóc sức khỏe","Laptop","Du lịch"]'
        WHEN 2 THEN '["Ăn trưa","Xe đưa đón","Thưởng hiệu suất"]'
        WHEN 3 THEN '["Làm việc tại nhà","Đào tạo","Chế độ thưởng"]'
        WHEN 4 THEN '["Công tác phí","Du lịch nước ngoài","Tăng lương"]'
        ELSE '["Nghỉ phép năm","Chăm sóc sức khỏe","Chế độ bảo hiểm"]'
    END AS benefits,
    CASE ((seq.n - 1) MOD 3)
        WHEN 0 THEN 'Nam/Nữ'
        WHEN 1 THEN 'Nam'
        ELSE 'Nữ'
    END AS gender,
    21 AS age_min,
    (30 + (seq.n MOD 9)) AS age_max,
    CASE ((seq.n - 1) MOD 5)
        WHEN 0 THEN 'Không yêu cầu kinh nghiệm'
        WHEN 1 THEN '1 năm'
        WHEN 2 THEN '2 năm'
        WHEN 3 THEN '3 năm'
        ELSE 'Trên 3 năm'
    END AS experience,
    CASE ((seq.n - 1) MOD 4)
        WHEN 0 THEN 'Cao đẳng'
        WHEN 1 THEN 'Đại học'
        WHEN 2 THEN 'Trung cấp'
        ELSE 'Không yêu cầu'
    END AS education_level,
    CONCAT(
        'Thông tin bổ sung cho job ', seq.n, ': ',
        'ưu tiên ứng viên chủ động, thái độ tốt và có thể nhận việc trong vòng 7-15 ngày.'
    ) AS additional_info,
    'ACTIVE' AS status,
    NOW() AS created_at,
    NOW() AS updated_at,
    @seed_employer_id AS employer_id
FROM (
    SELECT ones.n + tens.n * 10 + 1 AS n
    FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) AS ones
    CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) AS tens
    WHERE (ones.n + tens.n * 10) < 100
) AS seq
WHERE @seed_employer_id IS NOT NULL;

SELECT ROW_COUNT() AS inserted_jobs, @seed_employer_id AS used_employer_id;
