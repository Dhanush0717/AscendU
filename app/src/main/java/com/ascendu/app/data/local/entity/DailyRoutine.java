package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Universal Daily Routine / Non-Negotiable commitment that recurs every single day.
 * Allows users to customize what they commit to doing daily.
 */
@Entity(tableName = "daily_routines")
public class DailyRoutine {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String category; // Focus, Mastery, Discipline, Health, Reflection
    private int targetMinutes;
    private boolean isActive;
    private int currentStreak;
    private int bestStreak;
    private long createdAt;

    public DailyRoutine(String title, String category, int targetMinutes, boolean isActive,
                        int currentStreak, int bestStreak, long createdAt) {
        this.title = title;
        this.category = category;
        this.targetMinutes = targetMinutes;
        this.isActive = isActive;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(int targetMinutes) {
        this.targetMinutes = targetMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
