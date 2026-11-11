package com.ascendu.app.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.entity.Question;
import com.ascendu.app.data.repository.LearnRepository;

import java.util.List;

public class LearnViewModel extends AndroidViewModel {

    private final LearnRepository repository;

    public LearnViewModel(@NonNull Application application) {
        super(application);
        this.repository = new LearnRepository(application);
    }

    public LiveData<Question> getRandomQuestion() {
        return repository.getRandomQuestion();
    }

    public LiveData<List<Question>> getAllQuestions() {
        return repository.getAllQuestions();
    }

    public void recordEvaluation(Question question, int reviewStatus) {
        repository.updateQuestionEvaluation(question, reviewStatus);
    }

    public void insertQuestion(Question question) {
        repository.insert(question);
    }
}
