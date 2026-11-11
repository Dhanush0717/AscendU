package com.ascendu.app.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.StudySession;
import com.ascendu.app.data.repository.StudyRepository;
import com.ascendu.app.databinding.DialogStudyTimerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

/**
 * Live Study Session Timer Dialog.
 * Demonstrates MCA Syllabus: Handlers, Runnables, UI Updating, and Database Persistence.
 */
public class StartSessionBottomSheet extends BottomSheetDialogFragment {

    private DialogStudyTimerBinding binding;
    private StudyRepository studyRepository;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int secondsElapsed = 0;
    private boolean isTimerRunning = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                secondsElapsed++;
                updateTimerDisplay();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    public static StartSessionBottomSheet newInstance() {
        return new StartSessionBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogStudyTimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        studyRepository = new StudyRepository(requireActivity().getApplication());

        binding.btnStartPauseTimer.setOnClickListener(v -> toggleTimer());
        binding.btnFinishSaveSession.setOnClickListener(v -> finishAndSaveSession());
    }

    private void toggleTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            timerHandler.post(timerRunnable);
            binding.btnStartPauseTimer.setText("Pause Timer");
            binding.tvTimerStatus.setText("Studying in progress...");
        } else {
            isTimerRunning = false;
            timerHandler.removeCallbacks(timerRunnable);
            binding.btnStartPauseTimer.setText("Resume Timer");
            binding.tvTimerStatus.setText("Timer paused");
        }
    }

    private void updateTimerDisplay() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        binding.tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void finishAndSaveSession() {
        if (isTimerRunning) {
            toggleTimer();
        }

        String subject = binding.etSessionSubject.getText() != null
                ? binding.etSessionSubject.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(subject)) {
            subject = "Core Computer Science Study";
        }

        // Determine session type
        String sessionType = "Focused Study";
        int checkedChipId = binding.chipGroupSessionType.getCheckedChipId();
        if (checkedChipId == R.id.chip_type_coding) {
            sessionType = "Coding";
        } else if (checkedChipId == R.id.chip_type_dsa) {
            sessionType = "DSA Practice";
        } else if (checkedChipId == R.id.chip_type_revision) {
            sessionType = "Revision";
        }

        int durationMinutes = Math.max(1, secondsElapsed / 60);

        StudySession session = new StudySession(
                subject,
                durationMinutes,
                sessionType,
                "Focused session logged via timer",
                System.currentTimeMillis()
        );

        studyRepository.insertSession(session);
        Toast.makeText(requireContext(), "Study session logged: " + durationMinutes + " mins!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        binding = null;
    }
}
