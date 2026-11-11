package com.ascendu.app.ui.today;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ascendu.app.R;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.databinding.ItemTodayTimelineBinding;
import com.ascendu.app.util.DateUtils;

/**
 * RecyclerView Adapter for Today's chronological timeline.
 * Handles both recurring commitments and one-time tasks.
 */
public class TodayTimelineAdapter extends ListAdapter<TodayTimelineItem, TodayTimelineAdapter.TimelineViewHolder> {

    public interface OnTimelineActionListener {
        void onToggleItemCompletion(TodayTimelineItem item, boolean isCompleted);
    }

    private final OnTimelineActionListener listener;

    public TodayTimelineAdapter(OnTimelineActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TodayTimelineItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TodayTimelineItem oldItem, @NonNull TodayTimelineItem newItem) {
            return oldItem.getItemType() == newItem.getItemType() && oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TodayTimelineItem oldItem, @NonNull TodayTimelineItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    oldItem.isImportant() == newItem.isImportant() &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    String.valueOf(oldItem.getStartTime()).equals(String.valueOf(newItem.getStartTime())) &&
                    String.valueOf(oldItem.getEndTime()).equals(String.valueOf(newItem.getEndTime()));
        }
    };

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayTimelineBinding binding = ItemTodayTimelineBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TimelineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TimelineViewHolder extends RecyclerView.ViewHolder {
        private final ItemTodayTimelineBinding binding;

        TimelineViewHolder(ItemTodayTimelineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TodayTimelineItem item) {
            binding.tvTimelineTitle.setText(item.getTitle());

            // Format time window
            String start12 = DateUtils.formatTime12Hour(item.getStartTime());
            String end12 = DateUtils.formatTime12Hour(item.getEndTime());
            if (!start12.isEmpty() && !end12.isEmpty()) {
                binding.tvTimelineTime.setText(start12 + " - " + end12);
            } else if (!start12.isEmpty()) {
                binding.tvTimelineTime.setText("Starts: " + start12);
            } else if (!end12.isEmpty()) {
                binding.tvTimelineTime.setText("Deadline: " + end12);
            } else {
                binding.tvTimelineTime.setText("Flexible timing");
            }

            // Importance badge
            if (item.isImportant()) {
                binding.tvTimelineImportanceBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvTimelineImportanceBadge.setVisibility(View.GONE);
            }

            // Type badge
            if (item.getItemType() == TodayTimelineItem.TYPE_RECURRING) {
                binding.tvTimelineTypeBadge.setText("Habit");
            } else {
                binding.tvTimelineTypeBadge.setText("Task");
            }

            // Category badge
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                binding.tvTimelineCategoryBadge.setText(item.getCategory());
                binding.tvTimelineCategoryBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvTimelineCategoryBadge.setVisibility(View.GONE);
            }

            // Prevent unwanted triggers during bind
            binding.cbTimelineComplete.setOnCheckedChangeListener(null);
            binding.cbTimelineComplete.setChecked(item.isCompleted());

            // Visual Status styling
            if (item.isCompleted()) {
                binding.tvTimelineTitle.setPaintFlags(binding.tvTimelineTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTimelineTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_text_secondary));
                binding.tvTimelineStatusBadge.setText("Completed");
                binding.tvTimelineStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_status_success));
                binding.tvTimelineStatusBadge.setBackgroundResource(R.drawable.bg_neu_inset);
            } else if (item.isMissed()) {
                binding.tvTimelineTitle.setPaintFlags(binding.tvTimelineTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTimelineTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_text_primary));
                binding.tvTimelineStatusBadge.setText("Missed");
                binding.tvTimelineStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_status_missed));
                binding.tvTimelineStatusBadge.setBackgroundResource(R.drawable.bg_neu_badge_pill);
            } else {
                binding.tvTimelineTitle.setPaintFlags(binding.tvTimelineTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTimelineTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_text_primary));
                binding.tvTimelineStatusBadge.setText("Upcoming");
                binding.tvTimelineStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neu_primary_violet_dark));
                binding.tvTimelineStatusBadge.setBackgroundResource(R.drawable.bg_neu_badge_pill);
            }

            // Interactive completion toggle
            binding.cbTimelineComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleItemCompletion(item, isChecked);
                }
            });

            // Card click also toggles completion
            binding.getRoot().setOnClickListener(v -> {
                binding.cbTimelineComplete.setChecked(!binding.cbTimelineComplete.isChecked());
            });
        }
    }
}
