package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.Question;

import java.util.List;

@Dao
public interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Question question);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Question> questions);

    @Update
    void update(Question question);

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1")
    LiveData<Question> getRandomQuestion();

    @Query("SELECT * FROM questions WHERE topic = :topic ORDER BY RANDOM() LIMIT 1")
    LiveData<Question> getRandomQuestionByTopic(String topic);

    @Query("SELECT * FROM questions ORDER BY reviewStatus ASC, lastReviewedTimestamp ASC")
    LiveData<List<Question>> getAllQuestions();

    @Query("SELECT COUNT(*) FROM questions")
    int getQuestionCountDirect();
}
