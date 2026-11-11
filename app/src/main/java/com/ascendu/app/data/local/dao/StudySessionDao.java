package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ascendu.app.data.local.entity.StudySession;

import java.util.List;

@Dao
public interface StudySessionDao {

    @Insert
    long insert(StudySession session);

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    LiveData<List<StudySession>> getAllSessions();

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    LiveData<Integer> getTodayStudyMinutes(long startOfDay, long endOfDay);

    @Query("SELECT SUM(durationMinutes) FROM study_sessions")
    LiveData<Integer> getTotalStudyMinutes();
}
