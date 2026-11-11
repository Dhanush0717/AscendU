package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Historical snapshot instance of a RecurringTask on a specific calendar date.
 * Does NOT cascade-delete when a RecurringTask is removed.
 * Preserves historical title, category, and scheduled timings for My Arc consistency grid.
 */
@Entity(
        tableName = "recurring_task_occurrences",
        indices = {
                @Index(value = {"recurringTaskId", "dateString"}, unique = true),
                @Index(value = {"dateString"}),
                @Index(value = {"status"})
        }
)
public class RecurringTaskOccurrence {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long recurringTaskId;          // Reference to template (or 0 if parent was deleted)
    private String taskTitleSnapshot;      // Historical snapshot of title
    private String categorySnapshot;       // Historical snapshot of category
    private String dateString;             // "YYYY-MM-DD"
    private String scheduledStartTime;     // Frozen time range e.g. "07:00"
    private String scheduledEndTime;       // Frozen time range e.g. "08:00"
    private boolean isImportant;           // Importance marker
    private String status;                 // "UPCOMING", "COMPLETED", "MISSED", "CANCELLED"
    private long completedAt;              // Epoch timestamp when completed (0 if incomplete)

    public RecurringTaskOccurrence(long recurringTaskId, String taskTitleSnapshot, String categorySnapshot,
                                   String dateString, String scheduledStartTime, String scheduledEndTime,
                                   boolean isImportant, String status, long completedAt) {
        this.recurringTaskId = recurringTaskId;
        this.taskTitleSnapshot = taskTitleSnapshot;
        this.categorySnapshot = categorySnapshot;
        this.dateString = dateString;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.isImportant = isImportant;
        this.status = status;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecurringTaskId() {
        return recurringTaskId;
    }

    public void setRecurringTaskId(long recurringTaskId) {
        this.recurringTaskId = recurringTaskId;
    }

    public String getTaskTitleSnapshot() {
        return taskTitleSnapshot;
    }

    public void setTaskTitleSnapshot(String taskTitleSnapshot) {
        this.taskTitleSnapshot = taskTitleSnapshot;
    }

    public String getCategorySnapshot() {
        return categorySnapshot;
    }

    public void setCategorySnapshot(String categorySnapshot) {
        this.categorySnapshot = categorySnapshot;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(String scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public String getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(String scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
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

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
