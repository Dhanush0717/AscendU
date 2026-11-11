package com.ascendu.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;

import java.util.List;

/**
 * Handles incoming alarm triggers, notification action callbacks (Mark as Done, Snooze),
 * daily evening review broadcasts, and boot completion rescheduling.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            NotificationHelper.rescheduleAllAlarms(context);
            return;
        }

        if (NotificationHelper.ACTION_TASK_REMINDER.equals(action)) {
            handleTaskReminder(context, intent);
            return;
        }

        if (NotificationHelper.ACTION_TASK_MARK_DONE.equals(action)) {
            handleMarkDone(context, intent);
            return;
        }

        if (NotificationHelper.ACTION_TASK_SNOOZE.equals(action)) {
            handleSnooze(context, intent);
            return;
        }

        if (NotificationHelper.ACTION_EVENING_REVIEW.equals(action)) {
            handleEveningReview(context);
        }
    }

    private void handleTaskReminder(Context context, Intent intent) {
        String taskType = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TYPE);
        long taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE);
        String scheduledDate = intent.getStringExtra(NotificationHelper.EXTRA_DATE_STRING);
        int notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1000);

        if (taskId == -1 || scheduledDate == null) return;

        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            boolean shouldDisplay = false;
            String displayTitle = taskTitle;

            if (NotificationHelper.TYPE_RECURRING.equals(taskType)) {
                RecurringTaskOccurrence occ = db.recurringTaskOccurrenceDao().getByTaskAndDateDirect(taskId, scheduledDate);
                if (occ != null) {
                    displayTitle = occ.getTaskTitleSnapshot();
                    // Show ONLY if not already completed and not cancelled
                    if (!Task.STATUS_COMPLETED.equals(occ.getStatus()) && !Task.STATUS_CANCELLED.equals(occ.getStatus())) {
                        shouldDisplay = true;
                    }
                }
            } else if (NotificationHelper.TYPE_ONETIME.equals(taskType)) {
                Task task = db.taskDao().getByIdDirect(taskId);
                if (task != null) {
                    displayTitle = task.getTitle();
                    if (!task.isCompleted() && !task.isCancelled()) {
                        shouldDisplay = true;
                    }
                }
            }

            if (shouldDisplay) {
                final String finalTitle = displayTitle;
                AppExecutors.getInstance().mainThread().execute(() -> {
                    NotificationHelper.displayTaskNotification(context, taskType, taskId, finalTitle, scheduledDate, notificationId);
                });
            }
        });
    }

    private void handleMarkDone(Context context, Intent intent) {
        String taskType = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TYPE);
        long taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1);
        String scheduledDate = intent.getStringExtra(NotificationHelper.EXTRA_DATE_STRING);
        int notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1000);

        // Dismiss notification immediately
        NotificationManagerCompat.from(context).cancel(notificationId);

        if (taskId == -1 || scheduledDate == null) return;

        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            long now = System.currentTimeMillis();

            if (NotificationHelper.TYPE_RECURRING.equals(taskType)) {
                db.recurringTaskOccurrenceDao().updateStatusByTaskAndDate(taskId, scheduledDate, Task.STATUS_COMPLETED, now);
            } else if (NotificationHelper.TYPE_ONETIME.equals(taskType)) {
                db.taskDao().updateCompletionStatus(taskId, true, Task.STATUS_COMPLETED, now);
            }
        });
    }

    private void handleSnooze(Context context, Intent intent) {
        String taskType = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TYPE);
        long taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE);
        String scheduledDate = intent.getStringExtra(NotificationHelper.EXTRA_DATE_STRING);
        int notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1000);

        // Dismiss existing notification
        NotificationManagerCompat.from(context).cancel(notificationId);

        // Schedule new reminder 30 minutes later
        NotificationHelper.scheduleSnooze(context, taskType, taskId, taskTitle, scheduledDate, notificationId);
    }

    private void handleEveningReview(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            String todayStr = DateUtils.getTodayDateString();
            String currentTime = DateUtils.getCurrentTime24H();

            List<RecurringTaskOccurrence> occurrences = db.recurringTaskOccurrenceDao().getOccurrencesForDateDirect(todayStr);
            int completed = 0;
            int missed = 0;
            int pending = 0;

            for (RecurringTaskOccurrence occ : occurrences) {
                if (Task.STATUS_CANCELLED.equals(occ.getStatus())) continue;

                if (Task.STATUS_COMPLETED.equals(occ.getStatus())) {
                    completed++;
                } else if (DateUtils.isTimePast(occ.getScheduledEndTime(), currentTime)) {
                    missed++;
                } else {
                    pending++;
                }
            }

            int total = completed + missed + pending;
            int pct = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

            String title = "🌙 Daily Evening Discipline Review";
            String body = "Summary: " + completed + " Done • " + pending + " Pending • " + missed + " Missed (" + pct + "% Discipline). Tap to review your arc!";

            AppExecutors.getInstance().mainThread().execute(() -> {
                NotificationHelper.displayEveningReviewNotification(context, title, body);
                // Reschedule for tomorrow evening
                NotificationHelper.scheduleDailyEveningReview(context);
            });
        });
    }
}
