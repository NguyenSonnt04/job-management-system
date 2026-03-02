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
    public String callGeminiWithText(String prompt) {
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
            ? "CV Content (text extracted):\\n" + cvText
            : "The CV file is provided as an attachment above. Read and analyze it.";

        return """
            You are an expert HR consultant and CV evaluator. Analyze the following CV and return ONLY a JSON object with no markdown.

            %s

            Score this CV on EXACTLY these criteria (use the exact names as keys):
            %s

            Return JSON in this exact format:
            {
              "totalScore": <sum of all scores>,
              "maxTotalScore": <sum of all maxScores>,
              "overallFeedback": "<2-3 sentence overall summary>",
              "criteria": {
                "<criteria_name>": {
                  "score": <number>,
                  "maxScore": <number>,
                  "feedback": "<1-2 sentence specific feedback>"
                }
              },
              "strengths": ["<strength 1>", "<strength 2>", "<strength 3>"],
              "weaknesses": ["<weakness 1>", "<weakness 2>", "<weakness 3>"]
            }

            Be strict, analytical, and constructive. Use Vietnamese for all text fields.
            """.formatted(cvSection, criteriaBlock.toString());
    }
}
