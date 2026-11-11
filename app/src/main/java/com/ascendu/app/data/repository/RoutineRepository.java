package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.DailyRoutineDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.util.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository for managing daily non-negotiable routines and tracking completions.
 */
public class RoutineRepository {

    private final DailyRoutineDao routineDao;
    private final AppExecutors executors;

    public RoutineRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.routineDao = db.dailyRoutineDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<List<DailyRoutine>> getActiveRoutines() {
        return routineDao.getActiveRoutines();
    }

    public LiveData<Integer> getActiveRoutineCount() {
        return routineDao.getActiveRoutineCount();
    }

    public LiveData<List<DailyRoutineCompletion>> getCompletionsForDate(String dateString) {
        return routineDao.getCompletionsForDate(dateString);
    }

    public LiveData<Integer> getCompletedCountForDate(String dateString) {
        return routineDao.getCompletedCountForDate(dateString);
    }

    public void insertRoutine(DailyRoutine routine) {
        executors.diskIO().execute(() -> routineDao.insert(routine));
    }

    public void updateRoutine(DailyRoutine routine) {
        executors.diskIO().execute(() -> routineDao.update(routine));
    }

    public void deleteRoutine(DailyRoutine routine) {
        executors.diskIO().execute(() -> routineDao.delete(routine));
    }

    public void setRoutineCompleted(long routineId, String dateString, boolean completed) {
        executors.diskIO().execute(() -> {
            if (completed) {
                DailyRoutineCompletion completion = new DailyRoutineCompletion(
                        routineId,
                        dateString,
                        true,
                        System.currentTimeMillis()
                );
                routineDao.insertOrUpdateCompletion(completion);
            } else {
                routineDao.deleteCompletion(routineId, dateString);
            }
        });
    }

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
