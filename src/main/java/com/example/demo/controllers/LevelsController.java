package com.example.demo.controllers;

import com.example.demo.model.Level;
import com.example.demo.model.Topic;
import com.example.demo.model.UserAnswer;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.repository.UserAnswerRepository;
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
            Long userId = ((Number) payload.get("userId")).longValue();
            Long levelId = ((Number) payload.get("levelId")).longValue();
            String code = (String) payload.get("code");

            // 2. Validate that the level exists
            Optional<Level> levelOpt = levelRepository.findById(levelId);
            if (levelOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Level not found with ID: " + levelId);
            }

            // 3. Create and Save the Answer
            UserAnswer answer = new UserAnswer();
            answer.setUserId(userId);
            answer.setLevel(levelOpt.get());
            answer.setUserCode(code);
            // No isCorrect logic as requested

            userAnswerRepository.save(answer);

            return ResponseEntity.ok("Answer saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error saving answer: " + e.getMessage());
        }
    }
}