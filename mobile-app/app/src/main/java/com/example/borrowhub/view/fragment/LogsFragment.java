package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.borrowhub.R;
import com.example.borrowhub.databinding.FragmentLogsBinding;
import com.example.borrowhub.viewmodel.LogsViewModel;
import com.google.android.material.tabs.TabLayout;

public class LogsFragment extends Fragment {
    private static final int TAB_TRANSACTION = 0;
    private static final int TAB_ACTIVITY = 1;

    private FragmentLogsBinding binding;
    private LogsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LogsViewModel.class);
        setupTabs();

        if (savedInstanceState == null) {
            showTab(TAB_TRANSACTION);
        }
    }

    private void setupTabs() {
        binding.tabLayoutLogs.addTab(binding.tabLayoutLogs.newTab().setText(R.string.logs_tab_transaction));
        binding.tabLayoutLogs.addTab(binding.tabLayoutLogs.newTab().setText(R.string.logs_tab_activity));

        binding.tabLayoutLogs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showTab(int position) {
        Fragment fragment = position == TAB_ACTIVITY
                ? new ActivityLogsFragment()
                : new TransactionLogsFragment();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.logsTabContainer, fragment)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
