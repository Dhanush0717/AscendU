package com.ascendu.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.repository.RoutineRepository;
import com.ascendu.app.data.repository.TaskRepository;
import com.ascendu.app.util.AppExecutors;

/**
 * Triggered by AlarmManager to check if user has unfinished daily commitments.
 * Alerts user if they are falling behind before midnight.
 */
public class AccountabilityReceiver extends BroadcastReceiver {

    public static final String ACTION_CHECK_ACCOUNTABILITY = "com.ascendu.app.ACTION_CHECK_ACCOUNTABILITY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Reschedule evening accountability check upon device reboot
            AccountabilityHelper.scheduleDailyEveningCheck(context, 20, 0); // 8:00 PM default
            return;
        }

        if (ACTION_CHECK_ACCOUNTABILITY.equals(action)) {
            // Check pending commitments in background
            AppExecutors.getInstance().diskIO().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);

                // 1. Check daily routines
                String todayStr = RoutineRepository.getTodayDateString();
                int totalRoutines = db.dailyRoutineDao().getActiveRoutineCountDirect();
                int completedRoutines = db.dailyRoutineDao().getCompletedCountForDateDirect(todayStr);
                int pendingRoutines = Math.max(0, totalRoutines - completedRoutines);

                // 2. Check today's tasks
                long[] todayBounds = TaskRepository.getDayBounds(System.currentTimeMillis());
                int plannedTasks = db.taskDao().getPlannedTaskCountForDayDirect(todayBounds[0], todayBounds[1]);
                int completedTasks = db.taskDao().getCompletedTaskCountForDayDirect(todayBounds[0], todayBounds[1]);
                int pendingTasks = Math.max(0, plannedTasks - completedTasks);

                int totalPending = pendingRoutines + pendingTasks;

                if (totalPending > 0) {
                    String title = "⚠️ Discipline Alert: Don't Break Your Streak!";
                    String message = "You have " + totalPending + " pending commitment(s) today. " +
                            "A focused 15-minute push will protect your daily consistency!";
                    AccountabilityHelper.sendAccountabilityNotification(context, title, message);
                }

                // Reschedule for next evening
                AccountabilityHelper.scheduleDailyEveningCheck(context, 20, 0);
            });
        }
    }
}
