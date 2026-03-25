package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;
import com.example.borrowhub.repository.LogRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LogsViewModel extends AndroidViewModel {

    public static final String ACTION_ALL = "All Actions";
    public static final String TYPE_TRANSACTION = "Transaction";
    public static final String TYPE_ACTIVITY = "Activity";

    public static class LogEntry {
        public final long id;
        public final String type;
        public final String action;
        public final String actor;
        public final String target;
        public final String details;
        public final String timestamp;

        public LogEntry(long id, String type, String action, String actor, String target, String details, String timestamp) {
            this.id = id;
            this.type = type;
            this.action = action;
            this.actor = actor;
            this.target = target;
            this.details = details;
            this.timestamp = timestamp;
        }
    }

    private final LogRepository logRepository;
    private final MediatorLiveData<List<LogEntry>> transactionLogs = new MediatorLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<LogEntry>> activityLogs = new MediatorLiveData<>(new ArrayList<>());
    private LiveData<List<TransactionLogEntity>> transactionSource;
    private LiveData<List<ActivityLogEntity>> activitySource;

    private String transactionSearch = "";
    private String transactionAction = ACTION_ALL;
    private String activitySearch = "";
    private String activityAction = ACTION_ALL;

    public LogsViewModel(@NonNull Application application) {
        this(application, new LogRepository(
                AppDatabase.getInstance(application.getApplicationContext()),
                new SessionManager(application.getApplicationContext())
        ));
    }

    LogsViewModel(@NonNull Application application, @NonNull LogRepository logRepository) {
        super(application);
        this.logRepository = logRepository;
        applyTransactionFilters();
        applyActivityFilters();
    }

    public LiveData<List<LogEntry>> getTransactionLogs() {
        return transactionLogs;
    }

    public LiveData<List<LogEntry>> getActivityLogs() {
        return activityLogs;
    }

    public void setTransactionSearchQuery(String query) {
        transactionSearch = normalize(query);
        transactionLogs.setValue(filterTransactionLogs(
                transactionSource != null ? transactionSource.getValue() : null,
                transactionSearch
        ));
    }

    public void setActivitySearchQuery(String query) {
        activitySearch = normalize(query);
        activityLogs.setValue(filterActivityLogs(
                activitySource != null ? activitySource.getValue() : null,
                activitySearch
        ));
    }

    public void setTransactionActionFilter(String action) {
        transactionAction = action == null || action.trim().isEmpty() ? ACTION_ALL : action.trim();
        applyTransactionFilters();
    }

    public void setActivityActionFilter(String action) {
        activityAction = action == null || action.trim().isEmpty() ? ACTION_ALL : action.trim();
        applyActivityFilters();
    }

    public List<String> getTransactionActionOptions() {
        List<String> options = new ArrayList<>();
        options.add(ACTION_ALL);
        options.add("Items Borrowed");
        options.add("Items Returned");
        return Collections.unmodifiableList(options);
    }

    public List<String> getActivityActionOptions() {
        List<String> options = new ArrayList<>();
        options.add(ACTION_ALL);
        options.add("Item Added");
        options.add("Item Updated");
        options.add("Item Deleted");
        return Collections.unmodifiableList(options);
    }

    private void applyTransactionFilters() {
        String actionFilter = ACTION_ALL.equalsIgnoreCase(transactionAction) ? null : transactionAction;
        LiveData<List<TransactionLogEntity>> source = logRepository.getTransactionLogs(actionFilter, null, null);

        if (transactionSource != null) {
            transactionLogs.removeSource(transactionSource);
        }

        transactionSource = source;
        transactionLogs.addSource(source, logs -> transactionLogs.setValue(filterTransactionLogs(logs, transactionSearch)));
    }

    private void applyActivityFilters() {
        String actionFilter = ACTION_ALL.equalsIgnoreCase(activityAction) ? null : activityAction;
        LiveData<List<ActivityLogEntity>> source = logRepository.getActivityLogs(actionFilter, null, null);

        if (activitySource != null) {
            activityLogs.removeSource(activitySource);
        }

        activitySource = source;
        activityLogs.addSource(source, logs -> activityLogs.setValue(filterActivityLogs(logs, activitySearch)));
    }

    private List<LogEntry> filterTransactionLogs(List<TransactionLogEntity> logs, String query) {
        List<LogEntry> result = new ArrayList<>();
        if (logs == null) {
            return result;
        }

        for (TransactionLogEntity entity : logs) {
            LogEntry entry = new LogEntry(
                    entity.getId(),
                    TYPE_TRANSACTION,
                    entity.getAction(),
                    entity.getPerformedBy(),
                    entity.getTargetUserName(),
                    entity.getDetails(),
                    entity.getCreatedAt()
            );
            if (!query.isEmpty() && !matchesQuery(entry, query)) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }

    private List<LogEntry> filterActivityLogs(List<ActivityLogEntity> logs, String query) {
        List<LogEntry> result = new ArrayList<>();
        if (logs == null) {
            return result;
        }

        for (ActivityLogEntity entity : logs) {
            LogEntry entry = new LogEntry(
                    entity.getId(),
                    TYPE_ACTIVITY,
                    entity.getAction(),
                    entity.getPerformedBy(),
                    entity.getTargetUserName(),
                    entity.getDetails(),
                    entity.getCreatedAt()
            );
            if (!query.isEmpty() && !matchesQuery(entry, query)) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }

    private boolean matchesQuery(LogEntry entry, String query) {
        return contains(entry.actor, query)
                || contains(entry.target, query)
                || contains(entry.details, query)
                || contains(String.valueOf(entry.id), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.US).contains(query);
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.US);
    }
}
