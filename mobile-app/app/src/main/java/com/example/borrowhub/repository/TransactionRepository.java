package com.example.borrowhub.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.BorrowRecordDTO;
import com.example.borrowhub.data.remote.dto.BorrowRequestDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing transactions (borrow/return).
 */
public class TransactionRepository {
    private static final String TAG = "TransactionRepository";

    private final ApiService apiService;
    private final SessionManager sessionManager;

    public TransactionRepository(SessionManager sessionManager) {
        this.apiService = ApiClient.getInstance(sessionManager).getApiService();
        this.sessionManager = sessionManager;
    }

    public TransactionRepository(ApiService apiService, SessionManager sessionManager) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
    }

    /**
     * Get active transactions from API.
     */
    public void getActiveTransactions(MutableLiveData<List<BorrowRecordDTO>> liveData) {
        String token = getAuthHeader();

        apiService.getActiveTransactions(token).enqueue(new Callback<ApiResponseDTO<List<BorrowRecordDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<BorrowRecordDTO>>> call,
                                   Response<ApiResponseDTO<List<BorrowRecordDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    liveData.setValue(response.body().getData());
                } else {
                    Log.e(TAG, "Failed to fetch active transactions: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<BorrowRecordDTO>>> call, Throwable t) {
                Log.e(TAG, "Error fetching active transactions", t);
            }
        });
    }

    /**
     * Process a borrow transaction.
     */
    public void borrow(BorrowRequestDTO request, OperationCallback callback) {
        String token = getAuthHeader();

        apiService.borrow(token, request).enqueue(new Callback<ApiResponseDTO<BorrowRecordDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<BorrowRecordDTO>> call,
                                   Response<ApiResponseDTO<BorrowRecordDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to process borrow: " + response.code());
                    if (callback != null) callback.onError("Failed to process borrow transaction");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<BorrowRecordDTO>> call, Throwable t) {
                Log.e(TAG, "Error processing borrow", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Process a return transaction.
     */
    public void returnItem(int transactionId, OperationCallback callback) {
        String token = getAuthHeader();

        apiService.returnItem(token, transactionId).enqueue(new Callback<ApiResponseDTO<BorrowRecordDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<BorrowRecordDTO>> call,
                                   Response<ApiResponseDTO<BorrowRecordDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to process return: " + response.code());
                    if (callback != null) callback.onError("Failed to process return transaction");
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<BorrowRecordDTO>> call, Throwable t) {
                Log.e(TAG, "Error processing return", t);
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    private String getAuthHeader() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
