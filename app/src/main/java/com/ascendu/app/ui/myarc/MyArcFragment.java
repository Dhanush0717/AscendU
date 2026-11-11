package com.ascendu.app.ui.myarc;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.model.MonthlyConsistencyStats;
import com.ascendu.app.data.model.TaskMonthlyStats;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.BottomSheetArcCellDetailBinding;
import com.ascendu.app.databinding.FragmentMyArcBinding;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * My Arc Module - Multi-Task Monthly Consistency Grid & Historical Discipline Analytics.
 */
public class MyArcFragment extends Fragment {

    private FragmentMyArcBinding binding;
    private PlannerRepository repository;
    private final Calendar displayCalendar = Calendar.getInstance();

    private List<RecurringTask> displayHabits = new ArrayList<>();
    private List<RecurringTaskOccurrence> monthOccurrences = new ArrayList<>();
    private final Map<String, RecurringTaskOccurrence> occurrenceMap = new HashMap<>(); // key: "taskId_dateString"

    private long selectedHabitId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyArcBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new PlannerRepository(requireActivity().getApplication());

        // Default to current calendar month
        displayCalendar.set(Calendar.DAY_OF_MONTH, 1);

        initListeners();
        loadMonthData();
    }

    private void initListeners() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            displayCalendar.add(Calendar.MONTH, -1);
            loadMonthData();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            displayCalendar.add(Calendar.MONTH, 1);
            loadMonthData();
        });
    }

    private void loadMonthData() {
        SimpleDateFormat monthHeaderSdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.tvArcMonthLabel.setText(monthHeaderSdf.format(displayCalendar.getTime()));

        SimpleDateFormat isoSdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
        Calendar cal = (Calendar) displayCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = isoSdf.format(cal.getTime());

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, maxDay);
        String endDate = isoSdf.format(cal.getTime());

        AppExecutors.getInstance().diskIO().execute(() -> {
            // Fetch occurrences for this month
            List<RecurringTaskOccurrence> occurrences = repository.getOccurrencesBetweenDirect(startDate, endDate);
            List<RecurringTask> allTasks = repository.getAllRecurringTasksDirect();

            // Compute streaks
            int currentStreak = repository.calculateCurrentStreak();
            int bestStreak = repository.calculateLongestStreak();

            // Compute monthly stats
            MonthlyConsistencyStats monthStats = repository.calculateMonthlyStats(startDate, endDate);

            // Filter relevant habits for this month:
            // 1. Task is active, OR
            // 2. Task has occurrences in this month (preserves historical archived commitments)
            Set<Long> taskIdsInMonth = new HashSet<>();
            for (RecurringTaskOccurrence occ : occurrences) {
                taskIdsInMonth.add(occ.getRecurringTaskId());
            }

            List<RecurringTask> relevantHabits = new ArrayList<>();
            for (RecurringTask task : allTasks) {
                if (task.isActive() || taskIdsInMonth.contains(task.getId())) {
                    relevantHabits.add(task);
                }
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

                monthOccurrences = occurrences;
                displayHabits = relevantHabits;

                // Build fast lookup map
                occurrenceMap.clear();
                for (RecurringTaskOccurrence occ : occurrences) {
                    occurrenceMap.put(occ.getRecurringTaskId() + "_" + occ.getDateString(), occ);
                }

                // Update Key Metrics
                binding.tvArcCurrentStreak.setText("🔥 " + currentStreak);
                binding.tvArcBestStreak.setText("⚡ " + bestStreak);
                binding.tvArcConsistencyPct.setText(monthStats.getEligibleCount() > 0 ? (monthStats.getConsistencyPct() + "%") : "N/A");

                // Update Monthly Counters
                binding.tvMonthlyCompleted.setText("✓ " + monthStats.getCompletedCount() + " Done");
                binding.tvMonthlyMissed.setText("✗ " + monthStats.getMissedCount() + " Missed");
                binding.tvMonthlyEligible.setText("📋 " + monthStats.getEligibleCount() + " Scheduled");
                binding.tvMonthlyBestDay.setText("⭐ Best: " + monthStats.getBestDayOfWeek());

                // Build Grid
                renderConsistencyGrid(startDate, endDate, maxDay);

                // Populate Habit Selector Chips & Task Stats
                setupHabitSelectorChips(startDate, endDate);
            });
        });
    }

    private void renderConsistencyGrid(String startDate, String endDate, int maxDay) {
        binding.tableConsistencyGrid.removeAllViews();

        if (displayHabits.isEmpty()) {
            binding.gridHorizontalScroll.setVisibility(View.GONE);
            binding.layoutGridEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.gridHorizontalScroll.setVisibility(View.VISIBLE);
        binding.layoutGridEmpty.setVisibility(View.GONE);

        // Density scale for dp to px
        float density = getResources().getDisplayMetrics().density;
        int cellPadding = (int) (6 * density);
        int dateColWidth = (int) (70 * density);
        int habitColWidth = (int) (80 * density);
        int cellSize = (int) (28 * density);

        // Header Row (Top)
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setPadding(0, 0, 0, (int) (8 * density));

        // Corner cell: "Date"
        TextView tvDateHeader = new TextView(requireContext());
        tvDateHeader.setText("Date");
        tvDateHeader.setTextSize(12);
        tvDateHeader.setTypeface(null, Typeface.BOLD);
        tvDateHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.neu_text_secondary));
        tvDateHeader.setGravity(Gravity.CENTER);
        TableRow.LayoutParams dateParams = new TableRow.LayoutParams(dateColWidth, TableRow.LayoutParams.WRAP_CONTENT);
        headerRow.addView(tvDateHeader, dateParams);

        // Column for each habit
        for (RecurringTask habit : displayHabits) {
            TextView tvHabitHeader = new TextView(requireContext());
            tvHabitHeader.setText(habit.getTitle());
            tvHabitHeader.setTextSize(11);
            tvHabitHeader.setTypeface(null, Typeface.BOLD);
            tvHabitHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.neu_text_primary));
            tvHabitHeader.setGravity(Gravity.CENTER);
            tvHabitHeader.setMaxLines(2);
            tvHabitHeader.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvHabitHeader.setPadding((int) (4 * density), 0, (int) (4 * density), 0);

            TableRow.LayoutParams habitParams = new TableRow.LayoutParams(habitColWidth, TableRow.LayoutParams.WRAP_CONTENT);
            headerRow.addView(tvHabitHeader, habitParams);
        }
        binding.tableConsistencyGrid.addView(headerRow);

        // Calendar Date Rows
        SimpleDateFormat isoSdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
        SimpleDateFormat rowDateDisplaySdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        String todayIso = DateUtils.getTodayDateString();
        String currentTime = DateUtils.getCurrentTime24H();

        Calendar rowCal = (Calendar) displayCalendar.clone();

        for (int day = 1; day <= maxDay; day++) {
            rowCal.set(Calendar.DAY_OF_MONTH, day);
            String rowDateIso = isoSdf.format(rowCal.getTime());
            String rowDateDisplay = rowDateDisplaySdf.format(rowCal.getTime());
            boolean isToday = rowDateIso.equals(todayIso);

            TableRow dayRow = new TableRow(requireContext());
            dayRow.setGravity(Gravity.CENTER_VERTICAL);
            dayRow.setPadding(0, (int) (4 * density), 0, (int) (4 * density));

            if (isToday) {
                dayRow.setBackgroundResource(R.drawable.bg_row_today_highlight);
            }

            // Date label
            TextView tvDate = new TextView(requireContext());
            tvDate.setText(rowDateDisplay);
            tvDate.setTextSize(11);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), isToday ? R.color.neu_primary_violet : R.color.neu_text_secondary));
            if (isToday) {
                tvDate.setTypeface(null, Typeface.BOLD);
            }
            tvDate.setGravity(Gravity.CENTER);
            TableRow.LayoutParams rowDateParams = new TableRow.LayoutParams(dateColWidth, TableRow.LayoutParams.MATCH_PARENT);
            dayRow.addView(tvDate, rowDateParams);

            // Cells for each habit
            for (RecurringTask habit : displayHabits) {
                View cellContainer = createGridCell(habit, rowDateIso, isToday, todayIso, currentTime, cellSize, density);
                TableRow.LayoutParams cellParams = new TableRow.LayoutParams(habitColWidth, TableRow.LayoutParams.WRAP_CONTENT);
                cellParams.gravity = Gravity.CENTER;
                dayRow.addView(cellContainer, cellParams);
            }

            binding.tableConsistencyGrid.addView(dayRow);
        }
    }

    private View createGridCell(RecurringTask habit, String dateIso, boolean isToday, String todayIso, String currentTime, int cellSize, float density) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setGravity(Gravity.CENTER);
        container.setPadding(0, (int) (2 * density), 0, (int) (2 * density));

        View cellView = new View(requireContext());
        LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(cellSize, cellSize);
        cellView.setLayoutParams(cellParams);

        // Check occurrence status in Room
        String key = habit.getId() + "_" + dateIso;
        RecurringTaskOccurrence occ = occurrenceMap.get(key);

        // Historical creation protection: check if date is before task creation
        String taskCreatedDateIso = DateUtils.formatDateString(habit.getCreatedAt());
        boolean isBeforeCreation = dateIso.compareTo(taskCreatedDateIso) < 0;

        boolean isScheduledDay = DateUtils.isDayScheduled(habit.getDaysOfWeekBitmask(), dateIso);
        int cellBgRes;
        String statusTooltip;

        if (occ != null) {
            if (Task.STATUS_COMPLETED.equals(occ.getStatus())) {
                cellBgRes = R.drawable.bg_cell_completed; // Green
                statusTooltip = "Completed";
            } else if (Task.STATUS_CANCELLED.equals(occ.getStatus())) {
                cellBgRes = R.drawable.bg_cell_neutral; // Cancelled
                statusTooltip = "Cancelled";
            } else {
                // Status is UPCOMING / PENDING / MISSED
                if (dateIso.compareTo(todayIso) > 0) {
                    cellBgRes = R.drawable.bg_cell_future; // Grey (Future)
                    statusTooltip = "Future (Due)";
                } else if (dateIso.compareTo(todayIso) < 0) {
                    cellBgRes = R.drawable.bg_cell_missed; // Red (Past date missed)
                    statusTooltip = "Missed";
                } else {
                    // Today
                    boolean timePassed = DateUtils.isTimePast(habit.getEndTime(), currentTime);
                    if (timePassed) {
                        cellBgRes = R.drawable.bg_cell_missed; // Red (Deadline passed)
                        statusTooltip = "Missed (Deadline passed)";
                    } else {
                        cellBgRes = R.drawable.bg_cell_future; // Pending today
                        statusTooltip = "Pending Today";
                    }
                }
            }
        } else {
            // No occurrence record in database
            if (isBeforeCreation || !isScheduledDay) {
                cellBgRes = R.drawable.bg_cell_neutral; // Not scheduled / Rest
                statusTooltip = "Rest / Not Scheduled";
            } else if (dateIso.compareTo(todayIso) > 0) {
                cellBgRes = R.drawable.bg_cell_future; // Future scheduled
                statusTooltip = "Scheduled (Future)";
            } else if (dateIso.compareTo(todayIso) < 0) {
                cellBgRes = R.drawable.bg_cell_missed; // Unbackfilled past scheduled day
                statusTooltip = "Missed";
            } else {
                cellBgRes = R.drawable.bg_cell_future;
                statusTooltip = "Today";
            }
        }

        cellView.setBackgroundResource(cellBgRes);

        // Click interaction: show cell history details
        final RecurringTaskOccurrence finalOcc = occ;
        final String finalTooltip = statusTooltip;
        final int finalBgRes = cellBgRes;

        container.setOnClickListener(v -> showCellDetailBottomSheet(habit, dateIso, finalOcc, finalTooltip, finalBgRes));

        container.addView(cellView);
        return container;
    }

    private void showCellDetailBottomSheet(RecurringTask habit, String dateIso, @Nullable RecurringTaskOccurrence occ, String statusDescription, int bgRes) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetArcCellDetailBinding sheetBinding = BottomSheetArcCellDetailBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvDetailDateLabel.setText(DateUtils.formatDisplayDate(dateIso));
        sheetBinding.tvDetailTaskTitle.setText(habit.getTitle());
        sheetBinding.tvDetailScheduleTime.setText("Scheduled: " + DateUtils.format12HourTime(habit.getStartTime()) + " - " + DateUtils.format12HourTime(habit.getEndTime()));

        sheetBinding.viewDetailStatusDot.setBackgroundResource(bgRes);
        sheetBinding.tvDetailStatusText.setText("Status: " + statusDescription);

        if (occ != null && occ.getCompletedAt() > 0) {
            SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            sheetBinding.tvDetailCompletedAt.setText("at " + timeSdf.format(new Date(occ.getCompletedAt())));
            sheetBinding.tvDetailCompletedAt.setVisibility(View.VISIBLE);
        } else {
            sheetBinding.tvDetailCompletedAt.setVisibility(View.GONE);
        }

        sheetBinding.btnDetailClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupHabitSelectorChips(String startDate, String endDate) {
        binding.chipGroupHabits.removeAllViews();

        if (displayHabits.isEmpty()) {
            binding.cardHabitStats.setVisibility(View.GONE);
            return;
        }

        binding.cardHabitStats.setVisibility(View.VISIBLE);

        if (selectedHabitId == -1 || !containsHabitId(displayHabits, selectedHabitId)) {
            selectedHabitId = displayHabits.get(0).getId();
        }

        for (RecurringTask habit : displayHabits) {
            Chip chip = new Chip(requireContext());
            chip.setText(habit.getTitle());
            chip.setCheckable(true);
            chip.setChecked(habit.getId() == selectedHabitId);
            chip.setChipBackgroundColorResource(R.color.neu_surface);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.neu_text_primary));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedHabitId = habit.getId();
                    loadHabitStats(habit, startDate, endDate);
                }
            });

            binding.chipGroupHabits.addView(chip);
        }

        // Load initially selected habit
        RecurringTask initialHabit = getHabitById(displayHabits, selectedHabitId);
        if (initialHabit != null) {
            loadHabitStats(initialHabit, startDate, endDate);
        }
    }

    private void loadHabitStats(RecurringTask habit, String startDate, String endDate) {
        binding.tvHabitDetailTitle.setText(habit.getTitle());
        String timeSchedule = DateUtils.format12HourTime(habit.getStartTime()) + " - " + DateUtils.format12HourTime(habit.getEndTime()) + " • " + DateUtils.getBitmaskDaysLabel(habit.getDaysOfWeekBitmask());
        binding.tvHabitDetailSchedule.setText(timeSchedule);

        AppExecutors.getInstance().diskIO().execute(() -> {
            TaskMonthlyStats stats = repository.calculateTaskStats(habit.getId(), startDate, endDate);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                binding.tvHabitDetailPct.setText(stats.getEligibleCount() > 0 ? (stats.getConsistencyPct() + "%") : "N/A");
                binding.tvHabitDetailCompleted.setText(String.valueOf(stats.getCompletedCount()));
                binding.tvHabitDetailMissed.setText(String.valueOf(stats.getMissedCount()));
                binding.tvHabitDetailBestDay.setText(stats.getBestDayOfWeek());
            });
        });
    }

    private boolean containsHabitId(List<RecurringTask> habits, long id) {
        for (RecurringTask h : habits) {
            if (h.getId() == id) return true;
        }
        return false;
    }

    private RecurringTask getHabitById(List<RecurringTask> habits, long id) {
        for (RecurringTask h : habits) {
            if (h.getId() == id) return h;
        }
        return habits.isEmpty() ? null : habits.get(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
