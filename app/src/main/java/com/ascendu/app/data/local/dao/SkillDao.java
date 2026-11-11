package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.Skill;

import java.util.List;

@Dao
public interface SkillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Skill skill);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Skill> skills);

    @Update
    void update(Skill skill);

    @Delete
    void delete(Skill skill);

    @Query("SELECT * FROM skills ORDER BY currentLevel DESC, totalMinutesPracticed DESC")
    LiveData<List<Skill>> getAllSkills();

    @Query("SELECT * FROM skills WHERE category = :category ORDER BY currentLevel DESC")
    LiveData<List<Skill>> getSkillsByCategory(String category);

    @Query("SELECT * FROM skills ORDER BY lastPracticedTimestamp ASC LIMIT 1")
    LiveData<Skill> getSkillNeedingPractice();

    @Query("SELECT COUNT(*) FROM skills")
    int getSkillCountDirect();
}
