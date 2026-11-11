package com.ascendu.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.repository.RoutineRepository;
import com.ascendu.app.data.repository.TaskRepository;

import java.util.List;

public class TasksViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;

    public TasksViewModel(@NonNull Application application) {
        super(application);
        this.taskRepository = new TaskRepository(application);
        this.routineRepository = new RoutineRepository(application);
    }

    // Tasks
    public LiveData<List<Task>> getAllTasks() {
        return taskRepository.getAllTasks();
    }

    public LiveData<List<Task>> getTodayTasks() {
        return taskRepository.getTodayTasks();
    }

    public LiveData<List<Task>> getTasksForDate(long timestamp) {
        return taskRepository.getTasksForDate(timestamp);
    }

    public LiveData<Integer> getPlannedCountForDate(long timestamp) {
        return taskRepository.getPlannedCountForDate(timestamp);
    }

    public LiveData<Integer> getCompletedCountForDate(long timestamp) {
        return taskRepository.getCompletedCountForDate(timestamp);
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return taskRepository.getHighPriorityTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskRepository.getCompletedTasks();
    }

    public void insertTask(Task task) {
        taskRepository.insert(task);
    }

    public void updateTask(Task task) {
        taskRepository.update(task);
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    public void toggleTaskCompletion(Task task) {
        taskRepository.setTaskCompletion(task, !task.isCompleted());
    }

    // Daily Routines / Non-Negotiables
    public LiveData<List<DailyRoutine>> getActiveRoutines() {
        return routineRepository.getActiveRoutines();
    }

    public LiveData<Integer> getActiveRoutineCount() {
        return routineRepository.getActiveRoutineCount();
    }

    public LiveData<List<DailyRoutineCompletion>> getRoutineCompletionsForDate(String dateString) {
        return routineRepository.getCompletionsForDate(dateString);
    }

    public LiveData<Integer> getCompletedRoutineCountForDate(String dateString) {
        return routineRepository.getCompletedCountForDate(dateString);
    }

    public void insertRoutine(DailyRoutine routine) {
        routineRepository.insertRoutine(routine);
    }

    public void deleteRoutine(DailyRoutine routine) {
        routineRepository.deleteRoutine(routine);
    }

    public void toggleRoutineCompletion(DailyRoutine routine, String dateString, boolean isCompleted) {
        routineRepository.setRoutineCompleted(routine.getId(), dateString, isCompleted);
    }
}
