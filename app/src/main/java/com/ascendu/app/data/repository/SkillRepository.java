package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.SkillDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.util.AppExecutors;

import java.util.List;

public class SkillRepository {

    private final SkillDao skillDao;
    private final AppExecutors executors;

    public SkillRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.skillDao = db.skillDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<List<Skill>> getAllSkills() {
        return skillDao.getAllSkills();
    }

    public LiveData<List<Skill>> getSkillsByCategory(String category) {
        return skillDao.getSkillsByCategory(category);
    }

    public LiveData<Skill> getSkillNeedingPractice() {
        return skillDao.getSkillNeedingPractice();
    }

    public void insert(Skill skill) {
        executors.diskIO().execute(() -> skillDao.insert(skill));
    }

    public void update(Skill skill) {
        executors.diskIO().execute(() -> skillDao.update(skill));
    }

    public void delete(Skill skill) {
        executors.diskIO().execute(() -> skillDao.delete(skill));
    }

    public void addPracticeTime(Skill skill, int minutes) {
        skill.setTotalMinutesPracticed(skill.getTotalMinutesPracticed() + minutes);
        skill.setLastPracticedTimestamp(System.currentTimeMillis());
        update(skill);
    }
}
