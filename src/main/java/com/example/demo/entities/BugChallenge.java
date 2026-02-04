package com.example.demo.entities;

public class BugChallenge {
    private String id;
    private String title;
    private String description;
    private String brokenCode;
    private String correctCode;
    private int bugsCount;
    private String difficulty;

    public BugChallenge() {}

    public BugChallenge(String id, String title, String desc, String broken, String correct, int count, String diff) {
        this.id = id;
        this.title = title;
        this.description = desc;
        this.brokenCode = broken;
        this.correctCode = correct;
        this.bugsCount = count;
        this.difficulty = diff;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBrokenCode() { return brokenCode; }
    public String getCorrectCode() { return correctCode; }
    public int getBugsCount() { return bugsCount; }
    public String getDifficulty() { return difficulty; }
}