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
import java.util.List;
import java.util.Map;

/**
 * Calls Anthropic Claude API for mock interview conversations.
 */
@Service
public class ClaudeService {

    @Value("${claude.api.key:}")
    private String apiKey;

    private static final String CLAUDE_URL     = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_VERSION = "2023-06-01";
    private static final String CLAUDE_MODEL   = "claude-opus-4-6";

    private final HttpClient   httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper     = new ObjectMapper();

    /**
     * Send a multi-turn conversation to Claude and get the next reply.
     *
     * @param systemPrompt  The interviewer persona / instructions
     * @param messages      List of {"role":"user"|"assistant", "content":"..."}
     * @param maxTokens     Max tokens in response
     * @return Claude's text reply
     */
    public String chat(String systemPrompt, List<Map<String, String>> messages, int maxTokens) {
        requireApiKey();

        try {
            String body = mapper.writeValueAsString(Map.of(
                "model",      CLAUDE_MODEL,
                "max_tokens", maxTokens,
                "system",     systemPrompt,
                "messages",   messages
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_URL))
                .header("Content-Type",    "application/json")
                .header("x-api-key",       apiKey)
                .header("anthropic-version", CLAUDE_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                    "Claude API error: HTTP " + response.statusCode() + " — " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("content").get(0).path("text").asText();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Claude request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Claude API: " + e.getMessage(), e);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Claude API key chưa được cấu hình. " +
                "Thêm claude.api.key=YOUR_KEY vào application-local.properties");
        }
    }
}
