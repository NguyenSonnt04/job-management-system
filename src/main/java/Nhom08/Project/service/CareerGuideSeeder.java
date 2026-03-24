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
        if (articleRepository.count() > 0) {
            return;
        }

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

        List<SeedArticle> articles = List.of(
            new SeedArticle(
                "top-5-nganh-du-doan-khat-nhan-luc-2026-tai-viet-nam",
                "Top 5 ngành dự đoán khát nhân lực 2026 tại Việt Nam",
                laborMarket,
                "Phân tích những lĩnh vực đang tăng tốc tuyển dụng, vì sao nhu cầu nhân sự tiếp tục cao và ứng viên nên chuẩn bị gì từ bây giờ.",
                """
                <p>Năm 2026 tiếp tục là giai đoạn thị trường lao động tái cấu trúc theo nhu cầu số hóa, tự động hóa và tối ưu vận hành. Những ngành có tốc độ mở rộng cao thường không chỉ tuyển thêm người, mà còn tuyển người có khả năng làm việc đa nhiệm, hiểu dữ liệu và thích nghi nhanh.</p>
                <h2>1. Công nghệ và dữ liệu</h2>
                <p>Nhóm vai trò liên quan đến phát triển sản phẩm số, dữ liệu và hạ tầng vẫn giữ nhịp tuyển dụng ổn định. Ứng viên nên ưu tiên kỹ năng giải quyết vấn đề, tư duy hệ thống và khả năng phối hợp với các nhóm kinh doanh.</p>
                <h2>2. Logistics, chuỗi cung ứng</h2>
                <p>Nhu cầu vận chuyển, phân phối và quản trị tồn kho tăng đều khi thương mại đa kênh mở rộng. Đây là nhóm ngành cần tư duy quy trình, độ chính xác và năng lực dùng công cụ phân tích.</p>
                <h2>3. Y tế, chăm sóc sức khỏe</h2>
                <p>Già hóa dân số và nhu cầu chăm sóc chất lượng cao khiến nguồn lực y tế tiếp tục là bài toán dài hạn. Các vị trí vận hành, chăm sóc khách hàng và quản lý dịch vụ cũng tăng mạnh.</p>
                <blockquote>Điểm chung của các ngành tăng trưởng: họ không chỉ tìm ứng viên có chuyên môn, mà còn tìm người học nhanh và có tinh thần cải tiến.</blockquote>
                """,
                "/images/career-guide/guide-market.svg",
                "Ảnh minh họa xu hướng thị trường lao động 2026",
                "Ban biên tập CareerViet",
                true,
                true,
                6,
                2180L,
                LocalDateTime.now().minusDays(2),
                List.of(trend)
            ),
            new SeedArticle(
                "nghi-viec-truoc-hay-tim-viec-truoc-quyet-dinh-chien-luoc",
                "Nghỉ việc trước hay tìm việc trước? Quyết định chiến lược cho bước chuyển nghề",
                careerPath,
                "So sánh hai kịch bản chuyển việc phổ biến để ứng viên chọn được thời điểm phù hợp với tài chính, tâm lý và mục tiêu dài hạn.",
                """
                <p>Quyết định nghỉ việc trước hay tìm việc trước không có câu trả lời đúng tuyệt đối. Vấn đề là bạn đang ở vị thế đàm phán nào, mức dự phòng tài chính ra sao và mức độ cấp bách của nhu cầu thay đổi môi trường là gì.</p>
                <h2>Khi nào nên tìm việc trước?</h2>
                <ul>
                  <li>Bạn vẫn đang ổn định tài chính và muốn giảm rủi ro gián đoạn thu nhập.</li>
                  <li>Vai trò hiện tại cho phép bạn chủ động chuẩn bị hồ sơ, phỏng vấn và sắp xếp thời gian.</li>
                  <li>Thị trường đang có nhiều cơ hội phù hợp với năng lực hiện tại.</li>
                </ul>
                <h2>Khi nào nên nghỉ việc trước?</h2>
                <ul>
                  <li>Môi trường làm việc đang ảnh hưởng trực tiếp đến sức khỏe tinh thần.</li>
                  <li>Bạn cần thời gian hồi phục, học lại một kỹ năng hoặc đổi hướng nghề.</li>
                  <li>Tài chính cá nhân đủ đệm cho giai đoạn chuyển tiếp ngắn.</li>
                </ul>
                <p>Chọn đúng chiến lược giúp bạn kiểm soát nhịp chuyển nghề thay vì để cảm xúc quyết định thay.</p>
                """,
                "/images/career-guide/guide-growth.svg",
                "Ảnh minh họa chuyển hướng sự nghiệp",
                "Ban biên tập CareerViet",
                true,
                false,
                5,
                1640L,
                LocalDateTime.now().minusDays(4),
                List.of(interview, skill)
            ),
            new SeedArticle(
                "nhan-vien-moi-can-chuan-bi-gi-cho-tuan-dau-nhan-viec-sau-tet",
                "Nhân viên mới cần chuẩn bị gì cho tuần đầu nhận việc sau Tết?",
                events,
                "Checklist thực tế cho người mới đi làm: giấy tờ, kỳ vọng, cách ghi nhận công việc và bước tạo thiện cảm với team.",
                """
                <p>Tuần đầu sau Tết là giai đoạn hình thành ấn tượng đầu tiên. Bạn không cần thể hiện tất cả năng lực ngay, nhưng nên cho thấy mình chủ động, đúng giờ và biết cách hỏi thông minh.</p>
                <h2>3 việc cần chuẩn bị trước ngày đầu</h2>
                <ol>
                  <li>Rà lại email, lịch họp, thông tin người quản lý trực tiếp và các tài liệu onboarding.</li>
                  <li>Chuẩn bị trang phục, phương tiện di chuyển và giờ đến sớm hơn dự kiến 15 phút.</li>
                  <li>Ghi sẵn những câu hỏi cần xác nhận: KPI, quy trình báo cáo, công cụ đang dùng.</li>
                </ol>
                <p>Nếu bạn mới đi làm, mục tiêu của tuần đầu không phải là gây ấn tượng quá mạnh. Mục tiêu là xây được độ tin cậy ban đầu và hiểu được cách team vận hành.</p>
                """,
                "/images/career-guide/guide-event.svg",
                "Ảnh minh họa ngày đầu nhận việc",
                "Ban biên tập CareerViet",
                false,
                false,
                4,
                980L,
                LocalDateTime.now().minusDays(6),
                List.of(network)
            ),
            new SeedArticle(
                "cap-nhat-muc-luong-co-so-nam-2026-theo-quy-dinh-moi-nhat",
                "Cập nhật mức lương cơ sở năm 2026 theo quy định mới nhất",
                laborMarket,
                "Tóm tắt những điểm ứng viên và người lao động cần lưu ý khi cập nhật mức lương cơ sở, lương tối thiểu và ngân sách cá nhân.",
                """
                <p>Khi lương cơ sở thay đổi, tác động không chỉ nằm ở phần lương tháng. Nó còn ảnh hưởng đến các khoản đóng bảo hiểm, mức tham chiếu của nhiều chính sách và cách bạn lập kế hoạch thu nhập thực tế.</p>
                <h2>Người lao động cần theo dõi gì?</h2>
                <ul>
                  <li>Mức lương ghi trên hợp đồng lao động.</li>
                  <li>Các khoản đóng bảo hiểm và phúc lợi liên quan.</li>
                  <li>Thu nhập ròng sau khi khấu trừ thuế, bảo hiểm.</li>
                </ul>
                <h2>Cách dùng thông tin này trong đàm phán</h2>
                <p>Đừng chỉ hỏi nhà tuyển dụng về con số gross. Hãy hiểu rõ tổng giá trị đãi ngộ, lộ trình tăng lương và điều kiện để đạt mức cao hơn trong 6 đến 12 tháng.</p>
                """,
                "/images/career-guide/guide-salary.svg",
                "Ảnh minh họa lương cơ sở và thu nhập",
                "Ban biên tập CareerViet",
                true,
                false,
                5,
                1430L,
                LocalDateTime.now().minusDays(1),
                List.of(salary, trend)
            ),
            new SeedArticle(
                "7-dau-hieu-cv-dang-bi-ats-lo-tu-vong-dau",
                "7 dấu hiệu CV đang bị ATS loại từ vòng đầu",
                jobTips,
                "Những lỗi định dạng, nội dung và từ khóa khiến hồ sơ khó vượt qua hệ thống ATS dù ứng viên có kinh nghiệm tốt.",
                """
                <p>ATS không phải là kẻ thù, nhưng nó là một bộ lọc rất khắt khe. CV đẹp mắt chưa chắc đã qua được máy đọc nếu bố cục rối, tiêu đề mơ hồ hoặc thiếu từ khóa khớp với JD.</p>
                <h2>Những lỗi thường gặp</h2>
                <ul>
                  <li>CV dùng nhiều cột, icon hoặc khối hình phức tạp.</li>
                  <li>Tiêu đề kinh nghiệm không rõ chức danh.</li>
                  <li>Thiếu số liệu đo lường thành tích.</li>
                  <li>Không có từ khóa kỹ năng theo mô tả công việc.</li>
                </ul>
                <p>Nếu bạn muốn tối ưu cho ATS, hãy ưu tiên cấu trúc rõ ràng, tiêu đề nhất quán và thành tích có số liệu. Sau đó mới tính đến phong cách trình bày.</p>
                """,
                "/images/career-guide/guide-ats.svg",
                "Ảnh minh họa CV thân thiện với ATS",
                "Ban biên tập CareerViet",
                true,
                true,
                4,
                3012L,
                LocalDateTime.now().minusDays(3),
                List.of(ats, cv)
            ),
            new SeedArticle(
                "5-cau-hoi-phong-van-hanh-vi-can-chuan-bi-truoc",
                "5 câu hỏi phỏng vấn hành vi mà ứng viên nên chuẩn bị trước",
                jobTips,
                "Khung trả lời STAR gọn gàng để bạn kể kinh nghiệm, xử lý tình huống và chứng minh năng lực thay vì chỉ nói chung chung.",
                """
                <p>Phỏng vấn hành vi không kiểm tra câu chữ hay sự trôi chảy đơn thuần. Nhà tuyển dụng muốn xem cách bạn ra quyết định, phản ứng khi áp lực và học gì từ những tình huống trước đây.</p>
                <h2>Khung STAR</h2>
                <p>Situation - Task - Action - Result là cấu trúc ngắn gọn nhất để câu trả lời không bị lan man. Mỗi ví dụ nên có bối cảnh, mục tiêu, hành động của bạn và kết quả đo được.</p>
                <h2>5 câu hỏi cần chuẩn bị</h2>
                <ol>
                  <li>Hãy kể về một lần bạn giải quyết xung đột trong team.</li>
                  <li>Hãy mô tả một dự án thất bại và bài học rút ra.</li>
                  <li>Hãy kể về lần bạn phải xử lý deadline rất gấp.</li>
                  <li>Hãy cho ví dụ về việc bạn tạo ảnh hưởng khi không có quyền lực chính thức.</li>
                  <li>Hãy nói về một lần bạn học nhanh một kỹ năng mới.</li>
                </ol>
                """,
                "/images/career-guide/guide-interview.svg",
                "Ảnh minh họa phỏng vấn hành vi",
                "Ban biên tập CareerViet",
                false,
                false,
                6,
                740L,
                LocalDateTime.now().minusDays(8),
                List.of(interview, skill)
            ),
            new SeedArticle(
                "resilience-kha-nang-phuc-hoi-noi-luc-giup-ca-nhan-to-chuc-thich-nghi",
                "Resilience (khả năng phục hồi): Nội lực giúp cá nhân & tổ chức thích nghi",
                wellbeing,
                "Vì sao khả năng phục hồi đang trở thành năng lực cốt lõi trong môi trường làm việc biến động, và cách rèn luyện nó từng bước.",
                """
                <p>Resilience không phải là chịu đựng mọi thứ một cách im lặng. Đó là khả năng phục hồi trạng thái, học từ biến cố và quay trở lại công việc với năng lượng ổn định hơn.</p>
                <h2>Ở cấp cá nhân</h2>
                <p>Người có resilience tốt thường biết tách mình khỏi kết quả tạm thời, có thói quen hồi phục và dám nhờ hỗ trợ khi cần.</p>
                <h2>Ở cấp tổ chức</h2>
                <p>Doanh nghiệp resilient thường có quy trình rõ, giao tiếp minh bạch và đủ linh hoạt để điều chỉnh khi thị trường thay đổi.</p>
                <blockquote>Không phải ai cũng cần đứng vững hoàn hảo. Điều quan trọng là biết cách đứng dậy nhanh hơn sau mỗi va chạm.</blockquote>
                """,
                "/images/career-guide/guide-relax.svg",
                "Ảnh minh họa sức bền tinh thần",
                "Ban biên tập CareerViet",
                false,
                false,
                5,
                612L,
                LocalDateTime.now().minusDays(7),
                List.of(mental, growth)
            ),
            new SeedArticle(
                "employer-branding-la-gi-va-vi-sao-quyet-dinh-ty-le-ung-tuyen",
                "Employer branding là gì và vì sao quyết định tỷ lệ ứng tuyển?",
                partner,
                "Góc nhìn dành cho nhà tuyển dụng: thương hiệu tuyển dụng ảnh hưởng thế nào tới chất lượng ứng viên, tốc độ tuyển và chi phí tuyển dụng.",
                """
                <p>Employer branding không chỉ là logo đẹp hay video tuyển dụng bắt mắt. Nó là tổng hòa trải nghiệm, thông điệp và cảm nhận mà ứng viên có khi nhìn vào công ty của bạn.</p>
                <h2>Vì sao quan trọng?</h2>
                <ul>
                  <li>Ứng viên đánh giá rủi ro trước khi nộp đơn.</li>
                  <li>Thương hiệu mạnh làm tăng tỷ lệ chuyển đổi ở từng bước.</li>
                  <li>Nội dung minh bạch giúp rút ngắn vòng sàng lọc.</li>
                </ul>
                <h2>Nhà tuyển dụng nên bắt đầu từ đâu?</h2>
                <p>Bắt đầu từ những điều rất thực: mô tả công việc rõ ràng, phản hồi đúng hẹn, hình ảnh đội ngũ chân thật và một câu chuyện tổ chức nhất quán.</p>
                """,
                "/images/career-guide/guide-partner.svg",
                "Ảnh minh họa thương hiệu nhà tuyển dụng",
                "Ban biên tập CareerViet",
                true,
                false,
                6,
                520L,
                LocalDateTime.now().minusDays(5),
                List.of(employerBrand, network)
            ),
            new SeedArticle(
                "ban-do-nghe-nghiep-cho-nguoi-moi-di-lam",
                "Bản đồ nghề nghiệp cho người mới đi làm",
                wiki,
                "Gợi ý cách đọc thị trường, chọn kỹ năng và xây nền tảng để không bị đứng im sau năm đầu tiên đi làm.",
                """
                <p>Người mới đi làm thường bị choáng bởi quá nhiều lựa chọn: học thêm kỹ năng gì, nhận việc ra sao, khi nào nên đổi team. Bản đồ nghề nghiệp giúp bạn không đi theo cảm hứng ngắn hạn.</p>
                <h2>3 lớp cần xây</h2>
                <ol>
                  <li>Lớp nền tảng: giao tiếp, quản lý thời gian, sử dụng công cụ.</li>
                  <li>Lớp chuyên môn: nghiệp vụ cốt lõi của vai trò đang làm.</li>
                  <li>Lớp tăng trưởng: dữ liệu, hệ thống, tư duy sản phẩm hoặc lãnh đạo.</li>
                </ol>
                <p>Không cần giỏi ngay từ đầu. Hãy giỏi hơn bản thân tháng trước và có lý do rõ ràng cho mỗi kỹ năng bạn học tiếp theo.</p>
                """,
                "/images/career-guide/guide-wiki.svg",
                "Ảnh minh họa bản đồ nghề nghiệp",
                "Ban biên tập CareerViet",
                false,
                false,
                4,
                905L,
                LocalDateTime.now().minusDays(9),
                List.of(skill, growth)
            )
        );

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
