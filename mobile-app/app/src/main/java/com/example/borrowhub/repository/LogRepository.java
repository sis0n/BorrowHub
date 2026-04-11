package com.example.borrowhub.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.ActivityLogDao;
import com.example.borrowhub.data.local.dao.TransactionLogDao;
import com.example.borrowhub.data.local.entity.ActivityLogEntity;
import com.example.borrowhub.data.local.entity.TransactionLogEntity;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ActivityLogDTO;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.TransactionLogDTO;
import com.example.borrowhub.data.remote.dto.PaginatedResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogRepository {
    private static final String TAG = "LogRepository";

    private final ActivityLogDao activityLogDao;
    private final TransactionLogDao transactionLogDao;
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;

    public LogRepository(AppDatabase database, SessionManager sessionManager) {
        this.activityLogDao = database.activityLogDao();
        this.transactionLogDao = database.transactionLogDao();
        this.apiService = ApiClient.getInstance(sessionManager).getApiService();
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LogRepository(ActivityLogDao activityLogDao, TransactionLogDao transactionLogDao,
                         ApiService apiService, SessionManager sessionManager) {
        this.activityLogDao = activityLogDao;
        this.transactionLogDao = transactionLogDao;
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ActivityLogEntity>> getActivityLogs(String action, String targetUserId, String performedBy) {
        syncActivityLogsFromApi(action, targetUserId, performedBy);

        if (action != null && !action.trim().isEmpty()) {
            return activityLogDao.getLogsByAction(action);
        }
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            return activityLogDao.getLogsByTargetUserId(targetUserId);
        }
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            return activityLogDao.getLogsByPerformedBy(performedBy);
        }
        return activityLogDao.getAllLogs();
    }

    public LiveData<List<TransactionLogEntity>> getTransactionLogs(String action, String targetUserId, String performedBy) {
        syncTransactionLogsFromApi(action, targetUserId, performedBy);

        if (action != null && !action.trim().isEmpty()) {
            return transactionLogDao.getLogsByAction(action);
        }
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            return transactionLogDao.getLogsByTargetUserId(targetUserId);
        }
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            return transactionLogDao.getLogsByPerformedBy(performedBy);
        }
        return transactionLogDao.getAllLogs();
    }

    private void syncActivityLogsFromApi(String action, String targetUserId, String performedBy) {
        String token = "Bearer " + sessionManager.getAuthToken();

        apiService.getActivityLogs(token, action, targetUserId, performedBy)
                .enqueue(new Callback<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>> call,
                                           Response<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PaginatedResponseDTO<ActivityLogDTO> paginatedData = response.body().getData();
                            List<ActivityLogEntity> entities = new ArrayList<>();
                            if (paginatedData != null && paginatedData.getData() != null) {
                                for (ActivityLogDTO dto : paginatedData.getData()) {
                                    entities.add(convertActivityDtoToEntity(dto));
                                }
                            }
                            executorService.execute(() -> {
                                activityLogDao.deleteAll();
                                activityLogDao.insertAll(entities);
                                Log.d(TAG, "Activity logs synced: " + entities.size());
                            });
                        } else {
                            Log.e(TAG, "Failed to sync activity logs: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDTO<PaginatedResponseDTO<ActivityLogDTO>>> call, Throwable t) {
                        Log.e(TAG, "Error syncing activity logs from API", t);
                    }
                });
    }

    private void syncTransactionLogsFromApi(String action, String targetUserId, String performedBy) {
        String token = "Bearer " + sessionManager.getAuthToken();

        apiService.getTransactionLogs(token, action, targetUserId, performedBy)
                .enqueue(new Callback<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>> call,
                                           Response<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PaginatedResponseDTO<TransactionLogDTO> paginatedData = response.body().getData();
                            List<TransactionLogEntity> entities = new ArrayList<>();
                            if (paginatedData != null && paginatedData.getData() != null) {
                                for (TransactionLogDTO dto : paginatedData.getData()) {
                                    entities.add(convertTransactionDtoToEntity(dto));
                                }
                            }
                            executorService.execute(() -> {
                                transactionLogDao.deleteAll();
                                transactionLogDao.insertAll(entities);
                                Log.d(TAG, "Transaction logs synced: " + entities.size());
                            });
                        } else {
                            Log.e(TAG, "Failed to sync transaction logs: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDTO<PaginatedResponseDTO<TransactionLogDTO>>> call, Throwable t) {
                        Log.e(TAG, "Error syncing transaction logs from API", t);
                    }
                });
    }

    private ActivityLogEntity convertActivityDtoToEntity(ActivityLogDTO dto) {
        return new ActivityLogEntity(
                dto.getId(),
                safeString(dto.getPerformedBy()),
                safeString(dto.getTargetUserId()),
                safeString(dto.getTargetUserName()),
                safeString(dto.getTargetType()),
                safeString(dto.getAction()),
                safeString(dto.getDetails()),
                safeString(dto.getCreatedAt())
        );
    }

    private TransactionLogEntity convertTransactionDtoToEntity(TransactionLogDTO dto) {
        return new TransactionLogEntity(
                dto.getId(),
                safeString(dto.getPerformedBy()),
                safeString(dto.getTargetUserId()),
                safeString(dto.getTargetUserName()),
                safeString(dto.getTargetType()),
                safeString(dto.getAction()),
                safeString(dto.getDetails()),
                safeString(dto.getCreatedAt())
        );
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }
}
