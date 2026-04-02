package Nhom08.Project.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Nhom08.Project.entity.ChatMessage;
import Nhom08.Project.entity.ChatSession;
import Nhom08.Project.entity.Job;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.ChatMessageRepository;
import Nhom08.Project.repository.ChatSessionRepository;
import Nhom08.Project.repository.JobRepository;

/**
 * Service xử lý chatbot với Gemini AI tích hợp
 * Hỗ trợ tìm việc, gợi ý job phù hợp
 */
@Service
public class ContactChatbotService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private JobRepository jobRepository;

    // ===== System Prompt cho Chatbot =====
    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI của website JCO (Job Connection Online) - hệ thống quản lý tuyển dụng.

        PHẠM VI HỖ TRỢ - CHỈ trả lời các câu hỏi liên quan đến:
        1. Tìm kiếm việc làm trên website JCO
        2. Xem và ứng tuyển vào các tin tuyển dụng đang có trên hệ thống
        3. Quản lý CV: tạo CV, tải CV lên, xem CV của bản thân
        4. Quản lý hồ sơ ứng tuyển: xem trạng thái đơn ứng tuyển, lịch sử ứng tuyển
        5. Hỗ trợ nhà tuyển dụng: đăng tin tuyển dụng, quản lý ứng viên trên hệ thống
        6. Hướng dẫn sử dụng các tính năng của website JCO

        NGUYÊN TẮC TỪ CHỐI:
        - Nếu câu hỏi KHÔNG liên quan đến các chức năng của website JCO (tuyển dụng, CV, hồ sơ, đăng tin) -> Từ chối lịch sự và nhắc lại phạm vi hỗ trợ
        - KHÔNG trả lời các câu hỏi chung về: lập trình, học tập, tư vấn nghề nghiệp, thị trường lao động, chuẩn bị phỏng vấn, kiến thức kỹ thuật, hay bất kỳ chủ đề nào không liên quan đến hệ thống JCO
        - Khi từ chối, dùng mẫu: "Mình chỉ hỗ trợ các vấn đề liên quan đến website JCO như tìm việc, quản lý CV và hồ sơ ứng tuyển. Bạn có câu hỏi nào về những chức năng này không?"

        NGUYÊN TẮC TRẢ LỜI:
        - Trả lời ngắn gọn, thân thiện (dưới 100 từ)
        - LUÔN xưng là "mình" và gọi người dùng là "bạn"
        - Dùng ngôn ngữ tự nhiên, thân thiện
        - Không tiết lộ thông tin nội bộ của hệ thống
        - KHÔNG TỰ THÊM LINK vào câu trả lời

        XỬ LÝ NGÔN NGỮ VIỆT (CÓ DẤU & KHÔNG DẤU):
        - Người dùng có thể viết CÓ DẤU hoặc KHÔNG DẤU, bạn đều phải hiểu và trả lời đúng
        - Ví dụ: "viec lam" = "việc làm", "it" = "IT", "luong 10 trieu" = "lương 10 triệu"

        PHÂN TÍCH YÊU CẦU TÌM VIỆC - TÊN CÔNG VIỆC:
        - Hiểu tên công việc có dấu: "Lập trình viên", "Kế toán", "Marketing", "Nhân sự"
        - Hiểu tên công việc không dấu: "Lap trinh vien", "Ke toan", "IT", "HR", "Sale"
        - Hiểu tiếng Anh: "Developer", "Designer", "Accountant", "Manager"
        - Hiểu viết tắt: "Dev", "BA", "QA", "PM"

        PHÂN TÍCH YÊU CẦU TÌM VIỆC - ĐỊA ĐIỂM:
        - Hiểu địa điểm có dấu: "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng"
        - Hiểu địa điểm không dấu: "Ha Noi", "Hanoi", "HCM", "Da Nang", "DN", "Hai Phong", "HP"
        - Hiểu viết tắt: "HCM", "HN", "Sài Gòn" = "TP.HCM"

        PHÂN TÍCH YÊU CẦU TÌM VIỆC - MỨC LƯƠNG:
        - Hiểu format đầy đủ: "10 triệu", "15.000.000", "10tr", "10 tr"
        - Hiểu format khoảng: "7-12 triệu", "7-12tr"
        - Hiểu format tối thiểu: "từ 10 triệu", "min 10tr"
        - Hiểu format tối đa: "tối đa 15 triệu", "max 15tr"

        VÍ DỤ PHÂN TÍCH TÌM VIỆC:
        - "viec it hcm 10-15tr" → IT, TP.HCM, lương 10-15 triệu
        - "lap trinh vien ha noi 10 trieu" → Lập trình viên, Hà Nội, lương 10 triệu

        VÍ DỤ TỪ CHỐI:
        - "Testcase là gì?" → Từ chối vì không liên quan đến JCO
        - "Học lập trình như thế nào?" → Từ chối vì không liên quan đến JCO
        - "Chuẩn bị phỏng vấn thế nào?" → Từ chối vì không liên quan đến JCO
        """;

    /**
     * Tạo session chat mới hoặc lấy session cũ
     * Ưu tiên: existingSessionId → session active gần nhất → session mới
     */
    @Transactional
    public ChatSession createOrGetSession(User user, String existingSessionId) {
        System.out.println("=== createOrGetSession Called ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "Guest"));
        System.out.println("Existing Session ID: " + existingSessionId);

        // 1. Nếu có existingSessionId, thử lấy session cũ
        if (existingSessionId != null && !existingSessionId.isEmpty()) {
            System.out.println("Looking for existing session...");
            Optional<ChatSession> existingSession = sessionRepository.findBySessionIdWithMessages(existingSessionId);

            if (existingSession.isPresent()) {
                ChatSession session = existingSession.get();

                // Kiểm tra user có quyền truy cập session này không
                if (!canUserAccessSession(existingSessionId, user)) {
                    System.out.println("❌ User does not have access to this session.");
                } else {
                    System.out.println("✅ Found existing session! Reusing it.");
                    System.out.println("Session messages count: " + (session.getMessages() != null ? session.getMessages().size() : "0"));

                    // Cập nhật updatedAt để đánh dấu session còn active
                    session.setUpdatedAt(LocalDateTime.now());
                    sessionRepository.save(session);
                    System.out.println("✅ Session updatedAt refreshed");

                    return session;
                }
            } else {
                System.out.println("❌ Existing session not found in database.");
            }
        }

        // 2. Nếu không tìm thấy existing session và user đã login, tìm session active gần nhất
        if (user != null) {
            System.out.println("Looking for most recent active session for user...");
            List<ChatSession> activeSessions = sessionRepository.findActiveSessionsByUserIdOrderByUpdatedAtDesc(user.getId());

            if (!activeSessions.isEmpty()) {
                ChatSession session = activeSessions.get(0);
                System.out.println("✅ Found active session: " + session.getSessionId());

                // Load lại với messages
                session = sessionRepository.findBySessionIdWithMessages(session.getSessionId()).orElse(session);

                // Cập nhật updatedAt
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepository.save(session);

                return session;
            } else {
                System.out.println("❌ No active session found for user.");
            }
        }

        // 3. Tạo session mới
        System.out.println("Creating new session...");
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = user != null ? new ChatSession(user, sessionId) : new ChatSession(sessionId);
        session = sessionRepository.save(session);
        System.out.println("✅ New session created with ID: " + sessionId);

        // Thêm tin nhắn chào mừng
        ChatMessage welcomeMsg = new ChatMessage(session, user, "ASSISTANT", getWelcomeMessage());
        messageRepository.save(welcomeMsg);
        System.out.println("✅ Welcome message saved");

        // KHÔNG thêm tin nhắn gợi ý nữa - chỉ chào thôi
        // ChatMessage suggestionsMsg = new ChatMessage(session, user, "SYSTEM", buildSuggestionsMessage());
        // messageRepository.save(suggestionsMsg);
        // System.out.println("✅ Suggestions message saved");

        // Load lại session với messages để tránh lazy loading issue
        session = sessionRepository.findBySessionIdWithMessages(sessionId).orElse(session);
        System.out.println("✅ Session reloaded with messages");

        return session;
    }

    /**
     * Gửi tin nhắn và nhận phản hồi từ Gemini AI
     */
    @Transactional
    public Map<String, Object> sendMessage(String sessionId, String userMessage, User currentUser) {
        System.out.println("\n=== sendMessage Called ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("User Message: " + userMessage);

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Lấy session
            ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

            System.out.println("✅ Session found");

            // 2. Kiểm tra user có quyền truy cập session này không
            if (!canUserAccessSession(sessionId, currentUser)) {
                throw new RuntimeException("Bạn không có quyền truy cập session này");
            }

            System.out.println("✅ User access verified");

            // Cập nhật updatedAt để đánh dấu session còn active
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
            System.out.println("✅ Session updatedAt refreshed");

            // 3. Lưu tin nhắn của user
            ChatMessage userMsg = new ChatMessage(session, currentUser, "USER", userMessage);
            messageRepository.save(userMsg);
            System.out.println("✅ User message saved");

            // 3. Lấy lịch sử chat gần nhất để có context
            List<ChatMessage> history = messageRepository.findMessagesBySessionId(sessionId);
            System.out.println("✅ Found " + history.size() + " messages in database");

            StringBuilder contextBuilder = new StringBuilder();

            // Chỉ lấy 10 tin nhắn gần nhất
            int recentMessages = Math.min(history.size(), 10);
            for (int i = Math.max(0, history.size() - recentMessages); i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                if (!"SYSTEM".equals(msg.getRole())) { // Bỏ qua system messages
                    contextBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
                }
            }

            // 4. Xác định xem có cần gợi ý jobs hoặc CV không
            String response;
            if (shouldRecommendJobs(userMessage)) {
                System.out.println("Triggering job recommendations...");
                response = recommendJobs(userMessage);
            } else {
                // 5. Gọi Gemini API với context
                System.out.println("Calling Gemini AI...");
                String fullPrompt = SYSTEM_PROMPT + "\n\nLỊCH SỬ ĐỐI THOẠI GẦN NHẤT:\n" +
                    contextBuilder.toString() + "\n\nCÂU HỎI HIỆN TẠI: " + userMessage;

                response = geminiService.callGeminiPlainText(fullPrompt);

                // Nếu người dùng hỏi về chấm điểm CV, thêm link vào phản hồi (ưu tiên check trước)
                if (isAskingAboutCVScoring(userMessage)) {
                    response += "\n\n**Chấm điểm CV với AI:** [Chấm điểm CV ở đây!!!](http://localhost:8083/cham-diem-cv.html)";
                }
                // Nếu người dùng hỏi về CV (tạo CV, làm CV...), thêm link vào phản hồi
                // Nhưng KHÔNG thêm nếu đã là câu hỏi về chấm điểm CV
                else if (isAskingAboutCV(userMessage)) {
                    response += "\n\n**Tạo CV với AI:** [Tạo CV ở đây!!!](http://localhost:8083/tao-cv-ai.html)";
                }

                System.out.println("✅ AI response received");
            }

            // 6. Lưu phản hồi
            ChatMessage botMsg = new ChatMessage(session, currentUser, "ASSISTANT", response);
            messageRepository.save(botMsg);
            System.out.println("✅ Assistant message saved");

            // 7. Trả về kết quả
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("message", response);
            result.put("role", "ASSISTANT");
            result.put("timestamp", botMsg.getCreatedAt().toString());

        } catch (Exception e) {
            System.err.println("❌ Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return result;
    }

    /**
     * Kiểm tra xem có nên gợi ý jobs không
     * Mở rộng để phát hiện tên công việc, địa điểm, mức lương
     */
    private boolean shouldRecommendJobs(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        String normalizedMessage = normalizeText(message);

        // Check các từ khóa trực tiếp về việc làm
        boolean directKeywords = normalizedMessage.contains("goi y") || normalizedMessage.contains("recommend") ||
               normalizedMessage.contains("viec lam") || normalizedMessage.contains("job") ||
               normalizedMessage.contains("tuyen dung") || normalizedMessage.contains("ung tuyen") ||
               normalizedMessage.contains("tim viec") || normalizedMessage.contains("find job");

        if (directKeywords) {
            return true;
        }

        // Check tên công việc (có dấu và không dấu)
        boolean hasJobTitle = normalizedMessage.matches(".*(lap trinh|lập trình|developer|dev|backend|frontend|fullstack|full stack).*") ||
                             normalizedMessage.matches(".*(ke toan|kế toán|accountant|accounting).*") ||
                             normalizedMessage.matches(".*(nhan su|nhân sự|hr|human resource).*") ||
                             normalizedMessage.matches(".*(marketing|digital marketing|seo).*") ||
                             normalizedMessage.matches(".*(sale|sales|kinh doanh|business).*") ||
                             normalizedMessage.matches(".*(it|cong nghe thong tin|công nghệ thông tin|software).*") ||
                             normalizedMessage.matches(".*(thiet ke|thiết kế|design|designer).*") ||
                             normalizedMessage.matches(".*(bao ve|bảo vệ|security).*") ||
                             normalizedMessage.matches(".*(quan ly|quản lý|manager|management).*") ||
                             normalizedMessage.matches(".*(ho tro|hỗ trợ|support|assistant).*") ||
                             normalizedMessage.matches(".*(tu van|tư vấn|consultant|consulting).*") ||
                             normalizedMessage.matches(".*(giao vien|giáo viên|teacher).*") ||
                             normalizedMessage.matches(".*(bao cao|báo cáo|reporter).*") ||
                             normalizedMessage.matches(".*(noi that|nội thất|interior).*") ||
                             normalizedMessage.matches(".*(xay dung|xy dựng|construction).*") ||
                             normalizedMessage.matches(".*(ky su|kỹ sư|engineer).*") ||
                             normalizedMessage.matches(".*(chuyen vien|chuyên viên|specialist).*");

        if (hasJobTitle) {
            return true;
        }

        // Check địa điểm
        boolean hasLocation = normalizedMessage.matches(".*(tp|thanh pho|tinh|tỉnh)\\s+\\w+.*") ||
                             normalizedMessage.contains("hcm") || normalizedMessage.contains("ho chi minh") || normalizedMessage.contains("sai gon") || normalizedMessage.contains("sg") ||
                             normalizedMessage.contains("ha noi") || normalizedMessage.contains("hanoi") ||
                             normalizedMessage.contains("da nang") || normalizedMessage.contains("danang") ||
                             normalizedMessage.contains("hai phong") || normalizedMessage.contains("haiphong") ||
                             normalizedMessage.contains("can tho") || normalizedMessage.contains("cantho") ||
                             normalizedMessage.contains("hue") || normalizedMessage.contains("long an") ||
                             normalizedMessage.contains("binh duong") || normalizedMessage.contains("dong nai");

        if (hasLocation) {
            return true;
        }

        // Check mức lương (có số + đơn vị tiền tệ)
        boolean hasSalary = normalizedMessage.matches(".*\\d+\\s*(trieu|tr|k|nghin|million|m|usd|\\$).*");

        return hasSalary;
    }

    /**
     * Kiểm tra xem người dùng có hỏi về CV không
     */
    private boolean isAskingAboutCV(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("cv") || lowerMessage.contains("resume") ||
               lowerMessage.contains("sơ yếu lý lịch") || lowerMessage.contains("tạo cv") ||
               lowerMessage.contains("viết cv") || lowerMessage.contains("làm cv");
    }

    /**
     * Kiểm tra xem người dùng có hỏi về chấm điểm CV không
     */
    private boolean isAskingAboutCVScoring(String message) {
        String normalized = normalizeText(message);
        return normalized.contains("cham diem cv") || normalized.contains("cham cv") ||
               normalized.contains("review cv") || normalized.contains("danh gia cv") ||
               normalized.contains("kiem tra cv") || normalized.contains("scoring cv") ||
               normalized.contains("cham diem resume") || normalized.contains("cv scoring");
    }

    /**
     * Gợi ý jobs phù hợp theo tiêu chí (tên công việc, địa điểm, mức lương)
     */
    private String recommendJobs(String userQuery) {
        try {
            System.out.println("Fetching jobs from database...");
            System.out.println("User query: " + userQuery);

            // Lấy các jobs đang active
            List<Job> activeJobs = jobRepository.findByStatus("ACTIVE");

            if (activeJobs.isEmpty()) {
                return "Hiện tại chưa có công việc nào đang tuyển dụng. Vui lòng quay lại sau!\n\n**Xem tất cả việc làm: Tìm việc ở đây!!!** [Tìm việc làm ở đây!!!](http://localhost:8083/tim-viec-lam.html)";
            }

            System.out.println("✅ Found " + activeJobs.size() + " active jobs");

            // Phân tích câu hỏi để tìm các tiêu chí
            String normalizedQuery = normalizeText(userQuery);
            String location = extractLocation(normalizedQuery);
            String industry = extractIndustry(normalizedQuery);
            String jobTitle = extractJobTitle(normalizedQuery);
            long[] salaryRange = extractSalaryRange(normalizedQuery);

            System.out.println("Extracted criteria - Location: " + location + ", Industry: " + industry + ", JobTitle: " + jobTitle + ", Salary: " + (salaryRange != null ? salaryRange[0] + "-" + salaryRange[1] : "N/A"));

            // Lọc jobs theo các tiêu chí
            List<Job> filteredJobs = activeJobs;

            if (location != null) {
                filteredJobs = filterJobsByLocation(filteredJobs, location);
                System.out.println("✅ After location filter: " + filteredJobs.size() + " jobs");
            }

            if (industry != null) {
                filteredJobs = filterJobsByIndustry(filteredJobs, industry);
                System.out.println("✅ After industry filter: " + filteredJobs.size() + " jobs");
            }

            if (jobTitle != null) {
                filteredJobs = filterJobsByTitle(filteredJobs, jobTitle);
                System.out.println("✅ After job title filter: " + filteredJobs.size() + " jobs");
            }

            if (salaryRange != null) {
                filteredJobs = filterJobsBySalary(filteredJobs, salaryRange[0], salaryRange[1]);
                System.out.println("✅ After salary filter: " + filteredJobs.size() + " jobs");
            }

            // Nếu không tìm thấy jobs phù hợp, gợi ý jobs tương tự
            if (filteredJobs.isEmpty()) {
                StringBuilder noResultsMsg = new StringBuilder();

                // Phân tích nguyên nhân không có kết quả
                List<Job> jobsAtLocation = location != null ? filterJobsByLocation(activeJobs, location) : null;
                boolean locationHasNoJobs = location != null && jobsAtLocation.isEmpty();
                boolean salaryMismatch = location != null && !jobsAtLocation.isEmpty() && salaryRange != null
                        && filterJobsBySalary(jobsAtLocation, salaryRange[0], salaryRange[1]).isEmpty();

                if (locationHasNoJobs) {
                    // Trường hợp 1: Địa điểm không có job nào
                    noResultsMsg.append(String.format("**Xin lỗi, hiện tại không có công việc nào tại %s**.\n\n", location));

                    // Gợi ý jobs ở các địa điểm khác (lấy ngẫu nhiên tối đa 3 job)
                    List<Job> otherJobs = activeJobs;
                    if (jobTitle != null) otherJobs = filterJobsByTitle(otherJobs, jobTitle);
                    if (industry != null) otherJobs = filterJobsByIndustry(otherJobs, industry);

                    if (!otherJobs.isEmpty()) {
                        noResultsMsg.append("**Gợi ý một số công việc tương tự ở địa điểm khác:**\n\n");
                        int sampleSize = Math.min(3, otherJobs.size());
                        for (int i = 0; i < sampleSize; i++) {
                            Job job = otherJobs.get(i);
                            noResultsMsg.append(String.format("**%d. %s**\n", (i + 1), job.getTitle()));
                            noResultsMsg.append(String.format("Công ty: %s\n", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty"));
                            noResultsMsg.append(String.format("Địa điểm: %s\n", job.getLocation() != null ? job.getLocation() : "Toàn quốc"));
                            String salaryDisplay = "Thỏa thuận";
                            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                                salaryDisplay = formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax());
                            }
                            noResultsMsg.append(String.format("Lương: %s\n", salaryDisplay));
                            noResultsMsg.append(String.format("**Xem chi tiết:** [Mô tả ở đây!!!](http://localhost:8083/job-detail.html?id=%d)\n\n", job.getId()));
                        }
                        if (otherJobs.size() > 3) {
                            noResultsMsg.append(String.format("Và %d công việc khác...\n\n", otherJobs.size() - 3));
                        }
                    }

                } else if (salaryMismatch) {
                    // Trường hợp 2: Địa điểm có job nhưng không đúng mức lương
                    String salaryDesc = salaryRange[1] == Long.MAX_VALUE
                            ? "từ " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[0]))
                            : formatSalary(java.math.BigDecimal.valueOf(salaryRange[0])) + " - " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[1]));
                    noResultsMsg.append(String.format("**Xin lỗi, hiện tại không có công việc nào tại %s với mức lương %s**.\n\n", location, salaryDesc));

                    // Gợi ý các job tại địa điểm đó (bỏ qua lương)
                    noResultsMsg.append(String.format("**Tuy nhiên, mình tìm thấy %d công việc tại %s với mức lương khác:**\n\n", jobsAtLocation.size(), location));
                    int sampleSize = Math.min(3, jobsAtLocation.size());
                    for (int i = 0; i < sampleSize; i++) {
                        Job job = jobsAtLocation.get(i);
                        noResultsMsg.append(String.format("**%d. %s**\n", (i + 1), job.getTitle()));
                        noResultsMsg.append(String.format("Công ty: %s\n", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty"));
                        String salaryDisplay = "Thỏa thuận";
                        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                            salaryDisplay = formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax());
                        }
                        noResultsMsg.append(String.format("Lương: %s\n", salaryDisplay));
                        noResultsMsg.append(String.format("**Xem chi tiết:** [Mô tả ở đây!!!](http://localhost:8083/job-detail.html?id=%d)\n\n", job.getId()));
                    }
                    if (jobsAtLocation.size() > 3) {
                        noResultsMsg.append(String.format("Và %d công việc khác tại %s...\n\n", jobsAtLocation.size() - 3, location));
                    }

                } else {
                    // Trường hợp 3: Các tiêu chí khác không khớp (ngành, chức danh...)
                    noResultsMsg.append("Xin lỗi, hiện tại không có công việc nào phù hợp với tìm kiếm của bạn");
                    java.util.List<String> criteria = new java.util.ArrayList<>();
                    if (location != null) criteria.add("tại " + location);
                    if (jobTitle != null) criteria.add("vị trí " + jobTitle);
                    if (industry != null) criteria.add("lĩnh vực " + industry);
                    if (salaryRange != null && salaryRange[1] != Long.MAX_VALUE) {
                        criteria.add("lương " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[0])) + " - " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[1])));
                    } else if (salaryRange != null) {
                        criteria.add("lương từ " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[0])));
                    }
                    if (!criteria.isEmpty()) noResultsMsg.append(" ").append(String.join(", ", criteria));
                    noResultsMsg.append(".\n\n");

                    // Gợi ý mở rộng lương ±30%
                    if (salaryRange != null) {
                        long expandedMin = (long) (salaryRange[0] * 0.7);
                        long expandedMax = salaryRange[1] == Long.MAX_VALUE ? (long) (salaryRange[0] * 1.5) : (long) (salaryRange[1] * 1.3);
                        List<Job> similarJobs = filterJobsBySalary(activeJobs, expandedMin, expandedMax);
                        if (!similarJobs.isEmpty()) {
                            noResultsMsg.append("**Gợi ý một số công việc với mức lương gần tương đương:**\n\n");
                            int sampleSize = Math.min(3, similarJobs.size());
                            for (int i = 0; i < sampleSize; i++) {
                                Job job = similarJobs.get(i);
                                noResultsMsg.append(String.format("**%d. %s**\n", (i + 1), job.getTitle()));
                                noResultsMsg.append(String.format("Công ty: %s\n", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty"));
                                noResultsMsg.append(String.format("Địa điểm: %s\n", job.getLocation() != null ? job.getLocation() : "Toàn quốc"));
                                String salaryDisplay = "Thỏa thuận";
                                if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                                    salaryDisplay = formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax());
                                }
                                noResultsMsg.append(String.format("Lương: %s\n", salaryDisplay));
                                noResultsMsg.append(String.format("**Xem chi tiết:** [Mô tả ở đây!!!](http://localhost:8083/job-detail.html?id=%d)\n\n", job.getId()));
                            }
                            if (similarJobs.size() > 3) {
                                noResultsMsg.append(String.format("Và %d công việc khác...\n\n", similarJobs.size() - 3));
                            }
                        }
                    }
                }

                noResultsMsg.append("**Xem tất cả việc làm:** [Tìm việc làm ở đây!!!](http://localhost:8083/tim-viec-lam.html)");
                return noResultsMsg.toString();
            }

            // Lấy tối đa 5 jobs để gợi ý
            int sampleSize = Math.min(5, filteredJobs.size());
            List<Job> suggestedJobs = filteredJobs.subList(0, sampleSize);

            StringBuilder response = new StringBuilder();

            // Tạo tiêu đề phù hợp với bộ lọc (không hiển thị lương nếu là unlimited)
            java.util.List<String> filterDescriptions = new java.util.ArrayList<>();
            if (location != null) filterDescriptions.add("tại " + location);
            if (jobTitle != null) filterDescriptions.add("vị trí " + jobTitle);
            if (industry != null) filterDescriptions.add("lĩnh vực " + industry);
            if (salaryRange != null && salaryRange[1] != Long.MAX_VALUE) {
                filterDescriptions.add("lương " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[0])) +
                    " - " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[1])));
            } else if (salaryRange != null && salaryRange[1] == Long.MAX_VALUE) {
                filterDescriptions.add("lương từ " + formatSalary(java.math.BigDecimal.valueOf(salaryRange[0])));
            }

            if (!filterDescriptions.isEmpty()) {
                response.append(String.format("**Tìm thấy %d công việc %s:**\n\n",
                    filteredJobs.size(), String.join(", ", filterDescriptions)));
            } else {
                response.append("**Dưới đây là một số công việc phù hợp:**\n\n");
            }

            for (int i = 0; i < suggestedJobs.size(); i++) {
                Job job = suggestedJobs.get(i);
                response.append(String.format("**%d. %s**\n", (i + 1), job.getTitle()));
                response.append(String.format("Công ty: %s\n", job.getEmployer() != null ? job.getEmployer().getCompanyName() : "Công ty"));
                response.append(String.format("Địa điểm: %s\n", job.getLocation() != null ? job.getLocation() : "Toàn quốc"));

                // Format lương đẹp hơn
                String salaryDisplay = "Thỏa thuận";
                if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                    salaryDisplay = formatSalary(job.getSalaryMin()) + " - " + formatSalary(job.getSalaryMax());
                }
                response.append(String.format("Lương: %s\n", salaryDisplay));

                response.append(String.format("**Xem chi tiết: ** [Mô tả ở đây!!!](http://localhost:8083/job-detail.html?id=%d)\n\n", job.getId()));
            }

            // Thêm thông tin về số lượng kết quả
            if (filteredJobs.size() > 5) {
                response.append(String.format("Và %d công việc khác...\n\n", filteredJobs.size() - 5));
            }

            response.append("**Xem tất cả việc làm:** [Tìm việc làm ở đây!!!](http://localhost:8083/tim-viec-lam.html)");

            String result = response.toString();
            System.out.println("✅ Job recommendations generated: " + result.length() + " characters");

            return result;

        } catch (Exception e) {
            System.err.println("❌ Error recommending jobs: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, có lỗi khi tải danh sách việc làm. Vui lòng thử lại!";
        }
    }

    /**
     * Lấy câu chào mặc định
     */
    private String getWelcomeMessage() {
        return "Xin chào! Mình là trợ lý AI của JCO.\n\n" +
            "Mình có thể giúp bạn:\n" +
            "• Chấm điểm CV bằng AI\n" +
            "• Tạo CV mới\n" +
            "• Tìm kiếm công việc phù hợp\n" +
            "• Tư vấn viết CV chuyên nghiệp\n\n" +
            "Bạn cần hỗ trợ gì không?";
    }

    /**
     * Lấy lịch sử chat
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        System.out.println("=== getChatHistory Called ===");
        System.out.println("Session ID: " + sessionId);

        List<ChatMessage> messages = messageRepository.findMessagesBySessionId(sessionId);
        System.out.println("✅ Found " + messages.size() + " messages in database");

        // Log từng message để debug
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            System.out.println("  Message " + i + ": role=" + msg.getRole() +
                             ", content_length=" + (msg.getContent() != null ? msg.getContent().length() : 0));
        }

        return messages;
    }

    /**
     * Xóa sessions không hoạt động quá 24 giờ
     * Dựa trên updatedAt (thời gian cuối cùng tương tác) thay vì createdAt
     */
    @Transactional
    public int cleanupOldSessions() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        System.out.println("=== Cleanup Called ===");
        System.out.println("Looking for sessions inactive since: " + oneDayAgo);

        // Lấy tất cả sessions không hoạt động quá 24h (dựa trên updatedAt)
        List<ChatSession> oldSessions = sessionRepository.findAll().stream()
            .filter(s -> s.getUpdatedAt().isBefore(oneDayAgo))
            .toList();

        System.out.println("Found " + oldSessions.size() + " inactive sessions");

        int deletedCount = 0;
        for (ChatSession session : oldSessions) {
            try {
                // Xóa tất cả messages của session
                messageRepository.deleteBySessionId(session.getId());
                // Xóa session
                sessionRepository.delete(session);
                deletedCount++;
                System.out.println("✅ Deleted session: " + session.getSessionId() +
                                 " (last active: " + session.getUpdatedAt() + ")");
            } catch (Exception e) {
                // Log error but continue
                System.err.println("❌ Error deleting session: " + e.getMessage());
            }
        }

        System.out.println("=== Cleanup Complete ===");
        System.out.println("Total deleted: " + deletedCount + " sessions");

        return deletedCount;
    }

    /**
     * Xóa session cụ thể
     */
    @Transactional
    public void deleteSession(String sessionId) {
        System.out.println("=== deleteSession Called ===");
        System.out.println("Session ID: " + sessionId);

        ChatSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        System.out.println("Session found. Deleting...");

        // Xóa tất cả messages
        messageRepository.deleteBySessionId(session.getId());
        System.out.println("✅ Messages deleted");

        // Xóa session
        sessionRepository.delete(session);
        System.out.println("✅ Session deleted");
    }

    /**
     * Kiểm tra user có quyền truy cập session không
     * - Session của guest (không có user) -> chỉ guest dùng được, user đã login KHÔNG dùng lại
     * - Session của user đã đăng nhập -> chỉ user đó mới truy cập được
     */
    public boolean canUserAccessSession(String sessionId, User user) {
        try {
            System.out.println("=== canUserAccessSession Called ===");
            System.out.println("Session ID: " + sessionId);
            System.out.println("User: " + (user != null ? user.getEmail() + " (ID: " + user.getId() + ")" : "Guest"));

            Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isEmpty()) {
                System.out.println("❌ Session not found");
                return false;
            }

            ChatSession session = sessionOpt.get();

            // Nếu session là guest (không có user)
            if (session.getUser() == null) {
                // Chỉ guest user mới dùng được guest session
                // User đã login PHẢI tạo session riêng
                if (user == null) {
                    System.out.println("✅ Guest accessing guest session - OK");
                    return true;
                } else {
                    System.out.println("❌ Logged-in user trying to access guest session - MUST create new session");
                    return false;
                }
            }

            // Nếu session có user -> chỉ user đó mới truy cập được
            if (user == null) {
                // Guest user đang cố truy cập session của user đã login
                System.out.println("❌ Guest user trying to access session of user ID: " + session.getUser().getId());
                return false;
            }

            // Kiểm tra user ID có khớp không
            boolean hasAccess = session.getUser().getId().equals(user.getId());
            System.out.println("Session user ID: " + session.getUser().getId() + ", Current user ID: " + user.getId());
            System.out.println(hasAccess ? "✅ Access granted" : "❌ Access denied");

            return hasAccess;

        } catch (Exception e) {
            System.err.println("Error checking session access: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra session tồn tại
     */
    public boolean sessionExists(String sessionId) {
        boolean exists = sessionRepository.findBySessionId(sessionId).isPresent();
        System.out.println("Session exists check for " + sessionId + ": " + exists);
        return exists;
    }

    /**
     * Format lương từ BigDecimal sang dạng dễ đọc (ví dụ: 7.8 Tr)
     */
    private String formatSalary(java.math.BigDecimal salary) {
        if (salary == null) {
            return "Thỏa thuận";
        }

        // Chia cho 1,000,000 để ra số triệu
        double salaryInMillion = salary.doubleValue() / 1_000_000;

        // Làm tròn đến 1 chữ số thập phân
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.#");
        String formatted = df.format(salaryInMillion);

        return formatted + " Tr";
    }

    // ===== Helper Methods cho phân tích và lọc jobs =====

    /**
     * Loại bỏ dấu tiếng Việt
     */
    private String removeAccents(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String nfdNormalizedString = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     * Chuẩn hóa text để so sánh (thường + không dấu)
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return removeAccents(text.toLowerCase().trim());
    }

    /**
     * Trích xuất location từ câu hỏi (hỗ trợ có dấu/không dấu)
     */
    private String extractLocation(String userQuery) {
        if (userQuery == null || userQuery.isEmpty()) {
            return null;
        }

        String normalizedQuery = normalizeText(userQuery);

        // Danh sách các location phổ biến
        java.util.Map<String, String> locationKeywords = new java.util.HashMap<>();
        locationKeywords.put("tp ho chi minh", "TP. Hồ Chí Minh");
        locationKeywords.put("tp.ho chi minh", "TP. Hồ Chí Minh");
        locationKeywords.put("ho chi minh", "TP. Hồ Chí Minh");
        locationKeywords.put("hcm", "TP. Hồ Chí Minh");
        locationKeywords.put("tphcm", "TP. Hồ Chí Minh");
        locationKeywords.put("sai gon", "TP. Hồ Chí Minh");
        locationKeywords.put("saigon", "TP. Hồ Chí Minh");
        locationKeywords.put("sg", "TP. Hồ Chí Minh");

        locationKeywords.put("ha noi", "Hà Nội");
        locationKeywords.put("hà nội", "Hà Nội");
        locationKeywords.put("hn", "Hà Nội");

        locationKeywords.put("da nang", "Đà Nẵng");
        locationKeywords.put("đà nẵng", "Đà Nẵng");
        locationKeywords.put("dn", "Đà Nẵng");

        locationKeywords.put("hai phong", "Hải Phòng");
        locationKeywords.put("hải phòng", "Hải Phòng");
        locationKeywords.put("hp", "Hải Phòng");

        locationKeywords.put("can tho", "Cần Thơ");
        locationKeywords.put("cần thơ", "Cần Thơ");

        locationKeywords.put("hue", "Thừa Thiên Huế");
        locationKeywords.put("thua thien hue", "Thừa Thiên Huế");
        locationKeywords.put("thừa thiên huế", "Thừa Thiên Huế");

        locationKeywords.put("long an", "Long An");
        locationKeywords.put("binh duong", "Bình Dương");
        locationKeywords.put("bd", "Bình Dương");
        locationKeywords.put("dn", "Đồng Nai");
        locationKeywords.put("dong nai", "Đồng Nai");
        locationKeywords.put("bac ninh", "Bắc Ninh");
        locationKeywords.put("vt", "Vũng Tàu");
        locationKeywords.put("vung tau", "Vũng Tàu");

        // Tìm keyword trong query
        for (java.util.Map.Entry<String, String> entry : locationKeywords.entrySet()) {
            if (normalizedQuery.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Trích xuất industry từ câu hỏi (hỗ trợ có dấu/không dấu)
     */
    private String extractIndustry(String userQuery) {
        if (userQuery == null || userQuery.isEmpty()) {
            return null;
        }

        String normalizedQuery = normalizeText(userQuery);

        // Danh sách các ngành nghề phổ biến
        java.util.Map<String, String> industryKeywords = new java.util.HashMap<>();

        // IT / Technology
        industryKeywords.put("it", "Công nghệ thông tin");
        industryKeywords.put("cong nghe thong tin", "Công nghệ thông tin");
        industryKeywords.put("software", "Công nghệ thông tin");
        industryKeywords.put("lap trinh", "Công nghệ thông tin");
        industryKeywords.put("lập trình", "Công nghệ thông tin");
        industryKeywords.put("developer", "Công nghệ thông tin");
        industryKeywords.put("backend", "Công nghệ thông tin");
        industryKeywords.put("frontend", "Công nghệ thông tin");

        // Marketing
        industryKeywords.put("marketing", "Marketing");
        industryKeywords.put("digital marketing", "Digital Marketing");
        industryKeywords.put("seo", "Marketing");

        // Sales / Business
        industryKeywords.put("sales", "Sales");
        industryKeywords.put("kinh doanh", "Sales");
        industryKeywords.put("ban hang", "Sales");
        industryKeywords.put("business", "Kinh doanh");

        // Finance / Accounting
        industryKeywords.put("tai chinh", "Tài chính");
        industryKeywords.put("ke toan", "Kế toán");
        industryKeywords.put("accounting", "Kế toán");
        industryKeywords.put("finance", "Tài chính");

        // HR
        industryKeywords.put("nhan su", "Nhân sự");
        industryKeywords.put("hr", "Nhân sự");

        // Design
        industryKeywords.put("thiet ke", "Thiết kế");
        industryKeywords.put("design", "Thiết kế");
        industryKeywords.put("designer", "Thiết kế");

        // Tìm keyword trong query
        for (java.util.Map.Entry<String, String> entry : industryKeywords.entrySet()) {
            if (normalizedQuery.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Trích xuất tên công việc từ câu hỏi (hỗ trợ có dấu/không dấu)
     */
    private String extractJobTitle(String userQuery) {
        if (userQuery == null || userQuery.isEmpty()) {
            return null;
        }

        String normalizedQuery = normalizeText(userQuery);

        // Danh sách các tên công việc phổ biến
        java.util.Map<String, String> jobTitleKeywords = new java.util.HashMap<>();

        // Developer roles
        jobTitleKeywords.put("lap trinh vien", "Lập trình viên");
        jobTitleKeywords.put("developer", "Lập trình viên");
        jobTitleKeywords.put("dev", "Lập trình viên");
        jobTitleKeywords.put("backend", "Backend Developer");
        jobTitleKeywords.put("frontend", "Frontend Developer");
        jobTitleKeywords.put("fullstack", "Fullstack Developer");
        jobTitleKeywords.put("full stack", "Fullstack Developer");
        jobTitleKeywords.put("java", "Java Developer");
        jobTitleKeywords.put("python", "Python Developer");
        jobTitleKeywords.put("net", ".NET Developer");

        // Other IT roles
        jobTitleKeywords.put("ba", "Business Analyst");
        jobTitleKeywords.put("qa", "QA Tester");
        jobTitleKeywords.put("qc", "QC Tester");
        jobTitleKeywords.put("pm", "Project Manager");
        jobTitleKeywords.put("scrum master", "Scrum Master");
        jobTitleKeywords.put("tester", "Tester");

        // Business roles
        jobTitleKeywords.put("nhan vien kinh doanh", "Nhân viên kinh doanh");
        jobTitleKeywords.put("sale", "Nhân viên kinh doanh");
        jobTitleKeywords.put("sales", "Nhân viên kinh doanh");

        // Accounting/Finance
        jobTitleKeywords.put("ke toan vien", "Kế toán viên");
        jobTitleKeywords.put("ke toan", "Kế toán");
        jobTitleKeywords.put("accountant", "Kế toán");
        jobTitleKeywords.put("kiem toan", "Kiểm toán");

        // HR
        jobTitleKeywords.put("nhan su", "Nhân sự");
        jobTitleKeywords.put("hr", "Nhân sự");
        jobTitleKeywords.put("recruiter", "Nhân sự");

        // Marketing
        jobTitleKeywords.put("marketing", "Marketing");
        jobTitleKeywords.put("digital marketing", "Digital Marketing");
        jobTitleKeywords.put("seo", "SEO Specialist");

        // Design
        jobTitleKeywords.put("thiet ke", "Thiết kế");
        jobTitleKeywords.put("designer", "Thiết kế");
        jobTitleKeywords.put("ui ux", "UI/UX Designer");
        jobTitleKeywords.put("graphic", "Graphic Designer");

        // Other roles
        jobTitleKeywords.put("quan ly", "Quản lý");
        jobTitleKeywords.put("manager", "Quản lý");
        jobTitleKeywords.put("tro ly", "Trợ lý");
        jobTitleKeywords.put("assistant", "Trợ lý");
        jobTitleKeywords.put("chuyen vien", "Chuyên viên");
        jobTitleKeywords.put("ky su", "Kỹ sư");
        jobTitleKeywords.put("engineer", "Kỹ sư");

        // Tìm keyword trong query
        for (java.util.Map.Entry<String, String> entry : jobTitleKeywords.entrySet()) {
            if (normalizedQuery.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Trích xuất khoảng lương từ câu hỏi
     * Trả về mảng [minSalary, maxSalary] hoặc null
     */
    private long[] extractSalaryRange(String userQuery) {
        if (userQuery == null || userQuery.isEmpty()) {
            return null;
        }

        try {
            // Pattern để tìm khoảng lương: "7-12", "7 den 12", "7 to 12"
            java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile(
                "(\\d+(?:\\.\\d+)?)(?:\\s*(?:[-–à]|den|to|đến)\\s*)?(\\d+(?:\\.\\d+)?)?" +
                "\\s*(trieu|tr|million|m)?"
            );
            java.util.regex.Matcher matcher = rangePattern.matcher(userQuery);

            if (matcher.find()) {
                String minStr = matcher.group(1);
                String maxStr = matcher.group(2);
                String unit = matcher.group(3);

                double minSalary = Double.parseDouble(minStr);
                double maxSalary = maxStr != null ? Double.parseDouble(maxStr) : minSalary;

                // Nếu chỉ có 1 số, coi đó là min hoặc max tùy context
                if (maxStr == null) {
                    // Check context để xem đây là min hay max
                    if (userQuery.contains("tro len") || userQuery.contains("tu") ||
                        userQuery.contains("tren") || userQuery.contains("min")) {
                        minSalary = Double.parseDouble(minStr);
                        maxSalary = Double.MAX_VALUE;
                    } else if (userQuery.contains("toi da") || userQuery.contains("duoi") ||
                               userQuery.contains("max")) {
                        minSalary = 0;
                        maxSalary = Double.parseDouble(minStr);
                    } else {
                        // "10 trieu" → khoảng 8-12 (±20%)
                        minSalary = Double.parseDouble(minStr) * 0.8;
                        maxSalary = Double.parseDouble(minStr) * 1.2;
                    }
                }

                // Đảm bảo min <= max
                if (minSalary > maxSalary) {
                    double temp = minSalary;
                    minSalary = maxSalary;
                    maxSalary = temp;
                }

                // Chuyển đổi về VND
                if (unit != null) {
                    unit = unit.toLowerCase();
                    if (unit.contains("trieu") || unit.contains("tr")) {
                        minSalary = minSalary * 1_000_000;
                        maxSalary = maxSalary * 1_000_000;
                    } else if (unit.contains("million") || unit.contains("m")) {
                        minSalary = minSalary * 1_000_000;
                        maxSalary = maxSalary * 1_000_000;
                    }
                } else {
                    // Mặc định là triệu nếu không có đơn vị
                    minSalary = minSalary * 1_000_000;
                    maxSalary = maxSalary * 1_000_000;
                }

                return new long[]{(long) minSalary, (long) maxSalary};
            }

        } catch (Exception e) {
            System.err.println("Error extracting salary: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lọc jobs theo location (hỗ trợ có dấu/không dấu)
     */
    private List<Job> filterJobsByLocation(List<Job> jobs, String location) {
        if (location == null || location.isEmpty()) {
            return jobs;
        }

        String normalizedLocation = normalizeText(location);

        return jobs.stream()
            .filter(job -> {
                String jobLocation = job.getLocation();
                if (jobLocation == null) {
                    return false;
                }

                String normalizedJobLocation = normalizeText(jobLocation);

                // Check exact match hoặc contains
                return normalizedJobLocation.contains(normalizedLocation) ||
                       normalizedLocation.contains(normalizedJobLocation);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lọc jobs theo industry (hỗ trợ có dấu/không dấu)
     */
    private List<Job> filterJobsByIndustry(List<Job> jobs, String industry) {
        if (industry == null || industry.isEmpty()) {
            return jobs;
        }

        String normalizedIndustry = normalizeText(industry);

        return jobs.stream()
            .filter(job -> {
                // Check trong field industry
                String jobIndustry = job.getIndustry();
                if (jobIndustry != null) {
                    String normalizedJobIndustry = normalizeText(jobIndustry);
                    if (normalizedJobIndustry.contains(normalizedIndustry) ||
                        normalizedIndustry.contains(normalizedJobIndustry)) {
                        return true;
                    }
                }

                // Check trong title (vì đôi khi ngành nghề nằm trong title)
                String jobTitle = job.getTitle();
                if (jobTitle != null) {
                    String normalizedJobTitle = normalizeText(jobTitle);
                    if (normalizedJobTitle.contains(normalizedIndustry) ||
                        normalizedIndustry.contains(normalizedJobTitle)) {
                        return true;
                    }
                }

                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lọc jobs theo title (hỗ trợ có dấu/không dấu)
     */
    private List<Job> filterJobsByTitle(List<Job> jobs, String jobTitle) {
        if (jobTitle == null || jobTitle.isEmpty()) {
            return jobs;
        }

        String normalizedTitle = normalizeText(jobTitle);

        return jobs.stream()
            .filter(job -> {
                String jobTitleField = job.getTitle();
                if (jobTitleField == null) {
                    return false;
                }

                String normalizedJobTitle = normalizeText(jobTitleField);

                // Check exact match hoặc contains
                return normalizedJobTitle.contains(normalizedTitle) ||
                       normalizedTitle.contains(normalizedJobTitle);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lọc jobs theo khoảng lương
     */
    private List<Job> filterJobsBySalary(List<Job> jobs, long salaryMin, long salaryMax) {
        return jobs.stream()
            .filter(job -> {
                // Nếu job không hiển thị lương, skip
                if (job.getShowSalary() == null || !job.getShowSalary()) {
                    return false;
                }

                java.math.BigDecimal jobMin = job.getSalaryMin();
                java.math.BigDecimal jobMax = job.getSalaryMax();

                if (jobMin == null || jobMax == null) {
                    return false;
                }

                // Lọc theo khoảng lương
                long jobSalaryMin = jobMin.longValue();
                long jobSalaryMax = jobMax.longValue();

                // Check khoảng lương giao nhau
                if (salaryMin != Long.MAX_VALUE && salaryMax != Long.MAX_VALUE) {
                    // User yêu cầu khoảng: min đến max
                    boolean jobInRange = jobSalaryMin >= salaryMin && jobSalaryMax <= salaryMax;
                    boolean hasOverlap = jobSalaryMax >= salaryMin && jobSalaryMin <= salaryMax;
                    return jobInRange || hasOverlap;
                } else if (salaryMin != Long.MAX_VALUE) {
                    // Chỉ có min: "10 triệu trở lên"
                    return jobSalaryMax >= salaryMin;
                } else {
                    // Chỉ có max: "tối đa 15 triệu"
                    return jobSalaryMin <= salaryMax;
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
