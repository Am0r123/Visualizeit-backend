package com.example.demo.controllers;

import com.example.demo.entities.BugChallenge;
import com.example.demo.service.DetectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/detective") // 🟢 FIX: Ensure this is "/detective", not "/translate"
@CrossOrigin(origins = "*")
public class DetectiveController {

    @Autowired
    private DetectiveService detectiveService;

    // 1. Generate a new case
    @PostMapping("/generate")
    public ResponseEntity<BugChallenge> generateCase(@RequestBody Map<String, String> payload) {
        String topic = payload.getOrDefault("topic", "Random");
        String difficulty = payload.getOrDefault("difficulty", "Medium");
        String language = payload.getOrDefault("language", "Java");

        BugChallenge challenge = detectiveService.generateChallenge(topic, difficulty, language);
        return ResponseEntity.ok(challenge);
    }

    // 2. Submit a solution
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitSolution(@RequestBody Map<String, String> payload) {
        String id = payload.get("id");
        String userCode = payload.get("code");

        boolean isCorrect = detectiveService.checkSolution(id, userCode);

        if (isCorrect) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "🎉 Correct! Bug fixed."));
        } else {
            return ResponseEntity.ok(Map.of("status", "fail", "message", "❌ Incorrect. Try again!"));
        }
    }

    // 3. Get all active cases
    @GetMapping("/challenges")
    public ResponseEntity<List<BugChallenge>> getAllChallenges() {
        return ResponseEntity.ok(detectiveService.getAllChallenges());
    }
}