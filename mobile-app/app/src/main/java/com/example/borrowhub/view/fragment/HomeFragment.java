package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.borrowhub.R;
import com.example.borrowhub.databinding.FragmentHomeBinding;
import com.example.borrowhub.view.adapter.TransactionAdapter;
import com.example.borrowhub.viewmodel.DashboardViewModel;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private DashboardViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        setupRecyclerView();
        observeViewModel();

        NavController navController = Navigation.findNavController(view);

        binding.btnBorrowReturn.setOnClickListener(v -> 
            navController.navigate(R.id.transactionFragment));

        binding.btnManageInventory.setOnClickListener(v -> 
            navController.navigate(R.id.inventoryFragment));

        binding.btnViewAll.setOnClickListener(v -> 
            navController.navigate(R.id.logsFragment));

        // Refresh data on swipe or when returning to this fragment
        viewModel.fetchData();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.tvTotalBorrowedValue.setText(String.valueOf(stats.totalItems));
                binding.tvActiveRequestsValue.setText(String.valueOf(stats.currentlyBorrowed));
                binding.tvOverdueItemsValue.setText(String.valueOf(stats.dueToday));
            }
        });

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}