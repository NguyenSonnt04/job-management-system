package Nhom08.Project.config;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seed du lieu demo cho tat ca cac bang chua co seeder.
 * Chi insert khi bang con trong (idempotent).
 * Chay sau DataInitializer (can roles, admin user da ton tai).
 */
@Component
@Order(10)
public class MasterDataSeeder implements ApplicationRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private JobApplicationRepository jobApplicationRepository;
    @Autowired private JobStatisticsRepository jobStatisticsRepository;
    @Autowired private CvScoringCriteriaRepository cvScoringCriteriaRepository;
    @Autowired private CareerPathRepository careerPathRepository;
    @Autowired private CareerPathStageRepository careerPathStageRepository;
    @Autowired private CareerPathSkillRepository careerPathSkillRepository;
    @Autowired private InterviewLevelRepository interviewLevelRepository;
    @Autowired private InterviewTypeRepository interviewTypeRepository;
    @Autowired private InterviewRoleRepository interviewRoleRepository;
    @Autowired private InterviewPromptTemplateRepository interviewPromptTemplateRepository;
    @Autowired private InterviewQuestionBankRepository interviewQuestionBankRepository;
    @Autowired private HeroBannerRepository heroBannerRepository;
    @Autowired private TopEmployerLogoRepository topEmployerLogoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedDemoUsers();
        seedDemoEmployers();
        seedDemoJobs();
        seedDemoJobApplications();
        seedCvScoringCriteria();
        seedCareerPaths();
        seedInterviewLevels();
        seedInterviewTypes();
        seedInterviewRoles();
        seedInterviewPromptTemplates();
        seedInterviewQuestionBank();
        seedHeroBanners();
        seedTopEmployerLogos();
    }

    // ─── Demo Users ──────────────────────────────────────────────────────────────

    private void seedDemoUsers() {
        Role employerRole = roleRepository.findByName(Role.EMPLOYER).orElse(null);
        Role candidateRole = roleRepository.findByName(Role.CANDIDATE).orElse(null);
        if (employerRole == null || candidateRole == null) return;

        // Employer users
        createUserIfAbsent("employer1@fpt.vn", "123456", "Nguyen Van Hung", "0901111001", employerRole);
        createUserIfAbsent("employer2@vng.vn", "123456", "Tran Thi Mai", "0901111002", employerRole);
        createUserIfAbsent("employer3@tiki.vn", "123456", "Le Minh Tuan", "0901111003", employerRole);
        createUserIfAbsent("employer4@vingroup.vn", "123456", "Pham Hoang Nam", "0901111004", employerRole);
        createUserIfAbsent("employer5@momo.vn", "123456", "Vo Thi Lan", "0901111005", employerRole);

        // Candidate users
        createUserIfAbsent("candidate1@gmail.com", "123456", "Nguyen Thi Linh", "0912221001", candidateRole);
        createUserIfAbsent("candidate2@gmail.com", "123456", "Tran Quoc Bao", "0912221002", candidateRole);
        createUserIfAbsent("candidate3@gmail.com", "123456", "Le Hoang Anh", "0912221003", candidateRole);
        createUserIfAbsent("candidate4@gmail.com", "123456", "Pham Minh Duc", "0912221004", candidateRole);
        createUserIfAbsent("candidate5@gmail.com", "123456", "Hoang Thi Ngoc", "0912221005", candidateRole);

        System.out.println("✅ Seeded demo users");
    }

    private void createUserIfAbsent(String email, String password, String fullName, String phone, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User u = new User(email, passwordEncoder.encode(password), role);
            u.setFullName(fullName);
            u.setPhone(phone);
            u.setEnabled(true);
            userRepository.save(u);
        }
    }

    // ─── Demo Employers ──────────────────────────────────────────────────────────

    private void seedDemoEmployers() {
        if (employerRepository.count() > 0) return;

        createEmployer("employer1@fpt.vn", "FPT Software", "Công nghệ thông tin", "5000+",
                "Hồ Chí Minh", "Lô T2, Đường D1, Khu công nghệ cao, Quận 9",
                "FPT Software - Công ty phần mềm hàng đầu Việt Nam với hơn 30,000 nhân viên toàn cầu.",
                "Nguyen Van Hung", "0901111001", "0101248141",
                "https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg", "https://www.fpt-software.com");

        createEmployer("employer2@vng.vn", "VNG Corporation", "Công nghệ thông tin", "3000-5000",
                "Hồ Chí Minh", "182 Lê Đại Hành, Phường 15, Quận 11",
                "VNG Corporation - Công ty công nghệ hàng đầu Việt Nam, sở hữu Zalo, ZaloPay.",
                "Tran Thi Mai", "0901111002", "0301476977",
                "https://brand.zalo.me/wp-content/uploads/2021/07/VNG-logo.png", "https://www.vng.com.vn");

        createEmployer("employer3@tiki.vn", "Tiki Corporation", "Thương mại điện tử", "1000-3000",
                "Hồ Chí Minh", "52 Út Tịch, Phường 4, Quận Tân Bình",
                "Tiki - Sàn thương mại điện tử uy tín hàng đầu Việt Nam.",
                "Le Minh Tuan", "0901111003", "0312456789",
                "https://salt.tikicdn.com/ts/upload/e4/49/6c/tiki-logo.png", "https://tiki.vn");

        createEmployer("employer4@vingroup.vn", "Vingroup", "Bất động sản / Đa ngành", "10000+",
                "Hà Nội", "Số 7, đường Bằng Lăng 1, Khu đô thị Vinhomes Riverside",
                "Vingroup - Tập đoàn kinh tế tư nhân lớn nhất Việt Nam.",
                "Pham Hoang Nam", "0901111004", "0101245886",
                "https://vingroup.net/themes/flavor/images/logo.png", "https://vingroup.net");

        createEmployer("employer5@momo.vn", "MoMo", "Fintech", "1000-3000",
                "Hồ Chí Minh", "390 Hoàng Văn Thụ, Phường 4, Quận Tân Bình",
                "MoMo - Ví điện tử và nền tảng thanh toán di động hàng đầu Việt Nam.",
                "Vo Thi Lan", "0901111005", "0314567890",
                "https://homepage.momocdn.net/img/momo-logo.png", "https://momo.vn");

        System.out.println("✅ Seeded demo employers");
    }

    private void createEmployer(String userEmail, String companyName, String businessType,
            String employeeCount, String province, String address, String description,
            String contactName, String contactPhone, String taxCode, String logoUrl, String website) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return;
        Employer e = new Employer(companyName, user);
        e.setBusinessType(businessType);
        e.setEmployeeCount(employeeCount);
        e.setProvince(province);
        e.setAddress(address);
        e.setDescription(description);
        e.setContactName(contactName);
        e.setContactPhone(contactPhone);
        e.setTaxCode(taxCode);
        e.setLogoUrl(logoUrl);
        e.setWebsite(website);
        employerRepository.save(e);
    }

    // ─── Demo Jobs (100 jobs) ───────────────────────────────────────────────────

    private void seedDemoJobs() {
        if (jobRepository.count() > 0) return;

        Employer fpt = employerRepository.findByCompanyName("FPT Software").orElse(null);
        Employer vng = employerRepository.findByCompanyName("VNG Corporation").orElse(null);
        Employer tiki = employerRepository.findByCompanyName("Tiki Corporation").orElse(null);
        Employer vingroup = employerRepository.findByCompanyName("Vingroup").orElse(null);
        Employer momo = employerRepository.findByCompanyName("MoMo").orElse(null);

        if (fpt == null || vng == null || tiki == null || vingroup == null || momo == null) return;

        List<Job> jobs = new ArrayList<>();

        // ══════════════════════════════════════════════════════════════════════
        // FPT Software — 20 jobs
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Java Developer (Spring Boot)", "FPT-001", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Phát triển ứng dụng backend sử dụng Java Spring Boot, thiết kế RESTful API, tích hợp microservices.",
                "Tốt nghiệp ĐH CNTT. Thành thạo Java, Spring Boot, MySQL/PostgreSQL. Có kinh nghiệm Docker, CI/CD là lợi thế.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("ReactJS Frontend Developer", "FPT-002", "Công nghệ thông tin", "Đà Nẵng", fpt,
                "Phát triển giao diện web với ReactJS, tối ưu hiệu suất, làm việc với UI/UX team.",
                "Thành thạo ReactJS, TypeScript, Redux. Kinh nghiệm responsive design, REST API integration.",
                new BigDecimal("15000000"), new BigDecimal("30000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("DevOps Engineer", "FPT-003", "Công nghệ thông tin", "Hà Nội", fpt,
                "Quản lý hạ tầng cloud AWS/Azure, xây dựng CI/CD pipeline, monitoring và logging.",
                "Kinh nghiệm Docker, Kubernetes, Jenkins/GitLab CI. Hiểu biết về Linux, networking.",
                new BigDecimal("25000000"), new BigDecimal("45000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob(".NET Developer", "FPT-004", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Phát triển ứng dụng enterprise bằng .NET Core, Entity Framework, Azure services.",
                "Thành thạo C#, .NET Core, SQL Server. Kinh nghiệm Azure DevOps, microservices.",
                new BigDecimal("18000000"), new BigDecimal("38000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Thực tập sinh Lập trình", "FPT-005", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Tham gia dự án thực tế, học hỏi quy trình Agile/Scrum, được mentor hướng dẫn.",
                "Sinh viên năm 3-4 ngành CNTT. Có kiến thức cơ bản về lập trình Java hoặc JavaScript.",
                new BigDecimal("5000000"), new BigDecimal("8000000"), "Part-time", "Chưa có kinh nghiệm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Scrum Master", "FPT-006", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Điều phối quy trình Agile/Scrum cho team 8-12 người, loại bỏ impediments, coaching team.",
                "CSM/PSM certified. 2+ năm kinh nghiệm Scrum Master. Hiểu biết về phát triển phần mềm.",
                new BigDecimal("22000000"), new BigDecimal("40000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Solution Architect", "FPT-007", "Công nghệ thông tin", "Hà Nội", fpt,
                "Thiết kế kiến trúc hệ thống cho dự án outsource quy mô lớn, tư vấn công nghệ cho khách hàng.",
                "7+ năm kinh nghiệm phát triển phần mềm. Thành thạo cloud architecture, microservices, system design.",
                new BigDecimal("45000000"), new BigDecimal("80000000"), "Full-time", "Trên 5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Tuyển dụng IT", "FPT-008", "Nhân sự / Hành chính", "Hồ Chí Minh", fpt,
                "Tuyển dụng kỹ sư phần mềm, sourcing ứng viên, phối hợp với hiring manager.",
                "1+ năm kinh nghiệm tuyển dụng IT. Am hiểu thị trường lao động CNTT. Tiếng Anh giao tiếp.",
                new BigDecimal("12000000"), new BigDecimal("20000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Project Manager", "FPT-009", "Công nghệ thông tin", "Đà Nẵng", fpt,
                "Quản lý dự án phần mềm outsource, giao tiếp khách hàng Nhật Bản, lập kế hoạch dự án.",
                "PMP/Prince2 certified. 3+ năm PM. Tiếng Nhật N2 hoặc Tiếng Anh IELTS 7.0+.",
                new BigDecimal("30000000"), new BigDecimal("55000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Tester / QC Engineer", "FPT-010", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Viết test case, thực hiện kiểm thử manual và automation, báo cáo bug.",
                "Kinh nghiệm testing 1+ năm. Biết Selenium hoặc Appium. Hiểu biết ISTQB.",
                new BigDecimal("12000000"), new BigDecimal("22000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Angular Frontend Developer", "FPT-011", "Công nghệ thông tin", "Hà Nội", fpt,
                "Phát triển ứng dụng web SPA bằng Angular, RxJS, NgRx cho các dự án enterprise.",
                "2+ năm Angular. Thành thạo TypeScript, RxJS. Kinh nghiệm REST API, unit testing.",
                new BigDecimal("17000000"), new BigDecimal("32000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Database Administrator", "FPT-012", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Quản trị database MySQL/PostgreSQL/Oracle, tối ưu query, backup & recovery.",
                "3+ năm kinh nghiệm DBA. Thành thạo SQL, performance tuning. Hiểu biết replication.",
                new BigDecimal("20000000"), new BigDecimal("38000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Technical Writer", "FPT-013", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Viết tài liệu kỹ thuật, API documentation, user guide cho các dự án phần mềm.",
                "Tiếng Anh tốt (IELTS 6.5+). Kinh nghiệm viết tài liệu kỹ thuật. Am hiểu SDLC.",
                new BigDecimal("12000000"), new BigDecimal("20000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Security Engineer", "FPT-014", "Công nghệ thông tin", "Hà Nội", fpt,
                "Đánh giá bảo mật ứng dụng, penetration testing, xây dựng security framework.",
                "CEH/OSCP certified. Kinh nghiệm pentest, OWASP Top 10, security tools.",
                new BigDecimal("25000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Nhân viên Hành chính", "FPT-015", "Nhân sự / Hành chính", "Đà Nẵng", fpt,
                "Quản lý văn phòng, hỗ trợ nhân sự, tổ chức sự kiện nội bộ cho chi nhánh FPT Đà Nẵng.",
                "Tốt nghiệp Cao đẳng trở lên. Kỹ năng giao tiếp tốt. Thành thạo MS Office.",
                new BigDecimal("8000000"), new BigDecimal("13000000"), "Full-time", "Dưới 1 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("iOS Developer (Swift)", "FPT-016", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Phát triển ứng dụng iOS native bằng Swift, SwiftUI cho khách hàng quốc tế.",
                "2+ năm kinh nghiệm iOS. Thành thạo Swift, UIKit/SwiftUI. Có app trên App Store.",
                new BigDecimal("20000000"), new BigDecimal("40000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Embedded Software Engineer", "FPT-017", "Công nghệ thông tin", "Hà Nội", fpt,
                "Phát triển phần mềm nhúng cho các thiết bị IoT, automotive sử dụng C/C++.",
                "Thành thạo C/C++, RTOS. Kinh nghiệm embedded Linux, protocol (SPI, I2C, UART).",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("SAP Consultant", "FPT-018", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Tư vấn và triển khai giải pháp SAP (FI/CO, MM, SD) cho doanh nghiệp lớn.",
                "SAP Certified. 3+ năm kinh nghiệm SAP consulting. Hiểu biết quy trình doanh nghiệp.",
                new BigDecimal("30000000"), new BigDecimal("60000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Cloud Engineer (AWS)", "FPT-019", "Công nghệ thông tin", "Đà Nẵng", fpt,
                "Thiết kế và triển khai hạ tầng cloud trên AWS, Infrastructure as Code, cost optimization.",
                "AWS Certified. Kinh nghiệm Terraform/CloudFormation, ECS/EKS, Lambda, RDS.",
                new BigDecimal("25000000"), new BigDecimal("48000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Python Developer", "FPT-020", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Phát triển backend bằng Python Django/FastAPI, xây dựng data pipeline, automation scripts.",
                "Thành thạo Python, Django/FastAPI. Kinh nghiệm PostgreSQL, Redis, Celery.",
                new BigDecimal("17000000"), new BigDecimal("33000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));

        // ══════════════════════════════════════════════════════════════════════
        // VNG Corporation — 20 jobs
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Mobile Developer (Flutter)", "VNG-001", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phát triển ứng dụng mobile đa nền tảng bằng Flutter/Dart cho hệ sinh thái Zalo.",
                "Kinh nghiệm Flutter từ 1 năm. Hiểu biết về state management (Bloc/Riverpod).",
                new BigDecimal("20000000"), new BigDecimal("40000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Data Engineer", "VNG-002", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Xây dựng data pipeline, ETL, data warehouse. Xử lý big data với Spark, Kafka.",
                "Thành thạo Python/Scala, SQL. Kinh nghiệm Spark, Airflow, Kafka.",
                new BigDecimal("25000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("AI/ML Engineer", "VNG-003", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Nghiên cứu và triển khai mô hình AI/ML cho các sản phẩm Zalo, ZaloPay.",
                "Thạc sĩ CNTT hoặc tương đương. Kinh nghiệm TensorFlow/PyTorch.",
                new BigDecimal("30000000"), new BigDecimal("60000000"), "Full-time", "2-5 năm", "Thạc sĩ", "ACTIVE"));
        jobs.add(buildJob("Senior Go Developer", "VNG-004", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phát triển hệ thống backend hiệu năng cao bằng Golang cho game platform.",
                "5+ năm kinh nghiệm backend, 2+ năm Golang. Hiểu sâu về concurrency, system design.",
                new BigDecimal("40000000"), new BigDecimal("70000000"), "Full-time", "Trên 5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Game Developer (Unity)", "VNG-005", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phát triển game mobile bằng Unity/C#, tối ưu hiệu suất, multiplayer networking.",
                "2+ năm Unity. Kinh nghiệm game mobile published. Hiểu biết OOP, design patterns.",
                new BigDecimal("20000000"), new BigDecimal("40000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Backend Developer (Java)", "VNG-006", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phát triển hệ thống backend xử lý hàng triệu request/ngày cho Zalo, ZaloPay.",
                "Thành thạo Java, Spring Framework. Kinh nghiệm high-traffic system, distributed systems.",
                new BigDecimal("22000000"), new BigDecimal("45000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Site Reliability Engineer", "VNG-007", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Đảm bảo uptime 99.99% cho hệ thống Zalo, monitoring, incident response, capacity planning.",
                "Kinh nghiệm Linux, Kubernetes, Prometheus/Grafana. On-call rotation.",
                new BigDecimal("28000000"), new BigDecimal("55000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Product Designer", "VNG-008", "Thiết kế / Sáng tạo", "Hồ Chí Minh", vng,
                "Thiết kế UI/UX cho sản phẩm Zalo, user research, prototyping, design system.",
                "3+ năm kinh nghiệm Product Design. Thành thạo Figma. Portfolio ấn tượng.",
                new BigDecimal("20000000"), new BigDecimal("38000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Pháp chế", "VNG-009", "Pháp lý / Luật", "Hồ Chí Minh", vng,
                "Tư vấn pháp lý cho các sản phẩm fintech, soạn thảo hợp đồng, tuân thủ pháp luật.",
                "Tốt nghiệp ĐH Luật. 2+ năm kinh nghiệm pháp chế doanh nghiệp. Am hiểu luật CNTT.",
                new BigDecimal("15000000"), new BigDecimal("28000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Content Marketing Specialist", "VNG-010", "Marketing", "Hồ Chí Minh", vng,
                "Sáng tạo nội dung cho blog, social media, email marketing cho ZaloPay.",
                "2+ năm content marketing. Kỹ năng viết tốt, am hiểu SEO, social media trends.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Android Developer (Kotlin)", "VNG-011", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phát triển ứng dụng Zalo trên Android, tối ưu performance, xử lý real-time messaging.",
                "2+ năm Android Kotlin. Kinh nghiệm Jetpack Compose, Coroutines, WebSocket.",
                new BigDecimal("22000000"), new BigDecimal("42000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Data Analyst", "VNG-012", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Phân tích dữ liệu người dùng Zalo, xây dựng dashboard, đề xuất growth strategy.",
                "Thành thạo SQL, Python/R. Kinh nghiệm Tableau/PowerBI. Tư duy phân tích tốt.",
                new BigDecimal("15000000"), new BigDecimal("28000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Information Security Analyst", "VNG-013", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Giám sát an ninh mạng, phân tích threat, xây dựng chính sách bảo mật cho hệ thống VNG.",
                "Kinh nghiệm SOC, SIEM tools. Chứng chỉ CEH/CISSP. Am hiểu ISO 27001.",
                new BigDecimal("25000000"), new BigDecimal("48000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Truyền thông Nội bộ", "VNG-014", "Marketing", "Hồ Chí Minh", vng,
                "Xây dựng nội dung truyền thông nội bộ, tổ chức sự kiện, employer branding.",
                "2+ năm kinh nghiệm truyền thông. Kỹ năng viết, tổ chức sự kiện. Sáng tạo.",
                new BigDecimal("13000000"), new BigDecimal("22000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Fresher Backend Developer", "VNG-015", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Tham gia chương trình đào tạo VNG, phát triển backend cho sản phẩm nội bộ.",
                "Sinh viên mới tốt nghiệp CNTT. Kiến thức cơ bản Java/Python, SQL, OOP.",
                new BigDecimal("10000000"), new BigDecimal("15000000"), "Full-time", "Chưa có kinh nghiệm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Performance Engineer", "VNG-016", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Load testing, performance profiling, bottleneck analysis cho hệ thống high-traffic.",
                "Kinh nghiệm JMeter/Gatling, APM tools. Hiểu biết về JVM tuning, database optimization.",
                new BigDecimal("22000000"), new BigDecimal("42000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Video Editor", "VNG-017", "Thiết kế / Sáng tạo", "Hồ Chí Minh", vng,
                "Biên tập video quảng cáo, tutorial, sự kiện cho các sản phẩm VNG.",
                "Thành thạo Premiere Pro, After Effects. Portfolio video đa dạng. Sáng tạo.",
                new BigDecimal("12000000"), new BigDecimal("22000000"), "Full-time", "1-2 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Đối tác Chiến lược", "VNG-018", "Kinh doanh / Bán hàng", "Hồ Chí Minh", vng,
                "Phát triển quan hệ đối tác, đàm phán hợp đồng, mở rộng hệ sinh thái ZaloPay.",
                "3+ năm kinh nghiệm BD/partnership. Am hiểu fintech. Kỹ năng đàm phán xuất sắc.",
                new BigDecimal("20000000"), new BigDecimal("38000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Platform Engineer", "VNG-019", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Xây dựng internal developer platform, CI/CD, infrastructure automation.",
                "Kinh nghiệm Kubernetes, Terraform, ArgoCD. Strong Linux, scripting.",
                new BigDecimal("28000000"), new BigDecimal("52000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Thực tập sinh Data Science", "VNG-020", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Hỗ trợ team Data Science trong EDA, feature engineering, model evaluation.",
                "Sinh viên năm 3-4 ngành CNTT/Toán. Kiến thức Python, ML cơ bản.",
                new BigDecimal("5000000"), new BigDecimal("8000000"), "Part-time", "Chưa có kinh nghiệm", "Đại học", "ACTIVE"));

        // ══════════════════════════════════════════════════════════════════════
        // Tiki Corporation — 20 jobs
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Product Manager - E-commerce", "TIKI-001", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Quản lý sản phẩm, phân tích thị trường, định hướng phát triển tính năng mới cho Tiki.",
                "3+ năm kinh nghiệm PM. Am hiểu e-commerce, data-driven mindset. Tiếng Anh tốt.",
                new BigDecimal("30000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Backend Developer (Node.js)", "TIKI-002", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Phát triển microservices backend bằng Node.js, xử lý traffic cao cho sàn TMĐT.",
                "Thành thạo Node.js, Express/NestJS, MongoDB/PostgreSQL. Kinh nghiệm Redis, message queue.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Thực tập sinh Marketing", "TIKI-003", "Marketing", "Hồ Chí Minh", tiki,
                "Hỗ trợ team Marketing trong các chiến dịch quảng cáo, social media, content creation.",
                "Sinh viên năm cuối ngành Marketing, Truyền thông. Sử dụng tốt các công cụ thiết kế cơ bản.",
                new BigDecimal("5000000"), new BigDecimal("8000000"), "Part-time", "Chưa có kinh nghiệm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Warehouse Operations Manager", "TIKI-004", "Logistics / Xuất nhập khẩu", "Bình Dương", tiki,
                "Quản lý vận hành kho hàng Tiki, tối ưu quy trình fulfillment, quản lý đội ngũ 50+ nhân viên.",
                "3+ năm kinh nghiệm quản lý kho/logistics. Am hiểu WMS. Kỹ năng quản lý nhân sự.",
                new BigDecimal("18000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Category Manager - Electronics", "TIKI-005", "Kinh doanh / Bán hàng", "Hồ Chí Minh", tiki,
                "Quản lý danh mục điện tử, đàm phán với suppliers, chiến lược pricing và promotion.",
                "3+ năm kinh nghiệm category/product management trong TMĐT. Am hiểu thị trường điện tử.",
                new BigDecimal("20000000"), new BigDecimal("38000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Search & Recommendation Engineer", "TIKI-006", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Xây dựng và tối ưu hệ thống search, recommendation engine cho sàn TMĐT Tiki.",
                "Kinh nghiệm Elasticsearch, ML recommendation systems. Thành thạo Python, Java.",
                new BigDecimal("25000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Chăm sóc Khách hàng", "TIKI-007", "Kinh doanh / Bán hàng", "Hồ Chí Minh", tiki,
                "Xử lý khiếu nại, hỗ trợ khách hàng qua chat/call, đảm bảo CSAT score.",
                "Giao tiếp tốt, kiên nhẫn. Kinh nghiệm CSKH 1+ năm. Chấp nhận làm shift.",
                new BigDecimal("8000000"), new BigDecimal("13000000"), "Full-time", "Dưới 1 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("Frontend Developer (Vue.js)", "TIKI-008", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Phát triển giao diện web cho Tiki marketplace bằng Vue.js, Nuxt.js.",
                "2+ năm Vue.js/Nuxt.js. Kinh nghiệm TypeScript, performance optimization.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Nhân viên Vận hành Sàn TMĐT", "TIKI-009", "Kinh doanh / Bán hàng", "Hồ Chí Minh", tiki,
                "Hỗ trợ seller onboarding, giám sát chất lượng sản phẩm, xử lý vi phạm.",
                "Tốt nghiệp ĐH. Tỉ mỉ, trung thực. Kinh nghiệm TMĐT là lợi thế.",
                new BigDecimal("10000000"), new BigDecimal("16000000"), "Full-time", "Dưới 1 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Pricing Analyst", "TIKI-010", "Kế toán / Kiểm toán", "Hồ Chí Minh", tiki,
                "Phân tích giá cả thị trường, xây dựng chiến lược pricing, competitive intelligence.",
                "Thành thạo Excel, SQL, Python. Kinh nghiệm pricing/revenue optimization.",
                new BigDecimal("15000000"), new BigDecimal("28000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Supply Chain Analyst", "TIKI-011", "Logistics / Xuất nhập khẩu", "Hồ Chí Minh", tiki,
                "Phân tích dữ liệu supply chain, tối ưu inventory, dự báo demand.",
                "Kinh nghiệm supply chain/logistics. Thành thạo Excel, SQL. Am hiểu ERP.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Mobile Developer (React Native)", "TIKI-012", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Phát triển app Tiki Shopping trên React Native, tối ưu UX mua sắm mobile.",
                "2+ năm React Native. Kinh nghiệm native modules, performance profiling.",
                new BigDecimal("20000000"), new BigDecimal("40000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Graphic Designer", "TIKI-013", "Thiết kế / Sáng tạo", "Hồ Chí Minh", tiki,
                "Thiết kế banner, campaign visual, social media content cho Tiki.",
                "Thành thạo Adobe Illustrator, Photoshop. Portfolio ấn tượng. Sáng tạo.",
                new BigDecimal("12000000"), new BigDecimal("22000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Performance Marketing Specialist", "TIKI-014", "Marketing", "Hồ Chí Minh", tiki,
                "Quản lý chiến dịch Google Ads, Facebook Ads, tối ưu ROAS cho Tiki.",
                "2+ năm performance marketing. Kinh nghiệm Google/Facebook Ads Manager.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Delivery Driver (Last-mile)", "TIKI-015", "Vận tải / Lái xe", "Hồ Chí Minh", tiki,
                "Giao hàng last-mile cho khách hàng Tiki trong khu vực TP.HCM.",
                "Có xe máy, GPLX. Điện thoại thông minh. Chịu khó, trung thực. Biết đường HCM.",
                new BigDecimal("7000000"), new BigDecimal("14000000"), "Full-time", "Chưa có kinh nghiệm", "Không yêu cầu", "ACTIVE"));
        jobs.add(buildJob("Nhân viên Kho (Picker/Packer)", "TIKI-016", "Logistics / Xuất nhập khẩu", "Bình Dương", tiki,
                "Pick và pack hàng hóa theo đơn, kiểm tra chất lượng đóng gói, sắp xếp kho.",
                "Sức khỏe tốt. Chịu khó, cẩn thận. Chấp nhận làm ca. Không yêu cầu kinh nghiệm.",
                new BigDecimal("6000000"), new BigDecimal("10000000"), "Full-time", "Chưa có kinh nghiệm", "Không yêu cầu", "ACTIVE"));
        jobs.add(buildJob("Business Intelligence Analyst", "TIKI-017", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Xây dựng dashboard, phân tích dữ liệu kinh doanh, hỗ trợ ra quyết định cho ban lãnh đạo.",
                "Thành thạo SQL, Tableau/PowerBI. Kinh nghiệm data modeling, ETL.",
                new BigDecimal("18000000"), new BigDecimal("32000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Fraud Analyst", "TIKI-018", "Công nghệ thông tin", "Hồ Chí Minh", tiki,
                "Phát hiện và ngăn chặn gian lận trên sàn TMĐT, xây dựng rule-based fraud detection.",
                "Kinh nghiệm fraud detection, SQL, Python. Am hiểu TMĐT/payment fraud patterns.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("SEO Specialist", "TIKI-019", "Marketing", "Hồ Chí Minh", tiki,
                "Tối ưu SEO cho trang sản phẩm Tiki, keyword research, technical SEO audit.",
                "2+ năm kinh nghiệm SEO e-commerce. Am hiểu Google Search Console, Ahrefs/SEMrush.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Seller Development Executive", "TIKI-020", "Kinh doanh / Bán hàng", "Hà Nội", tiki,
                "Tìm kiếm và phát triển seller mới cho Tiki marketplace tại khu vực miền Bắc.",
                "Kinh nghiệm sales/BD 1+ năm. Kỹ năng giao tiếp, thuyết phục. Am hiểu TMĐT.",
                new BigDecimal("10000000"), new BigDecimal("20000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));

        // ══════════════════════════════════════════════════════════════════════
        // Vingroup — 20 jobs
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Kế toán Tổng hợp", "VIN-001", "Kế toán / Kiểm toán", "Hà Nội", vingroup,
                "Quản lý sổ sách kế toán, lập báo cáo tài chính, đối chiếu công nợ cho Vingroup.",
                "Tốt nghiệp ĐH Kế toán/Tài chính. 2+ năm kinh nghiệm. Thành thạo phần mềm kế toán.",
                new BigDecimal("12000000"), new BigDecimal("20000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Nhân viên Kinh doanh BĐS", "VIN-002", "Kinh doanh / Bán hàng", "Hồ Chí Minh", vingroup,
                "Tư vấn và bán các sản phẩm bất động sản của Vinhomes. Chăm sóc khách hàng.",
                "Giao tiếp tốt. Có xe máy và laptop cá nhân. Yêu thích kinh doanh.",
                new BigDecimal("10000000"), new BigDecimal("30000000"), "Full-time", "Dưới 1 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("Quản lý Nhà hàng Vinpearl", "VIN-003", "Nhà hàng / Khách sạn", "Đà Nẵng", vingroup,
                "Quản lý vận hành nhà hàng thuộc hệ thống Vinpearl. Đảm bảo chất lượng dịch vụ.",
                "3+ năm kinh nghiệm quản lý F&B. Tiếng Anh giao tiếp. Chấp nhận làm ca.",
                new BigDecimal("15000000"), new BigDecimal("25000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Kỹ sư Xây dựng", "VIN-004", "Xây dựng / Kiến trúc", "Hà Nội", vingroup,
                "Giám sát thi công dự án Vinhomes, kiểm tra chất lượng, lập hồ sơ kỹ thuật.",
                "Tốt nghiệp ĐH Xây dựng. 2+ năm giám sát. Đọc bản vẽ AutoCAD, hiểu biết TCVN.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Kiến trúc sư Thiết kế", "VIN-005", "Xây dựng / Kiến trúc", "Hồ Chí Minh", vingroup,
                "Thiết kế kiến trúc cho dự án Vinhomes, phối hợp với team MEP và structural.",
                "Tốt nghiệp ĐH Kiến trúc. Thành thạo AutoCAD, Revit, SketchUp. Portfolio tốt.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Giáo viên Tiếng Anh (Vinschool)", "VIN-006", "Giáo dục / Đào tạo", "Hà Nội", vingroup,
                "Giảng dạy Tiếng Anh cho học sinh K-12 hệ thống Vinschool theo chuẩn quốc tế.",
                "Bằng TESOL/CELTA. Native hoặc near-native English. Kinh nghiệm giảng dạy K-12.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Bác sĩ Đa khoa (Vinmec)", "VIN-007", "Y tế / Dược phẩm", "Hà Nội", vingroup,
                "Khám, chẩn đoán, điều trị bệnh nhân tại bệnh viện Vinmec. Trực ca theo lịch.",
                "Bằng Bác sĩ Y khoa. Chứng chỉ hành nghề. 2+ năm kinh nghiệm lâm sàng.",
                new BigDecimal("25000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Sau đại học", "ACTIVE"));
        jobs.add(buildJob("Dược sĩ (Vinmec)", "VIN-008", "Y tế / Dược phẩm", "Hồ Chí Minh", vingroup,
                "Quản lý thuốc, tư vấn sử dụng thuốc, kiểm soát tương tác thuốc tại bệnh viện Vinmec.",
                "Bằng Dược sĩ. Chứng chỉ hành nghề. Kinh nghiệm bệnh viện là lợi thế.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Lễ tân Khách sạn Vinpearl", "VIN-009", "Nhà hàng / Khách sạn", "Khánh Hòa", vingroup,
                "Đón tiếp, check-in/out cho khách, xử lý yêu cầu, hỗ trợ concierge tại Vinpearl Nha Trang.",
                "Tiếng Anh giao tiếp tốt. Ngoại hình ưa nhìn. Kinh nghiệm khách sạn 4-5 sao.",
                new BigDecimal("9000000"), new BigDecimal("15000000"), "Full-time", "Dưới 1 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("Nhân viên An ninh VinMart", "VIN-010", "Bán lẻ / Tiêu dùng", "Hà Nội", vingroup,
                "Đảm bảo an ninh trật tự tại chuỗi siêu thị VinMart, tuần tra, kiểm soát ra vào.",
                "Sức khỏe tốt. Trung thực, cẩn thận. Ưu tiên kinh nghiệm an ninh/bảo vệ.",
                new BigDecimal("6000000"), new BigDecimal("9000000"), "Full-time", "Chưa có kinh nghiệm", "Không yêu cầu", "ACTIVE"));
        jobs.add(buildJob("Kỹ sư Điện (VinFast)", "VIN-011", "Kỹ thuật / Cơ khí", "Hải Phòng", vingroup,
                "Thiết kế, lắp đặt hệ thống điện cho nhà máy VinFast. Bảo trì thiết bị điện.",
                "Tốt nghiệp ĐH Điện/Điện tử. Kinh nghiệm PLC, SCADA. Đọc bản vẽ điện.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Trưởng ca Sản xuất (VinFast)", "VIN-012", "Kỹ thuật / Cơ khí", "Hải Phòng", vingroup,
                "Quản lý dây chuyền sản xuất ô tô VinFast, đảm bảo output và chất lượng.",
                "3+ năm kinh nghiệm sản xuất ô tô/cơ khí. Kỹ năng quản lý nhóm. Tiếng Anh đọc tài liệu.",
                new BigDecimal("18000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Marketing BĐS", "VIN-013", "Marketing", "Hồ Chí Minh", vingroup,
                "Lên kế hoạch marketing cho dự án Vinhomes, quản lý event mở bán, digital marketing.",
                "2+ năm marketing BĐS. Am hiểu thị trường bất động sản. Kỹ năng tổ chức sự kiện.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Điều dưỡng (Vinmec)", "VIN-014", "Y tế / Dược phẩm", "Hà Nội", vingroup,
                "Chăm sóc bệnh nhân, thực hiện y lệnh, theo dõi sức khỏe tại bệnh viện Vinmec.",
                "Bằng Cử nhân Điều dưỡng. Chứng chỉ hành nghề. Trung thực, tận tâm.",
                new BigDecimal("10000000"), new BigDecimal("18000000"), "Full-time", "Dưới 1 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Quản lý Cửa hàng VinMart+", "VIN-015", "Bán lẻ / Tiêu dùng", "Hồ Chí Minh", vingroup,
                "Quản lý vận hành cửa hàng VinMart+, quản lý nhân viên, đảm bảo doanh số.",
                "2+ năm quản lý cửa hàng/bán lẻ. Kỹ năng lãnh đạo, quản lý tồn kho.",
                new BigDecimal("11000000"), new BigDecimal("18000000"), "Full-time", "2-5 năm", "Cao đẳng", "ACTIVE"));
        jobs.add(buildJob("Kỹ sư Cơ khí Ô tô (VinFast)", "VIN-016", "Kỹ thuật / Cơ khí", "Hải Phòng", vingroup,
                "Thiết kế, phân tích và tối ưu chi tiết cơ khí cho xe VinFast. Sử dụng CAD/CAE.",
                "Tốt nghiệp ĐH Cơ khí/Ô tô. Thành thạo CATIA/SolidWorks. Tiếng Anh đọc tài liệu.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Giáo viên Toán (Vinschool)", "VIN-017", "Giáo dục / Đào tạo", "Hồ Chí Minh", vingroup,
                "Giảng dạy Toán cho học sinh THPT hệ thống Vinschool, biên soạn đề thi.",
                "Tốt nghiệp ĐH Sư phạm Toán. Kinh nghiệm giảng dạy. Yêu thích giáo dục.",
                new BigDecimal("13000000"), new BigDecimal("22000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Nhân viên Spa (Vinpearl)", "VIN-018", "Nhà hàng / Khách sạn", "Đà Nẵng", vingroup,
                "Cung cấp dịch vụ massage, chăm sóc da cho khách hàng Vinpearl Resort.",
                "Có chứng chỉ spa/massage. Kinh nghiệm resort 4-5 sao là lợi thế.",
                new BigDecimal("8000000"), new BigDecimal("14000000"), "Full-time", "Dưới 1 năm", "Trung cấp", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Pháp lý BĐS", "VIN-019", "Pháp lý / Luật", "Hà Nội", vingroup,
                "Xử lý hồ sơ pháp lý dự án BĐS, giấy phép xây dựng, thủ tục sổ hồng cho khách hàng.",
                "Tốt nghiệp ĐH Luật. 2+ năm kinh nghiệm pháp lý BĐS. Am hiểu Luật Đất đai.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("IT Helpdesk (VinFast)", "VIN-020", "Công nghệ thông tin", "Hải Phòng", vingroup,
                "Hỗ trợ kỹ thuật IT cho nhân viên nhà máy, quản lý thiết bị, troubleshooting.",
                "Kiến thức Windows, networking, hardware. Kinh nghiệm IT support.",
                new BigDecimal("9000000"), new BigDecimal("15000000"), "Full-time", "Dưới 1 năm", "Cao đẳng", "ACTIVE"));

        // ══════════════════════════════════════════════════════════════════════
        // MoMo — 20 jobs
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Fullstack Developer", "MOMO-001", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Phát triển fullstack cho nền tảng thanh toán MoMo. Backend Java + Frontend React.",
                "Thành thạo Java/Kotlin, React/Vue. Kinh nghiệm fintech, bảo mật là lợi thế lớn.",
                new BigDecimal("22000000"), new BigDecimal("42000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("QA Engineer", "MOMO-002", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Kiểm thử phần mềm, viết test case, automation testing cho ứng dụng MoMo.",
                "Kinh nghiệm QA/Testing 1+ năm. Biết Selenium/Appium. Hiểu biết Agile/Scrum.",
                new BigDecimal("14000000"), new BigDecimal("25000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Business Analyst (Fintech)", "MOMO-003", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Phân tích yêu cầu nghiệp vụ thanh toán, viết tài liệu BRD/FRD, phối hợp dev team.",
                "2+ năm kinh nghiệm BA. Am hiểu fintech/payment. Tư duy logic, UML, SQL cơ bản.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Risk Analyst", "MOMO-004", "Tài chính / Ngân hàng", "Hồ Chí Minh", momo,
                "Phân tích rủi ro giao dịch, xây dựng mô hình fraud scoring, AML/KYC compliance.",
                "Kinh nghiệm risk management/fraud trong fintech/banking. SQL, Python.",
                new BigDecimal("18000000"), new BigDecimal("35000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("iOS Developer (Swift)", "MOMO-005", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Phát triển app MoMo trên iOS, tối ưu UX thanh toán, tích hợp payment SDK.",
                "2+ năm iOS Swift. Kinh nghiệm payment integration, biometric authentication.",
                new BigDecimal("22000000"), new BigDecimal("42000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Android Developer (Kotlin)", "MOMO-006", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Phát triển app MoMo trên Android, xử lý offline payment, push notification.",
                "2+ năm Android Kotlin. Kinh nghiệm Jetpack, Room, security best practices.",
                new BigDecimal("22000000"), new BigDecimal("42000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Đối tác Thanh toán", "MOMO-007", "Kinh doanh / Bán hàng", "Hồ Chí Minh", momo,
                "Phát triển đối tác merchant cho MoMo, đàm phán hợp đồng, hỗ trợ tích hợp.",
                "2+ năm BD/sales. Am hiểu payment, e-wallet. Kỹ năng đàm phán tốt.",
                new BigDecimal("14000000"), new BigDecimal("28000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Data Scientist (ML Platform)", "MOMO-008", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Xây dựng mô hình ML cho recommendation, fraud detection, credit scoring.",
                "Thạc sĩ CS/Math. Kinh nghiệm Python, TensorFlow/PyTorch, feature engineering.",
                new BigDecimal("30000000"), new BigDecimal("55000000"), "Full-time", "2-5 năm", "Thạc sĩ", "ACTIVE"));
        jobs.add(buildJob("Product Owner - Digital Lending", "MOMO-009", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Quản lý sản phẩm cho vay kỹ thuật số, xây dựng user journey, backlog management.",
                "3+ năm PO/PM. Am hiểu lending/credit. Kinh nghiệm Agile, data-driven approach.",
                new BigDecimal("30000000"), new BigDecimal("50000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Tuân thủ (Compliance)", "MOMO-010", "Tài chính / Ngân hàng", "Hồ Chí Minh", momo,
                "Đảm bảo MoMo tuân thủ quy định NHNN, PCI-DSS, xây dựng chính sách nội bộ.",
                "Tốt nghiệp Luật/Tài chính. 2+ năm compliance trong banking/fintech.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("UX Researcher", "MOMO-011", "Thiết kế / Sáng tạo", "Hồ Chí Minh", momo,
                "Nghiên cứu người dùng MoMo, usability testing, personas, journey mapping.",
                "2+ năm UX research. Kinh nghiệm survey, interview, A/B testing. Portfolio.",
                new BigDecimal("18000000"), new BigDecimal("32000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Senior Backend Engineer (Microservices)", "MOMO-012", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Thiết kế kiến trúc microservices cho hệ thống payment processing xử lý triệu giao dịch/ngày.",
                "5+ năm backend. Kinh nghiệm distributed systems, event-driven architecture, Kafka.",
                new BigDecimal("35000000"), new BigDecimal("65000000"), "Full-time", "Trên 5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Growth Marketing Manager", "MOMO-013", "Marketing", "Hồ Chí Minh", momo,
                "Xây dựng chiến lược growth, user acquisition, retention marketing cho MoMo.",
                "3+ năm growth/performance marketing. Am hiểu mobile app marketing, attribution.",
                new BigDecimal("25000000"), new BigDecimal("45000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên Kế toán Thanh toán", "MOMO-014", "Kế toán / Kiểm toán", "Hồ Chí Minh", momo,
                "Đối soát giao dịch thanh toán, hạch toán, báo cáo tài chính cho MoMo.",
                "Tốt nghiệp ĐH Kế toán/Tài chính. Kinh nghiệm đối soát thanh toán. Excel nâng cao.",
                new BigDecimal("12000000"), new BigDecimal("20000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("DevSecOps Engineer", "MOMO-015", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Tích hợp security vào CI/CD pipeline, SAST/DAST scanning, container security.",
                "Kinh nghiệm DevOps + security. OWASP, SonarQube, Trivy, vault management.",
                new BigDecimal("28000000"), new BigDecimal("52000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Chuyên viên CSKH Cao cấp", "MOMO-016", "Kinh doanh / Bán hàng", "Hồ Chí Minh", momo,
                "Xử lý khiếu nại phức tạp liên quan đến giao dịch, hoàn tiền, bảo mật tài khoản.",
                "2+ năm CSKH fintech/banking. Kiên nhẫn, giải quyết vấn đề tốt. Làm shift.",
                new BigDecimal("10000000"), new BigDecimal("16000000"), "Full-time", "1-2 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Technical Program Manager", "MOMO-017", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Quản lý chương trình kỹ thuật liên team, roadmap planning, cross-functional coordination.",
                "5+ năm kinh nghiệm tech/PM. Am hiểu fintech architecture. Tiếng Anh thành thạo.",
                new BigDecimal("35000000"), new BigDecimal("60000000"), "Full-time", "Trên 5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Thực tập sinh Backend", "MOMO-018", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Tham gia phát triển backend services, học hỏi microservices architecture tại MoMo.",
                "Sinh viên năm cuối CNTT. Kiến thức Java/Python, SQL, Git. Ham học hỏi.",
                new BigDecimal("5000000"), new BigDecimal("8000000"), "Part-time", "Chưa có kinh nghiệm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("UI Designer (Mobile App)", "MOMO-019", "Thiết kế / Sáng tạo", "Hồ Chí Minh", momo,
                "Thiết kế giao diện app MoMo, icon set, micro-interactions, design system mobile.",
                "2+ năm UI design mobile. Thành thạo Figma. Portfolio mobile app ấn tượng.",
                new BigDecimal("16000000"), new BigDecimal("30000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));
        jobs.add(buildJob("Database Reliability Engineer", "MOMO-020", "Công nghệ thông tin", "Hồ Chí Minh", momo,
                "Quản lý database production (MySQL, MongoDB, Redis) đảm bảo high availability cho payment system.",
                "3+ năm DBA. Kinh nghiệm MySQL replication, sharding, performance tuning, backup/DR.",
                new BigDecimal("25000000"), new BigDecimal("48000000"), "Full-time", "2-5 năm", "Đại học", "ACTIVE"));

        // ══════════════════════════════════════════════════════════════════════
        // DRAFT & CLOSED jobs (5 jobs each employer = 5 total mixed)
        // ══════════════════════════════════════════════════════════════════════
        jobs.add(buildJob("Blockchain Developer", "FPT-D01", "Công nghệ thông tin", "Hồ Chí Minh", fpt,
                "Phát triển smart contract Solidity, DeFi protocol, blockchain integration.",
                "Kinh nghiệm Solidity, Web3.js, Ethereum/BSC. Hiểu biết DeFi, NFT.",
                new BigDecimal("30000000"), new BigDecimal("60000000"), "Full-time", "2-5 năm", "Đại học", "DRAFT"));
        jobs.add(buildJob("Graphic Designer (Đã đóng)", "TIKI-C01", "Thiết kế / Sáng tạo", "Hồ Chí Minh", tiki,
                "Thiết kế banner, ấn phẩm cho các chiến dịch marketing của Tiki.",
                "Thành thạo Adobe Illustrator, Photoshop. Portfolio ấn tượng.",
                new BigDecimal("12000000"), new BigDecimal("20000000"), "Full-time", "1-2 năm", "Đại học", "CLOSED"));
        jobs.add(buildJob("Kỹ sư Cầu đường (Đã đóng)", "VIN-C01", "Xây dựng / Kiến trúc", "Hà Nội", vingroup,
                "Giám sát thi công hạ tầng giao thông cho khu đô thị Vinhomes.",
                "Tốt nghiệp ĐH Cầu đường. 3+ năm kinh nghiệm thi công hạ tầng.",
                new BigDecimal("15000000"), new BigDecimal("28000000"), "Full-time", "2-5 năm", "Đại học", "CLOSED"));
        jobs.add(buildJob("Head of Engineering", "VNG-D01", "Công nghệ thông tin", "Hồ Chí Minh", vng,
                "Dẫn dắt đội ngũ 50+ engineers, chiến lược công nghệ, hiring và mentoring.",
                "10+ năm kinh nghiệm, 5+ năm quản lý. Track record xây dựng team engineering.",
                new BigDecimal("60000000"), new BigDecimal("100000000"), "Full-time", "Trên 5 năm", "Đại học", "DRAFT"));
        jobs.add(buildJob("Marketing Intern (Đã đóng)", "MOMO-C01", "Marketing", "Hồ Chí Minh", momo,
                "Hỗ trợ team marketing trong chiến dịch Tết 2026, content creation, event.",
                "Sinh viên năm 3-4 Marketing/Truyền thông. Năng động, sáng tạo.",
                new BigDecimal("4000000"), new BigDecimal("6000000"), "Part-time", "Chưa có kinh nghiệm", "Đại học", "CLOSED"));

        jobRepository.saveAll(jobs);

        // Create job statistics for each job
        List<Job> savedJobs = jobRepository.findAll();
        for (Job job : savedJobs) {
            if (jobStatisticsRepository.findByJobId(job.getId()).isEmpty()) {
                JobStatistics stats = new JobStatistics(job);
                stats.setViewCount((long) (Math.random() * 500 + 50));
                stats.setApplicationCount((long) (Math.random() * 30 + 5));
                jobStatisticsRepository.save(stats);
            }
        }

        System.out.println("✅ Seeded demo jobs & statistics");
    }

    private Job buildJob(String title, String jobCode, String industry, String location, Employer employer,
            String description, String requirements,
            BigDecimal salaryMin, BigDecimal salaryMax,
            String employmentType, String experience, String educationLevel, String status) {
        Job job = new Job(title, location, LocalDate.now().plusDays(60), employer);
        job.setJobCode(jobCode);
        job.setIndustry(industry);
        job.setDescription(description);
        job.setRequirements(requirements);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setShowSalary(true);
        job.setEmploymentType(employmentType);
        job.setExperience(experience);
        job.setEducationLevel(educationLevel);
        job.setStatus(status);
        job.setUrgentRecruitment(Math.random() > 0.7);
        job.setBenefits("[\"Chế độ bảo hiểm\",\"Đào tạo\",\"Tăng lương\",\"Laptop\",\"Nghỉ phép năm\"]");
        return job;
    }

    // ─── Demo Job Applications ───────────────────────────────────────────────────

    private void seedDemoJobApplications() {
        if (jobApplicationRepository.count() > 0) return;

        List<Job> activeJobs = jobRepository.findByStatus("ACTIVE");
        if (activeJobs.isEmpty()) return;

        User candidate1 = userRepository.findByEmail("candidate1@gmail.com").orElse(null);
        User candidate2 = userRepository.findByEmail("candidate2@gmail.com").orElse(null);
        User candidate3 = userRepository.findByEmail("candidate3@gmail.com").orElse(null);
        User candidate4 = userRepository.findByEmail("candidate4@gmail.com").orElse(null);
        User candidate5 = userRepository.findByEmail("candidate5@gmail.com").orElse(null);

        List<User> candidates = new ArrayList<>();
        if (candidate1 != null) candidates.add(candidate1);
        if (candidate2 != null) candidates.add(candidate2);
        if (candidate3 != null) candidates.add(candidate3);
        if (candidate4 != null) candidates.add(candidate4);
        if (candidate5 != null) candidates.add(candidate5);
        if (candidates.isEmpty()) return;

        String[] statuses = {"PENDING", "PENDING", "PENDING", "INTERVIEW", "ACCEPTED", "REJECTED"};
        List<JobApplication> apps = new ArrayList<>();

        for (int i = 0; i < Math.min(activeJobs.size(), 10); i++) {
            Job job = activeJobs.get(i);
            // Each job gets 1-3 applications
            int numApps = (int) (Math.random() * 3) + 1;
            for (int j = 0; j < numApps && j < candidates.size(); j++) {
                User candidate = candidates.get((i + j) % candidates.size());
                JobApplication app = new JobApplication();
                app.setJob(job);
                app.setUser(candidate);
                app.setFullName(candidate.getFullName());
                app.setEmail(candidate.getEmail());
                app.setPhone(candidate.getPhone());
                app.setCvType("uploaded");
                app.setPrivacy("public");
                app.setCoverLetter("Em xin ứng tuyển vào vị trí " + job.getTitle()
                        + ". Em tin rằng với kinh nghiệm và kỹ năng của mình, em có thể đóng góp hiệu quả cho công ty.");
                app.setStatus(statuses[(int) (Math.random() * statuses.length)]);
                apps.add(app);
            }
        }

        jobApplicationRepository.saveAll(apps);
        System.out.println("✅ Seeded demo job applications");
    }

    // ─── CV Scoring Criteria ─────────────────────────────────────────────────────

    private void seedCvScoringCriteria() {
        if (cvScoringCriteriaRepository.count() > 0) return;

        List<CvScoringCriteria> criteria = List.of(
            buildCriteria("Thông tin cá nhân", "Họ tên, email, SĐT, ảnh, LinkedIn, portfolio — đầy đủ và chuyên nghiệp", 10, 1),
            buildCriteria("Mục tiêu nghề nghiệp", "Summary/Objective ngắn gọn, rõ ràng, phù hợp vị trí ứng tuyển", 10, 2),
            buildCriteria("Kinh nghiệm làm việc", "Mô tả chi tiết, sử dụng action verbs, có số liệu đo lường thành tích", 25, 3),
            buildCriteria("Học vấn", "Trường, ngành, GPA (nếu tốt), các chứng chỉ liên quan", 10, 4),
            buildCriteria("Kỹ năng", "Kỹ năng chuyên môn và mềm phù hợp JD, có phân loại rõ ràng", 15, 5),
            buildCriteria("Dự án cá nhân", "Mô tả dự án, công nghệ sử dụng, vai trò và thành quả cụ thể", 10, 6),
            buildCriteria("Trình bày & Định dạng", "Layout rõ ràng, dễ đọc, font chuyên nghiệp, không lỗi chính tả", 10, 7),
            buildCriteria("Tương thích ATS", "Keyword optimization, format thân thiện ATS, không dùng bảng/hình phức tạp", 10, 8)
        );

        cvScoringCriteriaRepository.saveAll(criteria);
        System.out.println("✅ Seeded CV scoring criteria");
    }

    private CvScoringCriteria buildCriteria(String name, String description, int maxScore, int order) {
        CvScoringCriteria c = new CvScoringCriteria();
        c.setName(name);
        c.setDescription(description);
        c.setMaxScore(maxScore);
        c.setDisplayOrder(order);
        c.setActive(true);
        return c;
    }

    // ─── Career Paths ────────────────────────────────────────────────────────────

    private void seedCareerPaths() {
        if (careerPathRepository.count() > 0) return;

        // 1. Software Engineer
        CareerPath swPath = buildCareerPath("software-engineer", "Kỹ sư Phần mềm",
                "Lộ trình phát triển sự nghiệp từ Fresher đến Tech Lead/Architect trong ngành CNTT.",
                "fas fa-laptop-code", "#2563eb", "Công nghệ thông tin",
                15000000, 80000000, 72, true, 1);
        swPath = careerPathRepository.save(swPath);

        CareerPathStage sw1 = buildStage(swPath, 1, "Fresher Developer", "Học hỏi, làm quen với quy trình phát triển phần mềm thực tế.",
                "Fresher Developer", "fresher", 12, 8000000, 15000000, "fas fa-seedling", "#22c55e");
        sw1 = careerPathStageRepository.save(sw1);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(sw1, 1, "Java / Python / JavaScript", "Technical", true, "beginner"),
            buildSkill(sw1, 2, "Git & Version Control", "Technical", true, "beginner"),
            buildSkill(sw1, 3, "SQL cơ bản", "Technical", true, "beginner"),
            buildSkill(sw1, 4, "Làm việc nhóm", "Soft Skill", true, "beginner")
        ));

        CareerPathStage sw2 = buildStage(swPath, 2, "Junior Developer", "Tự chủ trong việc triển khai tính năng, hiểu kiến trúc hệ thống.",
                "Junior Developer", "junior", 18, 15000000, 25000000, "fas fa-code", "#3b82f6");
        sw2 = careerPathStageRepository.save(sw2);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(sw2, 1, "Spring Boot / Django / Express", "Technical", true, "intermediate"),
            buildSkill(sw2, 2, "REST API Design", "Technical", true, "intermediate"),
            buildSkill(sw2, 3, "Unit Testing", "Technical", true, "intermediate"),
            buildSkill(sw2, 4, "Docker cơ bản", "Technical", false, "beginner")
        ));

        CareerPathStage sw3 = buildStage(swPath, 3, "Mid-level Developer", "Chủ động thiết kế giải pháp, mentor junior, đóng góp vào kiến trúc.",
                "Mid-level Developer", "middle", 24, 25000000, 45000000, "fas fa-cogs", "#8b5cf6");
        sw3 = careerPathStageRepository.save(sw3);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(sw3, 1, "System Design", "Technical", true, "intermediate"),
            buildSkill(sw3, 2, "CI/CD & DevOps", "Technical", true, "intermediate"),
            buildSkill(sw3, 3, "Code Review & Mentoring", "Leadership", true, "intermediate"),
            buildSkill(sw3, 4, "Microservices Architecture", "Technical", false, "intermediate")
        ));

        CareerPathStage sw4 = buildStage(swPath, 4, "Senior Developer / Tech Lead", "Dẫn dắt kỹ thuật, ra quyết định kiến trúc, quản lý đội nhóm.",
                "Senior Developer", "senior", 0, 45000000, 80000000, "fas fa-crown", "#f59e0b");
        sw4 = careerPathStageRepository.save(sw4);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(sw4, 1, "Architecture Design", "Technical", true, "advanced"),
            buildSkill(sw4, 2, "Team Leadership", "Leadership", true, "advanced"),
            buildSkill(sw4, 3, "Cloud Architecture (AWS/GCP)", "Technical", true, "advanced"),
            buildSkill(sw4, 4, "Stakeholder Management", "Soft Skill", true, "intermediate")
        ));

        // 2. Data Scientist
        CareerPath dsPath = buildCareerPath("data-scientist", "Nhà Khoa học Dữ liệu",
                "Lộ trình từ Data Analyst đến Lead Data Scientist trong lĩnh vực AI/ML.",
                "fas fa-brain", "#8b5cf6", "Công nghệ thông tin",
                14000000, 70000000, 60, true, 2);
        dsPath = careerPathRepository.save(dsPath);

        CareerPathStage ds1 = buildStage(dsPath, 1, "Data Analyst", "Phân tích dữ liệu, trực quan hóa, tạo báo cáo insight.",
                "Data Analyst", "fresher", 12, 10000000, 18000000, "fas fa-chart-bar", "#22c55e");
        ds1 = careerPathStageRepository.save(ds1);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ds1, 1, "SQL & Excel nâng cao", "Technical", true, "intermediate"),
            buildSkill(ds1, 2, "Python (Pandas, NumPy)", "Technical", true, "beginner"),
            buildSkill(ds1, 3, "Data Visualization (Tableau/PowerBI)", "Technical", true, "beginner"),
            buildSkill(ds1, 4, "Statistics cơ bản", "Technical", true, "beginner")
        ));

        CareerPathStage ds2 = buildStage(dsPath, 2, "Junior Data Scientist", "Xây dựng mô hình ML, feature engineering, A/B testing.",
                "Junior Data Scientist", "junior", 18, 18000000, 30000000, "fas fa-flask", "#3b82f6");
        ds2 = careerPathStageRepository.save(ds2);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ds2, 1, "Machine Learning (Scikit-learn)", "Technical", true, "intermediate"),
            buildSkill(ds2, 2, "Feature Engineering", "Technical", true, "intermediate"),
            buildSkill(ds2, 3, "Deep Learning cơ bản", "Technical", false, "beginner"),
            buildSkill(ds2, 4, "MLOps basics", "Technical", false, "beginner")
        ));

        CareerPathStage ds3 = buildStage(dsPath, 3, "Senior Data Scientist", "Thiết kế pipeline ML end-to-end, mentor team, research.",
                "Senior Data Scientist", "senior", 0, 35000000, 70000000, "fas fa-robot", "#f59e0b");
        ds3 = careerPathStageRepository.save(ds3);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ds3, 1, "Deep Learning (TensorFlow/PyTorch)", "Technical", true, "advanced"),
            buildSkill(ds3, 2, "NLP / Computer Vision", "Technical", true, "advanced"),
            buildSkill(ds3, 3, "ML System Design", "Technical", true, "advanced"),
            buildSkill(ds3, 4, "Research & Publication", "Research", false, "advanced")
        ));

        // 3. UI/UX Designer
        CareerPath uxPath = buildCareerPath("uiux-designer", "Thiết kế UI/UX",
                "Lộ trình phát triển từ Junior Designer đến Design Lead/Manager.",
                "fas fa-paint-brush", "#ec4899", "Thiết kế / Sáng tạo",
                10000000, 50000000, 60, true, 3);
        uxPath = careerPathRepository.save(uxPath);

        CareerPathStage ux1 = buildStage(uxPath, 1, "Junior UI Designer", "Thiết kế giao diện theo design system, học hỏi UX principles.",
                "Junior UI Designer", "fresher", 12, 8000000, 14000000, "fas fa-pencil-ruler", "#22c55e");
        ux1 = careerPathStageRepository.save(ux1);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ux1, 1, "Figma / Adobe XD", "Technical", true, "intermediate"),
            buildSkill(ux1, 2, "Design Principles", "Technical", true, "beginner"),
            buildSkill(ux1, 3, "Typography & Color Theory", "Technical", true, "beginner")
        ));

        CareerPathStage ux2 = buildStage(uxPath, 2, "Mid-level UX Designer", "User research, wireframing, prototyping, usability testing.",
                "UX Designer", "middle", 24, 18000000, 32000000, "fas fa-users", "#8b5cf6");
        ux2 = careerPathStageRepository.save(ux2);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ux2, 1, "User Research & Personas", "UX", true, "intermediate"),
            buildSkill(ux2, 2, "Wireframing & Prototyping", "UX", true, "intermediate"),
            buildSkill(ux2, 3, "Usability Testing", "UX", true, "intermediate"),
            buildSkill(ux2, 4, "Design System", "Technical", true, "intermediate")
        ));

        CareerPathStage ux3 = buildStage(uxPath, 3, "Senior / Lead Designer", "Xây dựng design system, lead team, chiến lược UX toàn sản phẩm.",
                "Lead Designer", "senior", 0, 32000000, 50000000, "fas fa-crown", "#f59e0b");
        ux3 = careerPathStageRepository.save(ux3);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ux3, 1, "Design Leadership", "Leadership", true, "advanced"),
            buildSkill(ux3, 2, "Product Strategy", "Business", true, "intermediate"),
            buildSkill(ux3, 3, "Design Ops", "Process", true, "advanced")
        ));

        // 4. Digital Marketing
        CareerPath mkPath = buildCareerPath("digital-marketing", "Digital Marketing",
                "Lộ trình từ Marketing Executive đến Marketing Director.",
                "fas fa-bullhorn", "#f97316", "Marketing",
                8000000, 50000000, 60, false, 4);
        mkPath = careerPathRepository.save(mkPath);

        CareerPathStage mk1 = buildStage(mkPath, 1, "Marketing Executive", "Thực hiện chiến dịch, quản lý social media, content creation.",
                "Marketing Executive", "fresher", 12, 8000000, 14000000, "fas fa-pen", "#22c55e");
        mk1 = careerPathStageRepository.save(mk1);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(mk1, 1, "Content Writing", "Marketing", true, "beginner"),
            buildSkill(mk1, 2, "Social Media Management", "Marketing", true, "beginner"),
            buildSkill(mk1, 3, "Google Analytics", "Technical", true, "beginner"),
            buildSkill(mk1, 4, "SEO cơ bản", "Technical", false, "beginner")
        ));

        CareerPathStage mk2 = buildStage(mkPath, 2, "Marketing Specialist", "Lên chiến lược, quản lý ngân sách ads, phân tích ROI.",
                "Marketing Specialist", "middle", 24, 15000000, 28000000, "fas fa-chart-line", "#3b82f6");
        mk2 = careerPathStageRepository.save(mk2);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(mk2, 1, "Facebook/Google Ads", "Marketing", true, "intermediate"),
            buildSkill(mk2, 2, "Marketing Automation", "Technical", true, "intermediate"),
            buildSkill(mk2, 3, "A/B Testing & CRO", "Analytics", true, "intermediate")
        ));

        CareerPathStage mk3 = buildStage(mkPath, 3, "Marketing Manager", "Quản lý team, chiến lược tổng thể, brand management.",
                "Marketing Manager", "senior", 0, 30000000, 50000000, "fas fa-trophy", "#f59e0b");
        mk3 = careerPathStageRepository.save(mk3);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(mk3, 1, "Brand Strategy", "Marketing", true, "advanced"),
            buildSkill(mk3, 2, "Team Management", "Leadership", true, "advanced"),
            buildSkill(mk3, 3, "Budget Planning & P&L", "Business", true, "intermediate")
        ));

        // 5. Business Analyst
        CareerPath baPath = buildCareerPath("business-analyst", "Business Analyst",
                "Lộ trình từ Junior BA đến Product Owner / Head of BA.",
                "fas fa-project-diagram", "#0ea5e9", "Công nghệ thông tin",
                12000000, 55000000, 60, false, 5);
        baPath = careerPathRepository.save(baPath);

        CareerPathStage ba1 = buildStage(baPath, 1, "Junior BA", "Thu thập yêu cầu, viết user stories, hỗ trợ QA testing.",
                "Junior Business Analyst", "fresher", 12, 10000000, 16000000, "fas fa-clipboard-list", "#22c55e");
        ba1 = careerPathStageRepository.save(ba1);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ba1, 1, "Requirement Gathering", "BA", true, "beginner"),
            buildSkill(ba1, 2, "UML & BPMN", "Technical", true, "beginner"),
            buildSkill(ba1, 3, "SQL cơ bản", "Technical", true, "beginner"),
            buildSkill(ba1, 4, "Agile/Scrum", "Process", true, "beginner")
        ));

        CareerPathStage ba2 = buildStage(baPath, 2, "Senior BA", "Phân tích nghiệp vụ phức tạp, thiết kế solution, stakeholder management.",
                "Senior Business Analyst", "middle", 24, 20000000, 35000000, "fas fa-sitemap", "#8b5cf6");
        ba2 = careerPathStageRepository.save(ba2);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ba2, 1, "Solution Design", "BA", true, "intermediate"),
            buildSkill(ba2, 2, "Data Modeling", "Technical", true, "intermediate"),
            buildSkill(ba2, 3, "Stakeholder Management", "Soft Skill", true, "intermediate")
        ));

        CareerPathStage ba3 = buildStage(baPath, 3, "Product Owner / Head of BA", "Quản lý product backlog, OKR, road-map sản phẩm.",
                "Product Owner", "senior", 0, 35000000, 55000000, "fas fa-crown", "#f59e0b");
        ba3 = careerPathStageRepository.save(ba3);
        careerPathSkillRepository.saveAll(List.of(
            buildSkill(ba3, 1, "Product Strategy", "Product", true, "advanced"),
            buildSkill(ba3, 2, "Roadmap Planning", "Product", true, "advanced"),
            buildSkill(ba3, 3, "OKR & KPI", "Business", true, "advanced")
        ));

        System.out.println("✅ Seeded career paths (5 paths)");
    }

    private CareerPath buildCareerPath(String slug, String title, String description,
            String iconUrl, String accentColor, String industryField,
            int salaryMin, int salaryMax, int durationMonths, boolean featured, int order) {
        CareerPath p = new CareerPath();
        p.setSlug(slug);
        p.setTitle(title);
        p.setDescription(description);
        p.setIconUrl(iconUrl);
        p.setAccentColor(accentColor);
        p.setIndustryField(industryField);
        p.setAverageSalaryMin(salaryMin);
        p.setAverageSalaryMax(salaryMax);
        p.setTotalDurationMonths(durationMonths);
        p.setFeatured(featured);
        p.setActive(true);
        p.setDisplayOrder(order);
        return p;
    }

    private CareerPathStage buildStage(CareerPath path, int order, String title, String description,
            String jobTitle, String experienceLevel, int durationMonths,
            int salaryMin, int salaryMax, String iconName, String iconColor) {
        CareerPathStage s = new CareerPathStage();
        s.setCareerPath(path);
        s.setStageOrder(order);
        s.setTitle(title);
        s.setDescription(description);
        s.setJobTitle(jobTitle);
        s.setExperienceLevel(experienceLevel);
        s.setDurationMonths(durationMonths);
        s.setSalaryMin(salaryMin);
        s.setSalaryMax(salaryMax);
        s.setIconName(iconName);
        s.setIconColor(iconColor);
        return s;
    }

    private CareerPathSkill buildSkill(CareerPathStage stage, int order, String name,
            String category, boolean required, String proficiency) {
        CareerPathSkill sk = new CareerPathSkill();
        sk.setStage(stage);
        sk.setSkillOrder(order);
        sk.setName(name);
        sk.setCategory(category);
        sk.setIsRequired(required);
        sk.setProficiencyLevel(proficiency);
        return sk;
    }

    // ─── Interview Levels ────────────────────────────────────────────────────────

    private void seedInterviewLevels() {
        if (interviewLevelRepository.count() > 0) return;

        List.of(
            buildLevel("fresher", "Fresher (0-1 nam)", "Ung vien moi ra truong hoac chua co kinh nghiem", 1),
            buildLevel("junior", "Junior (1-2 nam)", "Ung vien co 1-2 nam kinh nghiem", 2),
            buildLevel("middle", "Middle (2-4 nam)", "Ung vien co 2-4 nam kinh nghiem", 3),
            buildLevel("senior", "Senior (5+ nam)", "Ung vien co tren 5 nam kinh nghiem", 4),
            buildLevel("lead", "Lead / Manager", "Ung vien cap quan ly hoac tech lead", 5)
        ).forEach(interviewLevelRepository::save);

        System.out.println("✅ Seeded interview levels");
    }

    private InterviewLevel buildLevel(String key, String name, String description, int order) {
        InterviewLevel l = new InterviewLevel();
        l.setLevelKey(key);
        l.setLevelName(name);
        l.setDescription(description);
        l.setDisplayOrder(order);
        l.setActive(true);
        return l;
    }

    // ─── Interview Types ─────────────────────────────────────────────────────────

    private void seedInterviewTypes() {
        if (interviewTypeRepository.count() > 0) return;

        List.of(
            buildType("hr", "Phong van HR", "Cau hoi ve tinh cach, dong luc, van hoa cong ty", "fas fa-user-tie", 1),
            buildType("technical", "Phong van Ky thuat", "Cau hoi ve ky nang chuyen mon, coding, system design", "fas fa-code", 2),
            buildType("behavioral", "Phong van Hanh vi", "Cau hoi tinh huong STAR method, xu ly van de thuc te", "fas fa-comments", 3),
            buildType("mixed", "Phong van Tong hop", "Ket hop HR + Technical + Behavioral", "fas fa-layer-group", 4)
        ).forEach(interviewTypeRepository::save);

        System.out.println("✅ Seeded interview types");
    }

    private InterviewType buildType(String key, String name, String description, String icon, int order) {
        InterviewType t = new InterviewType();
        t.setTypeKey(key);
        t.setTypeName(name);
        t.setDescription(description);
        t.setIconClass(icon);
        t.setDisplayOrder(order);
        t.setActive(true);
        return t;
    }

    // ─── Interview Roles ─────────────────────────────────────────────────────────

    private void seedInterviewRoles() {
        if (interviewRoleRepository.count() > 0) return;

        // Cong nghe thong tin
        saveRole("frontend_developer", "Frontend Developer", "Cong nghe thong tin", "tech", "fab fa-react", 1);
        saveRole("backend_developer", "Backend Developer", "Cong nghe thong tin", "tech", "fas fa-server", 2);
        saveRole("fullstack_developer", "Fullstack Developer", "Cong nghe thong tin", "tech", "fas fa-laptop-code", 3);
        saveRole("mobile_developer", "Mobile Developer", "Cong nghe thong tin", "tech", "fas fa-mobile-alt", 4);
        saveRole("devops_engineer", "DevOps Engineer", "Cong nghe thong tin", "tech", "fas fa-cloud", 5);
        saveRole("data_engineer", "Data Engineer", "Cong nghe thong tin", "tech", "fas fa-database", 6);
        saveRole("data_scientist", "Data Scientist", "Cong nghe thong tin", "tech", "fas fa-brain", 7);
        saveRole("qa_engineer", "QA Engineer", "Cong nghe thong tin", "tech", "fas fa-bug", 8);
        saveRole("business_analyst", "Business Analyst", "Cong nghe thong tin", "tech", "fas fa-chart-pie", 9);
        saveRole("product_manager", "Product Manager", "Cong nghe thong tin", "tech", "fas fa-tasks", 10);

        // Kinh doanh
        saveRole("sales_executive", "Nhan vien Kinh doanh", "Kinh doanh", "business", "fas fa-handshake", 11);
        saveRole("account_manager", "Account Manager", "Kinh doanh", "business", "fas fa-user-tie", 12);
        saveRole("business_development", "Business Development", "Kinh doanh", "business", "fas fa-chart-line", 13);

        // Marketing
        saveRole("digital_marketing", "Digital Marketing", "Marketing", "marketing", "fas fa-bullhorn", 14);
        saveRole("content_creator", "Content Creator", "Marketing", "marketing", "fas fa-pen-nib", 15);
        saveRole("seo_specialist", "SEO Specialist", "Marketing", "marketing", "fas fa-search", 16);

        // Tai chinh / Ke toan
        saveRole("accountant", "Ke toan", "Tai chinh / Ke toan", "finance", "fas fa-calculator", 17);
        saveRole("financial_analyst", "Financial Analyst", "Tai chinh / Ke toan", "finance", "fas fa-money-bill-wave", 18);

        // Nhan su
        saveRole("hr_specialist", "Nhan vien Nhan su", "Nhan su", "hr", "fas fa-users", 19);
        saveRole("recruiter", "Recruiter", "Nhan su", "hr", "fas fa-user-plus", 20);

        // Thiet ke
        saveRole("uiux_designer", "UI/UX Designer", "Thiet ke", "design", "fas fa-paint-brush", 21);
        saveRole("graphic_designer", "Graphic Designer", "Thiet ke", "design", "fas fa-palette", 22);

        System.out.println("✅ Seeded interview roles");
    }

    private void saveRole(String key, String name, String category, String categoryKey, String icon, int order) {
        InterviewRole r = new InterviewRole();
        r.setRoleKey(key);
        r.setRoleName(name);
        r.setCategory(category);
        r.setCategoryKey(categoryKey);
        r.setIconClass(icon);
        r.setDisplayOrder(order);
        r.setActive(true);
        interviewRoleRepository.save(r);
    }

    // ─── Interview Prompt Templates ──────────────────────────────────────────────

    private void seedInterviewPromptTemplates() {
        if (interviewPromptTemplateRepository.count() > 0) return;

        // System main prompt
        savePrompt("system_main", "System Prompt chinh",
                "Ban la mot nha phong van chuyen nghiep dang phong van ung vien cho vi tri {{role}} "
                + "o cap do {{level}}. Loai phong van: {{interviewType}}.\n\n"
                + "QUY TAC:\n"
                + "1. Hoi tung cau mot, doi ung vien tra loi roi moi hoi tiep.\n"
                + "2. Bat dau bang loi chao va gioi thieu ngan gon.\n"
                + "3. Hoi 6-8 cau tuy theo cap do.\n"
                + "4. Cau hoi phai phu hop voi vi tri va cap do.\n"
                + "5. Co the hoi cau follow-up dua tren cau tra loi cua ung vien.\n"
                + "6. Giu giong dieu chuyen nghiep, than thien.\n"
                + "7. Khi ket thuc, noi cam on va thong bao se gui danh gia.",
                "Prompt he thong chinh cho AI interviewer");

        // Style prompts
        savePrompt("style_standard", "Phong cach Standard",
                "Phong van theo phong cach chuan: chuyen nghiep, hoi cau ro rang, khong gay ap luc.",
                "Phong cach phong van chuan");

        savePrompt("style_techlead", "Phong cach Tech Lead",
                "Phong van theo phong cach Tech Lead: tap trung vao system design, code quality, architecture decisions. "
                + "Hoi sau ve trade-offs, scalability, va kinh nghiem xu ly van de thuc te.",
                "Phong cach phong van tech lead");

        savePrompt("style_startup", "Phong cach Startup",
                "Phong van theo phong cach startup: nhanh, thuc te, tap trung vao kha nang tu hoc, "
                + "lam viec da nhiem, va passion. Hoi ve side projects va y tuong sang tao.",
                "Phong cach phong van startup");

        savePrompt("style_strict", "Phong cach Strict",
                "Phong van theo phong cach nghiem khac: hoi cau kho, follow-up chi tiet, "
                + "yeu cau giai thich sau ve moi cau tra loi. Danh gia khach quan va nghiem tuc.",
                "Phong cach phong van nghiem khac");

        // Level prompts
        savePrompt("level_fresher", "Prompt cap do Fresher",
                "Day la ung vien cap Fresher (0-1 nam kinh nghiem). Hoi cac cau co ban ve kien thuc nen tang, "
                + "du an hoc tap, y chi hoc hoi. Khong hoi qua sau ve system design hay architecture.",
                "Dieu chinh do kho cho fresher");

        savePrompt("level_junior", "Prompt cap do Junior",
                "Day la ung vien cap Junior (1-2 nam). Hoi ve kinh nghiem thuc te, cach xu ly bug, "
                + "lam viec nhom, va kien thuc trung binh ve framework/tool dang dung.",
                "Dieu chinh do kho cho junior");

        savePrompt("level_middle", "Prompt cap do Middle",
                "Day la ung vien cap Middle (2-4 nam). Hoi ve kha nang tu chu, thiet ke giai phap, "
                + "code review, performance optimization, va kinh nghiem mentor junior.",
                "Dieu chinh do kho cho middle");

        savePrompt("level_senior", "Prompt cap do Senior",
                "Day la ung vien cap Senior (5+ nam). Hoi ve system design, architecture decisions, "
                + "technical leadership, conflict resolution, va tam nhin dai han.",
                "Dieu chinh do kho cho senior");

        savePrompt("level_lead", "Prompt cap do Lead",
                "Day la ung vien cap Lead/Manager. Hoi ve chien luoc ky thuat, quan ly doi, "
                + "xu ly tinh huong phuc tap, hiring decisions, va OKR/KPI management.",
                "Dieu chinh do kho cho lead/manager");

        // Evaluation prompt
        savePrompt("eval_system", "Prompt danh gia tong hop",
                "Dua tren cuoc phong van tren, hay danh gia ung vien theo format JSON:\n"
                + "{\n"
                + "  \"overallScore\": <1-10>,\n"
                + "  \"scoreLabel\": \"Xuat sac\" | \"Kha tot\" | \"Trung binh\" | \"Can cai thien\",\n"
                + "  \"scoreCommunication\": <1-10>,\n"
                + "  \"scoreKnowledge\": <1-10>,\n"
                + "  \"scoreProblemSolving\": <1-10>,\n"
                + "  \"scoreAttitude\": <1-10>,\n"
                + "  \"strengths\": [\"diem manh 1\", \"diem manh 2\", ...],\n"
                + "  \"improvements\": [\"can cai thien 1\", \"can cai thien 2\", ...],\n"
                + "  \"recommendation\": \"Nhan xet tong the va loi khuyen cu the cho ung vien\"\n"
                + "}\n"
                + "Chi tra ve JSON, khong giai thich them.",
                "Prompt yeu cau AI tra ve ket qua danh gia dang JSON");

        // CV context prompt
        savePrompt("cv_context", "Prompt su dung CV",
                "Ung vien da nop CV voi noi dung sau:\n{{cvSection}}\n\n"
                + "Hay su dung thong tin trong CV de hoi cau hoi cu the hon, "
                + "vi du hoi ve du an da lam, ky nang da liet ke, hoac kinh nghiem cu the.",
                "Prompt them context tu CV cua ung vien");

        System.out.println("✅ Seeded interview prompt templates");
    }

    private void savePrompt(String key, String name, String content, String description) {
        InterviewPromptTemplate p = new InterviewPromptTemplate();
        p.setPromptKey(key);
        p.setPromptName(name);
        p.setPromptContent(content);
        p.setDescription(description);
        p.setActive(true);
        interviewPromptTemplateRepository.save(p);
    }

    // ─── Interview Question Bank ─────────────────────────────────────────────────

    private void seedInterviewQuestionBank() {
        if (interviewQuestionBankRepository.count() > 0) return;

        // ── HR Questions (chung cho moi role) ──
        saveQuestion("hr", "Gioi thieu ban than", null,
                "Hay gioi thieu ve ban than ban.",
                "Tap trung vao kinh nghiem lien quan, thanh tich noi bat, dong luc nghe nghiep.",
                "Toi co X nam kinh nghiem trong linh vuc Y, da tham gia cac du an Z...", "easy", 1);

        saveQuestion("hr", "Dong luc ung tuyen", null,
                "Tai sao ban muon lam viec tai cong ty chung toi?",
                "Nghien cuu ve cong ty, van hoa, san pham truoc khi tra loi.",
                null, "easy", 2);

        saveQuestion("hr", "Muc tieu nghe nghiep", null,
                "Muc tieu nghe nghiep cua ban trong 3-5 nam toi la gi?",
                "Cho thay su phu hop giua muc tieu ca nhan va co hoi tai cong ty.",
                null, "easy", 3);

        saveQuestion("hr", "Diem manh diem yeu", null,
                "Diem manh va diem yeu lon nhat cua ban la gi?",
                "Diem manh: cu the va co vi du. Diem yeu: trung thuc va cho thay dang cai thien.",
                null, "easy", 4);

        saveQuestion("hr", "Xu ly mau thuan", null,
                "Hay ke ve mot lan ban phai xu ly mau thuan voi dong nghiep.",
                "Su dung phuong phap STAR: Situation, Task, Action, Result.",
                null, "medium", 5);

        saveQuestion("hr", "Ap luc cong viec", null,
                "Ban xu ly ap luc va deadline gap nhu the nao?",
                "Chia se kinh nghiem cu the, cach uu tien cong viec.",
                null, "medium", 6);

        saveQuestion("hr", "Muc luong mong muon", null,
                "Muc luong mong muon cua ban la bao nhieu?",
                "Nghien cuu muc luong thi truong, co the dua ra khoang thay vi con so cu the.",
                null, "medium", 7);

        saveQuestion("hr", "Ly do nghi viec", null,
                "Tai sao ban roi cong ty cu / muon thay doi cong viec?",
                "Tap trung vao tim kiem co hoi phat trien, khong noi xau cong ty cu.",
                null, "medium", 8);

        // ── Behavioral Questions ──
        saveQuestion("behavioral", "Leadership", null,
                "Hay ke ve mot lan ban phai dan dat mot nhom de hoan thanh mot nhiem vu kho.",
                "Mo ta vai tro cu the, cach ban dong vien team, ket qua dat duoc.",
                null, "medium", 1);

        saveQuestion("behavioral", "Problem Solving", null,
                "Ke ve mot van de phuc tap nhat ban da giai quyet trong cong viec.",
                "Giai thich quy trinh phan tich, cac phuong an da xem xet, va ket qua cuoi cung.",
                null, "medium", 2);

        saveQuestion("behavioral", "Failure", null,
                "Hay ke ve mot that bai trong cong viec va ban da hoc duoc gi tu no.",
                "Trung thuc, tap trung vao bai hoc rut ra va cach ap dung sau do.",
                null, "medium", 3);

        saveQuestion("behavioral", "Teamwork", null,
                "Mo ta mot tinh huong ban phai lam viec voi nguoi kho tinh.",
                "The hien kha nang giao tiep, empathy, va tim giai phap win-win.",
                null, "medium", 4);

        saveQuestion("behavioral", "Initiative", null,
                "Hay ke ve mot lan ban chu dong de xuat va thuc hien mot y tuong moi.",
                "Mo ta y tuong, cach thuyet phuc, qua trinh thuc hien va ket qua.",
                null, "medium", 5);

        // ── Technical Questions - Frontend ──
        saveQuestion("technical", "JavaScript Core", "frontend_developer",
                "Giai thich su khac nhau giua var, let va const trong JavaScript.",
                "Noi ve scope (function vs block), hoisting, va temporal dead zone.",
                "var: function-scoped, hoisted. let: block-scoped, TDZ. const: block-scoped, khong the re-assign.", "easy", 1);

        saveQuestion("technical", "React Concepts", "frontend_developer",
                "Virtual DOM la gi? Tai sao React su dung Virtual DOM?",
                "Giai thich cach React so sanh virtual DOM trees (diffing) de toi uu re-render.",
                null, "easy", 2);

        saveQuestion("technical", "State Management", "frontend_developer",
                "So sanh Redux, Context API va Zustand. Khi nao nen dung cai nao?",
                "Noi ve do phuc tap, performance, use case phu hop.",
                null, "medium", 3);

        saveQuestion("technical", "Performance", "frontend_developer",
                "Lam the nao de toi uu performance cua mot ung dung React lon?",
                "Code splitting, lazy loading, memoization, virtualization, bundle analysis.",
                null, "hard", 4);

        // ── Technical Questions - Backend ──
        saveQuestion("technical", "REST API", "backend_developer",
                "Thiet ke REST API cho he thong quan ly don hang. Mo ta cac endpoints chinh.",
                "Noi ve HTTP methods, status codes, pagination, versioning.",
                null, "easy", 1);

        saveQuestion("technical", "Database", "backend_developer",
                "So sanh SQL va NoSQL. Khi nao nen dung cai nao?",
                "Noi ve ACID vs BASE, schema, scalability, use cases cu the.",
                null, "medium", 2);

        saveQuestion("technical", "Concurrency", "backend_developer",
                "Giai thich cac cach xu ly concurrent requests trong ung dung backend.",
                "Thread pool, async/await, message queue, database locking.",
                null, "hard", 3);

        saveQuestion("technical", "System Design", "backend_developer",
                "Thiet ke he thong URL shortener co the xu ly 1 trieu request/ngay.",
                "Noi ve hash function, database design, caching, CDN, analytics.",
                null, "hard", 4);

        // ── Technical Questions - Data Science ──
        saveQuestion("technical", "ML Basics", "data_scientist",
                "Giai thich su khac nhau giua Supervised va Unsupervised Learning.",
                "Vi du cu the: classification/regression vs clustering/dimensionality reduction.",
                null, "easy", 1);

        saveQuestion("technical", "Model Evaluation", "data_scientist",
                "Khi nao nen dung Precision, khi nao nen dung Recall? Vi du thuc te.",
                "Precision: khi false positive ton kem. Recall: khi false negative nguy hiem.",
                null, "medium", 2);

        saveQuestion("technical", "Deep Learning", "data_scientist",
                "Giai thich Transformer architecture va tai sao no hieu qua hon RNN cho NLP.",
                "Self-attention, parallelization, long-range dependencies.",
                null, "hard", 3);

        // ── Technical Questions - DevOps ──
        saveQuestion("technical", "Docker", "devops_engineer",
                "Docker container khac gi voi Virtual Machine?",
                "OS-level vs hardware-level virtualization, resource usage, startup time.",
                null, "easy", 1);

        saveQuestion("technical", "CI/CD", "devops_engineer",
                "Mo ta mot CI/CD pipeline hoan chinh tu code commit den production.",
                "Build, test, scan, staging deploy, approval, production deploy, monitoring.",
                null, "medium", 2);

        saveQuestion("technical", "Kubernetes", "devops_engineer",
                "Giai thich cach Kubernetes xu ly auto-scaling va self-healing.",
                "HPA, VPA, pod lifecycle, liveness/readiness probes, ReplicaSet.",
                null, "hard", 3);

        System.out.println("✅ Seeded interview question bank");
    }

    private void saveQuestion(String typeKey, String category, String roleKey,
            String question, String hint, String modelAnswer, String difficulty, int order) {
        InterviewQuestionBank q = new InterviewQuestionBank();
        q.setTypeKey(typeKey);
        q.setCategory(category);
        q.setRoleKey(roleKey);
        q.setQuestion(question);
        q.setHint(hint);
        q.setModelAnswer(modelAnswer);
        q.setDifficulty(difficulty);
        q.setDisplayOrder(order);
        q.setActive(true);
        interviewQuestionBankRepository.save(q);
    }

    // ─── Hero Banners ────────────────────────────────────────────────────────────

    private void seedHeroBanners() {
        if (heroBannerRepository.count() > 0) return;

        saveBanner("Tuyen dung CNTT 2026", "/images/banners/banner-it-2026.jpg",
                "/viec-lam?industry=CNTT", 1);
        saveBanner("Top Employer Q2/2026", "/images/banners/banner-top-employer.jpg",
                "/top-employers", 2);
        saveBanner("Huong dan viet CV chuyen nghiep", "/images/banners/banner-cv-guide.jpg",
                "/tao-cv-ai", 3);
        saveBanner("Luyen phong van voi AI", "/images/banners/banner-ai-interview.jpg",
                "/phong-van-ai", 4);

        System.out.println("✅ Seeded hero banners");
    }

    private void saveBanner(String name, String imageUrl, String targetUrl, int order) {
        HeroBanner b = new HeroBanner();
        b.setName(name);
        b.setImageUrl(imageUrl);
        b.setTargetUrl(targetUrl);
        b.setDisplayOrder(order);
        b.setActive(true);
        heroBannerRepository.save(b);
    }

    // ─── Top Employer Logos ──────────────────────────────────────────────────────

    private void seedTopEmployerLogos() {
        if (topEmployerLogoRepository.count() > 0) return;

        saveLogo("FPT Software", "/images/employers/fpt-logo.png", "https://www.fpt-software.com", 1);
        saveLogo("VNG Corporation", "/images/employers/vng-logo.png", "https://www.vng.com.vn", 2);
        saveLogo("Tiki", "/images/employers/tiki-logo.png", "https://tiki.vn", 3);
        saveLogo("Vingroup", "/images/employers/vingroup-logo.png", "https://vingroup.net", 4);
        saveLogo("MoMo", "/images/employers/momo-logo.png", "https://momo.vn", 5);
        saveLogo("Shopee", "/images/employers/shopee-logo.png", "https://shopee.vn", 6);
        saveLogo("VietcomBank", "/images/employers/vcb-logo.png", "https://vietcombank.com.vn", 7);
        saveLogo("Samsung Vietnam", "/images/employers/samsung-logo.png", "https://samsung.com/vn", 8);
        saveLogo("Grab Vietnam", "/images/employers/grab-logo.png", "https://grab.com/vn", 9);
        saveLogo("VNPT", "/images/employers/vnpt-logo.png", "https://vnpt.com.vn", 10);

        System.out.println("✅ Seeded top employer logos");
    }

    private void saveLogo(String name, String imageUrl, String targetUrl, int order) {
        TopEmployerLogo l = new TopEmployerLogo();
        l.setName(name);
        l.setImageUrl(imageUrl);
        l.setTargetUrl(targetUrl);
        l.setDisplayOrder(order);
        l.setActive(true);
        topEmployerLogoRepository.save(l);
    }
}
