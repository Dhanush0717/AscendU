package com.ascendu.app.ui.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ascendu.app.data.local.entity.Question;
import com.ascendu.app.databinding.FragmentLearnBinding;

import java.util.List;

public class LearnFragment extends Fragment {

    private FragmentLearnBinding binding;
    private LearnViewModel viewModel;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private Question currentQuestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LearnViewModel.class);

        setupClickListeners();
        observeQuestions();
    }

    private void observeQuestions() {
        viewModel.getAllQuestions().observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                questionList = questions;
                displayCurrentQuestion();
            }
        });
    }

    private void displayCurrentQuestion() {
        if (questionList == null || questionList.isEmpty()) return;

        if (currentQuestionIndex >= questionList.size()) {
            currentQuestionIndex = 0;
        }

        currentQuestion = questionList.get(currentQuestionIndex);
        binding.tvQuestionTopic.setText(currentQuestion.getTopic());
        binding.tvQuestionDifficulty.setText(currentQuestion.getDifficulty());
        binding.tvQuestionText.setText(currentQuestion.getQuestionText());
        binding.tvReferenceAnswer.setText(currentQuestion.getReferenceAnswer());

        // Reset reveal state
        binding.layoutAnswerContent.setVisibility(View.GONE);
        binding.btnRevealAnswer.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        binding.btnRevealAnswer.setOnClickListener(v -> {
            binding.layoutAnswerContent.setVisibility(View.VISIBLE);
            binding.btnRevealAnswer.setVisibility(View.GONE);
        });

        binding.btnEvalRevision.setOnClickListener(v -> handleEvaluation(1, "Marked for Revision"));
        binding.btnEvalUnderstood.setOnClickListener(v -> handleEvaluation(2, "Marked as Understood"));
        binding.btnEvalMastered.setOnClickListener(v -> handleEvaluation(3, "Marked as Mastered! Keep it up!"));

        binding.btnNextQuestion.setOnClickListener(v -> {
            if (questionList != null && !questionList.isEmpty()) {
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                displayCurrentQuestion();
            }
        });
    }

    private void handleEvaluation(int status, String toastMessage) {
        if (currentQuestion != null) {
            viewModel.recordEvaluation(currentQuestion, status);
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
            // Automatically advance to the next question
            if (questionList != null && !questionList.isEmpty()) {
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                displayCurrentQuestion();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
