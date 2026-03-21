package com.example.borrowhub.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.dao.DashboardStatsDao;
import com.example.borrowhub.data.local.dao.RecentTransactionDao;
import com.example.borrowhub.data.local.entity.DashboardStatsEntity;
import com.example.borrowhub.data.local.entity.RecentTransactionEntity;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.DashboardStatsDTO;
import com.example.borrowhub.data.remote.dto.ItemDTO;
import com.example.borrowhub.data.remote.dto.RecentTransactionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {

    private final ApiService apiService;
    private final DashboardStatsDao dashboardStatsDao;
    private final RecentTransactionDao recentTransactionDao;
    private final ExecutorService executorService;

    public DashboardRepository(ApiService apiService, DashboardStatsDao dashboardStatsDao, RecentTransactionDao recentTransactionDao) {
        this.apiService = apiService;
        this.dashboardStatsDao = dashboardStatsDao;
        this.recentTransactionDao = recentTransactionDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<DashboardStatsEntity> getDashboardStats(String token) {
        apiService.getDashboardStats(token).enqueue(new Callback<ApiResponseDTO<DashboardStatsDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<DashboardStatsDTO>> call, Response<ApiResponseDTO<DashboardStatsDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    DashboardStatsDTO dto = response.body().getData();
                    executorService.execute(() -> {
                        DashboardStatsEntity entity = new DashboardStatsEntity(
                            dto.getTotalItems(),
                            dto.getCurrentlyBorrowed(),
                            dto.getAvailableNow(),
                            dto.getDueToday()
                        );
                        dashboardStatsDao.deleteAll();
                        dashboardStatsDao.insert(entity);
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<DashboardStatsDTO>> call, Throwable t) {
            }
        });

        return dashboardStatsDao.getDashboardStats();
    }

    public LiveData<List<RecentTransactionEntity>> getRecentTransactions(String token) {
        apiService.getRecentTransactions(token).enqueue(new Callback<ApiResponseDTO<List<RecentTransactionDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<RecentTransactionDTO>>> call, Response<ApiResponseDTO<List<RecentTransactionDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<RecentTransactionDTO> dtoList = response.body().getData();
                    executorService.execute(() -> {
                        List<RecentTransactionEntity> entityList = new ArrayList<>();
                        for (RecentTransactionDTO dto : dtoList) {
                            String itemName = "Multiple Items";
                            if (dto.getItems() != null && !dto.getItems().isEmpty()) {
                                if (dto.getItems().size() == 1) {
                                    itemName = dto.getItems().get(0).getName();
                                } else {
                                    itemName = dto.getItems().get(0).getName() + " +" + (dto.getItems().size() - 1);
                                }
                            }
                            String borrowerName = "Unknown";
                            if (dto.getStudent() != null) {
                                borrowerName = dto.getStudent().getName();
                            }
                            entityList.add(new RecentTransactionEntity(
                                dto.getId(),
                                itemName,
                                borrowerName,
                                dto.getStatus(),
                                dto.getBorrowedAt()
                            ));
                        }
                        recentTransactionDao.deleteAll();
                        recentTransactionDao.insertAll(entityList);
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<RecentTransactionDTO>>> call, Throwable t) {
            }
        });

        return recentTransactionDao.getRecentTransactions();
    }
}
