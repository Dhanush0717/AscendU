package com.ascendu.app.ui.onboarding;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ascendu.app.MainActivity;
import com.ascendu.app.R;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.databinding.ActivityOnboardingBinding;
import com.ascendu.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 6-Screen First-Launch Onboarding Activity.
 * Collects preferences, custom/suggested habits, timings, and accountability configuration.
 * All steps are skippable where appropriate and persist safely to Room SQLite.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 6;

    // Stored preferences throughout the wizard
    private String userName = "Ascender";
    private String primaryCategory = "Academic Excellence";
    private final List<String> selectedHabits = new ArrayList<>();
    private String startTime = "08:00";
    private String endTime = "09:30";
    private int daysBitmask = 31; // Mon-Fri (1+2+4+8+16 = 31)
    private boolean isImportantCommitment = true;
    private boolean importantRemindersEnabled = true;
    private boolean eveningReminderEnabled = true;
    private String eveningSummaryTime = "21:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        updateStepUI();
    }

    private void setupListeners() {
        binding.btnOnboardingBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepUI();
            }
        });

        binding.btnOnboardingNext.setOnClickListener(v -> {
            handleStepNext();
        });

        binding.btnOnboardingSkip.setOnClickListener(v -> {
            handleStepSkip();
        });

        // Time pickers in Step 4
        binding.btnPickStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.btnPickEndTime.setOnClickListener(v -> showTimePicker(false));

        // Time picker in Step 5 (Evening summary)
        binding.btnPickEveningTime.setOnClickListener(v -> showEveningTimePicker());
    }

    private void showTimePicker(boolean isStart) {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (isStart) {
                startTime = timeStr;
                binding.btnPickStartTime.setText("Start: " + formatDisplayTime(hourOfDay, minute));
            } else {
                endTime = timeStr;
                binding.btnPickEndTime.setText("End: " + formatDisplayTime(hourOfDay, minute));
            }
        }, isStart ? 8 : 9, isStart ? 0 : 30, false);
        dialog.show();
    }

    private void showEveningTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            eveningSummaryTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            binding.btnPickEveningTime.setText("Evening Review: " + formatDisplayTime(hourOfDay, minute));
        }, 21, 0, false);
        dialog.show();
    }

    private String formatDisplayTime(int hourOfDay, int minute) {
        String amPm = hourOfDay >= 12 ? "PM" : "AM";
        int displayHour = hourOfDay % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
    }

    private void handleStepNext() {
        switch (currentStep) {
            case 1:
                currentStep = 2;
                updateStepUI();
                break;

            case 2:
                String nameInput = binding.etUserName.getText() != null
                        ? binding.etUserName.getText().toString().trim()
                        : "";
                if (TextUtils.isEmpty(nameInput)) {
                    binding.tilUserName.setError("Please enter your name");
                    return;
                }
                binding.tilUserName.setError(null);
                userName = nameInput;

                int checkedCatId = binding.rgProductivityCategory.getCheckedRadioButtonId();
                if (checkedCatId == R.id.rb_academic) primaryCategory = "Academic Excellence";
                else if (checkedCatId == R.id.rb_career) primaryCategory = "Career Growth & Tech";
                else if (checkedCatId == R.id.rb_fitness) primaryCategory = "Fitness & Discipline";
                else if (checkedCatId == R.id.rb_productivity) primaryCategory = "Personal Productivity";
                else if (checkedCatId == R.id.rb_balanced) primaryCategory = "Balanced Lifestyle";

                currentStep = 3;
                updateStepUI();
                break;

            case 3:
                collectSelectedHabits();
                currentStep = 4;
                updateStepUI();
                break;

            case 4:
                calculateWeekdayBitmask();
                isImportantCommitment = binding.switchMarkImportant.isChecked();
                currentStep = 5;
                updateStepUI();
                break;

            case 5:
                importantRemindersEnabled = binding.switchImportantTaskReminders.isChecked();
                eveningReminderEnabled = binding.switchEveningClosingReminder.isChecked();
                currentStep = 6;
                updateStepUI();
                break;

            case 6:
                finishOnboardingAndLaunchApp();
                break;
        }
    }

    private void handleStepSkip() {
        if (currentStep == 3) {
            // User skipped habit creation - clear habits and jump to accountability
            selectedHabits.clear();
            currentStep = 5;
            updateStepUI();
        } else if (currentStep == 4) {
            currentStep = 5;
            updateStepUI();
        }
    }

    private void collectSelectedHabits() {
        selectedHabits.clear();
        if (binding.cbHabitExercise.isChecked()) selectedHabits.add("Morning Physical Training");
        if (binding.cbHabitDeepWork.isChecked()) selectedHabits.add("Deep Focus Study Block");
        if (binding.cbHabitCoding.isChecked()) selectedHabits.add("Coding Practice & Projects");
        if (binding.cbHabitReading.isChecked()) selectedHabits.add("Reading & Conceptual Learning");
        if (binding.cbHabitPlanning.isChecked()) selectedHabits.add("Daily Review & Plan Tomorrow");

        String custom = binding.etCustomHabit.getText() != null
                ? binding.etCustomHabit.getText().toString().trim()
                : "";
        if (!TextUtils.isEmpty(custom)) {
            selectedHabits.add(custom);
        }
    }

    private void calculateWeekdayBitmask() {
        int mask = 0;
        if (binding.chipMon.isChecked()) mask |= (1 << 0);
        if (binding.chipTue.isChecked()) mask |= (1 << 1);
        if (binding.chipWed.isChecked()) mask |= (1 << 2);
        if (binding.chipThu.isChecked()) mask |= (1 << 3);
        if (binding.chipFri.isChecked()) mask |= (1 << 4);
        if (binding.chipSat.isChecked()) mask |= (1 << 5);
        if (binding.chipSun.isChecked()) mask |= (1 << 6);
        daysBitmask = mask > 0 ? mask : 127; // default all days if none checked
    }

    private void updateStepUI() {
        binding.indicatorOnboardingStep.setProgress(currentStep);
        binding.tvOnboardingStepLabel.setText(currentStep + "/" + TOTAL_STEPS);
        binding.btnOnboardingBack.setVisibility(currentStep > 1 ? View.VISIBLE : View.INVISIBLE);

        // Hide all screens
        binding.step1Welcome.setVisibility(View.GONE);
        binding.step2Personalization.setVisibility(View.GONE);
        binding.step3Commitments.setVisibility(View.GONE);
        binding.step4Schedule.setVisibility(View.GONE);
        binding.step5Accountability.setVisibility(View.GONE);
        binding.step6Completion.setVisibility(View.GONE);

        // Default bottom bar
        binding.btnOnboardingSkip.setVisibility(View.GONE);
        binding.btnOnboardingNext.setText("Next");

        switch (currentStep) {
            case 1:
                binding.step1Welcome.setVisibility(View.VISIBLE);
                binding.btnOnboardingNext.setText("Get Started");
                break;
            case 2:
                binding.step2Personalization.setVisibility(View.VISIBLE);
                break;
            case 3:
                binding.step3Commitments.setVisibility(View.VISIBLE);
                binding.btnOnboardingSkip.setVisibility(View.VISIBLE);
                break;
            case 4:
                binding.step4Schedule.setVisibility(View.VISIBLE);
                binding.btnOnboardingSkip.setVisibility(View.VISIBLE);
                break;
            case 5:
                binding.step5Accountability.setVisibility(View.VISIBLE);
                break;
            case 6:
                binding.step6Completion.setVisibility(View.VISIBLE);
                binding.btnOnboardingNext.setText("Start My Journey");
                populateSummaryScreen();
                break;
        }

        binding.scrollOnboardingContent.smoothScrollTo(0, 0);
    }

    private void populateSummaryScreen() {
        binding.tvSummaryName.setText("Welcome, " + userName + "! 🚀");
        if (selectedHabits.isEmpty()) {
            binding.tvSummaryCommitments.setText("• No commitments created yet (Configure in Plan tab anytime)");
        } else {
            binding.tvSummaryCommitments.setText("• " + selectedHabits.size() + " Core Commitments Configured");
        }
        binding.tvSummarySchedule.setText("• Time Window: " + startTime + " - " + endTime + " (" + getWeekdaysLabel(daysBitmask) + ")");
        binding.tvSummaryAccountability.setText("• Daily Review: " + eveningSummaryTime + (eveningReminderEnabled ? " (Alerts ON)" : ""));
    }

    private String getWeekdaysLabel(int bitmask) {
        if ((bitmask & 31) == 31 && (bitmask & 96) == 0) return "Mon - Fri";
        if (bitmask == 127) return "Every Day";
        return "Custom Days";
    }

    private void finishOnboardingAndLaunchApp() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            // 1. Save or update User Profile (Single user profile, id = 1)
            UserProfile profile = new UserProfile(
                    userName,
                    primaryCategory,
                    Math.max(1, selectedHabits.size()),
                    eveningSummaryTime,
                    importantRemindersEnabled || eveningReminderEnabled,
                    false,
                    System.currentTimeMillis(),
                    primaryCategory,
                    60,
                    Math.max(1, selectedHabits.size()),
                    eveningSummaryTime
            );
            db.userProfileDao().insertOrUpdate(profile);

            // 2. Save selected recurring commitments (if any)
            if (!selectedHabits.isEmpty()) {
                List<RecurringTask> tasksToInsert = new ArrayList<>();
                for (String habitTitle : selectedHabits) {
                    RecurringTask task = new RecurringTask(
                            habitTitle,
                            primaryCategory,
                            startTime,
                            endTime,
                            daysBitmask,
                            isImportantCommitment,
                            true,
                            System.currentTimeMillis()
                    );
                    tasksToInsert.add(task);
                }
                db.recurringTaskDao().insertAll(tasksToInsert);
            }

            // 3. Mark Onboarding as Completed in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(MainActivity.KEY_ONBOARDING_COMPLETED, true).apply();

            // 4. Launch MainActivity on the main thread
            AppExecutors.getInstance().mainThread().execute(() -> {
                Toast.makeText(this, "Journey Started!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
