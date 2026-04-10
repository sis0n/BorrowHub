package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.R;
import com.example.borrowhub.databinding.FragmentReturnItemBinding;
import com.example.borrowhub.view.adapter.ActiveBorrowAdapter;
import com.example.borrowhub.viewmodel.TransactionViewModel;
import com.example.borrowhub.viewmodel.TransactionViewModel.ActiveBorrow;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ReturnItemFragment extends Fragment implements ActiveBorrowAdapter.OnBorrowClickListener {

    private FragmentReturnItemBinding binding;
    private TransactionViewModel viewModel;
    private ActiveBorrowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReturnItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share the ViewModel with the parent TransactionFragment so tab state is preserved.
        viewModel = new ViewModelProvider(requireParentFragment())
                .get(TransactionViewModel.class);

        adapter = new ActiveBorrowAdapter(this);

        binding.rvActiveBorrows.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvActiveBorrows.setAdapter(adapter);

        setupSearchFilter();
        setupInfoCard();
        observeActiveBorrows();
    }

    private void setupInfoCard() {
        viewModel.getCurrentDateTimeLive().observe(getViewLifecycleOwner(), dateTime -> {
            if (dateTime != null) {
                binding.tvCurrentDateTime.setText(dateTime);
            }
        });
        viewModel.getProcessedByName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                binding.tvProcessedBy.setText(name);
            }
        });
    }

    private void setupSearchFilter() {
        binding.etReturnSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeActiveBorrows() {
        viewModel.getFilteredBorrows().observe(getViewLifecycleOwner(), this::renderBorrows);
    }

    private void renderBorrows(List<ActiveBorrow> borrows) {
        adapter.setBorrows(borrows);
        boolean isEmpty = borrows == null || borrows.isEmpty();
        binding.tvReturnEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBorrowClick(ActiveBorrow borrow) {
        showReturnVerificationDialog(borrow);
    }

    private void showReturnVerificationDialog(ActiveBorrow borrow) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_return_verification, null);

        // Borrower info
        bindText(dialogView, R.id.tvDialogStudentName, borrow.studentName);
        bindText(dialogView, R.id.tvDialogStudentNumber, borrow.studentNumber);
        bindText(dialogView, R.id.tvDialogCourse, borrow.course);
        bindText(dialogView, R.id.tvDialogCollateral, borrow.collateral);

        // Items list
        LinearLayout llDialogItems = dialogView.findViewById(R.id.llDialogItems);
        llDialogItems.removeAllViews();
        if (borrow.items != null) {
            for (String itemName : borrow.items) {
                TextView tvItem = new TextView(requireContext());
                tvItem.setText(getString(R.string.return_item_bullet_format, itemName));
                tvItem.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_700));
                tvItem.setTextSize(13f);
                tvItem.setPadding(0, 2, 0, 2);
                llDialogItems.addView(tvItem);
            }
        }

        // Time info
        bindText(dialogView, R.id.tvDialogBorrowDateTime,
                getString(R.string.return_dialog_borrow_datetime_format,
                        borrow.formattedDateTime));

        // Collateral reminder
        TextView tvReminder = dialogView.findViewById(R.id.tvCollateralReminder);
        tvReminder.setText(getString(R.string.return_dialog_collateral_reminder, borrow.collateral));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.return_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.return_dialog_action_cancel, null)
                .setPositiveButton(R.string.return_dialog_action_process, (dialog, which) -> {
                    viewModel.processReturn(borrow.id);
                    Toast.makeText(requireContext(),
                            R.string.return_success_toast, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void bindText(View root, int viewId, String text) {
        TextView tv = root.findViewById(viewId);
        if (tv != null) {
            tv.setText(text != null ? text : "—");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
