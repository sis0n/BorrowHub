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
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.view.ViewGroup;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.color.MaterialColors;

public class MainActivity extends AppCompatActivity {
    private static final String EXTRA_RESTORE_DESTINATION_ID = "extra_restore_destination_id";
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved theme before super.onCreate() to prevent theme flicker on startup
        sessionManager = new SessionManager(this);
        applySavedThemeMode();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = new UserRepository(getApplication());

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
            restoreDestinationIfNeeded();

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
        binding.topAppBar.post(this::updateProfileMenuIconTint);
    }

    private void showProfileDropdown(View anchor) {
        if (anchor == null) return;

        LayoutProfileDropdownBinding dropdownBinding = LayoutProfileDropdownBinding.inflate(getLayoutInflater());
        
        // Setup user info if available
        if (currentUser != null) {
            dropdownBinding.tvProfileName.setText(currentUser.getName());
            dropdownBinding.tvProfileRole.setText(currentUser.getRole());
        } else {
            dropdownBinding.tvProfileName.setText(sessionManager.getUserName());
            dropdownBinding.tvProfileRole.setText(sessionManager.getUserRole());
        }

        // Apply role-based visibility: hide management items for non-admin users
        String currentRole = currentUser != null ? currentUser.getRole() : sessionManager.getUserRole();
        boolean isAdmin = SessionManager.ROLE_ADMIN.equalsIgnoreCase(currentRole);
        if (!isAdmin) {
            dropdownBinding.itemUserManagement.setVisibility(View.GONE);
            dropdownBinding.itemStudentManagement.setVisibility(View.GONE);
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
            popupWindow.dismiss();
            toggleThemeMode();
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
        int savedMode = sessionManager.getThemeMode();
        // setLocalNightMode configures THIS activity's AppCompatDelegate directly before
        // super.onCreate() creates the window, so the correct theme is applied from the
        // very first frame with no recreation loop.
        // setDefaultNightMode is called second so the process-wide default stays in sync;
        // by then the local mode is already set, so the global broadcast back to this
        // delegate is a no-op and will NOT schedule a recreation.
        getDelegate().setLocalNightMode(savedMode);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    private void restoreDestinationIfNeeded() {
        if (navController == null) return;
        int targetDestinationId = getIntent().getIntExtra(EXTRA_RESTORE_DESTINATION_ID, -1);
        if (targetDestinationId == -1) return;
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == targetDestinationId) {
            return;
        }
        try {
            navController.navigate(targetDestinationId);
            getIntent().removeExtra(EXTRA_RESTORE_DESTINATION_ID);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Failed to restore destination id " + targetDestinationId
                    + ". Destination may be invalid or not present in nav_graph.", exception);
            getIntent().removeExtra(EXTRA_RESTORE_DESTINATION_ID);
        }
    }

    private void updateProfileMenuIconTint() {
        if (binding == null) return;
        MenuItem profileItem = binding.topAppBar.getMenu().findItem(R.id.action_profile);
        if (profileItem == null) return;

        int iconColor = MaterialColors.getColor(binding.topAppBar, com.google.android.material.R.attr.colorOnSurface);
        Drawable icon = profileItem.getIcon();
        if (icon == null) {
            icon = AppCompatResources.getDrawable(this, R.drawable.ic_profile_toolbar);
        }
        if (icon == null) {
            Log.w(TAG, "Profile toolbar icon is null; check menu_profile.xml action_profile icon.");
            return;
        }
        Drawable wrapped = DrawableCompat.wrap(icon.mutate());
        DrawableCompat.setTintList(wrapped, ColorStateList.valueOf(iconColor));
        profileItem.setIcon(wrapped);
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
        // Restart the activity with a smooth cross-fade instead of using Activity.recreate().
        // recreate() is asynchronous and overridePendingTransition is not honoured for it,
        // which causes a black-screen flash. Using startActivity + finish gives full control
        // over the transition. applySavedThemeMode() in the new onCreate() applies the theme
        // before super.onCreate() so there is no flicker.
        Intent restartIntent = new Intent(this, MainActivity.class);
        if (navController != null && navController.getCurrentDestination() != null) {
            restartIntent.putExtra(EXTRA_RESTORE_DESTINATION_ID, navController.getCurrentDestination().getId());
        }
        startActivity(restartIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
