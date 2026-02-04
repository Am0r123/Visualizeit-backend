package com.example.demo.controllers;

import com.example.demo.service.GeminiCodeTranslatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/translate") // 🟢 This controller owns "/translate"
@CrossOrigin(origins = "*")
public class TranslateController {

    @Autowired
    private GeminiCodeTranslatorService translatorService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String from = payload.getOrDefault("from", "detect");
        String to = payload.getOrDefault("to", "Python");

        if (code == null || code.isBlank()) {
            return new ResponseEntity<>(
                Map.of("status", "error", "message", "Source code cannot be empty."), 
                HttpStatus.BAD_REQUEST
            );
        }

        String translatedCode = translatorService.translateCode(from, to, code);

        if (translatedCode.startsWith("Error")) {
            return new ResponseEntity<>(
                Map.of("status", "error", "message", translatedCode), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

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