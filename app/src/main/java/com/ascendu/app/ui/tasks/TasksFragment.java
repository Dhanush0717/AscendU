package com.ascendu.app.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.databinding.FragmentTasksBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Tasks & Blueprint Fragment.
 * Features:
 * 1. Day & Month Customizer with DatePickerDialog.
 * 2. Daily Non-Negotiables (Routines) tracking.
 * 3. Date-filtered task execution.
 */
public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener, RoutineAdapter.OnRoutineClickListener {

    private FragmentTasksBinding binding;
    private TasksViewModel viewModel;
    private TaskAdapter taskAdapter;
    private RoutineAdapter routineAdapter;

    private LiveData<List<Task>> currentTaskSource;
    private long selectedTimestamp = System.currentTimeMillis();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        setupRecyclerViews();
        setupDateControls();
        setupFilters();
        setupActions();

        loadSelectedDate(selectedTimestamp);
    }

    private void setupRecyclerViews() {
        // Routines RecyclerView
        routineAdapter = new RoutineAdapter(this);
        binding.rvRoutines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRoutines.setAdapter(routineAdapter);

        // Tasks RecyclerView
        taskAdapter = new TaskAdapter(this);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(taskAdapter);
    }

    private void setupDateControls() {
        binding.btnSelectCalendarDate.setOnClickListener(v -> showDatePicker());

        binding.btnPrevDay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedTimestamp);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            loadSelectedDate(cal.getTimeInMillis());
        });

        binding.btnNextDay.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedTimestamp);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            loadSelectedDate(cal.getTimeInMillis());
        });

        binding.btnJumpToday.setOnClickListener(v -> {
            loadSelectedDate(System.currentTimeMillis());
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedTimestamp);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 12, 0, 0);
                    loadSelectedDate(picked.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadSelectedDate(long timestamp) {
        this.selectedTimestamp = timestamp;

        SimpleDateFormat displaySdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        binding.btnSelectCalendarDate.setText("📅 " + displaySdf.format(new Date(timestamp)));

        String dateString = getDateString(timestamp);

        // 1. Observe Daily Routines and their completions for this date
        viewModel.getActiveRoutines().observe(getViewLifecycleOwner(), routines -> {
            routineAdapter.submitList(routines);
        });

        viewModel.getRoutineCompletionsForDate(dateString).observe(getViewLifecycleOwner(), completions -> {
            Set<Long> completedIds = new HashSet<>();
            if (completions != null) {
                for (DailyRoutineCompletion c : completions) {
                    if (c.isCompleted()) {
                        completedIds.add(c.getRoutineId());
                    }
                }
            }
            routineAdapter.setCompletedRoutineIds(completedIds);
        });

        // 2. Observe Tasks for this date
        observeTaskSource(viewModel.getTasksForDate(selectedTimestamp));

        // 3. Update day summary
        viewModel.getPlannedCountForDate(selectedTimestamp).observe(getViewLifecycleOwner(), planned -> {
            int plannedCount = planned != null ? planned : 0;
            Integer completed = viewModel.getCompletedCountForDate(selectedTimestamp).getValue();
            int completedCount = completed != null ? completed : 0;
            binding.tvDayStatusSummary.setText(completedCount + " of " + plannedCount + " scheduled targets completed");
        });
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_for_date) {
                observeTaskSource(viewModel.getTasksForDate(selectedTimestamp));
            } else if (checkedId == R.id.chip_all) {
                observeTaskSource(viewModel.getAllTasks());
            } else if (checkedId == R.id.chip_high_priority) {
                observeTaskSource(viewModel.getHighPriorityTasks());
            } else if (checkedId == R.id.chip_completed) {
                observeTaskSource(viewModel.getCompletedTasks());
            }
        });
    }

    private void observeTaskSource(LiveData<List<Task>> source) {
        if (currentTaskSource != null) {
            currentTaskSource.removeObservers(getViewLifecycleOwner());
        }
        currentTaskSource = source;
        currentTaskSource.observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            if (tasks == null || tasks.isEmpty()) {
                binding.layoutEmptyTasks.setVisibility(View.VISIBLE);
                binding.rvTasks.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyTasks.setVisibility(View.GONE);
                binding.rvTasks.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupActions() {
        binding.btnAddRoutine.setOnClickListener(v -> {
            AddRoutineBottomSheet.newInstance().show(getParentFragmentManager(), "AddRoutineBottomSheet");
        });

        binding.fabAddTask.setOnClickListener(v -> {
            AddTaskBottomSheet.newInstance(selectedTimestamp).show(getParentFragmentManager(), "AddTaskBottomSheet");
        });
    }

    // Task click listeners
    @Override
    public void onToggleComplete(Task task) {
        viewModel.toggleTaskCompletion(task);
    }

    @Override
    public void onDeleteClick(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Target")
                .setMessage("Remove \"" + task.getTitle() + "\" from your blueprint?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTask(task);
                    Toast.makeText(requireContext(), "Target deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Routine click listeners
    @Override
    public void onToggleComplete(DailyRoutine routine, boolean isCompleted) {
        String dateString = getDateString(selectedTimestamp);
        viewModel.toggleRoutineCompletion(routine, dateString, isCompleted);
    }

    @Override
    public void onDeleteClick(DailyRoutine routine) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Non-Negotiable")
                .setMessage("Remove \"" + routine.getTitle() + "\" from your daily blueprint?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteRoutine(routine);
                    Toast.makeText(requireContext(), "Routine removed from daily blueprint", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getDateString(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
