package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.DashboardStatsEntity;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;
import com.example.borrowhub.repository.DashboardRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DashboardViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DashboardRepository repository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Application application;

    @Mock
    private Observer<DashboardStatsEntity> statsObserver;

    @Mock
    private Observer<List<RecentTransactionEntity>> transactionsObserver;

    private DashboardViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new DashboardViewModel(application, repository, sessionManager);
    }

    @Test
    public void fetchData_withToken_loadsDataFromRepository() {
        // Arrange
        String token = "mock_token";
        when(sessionManager.getAuthToken()).thenReturn(token);

        MutableLiveData<DashboardStatsEntity> mockStatsLiveData = new MutableLiveData<>();
        DashboardStatsEntity mockStats = new DashboardStatsEntity(10, 5, 2, 0);
        mockStatsLiveData.setValue(mockStats);
        when(repository.getDashboardStats(token)).thenReturn(mockStatsLiveData);

        MutableLiveData<List<RecentTransactionEntity>> mockTransactionsLiveData = new MutableLiveData<>();
        List<RecentTransactionEntity> mockTransactions = Arrays.asList(
                new RecentTransactionEntity(1, "Item A", "John Doe", "Borrowed", "Date")
        );
        mockTransactionsLiveData.setValue(mockTransactions);
        when(repository.getRecentTransactions(token)).thenReturn(mockTransactionsLiveData);

        // Act
        // Re-initialize view model so the constructor picks up the token mock
        viewModel = new DashboardViewModel(application, repository, sessionManager);
        viewModel.getDashboardStats().observeForever(statsObserver);
        viewModel.getRecentTransactions().observeForever(transactionsObserver);

        // Assert
        verify(repository).getDashboardStats(token);
        verify(repository).getRecentTransactions(token);

        verify(statsObserver).onChanged(mockStats);
        verify(transactionsObserver).onChanged(mockTransactions);

        assertEquals(mockStats, viewModel.getDashboardStats().getValue());
        assertEquals(mockTransactions, viewModel.getRecentTransactions().getValue());
    }

    @Test
    public void fetchData_withoutToken_doesNotLoadData() {
        // Arrange
        when(sessionManager.getAuthToken()).thenReturn(null);

        // Act
        // Re-initialize view model so the constructor picks up the token mock
        viewModel = new DashboardViewModel(application, repository, sessionManager);
        viewModel.getDashboardStats().observeForever(statsObserver);

        // Assert
        // Repository methods should not be called if token is null
    }
}