package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.R;
import com.example.borrowhub.databinding.FragmentTransactionLogsBinding;
import com.example.borrowhub.view.adapter.LogAdapter;
import com.example.borrowhub.viewmodel.LogsViewModel;

public class TransactionLogsFragment extends Fragment {

    private FragmentTransactionLogsBinding binding;
    private LogsViewModel viewModel;
    private LogAdapter logAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(LogsViewModel.class);
        logAdapter = new LogAdapter();

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLogs.setAdapter(logAdapter);

        setupActionFilter();
        setupDatePeriodFilter();
        setupSearch();
        setupPaginationButtons();
        observeLogs();
    }

    private void setupActionFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getTransactionActionOptions()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerActionFilter.setAdapter(adapter);
        binding.spinnerActionFilter.setSelection(0);

        binding.spinnerActionFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                viewModel.setTransactionActionFilter(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setTransactionActionFilter(LogsViewModel.ACTION_ALL);
            }
        });
    }

    private void setupDatePeriodFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewModel.getDatePeriodOptions()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDatePeriod.setAdapter(adapter);
        binding.spinnerDatePeriod.setSelection(0);

        binding.spinnerDatePeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                viewModel.setTransactionDatePeriod(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setTransactionDatePeriod(LogsViewModel.DATE_PERIOD_ALL_TIME);
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setTransactionSearchQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupPaginationButtons() {
        binding.btnPrev.setOnClickListener(v -> viewModel.previousTransactionPage());
        binding.btnNext.setOnClickListener(v -> viewModel.nextTransactionPage());
    }

    private void observeLogs() {
        viewModel.getTransactionLogs().observe(getViewLifecycleOwner(), logs -> {
            logAdapter.setLogs(logs);
            boolean isEmpty = logs == null || logs.isEmpty();
            binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        viewModel.getTransactionCurrentPage().observe(getViewLifecycleOwner(), page -> updatePageIndicator());
        viewModel.getTransactionTotalPages().observe(getViewLifecycleOwner(), total -> updatePageIndicator());
    }

    private void updatePageIndicator() {
        Integer current = viewModel.getTransactionCurrentPage().getValue();
        Integer total = viewModel.getTransactionTotalPages().getValue();
        if (current == null) current = 1;
        if (total == null) total = 1;

        binding.tvPageIndicator.setText(getString(R.string.logs_page_format, current, total));
        binding.btnPrev.setEnabled(current > 1);
        binding.btnNext.setEnabled(current < total);
        binding.layoutPagination.setVisibility(total > 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
