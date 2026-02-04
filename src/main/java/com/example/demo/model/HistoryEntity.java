package com.example.demo.entities; // 🟢 FIX: Matches folder name 'entities'

import jakarta.persistence.*; // 🟢 FIX: Changed javax to jakarta for Spring Boot 3+
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
public class HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    
    @Column(columnDefinition = "TEXT")
    private String codeContent;
    
    private String sourceType; // "LEVEL" or "CUSTOM"
    private String title;
    private String lang;
    private LocalDateTime savedAt;

    public HistoryEntity() {}

    public HistoryEntity(String userId, String code, String type, String title, String lang) {
        this.userId = userId;
        this.codeContent = code;
        this.sourceType = type;
        this.title = title;
        this.lang = lang;
        this.savedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getCodeContent() { return codeContent; }
    public String getSourceType() { return sourceType; }
    public String getTitle() { return title; }
    public String getLang() { return lang; }
    public LocalDateTime getSavedAt() { return savedAt; }
}