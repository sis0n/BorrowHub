package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.DashboardStatsEntity;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.repository.DashboardRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final DashboardRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> tokenLiveData = new MutableLiveData<>();
    private final LiveData<DashboardStatsEntity> dashboardStats;
    private final LiveData<List<RecentTransactionEntity>> recentTransactions;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);

        AppDatabase db = AppDatabase.getInstance(application);
        repository = new DashboardRepository(
            ApiClient.getInstance().getApiService(),
            db.dashboardStatsDao(),
            db.recentTransactionDao()
        );

        dashboardStats = Transformations.switchMap(tokenLiveData, token -> {
            if (token != null) {
                return repository.getDashboardStats(token);
            }
            return new MutableLiveData<>();
        });

        recentTransactions = Transformations.switchMap(tokenLiveData, token -> {
            if (token != null) {
                return repository.getRecentTransactions(token);
            }
            return new MutableLiveData<>();
        });

        fetchData();
    }

    // For testing
    public DashboardViewModel(@NonNull Application application, DashboardRepository repository, SessionManager sessionManager) {
        super(application);
        this.repository = repository;
        this.sessionManager = sessionManager;

        dashboardStats = Transformations.switchMap(tokenLiveData, token -> {
            if (token != null) {
                return repository.getDashboardStats(token);
            }
            return new MutableLiveData<>();
        });

        recentTransactions = Transformations.switchMap(tokenLiveData, token -> {
            if (token != null) {
                return repository.getRecentTransactions(token);
            }
            return new MutableLiveData<>();
        });

        fetchData();
    }

    public void fetchData() {
        String token = sessionManager.getAuthToken();
        tokenLiveData.setValue(token);
    }

    public LiveData<DashboardStatsEntity> getDashboardStats() {
        return dashboardStats;
    }

    public LiveData<List<RecentTransactionEntity>> getRecentTransactions() {
        return recentTransactions;
    }
}
