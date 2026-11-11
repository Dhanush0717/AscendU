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

import java.util.Calendar;

/**
 * Handles smart local notification channels, scheduling, and accountability alerts.
 * Notifies the user when they are falling behind on daily non-negotiables.
 */
public class AccountabilityHelper {

    public static final String CHANNEL_ID_ACCOUNTABILITY = "ascendu_accountability_channel";
    public static final String CHANNEL_ID_DAILY = "ascendu_daily_reminder_channel";
    public static final int NOTIFICATION_ID_ACCOUNTABILITY = 1001;

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Accountability / Fall-out Channel
            NotificationChannel accountabilityChannel = new NotificationChannel(
                    CHANNEL_ID_ACCOUNTABILITY,
                    "Accountability & Streak Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            accountabilityChannel.setDescription("Alerts you when daily commitments are in danger of falling behind");
            accountabilityChannel.enableVibration(true);
            manager.createNotificationChannel(accountabilityChannel);

            // General Daily Blueprint Channel
            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_ID_DAILY,
                    "Daily Blueprint & Kickoff",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyChannel.setDescription("Morning kickoff and daily planning nudges");
            manager.createNotificationChannel(dailyChannel);
        }
    }

    public static void scheduleDailyEveningCheck(Context context, int hourOfDay, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AccountabilityReceiver.class);
        intent.setAction(AccountabilityReceiver.ACTION_CHECK_ACCOUNTABILITY);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 101, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time already passed today, set for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (SecurityException ignored) {
            // In Android 12+, exact alarm permission might be revoked; fallback to inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void sendAccountabilityNotification(Context context, String title, String message) {
        createNotificationChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ACCOUNTABILITY)
                .setSmallIcon(R.drawable.ic_flame)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ACCOUNTABILITY, builder.build());
        } catch (SecurityException ignored) {
            // Notification permission might be denied
        }
    }
}
