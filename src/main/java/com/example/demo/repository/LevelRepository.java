package com.example.demo.repository;

import com.example.demo.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    // Magic query to find levels by topic name
    List<Level> findByTopicSlugOrderByLevelNumberAsc(String slug);
}