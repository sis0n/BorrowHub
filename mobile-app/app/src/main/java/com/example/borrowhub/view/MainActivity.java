package com.example.borrowhub.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.borrowhub.R;
import com.example.borrowhub.databinding.ActivityMainBinding;
import com.example.borrowhub.viewmodel.AuthViewModel;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            // Correctly handle highlight state for non-bottom-nav destinations
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                // Check if the destination is part of the bottom menu
                boolean isInBottomMenu = destId == R.id.homeFragment ||
                                         destId == R.id.inventoryFragment ||
                                         destId == R.id.transactionFragment ||
                                         destId == R.id.logsFragment;

                if (!isInBottomMenu) {
                    // Deselect all items if we are in a sub-view (like Student Management)
                    // but NOT if we are in one of the main tabs.
                    binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                    for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                        binding.bottomNavigationView.getMenu().getItem(i).setChecked(false);
                    }
                    binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
                } else {
                    // Select the corresponding item in the bottom navigation view
                    for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                        MenuItem item = binding.bottomNavigationView.getMenu().getItem(i);
                        item.setChecked(item.getItemId() == destId);
                    }
                }
            });
        }

        // Setup TopAppBar menu clicks
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_account_settings) {
                Toast.makeText(this, "Account Settings Clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_user_management) {
                Toast.makeText(this, "User Management Clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_student_management) {
                if (navController != null) {
                    // Navigate to student management fragment
                    navController.navigate(R.id.studentManagementFragment);
                }
                return true;
            } else if (itemId == R.id.action_logout) {
                authViewModel.logout();
                return true;
            }
            return false;
        });
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
}

