package com.example.demo.controllers;

import com.example.demo.model.HistoryEntity; // 🟢 FIX: Imports from 'entities'
import com.example.demo.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    @Autowired
    private HistoryService service;

    @GetMapping("/{userId}")
    public List<HistoryEntity> getHistory(@PathVariable String userId) {
        return service.getUserHistory(userId);
    }

    @PostMapping("/save")
    public HistoryEntity saveCode(@RequestBody Map<String, String> payload) {
        return service.saveHistory(
            payload.get("userId"),
            payload.get("code"),
            payload.get("type"),  // 'LEVEL' or 'CUSTOM'
            payload.get("title"),
            payload.get("lang")
        );
    }
}