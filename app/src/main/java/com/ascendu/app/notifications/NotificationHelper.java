package com.ascendu.app.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ascendu.app.MainActivity;
import com.ascendu.app.R;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Robust Central Notification Scheduler and Manager for AscendU.
 * Handles task alarms, 30-min snooze, direct Mark-as-Done broadcast actions,
 * daily evening review summary, and reboot recovery.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID_IMPORTANT_TASKS = "ascendu_important_tasks_channel";
    public static final String CHANNEL_ID_EVENING_REVIEW = "ascendu_evening_review_channel";

    public static final String ACTION_TASK_REMINDER = "com.ascendu.app.ACTION_TASK_REMINDER";
    public static final String ACTION_TASK_SNOOZE = "com.ascendu.app.ACTION_TASK_SNOOZE";
    public static final String ACTION_TASK_MARK_DONE = "com.ascendu.app.ACTION_TASK_MARK_DONE";
    public static final String ACTION_EVENING_REVIEW = "com.ascendu.app.ACTION_EVENING_REVIEW";

    public static final String EXTRA_TASK_TYPE = "extra_task_type"; // "RECURRING" or "ONETIME"
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_DATE_STRING = "extra_date_string";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    public static final String TYPE_RECURRING = "RECURRING";
    public static final String TYPE_ONETIME = "ONETIME";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Channel 1: Important Task Reminders
            NotificationChannel taskChannel = new NotificationChannel(
                    CHANNEL_ID_IMPORTANT_TASKS,
                    "Important Task Deadlines & Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            taskChannel.setDescription("High priority alerts for important recurring and one-time task deadlines");
            taskChannel.enableVibration(true);
            manager.createNotificationChannel(taskChannel);

            // Channel 2: Evening Review
            NotificationChannel eveningChannel = new NotificationChannel(
                    CHANNEL_ID_EVENING_REVIEW,
                    "Evening Discipline Review",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            eveningChannel.setDescription("Daily summary of completed, pending, and missed commitments");
            manager.createNotificationChannel(eveningChannel);
        }
    }

    public static void scheduleTaskReminder(Context context, String taskType, long taskId, String taskTitle, String scheduledDate, String scheduledTime24) {
        NotificationPreferences prefs = new NotificationPreferences(context);
        if (!prefs.isImportantTasksEnabled()) return;

        if (scheduledDate == null || scheduledTime24 == null || scheduledTime24.trim().isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date deadlineDate = sdf.parse(scheduledDate + " " + scheduledTime24);
            if (deadlineDate == null) return;

            long triggerTime = deadlineDate.getTime();
            if (triggerTime <= System.currentTimeMillis()) return; // Already passed

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            int requestCode = generateRequestCode(taskType, taskId, scheduledDate);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(ACTION_TASK_REMINDER);
            intent.putExtra(EXTRA_TASK_TYPE, taskType);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            intent.putExtra(EXTRA_TASK_TITLE, taskTitle);
            intent.putExtra(EXTRA_DATE_STRING, scheduledDate);
            intent.putExtra(EXTRA_NOTIFICATION_ID, requestCode);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
            scheduleAlarmCompat(alarmManager, triggerTime, pendingIntent);

        } catch (Exception ignored) {
        }
    }

    public static void cancelTaskReminder(Context context, String taskType, long taskId, String scheduledDate) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int requestCode = generateRequestCode(taskType, taskId, scheduledDate);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_TASK_REMINDER);

        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void scheduleSnooze(Context context, String taskType, long taskId, String taskTitle, String scheduledDate, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long snoozeTriggerTime = System.currentTimeMillis() + (30 * 60 * 1000L); // 30 minutes later

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_TASK_REMINDER);
        intent.putExtra(EXTRA_TASK_TYPE, taskType);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(EXTRA_DATE_STRING, scheduledDate);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, flags);
        scheduleAlarmCompat(alarmManager, snoozeTriggerTime, pendingIntent);
    }

    public static void scheduleDailyEveningReview(Context context) {
        NotificationPreferences prefs = new NotificationPreferences(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_EVENING_REVIEW);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9001, intent, flags);

        if (!prefs.isEveningReviewEnabled()) {
            alarmManager.cancel(pendingIntent);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, prefs.getEveningHour());
        cal.set(Calendar.MINUTE, prefs.getEveningMinute());
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        scheduleAlarmCompat(alarmManager, cal.getTimeInMillis(), pendingIntent);
    }

    public static void rescheduleAllAlarms(Context context) {
        scheduleDailyEveningReview(context);

        NotificationPreferences prefs = new NotificationPreferences(context);
        if (!prefs.isImportantTasksEnabled()) return;

        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            String todayStr = DateUtils.getTodayDateString();

            // 1. Reschedule today's important recurring occurrences
            List<RecurringTaskOccurrence> todayOccurrences = db.recurringTaskOccurrenceDao().getOccurrencesForDateDirect(todayStr);
            for (RecurringTaskOccurrence occ : todayOccurrences) {
                if (occ.isImportant() && !Task.STATUS_COMPLETED.equals(occ.getStatus()) && !Task.STATUS_CANCELLED.equals(occ.getStatus())) {
                    scheduleTaskReminder(context, TYPE_RECURRING, occ.getRecurringTaskId(), occ.getTaskTitleSnapshot(), occ.getDateString(), occ.getScheduledEndTime());
                }
            }

            // 2. Reschedule future important one-time tasks
            List<Task> upcomingTasks = db.taskDao().getUpcomingTasksDirect(todayStr);
            for (Task task : upcomingTasks) {
                if (task.isImportant() && !task.isCompleted() && !task.isCancelled()) {
                    scheduleTaskReminder(context, TYPE_ONETIME, task.getId(), task.getTitle(), task.getScheduledDate(), task.getDeadlineTime());
                }
            }
        });
    }

    public static void displayTaskNotification(Context context, String taskType, long taskId, String taskTitle, String scheduledDate, int notificationId) {
        createNotificationChannels(context);

        // Content Intent: Open AscendU MainActivity
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int immFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pContentIntent = PendingIntent.getActivity(context, notificationId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | immFlags);

        // Action 1: Mark as Done
        Intent doneIntent = new Intent(context, NotificationReceiver.class);
        doneIntent.setAction(ACTION_TASK_MARK_DONE);
        doneIntent.putExtra(EXTRA_TASK_TYPE, taskType);
        doneIntent.putExtra(EXTRA_TASK_ID, taskId);
        doneIntent.putExtra(EXTRA_DATE_STRING, scheduledDate);
        doneIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent pDoneIntent = PendingIntent.getBroadcast(context, notificationId + 1, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | immFlags);

        // Action 2: Snooze 30 Min
        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
        snoozeIntent.setAction(ACTION_TASK_SNOOZE);
        snoozeIntent.putExtra(EXTRA_TASK_TYPE, taskType);
        snoozeIntent.putExtra(EXTRA_TASK_ID, taskId);
        snoozeIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        snoozeIntent.putExtra(EXTRA_DATE_STRING, scheduledDate);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent pSnoozeIntent = PendingIntent.getBroadcast(context, notificationId + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | immFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_IMPORTANT_TASKS)
                .setSmallIcon(R.drawable.ic_flame)
                .setContentTitle("⏰ Important Commitment Due")
                .setContentText(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("'" + taskTitle + "' is due now. Protect your discipline streak!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pContentIntent)
                .addAction(R.drawable.ic_nav_today, "Mark Done", pDoneIntent)
                .addAction(R.drawable.ic_timer, "Snooze 30m", pSnoozeIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException ignored) {
        }
    }

    public static void displayEveningReviewNotification(Context context, String title, String body) {
        createNotificationChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int immFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pIntent = PendingIntent.getActivity(context, 9002, intent, PendingIntent.FLAG_UPDATE_CURRENT | immFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_EVENING_REVIEW)
                .setSmallIcon(R.drawable.ic_flame)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        try {
            NotificationManagerCompat.from(context).notify(9002, builder.build());
        } catch (SecurityException ignored) {
        }
    }

    private static void scheduleAlarmCompat(AlarmManager alarmManager, long triggerTime, PendingIntent pendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (SecurityException ignored) {
            // Android 12+ fallback when SCHEDULE_EXACT_ALARM is not granted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static int generateRequestCode(String taskType, long taskId, String dateStr) {
        int hash = (taskType + "_" + taskId + "_" + dateStr).hashCode();
        return Math.abs(hash % 100000) + 1000;
    }
}
