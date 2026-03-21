package com.example.borrowhub.repository;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.dao.DashboardStatsDao;
import com.example.borrowhub.data.local.dao.RecentTransactionDao;
import com.example.borrowhub.data.local.entity.DashboardStatsEntity;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.DashboardStatsDTO;
import com.example.borrowhub.data.remote.dto.RecentTransactionDTO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ApiService apiService;

    @Mock
    private DashboardStatsDao dashboardStatsDao;

    @Mock
    private RecentTransactionDao recentTransactionDao;

    @Mock
    private Call<ApiResponseDTO<DashboardStatsDTO>> statsCall;

    @Mock
    private Call<ApiResponseDTO<List<RecentTransactionDTO>>> transactionsCall;

    @Mock
    private Observer<DashboardStatsEntity> statsObserver;

    @Mock
    private Observer<List<RecentTransactionEntity>> transactionsObserver;

    private DashboardRepository repository;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        repository = new DashboardRepository(apiService, dashboardStatsDao, recentTransactionDao);
    }

    @Test
    public void testGetDashboardStats_Success_UpdatesCache() {
        DashboardStatsDTO mockStats = new DashboardStatsDTO();
        ApiResponseDTO<DashboardStatsDTO> responseBody = new ApiResponseDTO<>("success", "Success", mockStats);

        MutableLiveData<DashboardStatsEntity> cachedLiveData = new MutableLiveData<>();
        when(dashboardStatsDao.getDashboardStats()).thenReturn(cachedLiveData);
        when(apiService.getDashboardStats(anyString())).thenReturn(statsCall);

        doAnswer(invocation -> {
            Callback<ApiResponseDTO<DashboardStatsDTO>> callback = invocation.getArgument(0);
            callback.onResponse(statsCall, Response.success(responseBody));
            return null;
        }).when(statsCall).enqueue(any(Callback.class));

        LiveData<DashboardStatsEntity> liveData = repository.getDashboardStats("test_token");
        liveData.observeForever(statsObserver);

        // Verify that we return the LiveData from the DAO
        assertEquals(cachedLiveData, liveData);

        // Verify that the local cache insertion logic was called
        verify(dashboardStatsDao, timeout(100)).deleteAll();
        verify(dashboardStatsDao, timeout(100)).insert(any(DashboardStatsEntity.class));
    }

    @Test
    public void testGetRecentTransactions_Success_UpdatesCache() {
        List<RecentTransactionDTO> mockTransactions = new ArrayList<>();
        RecentTransactionDTO transaction = new RecentTransactionDTO();
        mockTransactions.add(transaction);
        ApiResponseDTO<List<RecentTransactionDTO>> responseBody = new ApiResponseDTO<>("success", "Success", mockTransactions);

        MutableLiveData<List<RecentTransactionEntity>> cachedLiveData = new MutableLiveData<>();
        when(recentTransactionDao.getRecentTransactions()).thenReturn(cachedLiveData);
        when(apiService.getRecentTransactions(anyString())).thenReturn(transactionsCall);

        doAnswer(invocation -> {
            Callback<ApiResponseDTO<List<RecentTransactionDTO>>> callback = invocation.getArgument(0);
            callback.onResponse(transactionsCall, Response.success(responseBody));
            return null;
        }).when(transactionsCall).enqueue(any(Callback.class));

        LiveData<List<RecentTransactionEntity>> liveData = repository.getRecentTransactions("test_token");
        liveData.observeForever(transactionsObserver);

        // Verify that we return the LiveData from the DAO
        assertEquals(cachedLiveData, liveData);

        // Verify that the local cache insertion logic was called
        verify(recentTransactionDao, timeout(100)).deleteAll();
        verify(recentTransactionDao, timeout(100)).insertAll(any());
    }
}
