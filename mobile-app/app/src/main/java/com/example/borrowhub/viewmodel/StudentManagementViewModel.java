package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.StudentEntity;
import com.example.borrowhub.data.remote.dto.CreateStudentRequestDTO;
import com.example.borrowhub.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentManagementViewModel extends AndroidViewModel {

    private final StudentRepository repository;
    private final LiveData<List<StudentEntity>> studentsLiveData;
    private final MutableLiveData<List<StudentEntity>> filteredStudents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> availableCourses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();
    private final Observer<List<StudentEntity>> studentsObserver;

    private List<StudentEntity> allStudents = new ArrayList<>();
    private String normalizedSearchQuery = "";

    public StudentManagementViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        SessionManager sessionManager = new SessionManager(application);
        repository = new StudentRepository(database, sessionManager);
        studentsLiveData = repository.getAllStudents();
        studentsObserver = students -> {
            allStudents = students == null ? new ArrayList<>() : students;
            applyFilters();
        };
        observeStudents();
        refreshCourses();
    }

    public LiveData<List<StudentEntity>> getFilteredStudents() {
        return filteredStudents;
    }

    public LiveData<List<String>> getAvailableCourses() {
        return availableCourses;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }

    public int getTotalStudentCount() {
        return allStudents.size();
    }

    public void clearOperationStates() {
        operationSuccess.setValue(null);
        operationError.setValue(null);
    }

    public void setSearchQuery(String query) {
        normalizedSearchQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);
        applyFilters();
    }

    public void addStudent(String studentNumber, String name, String course) {
        repository.createStudent(studentNumber.trim(), name.trim(), course.trim(), new StudentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
                refreshCourses();
            }

            @Override
            public void onError(String errorMessage) {
                operationError.postValue(errorMessage == null ? "Failed to add student" : errorMessage);
            }
        });
    }

    public void updateStudent(long id, String studentNumber, String name, String course) {
        repository.updateStudent(id, studentNumber.trim(), name.trim(), course.trim(), new StudentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
                refreshCourses();
            }

            @Override
            public void onError(String errorMessage) {
                operationError.postValue(errorMessage == null ? "Failed to update student" : errorMessage);
            }
        });
    }

    public void deleteStudent(long id) {
        repository.deleteStudent(id, new StudentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
                refreshCourses();
            }

            @Override
            public void onError(String errorMessage) {
                operationError.postValue(errorMessage == null ? "Failed to delete student" : errorMessage);
            }
        });
    }

    public void importFromCsv(String csvText) {
        List<CreateStudentRequestDTO> requests = buildImportRequests(csvText);
        if (requests.isEmpty()) {
            operationError.setValue("No valid new students found to import");
            return;
        }

        repository.importStudents(requests, new StudentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
                refreshCourses();
            }

            @Override
            public void onError(String errorMessage) {
                operationError.postValue(errorMessage == null ? "Failed to import students" : errorMessage);
            }
        });
    }

    public boolean isStudentNumberDuplicate(String studentNumber, long excludeId) {
        String normalized = studentNumber == null ? "" : studentNumber.trim().toLowerCase(Locale.US);
        for (StudentEntity student : allStudents) {
            if (student.getId() != excludeId
                    && student.getStudentNumber() != null
                    && student.getStudentNumber().trim().toLowerCase(Locale.US).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private void observeStudents() {
        studentsLiveData.observeForever(studentsObserver);
    }

    private void refreshCourses() {
        repository.refreshCoursesFromStudents(courseNames -> availableCourses.postValue(courseNames));
    }

    private void applyFilters() {
        if (normalizedSearchQuery.isEmpty()) {
            filteredStudents.setValue(new ArrayList<>(allStudents));
            return;
        }

        List<StudentEntity> filtered = new ArrayList<>();
        for (StudentEntity student : allStudents) {
            boolean matchesNumber = student.getStudentNumber() != null
                    && student.getStudentNumber().toLowerCase(Locale.US).contains(normalizedSearchQuery);
            boolean matchesName = student.getName() != null
                    && student.getName().toLowerCase(Locale.US).contains(normalizedSearchQuery);
            if (matchesNumber || matchesName) {
                filtered.add(student);
            }
        }
        filteredStudents.setValue(filtered);
    }

    private List<CreateStudentRequestDTO> buildImportRequests(String csvText) {
        List<CreateStudentRequestDTO> requests = new ArrayList<>();
        if (csvText == null || csvText.trim().isEmpty()) return requests;

        String[] lines = csvText.trim().split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 3) continue;

            String studentNumber = parts[0].trim();
            String name = parts[1].trim();
            StringBuilder courseSb = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (i > 2) courseSb.append(",");
                courseSb.append(parts[i].trim());
            }
            String course = courseSb.toString().trim();

            if (!studentNumber.isEmpty() && !name.isEmpty() && !course.isEmpty()) {
                requests.add(new CreateStudentRequestDTO(studentNumber, name, course));
            }
        }
        return requests;
    }

    @Override
    protected void onCleared() {
        studentsLiveData.removeObserver(studentsObserver);
        super.onCleared();
    }
}
