package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;
import com.example.borrowhub.repository.LogRepository;
import com.example.borrowhub.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LogsViewModel extends AndroidViewModel {

    public static final String ACTION_ALL = "All Actions";
    public static final String TYPE_TRANSACTION = "Transaction";
    public static final String TYPE_ACTIVITY = "Activity";

    public static final String DATE_PERIOD_ALL_TIME = "All Time";

    private static final int PAGE_SIZE = 15;

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

    // Filtered (full) log lists
    private final MediatorLiveData<List<LogEntry>> transactionLogs = new MediatorLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<LogEntry>> activityLogs = new MediatorLiveData<>(new ArrayList<>());
    private LiveData<List<TransactionLogEntity>> transactionSource;
    private LiveData<List<ActivityLogEntity>> activitySource;

    // Pagination
    private final MutableLiveData<Integer> transactionCurrentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> transactionTotalPages = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> activityCurrentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> activityTotalPages = new MutableLiveData<>(1);

    // Paginated log lists exposed to fragments
    private final MediatorLiveData<List<LogEntry>> paginatedTransactionLogs = new MediatorLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<LogEntry>> paginatedActivityLogs = new MediatorLiveData<>(new ArrayList<>());

    private String transactionSearch = "";
    private String transactionAction = ACTION_ALL;
    private String transactionDatePeriod = DATE_PERIOD_ALL_TIME;
    private String activitySearch = "";
    private String activityAction = ACTION_ALL;
    private String activityDatePeriod = DATE_PERIOD_ALL_TIME;

    public LogsViewModel(@NonNull Application application) {
        this(application, new LogRepository(
                AppDatabase.getInstance(application.getApplicationContext()),
                new SessionManager(application.getApplicationContext())
        ));
    }

    LogsViewModel(@NonNull Application application, @NonNull LogRepository logRepository) {
        super(application);
        this.logRepository = logRepository;

        paginatedTransactionLogs.addSource(transactionLogs, logs -> applyTransactionPagination());
        paginatedTransactionLogs.addSource(transactionCurrentPage, page -> applyTransactionPagination());

        paginatedActivityLogs.addSource(activityLogs, logs -> applyActivityPagination());
        paginatedActivityLogs.addSource(activityCurrentPage, page -> applyActivityPagination());

        applyTransactionFilters();
        applyActivityFilters();
    }

    public LiveData<List<LogEntry>> getTransactionLogs() {
        return paginatedTransactionLogs;
    }

    public LiveData<List<LogEntry>> getActivityLogs() {
        return paginatedActivityLogs;
    }

    public LiveData<Integer> getTransactionCurrentPage() {
        return transactionCurrentPage;
    }

    public LiveData<Integer> getTransactionTotalPages() {
        return transactionTotalPages;
    }

    public LiveData<Integer> getActivityCurrentPage() {
        return activityCurrentPage;
    }

    public LiveData<Integer> getActivityTotalPages() {
        return activityTotalPages;
    }

    public void nextTransactionPage() {
        Integer current = transactionCurrentPage.getValue();
        Integer total = transactionTotalPages.getValue();
        if (current != null && total != null && current < total) {
            transactionCurrentPage.setValue(current + 1);
        }
    }

    public void previousTransactionPage() {
        Integer current = transactionCurrentPage.getValue();
        if (current != null && current > 1) {
            transactionCurrentPage.setValue(current - 1);
        }
    }

    public void nextActivityPage() {
        Integer current = activityCurrentPage.getValue();
        Integer total = activityTotalPages.getValue();
        if (current != null && total != null && current < total) {
            activityCurrentPage.setValue(current + 1);
        }
    }

    public void previousActivityPage() {
        Integer current = activityCurrentPage.getValue();
        if (current != null && current > 1) {
            activityCurrentPage.setValue(current - 1);
        }
    }

    public void setTransactionSearchQuery(String query) {
        transactionSearch = normalize(query);
        transactionCurrentPage.setValue(1);
        transactionLogs.setValue(filterTransactionLogs(
                transactionSource != null ? transactionSource.getValue() : null,
                transactionSearch
        ));
    }

    public void setActivitySearchQuery(String query) {
        activitySearch = normalize(query);
        activityCurrentPage.setValue(1);
        activityLogs.setValue(filterActivityLogs(
                activitySource != null ? activitySource.getValue() : null,
                activitySearch
        ));
    }

    public void setTransactionActionFilter(String action) {
        transactionAction = action == null || action.trim().isEmpty() ? ACTION_ALL : action.trim();
        transactionCurrentPage.setValue(1);
        applyTransactionFilters();
    }

    public void setActivityActionFilter(String action) {
        activityAction = action == null || action.trim().isEmpty() ? ACTION_ALL : action.trim();
        activityCurrentPage.setValue(1);
        applyActivityFilters();
    }

    public void setTransactionDatePeriod(String period) {
        transactionDatePeriod = period == null || period.trim().isEmpty() ? DATE_PERIOD_ALL_TIME : period.trim();
        transactionCurrentPage.setValue(1);
        applyTransactionFilters();
    }

    public void setActivityDatePeriod(String period) {
        activityDatePeriod = period == null || period.trim().isEmpty() ? DATE_PERIOD_ALL_TIME : period.trim();
        activityCurrentPage.setValue(1);
        applyActivityFilters();
    }

    public List<String> getTransactionActionOptions() {
        List<String> options = new ArrayList<>();
        options.add(ACTION_ALL);
        options.add(getApplication().getString(R.string.logs_filter_borrowed));
        options.add(getApplication().getString(R.string.logs_filter_returned));
        return Collections.unmodifiableList(options);
    }

    public List<String> getActivityActionOptions() {
        List<String> options = new ArrayList<>();
        options.add(ACTION_ALL);
        options.add(getApplication().getString(R.string.logs_filter_created));
        options.add(getApplication().getString(R.string.logs_filter_updated));
        options.add(getApplication().getString(R.string.logs_filter_deleted));
        return Collections.unmodifiableList(options);
    }

    public List<String> getDatePeriodOptions() {
        List<String> options = new ArrayList<>();
        options.add(getApplication().getString(R.string.logs_date_period_all_time));
        options.add(getApplication().getString(R.string.logs_date_period_today));
        options.add(getApplication().getString(R.string.logs_date_period_last_7_days));
        options.add(getApplication().getString(R.string.logs_date_period_last_30_days));
        options.add(getApplication().getString(R.string.logs_date_period_this_month));
        options.add(getApplication().getString(R.string.logs_date_period_custom_range));
        return Collections.unmodifiableList(options);
    }

    /**
     * Maps a UI filter label to the lowercase action string expected by the backend API.
     * String resources (logs_filter_*) are intentionally named to match backend values
     * when lowercased: "Borrowed"→"borrowed", "Returned"→"returned",
     * "Created"→"created", "Updated"→"updated", "Deleted"→"deleted".
     */
    private String toBackendAction(String uiLabel) {
        if (uiLabel == null || ACTION_ALL.equalsIgnoreCase(uiLabel)) {
            return null;
        }
        return uiLabel.trim().toLowerCase(Locale.US);
    }

    private void applyTransactionFilters() {
        String actionFilter = toBackendAction(transactionAction);
        LiveData<List<TransactionLogEntity>> source = logRepository.getTransactionLogs(actionFilter, null, null);

        if (transactionSource != null) {
            transactionLogs.removeSource(transactionSource);
        }

        transactionSource = source;
        transactionLogs.addSource(source, logs -> {
            transactionCurrentPage.setValue(1);
            transactionLogs.setValue(filterTransactionLogs(logs, transactionSearch));
        });
    }

    private void applyActivityFilters() {
        String actionFilter = toBackendAction(activityAction);
        LiveData<List<ActivityLogEntity>> source = logRepository.getActivityLogs(actionFilter, null, null);

        if (activitySource != null) {
            activityLogs.removeSource(activitySource);
        }

        activitySource = source;
        activityLogs.addSource(source, logs -> {
            activityCurrentPage.setValue(1);
            activityLogs.setValue(filterActivityLogs(logs, activitySearch));
        });
    }

    private void applyTransactionPagination() {
        List<LogEntry> all = transactionLogs.getValue();
        if (all == null) {
            all = new ArrayList<>();
        }

        int total = (int) Math.ceil((double) all.size() / PAGE_SIZE);
        if (total < 1) total = 1;
        transactionTotalPages.setValue(total);

        Integer page = transactionCurrentPage.getValue();
        if (page == null || page < 1) page = 1;
        if (page > total) {
            transactionCurrentPage.setValue(total);
            page = total;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());
        paginatedTransactionLogs.setValue(new ArrayList<>(all.subList(start, end)));
    }

    private void applyActivityPagination() {
        List<LogEntry> all = activityLogs.getValue();
        if (all == null) {
            all = new ArrayList<>();
        }

        int total = (int) Math.ceil((double) all.size() / PAGE_SIZE);
        if (total < 1) total = 1;
        activityTotalPages.setValue(total);

        Integer page = activityCurrentPage.getValue();
        if (page == null || page < 1) page = 1;
        if (page > total) {
            activityCurrentPage.setValue(total);
            page = total;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());
        paginatedActivityLogs.setValue(new ArrayList<>(all.subList(start, end)));
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
