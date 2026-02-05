package com.example.demo.controllers;

import com.example.demo.service.ExecutionService;
import com.example.demo.service.LanguageDetectionService;
import com.example.demo.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController {

    @Autowired
    private LanguageDetectionService languageDetectionService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/detect")
    public ResponseEntity<?> detect(@RequestBody Map<String, String> payload) {
        String code = payload.getOrDefault("code", "");
        String lang = languageDetectionService.detectLanguage(code);
        return ResponseEntity.ok(Map.of("language", lang));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> payload) {
        String code = payload.getOrDefault("code", "");
        String language = payload.getOrDefault("language", "");
        Map<String, Object> result = verificationService.verifyCode(language, code);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/execute")
    public ResponseEntity<?> execute(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.getOrDefault("code", "");
        Map<String, Object> execResult = executionService.executePythonAndGetSteps(code, payload);

        return ResponseEntity.ok(execResult);
    }
}