package Nhom08.Project.service;

import Nhom08.Project.entity.CareerGuideArticle;
import Nhom08.Project.entity.CareerGuideCategory;
import Nhom08.Project.entity.CareerGuideTag;
import Nhom08.Project.repository.CareerGuideArticleRepository;
import Nhom08.Project.repository.CareerGuideCategoryRepository;
import Nhom08.Project.repository.CareerGuideTagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CareerGuideSeeder implements CommandLineRunner {

    private final CareerGuideCategoryRepository categoryRepository;
    private final CareerGuideTagRepository tagRepository;
    private final CareerGuideArticleRepository articleRepository;

    public CareerGuideSeeder(
            CareerGuideCategoryRepository categoryRepository,
            CareerGuideTagRepository tagRepository,
            CareerGuideArticleRepository articleRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
        seedTags();
        seedArticles();
    }

    private void seedCategories() {
        seedCategory("Bí quyết tìm việc", "bi-quyet-tim-viec", "Mẹo ứng tuyển, CV, phỏng vấn và cách tạo lợi thế cạnh tranh.", "fa-solid fa-magnifying-glass", "#1d4ed8", 1);
        seedCategory("Con đường sự nghiệp", "con-duong-su-nghiep", "Lộ trình phát triển, chuyển việc và kỹ năng để tiến xa hơn.", "fa-solid fa-road", "#0f766e", 2);
        seedCategory("Thị trường lao động", "thi-truong-lao-dong", "Dữ liệu thị trường, xu hướng tuyển dụng và mức lương.", "fa-solid fa-chart-line", "#1e3a8a", 3);
        seedCategory("Wiki Career", "wiki-career", "Khái niệm, thuật ngữ và kiến thức nền cho người đi làm.", "fa-solid fa-book-open", "#0369a1", 4);
        seedCategory("Thư giãn", "thu-gian", "Góc nhìn nhẹ nhàng về sức khỏe tinh thần và nhịp sống công sở.", "fa-solid fa-leaf", "#0f766e", 5);
        seedCategory("Sự kiện nghề nghiệp", "su-kien-nghe-nghiep", "Hội thảo, webinar, career fair và các hoạt động cộng đồng.", "fa-solid fa-calendar-check", "#b45309", 6);
        seedCategory("Góc đối tác", "goc-doi-tac", "Câu chuyện thương hiệu nhà tuyển dụng, hợp tác và case study.", "fa-solid fa-handshake", "#075985", 7);
    }

    private void seedTags() {
        seedTag("ATS", "ats", 1);
        seedTag("CV", "cv", 2);
        seedTag("Phỏng vấn", "phong-van", 3);
        seedTag("Lương thưởng", "luong-thuong", 4);
        seedTag("Kỹ năng", "ky-nang", 5);
        seedTag("Quản lý", "quan-ly", 6);
        seedTag("Xu hướng", "xu-huong", 7);
        seedTag("Sức khỏe tinh thần", "suc-khoe-tinh-than", 8);
        seedTag("Networking", "networking", 9);
        seedTag("Employer Branding", "employer-branding", 10);
    }

    private void seedArticles() {
        // Clear existing articles to avoid duplicates when reseeding
        articleRepository.deleteAll();

        // Map existing image files to categories
        // Available images: guide-ats.svg, guide-default.svg, guide-event.svg, guide-growth.svg,
        // guide-interview.svg, guide-market.svg, guide-partner.svg, guide-relax.svg,
        // guide-salary.svg, guide-wiki.svg

        // Temporarily disable check to reseed with 70 articles
        // if (articleRepository.count() > 0) {
        //     return;
        // }

        CareerGuideCategory jobTips = requireCategory("bi-quyet-tim-viec");
        CareerGuideCategory careerPath = requireCategory("con-duong-su-nghiep");
        CareerGuideCategory laborMarket = requireCategory("thi-truong-lao-dong");
        CareerGuideCategory wiki = requireCategory("wiki-career");
        CareerGuideCategory wellbeing = requireCategory("thu-gian");
        CareerGuideCategory events = requireCategory("su-kien-nghe-nghiep");
        CareerGuideCategory partner = requireCategory("goc-doi-tac");

        CareerGuideTag ats = requireTag("ats");
        CareerGuideTag cv = requireTag("cv");
        CareerGuideTag interview = requireTag("phong-van");
        CareerGuideTag salary = requireTag("luong-thuong");
        CareerGuideTag skill = requireTag("ky-nang");
        CareerGuideTag growth = requireTag("quan-ly");
        CareerGuideTag trend = requireTag("xu-huong");
        CareerGuideTag mental = requireTag("suc-khoe-tinh-than");
        CareerGuideTag network = requireTag("networking");
        CareerGuideTag employerBrand = requireTag("employer-branding");

        List<SeedArticle> articles = new ArrayList<>();

        // Gọi các helper methods để lấy 10 bài viết cho mỗi category
        articles.addAll(seedJobTipsArticles(jobTips, ats, cv, interview, skill, trend));
        articles.addAll(seedCareerPathArticles(careerPath, interview, skill, salary, growth, mental));
        articles.addAll(seedLaborMarketArticles(laborMarket, trend, salary, growth));
        articles.addAll(seedWikiArticles(wiki, skill, growth, trend));
        articles.addAll(seedWellbeingArticles(wellbeing, mental, growth, salary));
        articles.addAll(seedEventsArticles(events, network, skill, growth));
        articles.addAll(seedPartnerArticles(partner, employerBrand, network, growth));

        // Lưu tất cả bài viết
        for (SeedArticle seed : articles) {
            if (articleRepository.findBySlug(seed.slug()).isPresent()) {
                continue;
            }

            CareerGuideArticle article = new CareerGuideArticle();
            article.setCategory(seed.category());
            article.setSlug(seed.slug());
            article.setTitle(seed.title());
            article.setExcerpt(seed.excerpt());
            article.setContentHtml(seed.contentHtml());
            article.setCoverImageUrl(seed.coverImageUrl());
            article.setCoverImageAlt(seed.coverImageAlt());
            article.setAuthorName(seed.authorName());
            article.setStatus(CareerGuideArticle.ArticleStatus.PUBLISHED);
            article.setFeatured(seed.featured());
            article.setPinned(seed.pinned());
            article.setReadTimeMinutes(seed.readTimeMinutes());
            article.setViews(seed.views());
            article.setPublishedAt(seed.publishedAt());
            article.setTags(new ArrayList<>(seed.tags()));
            articleRepository.save(article);
        }
    }

    // ==================== HELPER METHODS ====================

    private List<SeedArticle> seedJobTipsArticles(CareerGuideCategory category, CareerGuideTag ats, CareerGuideTag cv, CareerGuideTag interview, CareerGuideTag skill, CareerGuideTag trend) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 1;

        // Bài 1: 7 dấu hiệu CV bị ATS loại
        articles.add(new SeedArticle(
            "7-dau-hieu-cv-dang-bi-ats-lo-tu-vong-dau",
            "7 dấu hiệu CV đang bị ATS loại từ vòng đầu",
            category,
            "Những lỗi định dạng, nội dung và từ khóa khiến hồ sơ khó vượt qua hệ thống ATS dù ứng viên có kinh nghiệm tốt.",
            "<p>ATS không phải là kẻ thù, nhưng nó là một bộ lọc rất khắt khe. CV đẹp mắt chưa chắc đã qua được máy đọc nếu bố cục rối, tiêu đề mơ hồ hoặc thiếu từ khóa khớp với JD.</p><h2>Những lỗi thường gặp</h2><ul><li>CV dùng nhiều cột, icon hoặc khối hình phức tạp.</li><li>Tiêu đề kinh nghiệm không rõ chức danh.</li><li>Thiếu số liệu đo lường thành tích.</li><li>Không có từ khóa kỹ năng theo mô tả công việc.</li></ul><p>Nếu bạn muốn tối ưu cho ATS, hãy ưu tiên cấu trúc rõ ràng, tiêu đề nhất quán và thành tích có số liệu.</p>",
            "/images/career-guide/guide-ats.svg",
            "Ảnh minh họa CV thân thiện với ATS",
            "Ban biên tập CoHoiViecLam",
            true,
            true,
            4,
            3012L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(ats, cv)
        ));

        // Bài 2: 5 câu hỏi phỏng vấn hành vi
        articles.add(new SeedArticle(
            "5-cau-hoi-phong-van-hanh-vi-can-chuan-bi-truoc",
            "5 câu hỏi phỏng vấn hành vi mà ứng viên nên chuẩn bị trước",
            category,
            "Khung trả lời STAR gọn gàng để bạn kể kinh nghiệm, xử lý tình huống và chứng minh năng lực thay vì chỉ nói chung chung.",
            "<p>Phỏng vấn hành vi không kiểm tra câu chữ hay sự trôi chảy đơn thuần. Nhà tuyển dụng muốn xem cách bạn ra quyết định, phản ứng khi áp lực và học gì từ những tình huống trước đây.</p><h2>Khung STAR</h2><p>Situation - Task - Action - Result là cấu trúc ngắn gọn nhất để câu trả lời không bị lan man. Mỗi ví dụ nên có bối cảnh, mục tiêu, hành động của bạn và kết quả đo được.</p><h2>5 câu hỏi cần chuẩn bị</h2><ol><li>Hãy kể về một lần bạn giải quyết xung đột trong team.</li><li>Hãy mô tả một dự án thất bại và bài học rút ra.</li><li>Hãy kể về lần bạn phải xử lý deadline rất gấp.</li><li>Hãy cho ví dụ về việc bạn tạo ảnh hưởng khi không có quyền lực chính thức.</li><li>Hãy nói về một lần bạn học nhanh một kỹ năng mới.</li></ol>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa phỏng vấn hành vi",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            740L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(interview, skill)
        ));

        // Bài 3: CV 1 trang hay 2 trang
        articles.add(new SeedArticle(
            "cv-1-trang-hay-2-trang-chuan-nha-tuyen-dung",
            "CV 1 trang hay 2 trang? Chuẩn nào nhà tuyển dụng ưu tiên?",
            category,
            "Phân tích ưu nhược điểm của CV 1 trang và 2 trang theo cấp bậc, ngành nghề và kinh nghiệm để đưa ra quyết định phù hợp.",
            "<p>Không có đáp án đúng tuyệt đối cho việc CV nên dài bao nhiêu. Vấn đề là độ dài có tương ứng với giá trị bạn mang lại không.</p><h2>Khi nào nên dùng CV 1 trang?</h2><ul><li>Người mới ra trường hoặc dưới 2 năm kinh nghiệm.</li><li>Ứng tuyển vị trí junior, intern hoặc thực tập.</li></ul><h2>Khi nào CV 2 trang chấp nhận được?</h2><ul><li>3 năm kinh nghiệm trở lên với thành tích đáng kể.</li><li>Ứng tuyển vị trí senior, manager hoặc chuyên ngành cần liệt kê dự án.</li></ul>",
            "/images/career-guide/guide-ats.svg",
            "Ảnh minh họa CV 1 trang và 2 trang",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1540L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(cv, skill)
        ));

        // Bài 4: 5 lỗi email ứng tuyển
        articles.add(new SeedArticle(
            "5-loi-pho-bien-khi-viet-email-ung-tuyen",
            "5 lỗi phổ biến khi viết email ứng tuyển mà ít ai nhận ra",
            category,
            "Những sai sót trong subject line, cách xưng hô, file đính kèm và văn phong khiến email của bạn bị bỏ qua dù CV tốt.",
            "<p>Email ứng tuyển là điểm chạm đầu tiên với nhà tuyển dụng. Nhiều ứng viên có CV tốt nhưng mất cơ hội ngay từ vòng gửi email vì những lỗi cơ bản.</p><h2>Lỗi 1: Subject line không rõ ràng</h2><p>Tránh subject mơ hồ. Hãy ghi rõ: [Vị trí] - [Họ tên] - [Kinh nghiệm].</p><h2>Lỗi 2: File đính kèm không đúng tên</h2><p>Đừng gửi file tên 'CV_final_final.pdf'. Tên file nên chứa: 'CV_HoTen_ViTri.pdf'.</p>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa viết email ứng tuyển",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            4,
            890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(cv, interview)
        ));

        // Bài 5: Nghiên cứu doanh nghiệp
        articles.add(new SeedArticle(
            "nghien-cuu-doanh-nghiep-truoc-khi-ung-tuyen-dung",
            "Nghiên cứu doanh nghiệp trước khi ứng tuyển: Dùng gì và tra ở đâu?",
            category,
            "Hướng dẫn tìm hiểu về công ty mục tiêu: văn hóa, tài chính, đội ngũ lãnh đạo và đánh giá từ nhân viên hiện tại.",
            "<p>Ứng viên được chuẩn bị thường chiếm lợi thế lớn ở vòng phỏng vấn. Nhưng không phải ai cũng biết cách nghiên cứu công ty một cách hiệu quả.</p><h2>Nguồn thông tin nên kiểm tra</h2><ol><li>Website chính thức: Sản phẩm, dịch vụ, giá trị cốt lõi.</li><li>LinkedIn: Đội ngũ lãnh đạo, quy mô.</li><li>Review nhân viên: Glassdoor, CoHoiViecLam.</li></ol>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa nghiên cứu công ty",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(interview, skill)
        ));

        // Bài 6: Xử lý câu hỏi điểm yếu
        articles.add(new SeedArticle(
            "xu-li-cau-hoi-ve-diem-yeu-trong-phong-van",
            "Xử lý câu hỏi về điểm yếu trong phỏng vấn mà không tự làm mất điểm",
            category,
            "Cách trả lời câu hỏi 'điểm yếu lớn nhất' một cách trung thực nhưng không khiến nhà tuyển dụng nghi ngờ năng lực.",
            "<p>Câu hỏi về điểm yếu là một trong những câu hóc búa nhất trong phỏng vấn. Mục đích không phải là khai thác lỗi lầm, mà xem cách bạn tự nhận thức và cải thiện.</p><h2>Ví dụ tốt</h2><p>'Tôi từng yếu trong thuyết trình trước đám đông, nhưng đã đăng ký khóa học online và chủ động present trong team meeting mỗi tháng.'</p>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa trả lời điểm yếu",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            2340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(interview)
        ));

        // Bài 7: Làm việc remote
        articles.add(new SeedArticle(
            "lam-viec-remote-van-de-can-biet-truoc-khi-chap-nhan",
            "Làm việc remote: Vấn đề cần biết trước khi chấp nhận công việc từ xa",
            category,
            "Những thứ ít ai nói về remote: cách phối hợp, giao tiếp async, quản lý thời gian và đánh lương công bằng.",
            "<p>Remote nghe có vẻ hấp dẫn, nhưng không phải ai cũng phù hợp và không phải công ty nào cũng làm remote tốt.</p><h2>Câu hỏi nên hỏi</h2><ol><li>Có bao nhiêu người làm remote? Tỷ lệ remote/onsite?</li><li>Có yêu cầu timezone cụ thể không?</li><li>Làm sao theo dõi performance?</li></ol>",
            "/images/career-guide/guide-default.svg",
            "Ảnh minh họa làm việc từ xa",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1870L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, trend)
        ));

        // Bài 8: Tại sao CV không phản hồi
        articles.add(new SeedArticle(
            "tai-sao-cv-cua-ban-khong-co-phan-hoi",
            "Tại sao CV của bạn không có phản hồi? 7 lý do phổ biến",
            category,
            "Phân tích những lý do khiến hồ sơ bị bỏ qua: từ format, nội dung đến cách bạn gửi và thời điểm ứng tuyển.",
            "<p>Gửi nhiều CV nhưng không thấy phản hồi là trải nghiệm phổ biến. Nhưng lý do thường nằm ở những chi tiết nhỏ hơn bạn nghĩ.</p><h2>Lý do 1: Không phù hợp với JD</h2><p>Nếu JD yêu cầu 3 năm kinh nghiệm Python mà bạn chỉ có Java, hãy cân nhắc kỹ.</p><h2>Lý do 2: CV không được tối ưu cho ATS</h2><p>Hệ thống lọc hồ sơ có thể loại CV của bạn nếu format phức tạp.</p>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa CV không phản hồi",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            3100L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(cv, ats)
        ));

        // Bài 9: Soft skills
        articles.add(new SeedArticle(
            "danh-sach-ky-nang-soft-skills-can-co-2026",
            "Danh sách kỹ năng soft skills cần có 2026 (kèm cách chứng minh)",
            category,
            "Những kỹ năng mềm được nhà tuyển dụng săn đón nhất năm nay và cách thể hiện chúng trong CV và phỏng vấn.",
            "<p>Hard skills giúp bạn được nhận, nhưng soft skills quyết định bạn được giữ lại và thăng tiến.</p><h2>1. Giao tiếp và thuyết phục</h2><p>Chứng minh: Ví dụ về lần bạn persuade stakeholder.</p><h2>2. Làm việc nhóm cross-functional</h2><p>Chứng minh: Dự án bạn phối hợp với dev, design, marketing.</p>",
            "/images/career-guide/guide-interview.svg",
            "Ảnh minh họa kỹ năng mềm",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1420L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, trend)
        ));

        // Bài 10: Tối ưu LinkedIn
        articles.add(new SeedArticle(
            "toi-uu-linkedin-de-tuyen-dung-tim-den-ban",
            "Tối ưu LinkedIn để tuyển dụng tìm đến bạn (thay vì bạn đi tìm việc)",
            category,
            "Cách xây dựng profile LinkedIn attractive, từ headline, about section đến portfolio và activity.",
            "<p>LinkedIn không chỉ là nơi nộp đơn, mà còn là kênh marketing bản thân hiệu quả nhất.</p><h2>Headline không chỉ là 'Job Title at Company'</h2><p>Thay vì 'Software Engineer', hãy dùng 'Software Engineer | Building scalable systems | Open to opportunities'.</p>",
            "/images/career-guide/guide-ats.svg",
            "Ảnh minh họa tối ưu LinkedIn",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            980L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, trend)
        ));

        return articles;
    }

    private List<SeedArticle> seedCareerPathArticles(CareerGuideCategory category, CareerGuideTag interview, CareerGuideTag skill, CareerGuideTag salary, CareerGuideTag growth, CareerGuideTag mental) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 20;

        // Bài 1: Nghỉ việc trước hay tìm việc trước
        articles.add(new SeedArticle(
            "nghi-viec-truoc-hay-tim-viec-truoc-quyet-dinh-chien-luoc",
            "Nghỉ việc trước hay tìm việc trước? Quyết định chiến lược cho bước chuyển nghề",
            category,
            "So sánh hai kịch bản chuyển việc phổ biến để ứng viên chọn được thời điểm phù hợp.",
            "<p>Quyết định nghỉ việc trước hay tìm việc trước không có câu trả lời đúng tuyệt đối.</p><h2>Khi nào nên tìm việc trước?</h2><ul><li>Bạn vẫn đang ổn định tài chính.</li><li>Vai trò hiện tại cho phép bạn chủ động chuẩn bị.</li></ul>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa chuyển hướng sự nghiệp",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            5,
            1640L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(interview, skill)
        ));

        // Thêm 9 bài nữa...
        articles.add(new SeedArticle(
            "khi-nen-chuyen-viec-voi-3-nam-kinh-nghiem",
            "Khi nào nên chuyển việc với 3 năm kinh nghiệm? Đánh giá đúng thời điểm",
            category,
            "Phân tích 3 năm là giai đoạn quan trọng để quyết định: gắn bó hay tìm cơ hội mới.",
            "<p>3 năm là milestone nhiều nhân viên đứng giữa ngã ba: tiếp tục hay chuyển sang công ty khác.</p><h2>3 dấu hiệu nên đi tiếp</h2><ul><li>Bạn đang mentor người khác.</li><li>Có lộ trình thăng tiến rõ.</li></ul>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa 3 năm kinh nghiệm",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            2130L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, salary)
        ));

        articles.add(new SeedArticle(
            "junior-den-senior-lo-trinh-phat-trien-nghiep",
            "Từ Junior đến Senior: Lộ trình phát triển nghề nghiệp thực tế",
            category,
            "Mô tả các giai đoạn từ Junior, Mid-level đến Senior và những kỹ năng cần ở mỗi level.",
            "<p>Để tiến từ Junior lên Senior không chỉ là kỹ năng chuyên môn, mà còn là tư duy hệ thống và khả năng mentor.</p><h2>Junior (0-2 năm)</h2><p>Tập trung vào execution, learning và hỏi thông minh.</p><h2>Mid-level (2-5 năm)</h2><p>Bắt đầu own feature, mentor junior và participate in architectural discussion.</p>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa lộ trình Junior-Senior",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            8,
            2890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth, skill)
        ));

        articles.add(new SeedArticle(
            "ky-nang-quan-ly-thoi-gian-cho-nguoi-di-lam",
            "Kỹ năng quản lý thời gian cho người đi làm: Từ theory đến practice",
            category,
            "Các phương pháp quản lý thời gian hiệu quả: Pomodoro, Time blocking, Eisenhower matrix.",
            "<p>Quản lý thời gian không phải là làm nhiều việc hơn, mà là làm đúng việc vào đúng thời điểm.</p><h2>1. Pomodoro Technique</h2><p>25 phút tập trung + 5 phút break. Sau 4 cycles, nghỉ dài 15-30 phút.</p><h2>2. Time Blocking</h2><p>Chia lịch làm việc thành các block dedicated cho mỗi task type.</p>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa quản lý thời gian",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        articles.add(new SeedArticle(
            "xay-dung-brand-ca-nhan-trong-nganh-nghe-cua-ban",
            "Xây dựng brand cá nhân trong ngành nghề của bạn",
            category,
            "Tại sao brand cá nhân quan trọng và cách xây dựng nó một cách có hệ thống.",
            "<p>Brand cá nhân không chỉ là cho influencer. Mọi professional đều cần một brand mạnh để attract opportunities.</p><h2>3 bước build brand</h2><ol><li>Định vị: Bạn là expert trong gì?</li><li>Visible: Share knowledge qua LinkedIn, blog, speaking.</li><li>Consistent: Show up regularly với value.</li></ol>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa brand cá nhân",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1240L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        articles.add(new SeedArticle(
            "negotiate-luong-huong-dan-cho-applicant-viet-nam",
            "Negotiate lương: Hướng dẫn cho applicant Việt Nam (đụng chạm culture)",
            category,
            "Cách thương lượng lương một cách professional, khéo léo và phù hợp văn hóa Việt Nam.",
            "<p>Nhiều người Việt ngại negotiate lương vì sợ mất lòng hoặc mất cơ hội. Nhưng nếu làm đúng cách, negotiate cho thấy bạn value yourself và understand market.</p><h2>Timing</h2><p>Negotiate sau khi họ offer, không phải trong first screen. Cho thấy enthusiasm rồi mới discuss compensation.</p>",
            "/images/career-guide/guide-salary.svg",
            "Ảnh minh họa thương lượng lương",
            "Ban biên tập CoHoiViecLam",
            true,
            true,
            6,
            3420L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(salary, interview)
        ));

        articles.add(new SeedArticle(
            "phan-biet-promotion-va-raise-luong",
            "Phân biệt Promotion và Raise lương: Khi nào push từng cái?",
            category,
            "Hiểu rõ difference giữa promotion (title change) và raise (salary increase) để strategize career move.",
            "<p>Promotion và raise thường đi cùng nhau, nhưng không phải lúc nào cũng vậy. Đôi khi bạn có thể negotiate raise mà không cần promotion, hoặc promotion sans raise.</p><h2>When to push for promotion?</h2><ul><li>Đã consistently exceed expectation trong 6-12 tháng.</li><li>Đang doing job level above current pay grade.</li></ul>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa promotion vs raise",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth, salary)
        ));

        articles.add(new SeedArticle(
            "burnout-la-gi-va-prevent-like-a-pro",
            "Burnout là gì và prevent like a pro (trước khi quá muộn)",
            category,
            "Nhận biết early signs of burnout và strategies để prevent và recover.",
            "<p>Burnout không phải là lazy hay lack of motivation. Nó là state của emotional, physical, and mental exhaustion caused by excessive and prolonged stress.</p><h2>Early signs</h2><ul><li>Cynical về work.</li><li>Difficulty concentrating.</li><li>Physical symptoms: insomnia, headaches.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa burnout",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            2150L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth, mental)
        ));

        articles.add(new SeedArticle(
            "mentor-vs-coach-su-khac-biet-quan-trong",
            "Mentor vs Coach: Sự khác biệt quan trọng cho career growth",
            category,
            "Hiểu rõ difference và biết khi nào nên tìm mentor, khi nào cần coach.",
            "<p>Nhiều người dùng interchangeably, nhưng mentor và serve different purposes ở different stages của career.</p><h2>Mentor</h2><p>Someone who's been there, done that. Share experience, wisdom, network. Long-term relationship.</p><h2>Coach</h2><p>Someone trained to help you think through problems. Ask powerful questions. Short-term engagement.</p>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa mentor vs coach",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1320L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth, skill)
        ));

        articles.add(new SeedArticle(
            "side-project-while-working-full-time-worth-it",
            "Side project while working full-time: Worth it hay không?",
            category,
            "Phân tích pros và cons của having side job và khi nào nên pursue.",
            "<p>Side project có thể accelerate learning, increase income, nhưng cũng có risk burnout và conflict với main job.</p><h2>Pros</h2><ul><li>Learn new skills faster.</li><li>Potential additional income.</li><li>Safety net nếu main job gặp vấn đề.</li></ul><h2>Cons</h2><ul><li>Less personal time.</li><li>Potential conflict of interest.</li></ul>",
            "/images/career-guide/guide-growth.svg",
            "Ảnh minh họa side project",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth, skill)
        ));

        return articles;
    }

    private List<SeedArticle> seedLaborMarketArticles(CareerGuideCategory category, CareerGuideTag trend, CareerGuideTag salary, CareerGuideTag growth) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 30;

        articles.add(new SeedArticle(
            "top-5-nganh-du-doan-khat-nhan-luc-2026-tai-viet-nam",
            "Top 5 ngành dự đoán khát nhân lực 2026 tại Việt Nam",
            category,
            "Phân tích những lĩnh vực đang tăng tốc tuyển dụng, vì sao nhu cầu nhân sự tiếp tục cao và ứng viên nên chuẩn bị gì.",
            "<p>Năm 2026 tiếp tục là giai đoạn thị trường lao động tái cấu trúc theo nhu cầu số hóa.</p><h2>1. Công nghệ và dữ liệu</h2><p>Vai trò liên quan đến phát triển sản phẩm số, dữ liệu và hạ tầng vẫn giữ nhịp tuyển dụng ổn định.</p>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa xu hướng thị trường",
            "Ban biên tập CoHoiViecLam",
            true,
            true,
            6,
            2180L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        articles.add(new SeedArticle(
            "cap-nhat-muc-luong-co-so-nam-2026-theo-quy-dinh",
            "Cập nhật mức lương cơ sở năm 2026 theo quy định mới nhất",
            category,
            "Tóm tắt những điểm ứng viên và người lao động cần lưu ý khi cập nhật mức lương cơ sở.",
            "<p>Khi lương cơ sở thay đổi, tác động không chỉ nằm ở phần lương tháng.</p><h2>Người lao động cần theo dõi gì?</h2><ul><li>Mức lương ghi trên hợp đồng lao động.</li><li>Các khoản đóng bảo hiểm và phúc lợi liên quan.</li></ul>",
            "/images/career-guide/guide-salary.svg",
            "Ảnh minh họa lương cơ sở",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            5,
            1430L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(salary, trend)
        ));

        // Thêm 8 bài nữa cho labor market...
        articles.add(new SeedArticle(
            "salary-guide-2026-it-marketing-sales-finance",
            "Salary Guide 2026: IT, Marketing, Sales, Finance",
            category,
            "Bảng tham khảo mức lương theo vị trí và level kinh nghiệm cho 4 nhóm ngành chính.",
            "<p>Dưới đây là salary ranges cho common positions ở major cities (HCM, Hanoi).</p><h2>IT/Tech</h2><p>Junior: 10-15M | Mid: 15-25M | Senior: 25-45M | Lead: 40M+</p><h2>Marketing</h2><p>Junior: 8-12M | Mid: 12-20M | Senior: 20-35M | Manager: 30M+</p>",
            "/images/career-guide/guide-salary-guide.svg",
            "Ảnh minh họa salary guide",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            7,
            4560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(salary)
        ));

        articles.add(new SeedArticle(
            "remote-work-trend-2026-viet-nam-market",
            "Remote Work Trend 2026: Việt Nam Market đang thay đổi như thế nào?",
            category,
            "Phân tích xu hướng làm việc từ xa tại Việt Nam và哪些 industries đang lead.",
            "<p>Remote work ở Việt Nam đã increase significantly post-pandemic, nhưng vẫn vary by industry.</p><h2>Industries với high remote rate</h2><ul><li>Tech/IT: 60-80% remote/hybrid.</li><li>Marketing Agency: 40-60%.</li><li>Finance/Banking: 10-20% (mostly hybrid).</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa remote work trend",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            2340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        articles.add(new SeedArticle(
            "ai-automation-impact-jobs-vietnam-2026",
            "AI & Automation Impact on Jobs in Vietnam 2026",
            category,
            "Cách AI và automation đang reshape job market và哪些 roles đang emerging vs declining.",
            "<p>AI không phải là threat duy nhất, mà là force accelerating change trong job market.</p><h2>Roles being augmented</h2><ul><li>Customer Service: Chatbot handle routine queries.</li><li>Content Writing: AI assist với draft và ideation.</li></ul><h2>Emerging roles</h2><ul><li>AI Trainer, Prompt Engineer.</li><li>Data Analyst với AI literacy.</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa AI impact",
            "Ban biên tập CoHoiViecLam",
            true,
            true,
            7,
            3120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        articles.add(new SeedArticle(
            "gig-economy-viet-nam-freelancer-trends",
            "Gig Economy Việt Nam: Freelancer Trends & Opportunities",
            category,
            "Sự rise của gig economy và implications cho career planning.",
            "<p>Freelancing và gig work đang become increasingly viable career path ở Việt Nam.</p><h2>Popular freelance categories</h2><ul><li>Content creation, Copywriting.</li><li>Graphic design, Video editing.</li><li>Web development, App development.</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa gig economy",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1870L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        articles.add(new SeedArticle(
            "green-jobs-sustainable-development-vietnam-2026",
            "Green Jobs & Sustainable Development: Career Opportunities 2026",
            category,
            "Những emerging roles trong sustainability, renewable energy và ESG.",
            "<p>Climate change và net-zero commitments đang drive demand cho green jobs.</p><h2>Key sectors</h2><ul><li>Renewable energy: Solar, wind installation và maintenance.</li><li>ESG consulting: Helping companies meet sustainability goals.</li><li>Green supply chain: Sustainable sourcing và logistics.</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa green jobs",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        articles.add(new SeedArticle(
            "startup-ecosystem-vietnam-hiring-trends",
            "Startup Ecosystem Vietnam 2026: Hiring Trends & Salary",
            category,
            "Insights về hiring practices, compensation packages trong startups.",
            "<p>Startups ở Việt Nam đang recover post-funding winter và hiring again với different expectations.</p><h2>What's different about startup jobs?</h2><ul><li>More roles: Less specialized, more generalist.</li><li>Compensation: Lower base, higher equity upside.</li><li>Culture: Faster pace, more ambiguity.</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa startup hiring",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend, salary)
        ));

        articles.add(new SeedArticle(
            "labor-shortage-vietnam-2026-causes-solutions",
            "Labor Shortage in Vietnam 2026: Causes & Solutions",
            category,
            "Phân tích causes của current labor shortage và implications cho job seekers.",
            "<p>Vietnam đang face paradoxical situation: high unemployment nhưng labor shortage trong key sectors.</p><h2>Causes</h2><ul><li>Skill mismatch: Education not aligned với industry needs.</li><li>Brain drain: Top talent moving overseas.</li><li>Demographic shifts: Aging population trong some sectors.</li></ul>",
            "/images/career-guide/guide-market.svg",
            "Ảnh minh họa labor shortage",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            2130L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(trend)
        ));

        return articles;
    }

    private List<SeedArticle> seedWikiArticles(CareerGuideCategory category, CareerGuideTag skill, CareerGuideTag growth, CareerGuideTag trend) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 40;

        articles.add(new SeedArticle(
            "ban-do-nghe-nghiep-cho-nguoi-moi-di-lam",
            "Bản đồ nghề nghiệp cho người mới đi làm",
            category,
            "Gợi ý cách đọc thị trường, chọn kỹ năng và xây nền tảng để không bị đứng im sau năm đầu tiên.",
            "<p>Người mới đi làm thường bị choáng bởi quá nhiều lựa chọn.</p><h2>3 lớp cần xây</h2><ol><li>Lớp nền tảng: giao tiếp, quản lý thời gian.</li><li>Lớp chuyên môn: nghiệp vụ cốt lõi.</li><li>Lớp tăng trưởng: dữ liệu, tư duy sản phẩm.</li></ol>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa bản đồ nghề nghiệp",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            4,
            905L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        // Thêm 9 bài nữa cho Wiki...
        articles.add(new SeedArticle(
            "kpi-okr-metrics-ban-can-biet-ve-performance-measurement",
            "KPI, OKR & Metrics: Bạn cần biết về Performance Measurement",
            category,
            "Giải thích common performance measurement frameworks và cách chúng được used trong workplaces.",
            "<p>Understanding performance measurement is critical cho career advancement.</p><h2>KPI (Key Performance Indicators)</h2><p>Metrics cụ thể đo lường performance vs goals. Usually quarterly hoặc annually.</p><h2>OKR (Objectives và Key Results)</h2><p>Framework goal-setting phổ biến trong tech. Focus trên ambitious, measurable goals.</p>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa KPI & OKR",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        articles.add(new SeedArticle(
            "org-chart-corporate-structure-hierarchy-explained",
            "Org Chart & Corporate Structure: Hierarchy explained cho新人",
            category,
            "Understanding typical organizational structures và cách navigate chúng.",
            "<p>Knowing org structure helps bạn understand reporting lines, decision-making flow và career paths.</p><h2>Common structures</h2><ul><li>Functional: Marketing, Sales, Tech as separate verticals.</li><li>Divisional: By product line hoặc geography.</li><li>Matrix: Dual reporting (function + project).</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa org chart",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1230L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill)
        ));

        articles.add(new SeedArticle(
            "full-time-part-time-contract-freelance-differences",
            "Full-time, Part-time, Contract, Freelance: Differences explained",
            category,
            "Compare các types của employment arrangements và pros/cons của mỗi loại.",
            "<p>Understanding employment types helps bạn make informed career decisions.</p><h2>Full-time</h2><p>Standard employment: 40h/week, benefits, job security.</p><h2>Contract</h2><p>Fixed-term, higher hourly rate, less benefits, more flexibility.</p>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa employment types",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill)
        ));

        articles.add(new SeedArticle(
            "onboarding-checklist-first-90-days-new-job",
            "Onboarding Checklist: First 90 Days ở New Job",
            category,
            "Comprehensive guide cho first 3 months ở new role để set yourself up cho success.",
            "<p>First 90 days là critical period cho forming impressions và building foundation.</p><h2>Days 1-30: Learn</h2><ul><li>Understand role, team dynamics, unwritten rules.</li><li>Build relationships với key stakeholders.</li></ul><h2>Days 31-60: Contribute</h2><ul><li>Start owning small tasks.</li><li>Identify quick wins.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa onboarding",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            7,
            2890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        articles.add(new SeedArticle(
            "performance-review-phong-danh-gia-hieu-qua",
            "Performance Review: Phong đánh giá hiệu quả explained",
            category,
            "Mọi thứ bạn cần biết về performance reviews và cách navigate chúng successfully.",
            "<p>Performance reviews là critical cho salary increases, promotions và job security.</p><h2>Types của reviews</h2><ul><li>Annual: Comprehensive evaluation, usually tied compensation.</li><li>360-degree: Feedback từ manager, peers, direct reports.</li><li>Project-based: Post-project debrief.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa performance review",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(growth)
        ));

        articles.add(new SeedArticle(
            "company-benefits-package-insurance-perks-explained",
            "Company Benefits Package: Insurance, perks explained",
            category,
            "Understanding typical benefits packages và cách evaluate chúng khi considering job offers.",
            "<p>Benefits có thể account cho 20-30% của total compensation, nhưng often overlooked.</p><h2>Common benefits</h2><ul><li>Health insurance: BHXH, private insurance.</li><li>Leave: Annual, sick, maternity, paternity.</li><li>Perks: Lunch allowance, phone, laptop.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa company benefits",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill)
        ));

        articles.add(new SeedArticle(
            "probation-period-thu-viet-uu-diem-nguy-co",
            "Probation Period (Thử việc): Ưu điểm & Nguy cơ",
            category,
            "Everything bạn cần know về probation periods trong Vietnam labor context.",
            "<p>Probation là trial period cho cả employer và employee to test fit.</p><h2>Legal framework</h2><ul><li>Max duration: 2 months cho professional roles, 3 months cho manager roles.</li><li>85% salary.</li><li>Can terminate với 3 days notice.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa probation period",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill)
        ));

        articles.add(new SeedArticle(
            "termination-resignation-vietnam-labor-law",
            "Termination & Resignation: Vietnam Labor Law explained",
            category,
            "Legal và practical considerations khi ending employment ở Vietnam.",
            "<p>Understanding termination và resignation helps protect your rights.</p><h2>Resignation</h2><ul><li>Notice period: 45 days (under 1 year service), 30 days (1-5 years).</li><li>Handover requirements.</li><li>Benefits entitlement.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa termination",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1670L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill)
        ));

        articles.add(new SeedArticle(
            "cross-functional-collaboration-siloss-broken",
            "Cross-functional Collaboration: Breaking down silos",
            category,
            "Làm thế nào để work effectively với other departments và teams.",
            "<p>Modern workplaces require cross-functional collaboration, nhưng silos often get in the way.</p><h2>Common challenges</h2><ul><li>Different priorities và incentives.</li><li>Communication barriers.</li><li>Resource competition.</li></ul><h2>Solutions</h2><ul><li>Shared goals.</li><li>Regular sync meetings.</li><li>Empathy building.</li></ul>",
            "/images/career-guide/guide-wiki.svg",
            "Ảnh minh họa cross-functional",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1420L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(skill, growth)
        ));

        return articles;
    }

    private List<SeedArticle> seedWellbeingArticles(CareerGuideCategory category, CareerGuideTag mental, CareerGuideTag growth, CareerGuideTag salary) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 50;

        articles.add(new SeedArticle(
            "resilience-kha-nang-phuc-hoi-noi-luc-giup-ca-nhan-to-chuc",
            "Resilience (khả năng phục hồi): Nội lực giúp cá nhân & tổ chức thích nghi",
            category,
            "Vì sao khả năng phục hồi đang trở thành năng lực cốt lõi trong môi trường làm việc biến động.",
            "<p>Resilience không phải là chịu đựng mọi thứ. Đó là khả năng phục hồi trạng thái và học từ biến cố.</p>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa sức bền tinh thần",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            612L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental, growth)
        ));

        // Thêm 9 bài nữa cho Thư giãn...
        articles.add(new SeedArticle(
            "work-life-balance-myth-reality-2026",
            "Work-Life Balance: Myth or Reality in 2026?",
            category,
            "Critical look tại concept work-life balance và practical alternatives.",
            "<p>'Balance' implies equal 50-50 split, nhưng reality is more nuanced.</p><h2>Alternative frameworks</h2><ul><li>Work-life integration: Blending work và personal life intentionally.</li><li>Work-life harmony: Finding flow between domains.</li><li>Seasons of life: Accepting imbalance during certain periods.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa work-life balance",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            6,
            2340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental)
        ));

        articles.add(new SeedArticle(
            "mental-health-stigma-workplace-vietnam-changing",
            "Mental Health Stigma in Workplace Vietnam: Is it changing?",
            category,
            "Exploring attitudes toward mental health trong Vietnamese workplaces.",
            "<p>Mental health stigma is real,但有 signs của gradual change.</p><h2>Where we're at</h2><ul><li>Younger generations more open.</li><li>MNCs leading với employee assistance programs.</li><li>Local companies slowly catching up.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa mental health stigma",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1870L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental)
        ));

        articles.add(new SeedArticle(
            "imposter-syndrome-high-achievers-overcome",
            "Imposter Syndrome in High Achievers: How to Overcome",
            category,
            "Understanding imposter syndrome và practical strategies để manage it.",
            "<p>Imposter syndrome affects even successful professionals, regardless của actual competence.</p><h2>Signs</h2><ul><li>Attributing success đến luck, not skill.</li><li>Fear being 'found out'.</li><li>Overpreparing cho every task.</li></ul><h2>Strategies</h2><ul><li>Keep evidence của accomplishments.</li><li>Talk about it với trusted peers.</li><li>Reframe thoughts.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa imposter syndrome",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental, growth)
        ));

        articles.add(new SeedArticle(
            "stress-management-techniques-workplace",
            "Stress Management Techniques for the Workplace",
            category,
            "Practical strategies để manage work-related stress effectively.",
            "<p>Stress is inevitable, nhưng chronic stress is harmful. Learning to manage it is a critical skill.</p><h2>Immediate techniques</h2><ul><li>Deep breathing: 4-7-8 technique.</li><li>Progressive muscle relaxation.</li><li>Quick walk outside.</li></ul><h2>Long-term strategies</h2><ul><li>Regular exercise routine.</li><li>Adequate sleep hygiene.</li><li>Setting boundaries.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa stress management",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental)
        ));

        articles.add(new SeedArticle(
            "mindfulness-work-beginners-guide",
            "Mindfulness at Work: A Beginner's Guide",
            category,
            "Introduction to mindfulness practices suitable cho busy professionals.",
            "<p>Mindfulness isn't just meditation. It's about being present trong whatever you're doing.</p><h2>Simple practices</h2><ul><li>Mindful breathing: 5 minutes giữa tasks.</li><li>Mindful eating: Lunch without phone.</li><li>Mindful walking: Commute as meditation.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa mindfulness",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1230L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental, growth)
        ));

        articles.add(new SeedArticle(
            "toxic-workplace-identify-escape-strategy",
            "Toxic Workplace: How to Identify và Escape Strategy",
            category,
            "Recognizing signs of toxic work environments và planning exit strategy.",
            "<p>Staying in toxic environment has serious mental health consequences.</p><h2>Warning signs</h2><ul><li>Constant fear và anxiety about work.</li><li>Lack của psychological safety.</li><li>Bullying, harassment, hoặc discrimination.</li><li>Unreasonable workload với inadequate support.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa toxic workplace",
            "Ban biên tập CoHoiViecLam",
            true,
            true,
            7,
            3120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental)
        ));

        articles.add(new SeedArticle(
            "social-connection-workplace-combatting-loneliness",
            "Social Connection at Workplace: Combatting Loneliness",
            category,
            "Why workplace relationships matter cho mental health và ways to build them.",
            "<p>Loneliness at work is a growing issue, đặc biệt trong remote era.</p><h2>Why it matters</h2><ul><li>Connected employees are happier, more productive.</li><li>Social support buffers stress.</li><li>Relationships are key cho career success.</li></ul><h2>Building connections</h2><ul><li>Schedule regular 1:1s.</li><li>Join ERGs (employee resource groups).</li><li>Participate trong team activities.</li></ul>",
            "/images/career-guide/guide-social-connection.svg",
            "Ảnh minh họa social connection",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental, growth)
        ));

        articles.add(new SeedArticle(
            "sleep-productivity-work-performance-connection",
            "Sleep & Productivity: The Work Performance Connection",
            category,
            "Science behind sleep's impact trên work performance và tips cho better sleep hygiene.",
            "<p>Sleep deprivation costs companies billions annually lost productivity.</p><h2>Sleep's impact</h2><ul><li>Cognitive function: Memory, focus, decision-making.</li><li>Emotional regulation: Irritability, mood swings.</li><li>Physical health: Immunity, long-term disease risk.</li></ul><h2>Tips</h2><ul><li>Consistent sleep schedule.</li><li>Limit screens before bed.</li><li>Create conducive sleep environment.</li></ul>",
            "/images/career-guide/guide-sleep-productivity.svg",
            "Ảnh minh họa sleep & productivity",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1670L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental)
        ));

        articles.add(new SeedArticle(
            "setting-boundaries-work-protecting-personal-time",
            "Setting Boundaries at Work: Protecting Personal Time",
            category,
            "Practical guide để establishing healthy work boundaries.",
            "<p>Without boundaries, work encroaches vào personal life, leading để burnout.</p><h2>Types of boundaries</h2><ul><li>Time: No emails after 7pm, weekends off.</li><li>Communication: Response time expectations.</li><li>Physical: Workspace vs personal space.</li><li>Emotional: Not taking work stress home.</li></ul><h2>Communicating boundaries</h2><ul><li>Be direct but polite.</li><li>Explain the 'why'.</li><li>Model boundary-respecting behavior.</li></ul>",
            "/images/career-guide/guide-relax.svg",
            "Ảnh minh họa setting boundaries",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(mental, growth)
        ));

        return articles;
    }

    private List<SeedArticle> seedEventsArticles(CareerGuideCategory category, CareerGuideTag network, CareerGuideTag skill, CareerGuideTag growth) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 60;

        articles.add(new SeedArticle(
            "nhan-vien-moi-can-chuan-bi-gi-cho-tuan-dau-nhan-viec",
            "Nhân viên mới cần chuẩn bị gì cho tuần đầu nhận việc sau Tết?",
            category,
            "Checklist thực tế cho người mới đi làm: giấy tờ, kỳ vọng, cách ghi nhận công việc.",
            "<p>Tuần đầu sau Tết là giai đoạn hình thành ấn tượng đầu tiên.</p>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa ngày đầu nhận việc",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            4,
            980L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network)
        ));

        // Thêm 9 bài nữa cho Events...
        articles.add(new SeedArticle(
            "career-fair-2026-maximize-experience",
            "Career Fair 2026: How to Maximize Your Experience",
            category,
            "Strategies để get most out của career fairs và job expos.",
            "<p>Career fairs offer opportunities để meet multiple employers trong one day.</p><h2>Preparation</h2><ul><li>Research attending companies.</li><li>Prepare elevator pitch.</li><li>Print multiple resumes.</li></ul><h2>At the fair</h2><ul><li>Don't just drop resume—engage trong conversation.</li><li>Collect business cards.</li><li>Take notes after each conversation.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa career fair",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, skill)
        ));

        articles.add(new SeedArticle(
            "networking-introverts-guide-extroverts",
            "Networking cho Introverts (và Extroverts who want to improve)",
            category,
            "Networking strategies tailored cho different personality types.",
            "<p>Networking isn't just cho extroverts. Introverts have unique advantages.</p><h2>Introvert-friendly strategies</h2><ul><li>Focus trên 1-on-1 conversations.</li><li>Prepare questions beforehand.</li><li>Leverage written communication (LinkedIn, email).</li></ul><h2>Extrovert tips</h2><ul><li>Listen more than talk.</li><li>Follow up meaningfully.</li><li>Quality over quantity.</li></ul>",
            "/images/career-guide/guide-networking.svg",
            "Ảnh minh họa networking",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network)
        ));

        articles.add(new SeedArticle(
            "linkedin-local-events-community-building",
            "LinkedIn Local Events: Building Community Offline",
            category,
            "Tapping vào LinkedIn Local và other professional community events.",
            "<p>LinkedIn Local brings online connections into face-to-face meetups.</p><h2>Benefits</h2><ul><li>Deeper relationships vs online-only.</li><li>Exposure diverse perspectives.</li><li>Potential collaborators, clients, mentors.</li></ul><h2>Finding events</h2><ul><li>LinkedIn Events section.</li><li>Local professional associations.</li><li>Industry meetups.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa LinkedIn Local",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network)
        ));

        articles.add(new SeedArticle(
            "webinar-online-events-virtual-participation",
            "Webinar & Online Events: Making the Most of Virtual Participation",
            category,
            "Tips cho engaging effectively trong virtual professional events.",
            "<p>Virtual events have become standard post-pandemic, offering unique advantages.</p><h2>Advantages</h2><ul><li>No travel time/cost.</li><li>Access global speakers.</li><li>Recordings available post-event.</li></ul><h2>Best practices</h2><ul><li>Turn camera on (when appropriate).</li><li>Use chat feature thoughtfully.</li><li>Follow up với speakers, attendees.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa webinar",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1230L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, skill)
        ));

        articles.add(new SeedArticle(
            "industry-conferences-justify-investment",
            "Industry Conferences: How to Justify Investment to Employer",
            category,
            "Making business case cho attending professional conferences.",
            "<p>Conferences can be expensive, nhưng often pay dividends trong knowledge và connections.</p><h2>ROI arguments</h2><ul><li>Knowledge: Latest trends, best practices.</li><li>Networking: Potential partners, hires, clients.</li><li>Brand: Representing company at industry events.</li></ul><h2>Post-conference</h2><ul><li>Share key takeaways với team.</li><li>Implement learned strategies.</li><li>Introduce new contacts.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa conferences",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, skill)
        ));

        articles.add(new SeedArticle(
            "alumni-networks-leveraging-university-connections",
            "Alumni Networks: Leveraging University Connections cho Career",
            category,
            "Tapping vào alumni networks cho job opportunities, mentorship, insights.",
            "<p>Your university alumni network is valuable career resource often underutilized.</p><h2>How to engage</h2><ul><li>Join alumni groups (LinkedIn, local chapters).</li><li>Attend alumni events.</li><li>Reach out cho informational interviews.</li></ul><h2>Value proposition</h2><ul><li>Shared experience creates instant connection.</li><li>Alumni often eager to help fellow graduates.</li><li>Access inside information về companies, industries.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa alumni networks",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network)
        ));

        articles.add(new SeedArticle(
            "professional-associations-membership-worth-it",
            "Professional Associations: Is Membership Worth It?",
            category,
            "Evaluating pros và cons của joining professional organizations.",
            "<p>Professional associations offer benefits,但 come với costs và time commitments.</p><h2>Benefits</h2><ul><li>Credentials: Certifications, designations.</li><li>Networking: Events, chapters, online communities.</li><li>Resources: Publications, job boards, templates.</li></ul><h2>Considerations</h2><ul><li>Membership fees: $100-500+ annually.</li><li>Time investment: Meetings, volunteering.</li><li>ROI varies by industry, career stage.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa professional associations",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, growth)
        ));

        articles.add(new SeedArticle(
            "hosting-workshop-sharing-expertise-build-brand",
            "Hosting a Workshop: Sharing Expertise để Build Your Brand",
            category,
            "How organizing teaching sessions establishes thought leadership.",
            "<p>Teaching others is powerful way để demonstrate expertise và build professional brand.</p><h2>Why host workshops?</h2><ul><li>Establish authority: Position yourself as expert.</li><li>Give value: Build goodwill trong community.</li><li>Learn: Teaching reinforces your own knowledge.</li></ul><h2>Getting started</h2><ul><li>Choose topic trong your sweet spot.</li><li>Partner với existing communities/groups.</li><li>Start small: 30-60 min session.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa hosting workshop",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, skill)
        ));

        articles.add(new SeedArticle(
            "volunteering-career-benefits-professional-development",
            "Volunteering cho Career Benefits & Professional Development",
            category,
            "How volunteer work enhances skills, network, và employability.",
            "<p>Volunteering isn't just về giving back—it also accelerates career growth.</p><h2>Skill development</h2><ul><li>Leadership: Leading volunteer teams.</li><li>Project management: Organizing events, campaigns.</li><li>Communication: Diverse stakeholders.</li></ul><h2>Networking</h2><ul><li>Meet passionate, committed people.</li><li>Demonstrate values cho potential employers.</li><li>Expand network beyond your industry.</li></ul>",
            "/images/career-guide/guide-event.svg",
            "Ảnh minh họa volunteering",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(network, growth)
        ));

        return articles;
    }

    private List<SeedArticle> seedPartnerArticles(CareerGuideCategory category, CareerGuideTag employerBrand, CareerGuideTag network, CareerGuideTag growth) {
        List<SeedArticle> articles = new ArrayList<>();
        int dayCounter = 70;

        articles.add(new SeedArticle(
            "employer-branding-la-gi-vi-sao-quyet-dinh-ty-le-ung-tuyen",
            "Employer branding là gì và vì sao quyết định tỷ lệ ứng tuyển?",
            category,
            "Góc nhìn dành cho nhà tuyển dụng: thương hiệu tuyển dụng ảnh hưởng thế nào tới chất lượng ứng viên.",
            "<p>Employer branding không chỉ là logo đẹp hay video tuyển dụng bắt mắt.</p>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa thương hiệu nhà tuyển dụng",
            "Ban biên tập CoHoiViecLam",
            true,
            false,
            6,
            520L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand, network)
        ));

        // Thêm 9 bài nữa cho Partner...
        articles.add(new SeedArticle(
            "candidate-experience-journey-mapping",
            "Candidate Experience: Mapping the Journey từ Apply đến Offer",
            category,
            "Understanding và optimizing candidate experience cho better hire rates.",
            "<p>Candidate experience directly impacts offer acceptance rates và employer brand.</p><h2>Key touchpoints</h2><ul><li>Job posting: Clear, engaging descriptions.</li><li>Application process: Mobile-friendly, not too lengthy.</li><li>Communication: Timely updates, respectful rejections.</li><li>Interview: Professional, organized, two-way.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa candidate experience",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1780L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand)
        ));

        articles.add(new SeedArticle(
            "employee-value-proposition-evp-development",
            "Employee Value Proposition (EVP): Development & Communication",
            category,
            "Creating compelling EVP that attracts right talent.",
            "<p>EVP is unique set of benefits employees receive cho work they do—beyond just salary.</p><h2>Components</h2><ul><li>Compensation: Salary, bonuses, equity.</li><li>Benefits: Insurance, leave, perks.</li><li>Career: Growth, development, advancement.</li><li>Work environment: Culture, flexibility, tools.</li><li>Company purpose: Mission, impact, values.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa EVP",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            1890L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand)
        ));

        articles.add(new SeedArticle(
            "recruitment-marketing-strategies-2026",
            "Recruitment Marketing Strategies 2026: Attract Top Talent",
            category,
            "Modern marketing approaches cho attracting quality candidates.",
            "<p>Recruitment marketing applies traditional marketing principles talent acquisition.</p><h2>Channels</h2><ul><li>Social media: LinkedIn, Facebook, Instagram, TikTok.</li><li>Content: Blog, video, employee stories.</li><li>Employer review sites: Glassdoor, CoHoiViecLam.</li><li>Events: Career fairs, webinars, meetups.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa recruitment marketing",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand, network)
        ));

        articles.add(new SeedArticle(
            "employee-advocacy-programs-leveraging-workforce",
            "Employee Advocacy Programs: Leveraging Your Workforce",
            category,
            "Turning employees vào brand ambassadors authentic recruitment marketing.",
            "<p>Employees are most credible source information về your company culture.</p><h2>Benefits</h2><ul><li>Authenticity: Real stories from real people.</li><li>Reach: Employees' networks combined massive.</li><li>Cost-effective: Organic vs paid advertising.</li></ul><h2>Implementation</h2><ul><li>Make it easy: Pre-written posts, graphics.</li><li>Incentivize: Recognition, rewards.</li><li>Train: Social media best practices.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa employee advocacy",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand)
        ));

        articles.add(new SeedArticle(
            "diversity-inclusion-hiring-reducing-bias",
            "Diversity & Inclusion trong Hiring: Reducing Bias",
            category,
            "Practical steps organizations can take build more diverse, inclusive workforce.",
            "<p>D&I isn't just right thing to do—it's business imperative.</p><h2>Where bias occurs</h2><ul><li>Job descriptions: Gendered language.</li><li>Sourcing: Homogeneous networks.</li><li>Screening: Subjective criteria.</li><li>Interviewing: Unconscious biases.</li></ul><h2>Solutions</h2><ul><li>Blind resume screening.</li><li>Structured interviews standardized questions.</li><li> diverse interview panels.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa D&I",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            7,
            2120L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand)
        ));

        articles.add(new SeedArticle(
            "boomerang-employees-rehiring-former-employees",
            "Boomerang Employees: The Case cho Rehiring Former Employees",
            category,
            "Why rehiring former employees can be smart talent strategy.",
            "<p>Boomerang employees—those who return previous employer—are increasingly common.</p><h2>Advantages</h2><ul><li>Ramp-up time: Already know systems, culture.</li><li>Proven track record: Performance known.</li><li>New perspectives: Bring external experience back.</li></ul><h2>When it makes sense</h2><ul><li>Left good terms, no hard feelings.</li><li>Reasons leaving addressed.</li><li>New skills/experience valuable.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa boomerang employees",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1450L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand, network)
        ));

        articles.add(new SeedArticle(
            "internal-mobility-programs-talent-retention",
            "Internal Mobility Programs: Key Talent Retention Strategy",
            category,
            "Why promoting from within improves retention, engagement, performance.",
            "<p>Internal mobility—employees moving between roles, teams, locations—is underutilized retention tool.</p><h2>Benefits</h2><ul><li>Retention: Employees stay longer khi can grow internally.</li><li>Engagement: New challenges prevent boredom.</li><li>Cost: Internal hires cheaper cheaper external.</li><li>Risk: Internal track record reduces bad hires.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa internal mobility",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1670L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand, growth)
        ));

        articles.add(new SeedArticle(
            "employer-review-management-online-reputation",
            "Employer Review Management: Protecting Online Reputation",
            category,
            "Strategies managing employer reviews Glassdoor, CoHoiViecLam, similar platforms.",
            "<p>Employer reviews significantly impact candidate interest quality.</p><h2>Monitoring</h2><ul><li>Set alerts cho company name.</li><li>Regularly check major review sites.</li><li>Track trends improvements, declines.</li></ul><h2>Responding</h2><ul><li>Address both positive negative reviews.</li><li>Take offline when appropriate.</li><li>Demonstrate actions taken.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa review management",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            5,
            1340L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand)
        ));

        articles.add(new SeedArticle(
            "university-partnerships-early-talent-pipeline",
            "University Partnerships: Building Early Talent Pipeline",
            category,
            "Creating strategic partnerships universities steady stream fresh talent.",
            "<p>University partnerships provide early access top talent employers build brand awareness.</p><h2>Partnership types</h2><ul><li>Recruitment: Career fairs, campus interviews.</li><li>Education: Guest lectures, case competitions.</li><li>Research: Collaborate faculty, student projects.</li><li>Scholarships, internships: Direct talent pipeline.</li></ul>",
            "/images/career-guide/guide-partner.svg",
            "Ảnh minh họa university partnerships",
            "Ban biên tập CoHoiViecLam",
            false,
            false,
            6,
            1560L,
            LocalDateTime.now().minusDays(dayCounter++),
            List.of(employerBrand, network)
        ));

        return articles;
    }

    // ==================== UTILITY METHODS ====================

    private void seedCategory(String name, String slug, String description, String icon, String accentColor, int order) {
        if (categoryRepository.findBySlug(slug).isPresent()) {
            return;
        }

        CareerGuideCategory category = new CareerGuideCategory();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setIcon(icon);
        category.setAccentColor(accentColor);
        category.setDisplayOrder(order);
        category.setActive(true);
        categoryRepository.save(category);
    }

    private void seedTag(String name, String slug, int order) {
        if (tagRepository.findBySlug(slug).isPresent()) {
            return;
        }

        CareerGuideTag tag = new CareerGuideTag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDisplayOrder(order);
        tag.setActive(true);
        tagRepository.save(tag);
    }

    private CareerGuideCategory requireCategory(String slug) {
        return categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy danh mục " + slug));
    }

    private CareerGuideTag requireTag(String slug) {
        return tagRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy tag " + slug));
    }

    private record SeedArticle(
        String slug,
        String title,
        CareerGuideCategory category,
        String excerpt,
        String contentHtml,
        String coverImageUrl,
        String coverImageAlt,
        String authorName,
        boolean featured,
        boolean pinned,
        Integer readTimeMinutes,
        Long views,
        LocalDateTime publishedAt,
        List<CareerGuideTag> tags
    ) {}
}
