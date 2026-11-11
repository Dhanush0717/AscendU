package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.StudySessionDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.StudySession;
import com.ascendu.app.util.AppExecutors;

import java.util.Calendar;
import java.util.List;

public class StudyRepository {

    private final StudySessionDao sessionDao;
    private final AppExecutors executors;

    public StudyRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.sessionDao = db.studySessionDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<List<StudySession>> getAllSessions() {
        return sessionDao.getAllSessions();
    }

    public LiveData<Integer> getTodayStudyMinutes() {
        Calendar cal = Calendar.getInstance();
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

        return sessionDao.getTodayStudyMinutes(start, end);
    }

    public LiveData<Integer> getTotalStudyMinutes() {
        return sessionDao.getTotalStudyMinutes();
    }

    public void insertSession(StudySession session) {
        executors.diskIO().execute(() -> sessionDao.insert(session));
    }
}
