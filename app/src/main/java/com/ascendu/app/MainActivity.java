package com.ascendu.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ascendu.app.databinding.ActivityMainBinding;
import com.ascendu.app.ui.onboarding.OnboardingActivity;

/**
 * Main application container implementing Single-Activity architecture.
 * MCA Syllabus Compliance: Activity Lifecycle, ViewBinding, Navigation Component, SharedPreferences.
 */
public class MainActivity extends AppCompatActivity {

    public static final String PREF_NAME = "AscendUPrefs";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private ActivityMainBinding binding;
    private NavController navController;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Notifications permission handled
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if first-launch onboarding is needed
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
        if (!isOnboardingCompleted) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle System Bar Insets gracefully:
        // - Top status bar inset is applied to root view
        // - Bottom gesture/navigation bar inset is applied as padding to the bottom_navigation_container
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.bottomNavigationContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Setup Jetpack Navigation Component with BottomNavigationView
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        // Initialize smart local notifications & schedule evening accountability review
        com.ascendu.app.notifications.NotificationHelper.createNotificationChannels(this);
        com.ascendu.app.notifications.NotificationHelper.rescheduleAllAlarms(this);

        // Request runtime notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
