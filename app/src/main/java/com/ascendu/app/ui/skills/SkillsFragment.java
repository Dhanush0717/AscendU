package com.ascendu.app.ui.skills;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.databinding.FragmentSkillsBinding;

import java.util.List;

public class SkillsFragment extends Fragment implements SkillAdapter.OnSkillClickListener {

    private FragmentSkillsBinding binding;
    private SkillsViewModel viewModel;
    private SkillAdapter adapter;
    private LiveData<List<Skill>> currentSkillsLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSkillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SkillsViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupFab();

        observeSkills(viewModel.getAllSkills());
    }

    private void setupRecyclerView() {
        adapter = new SkillAdapter(this);
        binding.rvSkills.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSkills.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupSkillCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_skill_all) {
                observeSkills(viewModel.getAllSkills());
            } else if (checkedId == R.id.chip_skill_prog) {
                observeSkills(viewModel.getSkillsByCategory("Programming"));
            } else if (checkedId == R.id.chip_skill_cs) {
                observeSkills(viewModel.getSkillsByCategory("CS Fundamentals"));
            } else if (checkedId == R.id.chip_skill_dev) {
                observeSkills(viewModel.getSkillsByCategory("Development"));
            } else if (checkedId == R.id.chip_skill_soft) {
                observeSkills(viewModel.getSkillsByCategory("Communication"));
            }
        });
    }

    private void observeSkills(LiveData<List<Skill>> source) {
        if (currentSkillsLiveData != null) {
            currentSkillsLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentSkillsLiveData = source;
        currentSkillsLiveData.observe(getViewLifecycleOwner(), skills -> {
            adapter.submitList(skills);
            if (skills == null || skills.isEmpty()) {
                binding.layoutEmptySkills.setVisibility(View.VISIBLE);
                binding.rvSkills.setVisibility(View.GONE);
            } else {
                binding.layoutEmptySkills.setVisibility(View.GONE);
                binding.rvSkills.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFab() {
        binding.fabAddSkill.setOnClickListener(v -> showAddSkillDialog());
    }

    private void showAddSkillDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Custom Skill");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText inputName = new EditText(requireContext());
        inputName.setHint("Capability Name (e.g. System Design, Public Speaking, Deep Work)");
        layout.addView(inputName);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                Skill newSkill = new Skill(name, "Mastery", 1, 5, 0, System.currentTimeMillis());
                viewModel.insertSkill(newSkill);
                Toast.makeText(requireContext(), "Capability added!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onSkillClick(Skill skill) {
        // Dialog to log practice minutes or adjust level
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(skill.getName());
        builder.setMessage("Category: " + skill.getCategory() + "\nCurrent Level: " + skill.getCurrentLevel() + "/5\nTotal Practiced: " + skill.getTotalMinutesPracticed() + " mins");

        builder.setPositiveButton("+30 Mins Practice", (dialog, which) -> {
            viewModel.addPracticeMinutes(skill, 30);
            Toast.makeText(requireContext(), "Logged 30 mins to " + skill.getName(), Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Level Up (+1)", (dialog, which) -> {
            if (skill.getCurrentLevel() < 5) {
                skill.setCurrentLevel(skill.getCurrentLevel() + 1);
                viewModel.updateSkill(skill);
                Toast.makeText(requireContext(), "Promoted to Level " + skill.getCurrentLevel(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Skill is already at maximum level 5!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
