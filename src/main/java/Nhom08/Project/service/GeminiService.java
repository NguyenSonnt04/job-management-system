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

/**
 * Calls Google Gemini API to score CV content.
 */
@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper   = new ObjectMapper();

    /**
     * Score a CV given its raw text and the list of criteria names.
     * Returns a JSON string from Gemini to be parsed by CvScoringService.
     */
    public String scoreCvText(String cvText, java.util.List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key chưa được cấu hình trong application.properties (gemini.api.key)");
        }

        String prompt = buildScoringPrompt(cvText, criteriaList);

        String requestBody = """
            {
              "contents": [{
                "parts": [{"text": %s}]
              }],
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
            """.formatted(mapper.valueToTree(prompt).toString());

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

            // Extract text from Gemini response
            JsonNode root   = mapper.readTree(response.body());
            JsonNode text   = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            return text.asText();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Score a CV from a base64-encoded file (PDF/DOCX).
     */
    public String scoreCvFile(byte[] fileBytes, String mimeType,
                              java.util.List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key chưa được cấu hình trong application.properties (gemini.api.key)");
        }

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);
        String prompt     = buildScoringPrompt(null, criteriaList);

        String requestBody = """
            {
              "contents": [{
                "parts": [
                  {
                    "inline_data": {
                      "mime_type": "%s",
                      "data": "%s"
                    }
                  },
                  {"text": %s}
                ]
              }],
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
            """.formatted(mimeType, base64Data, mapper.valueToTree(prompt).toString());

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
            throw new RuntimeException("Lỗi gọi Gemini API: " + e.getMessage(), e);
        }
    }

    // ---- Build scoring prompt ----
    private String buildScoringPrompt(String cvText,
                                       java.util.List<Nhom08.Project.entity.CvScoringCriteria> criteriaList) {
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
