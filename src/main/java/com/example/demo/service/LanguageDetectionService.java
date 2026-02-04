package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LanguageDetectionService {
    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private String callGeminiApi(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            String urlWithKey = GEMINI_API_URL + "?key=" + API_KEY;

            ResponseEntity<String> response = restTemplate.postForEntity(
                    urlWithKey,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String rawJson = response.getBody();
                JsonNode rootNode = mapper.readTree(rawJson);
                JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode() && textNode.isTextual()) {
                    return textNode.asText().trim();
                } else {
                    return "Error: unexpected result.";
                }

            } else {
                System.err.println("Detection Error: Gemini API returned status " + response.getStatusCode());
                return "Error: Gemini API returned status " + response.getStatusCode();
            }

        } catch (Exception e) {
            System.err.println("Detection Error: Error calling Gemini API: " + e.getMessage());
            return "Error calling Gemini API: " + e.getMessage();
        }
    }

    public String detectLanguage(String code) {
        String systemInstruction = "You are a language detection expert. Your sole task is to analyze the provided code and identify the programming language it is written in. "
                + "The response MUST be ONLY the language name (e.g., 'Java', 'C++', 'Python'). Do NOT include any other text, explanation, or punctuation.";

        String userContent = "What programming language is this code snippet written in:\n\n" + code;

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userContent)))
                ),
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction)))
        );

        String result = callGeminiApi(payload);

        if (result.startsWith("Error")) {
            return result;
        }
        return result.replaceAll("[^a-zA-Z\\+\\s]", "").trim();
    }
}
