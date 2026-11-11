package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a technical or professional capability being developed.
 */
@Entity(tableName = "skills")
public class Skill {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String category; // Programming, CS Fundamentals, Development, Communication
    private int currentLevel; // 1 to 5
    private int targetLevel; // 1 to 5
    private int totalMinutesPracticed;
    private long lastPracticedTimestamp;

    public Skill(String name, String category, int currentLevel, int targetLevel,
                 int totalMinutesPracticed, long lastPracticedTimestamp) {
        this.name = name;
        this.category = category;
        this.currentLevel = currentLevel;
        this.targetLevel = targetLevel;
        this.totalMinutesPracticed = totalMinutesPracticed;
        this.lastPracticedTimestamp = lastPracticedTimestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTotalMinutesPracticed() {
        return totalMinutesPracticed;
    }

    public void setTotalMinutesPracticed(int totalMinutesPracticed) {
        this.totalMinutesPracticed = totalMinutesPracticed;
    }

    public long getLastPracticedTimestamp() {
        return lastPracticedTimestamp;
    }

    public void setLastPracticedTimestamp(long lastPracticedTimestamp) {
        this.lastPracticedTimestamp = lastPracticedTimestamp;
    }
}
