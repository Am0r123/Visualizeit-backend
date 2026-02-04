package com.example.demo.controllers;

import com.example.demo.service.SmartHintService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class HintController {

    private Map<String, Integer> userHints = new HashMap<>();

    @Autowired
    private SmartHintService hintService; // 🟢 Use the new Service here

    public HintController() { userHints.put("user_123", 3); }

    @PostMapping("/get-smart-hint")
    public Map<String, Object> getSmartHint(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String code = payload.get("code");
        String topic = payload.get("topic");
        
        Map<String, Object> response = new HashMap<>();

        if (!userHints.containsKey(userId)) userHints.put(userId, 3);
        int hintsLeft = userHints.get(userId);

        if (hintsLeft > 0) {
            hintsLeft--; 
            userHints.put(userId, hintsLeft);
            
            // 🟢 Call the new method
            String aiHint = hintService.getHint(code, topic);

            response.put("status", "success");
            response.put("hint", aiHint);
            response.put("hints_left", hintsLeft);
        } else {
            response.put("status", "failed");
            response.put("message", "No hints remaining!");
            response.put("hints_left", 0);
        }
        return response;
    }
}