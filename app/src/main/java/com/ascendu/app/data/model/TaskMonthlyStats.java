package com.ascendu.app.data.model;

/**
 * Task-level monthly performance statistics.
 */
public class TaskMonthlyStats {
    private final long taskId;
    private final String taskTitle;
    private final int completedCount;
    private final int missedCount;
    private final int eligibleCount;
    private final int consistencyPct;
    private final String bestDayOfWeek;

    public TaskMonthlyStats(long taskId, String taskTitle, int completedCount, int missedCount, int eligibleCount, int consistencyPct, String bestDayOfWeek) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.completedCount = completedCount;
        this.missedCount = missedCount;
        this.eligibleCount = eligibleCount;
        this.consistencyPct = consistencyPct;
        this.bestDayOfWeek = bestDayOfWeek != null && !bestDayOfWeek.isEmpty() ? bestDayOfWeek : "None";
    }

    public long getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getMissedCount() {
        return missedCount;
    }

    public int getEligibleCount() {
        return eligibleCount;
    }

    public int getConsistencyPct() {
        return consistencyPct;
    }

    public String getBestDayOfWeek() {
        return bestDayOfWeek;
    }
}
