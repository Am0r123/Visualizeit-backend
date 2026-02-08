package com.example.demo.controllers;

import com.example.demo.model.Level;
import com.example.demo.model.Topic;
import com.example.demo.model.UserAnswer;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.repository.UserAnswerRepository;
import com.example.demo.service.AnswerCheckerService; // ✅ Import the Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class LevelsController {

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository; 

    @Autowired
    private AnswerCheckerService answerCheckerService; // ✅ Inject the AI Service

    @GetMapping("/topics")
    public ResponseEntity<?> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        return ResponseEntity.ok(topics); 
    }

    @GetMapping("/levels/{topicSlug}")
    public ResponseEntity<?> getLevelsByTopic(@PathVariable String topicSlug) {
        List<Level> levels = levelRepository.findByTopicSlugOrderByLevelNumberAsc(topicSlug);
        return ResponseEntity.ok(levels);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Extract Data
            Long userId = ((Number) payload.get("userId")).longValue();
            Long levelId = ((Number) payload.get("levelId")).longValue();
            String userCode = (String) payload.get("code");

            // 2. Validate that the level exists
            Optional<Level> levelOpt = levelRepository.findById(levelId);
            if (levelOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Level not found"));
            }

            Level level = levelOpt.get();
            String question = level.getQuestion(); // Assuming your Level model has 'question'

            // 3. 🧠 AI CHECK: Ask Gemini if the code solves the question
            boolean isCorrect = answerCheckerService.checkAnswer(question, userCode);

            // 4. Save the Attempt (Optional: You can add an 'isCorrect' field to UserAnswer later)
            UserAnswer answer = new UserAnswer();
            answer.setUserId(userId);
            answer.setLevel(level);
            answer.setUserCode(userCode);
            userAnswerRepository.save(answer);

            // 5. Return Result to Frontend
            if (isCorrect) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Correct! The AI verified your logic.",
                    "nextLevelId", levelId + 1 // Simple logic to hint next level
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "❌ Incorrect. The AI thinks your code does not solve the problem."
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Error processing request: " + e.getMessage()));
        }
    }
}