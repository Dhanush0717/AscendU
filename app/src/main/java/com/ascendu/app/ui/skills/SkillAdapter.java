package com.ascendu.app.ui.skills;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.databinding.ItemSkillBinding;

public class SkillAdapter extends ListAdapter<Skill, SkillAdapter.SkillViewHolder> {

    public interface OnSkillClickListener {
        void onSkillClick(Skill skill);
    }

    private final OnSkillClickListener listener;

    public SkillAdapter(OnSkillClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Skill> DIFF_CALLBACK = new DiffUtil.ItemCallback<Skill>() {
        @Override
        public boolean areItemsTheSame(@NonNull Skill oldItem, @NonNull Skill newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Skill oldItem, @NonNull Skill newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getCurrentLevel() == newItem.getCurrentLevel()
                    && oldItem.getTotalMinutesPracticed() == newItem.getTotalMinutesPracticed();
        }
    };

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSkillBinding binding = ItemSkillBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SkillViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class SkillViewHolder extends RecyclerView.ViewHolder {
        private final ItemSkillBinding binding;

        public SkillViewHolder(ItemSkillBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Skill skill) {
            binding.tvSkillName.setText(skill.getName());
            binding.tvSkillCategory.setText(skill.getCategory());
            binding.progressSkillLevel.setProgress(skill.getCurrentLevel());
            binding.tvSkillLevelText.setText("Level " + skill.getCurrentLevel() + " / 5");
            binding.tvSkillPracticeTime.setText(skill.getTotalMinutesPracticed() + " mins practiced");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSkillClick(skill);
                }
            });
        }
    }
}
