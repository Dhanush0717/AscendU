package com.ascendu.app.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.repository.TaskRepository;
import com.ascendu.app.databinding.DialogAddTaskBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PLANNED_DATE = "arg_planned_date";
    private DialogAddTaskBinding binding;
    private TasksViewModel viewModel;
    private long selectedPlannedDate = System.currentTimeMillis();

    public static AddTaskBottomSheet newInstance(long plannedDate) {
        AddTaskBottomSheet sheet = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_PLANNED_DATE, plannedDate);
        sheet.setArguments(args);
        return sheet;
    }

    public static AddTaskBottomSheet newInstance() {
        return newInstance(System.currentTimeMillis());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedPlannedDate = getArguments().getLong(ARG_PLANNED_DATE, System.currentTimeMillis());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        updateDateButtonText();

        binding.btnPickTaskDate.setOnClickListener(v -> showDatePicker());
        binding.btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        binding.btnPickTaskDate.setText("📅 Scheduled: " + sdf.format(new Date(selectedPlannedDate)));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedPlannedDate);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedPlannedDate = picked.getTimeInMillis();
                    updateDateButtonText();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveTask() {
        String title = binding.etTaskTitle.getText() != null ? binding.etTaskTitle.getText().toString().trim() : "";
        String desc = binding.etTaskDesc.getText() != null ? binding.etTaskDesc.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            binding.tilTaskTitle.setError("Please enter a task title");
            return;
        }
        binding.tilTaskTitle.setError(null);

        // Determine category
        String category = "Technical Mastery";
        int checkedChipId = binding.chipGroupTaskCategory.getCheckedChipId();
        if (checkedChipId == R.id.chip_cat_career) {
            category = "Career & Work";
        } else if (checkedChipId == R.id.chip_cat_acad) {
            category = "Deep Study";
        } else if (checkedChipId == R.id.chip_cat_comm) {
            category = "Communication";
        }

        // Determine priority
        int priority = 2; // Medium default
        int checkedPriorityId = binding.togglePriority.getCheckedButtonId();
        if (checkedPriorityId == R.id.btn_priority_low) {
            priority = 1;
        } else if (checkedPriorityId == R.id.btn_priority_high) {
            priority = 3;
        }

        // Ensure planned date is set to start of day
        long[] bounds = TaskRepository.getDayBounds(selectedPlannedDate);
        long plannedDate = bounds[0];

        Task newTask = new Task(
                title,
                desc,
                category,
                priority,
                plannedDate,
                false,
                0,
                0,
                System.currentTimeMillis(),
                0
        );

        viewModel.insertTask(newTask);
        Toast.makeText(requireContext(), "Target scheduled successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
