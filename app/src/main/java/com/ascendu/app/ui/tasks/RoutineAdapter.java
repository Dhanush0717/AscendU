package com.ascendu.app.ui.tasks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.databinding.ItemRoutineBinding;

import java.util.HashSet;
import java.util.Set;

public class RoutineAdapter extends ListAdapter<DailyRoutine, RoutineAdapter.RoutineViewHolder> {

    public interface OnRoutineClickListener {
        void onToggleComplete(DailyRoutine routine, boolean isCompleted);
        void onDeleteClick(DailyRoutine routine);
    }

    private final OnRoutineClickListener listener;
    private final Set<Long> completedRoutineIds = new HashSet<>();

    public RoutineAdapter(OnRoutineClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setCompletedRoutineIds(Set<Long> ids) {
        completedRoutineIds.clear();
        if (ids != null) {
            completedRoutineIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<DailyRoutine> DIFF_CALLBACK = new DiffUtil.ItemCallback<DailyRoutine>() {
        @Override
        public boolean areItemsTheSame(@NonNull DailyRoutine oldItem, @NonNull DailyRoutine newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DailyRoutine oldItem, @NonNull DailyRoutine newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getCategory().equals(newItem.getCategory())
                    && oldItem.getTargetMinutes() == newItem.getTargetMinutes()
                    && oldItem.getCurrentStreak() == newItem.getCurrentStreak();
        }
    };

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoutineBinding binding = ItemRoutineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RoutineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoutineBinding binding;

        public RoutineViewHolder(ItemRoutineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DailyRoutine routine) {
            binding.tvRoutineTitle.setText(routine.getTitle());
            binding.tvRoutineCategory.setText(routine.getCategory() + " • Daily Non-Negotiable");

            int streak = Math.max(1, routine.getCurrentStreak());
            binding.tvRoutineStreak.setText("🔥 " + streak + "d streak");

            boolean isCompleted = completedRoutineIds.contains(routine.getId());

            // Strikethrough effect on completion
            if (isCompleted) {
                binding.tvRoutineTitle.setPaintFlags(binding.tvRoutineTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvRoutineTitle.setAlpha(0.6f);
            } else {
                binding.tvRoutineTitle.setPaintFlags(binding.tvRoutineTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvRoutineTitle.setAlpha(1.0f);
            }

            binding.cbRoutineComplete.setOnCheckedChangeListener(null);
            binding.cbRoutineComplete.setChecked(isCompleted);
            binding.cbRoutineComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleComplete(routine, isChecked);
                }
            });

            binding.btnDeleteRoutine.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(routine);
                }
            });
        }
    }
}
