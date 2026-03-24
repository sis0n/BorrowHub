package com.example.borrowhub.repository;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.BorrowRecordDTO;
import com.example.borrowhub.data.remote.dto.BorrowRequestDTO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ApiService apiService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private TransactionRepository.OperationCallback operationCallback;

    private TransactionRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionManager.getAuthToken()).thenReturn("mock_token");
        repository = new TransactionRepository(apiService, sessionManager);
    }

    @Test
    public void getActiveTransactions_success_updatesLiveData() {
        // Arrange
        MutableLiveData<List<BorrowRecordDTO>> liveData = new MutableLiveData<>();
        BorrowRecordDTO mockRecord = new BorrowRecordDTO();
        List<BorrowRecordDTO> mockList = Collections.singletonList(mockRecord);
        ApiResponseDTO<List<BorrowRecordDTO>> mockResponse = new ApiResponseDTO<>();
        mockResponse.setStatus("success");
        mockResponse.setData(mockList);

        Call<ApiResponseDTO<List<BorrowRecordDTO>>> mockCall = mock(Call.class);
        when(apiService.getActiveTransactions(anyString())).thenReturn(mockCall);

        // Act
        repository.getActiveTransactions(liveData);

        // Capture callback and invoke
        ArgumentCaptor<Callback<ApiResponseDTO<List<BorrowRecordDTO>>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        assertEquals(mockList, liveData.getValue());
    }

    @Test
    public void borrow_success_callsOnSuccess() {
        // Arrange
        BorrowRequestDTO request = new BorrowRequestDTO(1L, "2024-0001", "ID Card", Collections.emptyList());
        ApiResponseDTO<BorrowRecordDTO> mockResponse = new ApiResponseDTO<>();
        mockResponse.setStatus("success");

        Call<ApiResponseDTO<BorrowRecordDTO>> mockCall = mock(Call.class);
        when(apiService.borrow(anyString(), eq(request))).thenReturn(mockCall);

        // Act
        repository.borrow(request, operationCallback);

        // Capture callback and invoke
        ArgumentCaptor<Callback<ApiResponseDTO<BorrowRecordDTO>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        verify(operationCallback).onSuccess();
    }

    @Test
    public void returnItem_success_callsOnSuccess() {
        // Arrange
        int transactionId = 123;
        ApiResponseDTO<BorrowRecordDTO> mockResponse = new ApiResponseDTO<>();
        mockResponse.setStatus("success");

        Call<ApiResponseDTO<BorrowRecordDTO>> mockCall = mock(Call.class);
        when(apiService.returnItem(anyString(), eq(transactionId))).thenReturn(mockCall);

        // Act
        repository.returnItem(transactionId, operationCallback);

        // Capture callback and invoke
        ArgumentCaptor<Callback<ApiResponseDTO<BorrowRecordDTO>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        verify(operationCallback).onSuccess();
    }
}
