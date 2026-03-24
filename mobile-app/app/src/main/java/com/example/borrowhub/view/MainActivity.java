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
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.repository.UserRepository;
import com.example.borrowhub.databinding.ActivityMainBinding;
import com.example.borrowhub.databinding.LayoutProfileDropdownBinding;
import com.example.borrowhub.viewmodel.AuthViewModel;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.view.ViewGroup;
import androidx.appcompat.content.res.AppCompatResources;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        userRepository = new UserRepository(getApplication());
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
            if (itemId == R.id.action_profile) {
                // Find the view for the menu item to anchor the dropdown
                View menuItemView = findViewById(R.id.action_profile);
                if (menuItemView == null) {
                    // Fallback to finding the action view by item id from the toolbar
                    menuItemView = binding.topAppBar.findViewById(R.id.action_profile);
                }
                showProfileDropdown(menuItemView);
                return true;
            }
            return false;
        });
    }

    private void showProfileDropdown(View anchor) {
        if (anchor == null) return;

        LayoutProfileDropdownBinding dropdownBinding = LayoutProfileDropdownBinding.inflate(getLayoutInflater());
        
        // Setup user info if available
        if (currentUser != null) {
            dropdownBinding.tvProfileName.setText(currentUser.getName());
            dropdownBinding.tvProfileRole.setText(currentUser.getRole());
        }

        // Setup theme toggle state
        updateThemeUI(dropdownBinding.ivThemeIcon, dropdownBinding.tvThemeText);

        float density = getResources().getDisplayMetrics().density;
        int widthInPx = (int) (220 * density);
        int marginInPx = (int) (16 * density);

        PopupWindow popupWindow = new PopupWindow(
                dropdownBinding.getRoot(),
                widthInPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Styling the popup - background is required for shadows and dismissal behavior
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(24);
        
        // Click listeners
        dropdownBinding.itemAccountSettings.setOnClickListener(v -> {
            if (navController != null) navController.navigate(R.id.accountSettingsFragment);
            popupWindow.dismiss();
        });

        dropdownBinding.itemUserManagement.setOnClickListener(v -> {
            if (navController != null) navController.navigate(R.id.userManagementFragment);
            popupWindow.dismiss();
        });

        dropdownBinding.itemStudentManagement.setOnClickListener(v -> {
            if (navController != null) navController.navigate(R.id.studentManagementFragment);
            popupWindow.dismiss();
        });

        dropdownBinding.itemThemeToggle.setOnClickListener(v -> {
            toggleThemeMode();
            // Optional: Dismiss or update UI. For system-wide theme change, activity usually recreates.
            popupWindow.dismiss();
        });

        dropdownBinding.itemLogout.setOnClickListener(v -> {
            authViewModel.logout();
            popupWindow.dismiss();
        });

        // Show as dropdown with offsets to align near the right edge of the screen
        popupWindow.showAsDropDown(anchor, -(widthInPx - anchor.getWidth()) - marginInPx, 12);
    }

    private void updateThemeUI(android.widget.ImageView icon, android.widget.TextView text) {
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

        if (text != null) {
            text.setText(isDarkMode ? R.string.theme_switch_to_light : R.string.theme_switch_to_dark);
        }
        if (icon != null) {
            icon.setImageDrawable(ContextCompat.getDrawable(this, isDarkMode ? R.drawable.ic_sun : R.drawable.ic_moon));
        }
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

        // Observe current user for profile header
        userRepository.getUser().observe(this, user -> {
            this.currentUser = user;
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
    }
}
