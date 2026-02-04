package com.example.demo.repository; // 🟢 FIX: Matches folder name 'repositories'

import com.example.demo.model.HistoryEntity; // 🟢 FIX: Imports from 'entities'
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
    
    List<HistoryEntity> findByUserIdOrderBySavedAtDesc(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HistoryEntity h WHERE h.savedAt < :cutoffDate")
    void deleteOlderThan(LocalDateTime cutoffDate);
}