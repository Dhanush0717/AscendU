package com.ascendu.app.ui.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.data.repository.RoutineRepository;
import com.ascendu.app.data.repository.StudyRepository;
import com.ascendu.app.data.repository.TaskRepository;
import com.ascendu.app.data.repository.UserRepository;

public class ProgressViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final StudyRepository studyRepository;
    private final RoutineRepository routineRepository;

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.taskRepository = new TaskRepository(application);
        this.studyRepository = new StudyRepository(application);
        this.routineRepository = new RoutineRepository(application);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userRepository.getUserProfile();
    }

    public LiveData<Integer> getTotalStudyMinutes() {
        return studyRepository.getTotalStudyMinutes();
    }

    public LiveData<Integer> getActiveRoutineCount() {
        return routineRepository.getActiveRoutineCount();
    }
}
