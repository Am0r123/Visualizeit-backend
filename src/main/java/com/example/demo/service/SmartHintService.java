package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

@Service
public class SmartHintService {
    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String getHint(String userCode, String levelTopic) {
        
        // 1. Prepare the AI Persona (System Instruction)
        String systemInstruction = "You are a helpful coding tutor. Your task is to analyze the student's code and find the error. "
                + "Provide ONE short, constructive hint (maximum 15 words). "
                + "DO NOT write the corrected code. "
                + "DO NOT say 'Here is a hint'. Just give the hint directly.";
        
        // 2. Prepare the Student's Input
        String userContent = "The student is learning " + levelTopic + ". Here is their code:\n\n" + userCode;

        // 3. Build the JSON Payload (Gemini 1.5 structure)
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
            // 🟢 URL Logic matched to your working file
            String urlWithKey = GEMINI_API_URL + "?key=" + API_KEY;
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                    urlWithKey,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String rawJson = response.getBody();
                JsonNode rootNode = mapper.readTree(rawJson);

                // Extract the text from Gemini response structure
                JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode() && textNode.isTextual()) {
                    return textNode.asText().trim();
                } else {
                    return "Error: unexpected result from AI.";
                }

            } else {
                return "Error: Gemini API returned status " + response.getStatusCode();
            }

        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}