package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Stores user profile, discipline targets, and notification settings.
 */
@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey
    private int id = 1; // Single user profile for local offline architecture

    private String name;
    private String growthDomain;
    private int dailyTargetCount;
    private String eveningSummaryTime; // "HH:mm" e.g. "21:00"
    private boolean notificationsEnabled;
    private boolean darkModeEnabled;
    private long createdAt;

    // Compatibility fields
    private String degree;
    private int dailyStudyTargetMinutes;
    private int dailyTaskLimit;
    private String preferredReminderTime;

    public UserProfile(String name, String growthDomain, int dailyTargetCount, String eveningSummaryTime,
                       boolean notificationsEnabled, boolean darkModeEnabled, long createdAt,
                       String degree, int dailyStudyTargetMinutes, int dailyTaskLimit, String preferredReminderTime) {
        this.name = name;
        this.growthDomain = growthDomain;
        this.dailyTargetCount = dailyTargetCount;
        this.eveningSummaryTime = eveningSummaryTime;
        this.notificationsEnabled = notificationsEnabled;
        this.darkModeEnabled = darkModeEnabled;
        this.createdAt = createdAt;
        this.degree = degree;
        this.dailyStudyTargetMinutes = dailyStudyTargetMinutes;
        this.dailyTaskLimit = dailyTaskLimit;
        this.preferredReminderTime = preferredReminderTime;
    }

    @Ignore
    public UserProfile(String name, String degree, int dailyStudyTargetMinutes, int dailyTaskLimit, String preferredReminderTime, long createdAt) {
        this.name = name;
        this.degree = degree;
        this.growthDomain = degree;
        this.dailyStudyTargetMinutes = dailyStudyTargetMinutes;
        this.dailyTaskLimit = dailyTaskLimit;
        this.dailyTargetCount = dailyTaskLimit;
        this.preferredReminderTime = preferredReminderTime;
        this.eveningSummaryTime = preferredReminderTime != null ? preferredReminderTime : "21:00";
        this.notificationsEnabled = true;
        this.darkModeEnabled = false;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrowthDomain() {
        return growthDomain != null && !growthDomain.isEmpty() ? growthDomain : (degree != null ? degree : "General Mastery");
    }

    public void setGrowthDomain(String growthDomain) {
        this.growthDomain = growthDomain;
        this.degree = growthDomain;
    }

    public int getDailyTargetCount() {
        return dailyTargetCount > 0 ? dailyTargetCount : dailyTaskLimit;
    }

    public void setDailyTargetCount(int dailyTargetCount) {
        this.dailyTargetCount = dailyTargetCount;
    }

    public String getEveningSummaryTime() {
        return eveningSummaryTime != null ? eveningSummaryTime : "21:00";
    }

    public void setEveningSummaryTime(String eveningSummaryTime) {
        this.eveningSummaryTime = eveningSummaryTime;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public int getDailyStudyTargetMinutes() {
        return dailyStudyTargetMinutes;
    }

    public void setDailyStudyTargetMinutes(int dailyStudyTargetMinutes) {
        this.dailyStudyTargetMinutes = dailyStudyTargetMinutes;
    }

    public int getDailyTaskLimit() {
        return dailyTaskLimit;
    }

    public void setDailyTaskLimit(int dailyTaskLimit) {
        this.dailyTaskLimit = dailyTaskLimit;
    }

    public String getPreferredReminderTime() {
        return preferredReminderTime;
    }

    public void setPreferredReminderTime(String preferredReminderTime) {
        this.preferredReminderTime = preferredReminderTime;
    }
}
