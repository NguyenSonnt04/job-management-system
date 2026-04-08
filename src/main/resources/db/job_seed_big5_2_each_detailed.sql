-- Seed 10 realistic jobs for 5 employers with detailed content
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
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Frontend Developer ReactJS/Next.js',
    'BIG5D-2026-FPT-001',
    'IT - Phần mềm',
    'TP. Hồ Chí Minh',
    0,
    'Phối hợp với đội ngũ UI/UX để chuyển đổi wireframe thiết kế thành mã nguồn thực tế, tạo ra các thành phần giao diện trực quan cho ứng dụng.
Tham gia phát triển và duy trì các ứng dụng web sử dụng ReactJS, Next.js và TypeScript.
Làm việc cùng các kỹ sư backend và QA để đảm bảo tính ổn định, hiệu năng và trải nghiệm người dùng.
Hỗ trợ toàn bộ vòng đời ứng dụng từ giai đoạn phân tích yêu cầu, thiết kế, phát triển, kiểm thử đến phát hành.
Viết mã sạch, dễ bảo trì, có khả năng tái sử dụng cao và bám sát coding standard của team.
Tham gia debug, tối ưu hiệu năng ứng dụng và đề xuất các cải tiến giao diện nhằm nâng cao trải nghiệm người dùng.
Phối hợp với đội phát triển sản phẩm để lên kế hoạch cho các tính năng mới và cập nhật xu hướng công nghệ frontend hiện đại.',
    'Có tối thiểu 3 năm kinh nghiệm phát triển frontend thực tế.
Thành thạo HTML, CSS, JavaScript, TypeScript.
Có kinh nghiệm làm việc tốt với ReactJS, Next.js, REST API và Git.
Hiểu biết về state management, component architecture, responsive design.
Có kiến thức về tối ưu hiệu năng frontend, SEO kỹ thuật cơ bản và bảo mật phía client.
Có khả năng đọc hiểu tài liệu kỹ thuật tiếng Anh.
Ưu tiên ứng viên đã từng làm dự án lớn, sản phẩm nhiều người dùng hoặc khách hàng quốc tế.',
    22000000,
    35000000,
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
    '["Chế độ BHYT, BHXH, BHTN đầy đủ","Thưởng dự án","Thưởng lễ tết","Laptop làm việc","Đào tạo nội bộ","Làm việc hybrid"]',
    'Nam/Nữ',
    23,
    32,
    '3 năm',
    'Đại học trở lên',
    'Môi trường chuyên nghiệp, làm việc từ thứ 2 đến thứ 6, có cơ hội tham gia dự án quốc tế tại FPT Software.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_fpt)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-FPT-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Java Backend Developer',
    'BIG5D-2026-FPT-002',
    'IT - Phần mềm',
    'Đà Nẵng',
    0,
    'Thiết kế và phát triển các dịch vụ backend phục vụ hệ thống nghiệp vụ cho khách hàng trong và ngoài nước.
Phân tích yêu cầu, xây dựng RESTful API và tích hợp với nhiều hệ thống vệ tinh.
Làm việc với MySQL, Redis và các công cụ message queue để xử lý dữ liệu ổn định và hiệu quả.
Viết unit test, tối ưu hiệu năng truy vấn và xử lý lỗi ở môi trường staging/production.
Phối hợp với BA, QA và DevOps trong suốt vòng đời phát triển phần mềm.
Tham gia review code, hỗ trợ chia sẻ kiến thức và mentor thành viên junior khi cần.',
    'Có tối thiểu 2-4 năm kinh nghiệm với Java và Spring Boot.
Hiểu rõ OOP, Design Patterns, REST API, JPA/Hibernate.
Có kinh nghiệm làm việc với MySQL hoặc PostgreSQL.
Biết Docker, Git, CI/CD là lợi thế.
Tư duy logic tốt, chủ động trong công việc và có trách nhiệm với deadline.
Ưu tiên ứng viên từng tham gia hệ thống microservices hoặc hệ thống có nhiều người dùng.',
    20000000,
    34000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 32 DAY),
    1,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    0,
    1,
    '["Bảo hiểm sức khỏe","Thưởng dự án","Review lương 2 lần/năm","Đào tạo công nghệ","Du lịch công ty"]',
    'Nam/Nữ',
    24,
    35,
    '2-4 năm',
    'Đại học trở lên',
    'Ưu tiên ứng viên có khả năng làm việc độc lập, tư duy hệ thống và giao tiếp tốt với các team liên quan.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_fpt
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_fpt)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-FPT-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Senior Android Developer',
    'BIG5D-2026-VNG-001',
    'IT - Phần mềm',
    'TP. Hồ Chí Minh',
    0,
    'Tham gia phát triển ứng dụng Android cho sản phẩm có quy mô người dùng lớn.
Thiết kế kiến trúc ứng dụng, xây dựng tính năng mới và tối ưu hiệu năng trên nhiều thiết bị.
Phối hợp chặt chẽ với backend, QA, product owner và UI/UX để đảm bảo tiến độ phát triển.
Viết mã chất lượng cao, có test và dễ mở rộng trong tương lai.
Theo dõi crash, phân tích log và xử lý các vấn đề phát sinh sau release.
Nghiên cứu công nghệ mới nhằm cải thiện trải nghiệm người dùng trên mobile app.',
    'Có tối thiểu 3 năm kinh nghiệm phát triển Android.
Thành thạo Kotlin, Android SDK, Jetpack Components.
Hiểu MVVM, clean architecture, REST API và tối ưu hiệu năng mobile.
Có kinh nghiệm làm việc với Firebase, Crashlytics hoặc analytics tools là lợi thế.
Có tư duy sản phẩm và khả năng phối hợp với nhiều team khác nhau.
Ưu tiên ứng viên từng phát triển ứng dụng có lượng người dùng lớn.',
    28000000,
    42000000,
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
    '["Thưởng hiệu suất","Bảo hiểm cao cấp","Review lương hàng năm","Du lịch","Hỗ trợ thiết bị làm việc"]',
    'Nam/Nữ',
    24,
    34,
    '3 năm',
    'Đại học trở lên',
    'Cơ hội tham gia phát triển sản phẩm số lớn trong hệ sinh thái VNG.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vng)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-VNG-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Product Data Analyst',
    'BIG5D-2026-VNG-002',
    'Phân tích dữ liệu',
    'TP. Hồ Chí Minh',
    0,
    'Phân tích dữ liệu hành vi người dùng, đo lường hiệu quả sản phẩm và hỗ trợ đội ngũ sản phẩm ra quyết định dựa trên số liệu.
Xây dựng dashboard và báo cáo định kỳ cho các chỉ số tăng trưởng, giữ chân và chuyển đổi.
Làm việc với product team để xác định vấn đề và đưa ra insight có giá trị.
Chuẩn hóa nguồn dữ liệu, kiểm tra chất lượng dữ liệu và phối hợp với data engineer khi cần.
Thực hiện ad-hoc analysis cho các chiến dịch hoặc tính năng mới.',
    'Có từ 2 năm kinh nghiệm ở vị trí Data Analyst hoặc Business Analyst.
Thành thạo SQL và Excel; biết Power BI, Tableau hoặc Looker Studio.
Có tư duy phân tích, khả năng kể chuyện bằng dữ liệu và trình bày báo cáo rõ ràng.
Ưu tiên ứng viên từng làm việc ở môi trường product hoặc internet company.
Có khả năng đọc hiểu tài liệu, trao đổi công việc bằng tiếng Anh là lợi thế.',
    20000000,
    32000000,
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
    '["Thưởng hiệu suất","Bảo hiểm","Hybrid","Đào tạo kỹ năng phân tích","Môi trường sản phẩm năng động"]',
    'Nam/Nữ',
    24,
    35,
    '2 năm',
    'Đại học trở lên',
    'Ưu tiên ứng viên có kinh nghiệm với dữ liệu sản phẩm, growth hoặc game/app.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vng
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vng)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-VNG-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'E-commerce Operations Executive',
    'BIG5D-2026-TIKI-001',
    'Thương mại điện tử',
    'TP. Hồ Chí Minh',
    0,
    'Theo dõi vận hành gian hàng và chuỗi xử lý đơn hàng để đảm bảo trải nghiệm mua sắm ổn định cho khách hàng.
Làm việc với bộ phận kho, vận chuyển, chăm sóc khách hàng và ngành hàng để giải quyết các vấn đề phát sinh.
Kiểm soát tồn kho, tỷ lệ giao hàng đúng hạn và các chỉ số vận hành quan trọng.
Tổng hợp báo cáo hằng ngày, đề xuất giải pháp nhằm tối ưu năng suất và giảm lỗi vận hành.
Hỗ trợ triển khai các chiến dịch bán hàng lớn theo mùa và chương trình khuyến mãi.',
    'Có từ 2 năm kinh nghiệm trong vận hành thương mại điện tử, retail hoặc logistics.
Thành thạo Excel, xử lý số liệu tốt và cẩn thận trong công việc.
Có khả năng phối hợp nhiều bộ phận, chịu được áp lực deadline cao.
Tư duy quy trình tốt, chủ động nhận diện và xử lý sự cố vận hành.
Ưu tiên ứng viên từng làm trong ngành thương mại điện tử hoặc sàn bán hàng trực tuyến.',
    14000000,
    22000000,
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
    '["Thưởng KPI","Bảo hiểm","Đào tạo","Mua hàng nội bộ ưu đãi","Du lịch công ty"]',
    'Nam/Nữ',
    23,
    33,
    '2 năm',
    'Đại học trở lên',
    'Phù hợp ứng viên thích môi trường vận hành nhanh, thiên về tối ưu hiệu quả công việc.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_tiki)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-TIKI-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Performance Marketing Specialist',
    'BIG5D-2026-TIKI-002',
    'Marketing',
    'TP. Hồ Chí Minh',
    0,
    'Lập kế hoạch, triển khai và tối ưu các chiến dịch quảng cáo số cho các ngành hàng chủ lực.
Theo dõi hiệu quả quảng cáo trên Facebook Ads, Google Ads và các kênh digital khác.
Phối hợp với team nội dung, thiết kế và product để tối ưu landing page và thông điệp truyền thông.
Phân tích dữ liệu chiến dịch, báo cáo CPA, ROAS, CTR và đề xuất phương án cải thiện.
Tham gia xây dựng chiến lược tăng trưởng cho các campaign lớn theo mùa.',
    'Có ít nhất 2 năm kinh nghiệm performance marketing.
Thành thạo Meta Ads, Google Ads và tư duy đọc hiểu số liệu tốt.
Có kinh nghiệm làm việc với ngành thương mại điện tử, retail hoặc agency là lợi thế.
Biết sử dụng Google Analytics, GTM và các công cụ đo lường hiệu quả quảng cáo.
Kỹ năng giao tiếp tốt, chủ động và có tinh thần thử nghiệm liên tục.',
    16000000,
    26000000,
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
    '["Thưởng KPI","Review lương","Đào tạo chuyên môn","Laptop","Làm việc linh hoạt"]',
    'Nam/Nữ',
    23,
    34,
    '2 năm',
    'Đại học trở lên',
    'Ưu tiên ứng viên từng chạy ngân sách lớn và có kinh nghiệm tối ưu campaign thương mại điện tử.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_tiki
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_tiki)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-TIKI-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Chuyên viên Phân tích Kinh doanh',
    'BIG5D-2026-VIN-001',
    'Kinh doanh / Bán hàng',
    'Hà Nội',
    0,
    'Phân tích dữ liệu kinh doanh, hỗ trợ xây dựng báo cáo quản trị cho ban điều hành và các đơn vị thành viên.
Tổng hợp số liệu vận hành, đánh giá hiệu quả hoạt động và đưa ra đề xuất cải tiến.
Làm việc với nhiều phòng ban để chuẩn hóa quy trình báo cáo và chỉ số đo lường.
Hỗ trợ lập kế hoạch tháng, quý, năm và theo dõi tiến độ thực hiện.
Tham gia các dự án tối ưu hiệu quả vận hành và chất lượng báo cáo quản trị nội bộ.',
    'Có ít nhất 2-3 năm kinh nghiệm ở vị trí Business Analyst, Planning Analyst hoặc tương đương.
Thành thạo Excel, PowerPoint; biết Power BI là lợi thế.
Có khả năng tổng hợp, phân tích số liệu và trình bày báo cáo tốt.
Tư duy logic, cẩn thận và có trách nhiệm cao với số liệu.
Ưu tiên ứng viên từng làm tại tập đoàn hoặc mô hình đa ngành.',
    18000000,
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
    '["Thưởng năm","Bảo hiểm","Ăn trưa","Đào tạo","Môi trường tập đoàn lớn"]',
    'Nam/Nữ',
    24,
    35,
    '2-3 năm',
    'Đại học trở lên',
    'Phù hợp ứng viên thích làm việc với số liệu kinh doanh và môi trường quy trình chuyên nghiệp.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vingroup
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vingroup)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-VIN-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Project Coordinator',
    'BIG5D-2026-VIN-002',
    'Xây dựng / Kiến trúc',
    'Hà Nội',
    0,
    'Theo dõi tiến độ triển khai dự án, tổng hợp đầu việc giữa các bộ phận và hỗ trợ quản lý dự án trong công tác điều phối.
Làm việc với các team kỹ thuật, vận hành, pháp lý và nhà thầu để đảm bảo tiến độ công việc.
Chuẩn bị báo cáo tiến độ, biên bản họp và các tài liệu phục vụ quản trị dự án.
Theo dõi các mốc quan trọng, cảnh báo rủi ro và hỗ trợ xử lý các vướng mắc phát sinh.
Đảm bảo việc phối hợp giữa các bên diễn ra thông suốt và đúng quy trình.',
    'Có từ 2 năm kinh nghiệm làm Project Coordinator, PMO hoặc điều phối dự án.
Kỹ năng tổ chức công việc và giao tiếp tốt.
Thành thạo Excel, PowerPoint; biết sử dụng các công cụ quản lý công việc là lợi thế.
Cẩn thận, có khả năng theo dõi nhiều đầu việc cùng lúc.
Ưu tiên ứng viên từng làm trong lĩnh vực xây dựng, bất động sản hoặc triển khai dự án quy mô lớn.',
    15000000,
    24000000,
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
    '["Bảo hiểm","Thưởng lễ tết","Nghỉ phép năm","Đào tạo nội bộ","Môi trường chuyên nghiệp"]',
    'Nam/Nữ',
    23,
    33,
    '2 năm',
    'Đại học trở lên',
    'Phù hợp ứng viên mạnh về điều phối công việc, theo sát tiến độ và giao tiếp đa phòng ban.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_vingroup
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_vingroup)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-VIN-002');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Backend Engineer',
    'BIG5D-2026-MOMO-001',
    'IT - Phần mềm',
    'TP. Hồ Chí Minh',
    0,
    'Phát triển các dịch vụ backend cho nền tảng thanh toán số, đảm bảo hệ thống ổn định, an toàn và đáp ứng tốt lưu lượng giao dịch lớn.
Thiết kế API, tối ưu database và xử lý logic nghiệp vụ liên quan đến giao dịch.
Phối hợp với product team, QA và DevOps trong quá trình triển khai tính năng mới.
Tham gia xử lý issue production, giám sát hiệu năng và tối ưu khả năng mở rộng của hệ thống.
Review code và đề xuất cải tiến kiến trúc khi cần thiết.',
    'Có tối thiểu 3 năm kinh nghiệm backend với Java, Golang hoặc Node.js.
Hiểu rõ về microservices, database, queue, caching và tối ưu hiệu năng hệ thống.
Có kinh nghiệm làm việc với hệ thống real-time hoặc lưu lượng truy cập cao là lợi thế.
Tư duy logic tốt, chủ động và có trách nhiệm với chất lượng sản phẩm.
Ưu tiên ứng viên từng làm trong fintech, ngân hàng số hoặc ví điện tử.',
    25000000,
    40000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 33 DAY),
    1,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    1,
    1,
    '["Bảo hiểm sức khỏe cao cấp","Thưởng hiệu suất","Laptop","Hybrid","Đào tạo chuyên sâu"]',
    'Nam/Nữ',
    24,
    35,
    '3 năm',
    'Đại học trở lên',
    'Cơ hội làm việc với hệ thống fintech quy mô lớn, yêu cầu tính ổn định và bảo mật cao.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_momo)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-MOMO-001');

INSERT INTO jobs (
    title, job_code, industry, location, hide_location, description, requirements,
    salary_min, salary_max, currency, show_salary, video_url_1, video_url_2, deadline,
    urgent_recruitment, resume_language, employment_type, work_from_home, work_at_office,
    benefits, gender, age_min, age_max, experience, education_level, additional_info,
    status, created_at, updated_at, employer_id
)
SELECT
    'Fraud Risk Analyst',
    'BIG5D-2026-MOMO-002',
    'Tài chính / Ngân hàng',
    'TP. Hồ Chí Minh',
    0,
    'Phân tích các giao dịch bất thường, xây dựng kịch bản phát hiện rủi ro và phối hợp với nhiều bộ phận để giảm thiểu gian lận trên nền tảng.
Theo dõi các tín hiệu cảnh báo, phân tích nguyên nhân và lập báo cáo rủi ro định kỳ.
Làm việc với team vận hành, sản phẩm và dữ liệu để hoàn thiện rule kiểm soát gian lận.
Đề xuất các giải pháp cải thiện quy trình nhận diện và xử lý hành vi bất thường.
Hỗ trợ điều tra các case phát sinh và tham gia cải thiện hệ thống kiểm soát nội bộ.',
    'Có từ 2 năm kinh nghiệm trong lĩnh vực fraud, risk, vận hành giao dịch hoặc phân tích dữ liệu.
Có tư duy logic, khả năng phân tích tình huống và xử lý dữ liệu tốt.
Biết SQL, Excel và hiểu quy trình giao dịch số là lợi thế.
Cẩn thận, bảo mật thông tin tốt và có tinh thần trách nhiệm cao.
Ưu tiên ứng viên từng làm tại ngân hàng, fintech, ví điện tử hoặc đơn vị thanh toán.',
    18000000,
    30000000,
    'VND',
    1,
    NULL,
    NULL,
    DATE_ADD(CURDATE(), INTERVAL 26 DAY),
    0,
    'Tiếng Việt hoặc Tiếng Anh',
    'Full-time',
    0,
    1,
    '["Thưởng hiệu suất","Bảo hiểm","Chăm sóc sức khỏe","Đào tạo nghiệp vụ","Môi trường fintech năng động"]',
    'Nam/Nữ',
    23,
    34,
    '2 năm',
    'Đại học trở lên',
    'Phù hợp ứng viên thích làm việc với dữ liệu, rủi ro và nghiệp vụ tài chính số.',
    'ACTIVE',
    NOW(),
    NOW(),
    @emp_momo
FROM DUAL
WHERE EXISTS (SELECT 1 FROM employers WHERE id = @emp_momo)
  AND NOT EXISTS (SELECT 1 FROM jobs WHERE job_code = 'BIG5D-2026-MOMO-002');

SELECT employer_id, COUNT(*) AS total_seeded_jobs
FROM jobs
WHERE job_code LIKE 'BIG5D-2026-%'
GROUP BY employer_id
ORDER BY employer_id;
