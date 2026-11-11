package com.ascendu.app.ui.tasks;

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
import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.databinding.DialogAddRoutineBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddRoutineBottomSheet extends BottomSheetDialogFragment {

    private DialogAddRoutineBinding binding;
    private TasksViewModel viewModel;

    public static AddRoutineBottomSheet newInstance() {
        return new AddRoutineBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        binding.sliderRoutineMinutes.addOnChangeListener((slider, value, fromUser) -> {
            binding.tvRoutineMinutesLabel.setText("Target Commitment: " + (int) value + " mins");
        });

        binding.btnSaveRoutine.setOnClickListener(v -> saveRoutine());
    }

    private void saveRoutine() {
        String title = binding.etRoutineTitle.getText() != null
                ? binding.etRoutineTitle.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(title)) {
            binding.tilRoutineTitle.setError("Please enter a routine title");
            return;
        }
        binding.tilRoutineTitle.setError(null);

        String category = "Focus";
        int checkedChipId = binding.chipGroupRoutineCategory.getCheckedChipId();
        if (checkedChipId == R.id.chip_cat_mastery) {
            category = "Mastery";
        } else if (checkedChipId == R.id.chip_cat_discipline) {
            category = "Discipline";
        } else if (checkedChipId == R.id.chip_cat_reflection) {
            category = "Reflection";
        }

        int targetMinutes = (int) binding.sliderRoutineMinutes.getValue();

        DailyRoutine routine = new DailyRoutine(
                title,
                category,
                targetMinutes,
                true,
                1,
                1,
                System.currentTimeMillis()
        );

        viewModel.insertRoutine(routine);
        Toast.makeText(requireContext(), "Added to Daily Blueprint!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
