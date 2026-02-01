package com.example.demo.model; 

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug;
    private String name;
    

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Level> levels; 
}