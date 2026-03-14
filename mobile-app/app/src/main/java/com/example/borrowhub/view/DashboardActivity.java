package com.example.borrowhub.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.databinding.ActivityDashboardBinding;
import com.example.borrowhub.view.adapter.TransactionAdapter;
import com.example.borrowhub.viewmodel.DashboardViewModel;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.dashboard, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView
        transactionAdapter = new TransactionAdapter();
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Observe Data
        observeViewModel();

        // Setup click listeners
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.getDashboardStats().observe(this, stats -> {
            if (stats != null) {
                binding.tvTotalBorrowedValue.setText(String.valueOf(stats.totalBorrowed));
                binding.tvActiveRequestsValue.setText(String.valueOf(stats.activeRequests));
                binding.tvOverdueItemsValue.setText(String.valueOf(stats.overdueItems));
            }
        });

        viewModel.getRecentTransactions().observe(this, transactions -> {
            if (transactions != null) {
                transactionAdapter.setTransactions(transactions);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBorrowReturn.setOnClickListener(v -> {
            Toast.makeText(this, "Navigate to Borrow/Return", Toast.LENGTH_SHORT).show();
            // In a complete implementation, this would navigate to a BorrowReturnActivity
            // Intent intent = new Intent(DashboardActivity.this, BorrowReturnActivity.class);
            // startActivity(intent);
        });

        binding.btnManageInventory.setOnClickListener(v -> {
            Toast.makeText(this, "Navigate to Manage Inventory", Toast.LENGTH_SHORT).show();
            // In a complete implementation, this would navigate to a ManageInventoryActivity
            // Intent intent = new Intent(DashboardActivity.this, ManageInventoryActivity.class);
            // startActivity(intent);
        });

        binding.btnViewAll.setOnClickListener(v -> {
            Toast.makeText(this, "Navigate to Logs", Toast.LENGTH_SHORT).show();
            // In a complete implementation, this would navigate to a LogsActivity
            // Intent intent = new Intent(DashboardActivity.this, LogsActivity.class);
            // startActivity(intent);
        });
    }
}