package Nhom08.Project.service;

import Nhom08.Project.entity.ChatHistory;
import Nhom08.Project.entity.ChatbotPrompt;
import Nhom08.Project.repository.ChatHistoryRepository;
import Nhom08.Project.repository.ChatbotPromptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý logic Chatbot
 * - Keyword matching để tìm prompt phù hợp
 * - Lưu lịch sử chat
 */
@Service
public class ChatbotService {

    @Autowired
    private ChatbotPromptRepository promptRepository;

    @Autowired
    private ChatHistoryRepository historyRepository;

    /**
     * Xử lý câu hỏi từ user - tìm keyword match và trả lời
     */
    public Map<String, Object> processQuestion(String question, String sessionId) {
        // 1. Tìm prompt phù hợp bằng keyword matching
        ChatbotPrompt matchedPrompt = findMatchingPrompt(question);

        String response;
        String matchedKeyword = null;
        Integer promptId = null;
        boolean found = false;

        if (matchedPrompt != null) {
            response = matchedPrompt.getResponse();
            matchedKeyword = extractMatchedKeyword(question, matchedPrompt);
            promptId = matchedPrompt.getId();
            found = true;
        } else {
            // Trả lời mặc định nếu không khớp
            response = getDefaultResponse();
        }

        // 2. Lưu lịch sử chat
        ChatHistory chatHistory = new ChatHistory(
            sessionId,
            question,
            response,
            matchedKeyword,
            promptId
        );
        historyRepository.save(chatHistory);

        // 3. Trả về kết quả
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", chatHistory.getId());
        result.put("answer", response);
        result.put("found", found);
        result.put("promptId", promptId);
        result.put("matchedKeyword", matchedKeyword);
        result.put("category", matchedPrompt != null ? matchedPrompt.getCategory() : "general");

        return result;
    }

    /**
     * Tìm prompt phù hợp bằng keyword matching
     * Ưu tiên prompt có priority cao hơn
     */
    private ChatbotPrompt findMatchingPrompt(String question) {
        List<ChatbotPrompt> activePrompts = promptRepository.findAllActiveForMatching();

        if (activePrompts.isEmpty()) {
            return null;
        }

        String normalizedQuestion = normalizeText(question);

        // Tìm prompt khớp, ưu tiên priority cao hơn
        return activePrompts.stream()
            .filter(prompt -> containsAnyKeyword(normalizedQuestion, prompt.getKeywords()))
            .max(Comparator.comparingInt(p -> p.getId())) // ID cao hơn = ưu tiên (hoặc dùng priority field)
            .orElse(null);
    }

    /**
     * Kiểm tra question có chứa bất kỳ keyword nào không
     */
    private boolean containsAnyKeyword(String question, String keywordsStr) {
        if (keywordsStr == null || keywordsStr.isBlank()) {
            return false;
        }

        String[] keywords = keywordsStr.split(",");
        String normalizedQuestion = question.toLowerCase().trim();

        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (normalizedQuestion.contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trích xuất keyword đã khớp (để hiển thị/log)
     */
    private String extractMatchedKeyword(String question, ChatbotPrompt prompt) {
        String[] keywords = prompt.getKeywords().split(",");
        String normalizedQuestion = normalizeText(question);

        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (normalizedQuestion.contains(normalizedKeyword)) {
                return keyword.trim();
            }
        }
        return null;
    }

    /**
     * Chuẩn hóa text: lowercase, bỏ dấu câu, bỏ tiếng Việt
     */
    private String normalizeText(String text) {
        if (text == null) return "";

        return text.toLowerCase()
            .trim()
            .replaceAll("[\\s\\p{Punct}]+", " ")  // Bỏ dấu câu, khoảng trắng thừa
            .replaceAll("àáảãạăằắẳẵặâầấẩẫậ", "a")
            .replaceAll("èéẻẽẹêềếểễệ", "e")
            .replaceAll("ìíỉĩị", "i")
            .replaceAll("òóỏõọôồốổỗộơờớởỡợ", "o")
            .replaceAll("ùúủũụưừứửữự", "u")
            .replaceAll("ỳýỷỹỵ", "y")
            .replaceAll("đ", "d");
    }

    /**
     * Câu trả lời mặc định
     */
    private String getDefaultResponse() {
        return "🤔 Xin lỗi, tôi không tìm thấy câu trả lời phù hợp. \n\n" +
               "**Bạn có thể hỏi về:**\n" +
               "• Tạo/Sửa/Xóa công việc\n" +
               "• Deadline và nhắc lịch\n" +
               "• Thống kê báo cáo\n" +
               "• Tài khoản và bảo mật\n\n" +
               "Hoặc liên hệ admin để được hỗ trợ!";
    }

    /**
     * Lấy danh sách câu hỏi nhanh (từ prompt hoặc mặc định)
     */
    public List<Map<String, Object>> getQuickQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();

        // Lấy từ database hoặc dùng mặc định
        List<ChatbotPrompt> prompts = promptRepository.findByIsActiveTrueOrderByCategoryAscIdDesc();

        if (prompts.isEmpty()) {
            // Dữ liệu mặc định
            questions.add(createQuickQuestion("Hệ thống có những chức năng chính nào?", "🔍"));
            questions.add(createQuickQuestion("Làm thế nào để tạo công việc mới?", "📝"));
            questions.add(createQuickQuestion("Đặt deadline như thế nào?", "⏰"));
            questions.add(createQuickQuestion("Xem thống kê ở đâu?", "📊"));
        } else {
            // Lấy từ database - các prompt quan trọng
            Set<String> added = new HashSet<>();
            for (ChatbotPrompt p : prompts) {
                if (added.size() >= 6) break; // Tối đa 6 câu
                String[] keywords = p.getKeywords().split(",");
                if (keywords.length > 0 && !added.contains(p.getCategory())) {
                    questions.add(createQuickQuestion(
                        "Làm thế nào để " + keywords[0].trim() + "?",
                        getIconForCategory(p.getCategory())
                    ));
                    added.add(p.getCategory());
                }
            }
        }

        return questions;
    }

    /**
     * Lấy danh sách categories
     */
    public List<String> getCategories() {
        return promptRepository.findByIsActiveTrueOrderByCategoryAscIdDesc()
            .stream()
            .map(ChatbotPrompt::getCategory)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử chat của session
     */
    public List<ChatHistory> getSessionHistory(String sessionId) {
        return historyRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }

    // --- Helper methods ---

    private Map<String, Object> createQuickQuestion(String question, String icon) {
        Map<String, Object> q = new HashMap<>();
        q.put("question", question);
        q.put("icon", icon);
        return q;
    }

    private String getIconForCategory(String category) {
        return switch (category.toLowerCase()) {
            case "greeting" -> "👋";
            case "task" -> "📝";
            case "deadline" -> "⏰";
            case "report" -> "📊";
            case "account" -> "👤";
            case "help" -> "❓";
            default -> "💬";
        };
    }
}
