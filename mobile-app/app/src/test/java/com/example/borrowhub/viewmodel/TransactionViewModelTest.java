package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.data.remote.dto.BorrowRequestDTO;
import com.example.borrowhub.data.remote.dto.StudentDTO;
import com.example.borrowhub.repository.ItemRepository;
import com.example.borrowhub.repository.StudentRepository;
import com.example.borrowhub.repository.TransactionRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private Observer<String> stringObserver;

    @Mock
    private Observer<Boolean> booleanObserver;

    private TransactionViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock ItemRepository for initial data
        when(itemRepository.getAllCategories()).thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(itemRepository.getAllItems()).thenReturn(new MutableLiveData<>(Collections.emptyList()));

        viewModel = new TransactionViewModel(application, transactionRepository, studentRepository, itemRepository);
    }

    @Test
    public void lookupStudent_validNumber_updatesStudentData() {
        // Arrange
        String studentNumber = "2024-0001";
        StudentDTO mockStudent = new StudentDTO();
        mockStudent.setId(1L);
        mockStudent.setName("John Doe");
        mockStudent.setCourse("BSCS");
        mockStudent.setStudentNumber(studentNumber);

        doAnswer(invocation -> {
            StudentRepository.OperationCallbackWithData<StudentDTO> callback = invocation.getArgument(1);
            callback.onSuccess(mockStudent);
            return null;
        }).when(studentRepository).getStudentByNumber(eq(studentNumber), any());

        // Act
        viewModel.lookupStudent(studentNumber);

        // Assert
        assertEquals("John Doe", viewModel.getStudentName().getValue());
        assertEquals("BSCS", viewModel.getCourse().getValue());
        assertTrue(viewModel.isStudentFound().getValue());
    }

    @Test
    public void lookupStudent_invalidNumber_clearsData() {
        // Arrange
        String studentNumber = "2024-XXXX";
        doAnswer(invocation -> {
            StudentRepository.OperationCallbackWithData<StudentDTO> callback = invocation.getArgument(1);
            callback.onError("Student not found");
            return null;
        }).when(studentRepository).getStudentByNumber(eq(studentNumber), any());

        // Act
        viewModel.lookupStudent(studentNumber);

        // Assert
        assertEquals("", viewModel.getStudentName().getValue());
        assertEquals("", viewModel.getCourse().getValue());
        assertFalse(viewModel.isStudentFound().getValue());
    }

    @Test
    public void submitBorrow_incompleteForm_showsError() {
        // Act
        viewModel.submitBorrow("", "", "", "");

        // Assert
        assertEquals("Please complete all borrower fields.", viewModel.getSubmitError().getValue());
    }

    @Test
    public void submitBorrow_validForm_callsRepository() {
        // Arrange
        String studentNumber = "2024-0001";
        String studentName = "John Doe";
        String course = "BSCS";
        String collateral = "ID Card";

        // Setup item rows
        viewModel.updateItemRowType(0, "Electronics");
        viewModel.updateItemRowName(0, "Laptop", 1);

        doAnswer(invocation -> {
            TransactionRepository.OperationCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(transactionRepository).borrow(any(BorrowRequestDTO.class), any());

        // Act
        viewModel.submitBorrow(studentNumber, studentName, course, collateral);

        // Assert
        verify(transactionRepository).borrow(any(BorrowRequestDTO.class), any());
        assertTrue(viewModel.isSubmitted().getValue());
        assertNull(viewModel.getSubmitError().getValue());
    }

    @Test
    public void submitBorrow_apiError_showsError() {
        // Arrange
        String studentNumber = "2024-0001";
        String studentName = "John Doe";
        String course = "BSCS";
        String collateral = "ID Card";

        // Setup item rows
        viewModel.updateItemRowType(0, "Electronics");
        viewModel.updateItemRowName(0, "Laptop", 1);

        String errorMsg = "Out of stock";
        doAnswer(invocation -> {
            TransactionRepository.OperationCallback callback = invocation.getArgument(1);
            callback.onError(errorMsg);
            return null;
        }).when(transactionRepository).borrow(any(BorrowRequestDTO.class), any());

        // Act
        viewModel.submitBorrow(studentNumber, studentName, course, collateral);

        // Assert
        assertEquals(errorMsg, viewModel.getSubmitError().getValue());
        assertFalse(viewModel.isSubmitted().getValue());
    }
}
