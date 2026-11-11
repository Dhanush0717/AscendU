package com.ascendu.app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.databinding.FragmentHomeBinding;
import com.ascendu.app.ui.tasks.AddTaskBottomSheet;
import com.ascendu.app.ui.tasks.RoutineAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Universal High-Performance Home Dashboard.
 * Designed with modern lavender SaaS aesthetics adapted for Android mobile.
 */
public class HomeFragment extends Fragment implements RoutineAdapter.OnRoutineClickListener {

    private FragmentHomeBinding binding;
    private DashboardViewModel viewModel;
    private RoutineAdapter routineAdapter;

    private int totalPlannedTasks = 0;
    private int completedTasks = 0;
    private int totalRoutines = 0;
    private int completedRoutines = 0;
    private String userName = "Ascender";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupDateHeader();
        setupRoutinesRecyclerView();
        setupObservers();
        setupClickListeners();
    }

    private void setupDateHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        binding.tvDate.setText(sdf.format(new Date()));
    }

    private void setupRoutinesRecyclerView() {
        routineAdapter = new RoutineAdapter(this);
        binding.rvHomeRoutines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHomeRoutines.setAdapter(routineAdapter);
    }

    private void setupObservers() {
        // Observe user profile
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                if (profile.getName() != null && !profile.getName().isEmpty()) {
                    userName = profile.getName().trim();
                    binding.tvGreeting.setText("Hi, " + userName + "! 👋");
                    binding.tvAvatarInitials.setText(extractInitials(userName));
                }
                binding.tvUserDomain.setText(profile.getGrowthDomain());
                updateHeroCard();
            }
        });

        // Observe daily routines & completions
        viewModel.getActiveRoutines().observe(getViewLifecycleOwner(), routines -> {
            totalRoutines = (routines != null) ? routines.size() : 0;
            routineAdapter.submitList(routines);
            recalculateDailyProgressAndAccountability();
        });

        viewModel.getTodayRoutineCompletions().observe(getViewLifecycleOwner(), completions -> {
            Set<Long> completedIds = new HashSet<>();
            if (completions != null) {
                for (DailyRoutineCompletion c : completions) {
                    if (c.isCompleted()) {
                        completedIds.add(c.getRoutineId());
                    }
                }
            }
            completedRoutines = completedIds.size();
            routineAdapter.setCompletedRoutineIds(completedIds);
            binding.tvRoutineProgressLabel.setText(completedRoutines + "/" + totalRoutines + " Done");
            recalculateDailyProgressAndAccountability();
        });

        // Observe planned tasks
        viewModel.getTodayPlannedCount().observe(getViewLifecycleOwner(), plannedCount -> {
            totalPlannedTasks = (plannedCount != null) ? plannedCount : 0;
            recalculateDailyProgressAndAccountability();
        });

        viewModel.getTodayCompletedCount().observe(getViewLifecycleOwner(), completedCount -> {
            completedTasks = (completedCount != null) ? completedCount : 0;
            recalculateDailyProgressAndAccountability();
        });

        // Observe focus time
        viewModel.getTodayStudyMinutes().observe(getViewLifecycleOwner(), minutes -> {
            int totalMins = (minutes != null) ? minutes : 0;
            binding.tvStatFocusTime.setText(totalMins + " mins");
            binding.tvStatFocusBadge.setText(totalMins > 0 ? "Deep Work Active" : "Start Session");
        });

        // Observe recommended focus skill
        viewModel.getFocusSkill().observe(getViewLifecycleOwner(), skill -> {
            if (skill != null) {
                binding.tvFocusSkillName.setText(skill.getName());
                binding.tvFocusSkillDetail.setText("Category: " + skill.getCategory() + " • Current: Level " + skill.getCurrentLevel() + "/5");
            }
        });
    }

    private String extractInitials(String name) {
        if (name == null || name.isEmpty()) return "AU";
        String[] parts = name.split("\\s+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.getDefault());
        } else {
            return ("" + name.charAt(0)).toUpperCase(Locale.getDefault());
        }
    }

    private void updateHeroCard() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeGreeting;
        if (currentHour < 12) {
            timeGreeting = "Good Morning";
        } else if (currentHour < 17) {
            timeGreeting = "Good Afternoon";
        } else {
            timeGreeting = "Good Evening";
        }

        binding.tvHeroTitle.setText(timeGreeting + ", " + userName + "! 🚀");
        binding.tvHeroSubtitle.setText("You have " + totalRoutines + " daily non-negotiables and " + totalPlannedTasks + " scheduled targets today.");
    }

    private void recalculateDailyProgressAndAccountability() {
        int totalCommitments = totalRoutines + totalPlannedTasks;
        int totalCompleted = completedRoutines + completedTasks;

        int percentage = 0;
        int dailyScore = 0;
        if (totalCommitments > 0) {
            percentage = (int) (((float) totalCompleted / (float) totalCommitments) * 100);
            dailyScore = Math.min(100, percentage);
        }

        // Update 2x2 Metric Cards (Directly from reference screenshot)
        binding.tvStatTasks.setText(totalCompleted + " / " + totalCommitments);
        binding.tvStatTasksBadge.setText(percentage + "% completed");

        binding.tvStatScore.setText(dailyScore + " pts");
        binding.tvStatScoreBadge.setText(percentage >= 80 ? "🔥 High Output" : "Discipline Rating");

        updateHeroCard();

        // EVALUATE ACCOUNTABILITY & FALLING-BEHIND STATUS
        int pending = Math.max(0, totalCommitments - totalCompleted);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (totalCommitments == 0) {
            binding.tvStatusBadge.setText("⚡ Daily Blueprint Ready");
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_purple));
            binding.cardAccountability.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
            binding.tvAccountabilityMessage.setText("Plan your commitments for today to build momentum and maintain your streak.");
            binding.btnAccountabilityAction.setVisibility(View.GONE);
        } else if (pending == 0) {
            binding.tvStatusBadge.setText("🌟 Unstoppable: Daily Blueprint Conquered");
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
            binding.cardAccountability.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.status_success));
            binding.tvAccountabilityMessage.setText("All non-negotiables & scheduled targets completed! Outstanding execution today.");
            binding.btnAccountabilityAction.setVisibility(View.GONE);
        } else if (currentHour >= 17) { // 5:00 PM onwards - Evening falling-behind risk!
            binding.tvStatusBadge.setText("🔴 Accountability Alert: Falling Behind");
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_error));
            binding.cardAccountability.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.status_error));
            binding.tvAccountabilityMessage.setText("⚠️ Streak at risk! You have " + pending + " pending commitment(s). A quick 15-minute push before midnight protects your streak.");
            binding.btnAccountabilityAction.setVisibility(View.VISIBLE);
        } else if (currentHour >= 12) { // Afternoon
            binding.tvStatusBadge.setText("🟡 Momentum Required: In Motion");
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning));
            binding.cardAccountability.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.status_warning));
            binding.tvAccountabilityMessage.setText(pending + " commitment(s) remaining for today. Schedule a dedicated afternoon focus session.");
            binding.btnAccountabilityAction.setVisibility(View.GONE);
        } else { // Morning
            binding.tvStatusBadge.setText("🟢 Discipline Status: On Track");
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_purple));
            binding.cardAccountability.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
            binding.tvAccountabilityMessage.setText("Morning momentum initiated. Focus on your highest-impact non-negotiable first.");
            binding.btnAccountabilityAction.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.btnHeroAction.setOnClickListener(v -> {
            StartSessionBottomSheet.newInstance().show(getParentFragmentManager(), "StartSessionBottomSheet");
        });

        binding.btnAccountabilityAction.setOnClickListener(v -> {
            StartSessionBottomSheet.newInstance().show(getParentFragmentManager(), "StartSessionBottomSheet");
        });
    }

    @Override
    public void onToggleComplete(DailyRoutine routine, boolean isCompleted) {
        viewModel.toggleTodayRoutine(routine, isCompleted);
    }

    @Override
    public void onDeleteClick(DailyRoutine routine) {
        // Routines managed from Blueprint tab
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
