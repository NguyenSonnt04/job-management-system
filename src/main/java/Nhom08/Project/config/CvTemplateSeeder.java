package Nhom08.Project.config;

import Nhom08.Project.entity.CvTemplate;
import Nhom08.Project.repository.CvTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds CV templates on server startup using upsert logic.
 * Uses template name as unique key:
 *   - INSERT if name not found in DB
 *   - UPDATE all fields if name already exists (so code changes are applied on restart)
 */
@Component
public class CvTemplateSeeder implements ApplicationRunner {

    @Autowired
    private CvTemplateRepository cvTemplateRepository;

    @Override
    public void run(ApplicationArguments args) {
        // === NGÀNH NGHỀ ===
        seedTemplate(buildItTemplate());
        seedTemplate(buildMarketingTemplate());
        seedTemplate(buildFinanceTemplate());
        seedTemplate(buildDesignTemplate());
        seedTemplate(buildHealthcareTemplate());
        seedTemplate(buildEducationTemplate());
        seedTemplate(buildLawTemplate());
        seedTemplate(buildHrTemplate());
        seedTemplate(buildSalesTemplate());
        seedTemplate(buildHospitalityTemplate());

        // === CẤP ĐỘ NGHỀ NGHIỆP ===
        seedTemplate(buildFresherTemplate());
        seedTemplate(buildInternTemplate());
        seedTemplate(buildSeniorItTemplate());
        seedTemplate(buildManagerTemplate());
        seedTemplate(buildFreelancerTemplate());
    }

    /**
     * Upsert: insert if template name not found, otherwise update all fields.
     * This ensures seeder changes are reflected in DB on every restart.
     */
    private void seedTemplate(CvTemplate tpl) {
        cvTemplateRepository.findByName(tpl.getName()).ifPresentOrElse(
            existing -> {
                existing.setDescription(tpl.getDescription());
                existing.setPreviewColor(tpl.getPreviewColor());
                existing.setBadgeLabel(tpl.getBadgeLabel());
                existing.setBadgeBgColor(tpl.getBadgeBgColor());
                existing.setBadgeTextColor(tpl.getBadgeTextColor());
                existing.setCategory(tpl.getCategory());
                existing.setStyleTag(tpl.getStyleTag());
                existing.setSortOrder(tpl.getSortOrder());
                existing.setTemplateContent(tpl.getTemplateContent());
                // Note: we do NOT override `active` — respect admin's toggle
                cvTemplateRepository.save(existing);
            },
            () -> cvTemplateRepository.save(tpl)
        );
    }

    // ── 1. Công nghệ & IT ──────────────────────────────────────────────────────
    private CvTemplate buildItTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Software Engineer Pro");
        t.setDescription("Mẫu CV chuyên nghiệp dành cho lập trình viên, kỹ sư phần mềm. Tối ưu ATS, nổi bật kỹ năng kỹ thuật và dự án.");
        t.setPreviewColor("#1e3a5f");
        t.setBadgeLabel("ATS-Friendly");
        t.setBadgeBgColor("#dbeafe");
        t.setBadgeTextColor("#1d4ed8");
        t.setCategory("Công nghệ & IT");
        t.setStyleTag("professional");
        t.setSortOrder(1);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "NGUYỄN MINH KHOA",
              "subtitle": "Full-Stack Software Engineer",
              "email": "minhkhoa.dev@gmail.com",
              "phone": "0912 345 678",
              "address": "Quận 1, TP. Hồ Chí Minh",
              "summary": "Kỹ sư phần mềm với 3 năm kinh nghiệm phát triển ứng dụng web full-stack sử dụng Java Spring Boot và ReactJS. Đam mê xây dựng hệ thống hiệu suất cao, sạch code và có khả năng mở rộng. Có kinh nghiệm làm việc trong môi trường Agile/Scrum.",
              "education": [
                {
                  "school": "ĐẠI HỌC BÁCH KHOA TP. HCM",
                  "location": "TP. Hồ Chí Minh",
                  "degree": "Kỹ sư Công nghệ Thông tin",
                  "period": "2018 – 2022",
                  "details": [
                    "GPA: 3.6/4.0 — Tốt nghiệp loại Giỏi",
                    "Đề tài tốt nghiệp: Hệ thống khuyến nghị việc làm sử dụng Machine Learning — đạt 9.5/10",
                    "Thành viên CLB Lập trình Bách Khoa — giải Nhì cuộc thi Hackathon 2021"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "FPT SOFTWARE",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Software Engineer",
                  "period": "Tháng 7, 2022 – Hiện tại",
                  "details": [
                    "Phát triển API RESTful cho hệ thống quản lý nhân sự phục vụ 10.000+ người dùng sử dụng Java Spring Boot",
                    "Tối ưu hóa truy vấn SQL và indexing, giảm 40% thời gian phản hồi của hệ thống",
                    "Xây dựng CI/CD pipeline với GitHub Actions và Docker, giảm 60% thời gian deployment",
                    "Tham gia Code Review và mentor 3 thực tập sinh về best practices trong lập trình"
                  ]
                },
                {
                  "company": "STARTUP TECH VN",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Backend Developer Intern",
                  "period": "Tháng 1, 2022 – Tháng 6, 2022",
                  "details": [
                    "Xây dựng module thanh toán tích hợp VNPay và Momo cho ứng dụng thương mại điện tử",
                    "Thiết kế database schema cho hệ thống quản lý đơn hàng với 15+ bảng quan hệ"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "AI-Powered Job Matching System",
                  "period": "Tháng 9, 2023 – Tháng 12, 2023",
                  "tech": "Java Spring Boot, Python Flask, MySQL, React, Docker",
                  "github": "https://github.com/minhkhoa/job-matching",
                  "details": [
                    "Xây dựng hệ thống gợi ý việc làm sử dụng thuật toán Cosine Similarity để khớp CV với JD",
                    "Triển khai RESTful APIs phục vụ 500+ requests/phút với độ trễ < 200ms",
                    "Tích hợp Google OAuth2 và JWT authentication cho bảo mật đa lớp"
                  ]
                }
              ],
              "skills": [
                { "category": "Backend", "items": ["Java", "Spring Boot", "Spring Security", "JPA/Hibernate", "Node.js"] },
                { "category": "Frontend", "items": ["ReactJS", "TypeScript", "HTML/CSS", "TailwindCSS"] },
                { "category": "Database", "items": ["MySQL", "PostgreSQL", "Redis", "MongoDB"] },
                { "category": "DevOps & Tools", "items": ["Docker", "Git", "GitHub Actions", "Postman", "IntelliJ IDEA"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (TOEIC 800)", "Tiếng Việt (Bản ngữ)"] }
              ]
            }
            """);
        return t;
    }

    // ── 2. Marketing & PR ─────────────────────────────────────────────────────
    private CvTemplate buildMarketingTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Marketing Specialist");
        t.setDescription("Mẫu CV năng động dành cho chuyên viên Marketing, Content Creator, Digital Marketing. Nổi bật số liệu KPIs.");
        t.setPreviewColor("#7c3aed");
        t.setBadgeLabel("Top Rated");
        t.setBadgeBgColor("#ede9fe");
        t.setBadgeTextColor("#6d28d9");
        t.setCategory("Marketing & PR");
        t.setStyleTag("creative");
        t.setSortOrder(2);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "TRẦN THU TRANG",
              "subtitle": "Digital Marketing Specialist",
              "email": "thutrang.mkt@gmail.com",
              "phone": "0987 654 321",
              "address": "Đống Đa, Hà Nội",
              "summary": "Chuyên viên Digital Marketing với 4 năm kinh nghiệm quản lý các chiến dịch quảng cáo đa kênh (Facebook Ads, Google Ads, TikTok). Đã giúp 10+ thương hiệu tăng doanh thu trực tuyến trung bình 35% trong 6 tháng. Thành thạo phân tích dữ liệu và tối ưu hóa ROI.",
              "education": [
                {
                  "school": "ĐẠI HỌC NGOẠI THƯƠNG HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Kinh doanh Quốc tế — Chuyên ngành Marketing",
                  "period": "2017 – 2021",
                  "details": [
                    "GPA: 3.5/4.0 — Học bổng Khuyến khích Học tập 4 học kỳ liên tiếp",
                    "Trưởng ban Marketing CLB Kinh doanh FTU — tổ chức 5 sự kiện lớn với 500+ người tham dự"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "VCCORP MEDIA",
                  "location": "Hà Nội",
                  "role": "Senior Digital Marketing Specialist",
                  "period": "Tháng 3, 2022 – Hiện tại",
                  "details": [
                    "Quản lý ngân sách quảng cáo 500 triệu đồng/tháng trên Facebook Ads và Google Ads",
                    "Tăng 42% lượng traffic organic thông qua chiến lược SEO và Content Marketing toàn diện",
                    "Xây dựng hệ thống báo cáo KPIs tự động bằng Google Data Studio, tiết kiệm 10 giờ/tuần",
                    "Dẫn dắt team 5 người thực hiện chiến dịch ra mắt sản phẩm đạt 200% target"
                  ]
                },
                {
                  "company": "OGILVY VIETNAM",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Marketing Executive",
                  "period": "Tháng 6, 2021 – Tháng 2, 2022",
                  "details": [
                    "Thực hiện chiến dịch TikTok cho thương hiệu FMCG đạt 5 triệu lượt xem trong 2 tuần",
                    "Phối hợp với team sáng tạo sản xuất 30+ nội dung social media mỗi tháng"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Chiến dịch Ra mắt Thương hiệu F&B Quốc gia",
                  "period": "Tháng 6, 2023 – Tháng 8, 2023",
                  "tech": "Facebook Ads, Google Ads, TikTok Ads, Google Analytics 4",
                  "github": "",
                  "details": [
                    "Lên kế hoạch và triển khai chiến dịch 360 độ cho thương hiệu đồ uống mới ra mắt tại 10 tỉnh thành",
                    "Đạt 1.2 triệu lượt tiếp cận, 8% CTR (gấp 4 lần benchmark ngành), ROAS đạt 4.5x"
                  ]
                }
              ],
              "skills": [
                { "category": "Digital Marketing", "items": ["Facebook Ads", "Google Ads", "TikTok Ads", "SEO/SEM", "Email Marketing"] },
                { "category": "Analytics & Tools", "items": ["Google Analytics 4", "Google Data Studio", "Hotjar", "Semrush", "HubSpot"] },
                { "category": "Content & Creative", "items": ["Content Strategy", "Copywriting", "Canva", "Adobe Premiere", "Figma"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.0)", "Tiếng Việt (Bản ngữ)"] }
              ]
            }
            """);
        return t;
    }

    // ── 3. Tài chính & Kế toán ────────────────────────────────────────────────
    private CvTemplate buildFinanceTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Finance & Accounting Pro");
        t.setDescription("Mẫu CV trang trọng dành cho chuyên viên Tài chính, Kế toán, Kiểm toán. Phù hợp Big4 và các tổ chức tài chính lớn.");
        t.setPreviewColor("#1a4731");
        t.setBadgeLabel("Premium");
        t.setBadgeBgColor("#d1fae5");
        t.setBadgeTextColor("#065f46");
        t.setCategory("Tài chính & Kinh doanh");
        t.setStyleTag("professional");
        t.setSortOrder(3);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "LÊ HOÀNG PHÚC",
              "subtitle": "Chuyên viên Tài chính Doanh nghiệp",
              "email": "hoangphuc.finance@gmail.com",
              "phone": "0938 111 222",
              "address": "Bình Thạnh, TP. Hồ Chí Minh",
              "summary": "Chuyên viên Tài chính với 5 năm kinh nghiệm trong phân tích tài chính, lập kế hoạch ngân sách và báo cáo tài chính theo chuẩn IFRS/VAS. Từng làm việc tại Big4 (Deloitte) và các doanh nghiệp FDI. Thành thạo mô hình tài chính, định giá doanh nghiệp và quản lý rủi ro.",
              "education": [
                {
                  "school": "ĐẠI HỌC KINH TẾ TP. HCM (UEH)",
                  "location": "TP. Hồ Chí Minh",
                  "degree": "Cử nhân Tài chính – Ngân hàng (Chương trình Chất lượng cao)",
                  "period": "2016 – 2020",
                  "details": [
                    "GPA: 3.7/4.0 — Tốt nghiệp loại Giỏi — Top 5% khóa",
                    "Nhận học bổng toàn phần từ Quỹ Học bổng UEH (2018 – 2020)",
                    "Chứng chỉ CFA Level 1 (2022) — CMA (2023)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "DELOITTE VIETNAM",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Financial Advisory Consultant",
                  "period": "Tháng 8, 2021 – Hiện tại",
                  "details": [
                    "Tham gia 12+ dự án tư vấn tài chính cho doanh nghiệp FDI trong lĩnh vực bất động sản và sản xuất",
                    "Xây dựng mô hình tài chính DCF và comparable analysis để định giá doanh nghiệp trị giá 50-500 triệu USD",
                    "Chuẩn bị báo cáo tài chính theo chuẩn IFRS 16, IFRS 9 cho doanh nghiệp niêm yết HoSE",
                    "Phân tích rủi ro và đề xuất cơ cấu tài chính tối ưu, giúp tiết kiệm 15% chi phí lãi vay"
                  ]
                },
                {
                  "company": "VINGROUP",
                  "location": "Hà Nội",
                  "role": "Financial Analyst",
                  "period": "Tháng 6, 2020 – Tháng 7, 2021",
                  "details": [
                    "Theo dõi và phân tích hiệu quả đầu tư của danh mục dự án BĐS tổng trị giá 2.000 tỷ VNĐ",
                    "Lập báo cáo tài chính hàng tháng và đề xuất phương án cải thiện dòng tiền"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Dự án M&A Công ty FMCG Việt Nam",
                  "period": "Tháng 3, 2023 – Tháng 6, 2023",
                  "tech": "Excel (Advanced), Power BI, Bloomberg Terminal",
                  "github": "",
                  "details": [
                    "Tham gia nhóm tư vấn thương vụ mua bán sáp nhập trị giá 120 triệu USD",
                    "Thực hiện due diligence tài chính, phân tích 5 năm báo cáo tài chính và dự phóng 10 năm",
                    "Xây dựng mô hình LBO và đề xuất cơ cấu giao dịch tối ưu"
                  ]
                }
              ],
              "skills": [
                { "category": "Tài chính & Kế toán", "items": ["Financial Modeling", "DCF Valuation", "M&A Advisory", "IFRS/VAS", "Budgeting & Forecasting"] },
                { "category": "Công cụ phân tích", "items": ["Excel (Advanced)", "Power BI", "SAP", "Bloomberg Terminal", "Python (Pandas)"] },
                { "category": "Chứng chỉ", "items": ["CFA Level 1", "CMA", "Chứng chỉ Kế toán CPA Vietnam"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.5 — Làm việc chuyên sâu)", "Tiếng Việt (Bản ngữ)"] }
              ]
            }
            """);
        return t;
    }

    // ── 4. Thiết kế & Sáng tạo ───────────────────────────────────────────────
    private CvTemplate buildDesignTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Creative Designer");
        t.setDescription("Mẫu CV sáng tạo dành cho UI/UX Designer, Graphic Designer. Kết hợp thông tin cá nhân và portfolio ấn tượng.");
        t.setPreviewColor("#c2410c");
        t.setBadgeLabel("Creative");
        t.setBadgeBgColor("#ffedd5");
        t.setBadgeTextColor("#c2410c");
        t.setCategory("Thiết kế & Sáng tạo");
        t.setStyleTag("creative");
        t.setSortOrder(4);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "PHẠM QUỲNH ANH",
              "subtitle": "Senior UI/UX Designer",
              "email": "quynhanh.design@gmail.com",
              "phone": "0905 789 012",
              "address": "Quận 3, TP. Hồ Chí Minh",
              "summary": "Senior UI/UX Designer với 5 năm kinh nghiệm thiết kế sản phẩm kỹ thuật số cho các công ty công nghệ và startup. Thành thạo quy trình Design Thinking, nghiên cứu người dùng và xây dựng Design System. Portfolio gồm 20+ sản phẩm đã launch với hàng triệu người dùng.",
              "education": [
                {
                  "school": "ĐẠI HỌC MỸ THUẬT CÔNG NGHIỆP HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Thiết kế Truyền thông Đa phương tiện",
                  "period": "2016 – 2020",
                  "details": [
                    "Tốt nghiệp Xuất sắc — Đồ án tốt nghiệp được triển lãm quốc gia",
                    "Học bổng trao đổi sinh viên tại Học viện Thiết kế Politecnico di Milano, Ý (2019)",
                    "Giải Nhất cuộc thi thiết kế Young Lotus 2019"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "TIKI CORPORATION",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Senior UX Designer",
                  "period": "Tháng 4, 2021 – Hiện tại",
                  "details": [
                    "Dẫn dắt redesign toàn diện luồng Checkout của Tiki, giảm tỷ lệ bỏ giỏ hàng 22%",
                    "Xây dựng và duy trì Tiki Design System gồm 200+ components phục vụ 30 product team",
                    "Thực hiện User Research với 50+ người dùng mỗi quý để đưa ra product insights",
                    "Cố vấn cho 4 junior designer về quy trình UX và presentation skills"
                  ]
                },
                {
                  "company": "BASE.VN",
                  "location": "Hà Nội",
                  "role": "UI/UX Designer",
                  "period": "Tháng 6, 2020 – Tháng 3, 2021",
                  "details": [
                    "Thiết kế toàn bộ UI cho nền tảng quản lý doanh nghiệp phục vụ 5.000+ doanh nghiệp",
                    "Tăng System Usability Score từ 68 lên 84 sau 6 tháng cải tiến liên tục"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Redesign App Giao đồ ăn SuperShip",
                  "period": "Tháng 9, 2022 – Tháng 11, 2022",
                  "tech": "Figma, Maze, Hotjar, Principle",
                  "github": "https://www.behance.net/quynhanh-design",
                  "details": [
                    "Redesign toàn bộ trải nghiệm đặt đồ ăn từ onboarding đến thanh toán",
                    "Tăng Conversion Rate từ 18% lên 31% sau khi launch phiên bản mới",
                    "Được featured trên Dribbble với 2.000+ lượt thích"
                  ]
                }
              ],
              "skills": [
                { "category": "Design Tools", "items": ["Figma", "Adobe XD", "Sketch", "Adobe Illustrator", "Photoshop"] },
                { "category": "UX Methods", "items": ["Design Thinking", "User Research", "Usability Testing", "A/B Testing", "Information Architecture"] },
                { "category": "Prototyping", "items": ["Principle", "Framer", "InVision", "Marvel", "Maze"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.0)", "Tiếng Ý (Cơ bản)", "Tiếng Việt (Bản ngữ)"] }
              ]
            }
            """);
        return t;
    }

    // ── 5. Sinh viên mới tốt nghiệp (Fresher) ────────────────────────────────
    private CvTemplate buildFresherTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Fresh Graduate");
        t.setDescription("Mẫu CV dành cho sinh viên mới tốt nghiệp, fresher chưa có nhiều kinh nghiệm. Nổi bật thành tích học tập và dự án.");
        t.setPreviewColor("#0e7490");
        t.setBadgeLabel("Cho Fresher");
        t.setBadgeBgColor("#cffafe");
        t.setBadgeTextColor("#0e7490");
        t.setCategory("Công nghệ & IT");
        t.setStyleTag("minimalist");
        t.setSortOrder(5);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "VŨ THANH BÌNH",
              "subtitle": "Backend Developer — Java Spring Boot",
              "email": "thanhbinh.dev@gmail.com",
              "phone": "0901 234 567",
              "address": "Cầu Giấy, Hà Nội",
              "summary": "Sinh viên CNTT mới tốt nghiệp (GPA 3.8/4.0) với nền tảng vững chắc về Java và Spring Boot. Đã hoàn thành 2 dự án thực tế trong quá trình học và 3 tháng thực tập tại công ty phần mềm. Đang tìm kiếm vị trí Junior Backend Developer để phát triển kỹ năng trong môi trường chuyên nghiệp.",
              "education": [
                {
                  "school": "ĐẠI HỌC CÔNG NGHỆ — ĐẠI HỌC QUỐC GIA HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Kỹ sư Công nghệ Thông tin",
                  "period": "2020 – 2024",
                  "details": [
                    "GPA: 3.8/4.0 — Tốt nghiệp loại Giỏi — Top 3% khóa K65",
                    "Học bổng Khuyến khích Học tập 6/8 học kỳ",
                    "Đồ án tốt nghiệp: Hệ thống tuyển dụng thông minh tích hợp AI — đạt 9.2/10",
                    "Thành viên tích cực CLB Lập trình UET-ACM — giải Ba ICPC Khu vực Hà Nội 2023"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "VIETTEL SOLUTIONS",
                  "location": "Hà Nội",
                  "role": "Backend Developer Intern",
                  "period": "Tháng 6, 2023 – Tháng 8, 2023",
                  "details": [
                    "Phát triển module quản lý khách hàng cho hệ thống CRM nội bộ bằng Java Spring Boot",
                    "Viết unit test với JUnit 5 và Mockito, đạt coverage 85%",
                    "Tham gia sprint planning và daily stand-up trong môi trường Agile Scrum"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Hệ thống Quản lý Thư viện Trực tuyến",
                  "period": "Tháng 9, 2023 – Tháng 12, 2023",
                  "tech": "Java Spring Boot, MySQL, React, JWT, Docker",
                  "github": "https://github.com/thanhbinh/library-management",
                  "details": [
                    "Thiết kế và xây dựng hệ thống quản lý thư viện với 15+ chức năng đầy đủ",
                    "Xây dựng RESTful APIs với Spring Boot, tích hợp Spring Security và JWT authentication",
                    "Triển khai trên VPS với Docker Compose, uptime đạt 99.9% trong 3 tháng vận hành"
                  ]
                },
                {
                  "name": "App Chia sẻ Lịch học Nhóm",
                  "period": "Tháng 3, 2023 – Tháng 5, 2023",
                  "tech": "Node.js, Express, MongoDB, Socket.io, React Native",
                  "github": "https://github.com/thanhbinh/study-schedule-app",
                  "details": [
                    "Xây dựng ứng dụng mobile cho phép sinh viên đồng bộ lịch học và nhắc nhở theo thời gian thực",
                    "100+ sinh viên trong trường đang sử dụng, rating 4.7/5 trên Google Play"
                  ]
                }
              ],
              "skills": [
                { "category": "Lập trình", "items": ["Java (OOP, Design Patterns)", "Spring Boot", "Spring MVC", "Spring Security", "Python"] },
                { "category": "Database", "items": ["MySQL", "PostgreSQL", "MongoDB", "Redis"] },
                { "category": "Tools & DevOps", "items": ["Git/GitHub", "Docker", "Maven", "Postman", "Linux"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (TOEIC 750 — đọc tài liệu kỹ thuật tốt)", "Tiếng Việt (Bản ngữ)"] }
              ]
            }
            """);
        return t;
    }

    // ── 6. Y tế & Sức khỏe ──────────────────────────────────────────────────
    private CvTemplate buildHealthcareTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Healthcare Professional");
        t.setDescription("Mẫu CV chuyên nghiệp dành cho Bác sĩ, Điều dưỡng, Dược sĩ và các ngành Y tế.");
        t.setPreviewColor("#0f766e");
        t.setBadgeLabel("Y tế");
        t.setBadgeBgColor("#ccfbf1");
        t.setBadgeTextColor("#0f766e");
        t.setCategory("Y tế & Sức khỏe");
        t.setStyleTag("professional");
        t.setSortOrder(6);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "BS. NGUYỄN THỊ MAI ANH",
              "subtitle": "Bác sĩ Nội khoa – Bệnh viện Bạch Mai",
              "email": "maianh.bsnt@gmail.com",
              "phone": "0918 222 333",
              "address": "Đống Đa, Hà Nội",
              "linkedin": "",
              "portfolio": "",
              "summary": "Bác sĩ Nội khoa với 5 năm kinh nghiệm lâm sàng tại bệnh viện tuyến đầu. Tốt nghiệp loại Giỏi Đại học Y Hà Nội, có chứng chỉ hành nghề và thành thạo các kỹ thuật chẩn đoán hiện đại. Định hướng chuyên sâu về Tim mạch.",
              "education": [
                { "school": "ĐẠI HỌC Y HÀ NỘI", "location": "Hà Nội", "degree": "Bác sĩ Đa khoa (Y6)", "period": "2014 – 2020", "details": ["Tốt nghiệp loại Giỏi – GPA 3.65/4.0", "Luận văn tốt nghiệp: Can thiệp động mạch vành qua da – đạt 9.0/10"] }
              ],
              "experience": [
                { "company": "BỆNH VIỆN BẠCH MAI", "location": "Hà Nội", "role": "Bác sĩ Nội trú Nội khoa", "period": "2020 – Hiện tại", "details": ["Thăm khám và điều trị 30–40 bệnh nhân/ngày tại khoa Nội Tim mạch", "Tham gia 200+ ca can thiệp tim mạch dưới hướng dẫn của GS đầu ngành", "Hướng dẫn 5 sinh viên Y6 thực tập lâm sàng"] }
              ],
              "projects": [],
              "skills": [
                { "category": "Lâm sàng", "items": ["Chẩn đoán Nội khoa", "Siêu âm tim cơ bản", "Điện tâm đồ", "Cấp cứu nội khoa"] },
                { "category": "Chứng chỉ", "items": ["Chứng chỉ hành nghề khám chữa bệnh", "ACLS – BLS (AHA 2022)"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh Y khoa (B2)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "Chứng chỉ hành nghề khám chữa bệnh", "issuer": "Bộ Y tế Việt Nam", "year": "2021" }],
              "awards": [{ "name": "Bác sĩ Nội trú Xuất sắc", "year": "2022" }],
              "activities": []
            }
            """);
        return t;
    }

    // ── 7. Giáo dục & Hàn lâm ───────────────────────────────────────────────
    private CvTemplate buildEducationTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Educator & Academic");
        t.setDescription("Mẫu CV dành cho Giáo viên, Giảng viên Đại học, Nhà nghiên cứu. Làm nổi bật công trình nghiên cứu và kinh nghiệm giảng dạy.");
        t.setPreviewColor("#1e40af");
        t.setBadgeLabel("Giáo dục");
        t.setBadgeBgColor("#dbeafe");
        t.setBadgeTextColor("#1e40af");
        t.setCategory("Giáo dục & Hàn lâm");
        t.setStyleTag("professional");
        t.setSortOrder(7);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "TS. PHẠM VĂN HƯNG",
              "subtitle": "Giảng viên Đại học – Khoa Khoa học Máy tính",
              "email": "pvhung@hust.edu.vn",
              "phone": "0912 888 777",
              "address": "Hai Bà Trưng, Hà Nội",
              "linkedin": "linkedin.com/in/pvhung-edu",
              "portfolio": "scholar.google.com/pvhung",
              "summary": "Tiến sĩ Khoa học Máy tính với 8 năm kinh nghiệm giảng dạy tại Đại học Bách Khoa Hà Nội. Tác giả 12 bài báo ISI/Scopus trong lĩnh vực Machine Learning. Hướng dẫn 3 NCS và 15 học viên Thạc sĩ.",
              "education": [
                { "school": "ĐẠI HỌC BÁCH KHOA HÀ NỘI", "location": "Hà Nội", "degree": "Tiến sĩ Khoa học Máy tính", "period": "2012 – 2017", "details": ["Học bổng NAFOSTED", "Luận án: Deep Learning for Vietnamese NLP"] },
                { "school": "ĐẠI HỌC BÁCH KHOA HÀ NỘI", "location": "Hà Nội", "degree": "Thạc sĩ Công nghệ Thông tin", "period": "2010 – 2012", "details": ["GPA: 3.9/4.0 – Tốt nghiệp loại Xuất sắc"] }
              ],
              "experience": [
                { "company": "ĐẠI HỌC BÁCH KHOA HÀ NỘI", "location": "Hà Nội", "role": "Giảng viên – Khoa KHMT", "period": "2017 – Hiện tại", "details": ["Giảng dạy các môn: Machine Learning, Deep Learning, Cơ sở Dữ liệu (300+ sinh viên/năm)", "Chủ nhiệm đề tài NAFOSTED 2021–2023 (500 triệu VNĐ)", "Phó Chủ nhiệm Bộ môn từ năm 2022"] }
              ],
              "projects": [
                { "name": "Hệ thống phân tích cảm xúc Tiếng Việt (ViSentiment)", "period": "2021 – 2023", "tech": "Python, PyTorch, BERT, FastAPI", "github": "https://github.com/pvhung/visentiment", "details": ["Xây dựng mô hình đạt F1-score 91.3% trên bộ dữ liệu VLSP 2016", "Được tích hợp vào 3 sản phẩm thương mại"] }
              ],
              "skills": [
                { "category": "Nghiên cứu", "items": ["Machine Learning", "NLP", "Computer Vision", "LaTeX"] },
                { "category": "Lập trình", "items": ["Python", "MATLAB", "R"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (C1 – Academic)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [],
              "awards": [{ "name": "Chiến sĩ thi đua cấp Bộ", "year": "2023" }, { "name": "Giải thưởng Bài báo xuất sắc – Hội nghị KH Quốc gia", "year": "2022" }],
              "activities": [{ "name": "Phản biện khoa học tại các hội nghị AAAI, ACL, EMNLP", "role": "Reviewer", "period": "2019 – Hiện tại", "details": [] }]
            }
            """);
        return t;
    }

    // ── 8. Luật ─────────────────────────────────────────────────────────────
    private CvTemplate buildLawTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Legal Professional");
        t.setDescription("Mẫu CV trang trọng dành cho Luật sư, Chuyên viên Pháp chế, Thẩm phán. Nhấn mạnh chứng chỉ hành nghề và lĩnh vực chuyên môn.");
        t.setPreviewColor("#1c1917");
        t.setBadgeLabel("Pháp lý");
        t.setBadgeBgColor("#f5f5f4");
        t.setBadgeTextColor("#1c1917");
        t.setCategory("Luật & Pháp chế");
        t.setStyleTag("professional");
        t.setSortOrder(8);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "LUẬT SƯ TRẦN ĐÌNH DŨNG",
              "subtitle": "Luật sư Doanh nghiệp – Chuyên gia M&A & Đầu tư nước ngoài",
              "email": "ls.dinhdung@chambers.vn",
              "phone": "0904 555 666",
              "address": "Ba Đình, Hà Nội",
              "linkedin": "linkedin.com/in/trandinhdung-lawyer",
              "portfolio": "",
              "summary": "Luật sư với 8 năm kinh nghiệm tư vấn pháp lý doanh nghiệp, chuyên sâu về M&A, đầu tư nước ngoài và tranh chấp thương mại. Thành viên Đoàn Luật sư TP. Hà Nội. Đã tư vấn cho 50+ thương vụ M&A và FDI tổng trị giá hơn 500 triệu USD.",
              "education": [
                { "school": "ĐẠI HỌC LUẬT HÀ NỘI", "location": "Hà Nội", "degree": "Cử nhân Luật – Chuyên ngành Luật Kinh tế", "period": "2010 – 2014", "details": ["Tốt nghiệp loại Giỏi – Top 5% khóa", "Học bổng toàn phần 4 năm"] }
              ],
              "experience": [
                { "company": "VILAF LAW FIRM", "location": "Hà Nội", "role": "Senior Associate – M&A & Corporate", "period": "2019 – Hiện tại", "details": ["Tư vấn pháp lý cho 30+ thương vụ M&A xuyên biên giới trong lĩnh vực bất động sản, tài chính và công nghệ", "Soạn thảo và đàm phán hợp đồng đầu tư, SPA, SHA cho các thương vụ trị giá 10–200 triệu USD", "Dẫn dắt team 4 luật sư junior trong các dự án tư vấn lớn"] },
                { "company": "BAKER MCKENZIE VIETNAM", "location": "TP. Hồ Chí Minh", "role": "Associate Lawyer", "period": "2014 – 2019", "details": ["Tư vấn cấp phép đầu tư FDI cho 20+ công ty đa quốc gia tại Việt Nam", "Xử lý tranh chấp thương mại quốc tế theo quy tắc ICC Arbitration"] }
              ],
              "projects": [],
              "skills": [
                { "category": "Lĩnh vực chuyên môn", "items": ["M&A & Corporate Finance", "FDI & Licensing", "Commercial Disputes", "Real Estate Law", "Employment Law"] },
                { "category": "Kỹ năng", "items": ["Đàm phán hợp đồng", "Legal Due Diligence", "Soạn thảo văn bản pháp lý song ngữ"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (C2 – Fluent, làm việc chuyên sâu)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "Chứng chỉ Luật sư hành nghề", "issuer": "Bộ Tư pháp Việt Nam", "year": "2015" }, { "name": "ICC International Arbitration Certificate", "issuer": "ICC Institute", "year": "2020" }],
              "awards": [{ "name": "Luật sư Nổi bật – Legal 500 Asia Pacific", "year": "2023" }],
              "activities": []
            }
            """);
        return t;
    }

    // ── 9. Nhân sự (HR) ─────────────────────────────────────────────────────
    private CvTemplate buildHrTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Human Resources Manager");
        t.setDescription("Mẫu CV dành cho chuyên viên Nhân sự, HRBP, Trưởng phòng Nhân sự. Nổi bật kỹ năng quản lý con người và chiến lược HR.");
        t.setPreviewColor("#9d174d");
        t.setBadgeLabel("HR");
        t.setBadgeBgColor("#fce7f3");
        t.setBadgeTextColor("#9d174d");
        t.setCategory("Nhân sự & Hành chính");
        t.setStyleTag("professional");
        t.setSortOrder(9);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "NGUYỄN HOÀNG LAN",
              "subtitle": "HR Business Partner – Chuyên gia Quản trị Nhân tài",
              "email": "hoanglan.hrbp@gmail.com",
              "phone": "0933 444 555",
              "address": "Tân Bình, TP. Hồ Chí Minh",
              "linkedin": "linkedin.com/in/nghoanglan-hr",
              "portfolio": "",
              "summary": "HR Business Partner với 7 năm kinh nghiệm tại các tập đoàn đa quốc gia. Chuyên sâu về tuyển dụng hàng loạt, xây dựng hệ thống đánh giá KPI và phát triển văn hóa doanh nghiệp. Đã tuyển dụng thành công 500+ vị trí từ nhân viên đến C-level.",
              "education": [
                { "school": "ĐẠI HỌC KINH TẾ TP. HCM (UEH)", "location": "TP. HCM", "degree": "Cử nhân Quản trị Nhân lực", "period": "2013 – 2017", "details": ["GPA: 3.6/4.0 – Tốt nghiệp loại Giỏi"] }
              ],
              "experience": [
                { "company": "UNILEVER VIETNAM", "location": "TP. HCM", "role": "HR Business Partner – Supply Chain", "period": "2021 – Hiện tại", "details": ["HRBP cho 800+ nhân viên tại 3 nhà máy sản xuất", "Giảm tỷ lệ nghỉ việc từ 18% xuống 11% qua chương trình Employee Engagement", "Chủ trì triển khai hệ thống đánh giá hiệu suất OKR cho toàn bộ khối Supply Chain"] },
                { "company": "KPMG VIETNAM", "location": "TP. HCM", "role": "Talent Acquisition Specialist", "period": "2017 – 2021", "details": ["Tuyển dụng 100+ nhân sự/năm cho các vị trí Audit, Tax, Advisory", "Xây dựng chương trình Graduate Recruitment thu hút 2.000+ ứng viên/năm"] }
              ],
              "projects": [],
              "skills": [
                { "category": "HR Chuyên môn", "items": ["Talent Acquisition", "HRBP", "OKR/KPI System", "L&D", "Compensation & Benefits"] },
                { "category": "Công cụ", "items": ["SAP SuccessFactors", "Workday", "LinkedIn Recruiter", "DISC Assessment"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.0)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "SHRM-CP (Society for Human Resource Management)", "issuer": "SHRM", "year": "2022" }],
              "awards": [{ "name": "HR Manager of the Year – Unilever Vietnam", "year": "2023" }],
              "activities": []
            }
            """);
        return t;
    }

    // ── 10. Kinh doanh & Bán hàng ────────────────────────────────────────────
    private CvTemplate buildSalesTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Sales & Business Development");
        t.setDescription("Mẫu CV dành cho Chuyên viên Bán hàng, Kinh doanh, Business Development. Nhấn mạnh doanh số và KPIs đạt được.");
        t.setPreviewColor("#b45309");
        t.setBadgeLabel("Sales");
        t.setBadgeBgColor("#fef3c7");
        t.setBadgeTextColor("#b45309");
        t.setCategory("Kinh doanh & Bán hàng");
        t.setStyleTag("professional");
        t.setSortOrder(10);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "VÕ MINH TUẤN",
              "subtitle": "Sales Manager – B2B Enterprise Solutions",
              "email": "minhTuan.sales@gmail.com",
              "phone": "0909 876 543",
              "address": "Quận 7, TP. Hồ Chí Minh",
              "linkedin": "linkedin.com/in/vominhtuan-sales",
              "portfolio": "",
              "summary": "Sales Manager với 6 năm kinh nghiệm B2B, chuyên về giải pháp phần mềm doanh nghiệp (SaaS/ERP). Đã xây dựng và quản lý team 10 Sales từ đầu, liên tục vượt chỉ tiêu doanh thu 120-150% trong 4 năm liên tiếp. Thành thạo quy trình bán hàng Solution Selling.",
              "education": [
                { "school": "ĐẠI HỌC KINH TẾ TP. HCM", "location": "TP. HCM", "degree": "Cử nhân Quản trị Kinh doanh", "period": "2014 – 2018", "details": ["GPA: 3.4/4.0"] }
              ],
              "experience": [
                { "company": "SAP VIETNAM", "location": "TP. HCM", "role": "Sales Manager – Mid-Market", "period": "2021 – Hiện tại", "details": ["Quản lý danh mục 50+ khách hàng doanh nghiệp vừa và lớn, ARR đạt 8 triệu USD/năm", "Vượt chỉ tiêu 135% trong năm 2023, đứng Top 3 toàn cầu khu vực SEA", "Xây dựng và dẫn dắt team 10 Account Executive"] },
                { "company": "FSOFT (FPT SOFTWARE)", "location": "TP. HCM", "role": "Senior Sales Executive", "period": "2018 – 2021", "details": ["Ký kết 3 hợp đồng outsourcing lớn với khách Nhật Bản tổng giá trị 2 triệu USD", "Top 1 Sales toàn công ty năm 2020"] }
              ],
              "projects": [],
              "skills": [
                { "category": "Bán hàng", "items": ["Solution Selling", "CRM (Salesforce)", "Pipeline Management", "Account Management", "Contract Negotiation"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (Business – TOEIC 900)", "Tiếng Nhật (N3)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "Salesforce Certified Sales Representative", "issuer": "Salesforce", "year": "2022" }],
              "awards": [{ "name": "Top Sales – SAP SEA Region", "year": "2023" }, { "name": "Top 1 Sales – FPT Software", "year": "2020" }],
              "activities": []
            }
            """);
        return t;
    }

    // ── 11. Du lịch & Khách sạn ──────────────────────────────────────────────
    private CvTemplate buildHospitalityTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Hospitality & Tourism");
        t.setDescription("Mẫu CV dành cho ngành Du lịch, Khách sạn, Nhà hàng, Tổ chức Sự kiện. Nổi bật kỹ năng dịch vụ khách hàng.");
        t.setPreviewColor("#0369a1");
        t.setBadgeLabel("Du lịch");
        t.setBadgeBgColor("#e0f2fe");
        t.setBadgeTextColor("#0369a1");
        t.setCategory("Du lịch & Khách sạn");
        t.setStyleTag("minimalist");
        t.setSortOrder(11);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "ĐẶNG THỊ HỒNG NHUNG",
              "subtitle": "Hotel Operations Manager – 5-Star Hospitality",
              "email": "hongnhung.hotel@gmail.com",
              "phone": "0922 333 444",
              "address": "Hoàn Kiếm, Hà Nội",
              "linkedin": "",
              "portfolio": "",
              "summary": "Hotel Operations Manager với 8 năm kinh nghiệm tại các khách sạn 5 sao quốc tế (Marriott, Accor). Chuyên về tối ưu hóa vận hành, nâng cao chỉ số hài lòng khách hàng (Guest Satisfaction Score) và đào tạo nhân viên theo tiêu chuẩn quốc tế.",
              "education": [
                { "school": "ĐẠI HỌC HUẾ – KHOA DU LỊCH", "location": "Huế", "degree": "Cử nhân Quản trị Dịch vụ Du lịch & Lữ hành", "period": "2012 – 2016", "details": ["Tốt nghiệp loại Giỏi", "Thực tập tại Sofitel Metropole Hà Nội"] }
              ],
              "experience": [
                { "company": "JW MARRIOTT HANOI", "location": "Hà Nội", "role": "Front Office Manager", "period": "2020 – Hiện tại", "details": ["Quản lý 40 nhân viên bộ phận lễ tân và concierge cho khách sạn 450 phòng", "Nâng Guest Satisfaction Score từ 82% lên 91% trong 2 năm", "Xử lý khủng hoảng và phàn nàn của khách VIP, duy trì tỷ lệ quay lại 68%"] }
              ],
              "projects": [],
              "skills": [
                { "category": "Vận hành", "items": ["Hotel PMS (Opera)", "Revenue Management", "F&B Operations", "Event Coordination"] },
                { "category": "Kỹ năng mềm", "items": ["Customer Service Excellence", "Team Leadership", "Crisis Management", "Training & Coaching"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.5 – Giao tiếp hàng ngày với khách quốc tế)", "Tiếng Pháp (B1)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "Hospitality Management Certificate", "issuer": "Cornell University (eCornell)", "year": "2021" }],
              "awards": [{ "name": "Best Front Office Manager – Marriott Vietnam", "year": "2022" }],
              "activities": []
            }
            """);
        return t;
    }

    // ── 12. Thực tập sinh (Intern) ────────────────────────────────────────────
    private CvTemplate buildInternTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Internship Applicant");
        t.setDescription("Mẫu CV dành cho sinh viên đang tìm kiếm vị trí thực tập. Nổi bật kỹ năng, dự án sinh viên và hoạt động ngoại khóa.");
        t.setPreviewColor("#155e75");
        t.setBadgeLabel("Cho Intern");
        t.setBadgeBgColor("#cffafe");
        t.setBadgeTextColor("#155e75");
        t.setCategory("Công nghệ & IT");
        t.setStyleTag("minimalist");
        t.setSortOrder(12);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "HOÀNG MỸ LINH",
              "subtitle": "Thực tập sinh Marketing Digital – Năm 3 ĐH",
              "email": "mylinh.intern@gmail.com",
              "phone": "0345 678 901",
              "address": "Tây Hồ, Hà Nội",
              "linkedin": "linkedin.com/in/hoang-my-linh",
              "portfolio": "",
              "summary": "Sinh viên năm 3 chuyên ngành Marketing tại ĐH Ngoại thương, đang tìm kiếm vị trí thực tập Marketing Digital để áp dụng kiến thức học thuật vào thực tiễn. Có kinh nghiệm vận hành fanpage 5.000+ followers và đam mê sáng tạo nội dung.",
              "education": [
                { "school": "ĐẠI HỌC NGOẠI THƯƠNG HÀ NỘI", "location": "Hà Nội", "degree": "Cử nhân Kinh doanh Quốc tế – Chuyên ngành Marketing", "period": "2022 – 2026 (dự kiến)", "details": ["GPA hiện tại: 3.6/4.0 – 4 học kỳ liên tiếp đạt học lực Giỏi", "Thành viên Ban Marketing – CLB Kinh doanh FTU"] }
              ],
              "experience": [],
              "projects": [
                { "name": "Chiến dịch truyền thông \"Mùa Tựu Trường\" – CLB FTU Business", "period": "Tháng 8, 2023", "tech": "Canva, Facebook Ads, Google Forms", "github": "", "details": ["Lên kế hoạch và thực hiện chiến dịch mạng xã hội thu hút 1.200 tân sinh viên đăng ký", "Quản lý fanpage đạt 5.000 followers trong 3 tháng"] }
              ],
              "skills": [
                { "category": "Marketing", "items": ["Content Writing", "Facebook Ads (cơ bản)", "SEO cơ bản", "Canva", "Google Analytics"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 6.5)", "Tiếng Trung (HSK 4)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "Google Digital Marketing Fundamentals", "issuer": "Google", "year": "2023" }],
              "awards": [{ "name": "Sinh viên Tiêu biểu khoa KTQT", "year": "2023" }],
              "activities": [{ "name": "CLB Kinh doanh FTU", "role": "Trưởng Ban Marketing", "period": "2022 – Hiện tại", "details": ["Tổ chức 4 sự kiện lớn trong năm với tổng 800+ người tham dự"] }]
            }
            """);
        return t;
    }

    // ── 13. Senior IT ─────────────────────────────────────────────────────────
    private CvTemplate buildSeniorItTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Senior IT & Tech Lead");
        t.setDescription("Mẫu CV 2 trang dành cho Senior Developer, Tech Lead, Solution Architect với 5+ năm kinh nghiệm. Trình bày kiến trúc hệ thống và thành tích kỹ thuật.");
        t.setPreviewColor("#312e81");
        t.setBadgeLabel("Senior");
        t.setBadgeBgColor("#ede9fe");
        t.setBadgeTextColor("#312e81");
        t.setCategory("Công nghệ & IT");
        t.setStyleTag("professional");
        t.setSortOrder(13);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "NGUYỄN THANH LONG",
              "subtitle": "Principal Software Engineer & Tech Lead",
              "email": "thanhlong.principal@gmail.com",
              "phone": "0916 999 888",
              "address": "Quận 2, TP. Hồ Chí Minh",
              "linkedin": "linkedin.com/in/thanhlong-tech",
              "portfolio": "github.com/thanhlong-dev",
              "summary": "Principal Engineer với 10 năm kinh nghiệm thiết kế và xây dựng hệ thống phân tán quy mô lớn. Dẫn dắt team 20+ kỹ sư tại Grab và VNG. Chuyên gia trong lĩnh vực Microservices, Cloud Architecture và System Design. Góp phần xây dựng các hệ thống phục vụ hàng triệu người dùng/ngày.",
              "education": [
                { "school": "ĐẠI HỌC BÁCH KHOA TP. HCM", "location": "TP. HCM", "degree": "Kỹ sư Công nghệ Thông tin", "period": "2010 – 2014", "details": ["GPA: 3.8/4.0 – Tốt nghiệp Xuất sắc"] }
              ],
              "experience": [
                { "company": "GRAB VIETNAM", "location": "TP. HCM", "role": "Principal Engineer – Payments Platform", "period": "2020 – Hiện tại", "details": ["Thiết kế và xây dựng Payment Platform xử lý 5 triệu giao dịch/ngày, uptime 99.99%", "Dẫn dắt team 20 engineers, mentoring 5 Senior Developers lên Principal", "Giảm infrastructure cost 35% qua tối ưu Kubernetes & auto-scaling", "Chủ trì migration từ monolith sang Microservices (18 tháng, 0 downtime)"] },
                { "company": "VNG CORPORATION", "location": "TP. HCM", "role": "Senior Backend Engineer – ZaloPay", "period": "2017 – 2020", "details": ["Xây dựng core banking module cho ZaloPay từ 0 đến 3 triệu users", "Thiết kế idempotent payment API chịu tải 10.000 TPS"] },
                { "company": "TIKI", "location": "TP. HCM", "role": "Backend Developer", "period": "2014 – 2017", "details": ["Phát triển hệ thống quản lý kho và đơn hàng cho sàn TMĐT top 3 Việt Nam"] }
              ],
              "projects": [
                { "name": "Open-source: Distributed Rate Limiter (Go)", "period": "2022 – Hiện tại", "tech": "Go, Redis Cluster, Lua Scripts", "github": "github.com/thanhlong/go-ratelimiter", "details": ["1.200+ GitHub stars, được dùng bởi 50+ công ty trong khu vực", "Đạt 1 triệu ops/giây trên single node"] }
              ],
              "skills": [
                { "category": "Architecture", "items": ["Microservices", "Event-Driven (Kafka)", "DDD", "CQRS", "API Gateway"] },
                { "category": "Backend", "items": ["Go (Golang)", "Java/Spring Boot", "Node.js", "gRPC", "GraphQL"] },
                { "category": "Cloud & DevOps", "items": ["AWS (Solutions Architect Pro)", "Kubernetes", "Terraform", "Datadog", "Prometheus"] },
                { "category": "Database", "items": ["PostgreSQL", "MySQL", "Redis", "Cassandra", "Elasticsearch"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (C1 – Technical)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "AWS Solutions Architect Professional", "issuer": "Amazon Web Services", "year": "2022" }, { "name": "Certified Kubernetes Administrator (CKA)", "issuer": "CNCF", "year": "2021" }],
              "awards": [{ "name": "Grab Engineering Excellence Award", "year": "2023" }, { "name": "VNG Outstanding Engineer", "year": "2019" }],
              "activities": [{ "name": "Diễn giả – Vietnam Developer Summit", "role": "Speaker", "period": "2022, 2023", "details": ["Thuyết trình về Distributed Systems và Payment Architecture"] }]
            }
            """);
        return t;
    }

    // ── 14. Quản lý cấp cao (Manager) ────────────────────────────────────────
    private CvTemplate buildManagerTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Department Manager & Director");
        t.setDescription("Mẫu CV dành cho Trưởng phòng, Giám đốc cấp trung và Senior Management. Nổi bật tầm nhìn chiến lược và thành tích lãnh đạo.");
        t.setPreviewColor("#1e293b");
        t.setBadgeLabel("Management");
        t.setBadgeBgColor("#f1f5f9");
        t.setBadgeTextColor("#1e293b");
        t.setCategory("Quản lý & Lãnh đạo");
        t.setStyleTag("professional");
        t.setSortOrder(14);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "BÙI THỊ THANH HÀ",
              "subtitle": "Country Sales Director – FMCG | P&L Owner",
              "email": "thanhha.director@gmail.com",
              "phone": "0902 111 999",
              "address": "Ba Đình, Hà Nội",
              "linkedin": "linkedin.com/in/thanhha-director",
              "portfolio": "",
              "summary": "Giám đốc Kinh doanh với 12 năm kinh nghiệm trong ngành FMCG tại các tập đoàn đa quốc gia (P&G, Unilever, Nestlé). Sở hữu track record quản lý P&L hơn 500 tỷ VNĐ/năm và dẫn dắt tổ chức 200+ nhân sự trên toàn quốc. Thế mạnh về xây dựng chiến lược Go-to-Market và phát triển kênh phân phối.",
              "education": [
                { "school": "ĐẠI HỌC NGOẠI THƯƠNG HÀ NỘI", "location": "Hà Nội", "degree": "Thạc sĩ Quản trị Kinh doanh (MBA)", "period": "2015 – 2017", "details": ["Học bổng Chính phủ – RMIT Vietnam"] },
                { "school": "ĐẠI HỌC NGOẠI THƯƠNG HÀ NỘI", "location": "Hà Nội", "degree": "Cử nhân Kinh doanh Quốc tế", "period": "2006 – 2010", "details": ["Tốt nghiệp loại Giỏi – Top 3% khóa"] }
              ],
              "experience": [
                { "company": "NESTLÉ VIETNAM", "location": "Hà Nội", "role": "National Sales Director", "period": "2020 – Hiện tại", "details": ["Quản lý P&L 500 tỷ VNĐ/năm cho danh mục sản phẩm Dairy & Nutrition", "Dẫn dắt tổ chức 200+ nhân sự Sales trên 63 tỉnh/thành", "Tăng trưởng doanh thu 28% CAGR trong 3 năm, vượt market growth 2x", "Xây dựng hệ thống phân phối 150+ Distributors, 80.000+ điểm bán"] },
                { "company": "UNILEVER VIETNAM", "location": "TP. HCM", "role": "Regional Sales Manager – South Vietnam", "period": "2015 – 2020", "details": ["Quản lý 8 tỉnh miền Nam, doanh thu 200 tỷ VNĐ/năm, team 60 người", "Đạt 118% target liên tiếp 5 năm, 2 lần nhận Unilever President Award"] }
              ],
              "projects": [],
              "skills": [
                { "category": "Chiến lược", "items": ["P&L Management", "Go-to-Market Strategy", "Commercial Planning", "Channel Development", "Key Account Management"] },
                { "category": "Lãnh đạo", "items": ["Team Building (200+ người)", "Talent Development", "Change Management", "Executive Presentation"] },
                { "category": "Phân tích", "items": ["Nielsen & Kantar Data", "Power BI", "Advanced Excel", "Salesforce"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (C1 – Executive Business)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "PMP – Project Management Professional", "issuer": "PMI", "year": "2018" }],
              "awards": [{ "name": "Nestlé Vietnam CEO Award", "year": "2022" }, { "name": "Unilever President Award", "year": "2017, 2019" }],
              "activities": [{ "name": "Hội đồng tư vấn – Chương trình MBA RMIT", "role": "Industry Advisor", "period": "2021 – Hiện tại", "details": [] }]
            }
            """);
        return t;
    }

    // ── 15. Freelancer ────────────────────────────────────────────────────────
    private CvTemplate buildFreelancerTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Freelancer & Independent Consultant");
        t.setDescription("Mẫu CV dành cho Freelancer, Tư vấn độc lập. Nổi bật danh mục dự án, khách hàng và kỹ năng đa dạng.");
        t.setPreviewColor("#065f46");
        t.setBadgeLabel("Freelance");
        t.setBadgeBgColor("#d1fae5");
        t.setBadgeTextColor("#065f46");
        t.setCategory("Freelance & Tư vấn");
        t.setStyleTag("creative");
        t.setSortOrder(15);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "LÝ QUỐC BẢO",
              "subtitle": "Fullstack Freelancer & UI/UX Consultant",
              "email": "quocbao.freelance@gmail.com",
              "phone": "0977 654 321",
              "address": "Đà Nẵng (Remote available worldwide)",
              "linkedin": "linkedin.com/in/lyquocbao-dev",
              "portfolio": "quocbao.design",
              "summary": "Freelancer với 6 năm kinh nghiệm tự do, chuyên xây dựng sản phẩm web/mobile tốc độ cao cho startups và SMEs tại Việt Nam, Singapore và Mỹ. Đã hoàn thành 80+ dự án từ MVP đến production. Chuyên sâu về React, Node.js và thiết kế UI/UX. Upwork Top Rated Plus (JSS 99%).",
              "education": [
                { "school": "ĐẠI HỌC ĐÀ NẴNG – BKDN", "location": "Đà Nẵng", "degree": "Kỹ sư CNTT", "period": "2013 – 2018", "details": ["GPA: 3.5/4.0"] }
              ],
              "experience": [
                { "company": "Freelance / Self-Employed", "location": "Remote", "role": "Fullstack Developer & UI/UX Consultant", "period": "2018 – Hiện tại", "details": ["Hoàn thành 80+ dự án cho khách hàng tại VN, Singapore, Úc, Mỹ – Tổng doanh thu >2 tỷ VNĐ", "Upwork Top Rated Plus – JSS 99%, 50+ đánh giá 5 sao", "Chuyên các dự án SaaS, E-commerce, Fintech và HealthTech"] }
              ],
              "projects": [
                { "name": "SaaS HR Platform – Client: Singapore Startup", "period": "2023", "tech": "Next.js, NestJS, PostgreSQL, AWS", "github": "", "details": ["Xây dựng MVP từ 0 đến launch trong 3 tháng, 200+ beta users", "Tích hợp Stripe payment và Zoom SDK cho tính năng interview"] },
                { "name": "Mobile App E-commerce – Client: VN SME", "period": "2022", "tech": "React Native, Firebase, Node.js", "github": "", "details": ["App đạt 10.000+ downloads trong tháng đầu, rating 4.8/5"] }
              ],
              "skills": [
                { "category": "Frontend", "items": ["React.js", "Next.js", "TypeScript", "React Native", "Figma (UI/UX)"] },
                { "category": "Backend", "items": ["Node.js", "NestJS", "Express", "Python/FastAPI", "GraphQL"] },
                { "category": "Cloud & Tools", "items": ["AWS", "Firebase", "Supabase", "Docker", "Vercel"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (C1 – Làm việc trực tiếp với khách nước ngoài)", "Tiếng Việt (Bản ngữ)"] }
              ],
              "certifications": [{ "name": "AWS Certified Developer – Associate", "issuer": "Amazon Web Services", "year": "2023" }, { "name": "Google UX Design Certificate", "issuer": "Google / Coursera", "year": "2021" }],
              "awards": [{ "name": "Upwork Top Rated Plus", "year": "2022" }, { "name": "Toptal Top 3% Developers", "year": "2023" }],
              "activities": [{ "name": "Mentor tại DaN Tech Community", "role": "Volunteer Mentor", "period": "2020 – Hiện tại", "details": ["Hướng dẫn 30+ junior developers chuyển ngành vào lập trình"] }]
            }
            """);
        return t;
    }
}
