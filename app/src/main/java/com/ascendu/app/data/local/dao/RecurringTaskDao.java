package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.RecurringTask;

import java.util.List;

@Dao
public interface RecurringTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecurringTask task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecurringTask> tasks);

    @Update
    void update(RecurringTask task);

    @Delete
    void delete(RecurringTask task);

    @Query("SELECT * FROM recurring_tasks WHERE id = :id LIMIT 1")
    RecurringTask getById(long id);

    @Query("SELECT * FROM recurring_tasks WHERE id = :id LIMIT 1")
    RecurringTask getByIdDirect(long id);

    @Query("SELECT * FROM recurring_tasks WHERE isActive = 1 ORDER BY isImportant DESC, startTime ASC, id ASC")
    LiveData<List<RecurringTask>> getActiveRecurringTasks();

    @Query("SELECT * FROM recurring_tasks WHERE isActive = 1 ORDER BY isImportant DESC, startTime ASC, id ASC")
    List<RecurringTask> getActiveRecurringTasksDirect();

    @Query("SELECT * FROM recurring_tasks ORDER BY isActive DESC, isImportant DESC, id ASC")
    LiveData<List<RecurringTask>> getAllRecurringTasks();

    @Query("SELECT * FROM recurring_tasks ORDER BY isActive DESC, isImportant DESC, id ASC")
    List<RecurringTask> getAllRecurringTasksDirect();

    @Query("SELECT COUNT(*) FROM recurring_tasks WHERE isActive = 1")
    LiveData<Integer> getActiveCount();

    @Query("SELECT COUNT(*) FROM recurring_tasks WHERE isActive = 1")
    int getActiveCountDirect();

    @Query("UPDATE recurring_tasks SET isActive = :isActive WHERE id = :id")
    void setActiveState(long id, boolean isActive);
}
