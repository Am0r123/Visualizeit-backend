
package com.example.demo.controllers;

import com.example.demo.service.GeminiCodeTranslatorService;
import com.example.demo.service.LanguageDetectionService;
import com.example.demo.service.ComplexityAnalysisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/complexity")
@CrossOrigin

public class ComplexityAnalysisController {
    @Autowired
    private ComplexityAnalysisService complexityAnalysisService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> analyzeComplexity(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String from = payload.getOrDefault("from", "unknown");

        if (code == null || code.isBlank()) {
            return new ResponseEntity<>(
                    Map.of("status", "error", "message", "Missing or empty 'code' parameter in request body."),
                    HttpStatus.BAD_REQUEST
            );
        }

        String result = complexityAnalysisService.CalculateComplexity(from, code);

        if (result.startsWith("Error")) {
            return new ResponseEntity<>(
                    Map.of("status", "error", "message", result),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return new ResponseEntity<>(
                Map.of(
                        "status", "success",
                        "complexity", result
                ),
                HttpStatus.OK
        );
    }
}