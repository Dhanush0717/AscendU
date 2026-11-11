package com.ascendu.app.ui.plan;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ascendu.app.R;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.DialogAddEditRecurringBinding;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

/**
 * BottomSheet dialog for creating and editing recurring core discipline commitments.
 */
public class AddEditRecurringBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_RECURRING_TASK_ID = "arg_recurring_task_id";

    private DialogAddEditRecurringBinding binding;
    private PlannerRepository plannerRepository;
    private long recurringTaskId = 0;
    private RecurringTask loadedTask = null;

    private String selectedStartTime = "08:00";
    private String selectedEndTime = "09:30";

    public static AddEditRecurringBottomSheet newInstance(long recurringTaskId) {
        AddEditRecurringBottomSheet fragment = new AddEditRecurringBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_RECURRING_TASK_ID, recurringTaskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recurringTaskId = getArguments().getLong(ARG_RECURRING_TASK_ID, 0);
        }
        plannerRepository = new PlannerRepository(requireActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddEditRecurringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateTimeButtonsUI();
        setupListeners();

        if (recurringTaskId > 0) {
            binding.tvSheetTitle.setText("Edit Core Commitment");
            binding.btnSaveRecurring.setText("Update Habit");
            binding.btnDeleteRecurring.setVisibility(View.VISIBLE);
            loadExistingTask();
        } else {
            binding.tvSheetTitle.setText("New Core Commitment");
            binding.btnSaveRecurring.setText("Save Habit");
            binding.btnDeleteRecurring.setVisibility(View.GONE);
        }
    }

    private void updateTimeButtonsUI() {
        binding.btnRecurringStartTime.setText("Start: " + DateUtils.formatTime12Hour(selectedStartTime));
        binding.btnRecurringEndTime.setText("End: " + DateUtils.formatTime12Hour(selectedEndTime));
    }

    private void setupListeners() {
        binding.btnRecurringStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.btnRecurringEndTime.setOnClickListener(v -> showTimePicker(false));

        binding.btnSaveRecurring.setOnClickListener(v -> saveRecurringHabit());

        binding.btnDeleteRecurring.setOnClickListener(v -> {
            if (loadedTask != null) {
                plannerRepository.archiveRecurringTask(loadedTask, () -> {
                    Toast.makeText(requireContext(), "Commitment archived", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        });
    }

    private void showTimePicker(boolean isStart) {
        String currentTime = isStart ? selectedStartTime : selectedEndTime;
        int initialHour = 8;
        int initialMinute = 0;
        try {
            String[] parts = currentTime.split(":");
            initialHour = Integer.parseInt(parts[0]);
            initialMinute = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {
        }

        TimePickerDialog picker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String formatted = String.format("%02d:%02d", hourOfDay, minute);
                    if (isStart) {
                        selectedStartTime = formatted;
                    } else {
                        selectedEndTime = formatted;
                    }
                    updateTimeButtonsUI();
                },
                initialHour,
                initialMinute,
                false
        );
        picker.show();
    }

    private void loadExistingTask() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            RecurringTask task = db.recurringTaskDao().getByIdDirect(recurringTaskId);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (task == null || binding == null) return;
                loadedTask = task;
                binding.etRecurringTitle.setText(task.getTitle());
                if (task.getStartTime() != null && !task.getStartTime().isEmpty()) {
                    selectedStartTime = task.getStartTime();
                }
                if (task.getEndTime() != null && !task.getEndTime().isEmpty()) {
                    selectedEndTime = task.getEndTime();
                }
                updateTimeButtonsUI();

                binding.switchRecurringImportant.setChecked(task.isImportant());

                // Weekday chips
                int mask = task.getDaysOfWeekMask();
                binding.chipRecMon.setChecked((mask & DateUtils.DAY_MON) != 0);
                binding.chipRecTue.setChecked((mask & DateUtils.DAY_TUE) != 0);
                binding.chipRecWed.setChecked((mask & DateUtils.DAY_WED) != 0);
                binding.chipRecThu.setChecked((mask & DateUtils.DAY_THU) != 0);
                binding.chipRecFri.setChecked((mask & DateUtils.DAY_FRI) != 0);
                binding.chipRecSat.setChecked((mask & DateUtils.DAY_SAT) != 0);
                binding.chipRecSun.setChecked((mask & DateUtils.DAY_SUN) != 0);

                // Category selection
                String cat = task.getCategory();
                if (cat != null) {
                    if (cat.contains("Fitness") || cat.contains("Discipline")) {
                        binding.chipCatDiscipline.setChecked(true);
                    } else if (cat.contains("Coding") || cat.contains("Mastery")) {
                        binding.chipCatMastery.setChecked(true);
                    } else if (cat.contains("Reflection")) {
                        binding.chipCatReflection.setChecked(true);
                    } else {
                        binding.chipCatFocus.setChecked(true);
                    }
                }
            });
        });
    }

    private void saveRecurringHabit() {
        String title = binding.etRecurringTitle.getText() != null
                ? binding.etRecurringTitle.getText().toString().trim()
                : "";

        if (title.isEmpty()) {
            binding.tilRecurringTitle.setError("Please enter a habit title");
            return;
        } else {
            binding.tilRecurringTitle.setError(null);
        }

        if (!DateUtils.isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
            Toast.makeText(requireContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        int mask = 0;
        if (binding.chipRecMon.isChecked()) mask |= DateUtils.DAY_MON;
        if (binding.chipRecTue.isChecked()) mask |= DateUtils.DAY_TUE;
        if (binding.chipRecWed.isChecked()) mask |= DateUtils.DAY_WED;
        if (binding.chipRecThu.isChecked()) mask |= DateUtils.DAY_THU;
        if (binding.chipRecFri.isChecked()) mask |= DateUtils.DAY_FRI;
        if (binding.chipRecSat.isChecked()) mask |= DateUtils.DAY_SAT;
        if (binding.chipRecSun.isChecked()) mask |= DateUtils.DAY_SUN;

        if (mask == 0) {
            Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = "Focus & Study";
        if (binding.chipCatDiscipline.isChecked()) category = "Discipline & Fitness";
        else if (binding.chipCatMastery.isChecked()) category = "Coding & Mastery";
        else if (binding.chipCatReflection.isChecked()) category = "Reflection";

        boolean isImportant = binding.switchRecurringImportant.isChecked();

        if (loadedTask != null) {
            // Update
            loadedTask.setTitle(title);
            loadedTask.setStartTime(selectedStartTime);
            loadedTask.setEndTime(selectedEndTime);
            loadedTask.setDaysOfWeekMask(mask);
            loadedTask.setCategory(category);
            loadedTask.setImportant(isImportant);
            loadedTask.setActive(true);

            plannerRepository.updateRecurringTask(loadedTask, () -> {
                if (isImportant) {
                    String todayIso = DateUtils.getTodayDateString();
                    if (DateUtils.isDayScheduled(loadedTask.getDaysOfWeekBitmask(), todayIso)) {
                        com.ascendu.app.notifications.NotificationHelper.scheduleTaskReminder(
                                requireContext(),
                                com.ascendu.app.notifications.NotificationHelper.TYPE_RECURRING,
                                loadedTask.getId(),
                                loadedTask.getTitle(),
                                todayIso,
                                loadedTask.getEndTime()
                        );
                    }
                } else {
                    com.ascendu.app.notifications.NotificationHelper.cancelTaskReminder(
                            requireContext(),
                            com.ascendu.app.notifications.NotificationHelper.TYPE_RECURRING,
                            loadedTask.getId(),
                            DateUtils.getTodayDateString()
                    );
                }
                Toast.makeText(requireContext(), "Habit updated", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        } else {
            // Create
            RecurringTask newTask = new RecurringTask(
                    title,
                    category,
                    selectedStartTime,
                    selectedEndTime,
                    mask,
                    isImportant,
                    true,
                    System.currentTimeMillis()
            );
            plannerRepository.createRecurringTask(newTask, () -> {
                if (isImportant) {
                    String todayIso = DateUtils.getTodayDateString();
                    if (DateUtils.isDayScheduled(newTask.getDaysOfWeekBitmask(), todayIso)) {
                        com.ascendu.app.notifications.NotificationHelper.scheduleTaskReminder(
                                requireContext(),
                                com.ascendu.app.notifications.NotificationHelper.TYPE_RECURRING,
                                newTask.getId(),
                                newTask.getTitle(),
                                todayIso,
                                newTask.getEndTime()
                        );
                    }
                }
                Toast.makeText(requireContext(), "Habit saved to Plan", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
