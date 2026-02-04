package com.example.demo.controllers;

import com.example.demo.entities.HistoryEntity;
import com.example.demo.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/{userId}")
    public Map<String, Object> getDashboardStats(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Static Stats (You can make these dynamic later if needed)
        response.put("challengesCompleted", 42);
        response.put("studentRank", 15);
        response.put("totalScore", 8500);
        response.put("levelsCompleted", 12);

        // 2. GET REAL HISTORY FROM DATABASE/SERVICE
        List<HistoryEntity> allHistory = historyService.getUserHistory(userId);

        // 3. Convert to Dashboard Format (Take top 5 recent items)
        List<Map<String, String>> recentHistory = allHistory.stream()
            .limit(5) // Only show the latest 5
            .map(entity -> {
                Map<String, String> item = new HashMap<>();
                item.put("title", entity.getTitle());
                
                // Format Date: "Feb 2, 2026"
                if (entity.getSavedAt() != null) {
                    item.put("date", entity.getSavedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                } else {
                    item.put("date", "Unknown");
                }

                // Show Language as Status (e.g., "JAVA") or just "Saved"
                // Mapping to "Completed" ensures it gets the Blue Badge color
                item.put("status", "Completed"); 
                
                return item;
            })
            .collect(Collectors.toList());
        
        response.put("recentHistory", recentHistory);
        return response;
    }
}