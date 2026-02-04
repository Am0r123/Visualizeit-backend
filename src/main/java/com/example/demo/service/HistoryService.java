package com.example.demo.service;

import com.example.demo.entities.HistoryEntity;     // 🟢 FIX: Imports from 'entities'
import com.example.demo.repositories.HistoryRepository; // 🟢 FIX: Imports from 'repositories'
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository repository;

    // Save
    public HistoryEntity saveHistory(String userId, String code, String type, String title, String lang) {
        return repository.save(new HistoryEntity(userId, code, type, title, lang));
    }

    // Get
    public List<HistoryEntity> getUserHistory(String userId) {
        return repository.findByUserIdOrderBySavedAtDesc(userId);
    }

    // Auto-Delete: Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?") 
    public void cleanupOldHistory() {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        repository.deleteOlderThan(tenDaysAgo);
        System.out.println("Deleted history older than: " + tenDaysAgo);
    }
}