package com.ascendu.app.ui.skills;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.data.repository.SkillRepository;

import java.util.List;

public class SkillsViewModel extends AndroidViewModel {

    private final SkillRepository repository;

    public SkillsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SkillRepository(application);
    }

    public LiveData<List<Skill>> getAllSkills() {
        return repository.getAllSkills();
    }

    public LiveData<List<Skill>> getSkillsByCategory(String category) {
        return repository.getSkillsByCategory(category);
    }

    public void insertSkill(Skill skill) {
        repository.insert(skill);
    }

    public void updateSkill(Skill skill) {
        repository.update(skill);
    }

    public void deleteSkill(Skill skill) {
        repository.delete(skill);
    }

    public void addPracticeMinutes(Skill skill, int minutes) {
        repository.addPracticeTime(skill, minutes);
    }
}
