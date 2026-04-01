package Nhom08.Project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Calls Google Gemini API to score CV content and match jobs.
 */
@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper   = new ObjectMapper();

    // ── Score CV from text ────────────────────────────────────────────────────
    public String scoreCvText(String cvText, List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
        requireApiKey();
        String prompt = buildScoringPrompt(cvText, criteriaList);
        String body = buildTextRequest(prompt);
        return callGemini(body);
    }

    /**
     * Call Gemini with a plain text prompt (no scoring wrapper).
     * Use this for industry extraction, job matching, etc.
     */
    /** Existing method kept for backward compatibility - returns JSON-forced response. */
    public String callGeminiWithText(String prompt) {
        requireApiKey();
        return callGemini(buildTextRequest(prompt));
    }

    /** Call Gemini expecting a plain conversational text reply (not forced JSON). */
    public String callGeminiPlainText(String prompt) {
        requireApiKey();
        return callGemini(buildPlainTextRequest(prompt));
    }

    /** Call Gemini expecting a JSON object response. */
    public String callGeminiJson(String prompt) {
        requireApiKey();
        return callGemini(buildTextRequest(prompt));
    }

    // ── Score CV from file bytes ──────────────────────────────────────────────
    public String scoreCvFile(byte[] fileBytes, String mimeType,
                               List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
        requireApiKey();
        String prompt = buildScoringPrompt(null, criteriaList);
        String body   = buildFileRequest(fileBytes, mimeType, prompt);
        return callGemini(body);
    }


    public String scoreCvWithPrompt(byte[] fileBytes, String mimeType, String prompt) {

        requireApiKey();
        String body = buildFileRequest(fileBytes, mimeType, prompt);
        return callGemini(body);
    }

    // ── Match CV file against job list ────────────────────────────────────────
    /**
     * Sends the CV file and a list of jobs to Gemini.
     * Returns JSON array: [{"jobId":X,"matchScore":85,"reason":"..."}]
     */
    public String matchJobsWithCv(byte[] fileBytes, String mimeType, List<Map<String, Object>> jobs) {
        requireApiKey();

        StringBuilder jobsBlock = new StringBuilder();
        for (Map<String, Object> job : jobs) {
            jobsBlock.append("---\n")
                .append("Job ID: ").append(job.get("id")).append("\n")
                .append("Title: ").append(job.get("title")).append("\n")
                .append("Industry: ").append(job.get("industry")).append("\n")
                .append("Experience Required: ").append(job.get("experience")).append("\n")
                .append("Description: ").append(job.get("description")).append("\n")
                .append("Requirements: ").append(job.get("requirements")).append("\n\n");
        }

        String prompt =
            "You are an expert job-matching AI. Analyze the CV in the attached file and match it against the following job listings.\n\n" +
            "For each job, score the match from 0-100 based on:\n" +
            "- Skill alignment (40%)\n" +
            "- Experience level match (30%)\n" +
            "- Industry/domain fit (20%)\n" +
            "- Education & other factors (10%)\n\n" +
            "JOBS TO MATCH:\n" + jobsBlock + "\n" +
            "Return ONLY a JSON array, no markdown:\n" +
            "[{\"jobId\": <number>, \"matchScore\": <0-100>, \"reason\": \"<1 sentence in Vietnamese explaining the match>\"}]\n" +
            "Sort by matchScore descending. Only include jobs with matchScore >= 40.";

        String body = buildFileRequest(fileBytes, mimeType, prompt);
        return callGemini(body);
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key ch\u01b0a \u0111\u01b0\u1ee3c c\u1ea5u h\u00ecnh trong application.properties (gemini.api.key)");
        }
    }

    private String buildTextRequest(String prompt) {
        try {
            return "{\"contents\":[{\"parts\":[{\"text\":" + mapper.valueToTree(prompt) + "}]}]," +
                   "\"generationConfig\":{\"responseMimeType\":\"application/json\"}}";
        } catch (Exception e) {
            throw new RuntimeException("Build request failed", e);
        }
    }

    private String buildPlainTextRequest(String prompt) {
        try {
            return "{\"contents\":[{\"parts\":[{\"text\":" + mapper.valueToTree(prompt) + "}]}]}";
        } catch (Exception e) {
            throw new RuntimeException("Build plain request failed", e);
        }
    }

    private String buildFileRequest(byte[] fileBytes, String mimeType, String prompt) {
        try {
            String b64 = Base64.getEncoder().encodeToString(fileBytes);
            return "{\"contents\":[{\"parts\":[" +
                   "{\"inline_data\":{\"mime_type\":\"" + mimeType + "\",\"data\":\"" + b64 + "\"}}," +
                   "{\"text\":" + mapper.valueToTree(prompt) + "}]}]," +
                   "\"generationConfig\":{\"responseMimeType\":\"application/json\"}}";
        } catch (Exception e) {
            throw new RuntimeException("Build request failed", e);
        }
    }

    private String callGemini(String requestBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API error: HTTP " + response.statusCode() + " - " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            return text.asText();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("L\u1ed7i g\u1ecdi Gemini API: " + e.getMessage(), e);
        }
    }

    private String buildScoringPrompt(String cvText,
                                       List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
        StringBuilder criteriaBlock = new StringBuilder();
        for (Nhom08.Project.entity.CvScoringCriteria c : criteriaList) {
            criteriaBlock.append("- ").append(c.getName())
                .append(" (max ").append(c.getMaxScore()).append(" pts): ")
                .append(c.getDescription() != null ? c.getDescription() : "").append("\\n");
        }

        String cvSection = cvText != null
            ? "NỘI dung CV sau (đã trích xuất):\n" + cvText
            : "File CV được đính kèm bên dưới. Đây là file CV CÓ SẴN - PHÂN TÍCH NỘI DUNG, KHÔNG tạo mới.";

        return """
            Bạn là chuyên gia đánh giá CV. Nhiệm vụ: PHÂN TÍCH file CV đã có, KHÔNG tạo CV mới.

            %s

            Đánh giá CV trên các tiêu chí (dùng đúng tên làm key):
            %s

            Return JSON theo định dạng:
            {
              "totalScore": <tổng điểm>,
              "maxTotalScore": <tổng điểm tối đa>,
              "overallFeedback": "<tổng quan 2-3 câu>",
              "criteria": {
                "<tên_tiêu chí>": {
                  "score": <số>,
                  "maxScore": <số>,
                  "feedback": "<nhận xét 1-2 câu>"
                }
              },
              "strengths": ["<điểm mạnh 1>", "<điểm mạnh 2>", "<điểm mạnh 3>"],
              "weaknesses": ["<điểm yếu 1>", "<điểm yếu 2>", "<điểm yếu 3>"]
            }

            LƯU Ý QUAN TRỌNG:
            - Chỉ TRÍCH XUẤT, KHÔNG TẠO MỚI
            - KHÔNG đưa ra link website, KHÔNG hướng dẫn tạo CV
            - Dùng tiếng Việt cho tất cả text fields
            """.formatted(cvSection, criteriaBlock.toString());
    }
}
