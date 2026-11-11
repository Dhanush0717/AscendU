package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.TaskDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.util.AppExecutors;

import java.util.Calendar;
import java.util.List;

/**
 * Repository for Task operations.
 * Separates data access from UI and ensures database writes run on background threads.
 */
public class TaskRepository {

    private final TaskDao taskDao;
    private final AppExecutors executors;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.taskDao = db.taskDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTodayTasks() {
        return getTasksForDate(System.currentTimeMillis());
    }

    public LiveData<List<Task>> getTasksForDate(long timestamp) {
        long[] bounds = getDayBounds(timestamp);
        return taskDao.getTasksForDay(bounds[0], bounds[1]);
    }

    public LiveData<Integer> getPlannedCountForDate(long timestamp) {
        long[] bounds = getDayBounds(timestamp);
        return taskDao.getPlannedTaskCountForDay(bounds[0], bounds[1]);
    }

    public LiveData<Integer> getCompletedCountForDate(long timestamp) {
        long[] bounds = getDayBounds(timestamp);
        return taskDao.getCompletedTaskCountForDay(bounds[0], bounds[1]);
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return taskDao.getHighPriorityTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getCompletedTasks();
    }

    public LiveData<Integer> getTodayPlannedCount() {
        long[] bounds = getTodayBounds();
        return taskDao.getPlannedTaskCountForDay(bounds[0], bounds[1]);
    }

    public LiveData<Integer> getTodayCompletedCount() {
        long[] bounds = getTodayBounds();
        return taskDao.getCompletedTaskCountForDay(bounds[0], bounds[1]);
    }

    public void insert(Task task) {
        executors.diskIO().execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        executors.diskIO().execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executors.diskIO().execute(() -> taskDao.delete(task));
    }

    public void setTaskCompletion(Task task, boolean isCompleted) {
        task.setCompleted(isCompleted);
        task.setCompletedAt(isCompleted ? System.currentTimeMillis() : 0);
        update(task);
    }

    public static long[] getDayBounds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    private long[] getTodayBounds() {
        return getDayBounds(System.currentTimeMillis());
    }
}
