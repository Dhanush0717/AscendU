package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskByIdDirect(long taskId);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getByIdDirect(long taskId);

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, isImportant DESC, dateString ASC, startTime ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY isCompleted ASC, isImportant DESC, startTime ASC")
    LiveData<List<Task>> getTasksForDateString(String dateString);

    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY isCompleted ASC, isImportant DESC, startTime ASC")
    List<Task> getTasksForDateStringDirect(String dateString);

    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY startTime ASC, id ASC")
    LiveData<List<Task>> getTasksForDateChronological(String dateString);

    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY startTime ASC, id ASC")
    List<Task> getTasksForDateChronologicalDirect(String dateString);

    @Query("SELECT * FROM tasks WHERE plannedDate >= :startOfDay AND plannedDate <= :endOfDay ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 OR status = 'COMPLETED'")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE (isImportant = 1 OR priority = 3) AND isCompleted = 0 AND status != 'COMPLETED' ORDER BY dateString ASC, startTime ASC")
    LiveData<List<Task>> getHighPriorityTasks();

    @Query("SELECT COUNT(*) FROM tasks WHERE dateString = :dateString")
    LiveData<Integer> getTaskCountForDate(String dateString);

    @Query("SELECT COUNT(*) FROM tasks WHERE dateString = :dateString AND (isCompleted = 1 OR status = 'COMPLETED')")
    LiveData<Integer> getCompletedTaskCountForDate(String dateString);

    @Query("SELECT COUNT(*) FROM tasks WHERE plannedDate >= :startOfDay AND plannedDate <= :endOfDay")
    LiveData<Integer> getPlannedTaskCountForDay(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM tasks WHERE plannedDate >= :startOfDay AND plannedDate <= :endOfDay AND (isCompleted = 1 OR status = 'COMPLETED')")
    LiveData<Integer> getCompletedTaskCountForDay(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM tasks WHERE plannedDate >= :startOfDay AND plannedDate <= :endOfDay")
    int getPlannedTaskCountForDayDirect(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM tasks WHERE plannedDate >= :startOfDay AND plannedDate <= :endOfDay AND (isCompleted = 1 OR status = 'COMPLETED')")
    int getCompletedTaskCountForDayDirect(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE dateString >= :startDate AND isCompleted = 0 ORDER BY dateString ASC, startTime ASC")
    List<Task> getUpcomingTasksDirect(String startDate);

    @Query("UPDATE tasks SET status = :status, isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    void updateCompletionStatus(long taskId, boolean isCompleted, String status, long completedAt);

    @Query("UPDATE tasks SET status = :status, isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    void updateTaskCompletion(long taskId, String status, boolean isCompleted, long completedAt);

    @Query("UPDATE tasks SET snoozedUntil = :snoozeUntil WHERE id = :taskId")
    void updateSnoozeTime(long taskId, long snoozeUntil);
}
