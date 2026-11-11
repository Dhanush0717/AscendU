package com.ascendu.app.ui.tasks;

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
import com.ascendu.app.databinding.ItemTaskBinding;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onToggleComplete(Task task);
        void onDeleteClick(Task task);
    }

    private final OnTaskClickListener listener;

    public TaskAdapter(OnTaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.isCompleted() == newItem.isCompleted()
                    && oldItem.getPriority() == newItem.getPriority()
                    && oldItem.getCategory().equals(newItem.getCategory());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task) {
            binding.tvTaskTitle.setText(task.getTitle());
            binding.tvTaskDesc.setText(task.getDescription());
            binding.tvTaskCategory.setText(task.getCategory());

            // Strikethrough if completed
            if (task.isCompleted()) {
                binding.tvTaskTitle.setPaintFlags(binding.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTaskTitle.setAlpha(0.6f);
            } else {
                binding.tvTaskTitle.setPaintFlags(binding.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTaskTitle.setAlpha(1.0f);
            }

            // Priority badge
            String priorityText = "Low";
            int priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_purple);
            int priorityBg = ContextCompat.getColor(itemView.getContext(), R.color.pastel_cyan);
            if (task.getPriority() == 3) {
                priorityText = "High";
                priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.status_error);
                priorityBg = ContextCompat.getColor(itemView.getContext(), R.color.pastel_rose);
            } else if (task.getPriority() == 2) {
                priorityText = "Medium";
                priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.status_warning);
                priorityBg = ContextCompat.getColor(itemView.getContext(), R.color.pastel_amber);
            }
            binding.tvTaskPriority.setText(priorityText);
            binding.tvTaskPriority.setTextColor(priorityColor);
            binding.tvTaskPriority.setBackgroundTintList(android.content.res.ColorStateList.valueOf(priorityBg));

            // Avoid triggering listener during binding
            binding.cbTaskComplete.setOnCheckedChangeListener(null);
            binding.cbTaskComplete.setChecked(task.isCompleted());
            binding.cbTaskComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleComplete(task);
                }
            });

            binding.btnDeleteTask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(task);
                }
            });
        }
    }
}
