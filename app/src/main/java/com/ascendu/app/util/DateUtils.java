package com.ascendu.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Standard Date & Calendar Utilities for AscendU.
 * Handles date formatting, weekday bitmask calculations, and calendar bounds.
 */
public class DateUtils {

    public static final String FORMAT_ISO_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DISPLAY_DATE = "EEEE, MMMM d";
    public static final String FORMAT_DISPLAY_DATE_SHORT = "EEE, MMM d";
    public static final String FORMAT_TIME_24H = "HH:mm";
    public static final String FORMAT_TIME_12H = "hh:mm a";

    public static String getCurrentTime24H() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_24H, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Checks whether deadline (in HH:mm format) has already passed relative to currentTime (HH:mm).
     * Returns true if currentTime is strictly greater than deadline.
     */
    public static boolean isTimePast(String deadlineTime, String currentTime) {
        if (deadlineTime == null || deadlineTime.trim().isEmpty() ||
                currentTime == null || currentTime.trim().isEmpty()) {
            return false;
        }
        return currentTime.compareTo(deadlineTime) > 0;
    }

    // Day of week bitmask constants (Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64)
    public static final int DAY_MON = 1;
    public static final int DAY_TUE = 2;
    public static final int DAY_WED = 4;
    public static final int DAY_THU = 8;
    public static final int DAY_FRI = 16;
    public static final int DAY_SAT = 32;
    public static final int DAY_SUN = 64;

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getTodayIso() {
        return getTodayDateString();
    }

    public static String formatTime12Hour(String time24) {
        return format12HourTime(time24);
    }

    public static String formatDateString(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDisplayDate(String dateString) {
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
            Date date = inSdf.parse(dateString);
            if (date != null) {
                SimpleDateFormat outSdf = new SimpleDateFormat(FORMAT_DISPLAY_DATE, Locale.getDefault());
                return outSdf.format(date);
            }
        } catch (ParseException ignored) {
        }
        return dateString;
    }

    public static String formatDisplayDateShort(String dateString) {
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
            Date date = inSdf.parse(dateString);
            if (date != null) {
                SimpleDateFormat outSdf = new SimpleDateFormat(FORMAT_DISPLAY_DATE_SHORT, Locale.getDefault());
                return outSdf.format(date);
            }
        } catch (ParseException ignored) {
        }
        return dateString;
    }

    public static String format12HourTime(String time24) {
        if (time24 == null || time24.trim().isEmpty()) return "";
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(FORMAT_TIME_24H, Locale.getDefault());
            Date date = inSdf.parse(time24);
            if (date != null) {
                SimpleDateFormat outSdf = new SimpleDateFormat(FORMAT_TIME_12H, Locale.getDefault());
                return outSdf.format(date);
            }
        } catch (ParseException ignored) {
        }
        return time24;
    }

    /**
     * Checks if a 7-bit bitmask includes the day of week for a specific dateString.
     * Mon=1(bit 0), Tue=2(bit 1), Wed=3(bit 2), Thu=4(bit 3), Fri=5(bit 4), Sat=6(bit 5), Sun=7(bit 6).
     */
    public static boolean isDayScheduled(int bitmask, String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                // In Java Calendar, SUNDAY=1, MONDAY=2, TUESDAY=3, WEDNESDAY=4, THURSDAY=5, FRIDAY=6, SATURDAY=7
                int bitIndex;
                if (dayOfWeek == Calendar.SUNDAY) {
                    bitIndex = 6;
                } else {
                    bitIndex = dayOfWeek - 2; // MONDAY(2) -> 0, TUESDAY(3) -> 1 ... SATURDAY(7) -> 5
                }
                return (bitmask & (1 << bitIndex)) != 0;
            }
        } catch (ParseException ignored) {
        }
        return false;
    }

    public static String getBitmaskDaysLabel(int bitmask) {
        if ((bitmask & 31) == 31 && (bitmask & 96) == 0) return "Mon - Fri";
        if (bitmask == 127) return "Every Day";
        if (bitmask == 96) return "Weekends";
        if (bitmask == 0) return "Rest (None)";

        StringBuilder sb = new StringBuilder();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            if ((bitmask & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(days[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Compares two "HH:mm" time strings.
     * Returns true if endTime is strictly after startTime.
     */
    public static boolean isEndTimeAfterStartTime(String startTime, String endTime) {
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            return true; // Not both provided, no conflict
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_24H, Locale.getDefault());
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            if (start != null && end != null) {
                return end.after(start);
            }
        } catch (ParseException ignored) {
        }
        return true;
    }

    public static String getDayOfWeekName(String dateString) {
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
            Date date = inSdf.parse(dateString);
            if (date != null) {
                SimpleDateFormat outSdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                return outSdf.format(date);
            }
        } catch (ParseException ignored) {
        }
        return "";
    }

    public static int getDayOfWeek(String dateString) {
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(FORMAT_ISO_DATE, Locale.getDefault());
            Date date = inSdf.parse(dateString);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal.get(Calendar.DAY_OF_WEEK);
            }
        } catch (ParseException ignored) {
        }
        return -1;
    }
}
