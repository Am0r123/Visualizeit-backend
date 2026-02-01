package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "levels")
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_number")
    private Integer levelNumber;

    private String title;
    private String question;
    
    // ✅ ADDED: Explicit mapping to 'code_snippet'
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;
    
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnore 
    private Topic topic;
}