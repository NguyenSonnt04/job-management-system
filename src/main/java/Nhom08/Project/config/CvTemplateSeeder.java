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

        // === STYLE ĐẶC BIỆT ===
        seedTemplate(buildHarvardTemplate());

        // === FEATURED STYLE TEMPLATES ===
        seedTemplate(buildTopCVAtsTemplate());
        seedTemplate(buildTopCVGradTemplate());
        seedTemplate(buildTopCVMinimalTemplate());
        seedTemplate(buildTopCvSidebarTemplate());

        // === NGÀNH NGHỀ MỚI ===
        seedTemplate(buildDataAnalystTemplate());
        seedTemplate(buildProductManagerTemplate());
        seedTemplate(buildDevOpsTemplate());
        seedTemplate(buildCustomerSuccessTemplate());
        seedTemplate(buildContentCreatorTemplate());
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
        t.setBadgeLabel("Classic");
        t.setBadgeBgColor("#d1fae5");
        t.setBadgeTextColor("#065f46");
        t.setCategory("Tài chính & Kinh doanh");
        t.setStyleTag("classic");
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
        t.setBadgeLabel("Academic Classic");
        t.setBadgeBgColor("#dbeafe");
        t.setBadgeTextColor("#1e40af");
        t.setCategory("Giáo dục & Hàn lâm");
        t.setStyleTag("classic");
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
        t.setBadgeLabel("Executive Classic");
        t.setBadgeBgColor("#f5f5f4");
        t.setBadgeTextColor("#1c1917");
        t.setCategory("Luật & Pháp chế");
        t.setStyleTag("classic");
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

    // ── 16. Harvard Style ─────────────────────────────────────────────────────
    private CvTemplate buildHarvardTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Harvard Classic");
        t.setDescription("Mẫu CV cổ điển kiểu Harvard – Typography trang trọng, đen trắng, phù hợp cho academic, nghiên cứu sinh, ứng viên MBA và các vị trí cấp cao tại tổ chức quốc tế.");
        t.setPreviewColor("#1a1a1a");
        t.setBadgeLabel("Harvard Style");
        t.setBadgeBgColor("#f5f5f4");
        t.setBadgeTextColor("#1a1a1a");
        t.setCategory("Giáo dục & Hàn lâm");
        t.setStyleTag("harvard");
        t.setSortOrder(16);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "TRAN THI MINH NGUYET",
              "subtitle": "MBA Candidate | Former Management Consultant at McKinsey",
              "email": "minhnguyet.mba@gmail.com",
              "phone": "+65 9123 4567",
              "address": "Singapore",
              "linkedin": "linkedin.com/in/minhnguyet-mba",
              "portfolio": "",
              "summary": "MBA candidate at Harvard Business School with 5 years of management consulting experience at McKinsey & Company across Southeast Asia. Passionate about strategy development, digital transformation, and sustainable business practices. Seeking summer associate roles in strategy consulting or private equity.",
              "education": [
                {
                  "school": "HARVARD BUSINESS SCHOOL",
                  "location": "Boston, MA",
                  "degree": "Master of Business Administration (MBA)",
                  "period": "2024 – 2026 (Expected)",
                  "details": [
                    "Recipient of Dean's Award for Academic Excellence",
                    "Co-President of Digital Healthcare Club",
                    "Member of Consulting Club and Women in Business"
                  ]
                },
                {
                  "school": "NATIONAL UNIVERSITY OF SINGAPORE (NUS)",
                  "location": "Singapore",
                  "degree": "Bachelor of Business Administration (First Class Honors)",
                  "period": "2016 – 2020",
                  "details": [
                    "GPA: 4.7/5.0 – Top 3% of cohort",
                    "Valedictorian, Business School",
                    "President's Graduate Fellowship recipient"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "MCKINSEY & COMPANY",
                  "location": "Singapore & Ho Chi Minh City",
                  "role": "Engagement Manager",
                  "period": "2022 – 2024",
                  "details": [
                    "Led 8+ consulting engagements for Fortune 500 clients across Southeast Asia, focusing on digital transformation and growth strategy",
                    "Managed project teams of 4–6 consultants, delivering $50M+ in identified client impact",
                    "Developed go-to-market strategy for a regional e-commerce platform, resulting in 35% market share increase within 18 months",
                    "Mentored 3 junior consultants to promotion; designed training program adopted firm-wide"
                  ]
                },
                {
                  "company": "MCKINSEY & COMPANY",
                  "location": "Singapore",
                  "role": "Associate",
                  "period": "2020 – 2022",
                  "details": [
                    "Supported due diligence for $200M+ PE investments in Southeast Asian technology sector",
                    "Built financial models and conducted market analysis for consumer goods entry into Vietnam",
                    "Co-authored firm knowledge piece on digital banking trends in ASEAN"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Impact Investment in Vietnamese Agritech",
                  "period": "2023 – 2024",
                  "tech": "Strategy, Financial Modeling, Impact Assessment",
                  "github": "",
                  "details": [
                    "Pro bono consulting for impact fund assessing $10M investment in climate-smart agriculture",
                    "Developed impact measurement framework aligned with UN SDGs"
                  ]
                }
              ],
              "skills": [
                { "category": "Strategic Consulting", "items": ["Growth Strategy", "Due Diligence", "Market Entry", "Digital Transformation", "M&A Advisory"] },
                { "category": "Analytical Tools", "items": ["Financial Modeling", "Excel (Advanced)", "Python (Pandas)", "Tableau", "PowerPoint"] },
                { "category": "Languages", "items": ["English (Native)", "Vietnamese (Native)", "Mandarin (Business Proficient)"] }
              ],
              "certifications": [
                { "name": "CFA Level III Candidate", "issuer": "CFA Institute", "year": "2023" },
                { "name": "GMAT 760", "issuer": "GMAC", "year": "2023" }
              ],
              "awards": [
                { "name": "Dean's Award for Academic Excellence", "year": "2024" },
                { "name": "McKinsey Engagement Manager of the Year – APAC", "year": "2023" }
              ],
              "activities": [
                {
                  "name": "Harvard Business School – Consulting Club",
                  "role": "Co-President",
                  "period": "2024 – Present",
                  "details": []
                },
                {
                  "name": "Teach For Malaysia – Board of Advisors",
                  "role": "Pro Bono Advisor",
                  "period": "2022 – Present",
                  "details": []
                }
              ]
            }
            """);
        return t;
    }

    // ────────────────────────────────────────────────────────────────
    //  FEATURED STYLE TEMPLATES - curated into the 5 core style families
    // ────────────────────────────────────────────────────────────────

    // ── 17. TopCV ATS-Friendly ───────────────────────────────────────────
    private CvTemplate buildTopCVAtsTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Classic ATS Standard");
        t.setDescription("Mẫu CV cổ điển, rõ ràng và tối ưu ATS. Phù hợp cho môi trường tuyển dụng truyền thống, ngân hàng, tư vấn và các vị trí yêu cầu bố cục chuẩn mực.");
        t.setPreviewColor("#2563eb");
        t.setBadgeLabel("Classic ATS");
        t.setBadgeBgColor("#dbeafe");
        t.setBadgeTextColor("#1d4ed8");
        t.setCategory("Mẫu nổi bật");
        t.setStyleTag("classic");
        t.setSortOrder(17);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "PHẠM THỊY DUNG",
              "subtitle": "Kỹ sư Phần Mềm | 3 Năm Kinh Nghiệm",
              "email": "thidung.dev@gmail.com",
              "phone": "0912 345 678",
              "address": "Quận 1, TP. Hồ Chí Minh",
              "summary": "Kỹ sư phần mềm với 3 năm kinh nghiệm phát triển ứng dụng Java và React. Chuyên về xây dựng hệ thống backend hiệu suất cao và kiến trúc microservices. Đam mê học hỏi và đóng góp vào các dự án open source.",
              "education": [
                {
                  "school": "ĐẠI HỌC BÁCH KHOA TP. HCM",
                  "location": "TP. Hồ Chí Minh",
                  "degree": "Kỹ sư Công nghệ Thông tin",
                  "period": "2019 – 2023",
                  "details": [
                    "GPA: 3.7/4.0",
                    "Đồ án: Hệ thống quản lý nhân sự sử dụng Spring Boot và React",
                    "Chứng chỉ: Oracle Certified Java SE 11 Developer (2022)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "FPT SOFTWARE",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Backend Developer",
                  "period": "07/2022 – Hiện tại",
                  "details": [
                    "Phát triển API RESTful cho hệ thống quản lý nhân sự phục vụ 10.000+ người dùng",
                    "Tối ưu hóa database queries, giảm 40% thời gian phản hồi",
                    "Xây dựng CI/CD pipeline với GitHub Actions và Docker",
                    "Mentor 3 thực tập sinh về Java best practices"
                  ]
                },
                {
                  "company": "CMC GLOBAL",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Java Developer Intern",
                  "period": "01/2022 – 06/2022",
                  "details": [
                    "Thực tập module thanh toán tích hợp VNPay và Momo",
                    "Học hỏi và áp dụng Agile/Scrum trong phát triển phần mềm"
                  ]
                }
              ],
              "skills": [
                { "category": "Backend", "items": ["Java", "Spring Boot", "Spring Security", "Microservices", "REST API"] },
                { "category": "Frontend", "items": ["ReactJS", "TypeScript", "HTML/CSS", "Bootstrap"] },
                { "category": "Database", "items": ["MySQL", "PostgreSQL", "Redis", "MongoDB"] },
                { "category": "DevOps", "items": ["Docker", "Git", "Jenkins", "AWS (Basic)"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (TOEIC 800)", "Tiếng Việt"] }
              ],
              "certifications": [
                { "name": "Oracle Certified Java SE 11 Developer", "issuer": "Oracle", "year": "2022" },
                { "name": "AWS Cloud Practitioner", "issuer": "Amazon Web Services", "year": "2023" }
              ]
            }
            """);
        return t;
    }

    // ── 18. TopCV Gradient Header ─────────────────────────────────────────
    private CvTemplate buildTopCVGradTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Gradient Creative Studio");
        t.setDescription("Mẫu CV sáng tạo với header gradient và nhịp thị giác mạnh. Phù hợp cho Creative, Marketing, Branding và Product Design.");
        t.setPreviewColor("#7c3aed");
        t.setBadgeLabel("Creative");
        t.setBadgeBgColor("#ede9fe");
        t.setBadgeTextColor("#6d28d9");
        t.setCategory("Mẫu nổi bật");
        t.setStyleTag("creative");
        t.setSortOrder(18);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "LÊI THỦ YÊN NGA",
              "subtitle": "UI/UX Designer | Creative Director",
              "email": "yenga.design@gmail.com",
              "phone": "0905 789 012",
              "address": "Quận 3, TP. Hồ Chí Minh",
              "summary": "UI/UX Designer với 5 năm kinh nghiệm thiết kế sản phẩm kỹ thuật số. Portfolio gồm 20+ sản phẩm đã launch với hàng triệu người dùng. Đam mê tạo ra trải nghiệm người dùng đáng nhớ và thiết kế hệ thống thiết kế nhất quán.",
              "education": [
                {
                  "school": "ĐẠI HỌC MỸ THUẬT CÔNG NGHIỆP HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Thiết kế Truyền thông Đa phương tiện",
                  "period": "2017 – 2021",
                  "details": [
                    "Tốt nghiệp Xuất sắc",
                    "Học bổng trao đổi tại Politecnico di Milano, Ý (2019)",
                    "Giải Nhất Young Lotus 2019"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "TIKI CORPORATION",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Senior UX Designer",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Redesign toàn diện luồng Checkout, giảm tỷ lệ bỏ giỏ hàng 22%",
                    "Xây dựng Tiki Design System với 200+ components",
                    "User Research với 50+ người dùng mỗi quý",
                    "Mentor 4 junior designer"
                  ]
                },
                {
                  "company": "BASE.VN",
                  "location": "Hà Nội",
                  "role": "UI/UX Designer",
                  "period": "2020 – 2021",
                  "details": [
                    "Thiết kế toàn bộ UI cho nền tảng quản lý doanh nghiệp",
                    "Tăng System Usability Score từ 68 lên 84"
                  ]
                }
              ],
              "projects": [
                {
                  "name": "Redesign App Giao đồ ăn SuperShip",
                  "period": "09/2022 – 11/2022",
                  "tech": "Figma, Maze, Hotjar, Principle",
                  "github": "behance.net/yenga-design",
                  "details": [
                    "Redesign toàn bộ trải nghiệm đặt đồ ăn",
                    "Tăng Conversion Rate từ 18% lên 31%",
                    "Featured trên Dribbble với 2.000+ lượt thích"
                  ]
                }
              ],
              "skills": [
                { "category": "Design Tools", "items": ["Figma", "Adobe XD", "Sketch", "Illustrator", "Photoshop"] },
                { "category": "UX Methods", "items": ["Design Thinking", "User Research", "Usability Testing", "Prototyping"] },
                { "category": "Development", "items": ["HTML/CSS", "JavaScript", "ReactJS"] },
                { "category": "Ngôn ngữ", "items": ["Tiếng Anh (IELTS 7.0)", "Tiếng Việt"] }
              ]
            }
            """);
        return t;
    }

    // ── 19. TopCV Minimalist Clean ────────────────────────────────────────
    private CvTemplate buildTopCVMinimalTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Minimal Clean Balance");
        t.setDescription("Mẫu CV tối giản với nhịp trắng thoáng, typography sạch và dễ đọc. Phù hợp cho ứng viên muốn làm nổi bật nội dung theo cách tinh tế.");
        t.setPreviewColor("#0e7490");
        t.setBadgeLabel("Minimalist");
        t.setBadgeBgColor("#cffafe");
        t.setBadgeTextColor("#0e7490");
        t.setCategory("Mẫu nổi bật");
        t.setStyleTag("minimalist");
        t.setSortOrder(19);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "TRỊN QUỐC BẢO",
              "subtitle": "Fullstack Developer | Open Source Contributor",
              "email": "quocbao.dev@gmail.com",
              "phone": "0977 654 321",
              "address": "Đà Nẵng (Remote)",
              "summary": "Fullstack Developer với 4 năm kinh nghiệm xây dựng ứng dụng web sử dụng MERN stack. Đóng góp vào nhiều dự án open source với hơn 500 stars trên GitHub. Đam mê tạo ra sản phẩm có giá trị và chia sẻ kiến thức thông qua blog và technical talks.",
              "education": [
                {
                  "school": "ĐẠI HỌC BÁCH KHOA ĐÀ NẴNG",
                  "location": "Đà Nẵng",
                  "degree": "Kỹ sư Công nghệ Thông tin",
                  "period": "2018 – 2022",
                  "details": [
                    "GPA: 3.8/4.0",
                    "Tốt nghiệp Đồ án tốt nghiệp: Hệ thống quản lý tasks với React và Node.js"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "TECH STARTUP VIETNAM",
                  "location": "Đà Nẵng",
                  "role": "Fullstack Developer",
                  "period": "2022 – Hiện tại",
                  "details": [
                    "Phát triển platform SaaS cho quản lý kho vận hành",
                    "Xây dựng RESTful APIs với Node.js, Express và MongoDB",
                    "Tích hợp các gateway thanh toán: Stripe, PayPal, VNPay",
                    "Triển khai ứng dụng trên AWS với Docker và Kubernetes"
                  ]
                },
                {
                  "company": "FREELANCE",
                  "location": "Remote",
                  "role": "Web Developer",
                  "period": "2020 – 2022",
                  "details": [
                    "Hoàn thành 20+ dự án cho khách hàng trên toàn thế giới",
                    "Làm việc trên các platform: Upwork, Fiverr, Freelancer.com",
                    "Specialized in React, Node.js and WordPress"
                  ]
                }
              ],
              "skills": [
                { "category": "Frontend", "items": ["React", "Vue.js", "Next.js", "TypeScript", "TailwindCSS"] },
                { "category": "Backend", "items": ["Node.js", "Express", "NestJS", "FastAPI"] },
                { "category": "Database", "items": ["PostgreSQL", "MongoDB", "Redis", "MySQL"] },
                { "category": "DevOps", "items": ["AWS", "Docker", "CI/CD", "Git"] },
                { "category": "Soft Skills", "items": ["Problem Solving", "Communication", "Time Management", "Adaptability"] }
              ],
              "certifications": [
                { "name": "AWS Certified Developer – Associate", "issuer": "Amazon Web Services", "year": "2023" },
                { "name": "MongoDB Certified Developer", "issuer": "MongoDB University", "year": "2022" }
              ],
              "projects": [
                {
                  "name": "Task Management System",
                  "period": "2023",
                  "tech": "React, Node.js, MongoDB, AWS",
                  "github": "github.com/quocbao/task-manager",
                  "details": [
                    "Ứng dụng quản lý công việc cá nhân với tính năng drag-and-drop",
                    "2,000+ monthly active users",
                    "Open source trên GitHub với 300+ stars"
                  ]
                }
              ]
            }
            """);
        return t;
    }

    // ── 20. TopCV Modern Sidebar ──────────────────────────────────────────
    private CvTemplate buildTopCvSidebarTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Executive Sidebar Pro");
        t.setDescription("Mẫu CV chuyên nghiệp với sidebar nhấn màu và hệ phân cấp rõ ràng. Phù hợp cho quản lý, kinh doanh, vận hành và các vai trò lead.");
        t.setPreviewColor("#0891b2");
        t.setBadgeLabel("Professional");
        t.setBadgeBgColor("#cffafe");
        t.setBadgeTextColor("#0891b2");
        t.setCategory("Mẫu nổi bật");
        t.setStyleTag("professional");
        t.setSortOrder(20);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "HOÀNG THỊ LAN CHI",
              "subtitle": "Digital Marketing Manager | 6 Năm Kinh Nghiệm",
              "email": "lanchi.mkt@gmail.com",
              "phone": "0933 444 555",
              "address": "Tân Bình, TP. Hồ Chí Minh",
              "linkedin": "linkedin.com/in/hoangthilanchi",
              "summary": "Digital Marketing Manager với 6 năm kinh nghiệm quản lý ngân sách và chiến dịch cho các thương hiệu FMCG lớn. Chuyên về Performance Marketing, Content Strategy và Brand Development. Đã giúp tăng 35% doanh thu online trong vòng 1 năm.",
              "education": [
                {
                  "school": "ĐẠI HỌC KINH TẾ TP. HCM (UEH)",
                  "location": "TP. Hồ Chí Minh",
                  "degree": "Cử nhân Quản trị Kinh doanh – Chuyên ngành Marketing",
                  "period": "2015 – 2019",
                  "details": [
                    "GPA: 3.6/4.0",
                    "Chứng chỉ Digital Marketing Professional (Google Hub 2020)",
                    "Chứng chỉ Facebook Blueprint Professional (Meta 2021)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "UNILEVER VIETNAM",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Digital Marketing Manager",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Quản lý ngân sách digital 500 triệu đồng/tháng",
                    "Chạy chiến dịch trên Facebook Ads, Google Ads, TikTok Ads",
                    "Tăng 42% traffic organic qua SEO và Content Marketing",
                    "Dẫn dắt team 5 người marketing executives"
                  ]
                },
                {
                  "company": "SHOPEE",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Performance Marketing Specialist",
                  "period": "2019 – 2021",
                  "details": [
                    "Optimize campaign performance, đạt ROAS 4x cho campaigns fashion",
                    "Phân tích dữ liệu và báo cáo hiệu suất hàng tuần",
                    "Làm việc với Product team để cải thiện conversion funnel"
                  ]
                }
              ],
              "skills": [
                { "category": "Digital Marketing", "items": ["Facebook Ads", "Google Ads", "TikTok Ads", "Performance Marketing"] },
                { "category": "Analytics", "items": ["Google Analytics 4", "Google Tag Manager", "Facebook Pixel", "Hotjar"] },
                { "category": "Tools", "items": ["HubSpot", "Mailchimp", "Canva", "Hootsuite"] },
                { "category": "Soft Skills", "items": ["Leadership", "Communication", "Strategic Planning", "Team Management"] },
                { "category": "Languages", "items": ["Tiếng Anh (IELTS 7.5)", "Tiếng Việt"] }
              ],
              "achievements": [
                { "name": "Top Performer – Unilever Vietnam Q4 2023", "year": "2023" },
                { "name": "Best Campaign – Shopee Mall Brand Festival 2020", "year": "2020" }
              ]
            }
            """);
        return t;
    }

    // ────────────────────────────────────────────────────────────────
    //  NGÀNH NGHỀ MỚI - HOT INDUSTRIES
    // ────────────────────────────────────────────────────────────────

    // ── 21. Data Analyst & BI ──────────────────────────────────────────────
    private CvTemplate buildDataAnalystTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Data Analyst & BI Specialist");
        t.setDescription("Mẫu CV chuyên nghiệp cho Data Analyst, Business Intelligence, Data Scientist. Nổi bật kỹ năng phân tích dữ liệu, SQL, Python và visualization.");
        t.setPreviewColor("#059669");
        t.setBadgeLabel("Data & Analytics");
        t.setBadgeBgColor("#d1fae5");
        t.setBadgeTextColor("#059669");
        t.setCategory("Công nghệ & Data");
        t.setStyleTag("professional");
        t.setSortOrder(21);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "NGUYỄN MINH QUÂN",
              "subtitle": "Data Analyst & BI Specialist | Python | SQL | Tableau",
              "email": "minhquan.data@gmail.com",
              "phone": "0982 333 444",
              "address": "Cầu Giấy, Hà Nội",
              "summary": "Data Analyst với 4 năm kinh nghiệm phân tích dữ liệu kinh doanh và xây dựng dashboard báo cáo. Thành thạo Python, SQL, Tableau và Power BI. Đã giúp các doanh nghiệp đưa ra quyết định dựa trên dữ liệu, tiết kiệm 20% chi phí vận hành.",
              "education": [
                {
                  "school": "ĐẠI HỌC BÁCH KHOA HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Khoa Toán – Ứng Dụng Toán",
                  "period": "2017 – 2021",
                  "details": [
                    "GPA: 3.5/4.0",
                    "Đồ án tốt nghiệp: Ứng dụng Machine Learning trong dự báo giá bất động sản",
                    "Chứng chỉ: Microsoft Certified: Data Analyst Associate (DA-100, 2022)",
                    "Chứng chỉ: Google Data Analytics Professional (2021)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "TECH COM VN",
                  "location": "Hà Nội",
                  "role": "Senior Data Analyst",
                  "period": "2022 – Hiện tại",
                  "details": [
                    "Phân tích dữ liệu bán hàng từ 50+ nền tảng thương mại điện tử",
                    "Xây dựng dashboard giám sát KPIs theo thời gian thực với Tableau",
                    "Tạo báo cáo tự động bằng Python, tiết kiệm 15 giờ/tuần cho team BI",
                    "Dự báo doanh thu bán hàng với độ chính xác 90%"
                  ]
                },
                {
                  "company": "MASSAN GROUP",
                  "location": "Hà Nội",
                  "role": "Data Analyst",
                  "period": "2021 – 2022",
                  "details": [
                    "Làm sạch và xử lý dữ liệu thô từ nhiều nguồn (ERP, CRM, Website)",
                    "Xây dựng mô hình RFM để phân khúc khách hàng",
                    "Hỗ trợ Marketing team tối ưu hóa các chiến dịch quảng cáo"
                  ]
                }
              ],
              "skills": [
                { "category": "Programming", "items": ["Python (Pandas, NumPy)", "R", "SQL (PostgreSQL, MySQL)", "Spark"] },
                { "category": "Visualization", "items": ["Tableau", "Power BI", "Google Data Studio", "Matplotlib"] },
                { "category": "Data Engineering", "items": ["ETL", "Data Warehousing", "BigQuery", "AWS S3"] },
                { "category": "Analytics", "items": ["Descriptive Analytics", "Predictive Analytics", "A/B Testing", "Cohort Analysis"] },
                { "category": "Soft Skills", "items": ["Data Storytelling", "Critical Thinking", "Business Acumen", "Communication"] }
              ],
              "tools": ["Excel Advanced", "SQL Server Management Studio", "Jupyter Notebook", "Google Analytics 4"],
              "certifications": [
                { "name": "Microsoft Certified: Data Analyst Associate", "issuer": "Microsoft", "year": "2022" },
                { "name": "Google Data Analytics Professional Certificate", "issuer": "Google", "year": "2021" }
              ]
            }
            """);
        return t;
    }

    // ── 22. Product Manager ────────────────────────────────────────────────
    private CvTemplate buildProductManagerTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Product Manager (Tech/SaaS)");
        t.setDescription("Mẫu CV cho Product Manager, Product Owner trong các công ty công nghệ. Nổi bật kỹ năng lãnh đạo sản phẩm, Agile/Scrum và roadmap.");
        t.setPreviewColor("#4f46e5");
        t.setBadgeLabel("Product");
        t.setBadgeBgColor("#eef2ff");
        t.setBadgeTextColor("#4f46e5");
        t.setCategory("Sản phẩm & Quản trị");
        t.setStyleTag("professional");
        t.setSortOrder(22);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "TRẦN THỊ TÚNG",
              "subtitle": "Senior Product Manager | FinTech & SaaS",
              "email": "thitung.pm@gmail.com",
              "phone": "0909 123 456",
              "address": "Thảo Điền, Hà Nội",
              "linkedin": "linkedin.com/in/thitung-product",
              "summary": "Senior Product Manager với 7 năm kinh nghiệm trong Fintech và SaaS. Đã lead 15+ sản phẩm từ ý tưởng đến launch, đạt tổng cộng 2 triệu người dùng. Thành thạo Agile/Scrum, Product Discovery và Go-to-Market Strategy. Chứng chỉ Professional Scrum Master (PSM II).",
              "education": [
                {
                  "school": "ĐẠI HỌC FPT",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Quản trị Kinh doanh – Chương trình Chất lượng cao",
                  "period": "2014 – 2018",
                  "details": [
                    "GPA: 3.8/4.0 – Top 5% khóa",
                    "Học bổng toàn phần 4 năm",
                    "Đổi优异成绩 sang Đại học Birmingham (Anh) – Chương trình 2+2"
                  ]
                },
                {
                  "school": "UNIVERSITY OF BIRMINGHAM (UK)",
                  "location": "Birmingham, UK",
                  "degree": "Bachelor of Science – Business Management",
                  "period": "2016 – 2018",
                  "details": [
                    "First Class Honours – Top 10% chương trình",
                    "Dissertation: Disruptive Technology in Emerging Markets"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "VNPAY (E-WALLET)",
                  "location": "Hà Nội",
                  "role": "Senior Product Manager – Digital Payments",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Lead Product cho ví điện tử VNPAY-QR với 5 triệu người dùng",
                    "Xây dựng roadmap và backlog cho team 15 developers & designers",
                    "Chạy thử nghiệm A/B testing, tăng conversion rate 25%",
                    "Phối hợp với Tech, Design, Marketing, Business teams để định hướng sản phẩm"
                  ]
                },
                {
                  "company": "MOVI GROUP",
                  "location": "Hà Nội",
                  "role": "Product Manager – E-Commerce",
                  "period": "2019 – 2021",
                  "details": [
                    "Quản lý nền tảng E-commerce cho hơn 1.000 SME sellers",
                    "Ra mắt tính năng Multi-vendor và Flash Sale, tăng GMV 40%",
                    "Thiết kế và chạy 50+ experiments để optimize conversion funnel"
                  ]
                },
                {
                  "company": "GRAB",
                  "location": "Singapore",
                  "role": "Associate Product Manager",
                  "period": "2018 – 2019",
                  "details": [
                    "Tham gia phát triển GrabFood – tính năng đặt đồ ăn theo nhóm",
                    "Product Discovery với 50+ người dùng qua interviews và surveys",
                    "Viết PRDs và user stories cho development teams"
                  ]
                }
              ],
              "skills": [
                { "category": "Product Management", "items": ["Product Strategy", "Roadmap Planning", "Agile/Scrum", "Product Discovery"] },
                { "category": "Data & Analytics", "items": ["Product Analytics", "A/B Testing", "SQL", "Tableau", "Google Analytics 4"] },
                { "category": "Design", "items": ["Figma", "Wireframing", "User Journey Mapping", "Prototyping"] },
                { "category": "Technical", "items": ["API Design", "Basic Frontend (HTML/CSS)", "Understanding Architecture"] },
                { "category": "Soft Skills", "items": ["Stakeholder Management", "Prioritization", "Communication", "Leadership"] }
              ],
              "achievements": [
                { "name": "Top 5 PM – VNPAY All Hands 2023", "year": "2023" },
                { "name: "Best New Feature – MOVY Product Awards 2020", "year": "2020" }
              ],
              "certifications": [
                { "name": "Professional Scrum Master II (PSM II)", "issuer": "Scrum Alliance", "year": "2022" },
                { "name": "Google Product Analytics Certified", "issuer": "Google", "year": "2020" }
              ]
            }
            """);
        return t;
    }

    // ── 23. DevOps & SRE ────────────────────────────────────────────────────────
    private CvTemplate buildDevOpsTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("DevOps & SRE Engineer");
        t.setDescription("Mẫu CV chuyên nghiệp cho DevOps Engineer, SRE, Cloud Engineer. Nổi bật kỹ năng CI/CD, Cloud, Containerization.");
        t.setPreviewColor("#0f172a");
        t.setBadgeLabel("DevOps");
        t.setBadgeBgColor("#e2e8f0");
        t.setBadgeTextColor("#0f172a");
        t.setCategory("Công nghệ & DevOps");
        t.setStyleTag("professional");
        t.setSortOrder(23);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "PHẠM HỮNG KHÁNH",
              "subtitle": "DevOps Engineer | AWS Certified Solutions Architect",
              "email": "khanh.devops@gmail.com",
              "phone": "0918 777 888",
              "address": "Nam Từ Liêm, Hà Nội",
              "summary": "DevOps Engineer với 5 năm kinh nghiệm xây dựng và quản lý infrastructure on-premise và cloud. AWS Certified Solutions Architect với chuyên sâu về Kubernetes, Terraform và CI/CD. Đã hỗ trợ migration từ monolith sang microservices cho nhiều khách hàng enterprise.",
              "education": [
                {
                  "school": "ĐẠI HỌC BÁCH KHOA HÀ NỘI",
                  "location": "Hà Nội",
                  "degree": "Kỹ sư Công nghệ Thông tin",
                  "period": "2015 – 2019",
                  "details": [
                    "GPA: 3.6/4.0",
                    "Chứng chỉ: AWS Certified Solutions Architect – Associate (2022)",
                    "Chứng chỉ: Certified Kubernetes Administrator (CKA, 2021)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "FPT SMART CLOUD",
                  "location": "Hà Nội",
                  "role": "Senior DevOps Engineer",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Thiết kế và triển khai Kubernetes cluster phục vụ 50+ microservices",
                    "Xây dựng CI/CD pipeline với Jenkins, GitLab CI, ArgoCD",
                    "Automate infrastructure provisioning với Terraform, giảm 80% thời gian setup",
                    "Monitoring & Alerting với Prometheus, Grafana, ELK Stack",
                    "Giảm incident response time từ 2 tiếng xuống 15 phút"
                  ]
                },
                {
                  "company": "VIN GROUP",
                  "location": "Hà Nội",
                  "role": "DevOps Engineer",
                  "period": "2019 – 2021",
                  "details": [
                    "Quản lý Jenkins server và 200+ jobs CI/CD",
                    "Dockerize applications và orchestrate với Docker Swarm",
                    "Maintain GitLab Runner và self-hosted GitLab instance",
                    "Support development teams với 30+ projects"
                  ]
                }
              ],
              "skills": [
                { "category": "Cloud", "items": ["AWS (EC2, S3, RDS, Lambda)", "Azure", "Google Cloud"] },
                { "category": "Container Orchestration", "items": ["Kubernetes", "Docker Swarm", "EKS", "AKS", "GKE"] },
                { "category": "IaC", "items": ["Terraform", "CloudFormation", "Ansible", "Puppet"] },
                { "category": "CI/CD", "items": ["Jenkins", "GitLab CI", "GitHub Actions", "ArgoCD", "Flux"] },
                { "category": "Monitoring", "items": ["Prometheus", "Grafana", "ELK Stack", "Datadog"] },
                { "category": "OS & Scripting", "items": ["Linux", "Bash", "Python", "Go"] }
              ],
              "projects": [
                {
                  "name": "Open Source: Kubernetes Auto-scaler",
                  "period": "2022 – Hiện tại",
                  "tech": "Go, Kubernetes, Helm",
                  "github": "github.com/khanh-devops/k8s-autoscaler",
                  "details": [
                    "Auto-scaling dựa trên custom metrics (500+ GitHub stars)",
                    "Used bởi 100+ companies worldwide",
                    "Contributed to upstream Kubernetes community"
                  ]
                }
              ],
              "certifications": [
                { "name": "AWS Certified Solutions Architect – Professional", "issuer": "Amazon Web Services", "year": "2023" },
                { "name": "Certified Kubernetes Administrator (CKA)", "issuer": "CNCF", "year": "2021" },
                { "name": "HashiCorp Certified: Terraform Associate", "issuer": "HashiCorp", "year": "2022" }
              ]
            }
            """);
        return t;
    }

    // ── 24. Customer Success & CX ──────────────────────────────────────────
    private CvTemplate buildCustomerSuccessTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Customer Success Manager");
        t.setDescription("Mẫu CV cho Customer Success Manager, Customer Experience Lead. Nổi bật kỹ năng quản lý quan hệ khách hàng, retention và upsell.");
        t.setPreviewColor("#ea580c");
        t.setBadgeLabel("Customer Success");
        t.setBadgeBgColor("#fff7ed");
        t.setBadgeTextColor("#ea580c");
        t.setCategory("Khách hàng & CS");
        t.setStyleTag("professional");
        t.setSortOrder(24);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "PHẠM LÊ THỊ HIỀN",
              "subtitle": "Senior Customer Success Manager | B2B SaaS",
              "email": "hien.cs.manager@gmail.com",
              "phone": "0945 666 777",
              "address": "Tân Bình, TP. Hồ Chí Minh",
              "linkedin": "linkedin.com/in/phamthihien-cs",
              "summary": "Customer Success Manager với 6 năm kinh nghiệm quản lý portfolio 200+ khách hàng doanh nghiệp B2B. Chuyên về customer retention, upsell/cross-sell và building customer advocacy. Đã giúp tăng 30% retention rate và 25% upsell revenue.",
              "education": [
                {
                  "school": "ĐẠI HỌC KINH TẾ TP. HCM (UEH)",
                  "location": "TP. Hồ Chí Minh",
                  "degree": "Cử nhân Quản trị Kinh doanh – Marketing",
                  "period": "2014 – 2018",
                  "details": [
                    "GPA: 3.5/4.0",
                    "Chứng chỉ: Customer Success Management (CSM) – Google Hub (2021)",
                    "Chứng chỉ: Salesforce Administration (2020)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "KATONA",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Senior Customer Success Manager",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Quản lý portfolio 50+ enterprise customers, tổng giá ARR $2M",
                    "Giảm churn rate từ 25% xuống 15% qua proactive customer engagement",
                    "Identify and close upsell opportunities, đạt 120% upsell target 2023",
                    "Train và mentor team 5 CSMs",
                    "Build customer advocacy program với 20+ case studies"
                  ]
                },
                {
                  "company": "SHOPEE",
                  "location": "TP. Hồ Chí Minh",
                  "role": "Customer Success Specialist",
                  "period": "2019 – 2021",
                  "details": [
                    "Manage 200+ active sellers, đảm bảo satisfaction rate > 90%",
                    "Handle escalations và resolve critical issues trong 24h",
                    "Organize quarterly business reviews cho key accounts",
                    "Collaborate with Product team to feedback customer insights"
                  ]
                }
              ],
              "skills": [
                { "category": "Customer Success", "items": ["Customer Onboarding", "Customer Retention", "Churn Management", "Upsell/Cross-sell"] },
                { "category": "Communication", "items": ["Stakeholder Management", "Presentation Skills", "Conflict Resolution", "Empathy"] },
                { "category": "Tools", "items": ["Salesforce", "Gainsight", "HubSpot CRM", "Zendesk"] },
                { "category": "Data Analysis", "items": ["Health Scoring", "Usage Analytics", "NPS Survey", "Churn Prediction"] },
                { "category": "Languages", "items": ["Tiếng Anh (IELTS 7.5)", "Tiếng Việt"] }
              ],
              "achievements": [
                { "name": "Top CSM – Katona All Hands 2023", "year": "2023" },
                { "name": "Best Customer Retention – Shopee CS Team 2020", "year": "2020" }
              ]
            }
            """);
        return t;
    }

    // ── 25. Content Creator ───────────────────────────────────────────────────
    private CvTemplate buildContentCreatorTemplate() {
        CvTemplate t = new CvTemplate();
        t.setName("Content Creator & Social Media Manager");
        t.setDescription("Mẫu CV năng động cho Content Creator, Social Media Manager, Copywriter. Nổi bật portfolio, KPIs social media và skills tạo nội dung.");
        t.setPreviewColor("#db2777");
        t.setBadgeLabel("Creative Content");
        t.setBadgeBgColor("#ffe4e6");
        t.setBadgeTextColor("#db2777");
        t.setCategory("Marketing & Creative");
        t.setStyleTag("creative");
        t.setSortOrder(25);
        t.setActive(true);
        t.setTemplateContent("""
            {
              "name": "NGUYỄN MINH ANH",
              "subtitle": "Content Creator & Social Media Manager | 500K+ Followers",
              "email": "minhanh.content@gmail.com",
              "phone": "0906 111 222",
              "address": "Hà Đông, Hà Nội",
              "linkedin": "linkedin.com/in/nguyenminhanh-content",
              "portfolio": "behance.net/minhanh-content",
              "summary": "Content Creator với 4 năm kinh nghiệm tạo content cho social media và brand campaigns. Có 500K+ followers trên TikTok và 200K+ trên Instagram. Đã hợp tác với 50+ brands lớn như Shopee, Unilever, Samsung. Chuyên về short-form video, copywriting và social media strategy.",
              "education": [
                {
                  "school": "HỌC VIỆT NAM TRƯỜNG THỜNG MỸ THUẬT",
                  "location": "Hà Nội",
                  "degree": "Cử nhân Quan hệ Công chúng – Chuyên ngành Truyền thông Đa phương tiện",
                  "period": "2017 – 2021",
                  "details": [
                    "Tốt nghiệp loại Giỏi",
                    "Dự án tốt nghiệp: Chiến lược Content Marketing cho Startup",
                    "Nghiên cứu về Short-form Video Marketing (TikTok, Reels)"
                  ]
                }
              ],
              "experience": [
                {
                  "company": "FREELANCE CONTENT CREATOR",
                  "location": "Remote",
                  "role": "Social Media Manager & Content Creator",
                  "period": "2021 – Hiện tại",
                  "details": [
                    "Quản lý social media cho 5 brands đồng thời với tổng cộng 1M+ followers",
                    "Sản xuất 100+ videos/tháng, đạt average engagement rate 8%",
                    "Tăng organic followers 300% trong 12 tháng cho brand FMCG",
                    "Collaborate với sales team để drive traffic từ social sang website"
                  ]
                },
                {
                  "company": "DIGITAL AGENCY VN",
                  "location": "Hà Nội",
                  "role": "Content Specialist",
                  "period": "2020 – 2021",
                  "details": [
                    "Viết content cho Facebook Ads, Instagram Ads, TikTok Ads cho 20+ clients",
                    "Sản xuất creative briefs cho design và video production teams",
                    "Phân tích competitors và đề xuất content strategy",
                    "Report monthly performance metrics và optimize content"
                  ]
                }
              ],
              "skills": [
                { "category": "Content Creation", "items": ["Short-form Video (TikTok, Reels)", "Copywriting", "Script Writing", "Video Editing"] },
                { "category": "Social Media", "items": ["TikTok", "Instagram", "Facebook", "YouTube Shorts", "LinkedIn"] },
                { "category": "Tools", "items": ["CapCut", "Premiere Pro", "After Effects", "Canva", "Hootsuite"] },
                { "category": "Analytics", "items": ["Instagram Insights", "TikTok Analytics", "Facebook Analytics", "Google Analytics"] },
                { "category": "Soft Skills", "items": ["Creativity", "Trend-spotting", "Brand Voice", "Storytelling"] }
              ],
              "portfolio": [
                {
                  "name": "TikTok Series – Cooking Hacks",
                  "period": "2023",
                  "platform": "TikTok",
                  "details": [
                    "10 videos, 5M+ total views",
                    "Series alcanzou 500K followers trong 3 tháng",
                    "Average engagement rate 12%"
                  ]
                },
                {
                  "name": "Instagram Reels – Fashion Tips",
                  "period": "2022 – 2023",
                  "platform": "Instagram",
                  "details": [
                    "30 reels, 2M+ total views",
                    "Collab với 10 fashion brands",
                    "100K+ link clicks to product pages"
                  ]
                }
              ],
              "achievements": [
                { "name": "Top Creator – TikTok Vietnam Awards 2023", "year": "2023" },
                { "name": "Best Content Creator – Digital Agency VN 2022", "year": "2022" }
              ]
            }
            """);
        return t;
    }
}
