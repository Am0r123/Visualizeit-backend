package com.example.demo.controllers;

import com.example.demo.service.CodeRequest;
import com.example.demo.service.ComplexityModelService;
import com.example.demo.service.AlgorithmDetectionService;
import com.example.demo.service.AlgorithmDetectionService.DetectionResult; // Import the Record

import ai.djl.modality.Classifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class AIController {

    @Autowired
    private ComplexityModelService complexityService;

    @Autowired
    private AlgorithmDetectionService algorithmService;

    @PostMapping("/analyze-complexity")
    public ResponseEntity<Map<String, Object>> analyzeComplexity(@RequestBody CodeRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Classifications result = complexityService.predict(request.getCode());
            Classifications.Classification best = result.best();
            
            response.put("complexity", best.getClassName()); // e.g., "O(n^2)"
            response.put("confidence", best.getProbability() * 100);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Complexity Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect-algorithm")
    public ResponseEntity<Map<String, Object>> detectAlgorithm(@RequestBody CodeRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call the Service
            DetectionResult result = algorithmService.detectAlgorithm(request.getCode());
            
            response.put("algorithm", result.algorithmName());
            response.put("confidence", result.confidence() * 100);
            
            // Friendly Message Logic
            if (result.algorithmName().contains("Unknown")) {
                 response.put("message", "We couldn't identify a standard algorithm here.");
                 response.put("success", false); // Optional: mark as false if unknown
            } else {
                 // Clean up name: "bubblesort" -> "Bubble Sort"
                 String prettyName = result.algorithmName().substring(0, 1).toUpperCase() + result.algorithmName().substring(1);
                 response.put("message", "This looks like " + prettyName + "!");
                 response.put("success", true);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Algorithm Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}