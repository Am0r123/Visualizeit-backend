package com.example.demo.controllers;

import com.example.demo.service.GeminiCodeTranslatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/translate")
@CrossOrigin(origins = "*") // Allows your Angular app to call this
public class TranslateController {

    @Autowired
    private GeminiCodeTranslatorService translatorService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, String> payload) {
        // 1. Extract Data
        String code = payload.get("code");
        String from = payload.getOrDefault("from", "detect");
        String to = payload.getOrDefault("to", "Python");

        // 2. Validate Input
        if (code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>(
                Map.of("status", "error", "message", "Source code cannot be empty."), 
                HttpStatus.BAD_REQUEST
            );
        }

        // 3. Call Service
        String translatedCode = translatorService.translateCode(from, to, code);

        // 4. Handle Service Errors
        if (translatedCode.startsWith("Error")) {
            return new ResponseEntity<>(
                Map.of("status", "error", "message", translatedCode), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // 5. Return Success Response
        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "source_language", from,
                "target_language", to,
                "translated_code", translatedCode
            ),
            HttpStatus.OK
        );
    }
}