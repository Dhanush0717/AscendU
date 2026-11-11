package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.RecurringTaskDao;
import com.ascendu.app.data.local.dao.RecurringTaskOccurrenceDao;
import com.ascendu.app.data.local.dao.TaskDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Authoritative Central Planner Repository.
 * Handles CRUD for Recurring Habits and One-Time Tasks, as well as
 * reliable historical occurrence generation and backfilling.
 */
public class PlannerRepository {

    private final RecurringTaskDao recurringTaskDao;
    private final RecurringTaskOccurrenceDao occurrenceDao;
    private final TaskDao taskDao;
    private final AppExecutors executors;

    public PlannerRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.recurringTaskDao = db.recurringTaskDao();
        this.occurrenceDao = db.recurringTaskOccurrenceDao();
        this.taskDao = db.taskDao();
        this.executors = AppExecutors.getInstance();
    }

    // ========================================================
    // RECURRING CORE COMMITMENTS CRUD
    // ========================================================

    public LiveData<List<RecurringTask>> getAllRecurringTasks() {
        return recurringTaskDao.getAllRecurringTasks();
    }

    public LiveData<List<RecurringTask>> getActiveRecurringTasks() {
        return recurringTaskDao.getActiveRecurringTasks();
    }

    public LiveData<Integer> getActiveRecurringCount() {
        return recurringTaskDao.getActiveCount();
    }

    public void insertRecurringTask(RecurringTask task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            long id = recurringTaskDao.insert(task);
            task.setId(id);
            // Immediately generate occurrence for today if scheduled on today's weekday
            generateOccurrencesForSingleTask(task, DateUtils.getTodayDateString());
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void createRecurringTask(RecurringTask task, OnTaskOperationCallback callback) {
        insertRecurringTask(task, callback);
    }

    public void updateRecurringTask(RecurringTask task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            recurringTaskDao.update(task);
            // If active and scheduled today, ensure occurrence reflects active state
            if (task.isActive()) {
                generateOccurrencesForSingleTask(task, DateUtils.getTodayDateString());
            }
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void setRecurringTaskActiveState(long taskId, boolean isActive, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            recurringTaskDao.setActiveState(taskId, isActive);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void toggleRecurringTaskActive(RecurringTask task, boolean isActive, OnTaskOperationCallback callback) {
        setRecurringTaskActiveState(task.getId(), isActive, callback);
    }

    public void archiveRecurringTask(RecurringTask task, OnTaskOperationCallback callback) {
        deleteRecurringTask(task, callback);
    }

    public void deleteRecurringTask(RecurringTask task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            // Soft-archive by default to protect historical occurrences in My Arc
            recurringTaskDao.setActiveState(task.getId(), false);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    // ========================================================
    // ONE-TIME TASKS CRUD
    // ========================================================

    public LiveData<List<Task>> getAllOneTimeTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getOneTimeTasksForDate(String dateString) {
        return taskDao.getTasksForDateString(dateString);
    }

    public void insertOneTimeTask(Task task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            taskDao.insert(task);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void createOneTimeTask(Task task, OnTaskOperationCallback callback) {
        insertOneTimeTask(task, callback);
    }

    public void updateOneTimeTask(Task task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            taskDao.update(task);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void toggleTaskCompletion(Task task, boolean isCompleted) {
        executors.diskIO().execute(() -> {
            String newStatus = isCompleted ? Task.STATUS_COMPLETED : Task.STATUS_UPCOMING;
            long completedAt = isCompleted ? System.currentTimeMillis() : 0;
            taskDao.updateTaskCompletion(task.getId(), newStatus, isCompleted, completedAt);
        });
    }

    public void toggleOneTimeTaskCompletion(Task task, boolean isCompleted, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            toggleTaskCompletion(task, isCompleted);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    public void deleteOneTimeTask(Task task, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            taskDao.delete(task);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    // ========================================================
    // OCCURRENCE GENERATION & RELIABLE BACKFILLING
    // ========================================================

    public LiveData<List<RecurringTaskOccurrence>> getOccurrencesForDateChronological(String dateString) {
        return occurrenceDao.getOccurrencesForDateChronological(dateString);
    }

    public LiveData<List<Task>> getOneTimeTasksForDateChronological(String dateString) {
        return taskDao.getTasksForDateChronological(dateString);
    }

    public void toggleOccurrenceCompletion(RecurringTaskOccurrence occurrence, boolean isCompleted, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            String newStatus = isCompleted ? Task.STATUS_COMPLETED : Task.STATUS_UPCOMING;
            long completedAt = isCompleted ? System.currentTimeMillis() : 0;
            occurrenceDao.updateStatus(occurrence.getId(), newStatus, completedAt);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    /**
     * Calculates the consecutive day streak where at least 80% of scheduled recurring tasks were completed.
     * Evaluates backwards from yesterday (or today if today already achieved >= 80%).
     */
    public int calculateStreak(String todayStr) {
        List<String> dates = occurrenceDao.getAllRecordedDatesDirect();
        if (dates.isEmpty()) return 0;

        int streak = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());

        try {
            Date today = sdf.parse(todayStr);
            if (today == null) return 0;

            // Check if today already achieved >= 80% completion
            int todayScheduled = occurrenceDao.getScheduledCountForDateDirect(todayStr);
            int todayCompleted = occurrenceDao.getCompletedCountForDateDirect(todayStr);
            boolean todayQualified = (todayScheduled > 0 && ((double) todayCompleted / todayScheduled) >= 0.80);

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);

            if (todayQualified) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                // Today not yet qualified or still in progress; streak is based on preceding days
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }

            // Check past days consecutively
            while (true) {
                String checkDateStr = sdf.format(cal.getTime());
                int scheduled = occurrenceDao.getScheduledCountForDateDirect(checkDateStr);

                if (scheduled == 0) {
                    // Rest day (no scheduled recurring tasks) - does not break the streak
                    // But if this date is before any recorded history, break
                    if (dates.get(0).compareTo(checkDateStr) > 0) {
                        break;
                    }
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    continue;
                }

                int completed = occurrenceDao.getCompletedCountForDateDirect(checkDateStr);
                double ratio = (double) completed / scheduled;
                if (ratio >= 0.80) {
                    streak++;
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                } else {
                    // Day failed 80% threshold, streak ends
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        return streak;
    }

    public void ensureOccurrencesForDate(String dateString) {
        executors.diskIO().execute(() -> ensureOccurrencesForDateInternal(dateString));
    }

    public void ensureOccurrencesForDate(String dateString, OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            ensureOccurrencesForDateInternal(dateString);
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    private void ensureOccurrencesForDateInternal(String dateString) {
        List<RecurringTask> activeTasks = recurringTaskDao.getActiveRecurringTasksDirect();
        List<RecurringTaskOccurrence> occurrencesToInsert = new ArrayList<>();

        for (RecurringTask task : activeTasks) {
            if (DateUtils.isDayScheduled(task.getDaysOfWeekBitmask(), dateString)) {
                // Check if occurrence already exists
                RecurringTaskOccurrence existing = occurrenceDao.getByTaskAndDateDirect(task.getId(), dateString);
                if (existing == null) {
                    // Determine initial status based on whether dateString is before today
                    String status = Task.STATUS_UPCOMING;
                    String todayStr = DateUtils.getTodayDateString();
                    if (dateString.compareTo(todayStr) < 0) {
                        status = Task.STATUS_MISSED; // Past date elapsed without completion
                    }

                    RecurringTaskOccurrence occ = new RecurringTaskOccurrence(
                            task.getId(),
                            task.getTitle(),
                            task.getCategory(),
                            dateString,
                            task.getStartTime(),
                            task.getEndTime(),
                            task.isImportant(),
                            status,
                            0
                    );
                    occurrencesToInsert.add(occ);
                }
            }
        }

        if (!occurrencesToInsert.isEmpty()) {
            occurrenceDao.insertAllIgnore(occurrencesToInsert);
        }
    }

    public void backfillMissedOccurrences() {
        executors.diskIO().execute(this::backfillMissedOccurrencesInternal);
    }

    public void backfillMissedOccurrences(OnTaskOperationCallback callback) {
        executors.diskIO().execute(() -> {
            backfillMissedOccurrencesInternal();
            if (callback != null) {
                executors.mainThread().execute(callback::onSuccess);
            }
        });
    }

    private void backfillMissedOccurrencesInternal() {
        String todayStr = DateUtils.getTodayDateString();
        List<String> recordedDates = occurrenceDao.getAllRecordedDatesDirect();

        String startDateStr = todayStr;
        if (!recordedDates.isEmpty()) {
            startDateStr = recordedDates.get(recordedDates.size() - 1);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
            Date startDate = sdf.parse(startDateStr);
            Date today = sdf.parse(todayStr);

            if (startDate != null && today != null && startDate.before(today)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DAY_OF_YEAR, 1);

                while (!cal.getTime().after(today)) {
                    String dateStr = sdf.format(cal.getTime());
                    ensureOccurrencesForDateInternal(dateStr);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
            } else {
                ensureOccurrencesForDateInternal(todayStr);
            }
        } catch (Exception ignored) {
            ensureOccurrencesForDateInternal(todayStr);
        }
    }

    private void generateOccurrencesForSingleTask(RecurringTask task, String dateString) {
        if (task.isActive() && DateUtils.isDayScheduled(task.getDaysOfWeekBitmask(), dateString)) {
            RecurringTaskOccurrence existing = occurrenceDao.getByTaskAndDateDirect(task.getId(), dateString);
            if (existing == null) {
                RecurringTaskOccurrence occ = new RecurringTaskOccurrence(
                        task.getId(),
                        task.getTitle(),
                        task.getCategory(),
                        dateString,
                        task.getStartTime(),
                        task.getEndTime(),
                        task.isImportant(),
                        Task.STATUS_UPCOMING,
                        0
                );
                occurrenceDao.insertIgnore(occ);
            }
        }
    }

    public LiveData<List<RecurringTaskOccurrence>> getOccurrencesBetween(String startDate, String endDate) {
        return occurrenceDao.getOccurrencesBetween(startDate, endDate);
    }

    public List<RecurringTaskOccurrence> getOccurrencesBetweenDirect(String startDate, String endDate) {
        return occurrenceDao.getOccurrencesBetweenDirect(startDate, endDate);
    }

    public List<RecurringTask> getAllRecurringTasksDirect() {
        return recurringTaskDao.getAllRecurringTasksDirect();
    }

    public int calculateCurrentStreak() {
        String today = DateUtils.getTodayDateString();
        List<String> dates = occurrenceDao.getAllRecordedDatesDirect();
        if (dates.isEmpty()) return 0;

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());

        boolean todayEvaluated = false;
        int todayScheduled = occurrenceDao.getScheduledCountForDateDirect(today);
        if (todayScheduled > 0) {
            int todayDone = occurrenceDao.getCompletedCountForDateDirect(today);
            double ratio = (double) todayDone / todayScheduled;
            if (ratio >= 0.80) {
                streak++;
                todayEvaluated = true;
            }
        }

        cal.add(Calendar.DAY_OF_YEAR, -1);
        while (true) {
            String checkDate = sdf.format(cal.getTime());
            if (!dates.contains(checkDate) && cal.getTime().before(new Date())) {
                boolean hasEarlier = false;
                for (String d : dates) {
                    if (d.compareTo(checkDate) < 0) {
                        hasEarlier = true;
                        break;
                    }
                }
                if (!hasEarlier) break;
            }

            int scheduled = occurrenceDao.getScheduledCountForDateDirect(checkDate);
            if (scheduled == 0) {
                // Rest day, preserve continuity
                cal.add(Calendar.DAY_OF_YEAR, -1);
                continue;
            }

            int completed = occurrenceDao.getCompletedCountForDateDirect(checkDate);
            double ratio = (double) completed / scheduled;
            if (ratio >= 0.80) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    public int calculateLongestStreak() {
        List<String> recordedDates = occurrenceDao.getAllRecordedDatesDirect();
        if (recordedDates.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
        int maxStreak = 0;
        int currentStreak = 0;

        try {
            Date firstDate = sdf.parse(recordedDates.get(0));
            Date lastDate = sdf.parse(recordedDates.get(recordedDates.size() - 1));
            Date today = sdf.parse(DateUtils.getTodayDateString());

            if (firstDate == null || lastDate == null) return 0;
            Date endDate = lastDate.before(today) ? lastDate : today;

            Calendar cal = Calendar.getInstance();
            cal.setTime(firstDate);

            while (!cal.getTime().after(endDate)) {
                String dStr = sdf.format(cal.getTime());
                int scheduled = occurrenceDao.getScheduledCountForDateDirect(dStr);
                if (scheduled == 0) {
                    // Rest day, does not break or increment streak
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    continue;
                }

                int completed = occurrenceDao.getCompletedCountForDateDirect(dStr);
                double ratio = (double) completed / scheduled;
                if (ratio >= 0.80) {
                    currentStreak++;
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak;
                    }
                } else {
                    currentStreak = 0;
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception ignored) {
        }

        return maxStreak;
    }

    public com.ascendu.app.data.model.MonthlyConsistencyStats calculateMonthlyStats(String startDate, String endDate) {
        String today = DateUtils.getTodayDateString();
        String currentTime = DateUtils.getCurrentTime24H();
        List<RecurringTaskOccurrence> occurrences = occurrenceDao.getOccurrencesBetweenDirect(startDate, endDate);

        int completed = 0;
        int missed = 0;
        int eligible = 0;
        int[] weekdayCompletions = new int[8]; // 1=Sun, 2=Mon... 7=Sat

        for (RecurringTaskOccurrence occ : occurrences) {
            if (Task.STATUS_CANCELLED.equals(occ.getStatus())) continue;

            String occDate = occ.getDateString();
            if (occDate.compareTo(today) > 0) continue; // Future date, not yet due

            boolean isElapsed;
            if (occDate.compareTo(today) < 0) {
                isElapsed = true;
            } else {
                // Today: elapsed if deadline passed or already completed
                boolean timePassed = DateUtils.isTimePast(occ.getScheduledEndTime(), currentTime);
                isElapsed = Task.STATUS_COMPLETED.equals(occ.getStatus()) || timePassed;
            }

            if (isElapsed) {
                eligible++;
                if (Task.STATUS_COMPLETED.equals(occ.getStatus())) {
                    completed++;
                    int dayOfWeek = DateUtils.getDayOfWeek(occDate);
                    if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                        weekdayCompletions[dayOfWeek]++;
                    }
                } else {
                    missed++;
                }
            }
        }

        int pct = eligible > 0 ? (int) Math.round(((double) completed / eligible) * 100) : 0;

        String bestDay = "None";
        int maxDayCount = 0;
        String[] dayNames = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int i = 1; i <= 7; i++) {
            if (weekdayCompletions[i] > maxDayCount) {
                maxDayCount = weekdayCompletions[i];
                bestDay = dayNames[i];
            }
        }

        return new com.ascendu.app.data.model.MonthlyConsistencyStats(completed, missed, eligible, pct, bestDay);
    }

    public com.ascendu.app.data.model.TaskMonthlyStats calculateTaskStats(long taskId, String startDate, String endDate) {
        RecurringTask task = recurringTaskDao.getByIdDirect(taskId);
        String taskTitle = task != null ? task.getTitle() : "Commitment";
        String today = DateUtils.getTodayDateString();
        String currentTime = DateUtils.getCurrentTime24H();

        List<RecurringTaskOccurrence> occurrences = occurrenceDao.getOccurrencesForTaskBetweenDirect(taskId, startDate, endDate);

        int completed = 0;
        int missed = 0;
        int eligible = 0;
        int[] weekdayCompletions = new int[8];

        for (RecurringTaskOccurrence occ : occurrences) {
            if (Task.STATUS_CANCELLED.equals(occ.getStatus())) continue;

            String occDate = occ.getDateString();
            if (occDate.compareTo(today) > 0) continue;

            boolean isElapsed;
            if (occDate.compareTo(today) < 0) {
                isElapsed = true;
            } else {
                boolean timePassed = DateUtils.isTimePast(occ.getScheduledEndTime(), currentTime);
                isElapsed = Task.STATUS_COMPLETED.equals(occ.getStatus()) || timePassed;
            }

            if (isElapsed) {
                eligible++;
                if (Task.STATUS_COMPLETED.equals(occ.getStatus())) {
                    completed++;
                    int dayOfWeek = DateUtils.getDayOfWeek(occDate);
                    if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                        weekdayCompletions[dayOfWeek]++;
                    }
                } else {
                    missed++;
                }
            }
        }

        int pct = eligible > 0 ? (int) Math.round(((double) completed / eligible) * 100) : 0;

        String bestDay = "None";
        int maxDayCount = 0;
        String[] dayNames = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int i = 1; i <= 7; i++) {
            if (weekdayCompletions[i] > maxDayCount) {
                maxDayCount = weekdayCompletions[i];
                bestDay = dayNames[i];
            }
        }

        return new com.ascendu.app.data.model.TaskMonthlyStats(taskId, taskTitle, completed, missed, eligible, pct, bestDay);
    }

    public interface OnTaskOperationCallback {
        void onSuccess();
    }
}
