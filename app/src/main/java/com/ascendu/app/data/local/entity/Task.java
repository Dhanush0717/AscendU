package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Discrete one-time task with time range, status, importance marker, and snooze support.
 */
@Entity(
        tableName = "tasks",
        indices = {
                @Index(value = {"dateString"}),
                @Index(value = {"status"}),
                @Index(value = {"isImportant"})
        }
)
public class Task {

    public static final String STATUS_UPCOMING = "UPCOMING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_MISSED = "MISSED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;
    private String category;            // e.g. Technical, Academics, Deep Work, Personal
    private String dateString;          // "YYYY-MM-DD"
    private String startTime;           // "HH:mm" e.g. "14:00"
    private String endTime;             // "HH:mm" e.g. "15:30"
    private boolean isImportant;        // High priority / important marker
    private String status;              // UPCOMING, COMPLETED, MISSED, CANCELLED
    private boolean isCompleted;        // Compatibility flag
    private int priority;               // 1 = Low, 2 = Medium, 3 = High
    private long plannedDate;           // Timestamp for date compatibility
    private long completedAt;           // Timestamp when marked completed
    private long snoozedUntil;          // Timestamp for 30-min snooze
    private long createdAt;

    public Task(String title, String description, String category, String dateString,
                String startTime, String endTime, boolean isImportant, String status,
                boolean isCompleted, int priority, long plannedDate, long completedAt,
                long snoozedUntil, long createdAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dateString = dateString;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isImportant = isImportant;
        this.status = status;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.plannedDate = plannedDate;
        this.completedAt = completedAt;
        this.snoozedUntil = snoozedUntil;
        this.createdAt = createdAt;
    }

    @Ignore
    public Task(String title, String description, String category, int priority, long plannedDate,
                boolean isCompleted, int actualMinutesSpent, long linkedSkillId, long createdAt, long completedAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.plannedDate = plannedDate;
        this.isCompleted = isCompleted;
        this.isImportant = (priority == 3);
        this.status = isCompleted ? STATUS_COMPLETED : STATUS_UPCOMING;
        this.startTime = "";
        this.endTime = "";
        this.snoozedUntil = 0;
        this.createdAt = createdAt;
        this.completedAt = completedAt;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        this.dateString = sdf.format(new java.util.Date(plannedDate > 0 ? plannedDate : System.currentTimeMillis()));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDateString() {
        return dateString;
    }

    public String getScheduledDate() {
        return dateString;
    }

    public void setScheduledDate(String dateString) {
        this.dateString = dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return isCompleted || STATUS_COMPLETED.equals(status);
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        this.status = completed ? STATUS_COMPLETED : STATUS_UPCOMING;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(long plannedDate) {
        this.plannedDate = plannedDate;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public long getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(long snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    public String getDeadlineTime() {
        return endTime != null && !endTime.isEmpty() ? endTime : startTime;
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
