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
public class GeminiCodeTranslatorService {

    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String translateCode(String sourceLang, String targetLang, String code) {
        
        // 1. Smart Prompting
        String sourcePhrase = (sourceLang.equalsIgnoreCase("detect")) 
            ? "the provided code" 
            : "the provided " + sourceLang + " code";

        String systemInstruction = "You are an expert code translator. Translate " + sourcePhrase + " into " + targetLang + ".\n" +
                "Rules:\n" +
                "1. Return ONLY the translated code.\n" +
                "2. Do NOT wrap code in markdown blocks (no ```).\n" +
                "3. Do NOT add conversational text.\n" +
                "4. Preserve original logic and comments.";

        String userContent = "Code:\n" + code;

        // 2. Build Payload
        Map<String, Object> payload = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", userContent)))
            ),
            "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction)))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            String url = GEMINI_API_URL.contains("?") ? GEMINI_API_URL + "&key=" + API_KEY : GEMINI_API_URL + "?key=" + API_KEY;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = mapper.readTree(response.getBody());
                JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode()) {
                    String result = textNode.asText().trim();
                    // Clean up markdown just in case
                    return result.replaceAll("```[a-zA-Z]*", "").replace("```", "").trim();
                }
            }
            return "Error: AI response was empty.";

        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}