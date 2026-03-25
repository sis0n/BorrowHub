package com.example.borrowhub.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.ActivityLogDao;
import com.example.borrowhub.data.local.dao.TransactionLogDao;
import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ActivityLogDTO;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.TransactionLogDTO;
import com.example.borrowhub.data.remote.dto.PaginatedResponseDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class LogRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ActivityLogDao activityLogDao;

    @Mock
    private TransactionLogDao transactionLogDao;

    @Mock
    private ApiService apiService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Call<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>> activityCall;

    @Mock
    private Call<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>> transactionCall;

    private LogRepository repository;

    @Before
    public void setup() {
        repository = new LogRepository(activityLogDao, transactionLogDao, apiService, sessionManager);
        when(sessionManager.getAuthToken()).thenReturn("test_token");
    }

    @Test
    public void getActivityLogs_withAction_filterUsesDaoAndCachesResponse() {
        String json = "{\"status\":\"success\",\"message\":\"ok\",\"data\":{\"current_page\":1,\"data\":[{\"id\":1,\"performed_by\":\"Staff (Maria)\",\"target_user_id\":\"STU123\",\"target_user_name\":\"Lisa\",\"action\":\"Added\",\"details\":\"New item\",\"created_at\":\"2026-03-20T10:00:00Z\"}],\"last_page\":1,\"total\":1}}";
        Type responseType = new TypeToken<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>>() {}.getType();
        ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>> apiResponse = new Gson().fromJson(json, responseType);

        MutableLiveData<List<ActivityLogEntity>> cachedLiveData = new MutableLiveData<>();
        when(activityLogDao.getLogsByAction("Added")).thenReturn(cachedLiveData);
        when(apiService.getActivityLogs(anyString(), anyString(), any(), any())).thenReturn(activityCall);

        doAnswer(invocation -> {
            Callback<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>> callback = invocation.getArgument(0);
            callback.onResponse(activityCall, Response.success(apiResponse));
            return null;
        }).when(activityCall).enqueue(any(Callback.class));

        LiveData<List<ActivityLogEntity>> liveData = repository.getActivityLogs("Added", null, null);

        assertEquals(cachedLiveData, liveData);
        verify(apiService).getActivityLogs("Bearer test_token", "Added", null, null);
        verify(activityLogDao, timeout(200)).deleteAll();

        ArgumentCaptor<List<ActivityLogEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(activityLogDao, timeout(200)).insertAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        ActivityLogEntity entity = captor.getValue().get(0);
        assertEquals("Staff (Maria)", entity.getPerformedBy());
        assertEquals("STU123", entity.getTargetUserId());
        assertEquals("Lisa", entity.getTargetUserName());
    }

    @Test
    public void getTransactionLogs_noFilters_usesAllLogsDaoAndCachesResponse() {
        String json = "{\"status\":\"success\",\"message\":\"ok\",\"data\":{\"current_page\":1,\"data\":[{\"id\":11,\"performed_by\":\"Staff (Ana)\",\"target_user_id\":\"EMP123\",\"target_user_name\":\"James\",\"action\":\"Borrowed\",\"details\":\"Laptop\",\"created_at\":\"2026-03-20T12:00:00Z\"}],\"last_page\":1,\"total\":1}}";
        Type responseType = new TypeToken<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>>() {}.getType();
        ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>> apiResponse = new Gson().fromJson(json, responseType);

        MutableLiveData<List<TransactionLogEntity>> cachedLiveData = new MutableLiveData<>();
        when(transactionLogDao.getAllLogs()).thenReturn(cachedLiveData);
        when(apiService.getTransactionLogs(anyString(), any(), any(), any())).thenReturn(transactionCall);

        doAnswer(invocation -> {
            Callback<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>> callback = invocation.getArgument(0);
            callback.onResponse(transactionCall, Response.success(apiResponse));
            return null;
        }).when(transactionCall).enqueue(any(Callback.class));

        LiveData<List<TransactionLogEntity>> liveData = repository.getTransactionLogs(null, null, null);

        assertEquals(cachedLiveData, liveData);
        verify(apiService).getTransactionLogs("Bearer test_token", null, null, null);
        verify(transactionLogDao, timeout(200)).deleteAll();

        ArgumentCaptor<List<TransactionLogEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(transactionLogDao, timeout(200)).insertAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        TransactionLogEntity entity = captor.getValue().get(0);
        assertEquals("Staff (Ana)", entity.getPerformedBy());
        assertEquals("Borrowed", entity.getAction());
    }

    @Test
    public void apiResponseParsing_activityAndTransactionDtos_mapFields() {
        String activityJson = "{\"status\":\"success\",\"message\":\"ok\",\"data\":{\"current_page\":1,\"data\":[{\"id\":2,\"performed_by\":\"Admin (John)\",\"target_user_id\":\"SYSTEM\",\"target_user_name\":\"Admin\",\"action\":\"Updated\",\"details\":\"Status change\",\"created_at\":\"2026-03-20T13:00:00Z\"}]}}";
        String transactionJson = "{\"status\":\"success\",\"message\":\"ok\",\"data\":{\"current_page\":1,\"data\":[{\"id\":3,\"performed_by\":\"Staff (Maria)\",\"target_user_id\":\"STU300\",\"target_user_name\":\"Alice\",\"action\":\"Returned\",\"details\":\"Projector\",\"created_at\":\"2026-03-20T14:00:00Z\"}]}}";

        Type activityType = new TypeToken<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>>() {}.getType();
        Type transactionType = new TypeToken<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>>() {}.getType();

        ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>> activityResponse = new Gson().fromJson(activityJson, activityType);
        ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>> transactionResponse = new Gson().fromJson(transactionJson, transactionType);

        assertEquals(true, activityResponse.isSuccess());
        assertEquals("Admin (John)", activityResponse.getData().getData().get(0).getPerformedBy());
        assertEquals("SYSTEM", activityResponse.getData().getData().get(0).getTargetUserId());

        assertEquals(true, transactionResponse.isSuccess());
        assertEquals("Staff (Maria)", transactionResponse.getData().getData().get(0).getPerformedBy());
        assertEquals("Returned", transactionResponse.getData().getData().get(0).getAction());
    }
}
