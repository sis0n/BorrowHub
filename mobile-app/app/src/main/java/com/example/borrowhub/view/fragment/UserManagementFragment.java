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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.databinding.FragmentUserManagementBinding;
import com.example.borrowhub.view.adapter.UserAdapter;
import com.example.borrowhub.viewmodel.UserManagementViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class UserManagementFragment extends Fragment implements UserAdapter.UserActionListener {

    private FragmentUserManagementBinding binding;
    private UserManagementViewModel viewModel;
    private UserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Guard: only admin users may access this screen
        SessionManager sessionManager = new SessionManager(requireContext());
        if (!SessionManager.ROLE_ADMIN.equalsIgnoreCase(sessionManager.getUserRole())) {
            Toast.makeText(requireContext(), R.string.error_unauthorized, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);
        userAdapter = new UserAdapter(this);

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(userAdapter);

        setupSearchFilter();
        setupButtons();
        observeUsers();
        observeOperationState();
    }

    private void setupSearchFilter() {
        binding.etUserSearch.addTextChangedListener(new TextWatcher() {
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

    private void setupButtons() {
        binding.btnAddUser.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void observeUsers() {
        viewModel.getFilteredUsers().observe(getViewLifecycleOwner(), this::renderUsers);
    }

    private void observeOperationState() {
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), R.string.user_operation_success, Toast.LENGTH_SHORT).show();
                updateUserCount();
                viewModel.clearOperationStates();
            }
        });

        viewModel.getOperationError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearOperationStates();
            }
        });
    }

    private void renderUsers(List<User> users) {
        userAdapter.setUsers(users);
        boolean isEmpty = users == null || users.isEmpty();
        binding.tvUserEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        updateUserCount();
    }

    private void updateUserCount() {
        binding.tvUserCount.setText(getString(R.string.user_total_count, viewModel.getTotalUserCount()));
    }

    @Override
    public void onEditUser(User user) {
        showAddEditDialog(user);
    }

    @Override
    public void onDeleteUser(User user) {
        if (viewModel.isProtectedAdmin(user)) {
            Toast.makeText(requireContext(), R.string.user_delete_protected_admin, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_delete_title)
                .setMessage(getString(R.string.user_delete_message, user.getName()))
                .setNegativeButton(R.string.user_action_cancel, null)
                .setPositiveButton(R.string.user_action_delete, (dialog, which) -> viewModel.deleteUser(user))
                .show();
    }

    @Override
    public void onResetPassword(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_reset_password_title)
                .setMessage(getString(R.string.user_reset_password_message, user.getUsername()))
                .setNegativeButton(R.string.user_action_cancel, null)
                .setPositiveButton(R.string.user_action_reset, (dialog, which) -> viewModel.resetPasswordToDefault(user))
                .show();
    }

    private void showAddEditDialog(@Nullable User userToEdit) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user, null);

        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etUsername);
        AutoCompleteTextView acRole = dialogView.findViewById(R.id.acRole);

        List<String> roles = Arrays.asList(
                getString(R.string.user_role_admin),
                getString(R.string.user_role_staff)
        );
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        acRole.setAdapter(roleAdapter);

        if (userToEdit != null) {
            etFullName.setText(userToEdit.getName());
            etUsername.setText(userToEdit.getUsername());
            acRole.setText(toRoleLabel(userToEdit.getRole()), false);
        } else {
            acRole.setText(getString(R.string.user_role_staff), false);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(userToEdit == null ? R.string.user_dialog_add_title : R.string.user_dialog_edit_title)
                .setView(dialogView)
                .setNegativeButton(R.string.user_action_cancel, null)
                .setPositiveButton(userToEdit == null ? R.string.user_action_save : R.string.user_action_update, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String fullName = asString(etFullName.getText());
            String username = asString(etUsername.getText());
            String role = normalizeRole(asString(acRole.getText()));

            if (fullName.isEmpty() || username.isEmpty() || role.isEmpty()) {
                Toast.makeText(requireContext(), R.string.user_error_complete_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (userToEdit == null) {
                viewModel.addUser(fullName, username, role);
                Toast.makeText(requireContext(), R.string.user_add_processing, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.updateUser(userToEdit, fullName, username, role);
                Toast.makeText(requireContext(), R.string.user_update_processing, Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        }));

        dialog.show();
    }

    private String normalizeRole(String roleText) {
        String normalized = roleText.trim();
        if (normalized.equalsIgnoreCase(getString(R.string.user_role_admin))) {
            return "admin";
        }
        if (normalized.equalsIgnoreCase(getString(R.string.user_role_staff))) {
            return "staff";
        }
        return normalized.toLowerCase();
    }

    private String toRoleLabel(String role) {
        if (role != null && role.equalsIgnoreCase("admin")) {
            return getString(R.string.user_role_admin);
        }
        return getString(R.string.user_role_staff);
    }

    private String asString(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
