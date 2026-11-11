package com.ascendu.app.ui.plan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.databinding.ItemRecurringTaskBinding;
import com.ascendu.app.util.DateUtils;

/**
 * RecyclerView Adapter for Recurring Core Commitments.
 */
public class RecurringTaskAdapter extends ListAdapter<RecurringTask, RecurringTaskAdapter.RecurringViewHolder> {

    private final OnRecurringActionListener listener;

    public interface OnRecurringActionListener {
        void onEdit(RecurringTask task);
        void onToggleActive(RecurringTask task, boolean isActive);
    }

    public RecurringTaskAdapter(OnRecurringActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RecurringTask> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecurringTask oldItem, @NonNull RecurringTask newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecurringTask oldItem, @NonNull RecurringTask newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isActive() == newItem.isActive() &&
                    oldItem.isImportant() == newItem.isImportant() &&
                    oldItem.getDaysOfWeekBitmask() == newItem.getDaysOfWeekBitmask() &&
                    oldItem.getStartTime().equals(newItem.getStartTime()) &&
                    oldItem.getEndTime().equals(newItem.getEndTime());
        }
    };

    @NonNull
    @Override
    public RecurringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecurringTaskBinding binding = ItemRecurringTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecurringViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RecurringViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecurringTaskBinding binding;

        RecurringViewHolder(ItemRecurringTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RecurringTask task) {
            binding.tvRecurringTitle.setText(task.getTitle());

            // Build schedule line: Weekdays • Time Range
            String days = DateUtils.getBitmaskDaysLabel(task.getDaysOfWeekBitmask());
            String timeWindow = "";
            if (task.getStartTime() != null && !task.getStartTime().isEmpty()) {
                timeWindow = " • " + DateUtils.format12HourTime(task.getStartTime()) +
                        " - " + DateUtils.format12HourTime(task.getEndTime());
            }
            binding.tvRecurringTimeAndDays.setText(days + timeWindow);

            // Category badge
            String cat = task.getCategory();
            binding.tvRecurringCategoryBadge.setText(cat != null && !cat.isEmpty() ? cat : "General");

            // Importance marker
            if (task.isImportant()) {
                binding.tvRecurringImportantBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvRecurringImportantBadge.setVisibility(View.GONE);
            }

            // Active toggle switch
            binding.switchRecurringActive.setOnCheckedChangeListener(null);
            binding.switchRecurringActive.setChecked(task.isActive());
            binding.switchRecurringActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActive(task, isChecked);
                }
            });

            // Edit button & card click
            binding.btnEditRecurring.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });

            // Soft dimming for archived/inactive tasks
            binding.getRoot().setAlpha(task.isActive() ? 1.0f : 0.6f);
        }
    }
}
