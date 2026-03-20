package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LogsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    private LogsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LogsViewModel(application);
    }

    @Test
    public void initialData_isSeededByType() {
        List<LogsViewModel.LogEntry> transactionLogs = viewModel.getTransactionLogs().getValue();
        List<LogsViewModel.LogEntry> activityLogs = viewModel.getActivityLogs().getValue();

        assertNotNull(transactionLogs);
        assertNotNull(activityLogs);
        assertEquals(3, transactionLogs.size());
        assertEquals(3, activityLogs.size());
    }

    @Test
    public void transactionFilterByActionAndSearch_returnsMatchingLogs() {
        viewModel.setTransactionActionFilter("Borrowed");
        viewModel.setTransactionSearchQuery("Sarah");

        List<LogsViewModel.LogEntry> filtered = viewModel.getTransactionLogs().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Borrowed", filtered.get(0).action);
        assertEquals("Sarah Chen", filtered.get(0).actor);
    }

    @Test
    public void activityFilterByActionAndSearch_returnsMatchingLogs() {
        viewModel.setActivityActionFilter("Deleted");
        viewModel.setActivitySearchQuery("HDMI");

        List<LogsViewModel.LogEntry> filtered = viewModel.getActivityLogs().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Deleted", filtered.get(0).action);
    }
}
