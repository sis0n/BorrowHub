package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.databinding.FragmentTransactionHistoryBinding;
import com.example.borrowhub.view.adapter.TransactionHistoryAdapter;
import com.example.borrowhub.viewmodel.TransactionViewModel;

public class TransactionHistoryFragment extends Fragment {

    private FragmentTransactionHistoryBinding binding;
    private TransactionViewModel viewModel;
    private TransactionHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(TransactionViewModel.class);

        adapter = new TransactionHistoryAdapter();
        binding.rvTransactionHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactionHistory.setAdapter(adapter);

        setupSearch();
        observeViewModel();

        viewModel.fetchTransactionHistory(null);
    }

    private void setupSearch() {
        binding.etSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? null : s.toString().trim();
                viewModel.fetchTransactionHistory(query == null || query.isEmpty() ? null : query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getTransactionHistory().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
            boolean isEmpty = records == null || records.isEmpty();
            binding.tvHistoryEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
