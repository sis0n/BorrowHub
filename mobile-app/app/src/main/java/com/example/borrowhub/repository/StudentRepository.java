package com.example.borrowhub.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.StudentDao;
import com.example.borrowhub.data.local.dao.CourseDao;
import com.example.borrowhub.data.local.entity.StudentEntity;
import com.example.borrowhub.data.local.entity.CourseEntity;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.StudentDTO;
import com.example.borrowhub.data.remote.dto.CreateStudentRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateStudentRequestDTO;
import com.example.borrowhub.data.remote.dto.ImportStudentsRequestDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing Student data.
 * Implements Network-First with Local Caching strategy.
 *
 * This repository:
 * - Fetches data from the remote API
 * - Stores data in the local Room database for offline access
 * - Returns LiveData from the local database for reactive UI updates
 */
public class StudentRepository {
    private static final String TAG = "StudentRepository";

    private final StudentDao studentDao;
    private final CourseDao courseDao;
    private final AppDatabase database;
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;

    /**
     * Constructor for production use.
     */
    public StudentRepository(AppDatabase database, SessionManager sessionManager) {
        this.studentDao = database.studentDao();
        this.courseDao = database.courseDao();
        this.database = database;
        this.apiService = ApiClient.getInstance(sessionManager).getApiService();
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Constructor for testing with dependency injection.
     */
    public StudentRepository(StudentDao studentDao, CourseDao courseDao,
                             AppDatabase database, ApiService apiService, SessionManager sessionManager) {
        this.studentDao = studentDao;
        this.courseDao = courseDao;
        this.database = database;
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ==================== Students ====================

    /**
     * Get all students from local database.
     * Automatically syncs with remote API in the background.
     */
    public LiveData<List<StudentEntity>> getAllStudents() {
        syncStudentsFromApi();
        return studentDao.getAllStudents();
    }

    /**
     * Get a single student by ID from local database.
     */
    public LiveData<StudentEntity> getStudentById(long studentId) {
        return studentDao.getStudentById(studentId);
    }

    /**
     * Search students by student number or name.
     */
    public LiveData<List<StudentEntity>> searchStudents(String query) {
        return studentDao.searchStudents(query);
    }

    /**
     * Create a new student via API, then sync to local database.
     */
    public void createStudent(String studentNumber, String name, String course,
                              OperationCallback callback) {
        String token = "Bearer " + sessionManager.getAuthToken();
        CreateStudentRequestDTO request = new CreateStudentRequestDTO(studentNumber, name, course);

        apiService.createStudent(token, request).enqueue(new Callback<ApiResponseDTO<StudentDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<StudentDTO>> call,
                                   Response<ApiResponseDTO<StudentDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    StudentDTO dto = response.body().getData();
                    StudentEntity entity = convertDtoToEntity(dto);
                    executorService.execute(() -> {
                        studentDao.insert(entity);
                        Log.d(TAG, "Student created: " + entity.studentNumber);
                    });
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to create student: " + response.code());
                    if (callback != null) callback.onError("Failed to create student");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<StudentDTO>> call, Throwable t) {
                Log.e(TAG, "Error creating student", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update an existing student via API, then sync to local database.
     */
    public void updateStudent(long studentId, String studentNumber, String name, String course,
                              OperationCallback callback) {
        String token = "Bearer " + sessionManager.getAuthToken();
        UpdateStudentRequestDTO request = new UpdateStudentRequestDTO(studentNumber, name, course);

        apiService.updateStudent(token, studentId, request).enqueue(new Callback<ApiResponseDTO<StudentDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<StudentDTO>> call,
                                   Response<ApiResponseDTO<StudentDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    StudentDTO dto = response.body().getData();
                    StudentEntity entity = convertDtoToEntity(dto);
                    executorService.execute(() -> {
                        studentDao.update(entity);
                        Log.d(TAG, "Student updated: " + entity.studentNumber);
                    });
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to update student: " + response.code());
                    if (callback != null) callback.onError("Failed to update student");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<StudentDTO>> call, Throwable t) {
                Log.e(TAG, "Error updating student", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Delete a student via API, then remove from local database.
     */
    public void deleteStudent(long studentId, OperationCallback callback) {
        String token = "Bearer " + sessionManager.getAuthToken();

        apiService.deleteStudent(token, studentId).enqueue(new Callback<ApiResponseDTO<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<Void>> call,
                                   Response<ApiResponseDTO<Void>> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        studentDao.deleteById(studentId);
                        Log.d(TAG, "Student deleted: id=" + studentId);
                    });
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to delete student: " + response.code());
                    if (callback != null) callback.onError("Failed to delete student");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<Void>> call, Throwable t) {
                Log.e(TAG, "Error deleting student", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Bulk import students via API.
     */
    public void importStudents(List<CreateStudentRequestDTO> students, OperationCallback callback) {
        String token = "Bearer " + sessionManager.getAuthToken();
        ImportStudentsRequestDTO request = new ImportStudentsRequestDTO(students);

        apiService.importStudents(token, request).enqueue(new Callback<ApiResponseDTO<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<Void>> call,
                                   Response<ApiResponseDTO<Void>> response) {
                if (response.isSuccessful()) {
                    syncStudentsFromApi();
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to import students: " + response.code());
                    if (callback != null) callback.onError("Failed to import students");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<Void>> call, Throwable t) {
                Log.e(TAG, "Error importing students", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Sync all students from remote API to local database.
     */
    private void syncStudentsFromApi() {
        String token = "Bearer " + sessionManager.getAuthToken();

        apiService.getStudents(token).enqueue(new Callback<ApiResponseDTO<List<StudentDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<StudentDTO>>> call,
                                   Response<ApiResponseDTO<List<StudentDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<StudentDTO> dtos = response.body().getData();
                    List<StudentEntity> entities = new ArrayList<>();
                    for (StudentDTO dto : dtos) {
                        entities.add(convertDtoToEntity(dto));
                    }
                    executorService.execute(() -> {
                        database.runInTransaction(() -> {
                            studentDao.deleteAll();
                            studentDao.insertAll(entities);
                        });
                        Log.d(TAG, "Students synced: " + entities.size());
                    });
                } else {
                    Log.e(TAG, "Failed to sync students: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<StudentDTO>>> call, Throwable t) {
                Log.e(TAG, "Error syncing students from API", t);
            }
        });
    }

    private StudentEntity convertDtoToEntity(StudentDTO dto) {
        StudentEntity entity = new StudentEntity();
        entity.id = dto.getId();
        entity.studentNumber = dto.getStudentNumber() != null ? dto.getStudentNumber() : "";
        entity.name = dto.getName() != null ? dto.getName() : "";
        entity.course = dto.getCourse() != null ? dto.getCourse() : "";
        entity.createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : "";
        entity.updatedAt = dto.getUpdatedAt() != null ? dto.getUpdatedAt() : "";
        return entity;
    }

    // ==================== Courses ====================

    /**
     * Get all available courses from local database.
     */
    public LiveData<List<CourseEntity>> getAllCourses() {
        return courseDao.getAllCourses();
    }

    public interface CoursesCallback {
        void onSuccess(List<String> courseNames);
    }

    public void refreshCoursesFromStudents(CoursesCallback callback) {
        executorService.execute(() -> {
            List<StudentEntity> students = studentDao.getAllStudentsSync();
            Set<String> uniqueCourses = new HashSet<>();
            List<CourseEntity> candidateCourses = new ArrayList<>();

            for (StudentEntity student : students) {
                String course = student.getCourse();
                if (course != null) {
                    String normalized = course.trim();
                    String normalizedKey = normalized.toLowerCase(Locale.US);
                    if (!normalized.isEmpty() && uniqueCourses.add(normalizedKey)) {
                        candidateCourses.add(new CourseEntity(normalized));
                    }
                }
            }

            List<CourseEntity> existingCourses = courseDao.getAllCoursesSync();
            Map<String, CourseEntity> existingByKey = new HashMap<>();
            for (CourseEntity existing : existingCourses) {
                if (existing.getName() != null) {
                    existingByKey.put(existing.getName().trim().toLowerCase(Locale.US), existing);
                }
            }

            List<CourseEntity> toInsert = new ArrayList<>();
            for (CourseEntity candidate : candidateCourses) {
                String key = candidate.getName().trim().toLowerCase(Locale.US);
                if (!existingByKey.containsKey(key)) {
                    toInsert.add(candidate);
                }
            }

            List<Integer> toDeleteIds = new ArrayList<>();
            for (CourseEntity existing : existingCourses) {
                String key = existing.getName() == null ? "" : existing.getName().trim().toLowerCase(Locale.US);
                if (!uniqueCourses.contains(key)) {
                    toDeleteIds.add(existing.getId());
                }
            }

            if (!toDeleteIds.isEmpty()) {
                courseDao.deleteByIds(toDeleteIds);
            }
            if (!toInsert.isEmpty()) {
                courseDao.insertAll(toInsert);
            }

            List<CourseEntity> storedCourses = courseDao.getAllCoursesSync();
            List<String> courseNames = new ArrayList<>();
            for (CourseEntity course : storedCourses) {
                if (course.getName() != null && !course.getName().trim().isEmpty()) {
                    courseNames.add(course.getName());
                }
            }

            if (callback != null) {
                callback.onSuccess(courseNames);
            }
        });
    }

    // ==================== Callback Interface ====================

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
