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
public class AnswerCheckerService {

    @Value("${gemini.api.key}")
    private String API_KEY;

    // Make sure this is the generic generateContent URL (not streaming)
    // Example: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean checkAnswer(String question, String userCode) {
        // 1. Construct the Prompt
        String systemInstruction = "You are a strict computer science teacher. " +
                "Your job is to check if a student's code correctly solves a specific problem. " +
                "Ignore minor syntax errors (like missing semicolons) if the logic is correct. " +
                "Reply with EXACTLY one word: 'YES' if it works, or 'NO' if it fails.";

        String userPrompt = "Problem: " + question + "\n\n" +
                            "Student Code:\n" + userCode + "\n\n" +
                            "Does this code solve the problem?";

        // 2. Build JSON Payload
        Map<String, Object> payload = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", userPrompt)))
            ),
            "system_instruction", Map.of(
                "parts", List.of(Map.of("text", systemInstruction))
            )
        );

        // 3. Set Headers
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

            // 4. Parse Response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = mapper.readTree(response.getBody());
                // Navigate to: candidates[0] -> content -> parts[0] -> text
                JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode()) {
                    String aiResponse = textNode.asText().trim().toUpperCase();
                    // Check if AI said YES
                    return aiResponse.contains("YES");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gemini API Error: " + e.getMessage());
        }

        return false; // Fail safe if API crashes
    }
}