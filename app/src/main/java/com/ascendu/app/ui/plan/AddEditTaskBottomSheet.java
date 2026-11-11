package com.ascendu.app.ui.plan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.DialogAddEditOneTimeTaskBinding;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

/**
 * BottomSheet dialog for creating and editing discrete one-time scheduled tasks.
 */
public class AddEditTaskBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TASK_ID = "arg_task_id";

    private DialogAddEditOneTimeTaskBinding binding;
    private PlannerRepository plannerRepository;
    private long taskId = 0;
    private Task loadedTask = null;

    private String selectedDate = DateUtils.getTodayIso();
    private String selectedStartTime = "10:00";
    private String selectedEndTime = "11:30";

    public static AddEditTaskBottomSheet newInstance(long taskId) {
        AddEditTaskBottomSheet fragment = new AddEditTaskBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID, 0);
        }
        plannerRepository = new PlannerRepository(requireActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddEditOneTimeTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateDateButtonUI();
        updateTimeButtonsUI();
        setupListeners();

        if (taskId > 0) {
            binding.tvSheetTaskTitle.setText("Edit Scheduled Task");
            binding.btnSaveTask.setText("Update Target");
            binding.btnDeleteTask.setVisibility(View.VISIBLE);
            loadExistingTask();
        } else {
            binding.tvSheetTaskTitle.setText("New One-Time Task");
            binding.btnSaveTask.setText("Save Target");
            binding.btnDeleteTask.setVisibility(View.GONE);
        }
    }

    private void updateDateButtonUI() {
        binding.btnPickTaskDate.setText("📅 Scheduled: " + DateUtils.formatDisplayDate(selectedDate));
    }

    private void updateTimeButtonsUI() {
        binding.btnTaskStartTime.setText("Start: " + DateUtils.formatTime12Hour(selectedStartTime));
        binding.btnTaskEndTime.setText("Deadline: " + DateUtils.formatTime12Hour(selectedEndTime));
    }

    private void setupListeners() {
        binding.btnPickTaskDate.setOnClickListener(v -> showDatePicker());
        binding.btnTaskStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.btnTaskEndTime.setOnClickListener(v -> showTimePicker(false));

        binding.btnSaveTask.setOnClickListener(v -> saveTask());

        binding.btnDeleteTask.setOnClickListener(v -> {
            if (loadedTask != null) {
                plannerRepository.deleteOneTimeTask(loadedTask, () -> {
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            String[] parts = selectedDate.split("-");
            cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        } catch (Exception ignored) {
        }

        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    updateDateButtonUI();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker(boolean isStart) {
        String currentTime = isStart ? selectedStartTime : selectedEndTime;
        int initialHour = 10;
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
            Task task = db.taskDao().getByIdDirect(taskId);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (task == null || binding == null) return;
                loadedTask = task;
                binding.etTaskTitle.setText(task.getTitle());
                binding.etTaskDesc.setText(task.getDescription() != null ? task.getDescription() : "");

                if (task.getScheduledDate() != null && !task.getScheduledDate().isEmpty()) {
                    selectedDate = task.getScheduledDate();
                }
                if (task.getStartTime() != null && !task.getStartTime().isEmpty()) {
                    selectedStartTime = task.getStartTime();
                }
                if (task.getEndTime() != null && !task.getEndTime().isEmpty()) {
                    selectedEndTime = task.getEndTime();
                }
                updateDateButtonUI();
                updateTimeButtonsUI();

                binding.switchTaskImportant.setChecked(task.isImportant());

                // Category selection
                String cat = task.getCategory();
                if (cat != null) {
                    if (cat.contains("Acad")) {
                        binding.chipTaskAcad.setChecked(true);
                    } else if (cat.contains("Personal")) {
                        binding.chipTaskPersonal.setChecked(true);
                    } else {
                        binding.chipTaskWork.setChecked(true);
                    }
                }
            });
        });
    }

    private void saveTask() {
        String title = binding.etTaskTitle.getText() != null
                ? binding.etTaskTitle.getText().toString().trim()
                : "";

        if (title.isEmpty()) {
            binding.tilTaskTitle.setError("Please enter a task title");
            return;
        } else {
            binding.tilTaskTitle.setError(null);
        }

        if (!DateUtils.isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
            Toast.makeText(requireContext(), "Deadline must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = binding.etTaskDesc.getText() != null
                ? binding.etTaskDesc.getText().toString().trim()
                : "";

        String category = "Technical & Work";
        if (binding.chipTaskAcad.isChecked()) category = "Academics";
        else if (binding.chipTaskPersonal.isChecked()) category = "Personal";

        boolean isImportant = binding.switchTaskImportant.isChecked();

        if (loadedTask != null) {
            // Update
            loadedTask.setTitle(title);
            loadedTask.setDescription(description);
            loadedTask.setScheduledDate(selectedDate);
            loadedTask.setStartTime(selectedStartTime);
            loadedTask.setEndTime(selectedEndTime);
            loadedTask.setCategory(category);
            loadedTask.setImportant(isImportant);

            plannerRepository.updateOneTimeTask(loadedTask, () -> {
                if (isImportant) {
                    com.ascendu.app.notifications.NotificationHelper.scheduleTaskReminder(
                            requireContext(),
                            com.ascendu.app.notifications.NotificationHelper.TYPE_ONETIME,
                            loadedTask.getId(),
                            loadedTask.getTitle(),
                            loadedTask.getScheduledDate(),
                            loadedTask.getDeadlineTime()
                    );
                } else {
                    com.ascendu.app.notifications.NotificationHelper.cancelTaskReminder(
                            requireContext(),
                            com.ascendu.app.notifications.NotificationHelper.TYPE_ONETIME,
                            loadedTask.getId(),
                            loadedTask.getScheduledDate()
                    );
                }
                Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        } else {
            // Create
            Task newTask = new Task(
                    title,
                    description,
                    category,
                    selectedDate,
                    selectedStartTime,
                    selectedEndTime,
                    isImportant,
                    Task.STATUS_UPCOMING,
                    false,
                    isImportant ? 3 : 2,
                    0,
                    0,
                    0,
                    System.currentTimeMillis()
            );
            plannerRepository.createOneTimeTask(newTask, () -> {
                if (isImportant) {
                    com.ascendu.app.notifications.NotificationHelper.scheduleTaskReminder(
                            requireContext(),
                            com.ascendu.app.notifications.NotificationHelper.TYPE_ONETIME,
                            newTask.getId(),
                            newTask.getTitle(),
                            newTask.getScheduledDate(),
                            newTask.getDeadlineTime()
                    );
                }
                Toast.makeText(requireContext(), "Task added to Plan", Toast.LENGTH_SHORT).show();
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
