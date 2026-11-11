package com.ascendu.app.data.model;

/**
 * Encapsulates monthly consistency statistics for My Arc.
 */
public class MonthlyConsistencyStats {
    private final int completedCount;
    private final int missedCount;
    private final int eligibleCount;
    private final int consistencyPct;
    private final String bestDayOfWeek;

    public MonthlyConsistencyStats(int completedCount, int missedCount, int eligibleCount, int consistencyPct, String bestDayOfWeek) {
        this.completedCount = completedCount;
        this.missedCount = missedCount;
        this.eligibleCount = eligibleCount;
        this.consistencyPct = consistencyPct;
        this.bestDayOfWeek = bestDayOfWeek != null && !bestDayOfWeek.isEmpty() ? bestDayOfWeek : "None";
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
