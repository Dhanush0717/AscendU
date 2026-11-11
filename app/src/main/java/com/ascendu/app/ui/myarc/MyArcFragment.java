package com.ascendu.app.ui.myarc;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ascendu.app.R;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.model.MonthlyConsistencyStats;
import com.ascendu.app.data.repository.PlannerRepository;
import com.ascendu.app.databinding.BottomSheetArcCellDetailBinding;
import com.ascendu.app.databinding.FragmentMyArcBinding;
import com.ascendu.app.util.AppExecutors;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
 * Stitch Redesign System:
 * - 3-Column Key Metric Cards (Current Streak, Record, Discipline %)
 * - Month Navigation with summary counters (Done, Missed, Total, Best Day)
 * - Synchronized Horizontal Scrolling Consistency Grid with Fixed Sticky Date Column
 * - Manual Historical Occurrence Correction Modal (Completed, Missed, Auto)
 */
public class MyArcFragment extends Fragment {

    private FragmentMyArcBinding binding;
    private PlannerRepository repository;
    private final Calendar displayCalendar = Calendar.getInstance();

    private List<RecurringTask> displayHabits = new ArrayList<>();
    private List<RecurringTaskOccurrence> monthOccurrences = new ArrayList<>();
    private final Map<String, RecurringTaskOccurrence> occurrenceMap = new HashMap<>(); // key: "taskId_dateString"
    private final List<HorizontalScrollView> rowScrollViews = new ArrayList<>();

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
        binding.tvCurrentMonthLabel.setText(monthHeaderSdf.format(displayCalendar.getTime()));

        SimpleDateFormat isoSdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
        Calendar cal = (Calendar) displayCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = isoSdf.format(cal.getTime());

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, maxDay);
        String endDate = isoSdf.format(cal.getTime());

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<RecurringTaskOccurrence> occurrences = repository.getOccurrencesBetweenDirect(startDate, endDate);
            List<RecurringTask> allTasks = repository.getAllRecurringTasksDirect();

            int currentStreak = repository.calculateCurrentStreak();
            int bestStreak = repository.calculateLongestStreak();
            MonthlyConsistencyStats monthStats = repository.calculateMonthlyStats(startDate, endDate);

            Set<Long> taskIdsInMonth = new HashSet<>();
            for (RecurringTaskOccurrence occ : occurrences) {
                taskIdsInMonth.add(occ.getRecurringTaskId());
            }

            List<RecurringTask> activeOrHistoricalHabits = new ArrayList<>();
            for (RecurringTask t : allTasks) {
                if (t.isActive() || taskIdsInMonth.contains(t.getId())) {
                    activeOrHistoricalHabits.add(t);
                }
            }

            Collections.sort(activeOrHistoricalHabits, (o1, o2) -> {
                if (o1.isImportant() != o2.isImportant()) {
                    return o1.isImportant() ? -1 : 1;
                }
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            });

            Map<String, RecurringTaskOccurrence> map = new HashMap<>();
            for (RecurringTaskOccurrence occ : occurrences) {
                map.put(occ.getRecurringTaskId() + "_" + occ.getDateString(), occ);
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

                displayHabits = activeOrHistoricalHabits;
                monthOccurrences = occurrences;
                occurrenceMap.clear();
                occurrenceMap.putAll(map);

                // Update 3 Key Metric Cards
                binding.tvArcCurrentStreak.setText("🔥 " + currentStreak);
                binding.tvArcBestStreak.setText("⚡ " + bestStreak);
                binding.tvArcMonthlyPct.setText(monthStats.getEligibleCount() > 0 ? ("🎯 " + monthStats.getConsistencyPct() + "%") : "N/A");

                // Update Month Summary Chips
                binding.tvMonthDoneCount.setText("✅ " + monthStats.getCompletedCount() + " Done");
                binding.tvMonthMissedCount.setText("⚠️ " + monthStats.getMissedCount() + " Missed");
                binding.tvMonthTotalCount.setText("📊 " + monthStats.getEligibleCount() + " Total");
                binding.tvMonthBestDay.setText("⭐ Best: " + monthStats.getBestDayOfWeek());

                // Build Matrix
                buildConsistencyGrid(startDate, endDate);
            });
        });
    }

    private void buildConsistencyGrid(String startDate, String endDate) {
        binding.llHabitColumnHeaders.removeAllViews();
        binding.llGridRowsContainer.removeAllViews();
        rowScrollViews.clear();

        if (displayHabits.isEmpty()) {
            binding.scrollHeaderHabits.setVisibility(View.GONE);
            binding.llArcEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        binding.scrollHeaderHabits.setVisibility(View.VISIBLE);
        binding.llArcEmptyState.setVisibility(View.GONE);

        int columnWidth = (int) (76 * getResources().getDisplayMetrics().density);

        // Populate Habit Column Headers
        for (RecurringTask habit : displayHabits) {
            TextView headerCell = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(4, 0, 4, 0);
            headerCell.setLayoutParams(lp);
            headerCell.setGravity(Gravity.CENTER);
            headerCell.setText(habit.getTitle());
            headerCell.setTextColor(ContextCompat.getColor(requireContext(), R.color.stitch_text_primary));
            headerCell.setTextSize(11);
            headerCell.setTypeface(Typeface.DEFAULT_BOLD);
            headerCell.setSingleLine(true);
            headerCell.setEllipsize(android.text.TextUtils.TruncateAt.END);
            binding.llHabitColumnHeaders.addView(headerCell);
        }

        // Generate Dates in descending order (Today/Latest first)
        List<String> monthDates = generateMonthDatesDescending(startDate, endDate);

        String todayIso = DateUtils.getTodayIso();
        SimpleDateFormat daySdf = new SimpleDateFormat("EEE, dd", Locale.getDefault());
        SimpleDateFormat isoFormat = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());

        for (String dateIso : monthDates) {
            LinearLayout dayRow = new LinearLayout(requireContext());
            dayRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dayRow.setOrientation(LinearLayout.HORIZONTAL);
            dayRow.setGravity(Gravity.CENTER_VERTICAL);
            dayRow.setPadding(0, 4, 0, 4);

            boolean isToday = dateIso.equals(todayIso);
            if (isToday) {
                dayRow.setBackgroundResource(R.drawable.bg_stitch_chip_inactive);
            }

            // Fixed Sticky Date Column
            TextView dateLabel = new TextView(requireContext());
            dateLabel.setLayoutParams(new LinearLayout.LayoutParams((int) (60 * getResources().getDisplayMetrics().density), ViewGroup.LayoutParams.WRAP_CONTENT));
            dateLabel.setGravity(Gravity.CENTER_VERTICAL);
            dateLabel.setTextColor(ContextCompat.getColor(requireContext(), isToday ? R.color.stitch_primary : R.color.stitch_text_secondary));
            dateLabel.setTextSize(11);
            dateLabel.setTypeface(isToday ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

            try {
                Date d = isoFormat.parse(dateIso);
                if (d != null) {
                    dateLabel.setText(daySdf.format(d));
                } else {
                    dateLabel.setText(dateIso.substring(5));
                }
            } catch (Exception e) {
                dateLabel.setText(dateIso.substring(5));
            }
            dayRow.addView(dateLabel);

            // Horizontally Scrollable Cells Container
            HorizontalScrollView cellScrollView = new HorizontalScrollView(requireContext());
            cellScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            cellScrollView.setHorizontalScrollBarEnabled(false);
            cellScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            LinearLayout cellsContainer = new LinearLayout(requireContext());
            cellsContainer.setOrientation(LinearLayout.HORIZONTAL);

            for (RecurringTask habit : displayHabits) {
                View cellView = createCellView(habit, dateIso, isToday, columnWidth);
                cellsContainer.addView(cellView);
            }

            cellScrollView.addView(cellsContainer);
            dayRow.addView(cellScrollView);

            // Sync scrolling with header and sibling rows
            rowScrollViews.add(cellScrollView);
            cellScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                binding.scrollHeaderHabits.scrollTo(scrollX, scrollY);
                for (HorizontalScrollView sv : rowScrollViews) {
                    if (sv != v) {
                        sv.scrollTo(scrollX, scrollY);
                    }
                }
            });

            binding.llGridRowsContainer.addView(dayRow);
        }

        // Header syncs with all rows
        binding.scrollHeaderHabits.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            for (HorizontalScrollView sv : rowScrollViews) {
                sv.scrollTo(scrollX, scrollY);
            }
        });
    }

    private List<String> generateMonthDatesDescending(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_ISO_DATE, Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start != null && end != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(end);
                while (!cal.getTime().before(start)) {
                    dates.add(sdf.format(cal.getTime()));
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
        } catch (Exception ignored) {
        }
        return dates;
    }

    private View createCellView(RecurringTask habit, String dateIso, boolean isToday, int columnWidth) {
        LinearLayout container = new LinearLayout(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(columnWidth, (int) (36 * getResources().getDisplayMetrics().density));
        lp.setMargins(4, 2, 4, 2);
        container.setLayoutParams(lp);
        container.setGravity(Gravity.CENTER);

        View cellView = new View(requireContext());
        int cellSize = (int) (28 * getResources().getDisplayMetrics().density);
        cellView.setLayoutParams(new LinearLayout.LayoutParams(cellSize, cellSize));

        String key = habit.getId() + "_" + dateIso;
        RecurringTaskOccurrence occ = occurrenceMap.get(key);

        int cellBgRes;
        String statusDescription;

        if (occ != null) {
            if (Task.STATUS_COMPLETED.equalsIgnoreCase(occ.getStatus())) {
                cellBgRes = R.drawable.bg_stitch_cell_completed;
                statusDescription = "Completed";
            } else if (Task.STATUS_MISSED.equalsIgnoreCase(occ.getStatus())) {
                cellBgRes = R.drawable.bg_stitch_cell_missed;
                statusDescription = "Missed";
            } else if ("SKIPPED".equalsIgnoreCase(occ.getStatus())) {
                cellBgRes = R.drawable.bg_stitch_cell_rest;
                statusDescription = "Rest Day";
            } else {
                cellBgRes = isToday ? R.drawable.bg_stitch_badge_neutral : R.drawable.bg_stitch_cell_future;
                statusDescription = isToday ? "Pending Today" : "Scheduled";
            }
        } else {
            cellBgRes = R.drawable.bg_stitch_cell_rest;
            statusDescription = "Rest / Not Scheduled";
        }

        cellView.setBackgroundResource(cellBgRes);

        final RecurringTaskOccurrence finalOcc = occ;
        final String finalDesc = statusDescription;
        final int finalBg = cellBgRes;

        container.setOnClickListener(v -> showCellDetailBottomSheet(habit, dateIso, finalOcc, finalDesc, finalBg));
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

        // Manual Correction Actions
        sheetBinding.btnMarkCompleted.setOnClickListener(v -> {
            if (occ != null) {
                repository.toggleOccurrenceCompletion(occ, true, this::loadMonthData);
            }
            dialog.dismiss();
        });

        sheetBinding.btnMarkMissed.setOnClickListener(v -> {
            if (occ != null) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    AppDatabase.getInstance(requireContext())
                            .recurringTaskOccurrenceDao()
                            .updateStatus(occ.getId(), Task.STATUS_MISSED, 0);
                    AppExecutors.getInstance().mainThread().execute(this::loadMonthData);
                });
            }
            dialog.dismiss();
        });

        sheetBinding.btnResetAutomatic.setOnClickListener(v -> {
            if (occ != null) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    AppDatabase.getInstance(requireContext())
                            .recurringTaskOccurrenceDao()
                            .updateStatus(occ.getId(), Task.STATUS_UPCOMING, 0);
                    AppExecutors.getInstance().mainThread().execute(this::loadMonthData);
                });
            }
            dialog.dismiss();
        });

        sheetBinding.btnDetailClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
