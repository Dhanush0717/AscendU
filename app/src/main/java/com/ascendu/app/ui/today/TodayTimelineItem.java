package com.ascendu.app.ui.today;

import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;

/**
 * Unified Timeline Item Model for TodayFragment.
 * Can represent either a recurring occurrence or a discrete one-time task.
 */
public class TodayTimelineItem {

    public static final int TYPE_RECURRING = 1;
    public static final int TYPE_ONE_TIME = 2;

    private final int itemType;
    private final long id;
    private final String title;
    private final String category;
    private final String startTime;
    private final String endTime;
    private final boolean isImportant;
    private final String status;          // "UPCOMING", "COMPLETED", "MISSED", "CANCELLED"
    private final boolean isCompleted;
    private final RecurringTaskOccurrence occurrence;
    private final Task oneTimeTask;

    public TodayTimelineItem(RecurringTaskOccurrence occurrence, String currentTime24) {
        this.itemType = TYPE_RECURRING;
        this.id = occurrence.getId();
        this.title = occurrence.getTaskTitleSnapshot();
        this.category = occurrence.getCategorySnapshot();
        this.startTime = occurrence.getScheduledStartTime();
        this.endTime = occurrence.getScheduledEndTime();
        this.isImportant = occurrence.isImportant();
        this.occurrence = occurrence;
        this.oneTimeTask = null;

        boolean completed = Task.STATUS_COMPLETED.equalsIgnoreCase(occurrence.getStatus());
        this.isCompleted = completed;

        if (completed) {
            this.status = Task.STATUS_COMPLETED;
        } else if (Task.STATUS_CANCELLED.equalsIgnoreCase(occurrence.getStatus())) {
            this.status = Task.STATUS_CANCELLED;
        } else {
            // Check if deadline (endTime) has passed today
            if (endTime != null && !endTime.isEmpty() && currentTime24 != null && currentTime24.compareTo(endTime) > 0) {
                this.status = Task.STATUS_MISSED;
            } else {
                this.status = Task.STATUS_UPCOMING;
            }
        }
    }

    public TodayTimelineItem(Task task, String currentTime24) {
        this.itemType = TYPE_ONE_TIME;
        this.id = task.getId();
        this.title = task.getTitle();
        this.category = task.getCategory();
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
        this.isImportant = task.isImportant();
        this.occurrence = null;
        this.oneTimeTask = task;

        boolean completed = task.isCompleted() || Task.STATUS_COMPLETED.equalsIgnoreCase(task.getStatus());
        this.isCompleted = completed;

        if (completed) {
            this.status = Task.STATUS_COMPLETED;
        } else if (Task.STATUS_CANCELLED.equalsIgnoreCase(task.getStatus())) {
            this.status = Task.STATUS_CANCELLED;
        } else {
            if (endTime != null && !endTime.isEmpty() && currentTime24 != null && currentTime24.compareTo(endTime) > 0) {
                this.status = Task.STATUS_MISSED;
            } else {
                this.status = Task.STATUS_UPCOMING;
            }
        }
    }

    public int getItemType() {
        return itemType;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public String getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isMissed() {
        return Task.STATUS_MISSED.equalsIgnoreCase(status);
    }

    public boolean isUpcoming() {
        return Task.STATUS_UPCOMING.equalsIgnoreCase(status);
    }

    public RecurringTaskOccurrence getOccurrence() {
        return occurrence;
    }

    public Task getOneTimeTask() {
        return oneTimeTask;
    }
}
