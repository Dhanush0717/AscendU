package com.ascendu.app.ui.today;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascendu.app.R;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.Quote;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.FragmentTodayBinding;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Today Module - Primary daily execution dashboard.
 * Coordinates real-time Room data for:
 * 1. User greeting & daily motivational quote
 * 2. Daily discipline progress & streak calculation
 * 3. Completed, pending, and missed task counters
 * 4. Current focus / next upcoming target
 * 5. Full chronological timeline with completion toggles
 */
public class TodayFragment extends Fragment implements TodayTimelineAdapter.OnTimelineActionListener {

    private FragmentTodayBinding binding;
    private PlannerRepository plannerRepository;
    private TodayTimelineAdapter timelineAdapter;

    private List<RecurringTaskOccurrence> currentOccurrences = new ArrayList<>();
    private List<Task> currentOneTimeTasks = new ArrayList<>();
    private TodayTimelineItem currentFocusItem = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTodayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plannerRepository = new PlannerRepository(requireActivity().getApplication());

        setupDateHeader();
        setupRecyclerView();
        setupFocusButtonListener();

        binding.btnNotificationSettings.setOnClickListener(v -> {
            NotificationSettingsBottomSheet sheet = NotificationSettingsBottomSheet.newInstance();
            sheet.show(getChildFragmentManager(), "NotificationSettingsBottomSheet");
        });

        // 1. Ensure occurrences are materialized for today and backfilled
        String todayIso = DateUtils.getTodayIso();
        plannerRepository.ensureOccurrencesForDate(todayIso, () -> {
            // After ensuring today's occurrences, trigger streak calculation
            recalculateStreak();
        });
        plannerRepository.backfillMissedOccurrences(null);

        // 2. Observe user profile and daily quote
        observeProfileAndQuote();

        // 3. Observe today's timeline data from Room
        observeTodayData(todayIso);
    }

    private void setupDateHeader() {
        String todayIso = DateUtils.getTodayIso();
        binding.tvTodayDate.setText(DateUtils.formatDisplayDate(todayIso));
    }

    private void setupRecyclerView() {
        timelineAdapter = new TodayTimelineAdapter(this);
        binding.rvTodayTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayTimeline.setAdapter(timelineAdapter);
    }

    private void setupFocusButtonListener() {
        binding.btnCompleteCurrent.setOnClickListener(v -> {
            if (currentFocusItem != null) {
                boolean targetState = !currentFocusItem.isCompleted();
                onToggleItemCompletion(currentFocusItem, targetState);
            }
        });
    }

    private void observeProfileAndQuote() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Observe UserProfile LiveData
        db.userProfileDao().getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (binding == null) return;
            if (profile != null && profile.getName() != null && !profile.getName().trim().isEmpty()) {
                binding.tvTodayGreeting.setText("Today, " + profile.getName().trim() + " 🚀");
            } else {
                binding.tvTodayGreeting.setText("Today's Blueprint 🚀");
            }
        });

        // Load daily motivational quote from database
        AppExecutors.getInstance().diskIO().execute(() -> {
            Quote quote = db.quoteDao().getRandomQuoteDirect();
            if (quote != null) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (binding == null) return;
                    binding.tvQuoteText.setText("\"" + quote.getQuoteText() + "\"");
                    binding.tvQuoteAuthor.setText("— " + quote.getAuthor());
                });
            }
        });
    }

    private void observeTodayData(String todayIso) {
        // Observe recurring task occurrences for today
        plannerRepository.getOccurrencesForDateChronological(todayIso).observe(getViewLifecycleOwner(), occurrences -> {
            currentOccurrences = (occurrences != null) ? occurrences : new ArrayList<>();
            rebuildTimelineAndStats();
            recalculateStreak();
        });

        // Observe one-time tasks for today
        plannerRepository.getOneTimeTasksForDateChronological(todayIso).observe(getViewLifecycleOwner(), tasks -> {
            currentOneTimeTasks = (tasks != null) ? tasks : new ArrayList<>();
            rebuildTimelineAndStats();
        });
    }

    /**
     * Rebuilds the unified chronological timeline and updates:
     * - Progress ratio & progress bar
     * - Task counts (completed, pending, missed)
     * - Current focus block
     * - Empty state container
     */
    private void rebuildTimelineAndStats() {
        if (binding == null) return;

        String currentTime24 = DateUtils.getCurrentTime24H();
        List<TodayTimelineItem> timelineList = new ArrayList<>();

        int completedCount = 0;
        int missedCount = 0;
        int pendingCount = 0;

        // 1. Process recurring occurrences
        int totalRecurringEligible = 0;
        int completedRecurringCount = 0;

        for (RecurringTaskOccurrence occ : currentOccurrences) {
            if (Task.STATUS_CANCELLED.equalsIgnoreCase(occ.getStatus())) {
                continue; // Exclude cancelled tasks from eligibility
            }
            totalRecurringEligible++;
            TodayTimelineItem item = new TodayTimelineItem(occ, currentTime24);
            timelineList.add(item);

            if (item.isCompleted()) {
                completedRecurringCount++;
                completedCount++;
            } else if (item.isMissed()) {
                missedCount++;
            } else {
                pendingCount++;
            }
        }

        // 2. Process one-time tasks scheduled for today
        for (Task task : currentOneTimeTasks) {
            if (Task.STATUS_CANCELLED.equalsIgnoreCase(task.getStatus())) {
                continue;
            }
            TodayTimelineItem item = new TodayTimelineItem(task, currentTime24);
            timelineList.add(item);

            if (item.isCompleted()) {
                completedCount++;
            } else if (item.isMissed()) {
                missedCount++;
            } else {
                pendingCount++;
            }
        }

        // 3. Sort chronologically by start time, then end time, then importance
        Collections.sort(timelineList, new Comparator<TodayTimelineItem>() {
            @Override
            public int compare(TodayTimelineItem o1, TodayTimelineItem o2) {
                String t1 = o1.getStartTime() != null ? o1.getStartTime() : "99:99";
                String t2 = o2.getStartTime() != null ? o2.getStartTime() : "99:99";
                int timeComp = t1.compareTo(t2);
                if (timeComp != 0) return timeComp;

                // If same start time, prioritize important
                if (o1.isImportant() != o2.isImportant()) {
                    return o1.isImportant() ? -1 : 1;
                }
                return Long.compare(o1.getId(), o2.getId());
            }
        });

        // 4. Update timeline adapter
        timelineAdapter.submitList(timelineList);
        int totalItems = timelineList.size();

        // Empty state visibility
        if (totalItems == 0) {
            binding.llEmptyState.setVisibility(View.VISIBLE);
            binding.rvTodayTimeline.setVisibility(View.GONE);
        } else {
            binding.llEmptyState.setVisibility(View.GONE);
            binding.rvTodayTimeline.setVisibility(View.VISIBLE);
        }

        // 5. Update Task Counts Summary
        binding.tvCompletedCount.setText(completedCount + " Done");
        binding.tvPendingCount.setText(pendingCount + " Pending");
        binding.tvMissedCount.setText(missedCount + " Missed");

        // 6. Update Daily Discipline Progress (based on eligible recurring commitments)
        if (totalRecurringEligible > 0) {
            int percentage = (int) Math.round(((double) completedRecurringCount / totalRecurringEligible) * 100);
            binding.progressToday.setProgress(percentage);
            binding.tvProgressRatio.setText(String.format(
                    "%d of %d Core Targets Completed (%d%%)",
                    completedRecurringCount,
                    totalRecurringEligible,
                    percentage
            ));
        } else {
            binding.progressToday.setProgress(0);
            binding.tvProgressRatio.setText("No recurring commitments scheduled for today");
        }

        // 7. Update Current Focus Card
        updateCurrentFocus(timelineList, currentTime24);
    }

    /**
     * Determines current active focus item or next upcoming commitment.
     */
    private void updateCurrentFocus(List<TodayTimelineItem> timelineList, String currentTime24) {
        if (timelineList.isEmpty()) {
            binding.cardCurrentFocus.setVisibility(View.GONE);
            binding.tvCurrentFocusHeader.setVisibility(View.GONE);
            currentFocusItem = null;
            return;
        }

        binding.cardCurrentFocus.setVisibility(View.VISIBLE);
        binding.tvCurrentFocusHeader.setVisibility(View.VISIBLE);

        TodayTimelineItem activeItem = null;
        TodayTimelineItem nextUpcomingItem = null;

        for (TodayTimelineItem item : timelineList) {
            if (item.isCompleted()) continue;

            String start = item.getStartTime();
            String end = item.getEndTime();

            // Currently active window?
            if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
                if (currentTime24.compareTo(start) >= 0 && currentTime24.compareTo(end) <= 0) {
                    activeItem = item;
                    break;
                }
            }

            // Next upcoming
            if (nextUpcomingItem == null && item.isUpcoming()) {
                nextUpcomingItem = item;
            }
        }

        // Priority: active currently in-flight -> next upcoming -> first incomplete -> first item
        TodayTimelineItem focusItem = activeItem != null ? activeItem : nextUpcomingItem;
        if (focusItem == null) {
            for (TodayTimelineItem item : timelineList) {
                if (!item.isCompleted()) {
                    focusItem = item;
                    break;
                }
            }
        }
        if (focusItem == null) {
            focusItem = timelineList.get(0);
        }

        currentFocusItem = focusItem;
        binding.tvCurrentTaskTitle.setText(focusItem.getTitle());

        String start12 = DateUtils.formatTime12Hour(focusItem.getStartTime());
        String end12 = DateUtils.formatTime12Hour(focusItem.getEndTime());
        String timeStr = (!start12.isEmpty() && !end12.isEmpty())
                ? (start12 + " - " + end12)
                : (!start12.isEmpty() ? ("Starts " + start12) : (!end12.isEmpty() ? ("Deadline " + end12) : "Flexible"));

        String subtitle = timeStr + (focusItem.isImportant() ? " • High Impact" : "") +
                (focusItem.getCategory() != null ? " • " + focusItem.getCategory() : "");
        binding.tvCurrentTaskTime.setText(subtitle);

        if (focusItem.isCompleted()) {
            binding.btnCompleteCurrent.setText("Reopen");
        } else {
            binding.btnCompleteCurrent.setText("Done");
        }
    }

    private void recalculateStreak() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            int streak = plannerRepository.calculateStreak(DateUtils.getTodayIso());
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (binding == null) return;
                binding.tvStreakBadge.setText("🔥 " + streak + (streak == 1 ? " Day Streak" : " Days Streak"));
            });
        });
    }

    // --- TodayTimelineAdapter.OnTimelineActionListener Callback ---

    @Override
    public void onToggleItemCompletion(TodayTimelineItem item, boolean isCompleted) {
        if (item.getItemType() == TodayTimelineItem.TYPE_RECURRING && item.getOccurrence() != null) {
            plannerRepository.toggleOccurrenceCompletion(item.getOccurrence(), isCompleted, () -> {
                recalculateStreak();
            });
        } else if (item.getItemType() == TodayTimelineItem.TYPE_ONE_TIME && item.getOneTimeTask() != null) {
            plannerRepository.toggleOneTimeTaskCompletion(item.getOneTimeTask(), isCompleted, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
