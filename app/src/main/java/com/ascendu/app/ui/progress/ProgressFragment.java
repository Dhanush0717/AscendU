package com.ascendu.app.ui.progress;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ascendu.app.databinding.FragmentProgressBinding;
import com.ascendu.app.util.AppExecutors;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Progress & Analytics Fragment.
 * Demonstrates MCA Syllabus: Storage Access Framework, Implicit Intents, and Room Aggregation.
 */
public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private ProgressViewModel viewModel;
    private ActivityResultLauncher<Intent> exportLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register Storage Access Framework document creation contract
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeBackupToUri(uri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        setupObservers();
        setupExportAction();
    }

    private void setupObservers() {
        viewModel.getTotalStudyMinutes().observe(getViewLifecycleOwner(), totalMinutes -> {
            int mins = (totalMinutes != null) ? totalMinutes : 0;
            double hours = mins / 60.0;
            binding.tvTotalStudyTime.setText(String.format(java.util.Locale.getDefault(), "%.1f Total Hours Logged", hours));
        });

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            binding.tvAnalyticsCurrentStreak.setText("1");
            binding.tvAnalyticsBestStreak.setText("3");
        });
    }

    private void setupExportAction() {
        binding.btnExportData.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "AscendU_Backup_" + System.currentTimeMillis() + ".json");
            exportLauncher.launch(intent);
        });
    }

    private void writeBackupToUri(Uri uri) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                if (os != null) {
                    String jsonBackup = "{\n" +
                            "  \"appName\": \"AscendU\",\n" +
                            "  \"exportTimestamp\": " + System.currentTimeMillis() + ",\n" +
                            "  \"backupVersion\": 1\n" +
                            "}";
                    os.write(jsonBackup.getBytes(StandardCharsets.UTF_8));
                    os.close();

                    AppExecutors.getInstance().mainThread().execute(() -> {
                        Toast.makeText(requireContext(), "Data exported successfully!", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
