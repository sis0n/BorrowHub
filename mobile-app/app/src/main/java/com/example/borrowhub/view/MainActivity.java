package com.example.borrowhub.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.databinding.ActivityMainBinding;
import com.example.borrowhub.viewmodel.AuthViewModel;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.content.res.AppCompatResources;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        applySavedThemeMode();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        // Set settings overflow icon
        binding.topAppBar.setOverflowIcon(AppCompatResources.getDrawable(this, R.drawable.ic_account_settings));

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            //NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                NavOptions.Builder builder = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(true);

                // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                // on the back stack as users select items
                builder.setPopUpTo(navController.getGraph().getStartDestinationId(), false);

                NavOptions options = builder.build();
                navController.navigate(item.getItemId(), null, options);
                return true;
            });

            // Correctly handle highlight state for all destinations
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                MenuItem item = binding.bottomNavigationView.getMenu().findItem(destId);

                if (item != null) {
                    // Destination is in bottom menu, select it directly.
                    // setChecked(true) automatically unchecks others in an exclusive group.
                    item.setChecked(true);
                } else {
                    // Destination is NOT in bottom menu (e.g., Student/User Management)
                    // Temporarily disable exclusive checkable to allow having NO items selected.
                    binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                    for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                        binding.bottomNavigationView.getMenu().getItem(i).setChecked(false);
                    }
                    // Re-enable exclusive checkable for future bottom menu interactions.
                    binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
                }
            });
        }

        // Setup TopAppBar menu clicks
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_account_settings) {
                if (navController != null) {
                    navController.navigate(R.id.accountSettingsFragment);
                }
                return true;
            } else if (itemId == R.id.action_user_management) {
                if (navController != null) {
                    navController.navigate(R.id.userManagementFragment);
                }
                return true;
            } else if (itemId == R.id.action_student_management) {
                if (navController != null) {
                    // Navigate to student management fragment
                    navController.navigate(R.id.studentManagementFragment);
                }
                return true;
            } else if (itemId == R.id.action_theme_toggle) {
                toggleThemeMode();
                return true;
            } else if (itemId == R.id.action_logout) {
                authViewModel.logout();
                return true;
            }
            return false;
        });
        binding.topAppBar.post(this::refreshThemeMenuItem);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void setupObservers() {
        authViewModel.getLogoutResult().observe(this, isSuccess -> {
            boolean handled = false;
            if (Boolean.TRUE.equals(isSuccess)) {
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                handled = true;
            } else if (Boolean.FALSE.equals(isSuccess)) {
                Toast.makeText(this, R.string.logout_failed, Toast.LENGTH_SHORT).show();
                handled = true;
            }

            if (handled) {
                authViewModel.clearLogoutResult();
            }
        });
    }

    private void applySavedThemeMode() {
        AppCompatDelegate.setDefaultNightMode(sessionManager.getThemeMode());
    }

    private void toggleThemeMode() {
        int currentMode = sessionManager.getThemeMode();
        int nextMode;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            nextMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            nextMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            int currentUiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isCurrentlyDark = currentUiMode == Configuration.UI_MODE_NIGHT_YES;
            nextMode = isCurrentlyDark
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES;
        }

        sessionManager.setThemeMode(nextMode);
        AppCompatDelegate.setDefaultNightMode(nextMode);
        refreshThemeMenuItem();
    }

    private void updateThemeMenuItem(MenuItem themeItem) {
        if (themeItem == null) {
            return;
        }

        int savedMode = sessionManager.getThemeMode();
        boolean isDarkMode;
        if (savedMode == AppCompatDelegate.MODE_NIGHT_YES) {
            isDarkMode = true;
        } else if (savedMode == AppCompatDelegate.MODE_NIGHT_NO) {
            isDarkMode = false;
        } else {
            int currentUiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isDarkMode = currentUiMode == Configuration.UI_MODE_NIGHT_YES;
        }
        themeItem.setTitle(isDarkMode ? R.string.theme_switch_to_light : R.string.theme_switch_to_dark);
        themeItem.setIcon(ContextCompat.getDrawable(this, isDarkMode ? R.drawable.ic_sun : R.drawable.ic_moon));
    }

    private void refreshThemeMenuItem() {
        updateThemeMenuItem(binding.topAppBar.getMenu().findItem(R.id.action_theme_toggle));
    }
}
