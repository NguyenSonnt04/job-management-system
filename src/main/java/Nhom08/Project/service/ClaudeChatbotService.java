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

/**
 * Service for Chatbot using Anthropic Claude AI
 * Sử dụng Java 11+ HttpClient để gọi API trực tiếp
 * Chỉ dùng cho chatbot, không dùng cho CV parsing/scoring
 */
@Service
public class ClaudeChatbotService {

    @Value("${claude.api.key:}")
    private String apiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-3-5-sonnet-20241022";
    private static final String VERSION = "2023-06-01";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ClaudeChatbotService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Gọi Claude API với text prompt và nhận phản hồi text
     * Dùng cho chatbot
     */
    public String chatWithClaude(String prompt) {
        requireApiKey();

        try {
            // Build request body
            String requestBody = buildRequestBody(prompt);

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check response status
            if (response.statusCode() != 200) {
                throw new RuntimeException("Claude API error: HTTP " + response.statusCode() + " - " + response.body());
            }

            // Parse response
            return parseResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Claude request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Build request body for Claude API
     */
    private String buildRequestBody(String prompt) {
        try {
            JsonNode requestBody = mapper.createObjectNode()
                .put("model", MODEL)
                .put("max_tokens", 4096)
                .set("messages", mapper.createArrayNode()
                    .add(mapper.createObjectNode()
                        .put("role", "user")
                        .put("content", prompt)));

            return mapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    /**
     * Parse Claude API response and extract text content
     */
    private String parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);

            // Navigate to content array and extract text
            JsonNode contentArray = root.path("content");
            for (JsonNode contentBlock : contentArray) {
                if ("text".equals(contentBlock.path("type").asText())) {
                    return contentBlock.path("text").asText();
                }
            }

            return "";

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Claude API key chưa được cấu hình trong application.properties (claude.api.key)");
        }
    }
}
