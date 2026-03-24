package com.example.borrowhub.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.databinding.FragmentInventoryBinding;
import com.example.borrowhub.view.adapter.ItemAdapter;
import com.example.borrowhub.viewmodel.InventoryConstants;
import com.example.borrowhub.viewmodel.InventoryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment implements ItemAdapter.ItemActionListener {

    private FragmentInventoryBinding binding;
    private InventoryViewModel viewModel;
    private ItemAdapter itemAdapter;
    
    private List<String> categoryNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        boolean useCompactLayout = isCompactLayout();
        itemAdapter = new ItemAdapter(useCompactLayout, this);

        binding.rvInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInventory.setAdapter(itemAdapter);

        setupSearchFilter();
        setupAddItemButton();
        setupPaginationButtons();
        observeViewModel();
    }

    private void setupSearchFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
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

    private void setupTypeFilter(List<String> types) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                types
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTypeFilter.setAdapter(spinnerAdapter);

        binding.spinnerTypeFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) parent.getItemAtPosition(position);
                viewModel.setTypeFilter(selectedType);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setTypeFilter(InventoryConstants.TYPE_ALL);
            }
        });
    }

    private void setupAddItemButton() {
        binding.btnAddItem.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void setupPaginationButtons() {
        binding.btnPrev.setOnClickListener(v -> viewModel.previousPage());
        binding.btnNext.setOnClickListener(v -> viewModel.nextPage());
    }

    private void observeViewModel() {
        // Observe paginated items instead of all items
        viewModel.getPaginatedItems().observe(getViewLifecycleOwner(), this::renderInventory);
        
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryNames.clear();
            List<String> filterTypes = new ArrayList<>();
            filterTypes.add(InventoryConstants.TYPE_ALL);
            
            for (CategoryEntity category : categories) {
                categoryNames.add(category.getName());
                filterTypes.add(category.getName());
            }
            
            setupTypeFilter(filterTypes);
        });

        // Observe pagination state
        viewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> updatePageIndicator());
        viewModel.getTotalPages().observe(getViewLifecycleOwner(), total -> updatePageIndicator());
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnAddItem.setEnabled(!isLoading);
        });

        // Observe operation success to show toasts
        viewModel.getItemOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Determine which message to show based on last action (simple approach for now)
                Toast.makeText(requireContext(), R.string.user_operation_success, Toast.LENGTH_SHORT).show();
                // Optionally reset the success state if using a persistent LiveData
            }
        });
        
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePageIndicator() {
        Integer current = viewModel.getCurrentPage().getValue();
        Integer total = viewModel.getTotalPages().getValue();
        
        if (current == null) current = 1;
        if (total == null) total = 1;

        binding.tvPageIndicator.setText(getString(R.string.inventory_page_format, current, total));
        
        binding.btnPrev.setEnabled(current > 1);
        binding.btnNext.setEnabled(current < total);
        
        // Hide pagination if there is only 1 page and it's empty or has few items
        binding.layoutPagination.setVisibility(total > 1 ? View.VISIBLE : View.GONE);
    }

    private void renderInventory(List<ItemEntity> items) {
        itemAdapter.setItems(items);
        boolean isEmpty = items == null || items.isEmpty();
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditItem(ItemEntity item) {
        showAddEditDialog(item);
    }

    @Override
    public void onDeleteItem(ItemEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.inventory_delete_title)
                .setMessage(getString(R.string.inventory_delete_message, item.name))
                .setNegativeButton(R.string.inventory_action_cancel, null)
                .setPositiveButton(R.string.inventory_action_delete, (dialog, which) -> {
                    viewModel.deleteItem(item.id);
                    Toast.makeText(requireContext(), R.string.inventory_item_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static final String[] ITEM_STATUS_OPTIONS = new String[]{
            "Available",
            "Maintenance",
            "Archived"
    };

    private void showAddEditDialog(@Nullable ItemEntity itemToEdit) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventory_item, null);

        TextInputEditText etItemName = dialogView.findViewById(R.id.etItemName);
        AutoCompleteTextView acType = dialogView.findViewById(R.id.acType);
        AutoCompleteTextView acStatus = dialogView.findViewById(R.id.acStatus);
        TextInputEditText etTotalQuantity = dialogView.findViewById(R.id.etTotalQuantity);
        TextInputEditText etAvailableQuantity = dialogView.findViewById(R.id.etAvailableQuantity);

        // Setup Type Adapter
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
        );
        acType.setAdapter(typeAdapter);

        // Setup Status Adapter
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                ITEM_STATUS_OPTIONS
        );
        acStatus.setAdapter(statusAdapter);

        if (itemToEdit != null) {
            etItemName.setText(itemToEdit.name);
            acType.setText(itemToEdit.type, false);
            
            // Map backend status back to UI display status
            String uiStatus = "Available";
            if ("maintenance".equalsIgnoreCase(itemToEdit.status)) uiStatus = "Maintenance";
            else if ("archived".equalsIgnoreCase(itemToEdit.status)) uiStatus = "Archived";
            acStatus.setText(uiStatus, false);

            etTotalQuantity.setText(String.valueOf(itemToEdit.totalQuantity));
            etAvailableQuantity.setText(String.valueOf(itemToEdit.availableQuantity));
        } else {
            // Default status for new item
            acStatus.setText("Available", false);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(itemToEdit == null ? R.string.inventory_dialog_add_title : R.string.inventory_dialog_edit_title)
                .setView(dialogView)
                .setNegativeButton(R.string.inventory_action_cancel, null)
                .setPositiveButton(itemToEdit == null ? R.string.inventory_action_add : R.string.inventory_action_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = asString(etItemName.getText());
            String type = asString(acType.getText());
            String status = asString(acStatus.getText());
            Integer totalQuantity = parsePositiveInt(etTotalQuantity.getText());
            Integer availableQuantity = parsePositiveInt(etAvailableQuantity.getText());

            if (name.isEmpty() || type.isEmpty() || status.isEmpty() || totalQuantity == null || availableQuantity == null) {
                Toast.makeText(requireContext(), R.string.inventory_error_complete_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (availableQuantity > totalQuantity) {
                Toast.makeText(requireContext(), R.string.inventory_error_available_exceeds_total, Toast.LENGTH_SHORT).show();
                return;
            }

            if (itemToEdit == null) {
                viewModel.addItem(name, type, totalQuantity, availableQuantity, status);
            } else {
                viewModel.updateItem(itemToEdit.id, name, type, totalQuantity, availableQuantity, status);
            }

            dialog.dismiss();
        }));

        dialog.show();
    }

    private boolean isCompactLayout() {
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        boolean isLandscape = getResources().getConfiguration().orientation
                == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        return !isLandscape && screenWidthDp < 600;
    }

    private String asString(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private Integer parsePositiveInt(CharSequence value) {
        try {
            int parsed = Integer.parseInt(asString(value));
            return parsed < 0 ? null : parsed;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
