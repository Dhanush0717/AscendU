package com.ascendu.app.ui.today;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ascendu.app.databinding.BottomSheetNotificationSettingsBinding;
import com.ascendu.app.notifications.NotificationHelper;
import com.ascendu.app.notifications.NotificationPreferences;
import com.ascendu.app.util.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

/**
 * Bottom sheet dialog allowing users to manage local notification preferences,
 * enable/disable important task alarms, and configure daily evening review timing.
 */
public class NotificationSettingsBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetNotificationSettingsBinding binding;
    private NotificationPreferences prefs;

    public static NotificationSettingsBottomSheet newInstance() {
        return new NotificationSettingsBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNotificationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new NotificationPreferences(requireContext());

        // Bind initial states
        binding.switchImportantReminders.setChecked(prefs.isImportantTasksEnabled());
        binding.switchEveningReview.setChecked(prefs.isEveningReviewEnabled());
        updateEveningTimeButton(prefs.getEveningHour(), prefs.getEveningMinute());

        binding.layoutEveningTimeContainer.setVisibility(prefs.isEveningReviewEnabled() ? View.VISIBLE : View.GONE);

        // Listeners
        binding.switchImportantReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setImportantTasksEnabled(isChecked);
            if (isChecked) {
                NotificationHelper.rescheduleAllAlarms(requireContext());
            }
        });

        binding.switchEveningReview.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setEveningReviewEnabled(isChecked);
            binding.layoutEveningTimeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            NotificationHelper.scheduleDailyEveningReview(requireContext());
        });

        binding.btnEveningTime.setOnClickListener(v -> {
            int currentHour = prefs.getEveningHour();
            int currentMinute = prefs.getEveningMinute();
            TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                prefs.setEveningTime(hourOfDay, minute);
                updateEveningTimeButton(hourOfDay, minute);
                NotificationHelper.scheduleDailyEveningReview(requireContext());
            }, currentHour, currentMinute, false);
            dialog.show();
        });

        binding.btnSettingsClose.setOnClickListener(v -> dismiss());
    }

    private void updateEveningTimeButton(int hour, int minute) {
        String time24 = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        binding.btnEveningTime.setText(DateUtils.format12HourTime(time24));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
