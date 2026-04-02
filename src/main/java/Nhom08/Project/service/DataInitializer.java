package Nhom08.Project.service;

import Nhom08.Project.entity.*;
import Nhom08.Project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository           roleRepository;
    @Autowired private UserRepository           userRepository;
    @Autowired private PasswordEncoder          passwordEncoder;

    // Form option repositories
    @Autowired private IndustryRepository       industryRepository;
    @Autowired private ExperienceLevelRepository experienceLevelRepository;
    @Autowired private EducationLevelRepository  educationLevelRepository;
    @Autowired private DegreeLevelRepository     degreeLevelRepository;
    @Autowired private JobBenefitRepository      jobBenefitRepository;
    @Autowired private ProvinceRepository        provinceRepository;

    // Dynamic filter repositories
    @Autowired private Nhom08.Project.repository.FilterGroupRepository  filterGroupRepository;
    @Autowired private Nhom08.Project.repository.FilterOptionRepository filterOptionRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
        seedIndustries();
        seedExperienceLevels();
        seedEducationLevels();
        seedDegreeLevels();
        seedJobBenefits();
        seedProvinces();
        seedFilterGroups();
    }

    // ─── Roles & Admin ─────────────────────────────────────────────────────────

    private void seedRoles() {
        if (!roleRepository.existsByName(Role.ADMIN)) {
            roleRepository.save(new Role(Role.ADMIN, "Quản trị viên hệ thống"));
            System.out.println("✅ Created role: ROLE_ADMIN");
        }
        if (!roleRepository.existsByName(Role.CANDIDATE)) {
            roleRepository.save(new Role(Role.CANDIDATE, "Ứng viên tìm việc"));
            System.out.println("✅ Created role: ROLE_CANDIDATE");
        }
        if (!roleRepository.existsByName(Role.EMPLOYER)) {
            roleRepository.save(new Role(Role.EMPLOYER, "Nhà tuyển dụng"));
            System.out.println("✅ Created role: ROLE_EMPLOYER");
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@careerviet.vn";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(Role.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFullName("Administrator");
            adminUser.setPhone("0900000000");
            adminUser.setRole(adminRole);
            adminUser.setEnabled(true);
            userRepository.save(adminUser);
            System.out.println("✅ Created default admin: " + adminEmail);
        }
    }

    // ─── Industries ─────────────────────────────────────────────────────────────

    private void seedIndustries() {
        if (industryRepository.count() > 0) return;
        industryRepository.saveAll(List.of(
            new Industry("CNTT",       "Công nghệ thông tin",        1),
            new Industry("Marketing",  "Marketing",                   2),
            new Industry("Kinh doanh", "Kinh doanh / Bán hàng",      3),
            new Industry("Kế toán",    "Kế toán / Kiểm toán",        4),
            new Industry("Nhân sự",    "Nhân sự / Hành chính",       5),
            new Industry("Kỹ thuật",   "Kỹ thuật / Cơ khí",          6),
            new Industry("Xây dựng",   "Xây dựng / Kiến trúc",       7),
            new Industry("Giáo dục",   "Giáo dục / Đào tạo",         8),
            new Industry("Y tế",       "Y tế / Dược phẩm",           9),
            new Industry("Tài chính",  "Tài chính / Ngân hàng",      10),
            new Industry("Logistics",  "Logistics / Xuất nhập khẩu", 11),
            new Industry("Bán lẻ",     "Bán lẻ / Tiêu dùng",         12),
            new Industry("Nhà hàng",   "Nhà hàng / Khách sạn",       13),
            new Industry("Luật",       "Pháp lý / Luật",             14),
            new Industry("Thiết kế",   "Thiết kế / Sáng tạo",        15),
            new Industry("Môi trường", "Môi trường / Nông nghiệp",   16),
            new Industry("Vận tải",    "Vận tải / Lái xe",           17),
            new Industry("Bảo hiểm",  "Bảo hiểm",                   18),
            new Industry("Khác",       "Ngành nghề khác",             19)
        ));
        System.out.println("✅ Seeded industries");
    }

    // ─── Experience Levels ───────────────────────────────────────────────────────

    private void seedExperienceLevels() {
        if (experienceLevelRepository.count() > 0) return;
        experienceLevelRepository.saveAll(List.of(
            new ExperienceLevel("Chưa có kinh nghiệm", "Chưa có kinh nghiệm", 1),
            new ExperienceLevel("Dưới 1 năm",           "Dưới 1 năm",           2),
            new ExperienceLevel("1-2 năm",               "1 - 2 năm",            3),
            new ExperienceLevel("2-5 năm",               "2 - 5 năm",            4),
            new ExperienceLevel("Trên 5 năm",            "Trên 5 năm",           5)
        ));
        System.out.println("✅ Seeded experience levels");
    }

    // ─── Education Levels ────────────────────────────────────────────────────────

    private void seedEducationLevels() {
        if (educationLevelRepository.count() > 0) return;
        educationLevelRepository.saveAll(List.of(
            new EducationLevel("Không yêu cầu", "Không yêu cầu", 1),
            new EducationLevel("Trung cấp",      "Trung cấp",      2),
            new EducationLevel("Cao đẳng",       "Cao đẳng",       3),
            new EducationLevel("Đại học",        "Đại học",        4),
            new EducationLevel("Thạc sĩ",        "Thạc sĩ",        5),
            new EducationLevel("Tiến sĩ",        "Tiến sĩ",        6)
        ));
        System.out.println("✅ Seeded education levels");
    }
    private void seedDegreeLevels() {
        if (degreeLevelRepository.count() > 0) return;
        degreeLevelRepository.saveAll(List.of(
            new DegreeLevel("Không yêu cầu bằng cấp", "Không yêu cầu bằng cấp", 1),
            new DegreeLevel("Trung học",                "Trung học",                2),
            new DegreeLevel("Trung cấp",                "Trung cấp",                3),
            new DegreeLevel("Cao đẳng",                 "Cao đẳng",                 4),
            new DegreeLevel("Đại học",                  "Đại học",                  5),
            new DegreeLevel("Sau đại học",              "Sau đại học",              6)
        ));
        System.out.println("✅ Seeded degree levels");
    }

    // ─── Job Benefits ────────────────────────────────────────────────────────────

    private void seedJobBenefits() {
        if (jobBenefitRepository.count() > 0) return;
        jobBenefitRepository.saveAll(List.of(
            new JobBenefit("Chế độ bảo hiểm",   "Chế độ bảo hiểm",   1),
            new JobBenefit("Chăm sóc sức khỏe", "Chăm sóc sức khỏe", 2),
            new JobBenefit("Du Lịch",            "Du Lịch",            3),
            new JobBenefit("Đào tạo",            "Đào tạo",            4),
            new JobBenefit("Tăng lương",         "Tăng lương",         5),
            new JobBenefit("Laptop",             "Laptop",             6),
            new JobBenefit("Công tác phí",       "Công tác phí",       7),
            new JobBenefit("Du lịch nước ngoài", "Du lịch nước ngoài", 8),
            new JobBenefit("Nghỉ phép năm",      "Nghỉ phép năm",      9),
            new JobBenefit("Thưởng KPI",         "Thưởng KPI",         10),
            new JobBenefit("Xe đưa đón",         "Xe đưa đón",         11),
            new JobBenefit("Ăn trưa",            "Ăn trưa",            12),
            new JobBenefit("Chế độ thưởng",      "Chế độ thưởng",      13)
        ));
        System.out.println("✅ Seeded job benefits");
    }

    // ─── Provinces ───────────────────────────────────────────────────────────────

    private void seedProvinces() {
        if (provinceRepository.count() == 35) return; // da co du 35 tinh thanh moi
        provinceRepository.deleteAll();
        provinceRepository.saveAll(List.of(
            // ── Toàn quốc ──
            new Province("Toàn quốc",    "Toàn quốc",     0),
            // ── Thành phố trực thuộc TW (sáp nhập) ──
            new Province("Hà Nội",       "Hà Nội",         1),
            new Province("Hồ Chí Minh",  "TP. Hồ Chí Minh (+ Bình Dương, Bà Rịa–Vũng Tàu)",  2),
            new Province("Đà Nẵng",      "Đà Nẵng (+ Quảng Nam)",       3),
            new Province("Hải Phòng",    "Hải Phòng (+ Hải Dương)",     4),
            new Province("Cần Thơ",      "Cần Thơ (+ Sóc Trăng, Hậu Giang)", 5),
            new Province("Huế",          "Huế",            6),
            // ── Tỉnh sáp nhập ──
            new Province("Tuyên Quang",  "Tuyên Quang (+ Hà Giang)",    7),
            new Province("Lào Cai",      "Lào Cai (+ Yên Bái)",         8),
            new Province("Thái Nguyên",  "Thái Nguyên (+ Bắc Kạn)",    9),
            new Province("Phú Thọ",      "Phú Thọ (+ Vĩnh Phúc, Hòa Bình)", 10),
            new Province("Bắc Ninh",     "Bắc Ninh (+ Bắc Giang)",     11),
            new Province("Hưng Yên",     "Hưng Yên (+ Thái Bình)",     12),
            new Province("Ninh Bình",    "Ninh Bình (+ Hà Nam, Nam Định)", 13),
            new Province("Quảng Trị",    "Quảng Trị (+ Quảng Bình)",   14),
            new Province("Quảng Ngãi",   "Quảng Ngãi (+ Kon Tum)",     15),
            new Province("Gia Lai",      "Gia Lai (+ Bình Định)",       16),
            new Province("Khánh Hòa",    "Khánh Hòa (+ Ninh Thuận)",   17),
            new Province("Lâm Đồng",    "Lâm Đồng (+ Đắk Nông, Bình Thuận)", 18),
            new Province("Đắk Lắk",     "Đắk Lắk (+ Phú Yên)",        19),
            new Province("Đồng Nai",     "Đồng Nai (+ Bình Phước)",    20),
            new Province("Tây Ninh",     "Tây Ninh (+ Long An)",        21),
            new Province("Vĩnh Long",    "Vĩnh Long (+ Bến Tre, Trà Vinh)", 22),
            new Province("Đồng Tháp",    "Đồng Tháp (+ Tiền Giang)",   23),
            new Province("Cà Mau",       "Cà Mau (+ Bạc Liêu)",        24),
            new Province("An Giang",     "An Giang (+ Kiên Giang)",     25),
            // ── Tỉnh giữ nguyên ──
            new Province("Lai Châu",     "Lai Châu",       26),
            new Province("Điện Biên",    "Điện Biên",      27),
            new Province("Sơn La",       "Sơn La",         28),
            new Province("Lạng Sơn",     "Lạng Sơn",       29),
            new Province("Quảng Ninh",   "Quảng Ninh",     30),
            new Province("Thanh Hóa",    "Thanh Hóa",      31),
            new Province("Nghệ An",      "Nghệ An",        32),
            new Province("Hà Tĩnh",      "Hà Tĩnh",        33),
            new Province("Cao Bằng",     "Cao Bằng",       34)
        ));
        System.out.println("✅ Seeded 35 provinces (sau sáp nhập)");
    }

    // ─── Dynamic Filter Groups & Options ─────────────────────────────────────────

    private void seedFilterGroups() {
        if (filterGroupRepository.count() > 0) return;

        Nhom08.Project.entity.FilterGroup salary = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("salary", "Mức lương", 1));
        Nhom08.Project.entity.FilterGroup level = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("level", "Cấp bậc", 2));
        Nhom08.Project.entity.FilterGroup posted = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("posted_within", "Đăng trong vòng", 3));
        Nhom08.Project.entity.FilterGroup empType = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("employment_type", "Hình thức việc làm", 4));
        Nhom08.Project.entity.FilterGroup exp = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("experience", "Kinh nghiệm làm việc", 5));
        Nhom08.Project.entity.FilterGroup rank = filterGroupRepository.save(
            new Nhom08.Project.entity.FilterGroup("job_rank", "Công việc làm khiến cấp", 6));

        filterOptionRepository.saveAll(List.of(
            new Nhom08.Project.entity.FilterOption(salary,   "Dưới 10 triệu",  "Dưới 10 triệu",  1),
            new Nhom08.Project.entity.FilterOption(salary,   "10-15 triệu",    "10 - 15 triệu",  2),
            new Nhom08.Project.entity.FilterOption(salary,   "15-20 triệu",    "15 - 20 triệu",  3),
            new Nhom08.Project.entity.FilterOption(salary,   "20-30 triệu",    "20 - 30 triệu",  4),
            new Nhom08.Project.entity.FilterOption(salary,   "Trên 30 triệu",  "Trên 30 triệu",  5),

            new Nhom08.Project.entity.FilterOption(level,    "Thực tập sinh",  "Thực tập sinh",  1),
            new Nhom08.Project.entity.FilterOption(level,    "Nhân viên",      "Nhân viên",      2),
            new Nhom08.Project.entity.FilterOption(level,    "Trưởng nhóm",    "Trưởng nhóm",    3),
            new Nhom08.Project.entity.FilterOption(level,    "Quản lý",        "Quản lý",        4),

            new Nhom08.Project.entity.FilterOption(posted,   "24 giờ qua",     "24 giờ qua",     1),
            new Nhom08.Project.entity.FilterOption(posted,   "7 ngày qua",     "7 ngày qua",     2),
            new Nhom08.Project.entity.FilterOption(posted,   "30 ngày qua",    "30 ngày qua",    3),

            new Nhom08.Project.entity.FilterOption(empType,  "Toàn thời gian", "Toàn thời gian", 1),
            new Nhom08.Project.entity.FilterOption(empType,  "Bán thời gian",  "Bán thời gian",  2),
            new Nhom08.Project.entity.FilterOption(empType,  "Remote",         "Remote",         3),

            new Nhom08.Project.entity.FilterOption(exp,      "Chưa có kinh nghiệm", "Chưa có kinh nghiệm", 1),
            new Nhom08.Project.entity.FilterOption(exp,      "Dưới 1 năm",          "Dưới 1 năm",          2),
            new Nhom08.Project.entity.FilterOption(exp,      "1-2 năm",             "1-2 năm",             3),
            new Nhom08.Project.entity.FilterOption(exp,      "3-5 năm",             "3-5 năm",             4),
            new Nhom08.Project.entity.FilterOption(exp,      "Trên 5 năm",          "Trên 5 năm",          5),

            new Nhom08.Project.entity.FilterOption(rank,     "Thực tập",       "Thực tập",       1),
            new Nhom08.Project.entity.FilterOption(rank,     "Mới đi làm",     "Mới đi làm",     2),
            new Nhom08.Project.entity.FilterOption(rank,     "Có kinh nghiệm", "Có kinh nghiệm", 3),
            new Nhom08.Project.entity.FilterOption(rank,     "Chuyên gia",     "Chuyên gia",     4)
        ));
        System.out.println("✅ Seeded filter groups & options");
    }
}
