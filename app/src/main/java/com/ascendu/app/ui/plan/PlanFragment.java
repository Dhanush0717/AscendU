package com.ascendu.app.ui.plan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.FragmentPlanBinding;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Plan Module - Complete task management & scheduling interface.
 * Coordinates Core Commitments (recurring) and One-Time Tasks.
 */
public class PlanFragment extends Fragment implements
        RecurringTaskAdapter.OnRecurringActionListener,
        OneTimeTaskAdapter.OnTaskActionListener {

    private FragmentPlanBinding binding;
    private PlannerRepository plannerRepository;
    private RecurringTaskAdapter recurringAdapter;
    private OneTimeTaskAdapter oneTimeTaskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plannerRepository = new PlannerRepository(requireActivity().getApplication());

        setupDateHeader();
        setupRecyclerViews();
        setupActionButtons();
        observePlannerData();

        // Ensure occurrences are generated and historical gaps are backfilled
        plannerRepository.ensureOccurrencesForDate(DateUtils.getTodayIso(), null);
        plannerRepository.backfillMissedOccurrences(null);
    }

    private void setupDateHeader() {
        String displayDate = DateUtils.formatDisplayDate(DateUtils.getTodayIso());
        binding.tvPlanDateSubtitle.setText("Today is " + displayDate);
    }

    private void setupRecyclerViews() {
        // Recurring Commitments
        recurringAdapter = new RecurringTaskAdapter(this);
        binding.rvRecurringCommitments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecurringCommitments.setAdapter(recurringAdapter);

        // One-Time Tasks
        oneTimeTaskAdapter = new OneTimeTaskAdapter(this);
        binding.rvOneTimeTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOneTimeTasks.setAdapter(oneTimeTaskAdapter);
    }

    private void setupActionButtons() {
        binding.btnAddAction.setOnClickListener(v -> showAddChoiceDialog());

        binding.btnAddRecurring.setOnClickListener(v -> {
            AddEditRecurringBottomSheet sheet = AddEditRecurringBottomSheet.newInstance(0);
            sheet.show(getChildFragmentManager(), "AddRecurringBottomSheet");
        });

        binding.btnAddTask.setOnClickListener(v -> {
            AddEditTaskBottomSheet sheet = AddEditTaskBottomSheet.newInstance(0);
            sheet.show(getChildFragmentManager(), "AddTaskBottomSheet");
        });
    }

    private void showAddChoiceDialog() {
        String[] options = {"Recurring Core Commitment (Habit)", "One-Time Scheduled Target"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("What would you like to plan?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        AddEditRecurringBottomSheet sheet = AddEditRecurringBottomSheet.newInstance(0);
                        sheet.show(getChildFragmentManager(), "AddRecurringBottomSheet");
                    } else {
                        AddEditTaskBottomSheet sheet = AddEditTaskBottomSheet.newInstance(0);
                        sheet.show(getChildFragmentManager(), "AddTaskBottomSheet");
                    }
                })
                .show();
    }

    private void observePlannerData() {
        // Observe recurring habits
        plannerRepository.getActiveRecurringTasks().observe(getViewLifecycleOwner(), recurringTasks -> {
            if (binding == null) return;
            recurringAdapter.submitList(recurringTasks);

            int count = recurringTasks != null ? recurringTasks.size() : 0;
            binding.tvRecurringCount.setText(count + " Active");

            if (count == 0) {
                binding.cardRecurringEmpty.setVisibility(View.VISIBLE);
                binding.rvRecurringCommitments.setVisibility(View.GONE);
            } else {
                binding.cardRecurringEmpty.setVisibility(View.GONE);
                binding.rvRecurringCommitments.setVisibility(View.VISIBLE);
            }
        });

        // Observe one-time tasks
        plannerRepository.getAllOneTimeTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (binding == null) return;
            oneTimeTaskAdapter.submitList(tasks);

            int count = tasks != null ? tasks.size() : 0;
            binding.tvTasksCount.setText(count + (count == 1 ? " Task" : " Tasks"));

            if (count == 0) {
                binding.cardTasksEmpty.setVisibility(View.VISIBLE);
                binding.rvOneTimeTasks.setVisibility(View.GONE);
            } else {
                binding.cardTasksEmpty.setVisibility(View.GONE);
                binding.rvOneTimeTasks.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- RecurringTaskAdapter.OnRecurringActionListener Callbacks ---

    @Override
    public void onEdit(RecurringTask task) {
        AddEditRecurringBottomSheet sheet = AddEditRecurringBottomSheet.newInstance(task.getId());
        sheet.show(getChildFragmentManager(), "EditRecurringBottomSheet");
    }

    @Override
    public void onToggleActive(RecurringTask task, boolean isActive) {
        plannerRepository.toggleRecurringTaskActive(task, isActive, null);
    }

    // --- OneTimeTaskAdapter.OnTaskActionListener Callbacks ---

    @Override
    public void onEdit(Task task) {
        AddEditTaskBottomSheet sheet = AddEditTaskBottomSheet.newInstance(task.getId());
        sheet.show(getChildFragmentManager(), "EditTaskBottomSheet");
    }

    @Override
    public void onToggleComplete(Task task, boolean isCompleted) {
        plannerRepository.toggleOneTimeTaskCompletion(task, isCompleted, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
