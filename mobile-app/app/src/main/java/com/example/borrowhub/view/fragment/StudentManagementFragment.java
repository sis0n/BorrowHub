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
import com.example.borrowhub.data.local.entity.StudentEntity;
import com.example.borrowhub.databinding.FragmentStudentManagementBinding;
import com.example.borrowhub.view.adapter.StudentAdapter;
import com.example.borrowhub.viewmodel.StudentManagementViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementFragment extends Fragment implements StudentAdapter.StudentActionListener {

    private FragmentStudentManagementBinding binding;
    private StudentManagementViewModel viewModel;
    private StudentAdapter studentAdapter;
    private List<String> availableCourses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StudentManagementViewModel.class);
        studentAdapter = new StudentAdapter(this);

        binding.rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStudents.setAdapter(studentAdapter);

        setupSearchFilter();
        setupButtons();
        observeStudents();
        observeCourses();
        observeOperationState();
    }

    private void setupSearchFilter() {
        binding.etStudentSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        binding.btnAddStudent.setOnClickListener(v -> showAddEditDialog(null));
        binding.btnImportStudents.setOnClickListener(v -> showImportDialog());
    }

    private void observeStudents() {
        viewModel.getFilteredStudents().observe(getViewLifecycleOwner(), this::renderStudents);
    }

    private void observeCourses() {
        viewModel.getAvailableCourses().observe(getViewLifecycleOwner(), courses -> {
            availableCourses = courses == null ? new ArrayList<>() : new ArrayList<>(courses);
        });
    }

    private void observeOperationState() {
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                updateStudentCount();
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

    private void renderStudents(List<StudentEntity> students) {
        studentAdapter.setStudents(students);
        boolean isEmpty = students == null || students.isEmpty();
        binding.tvStudentEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        updateStudentCount();
    }

    private void updateStudentCount() {
        int total = viewModel.getTotalStudentCount();
        binding.tvStudentCount.setText(
                getString(R.string.student_total_count, total));
    }

    @Override
    public void onEditStudent(StudentEntity student) {
        showAddEditDialog(student);
    }

    @Override
    public void onDeleteStudent(StudentEntity student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.student_delete_title)
                .setMessage(getString(R.string.student_delete_message, student.name))
                .setNegativeButton(R.string.student_action_cancel, null)
                .setPositiveButton(R.string.student_action_delete, (dialog, which) -> {
                    viewModel.deleteStudent(student.id);
                    Toast.makeText(requireContext(), R.string.student_delete_processing, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showAddEditDialog(@Nullable StudentEntity studentToEdit) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_student, null);

        TextInputEditText etStudentNumber = dialogView.findViewById(R.id.etStudentNumber);
        TextInputEditText etStudentName = dialogView.findViewById(R.id.etStudentName);
        AutoCompleteTextView acCourse = dialogView.findViewById(R.id.acCourse);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                availableCourses
        );
        acCourse.setAdapter(courseAdapter);

        if (studentToEdit != null) {
            etStudentNumber.setText(studentToEdit.studentNumber);
            etStudentName.setText(studentToEdit.name);
            acCourse.setText(studentToEdit.course, false);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(studentToEdit == null
                        ? R.string.student_dialog_add_title
                        : R.string.student_dialog_edit_title)
                .setView(dialogView)
                .setNegativeButton(R.string.student_action_cancel, null)
                .setPositiveButton(studentToEdit == null
                        ? R.string.student_action_save
                        : R.string.student_action_update, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String studentNumber = asString(etStudentNumber.getText());
            String name = asString(etStudentName.getText());
            String course = asString(acCourse.getText());

            if (studentNumber.isEmpty() || name.isEmpty() || course.isEmpty()) {
                Toast.makeText(requireContext(), R.string.student_error_complete_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            long excludeId = studentToEdit != null ? studentToEdit.id : -1L;
            if (viewModel.isStudentNumberDuplicate(studentNumber, excludeId)) {
                Toast.makeText(requireContext(), R.string.student_error_duplicate_number, Toast.LENGTH_SHORT).show();
                return;
            }

            if (studentToEdit == null) {
                viewModel.addStudent(studentNumber, name, course);
                Toast.makeText(requireContext(), R.string.student_add_processing, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.updateStudent(studentToEdit.id, studentNumber, name, course);
                Toast.makeText(requireContext(), R.string.student_update_processing, Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        }));

        dialog.show();
    }

    private void showImportDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_student_import, null);

        TextInputEditText etCsvData = dialogView.findViewById(R.id.etCsvData);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.student_import_title)
                .setView(dialogView)
                .setNegativeButton(R.string.student_action_cancel, null)
                .setPositiveButton(R.string.student_action_import, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String csvText = asString(etCsvData.getText());
            if (csvText.isEmpty()) {
                Toast.makeText(requireContext(), R.string.student_import_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.importFromCsv(csvText);
            Toast.makeText(requireContext(), R.string.student_import_processing, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));

        dialog.show();
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
