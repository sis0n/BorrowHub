package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
        public final String details;
        public final String timestamp;

        public LogEntry(long id, String type, String action, String actor, String details, String timestamp) {
            this.id = id;
            this.type = type;
            this.action = action;
            this.actor = actor;
            this.details = details;
            this.timestamp = timestamp;
        }
    }

    private final List<LogEntry> allLogs = seedLogs();

    private final MutableLiveData<List<LogEntry>> transactionLogs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LogEntry>> activityLogs = new MutableLiveData<>(new ArrayList<>());

    private String transactionSearch = "";
    private String transactionAction = ACTION_ALL;
    private String activitySearch = "";
    private String activityAction = ACTION_ALL;

    public LogsViewModel(@NonNull Application application) {
        super(application);
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
        applyTransactionFilters();
    }

    public void setActivitySearchQuery(String query) {
        activitySearch = normalize(query);
        applyActivityFilters();
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
        options.add("Borrowed");
        options.add("Returned");
        return Collections.unmodifiableList(options);
    }

    public List<String> getActivityActionOptions() {
        List<String> options = new ArrayList<>();
        options.add(ACTION_ALL);
        options.add("Added");
        options.add("Updated");
        options.add("Deleted");
        return Collections.unmodifiableList(options);
    }

    private void applyTransactionFilters() {
        transactionLogs.setValue(filter(TYPE_TRANSACTION, transactionSearch, transactionAction));
    }

    private void applyActivityFilters() {
        activityLogs.setValue(filter(TYPE_ACTIVITY, activitySearch, activityAction));
    }

    private List<LogEntry> filter(String type, String query, String action) {
        List<LogEntry> result = new ArrayList<>();
        String normalizedAction = action == null ? ACTION_ALL : action.trim();

        for (LogEntry entry : allLogs) {
            if (!type.equals(entry.type)) {
                continue;
            }

            if (!ACTION_ALL.equalsIgnoreCase(normalizedAction)
                    && !entry.action.equalsIgnoreCase(normalizedAction)) {
                continue;
            }

            if (!query.isEmpty() && !matchesQuery(entry, query)) {
                continue;
            }

            result.add(entry);
        }

        return result;
    }

    private boolean matchesQuery(LogEntry entry, String query) {
        return contains(entry.actor, query)
                || contains(entry.details, query)
                || contains(String.valueOf(entry.id), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.US).contains(query);
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.US);
    }

    private List<LogEntry> seedLogs() {
        List<LogEntry> seed = new ArrayList<>();

        seed.add(new LogEntry(1001, TYPE_TRANSACTION, "Borrowed", "Sarah Chen", "Borrowed Projector - Epson EB-X41", "Mar 20, 2026 09:15 AM"));
        seed.add(new LogEntry(1002, TYPE_TRANSACTION, "Returned", "Mark Santos", "Returned Camera - Canon EOS R6", "Mar 20, 2026 10:42 AM"));
        seed.add(new LogEntry(1003, TYPE_TRANSACTION, "Borrowed", "Emily Rodriguez", "Borrowed Laptop - Dell XPS 15", "Mar 19, 2026 03:10 PM"));

        seed.add(new LogEntry(2001, TYPE_ACTIVITY, "Added", "System Staff", "Added inventory item: Wireless Presenter", "Mar 19, 2026 09:00 AM"));
        seed.add(new LogEntry(2002, TYPE_ACTIVITY, "Updated", "Admin User", "Updated student profile: 2024-12345", "Mar 18, 2026 01:23 PM"));
        seed.add(new LogEntry(2003, TYPE_ACTIVITY, "Deleted", "System Staff", "Deleted inventory item: Old HDMI Cable", "Mar 17, 2026 04:50 PM"));

        return seed;
    }
}
