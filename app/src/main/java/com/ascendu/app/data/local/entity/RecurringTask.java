package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Universal Recurring Task / Habit template.
 * Configured with weekday scheduling, time ranges, and active status.
 */
@Entity(
        tableName = "recurring_tasks",
        indices = {
                @Index(value = {"isActive"}),
                @Index(value = {"isImportant"})
        }
)
public class RecurringTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String category;             // Focus, Discipline, Mastery, Health, Reflection
    private String startTime;            // "07:00" or empty
    private String endTime;              // "08:00" or empty
    private int daysOfWeekBitmask;       // Bitmask: Bit 1=Mon(1) ... Bit 7=Sun(64). e.g., 127 = all 7 days
    private boolean isImportant;
    private boolean isActive;            // 1 = Active, 0 = Archived / Disabled
    private long createdAt;

    public RecurringTask(String title, String category, String startTime, String endTime,
                         int daysOfWeekBitmask, boolean isImportant, boolean isActive, long createdAt) {
        this.title = title;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfWeekBitmask = daysOfWeekBitmask;
        this.isImportant = isImportant;
        this.isActive = isActive;
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

    public int getDaysOfWeekBitmask() {
        return daysOfWeekBitmask;
    }

    public int getDaysOfWeekMask() {
        return daysOfWeekBitmask;
    }

    public void setDaysOfWeekMask(int daysOfWeekBitmask) {
        this.daysOfWeekBitmask = daysOfWeekBitmask;
    }

    public void setDaysOfWeekBitmask(int daysOfWeekBitmask) {
        this.daysOfWeekBitmask = daysOfWeekBitmask;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
