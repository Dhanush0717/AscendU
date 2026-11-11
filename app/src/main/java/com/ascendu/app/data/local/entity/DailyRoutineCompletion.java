package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Records completion of a DailyRoutine for a specific date (yyyy-MM-dd).
 */
@Entity(
        tableName = "routine_completions",
        indices = {@Index(value = {"routineId", "dateString"}, unique = true)}
)
public class DailyRoutineCompletion {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long routineId;
    private String dateString; // e.g. "2026-11-04"
    private boolean isCompleted;
    private long completedAt;

    public DailyRoutineCompletion(long routineId, String dateString, boolean isCompleted, long completedAt) {
        this.routineId = routineId;
        this.dateString = dateString;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoutineId() {
        return routineId;
    }

    public void setRoutineId(long routineId) {
        this.routineId = routineId;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
