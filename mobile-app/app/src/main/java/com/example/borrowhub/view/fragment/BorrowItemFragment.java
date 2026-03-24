package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.databinding.FragmentBorrowItemBinding;
import com.example.borrowhub.view.adapter.BorrowItemRowAdapter;
import com.example.borrowhub.viewmodel.TransactionViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class BorrowItemFragment extends Fragment {

    private static final long SUCCESS_DISPLAY_MS = 2500;

    private FragmentBorrowItemBinding binding;
    private TransactionViewModel viewModel;
    private BorrowItemRowAdapter rowAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBorrowItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share ViewModel with parent TransactionFragment so state survives tab switches
        viewModel = new ViewModelProvider(requireParentFragment()).get(TransactionViewModel.class);

        setupInfoCard();
        setupStudentLookup();
        setupItemRows();
        setupSubmitButton();
        observeViewModel();
    }

    private void setupInfoCard() {
        binding.tvCurrentDateTime.setText(viewModel.getCurrentDateTime());
    }

    private void setupStudentLookup() {
        binding.etStudentNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.lookupStudent(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupItemRows() {
        rowAdapter = new BorrowItemRowAdapter(new BorrowItemRowAdapter.RowListener() {
            @Override
            public void onTypeChanged(int position, String type) {
                viewModel.updateItemRowType(position, type);
            }

            @Override
            public void onNameChanged(int position, String name, int itemId) {
                viewModel.updateItemRowName(position, name, itemId);
            }

            @Override
            public void onRemove(int position) {
                viewModel.removeItemRow(position);
            }
        });

        binding.rvBorrowItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBorrowItems.setAdapter(rowAdapter);
        binding.btnAddItemRow.setOnClickListener(v -> viewModel.addItemRow());
    }

    private void setupSubmitButton() {
        binding.btnSubmitBorrow.setOnClickListener(v -> {
            String studentNumber = getText(binding.etStudentNumber);
            String studentName = getText(binding.etStudentName);
            String course = getText(binding.etCourse);
            String collateral = getText(binding.etCollateral);
            viewModel.submitBorrow(studentNumber, studentName, course, collateral);
        });
    }

    private void observeViewModel() {
        // Auto-fill student name when a matching student number is found
        viewModel.getStudentName().observe(getViewLifecycleOwner(), name -> {
            if (name != null && !name.equals(getText(binding.etStudentName))) {
                binding.etStudentName.setText(name);
            }
        });

        // Auto-fill course when a matching student number is found
        viewModel.getCourse().observe(getViewLifecycleOwner(), course -> {
            if (course != null && !course.equals(getText(binding.etCourse))) {
                binding.etCourse.setText(course);
            }
        });

        // Lock / unlock name and course fields based on whether a student was found
        viewModel.isStudentFound().observe(getViewLifecycleOwner(), found -> {
            boolean editable = found == null || !found;
            binding.tilStudentName.setEnabled(editable);
            binding.tilCourse.setEnabled(editable);
        });

        // Refresh item rows whenever the ViewModel list changes
        viewModel.getItemRows().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                rowAdapter.updateRows(rows);
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                rowAdapter.setCategories(categories);
            }
        });

        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                rowAdapter.setAllItems(items);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.btnSubmitBorrow.setEnabled(!loading);
                binding.btnAddItemRow.setEnabled(!loading);
            }
        });

        // Toggle between form and success state
        viewModel.isSubmitted().observe(getViewLifecycleOwner(), submitted -> {
            if (submitted != null && submitted) {
                binding.tvSuccessReturnDate.setText(viewModel.getCurrentDate());
                binding.scrollViewBorrowForm.setVisibility(View.GONE);
                binding.cardSuccess.setVisibility(View.VISIBLE);

                // Auto-reset the form after a short delay so the user can start a new transaction
                binding.cardSuccess.postDelayed(() -> {
                    if (isAdded() && binding != null) {
                        viewModel.resetForm();
                        if (binding.etStudentNumber != null) {
                            binding.etStudentNumber.setText("");
                        }
                    }
                }, SUCCESS_DISPLAY_MS);
            } else {
                binding.cardSuccess.setVisibility(View.GONE);
                binding.scrollViewBorrowForm.setVisibility(View.VISIBLE);
            }
        });

        // Show validation errors as toasts and immediately clear them
        viewModel.getSubmitError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearSubmitError();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
