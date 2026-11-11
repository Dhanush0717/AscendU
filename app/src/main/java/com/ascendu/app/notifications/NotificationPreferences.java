package com.ascendu.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user preferences for AscendU notifications.
 */
public class NotificationPreferences {

    private static final String PREF_NAME = "AscendUNotificationPrefs";
    public static final String KEY_IMPORTANT_TASKS_ENABLED = "important_tasks_enabled";
    public static final String KEY_EVENING_REVIEW_ENABLED = "evening_review_enabled";
    public static final String KEY_EVENING_HOUR = "evening_hour";
    public static final String KEY_EVENING_MINUTE = "evening_minute";

    private final SharedPreferences prefs;

    public NotificationPreferences(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isImportantTasksEnabled() {
        return prefs.getBoolean(KEY_IMPORTANT_TASKS_ENABLED, true);
    }

    public void setImportantTasksEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_IMPORTANT_TASKS_ENABLED, enabled).apply();
    }

    public boolean isEveningReviewEnabled() {
        return prefs.getBoolean(KEY_EVENING_REVIEW_ENABLED, true);
    }

    public void setEveningReviewEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EVENING_REVIEW_ENABLED, enabled).apply();
    }

    public int getEveningHour() {
        return prefs.getInt(KEY_EVENING_HOUR, 20); // 8:00 PM default
    }

    public int getEveningMinute() {
        return prefs.getInt(KEY_EVENING_MINUTE, 0);
    }

    public void setEveningTime(int hour, int minute) {
        prefs.edit().putInt(KEY_EVENING_HOUR, hour).putInt(KEY_EVENING_MINUTE, minute).apply();
    }
}
