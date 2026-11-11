package com.ascendu.app.ui.plan;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.databinding.ItemOneTimeTaskBinding;
import com.ascendu.app.util.DateUtils;

/**
 * RecyclerView Adapter for One-Time Scheduled Tasks.
 */
public class OneTimeTaskAdapter extends ListAdapter<Task, OneTimeTaskAdapter.TaskViewHolder> {

    private final OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onEdit(Task task);
        void onToggleComplete(Task task, boolean isCompleted);
    }

    public OneTimeTaskAdapter(OnTaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    oldItem.isImportant() == newItem.isImportant() &&
                    oldItem.getDateString().equals(newItem.getDateString()) &&
                    oldItem.getStartTime().equals(newItem.getStartTime()) &&
                    oldItem.getEndTime().equals(newItem.getEndTime());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOneTimeTaskBinding binding = ItemOneTimeTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemOneTimeTaskBinding binding;

        TaskViewHolder(ItemOneTimeTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            binding.tvTaskTitle.setText(task.getTitle());

            // Strike-through if completed
            if (task.isCompleted()) {
                binding.tvTaskTitle.setPaintFlags(binding.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.getRoot().setAlpha(0.65f);
            } else {
                binding.tvTaskTitle.setPaintFlags(binding.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.getRoot().setAlpha(1.0f);
            }

            // Subtitle: Date • Start Time - End Time
            String dateLabel = DateUtils.formatDisplayDateShort(task.getDateString());
            String timeWindow = "";
            if (task.getStartTime() != null && !task.getStartTime().isEmpty()) {
                timeWindow = " • " + DateUtils.format12HourTime(task.getStartTime());
                if (task.getEndTime() != null && !task.getEndTime().isEmpty()) {
                    timeWindow += " - " + DateUtils.format12HourTime(task.getEndTime());
                }
            }
            binding.tvTaskSubtitle.setText(dateLabel + timeWindow);

            // Importance tag
            if (task.isImportant()) {
                binding.tvTaskImportanceBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvTaskImportanceBadge.setVisibility(View.GONE);
            }

            // Category tag
            String cat = task.getCategory();
            binding.tvTaskCategoryBadge.setText(cat != null && !cat.isEmpty() ? cat : "General");

            // Checkbox completion
            binding.cbTaskComplete.setOnCheckedChangeListener(null);
            binding.cbTaskComplete.setChecked(task.isCompleted());
            binding.cbTaskComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleComplete(task, isChecked);
                }
            });

            // Edit button & root click
            binding.btnEditTask.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });
        }
    }
}
