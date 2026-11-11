package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;

import java.util.List;

@Dao
public interface DailyRoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DailyRoutine routine);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyRoutine> routines);

    @Update
    void update(DailyRoutine routine);

    @Delete
    void delete(DailyRoutine routine);

    @Query("SELECT * FROM daily_routines WHERE isActive = 1 ORDER BY id ASC")
    LiveData<List<DailyRoutine>> getActiveRoutines();

    @Query("SELECT * FROM daily_routines WHERE isActive = 1 ORDER BY id ASC")
    List<DailyRoutine> getActiveRoutinesDirect();

    @Query("SELECT COUNT(*) FROM daily_routines WHERE isActive = 1")
    LiveData<Integer> getActiveRoutineCount();

    @Query("SELECT COUNT(*) FROM daily_routines WHERE isActive = 1")
    int getActiveRoutineCountDirect();

    // Completions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateCompletion(DailyRoutineCompletion completion);

    @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND dateString = :dateString")
    void deleteCompletion(long routineId, String dateString);

    @Query("SELECT * FROM routine_completions WHERE dateString = :dateString")
    LiveData<List<DailyRoutineCompletion>> getCompletionsForDate(String dateString);

    @Query("SELECT * FROM routine_completions WHERE dateString = :dateString")
    List<DailyRoutineCompletion> getCompletionsForDateDirect(String dateString);

    @Query("SELECT COUNT(*) FROM routine_completions WHERE dateString = :dateString AND isCompleted = 1")
    LiveData<Integer> getCompletedCountForDate(String dateString);

    @Query("SELECT COUNT(*) FROM routine_completions WHERE dateString = :dateString AND isCompleted = 1")
    int getCompletedCountForDateDirect(String dateString);

    @Query("SELECT * FROM routine_completions WHERE routineId = :routineId ORDER BY dateString DESC")
    LiveData<List<DailyRoutineCompletion>> getCompletionsForRoutine(long routineId);
}
