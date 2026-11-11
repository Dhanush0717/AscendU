package com.ascendu.app.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.data.repository.RoutineRepository;
import com.ascendu.app.data.repository.SkillRepository;
import com.ascendu.app.data.repository.StudyRepository;
import com.ascendu.app.data.repository.TaskRepository;
import com.ascendu.app.data.repository.UserRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final StudyRepository studyRepository;
    private final SkillRepository skillRepository;
    private final RoutineRepository routineRepository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.taskRepository = new TaskRepository(application);
        this.studyRepository = new StudyRepository(application);
        this.skillRepository = new SkillRepository(application);
        this.routineRepository = new RoutineRepository(application);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userRepository.getUserProfile();
    }

    public LiveData<Integer> getTodayPlannedCount() {
        return taskRepository.getTodayPlannedCount();
    }

    public LiveData<Integer> getTodayCompletedCount() {
        return taskRepository.getTodayCompletedCount();
    }

    public LiveData<Integer> getTodayStudyMinutes() {
        return studyRepository.getTodayStudyMinutes();
    }

    public LiveData<Skill> getFocusSkill() {
        return skillRepository.getSkillNeedingPractice();
    }

    public LiveData<List<DailyRoutine>> getActiveRoutines() {
        return routineRepository.getActiveRoutines();
    }

    public LiveData<Integer> getActiveRoutineCount() {
        return routineRepository.getActiveRoutineCount();
    }

    public LiveData<List<DailyRoutineCompletion>> getTodayRoutineCompletions() {
        return routineRepository.getCompletionsForDate(RoutineRepository.getTodayDateString());
    }

    public LiveData<Integer> getTodayCompletedRoutineCount() {
        return routineRepository.getCompletedCountForDate(RoutineRepository.getTodayDateString());
    }

    public void toggleTodayRoutine(DailyRoutine routine, boolean completed) {
        routineRepository.setRoutineCompleted(routine.getId(), RoutineRepository.getTodayDateString(), completed);
    }
}
