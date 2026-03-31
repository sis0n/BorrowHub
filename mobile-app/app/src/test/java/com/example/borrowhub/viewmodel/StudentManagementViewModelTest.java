package com.example.borrowhub.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.entity.StudentEntity;
import com.example.borrowhub.repository.StudentRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentManagementViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private StudentRepository repository;

    private final MutableLiveData<List<StudentEntity>> studentsLiveData = new MutableLiveData<>();

    private StudentManagementViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(repository.getAllStudents()).thenReturn(studentsLiveData);
        doAnswer(invocation -> {
            StudentRepository.CoursesCallback callback = invocation.getArgument(0);
            callback.onSuccess(Arrays.asList("Computer Science", "Information Systems"));
            return null;
        }).when(repository).refreshCoursesFromApi(any());

        // Mock course ID lookup
        when(repository.getCourseIdByNameSync("Computer Science")).thenReturn(101);
        when(repository.getCourseIdByNameSync("Information Systems")).thenReturn(102);

        studentsLiveData.setValue(Collections.emptyList());

        viewModel = new StudentManagementViewModel(application, repository);
    }

    @Test
    public void init_observesStudentsAndRefreshesCourses() {
        studentsLiveData.setValue(Arrays.asList(
                new StudentEntity(1L, "STU001", "Alice", "Computer Science"),
                new StudentEntity(2L, "STU002", "Bob", "Information Systems")
        ));

        assertEquals(2, viewModel.getTotalStudentCount());
        assertEquals(2, viewModel.getFilteredStudents().getValue().size());
        assertEquals(2, viewModel.getAvailableCourses().getValue().size());
        verify(repository).getAllStudents();
        verify(repository).refreshCoursesFromApi(any());
    }

    @Test
    public void addStudent_successAfterError_clearsErrorStateAndResyncs() throws InterruptedException {
        AtomicInteger invocationCounter = new AtomicInteger(0);
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(3);
            if (invocationCounter.getAndIncrement() == 0) {
                callback.onError("Old error");
            } else {
                callback.onSuccess();
            }
            return null;
        }).when(repository).createStudent(any(), any(), anyInt(), any());

        viewModel.addStudent(" STU003 ", " Charlie ", " Computer Science ");
        Thread.sleep(100);
        viewModel.addStudent(" STU003 ", " Charlie ", " Computer Science ");
        Thread.sleep(100);

        verify(repository, org.mockito.Mockito.times(2)).createStudent(eq("STU003"), eq("Charlie"), eq(101), any());
        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository).refreshStudentsFromApi();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertEquals(Boolean.TRUE, viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());
    }

    @Test
    public void addStudent_errorAfterSuccess_clearsSuccessStateAndSetsDefaultError() throws InterruptedException {
        AtomicInteger invocationCounter = new AtomicInteger(0);
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(3);
            if (invocationCounter.getAndIncrement() == 0) {
                callback.onSuccess();
            } else {
                callback.onError(null);
            }
            return null;
        }).when(repository).createStudent(any(), any(), anyInt(), any());

        viewModel.addStudent("STU003", "Charlie", "Computer Science");
        Thread.sleep(100);
        viewModel.addStudent("STU003", "Charlie", "Computer Science");
        Thread.sleep(100);

        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertNull(viewModel.getOperationSuccess().getValue());
        assertEquals("Failed to add student", viewModel.getOperationError().getValue());
    }

    @Test
    public void updateStudent_success_setsSuccessAndResyncs() throws InterruptedException {
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(4);
            callback.onSuccess();
            return null;
        }).when(repository).updateStudent(anyLong(), any(), any(), anyInt(), any());

        viewModel.updateStudent(5L, " STU005 ", " Diana ", " Information Systems ");
        Thread.sleep(100);

        verify(repository).updateStudent(eq(5L), eq("STU005"), eq("Diana"), eq(102), any());
        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository).refreshStudentsFromApi();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertEquals(Boolean.TRUE, viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());
    }

    @Test
    public void updateStudent_error_setsRepositoryErrorMessage() throws InterruptedException {
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(4);
            callback.onError("Backend rejected update");
            return null;
        }).when(repository).updateStudent(anyLong(), any(), any(), anyInt(), any());

        viewModel.updateStudent(5L, "STU005", "Diana", "Information Systems");
        Thread.sleep(100);

        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertNull(viewModel.getOperationSuccess().getValue());
        assertEquals("Backend rejected update", viewModel.getOperationError().getValue());
    }

    @Test
    public void deleteStudent_success_setsSuccessAndResyncs() {
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(repository).deleteStudent(anyLong(), any());

        viewModel.deleteStudent(7L);

        verify(repository).deleteStudent(eq(7L), any());
        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository).refreshStudentsFromApi();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertEquals(Boolean.TRUE, viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());
    }

    @Test
    public void deleteStudent_error_setsDefaultErrorMessage() {
        doAnswer(invocation -> {
            StudentRepository.OperationCallback callback = invocation.getArgument(1);
            callback.onError(null);
            return null;
        }).when(repository).deleteStudent(anyLong(), any());

        viewModel.deleteStudent(7L);

        verify(repository).getAllStudents();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertNull(viewModel.getOperationSuccess().getValue());
        assertEquals("Failed to delete student", viewModel.getOperationError().getValue());
    }

    @Test
    public void importFromCsv_emptyInput_setsValidationError() throws InterruptedException {
        viewModel.importFromCsv(" \n ");
        Thread.sleep(100);

        verify(repository, atLeastOnce()).getAllStudents();
        verify(repository, atLeastOnce()).refreshCoursesFromApi(any());
        assertEquals("No valid new students found to import", viewModel.getOperationError().getValue());
    }

    @Test
    public void setSearchQuery_filtersStudentsByNameOrNumber() {
        studentsLiveData.setValue(Arrays.asList(
                new StudentEntity(1L, "STU101", "Eve", "Computer Science"),
                new StudentEntity(2L, "STU202", "Frank", "Information Systems")
        ));

        viewModel.setSearchQuery("202");
        assertEquals(1, viewModel.getFilteredStudents().getValue().size());
        assertEquals("STU202", viewModel.getFilteredStudents().getValue().get(0).getStudentNumber());

        viewModel.setSearchQuery("eve");
        assertEquals(1, viewModel.getFilteredStudents().getValue().size());
        assertEquals("Eve", viewModel.getFilteredStudents().getValue().get(0).getName());

        viewModel.setSearchQuery("");
        assertEquals(2, viewModel.getFilteredStudents().getValue().size());
    }
}
