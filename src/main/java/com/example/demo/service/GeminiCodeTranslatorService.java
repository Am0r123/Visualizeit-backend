package com.example.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

        String systemInstruction = 
            "You are an expert software engineer and code translator. Your goal is to translate " + sourcePhrase + " into " + targetLang + " while ensuring functional equivalence.\n" +
            "Strict Guidelines:\n" +
            "1. **Functional Equivalence**: If a specific library, method, or feature in the source does not exist in " + targetLang + ", you MUST write valid, equivalent logic using " + targetLang + " standard libraries to achieve the same result.\n" +
            "2. **Idiomatic Translation**: Do not translate line-by-line. Use the standard conventions, best practices, and idioms of " + targetLang + ".\n" +
            "3. **Missing Features**: If a feature is impossible to replicate (e.g., pointers in Python), implement the closest possible alternative and add a comment explaining the adaptation.\n" +
            "4. **Output Format**: Return ONLY the raw code. Do NOT use markdown formatting (no ``` or ```" + targetLang + "). Do NOT include explanations or conversational text.";

        String userContent = "Code to translate:\n" + code;

        try {
            // 2. Build Payload (Safe JSON Construction)
            Map<String, Object> payload = Map.of(
                "contents", List.of(
                    Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", systemInstruction + "\n\n" + userContent))
                    )
                ),
                "generationConfig", Map.of(
                    "temperature", 0.2 // Low temperature for accuracy
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonPayload = mapper.writeValueAsString(payload);
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            // 3. Construct URL
            String url = GEMINI_API_URL.contains("?") 
                ? GEMINI_API_URL + "&key=" + API_KEY 
                : GEMINI_API_URL + "?key=" + API_KEY;

            // 4. Send Request
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 5. Parse Response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = mapper.readTree(response.getBody());
                JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode()) {
                    return cleanResponse(textNode.asText());
                }
            }
            return "Error: AI response was empty.";

        } catch (JsonProcessingException e) {
            return "Error building JSON payload: " + e.getMessage();
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }

    /**
     * cleans the response from Markdown code blocks or conversational text.
     */
    private String cleanResponse(String response) {
        if (response == null) return "";
        String result = response.trim();

        // If response contains markdown blocks, extract content inside them
        if (result.contains("```")) {
            int start = result.indexOf("```");
            int end = result.lastIndexOf("```");
            
            if (end > start) {
                // Skip the first ``` (and potentially language name like ```java)
                result = result.substring(start + 3, end);
                if (result.startsWith("java")) result = result.substring(4);
                else if (result.startsWith("python")) result = result.substring(6);
                else if (result.startsWith("cpp")) result = result.substring(3);
                else if (result.startsWith("csharp")) result = result.substring(6);
                
                // Generic cleanup: remove first line if it's just a language name
                if (!result.trim().isEmpty() && !result.trim().contains(" ") && result.trim().length() < 10) {
                     // likely just "php" or "javascript" left
                     result = result.substring(result.indexOf("\n") + 1);
                }
            }
        }
        return result.trim();
    }
}