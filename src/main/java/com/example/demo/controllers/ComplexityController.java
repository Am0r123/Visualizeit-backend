package com.example.demo.controllers;

import com.example.demo.service.CodeRequest;
import com.example.demo.service.ComplexityModelService;
import ai.djl.modality.Classifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200") // Allow Angular to access this
public class ComplexityController {

    @Autowired
    private ComplexityModelService complexityService;

    @PostMapping("/analyze-complexity")
    public ResponseEntity<Map<String, Object>> analyzeCode(@RequestBody CodeRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Run Prediction
            Classifications result = complexityService.predict(request.getCode());
            
            // Extract the best guess
            Classifications.Classification best = result.best();
            
            response.put("complexity", best.getClassName()); // e.g., "O(n^2)"
            response.put("confidence", best.getProbability() * 100); // e.g., 98.5
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }
}