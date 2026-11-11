package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.QuestionDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.Question;
import com.ascendu.app.util.AppExecutors;

import java.util.List;

public class LearnRepository {

    private final QuestionDao questionDao;
    private final AppExecutors executors;

    public LearnRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.questionDao = db.questionDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<Question> getRandomQuestion() {
        return questionDao.getRandomQuestion();
    }

    public LiveData<Question> getRandomQuestionByTopic(String topic) {
        return questionDao.getRandomQuestionByTopic(topic);
    }

    public LiveData<List<Question>> getAllQuestions() {
        return questionDao.getAllQuestions();
    }

    public void insert(Question question) {
        executors.diskIO().execute(() -> questionDao.insert(question));
    }

    public void updateQuestionEvaluation(Question question, int reviewStatus) {
        question.setReviewStatus(reviewStatus);
        question.setLastReviewedTimestamp(System.currentTimeMillis());
        executors.diskIO().execute(() -> questionDao.update(question));
    }
}
