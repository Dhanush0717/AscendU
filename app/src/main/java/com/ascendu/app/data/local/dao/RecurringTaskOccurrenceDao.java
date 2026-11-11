package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;

import java.util.List;

@Dao
public interface RecurringTaskOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertIgnore(RecurringTaskOccurrence occurrence);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllIgnore(List<RecurringTaskOccurrence> occurrences);

    @Update
    void update(RecurringTaskOccurrence occurrence);

    @Delete
    void delete(RecurringTaskOccurrence occurrence);

    @Query("SELECT * FROM recurring_task_occurrences WHERE id = :id LIMIT 1")
    RecurringTaskOccurrence getById(long id);

    @Query("SELECT * FROM recurring_task_occurrences WHERE recurringTaskId = :recurringTaskId AND dateString = :dateString LIMIT 1")
    RecurringTaskOccurrence getByTaskAndDateDirect(long recurringTaskId, String dateString);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString = :dateString ORDER BY isImportant DESC, scheduledStartTime ASC, id ASC")
    LiveData<List<RecurringTaskOccurrence>> getOccurrencesForDate(String dateString);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString = :dateString ORDER BY isImportant DESC, scheduledStartTime ASC, id ASC")
    List<RecurringTaskOccurrence> getOccurrencesForDateDirect(String dateString);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString = :dateString ORDER BY scheduledStartTime ASC, id ASC")
    LiveData<List<RecurringTaskOccurrence>> getOccurrencesForDateChronological(String dateString);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString = :dateString ORDER BY scheduledStartTime ASC, id ASC")
    List<RecurringTaskOccurrence> getOccurrencesForDateChronologicalDirect(String dateString);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC, id ASC")
    LiveData<List<RecurringTaskOccurrence>> getOccurrencesBetween(String startDate, String endDate);

    @Query("SELECT * FROM recurring_task_occurrences WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC, id ASC")
    List<RecurringTaskOccurrence> getOccurrencesBetweenDirect(String startDate, String endDate);

    @Query("SELECT * FROM recurring_task_occurrences WHERE recurringTaskId = :taskId AND dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC")
    LiveData<List<RecurringTaskOccurrence>> getOccurrencesForTaskBetween(long taskId, String startDate, String endDate);

    @Query("SELECT * FROM recurring_task_occurrences WHERE recurringTaskId = :taskId AND dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC")
    List<RecurringTaskOccurrence> getOccurrencesForTaskBetweenDirect(long taskId, String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM recurring_task_occurrences WHERE dateString = :dateString AND status != 'CANCELLED'")
    LiveData<Integer> getScheduledCountForDate(String dateString);

    @Query("SELECT COUNT(*) FROM recurring_task_occurrences WHERE dateString = :dateString AND status != 'CANCELLED'")
    int getScheduledCountForDateDirect(String dateString);

    @Query("SELECT COUNT(*) FROM recurring_task_occurrences WHERE dateString = :dateString AND status = 'COMPLETED'")
    LiveData<Integer> getCompletedCountForDate(String dateString);

    @Query("SELECT COUNT(*) FROM recurring_task_occurrences WHERE dateString = :dateString AND status = 'COMPLETED'")
    int getCompletedCountForDateDirect(String dateString);

    @Query("UPDATE recurring_task_occurrences SET status = :status, completedAt = :completedAt WHERE id = :id")
    void updateStatus(long id, String status, long completedAt);

    @Query("UPDATE recurring_task_occurrences SET status = :status, completedAt = :completedAt WHERE recurringTaskId = :recurringTaskId AND dateString = :dateString")
    void updateStatusByTaskAndDate(long recurringTaskId, String dateString, String status, long completedAt);

    @Query("SELECT DISTINCT dateString FROM recurring_task_occurrences ORDER BY dateString ASC")
    List<String> getAllRecordedDatesDirect();
}
