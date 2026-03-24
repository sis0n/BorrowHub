package com.example.borrowhub.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.ItemDao;
import com.example.borrowhub.data.local.dao.CategoryDao;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.ItemDTO;
import com.example.borrowhub.data.remote.dto.CategoryDTO;
import com.example.borrowhub.data.remote.dto.CreateItemRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateItemRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing Item and Category data.
 * Implements Network-First with Local Caching strategy.
 *
 * This repository:
 * - Fetches data from the remote API
 * - Stores data in the local Room database for offline access
 * - Returns LiveData from the local database for reactive UI updates
 */
public class ItemRepository {
    private static final String TAG = "ItemRepository";

    private final ItemDao itemDao;
    private final CategoryDao categoryDao;
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;

    /**
     * Constructor for production use.
     */
    public ItemRepository(AppDatabase database, SessionManager sessionManager) {
        this.itemDao = database.itemDao();
        this.categoryDao = database.categoryDao();
        this.apiService = ApiClient.getInstance(sessionManager).getApiService();
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Constructor for testing with dependency injection.
     */
    public ItemRepository(ItemDao itemDao, CategoryDao categoryDao,
                         ApiService apiService, SessionManager sessionManager) {
        this.itemDao = itemDao;
        this.categoryDao = categoryDao;
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ==================== Categories ====================

    /**
     * Get all categories from local database.
     * Automatically syncs with remote API in the background.
     */
    public LiveData<List<CategoryEntity>> getAllCategories() {
        // Fetch fresh data from API
        syncCategoriesFromApi();

        // Return LiveData from local database (single source of truth)
        return categoryDao.getAllCategories();
    }

    /**
     * Sync categories from remote API to local database.
     */
    private void syncCategoriesFromApi() {
        String token = sessionManager.getAuthToken();

        apiService.getCategories(token).enqueue(new Callback<ApiResponseDTO<List<CategoryDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<CategoryDTO>>> call,
                                 Response<ApiResponseDTO<List<CategoryDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CategoryDTO> categoryDTOs = response.body().getData();

                    // Convert DTOs to Entities
                    List<CategoryEntity> categories = new ArrayList<>();
                    for (CategoryDTO dto : categoryDTOs) {
                        CategoryEntity entity = new CategoryEntity(
                            dto.getId(),
                            dto.getName(),
                            dto.getCreatedAt()
                        );
                        categories.add(entity);
                    }

                    // Save to local database in background thread
                    executorService.execute(() -> {
                        categoryDao.insertAll(categories);
                        Log.d(TAG, "Categories synced successfully: " + categories.size());
                    });
                } else {
                    Log.e(TAG, "Failed to sync categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<CategoryDTO>>> call, Throwable t) {
                Log.e(TAG, "Error syncing categories from API", t);
            }
        });
    }

    // ==================== Items ====================

    /**
     * Get all items from local database.
     * Automatically syncs all pages from remote API in the background.
     */
    public LiveData<List<ItemEntity>> getAllItems() {
        // Fetch all items from API to ensure local cache is complete
        syncAllItems();

        // Return LiveData from local database (single source of truth)
        return itemDao.getAllItems();
    }

    /**
     * Sync all items from API by iterating through pages.
     */
    public void syncAllItems() {
        // Start with page 1, 50 items per page should cover the current 37 items in one go
        syncItemsFromApi(1, 50);
    }

    /**
     * Sync items from remote API to local database with pagination.
     */
    public void syncItemsFromApi(int page, int perPage) {
        String token = sessionManager.getAuthToken();

        apiService.getItems(token, page, perPage, null, null, null).enqueue(new Callback<ApiResponseDTO<List<ItemDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<ItemDTO>>> call,
                                 Response<ApiResponseDTO<List<ItemDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ItemDTO> itemDTOs = response.body().getData();

                    if (itemDTOs == null || itemDTOs.isEmpty()) {
                        return;
                    }

                    // Convert DTOs to Entities
                    List<ItemEntity> items = new ArrayList<>();
                    for (ItemDTO dto : itemDTOs) {
                        ItemEntity entity = convertDtoToEntity(dto);
                        items.add(entity);
                    }

                    // Save to local database in background thread
                    executorService.execute(() -> {
                        itemDao.insertAll(items);
                        Log.d(TAG, "Items synced successfully: Page " + page + ", Count " + items.size());
                    });
                    
                    // Note: In a more complex app, we'd check if there are more pages 
                    // and recursively call syncItemsFromApi(page + 1, perPage)
                } else {
                    Log.e(TAG, "Failed to sync items: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<ItemDTO>>> call, Throwable t) {
                Log.e(TAG, "Error syncing items from API", t);
            }
        });
    }

    /**
     * Create a new item.
     * Returns LiveData with the operation result.
     */
    public MutableLiveData<Result<ItemEntity>> createItem(CreateItemRequestDTO request) {
        MutableLiveData<Result<ItemEntity>> result = new MutableLiveData<>();
        String token = sessionManager.getAuthToken();

        apiService.createItem(token, request).enqueue(new Callback<ApiResponseDTO<ItemDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<ItemDTO>> call,
                                 Response<ApiResponseDTO<ItemDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ItemDTO dto = response.body().getData();
                    ItemEntity entity = convertDtoToEntity(dto);

                    // Save to local database
                    executorService.execute(() -> {
                        itemDao.insert(entity);
                        Log.d(TAG, "Item created successfully: " + entity.getName());
                    });

                    result.postValue(new Result<>(entity, null));
                } else {
                    String errorMsg = "Failed to create item: " + response.code();
                    Log.e(TAG, errorMsg);
                    result.postValue(new Result<>(null, errorMsg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<ItemDTO>> call, Throwable t) {
                String errorMsg = "Error creating item: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                result.postValue(new Result<>(null, errorMsg));
            }
        });

        return result;
    }

    /**
     * Update an existing item.
     * Returns LiveData with the operation result.
     */
    public MutableLiveData<Result<ItemEntity>> updateItem(int itemId, UpdateItemRequestDTO request) {
        MutableLiveData<Result<ItemEntity>> result = new MutableLiveData<>();
        String token = sessionManager.getAuthToken();

        apiService.updateItem(token, itemId, request).enqueue(new Callback<ApiResponseDTO<ItemDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<ItemDTO>> call,
                                 Response<ApiResponseDTO<ItemDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ItemDTO dto = response.body().getData();
                    ItemEntity entity = convertDtoToEntity(dto);

                    // Update local database
                    executorService.execute(() -> {
                        itemDao.update(entity);
                        Log.d(TAG, "Item updated successfully: " + entity.getName());
                    });

                    result.postValue(new Result<>(entity, null));
                } else {
                    String errorMsg = "Failed to update item: " + response.code();
                    Log.e(TAG, errorMsg);
                    result.postValue(new Result<>(null, errorMsg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<ItemDTO>> call, Throwable t) {
                String errorMsg = "Error updating item: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                result.postValue(new Result<>(null, errorMsg));
            }
        });

        return result;
    }

    /**
     * Delete an item.
     * Returns LiveData with the operation result.
     */
    public MutableLiveData<Result<Void>> deleteItem(int itemId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        String token = sessionManager.getAuthToken();

        apiService.deleteItem(token, itemId).enqueue(new Callback<ApiResponseDTO<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<Void>> call,
                                 Response<ApiResponseDTO<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Delete from local database
                    executorService.execute(() -> {
                        itemDao.deleteById(itemId);
                        Log.d(TAG, "Item deleted successfully: " + itemId);
                    });

                    result.postValue(new Result<>(null, null));
                } else {
                    String errorMsg = "Failed to delete item: " + response.code();
                    Log.e(TAG, errorMsg);
                    result.postValue(new Result<>(null, errorMsg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<Void>> call, Throwable t) {
                String errorMsg = "Error deleting item: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                result.postValue(new Result<>(null, errorMsg));
            }
        });

        return result;
    }

    // ==================== Helper Methods ====================

    /**
     * Convert ItemDTO to ItemEntity.
     */
    private ItemEntity convertDtoToEntity(ItemDTO dto) {
        String categoryName = dto.getCategory() != null ? dto.getCategory().getName() : "";
        int categoryId = dto.getCategory() != null ? dto.getCategory().getId() : 0;

        return new ItemEntity(
            dto.getId(),
            dto.getName(),
            categoryId,
            categoryName,
            dto.getTotalQuantity(),
            dto.getAvailableQuantity(),
            dto.getStatus(),
            dto.getCreatedAt(),
            dto.getUpdatedAt()
        );
    }

    /**
     * Result wrapper class for API operations.
     */
    public static class Result<T> {
        private final T data;
        private final String error;

        public Result(T data, String error) {
            this.data = data;
            this.error = error;
        }

        public T getData() {
            return data;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null;
        }
    }
}