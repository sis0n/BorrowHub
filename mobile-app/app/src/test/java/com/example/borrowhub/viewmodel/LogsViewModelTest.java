package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;
import com.example.borrowhub.repository.LogRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private LogRepository logRepository;

    private LogsViewModel viewModel;
    private MutableLiveData<List<TransactionLogEntity>> transactionSource;
    private MutableLiveData<List<ActivityLogEntity>> activitySource;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        transactionSource = new MutableLiveData<>();
        activitySource = new MutableLiveData<>();

        when(logRepository.getTransactionLogs(isNull(), isNull(), isNull())).thenReturn(transactionSource);
        when(logRepository.getTransactionLogs(eq("borrowed"), isNull(), isNull())).thenReturn(transactionSource);
        when(logRepository.getActivityLogs(isNull(), isNull(), isNull())).thenReturn(activitySource);
        when(logRepository.getActivityLogs(eq("deleted"), isNull(), isNull())).thenReturn(activitySource);

        viewModel = new LogsViewModel(application, logRepository);
    }

    @Test
    public void initialLoad_usesRepositorySources() {
        verify(logRepository).getTransactionLogs(isNull(), isNull(), isNull());
        verify(logRepository).getActivityLogs(isNull(), isNull(), isNull());
    }

    @Test
    public void transactionFilterByActionAndSearch_returnsMatchingLogs() {
        viewModel.getTransactionLogs().observeForever(logs -> {});

        transactionSource.setValue(Arrays.asList(
                new TransactionLogEntity(1001L, "Sarah Chen", "u1", "Sarah", "user", "Borrowed", "Borrowed Projector", "Mar 20, 2026 09:15 AM"),
                new TransactionLogEntity(1002L, "Mark Santos", "u2", "Mark", "user", "Borrowed", "Borrowed Camera", "Mar 20, 2026 10:42 AM")
        ));

        viewModel.setTransactionActionFilter("Borrowed");
        viewModel.setTransactionSearchQuery("Sarah");

        List<LogsViewModel.LogEntry> filtered = viewModel.getTransactionLogs().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        verify(logRepository).getTransactionLogs(eq("borrowed"), isNull(), isNull());
        assertEquals("Borrowed", filtered.get(0).action);
        assertEquals("Sarah Chen", filtered.get(0).actor);
    }

    @Test
    public void activityFilterByActionAndSearch_returnsMatchingLogs() {
        viewModel.getActivityLogs().observeForever(logs -> {});

        activitySource.setValue(Arrays.asList(
                new ActivityLogEntity(2001L, "System Staff", "u1", "User One", "user", "Deleted", "Deleted inventory item: Old HDMI Cable", "Mar 17, 2026 04:50 PM"),
                new ActivityLogEntity(2002L, "Admin User", "u2", "User Two", "user", "Deleted", "Deleted inventory item: Broken Mouse", "Mar 18, 2026 08:50 PM")
        ));

        viewModel.setActivityActionFilter("Deleted");
        viewModel.setActivitySearchQuery("HDMI");

        List<LogsViewModel.LogEntry> filtered = viewModel.getActivityLogs().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        verify(logRepository).getActivityLogs(eq("deleted"), isNull(), isNull());
        assertEquals("Deleted", filtered.get(0).action);
    }
}
