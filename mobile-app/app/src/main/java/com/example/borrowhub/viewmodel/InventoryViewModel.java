package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.data.remote.dto.CreateItemRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateItemRequestDTO;
import com.example.borrowhub.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryViewModel extends AndroidViewModel {

    private static final int PAGE_SIZE = 10;

    private final ItemRepository repository;
    private final LiveData<List<ItemEntity>> allItems;
    private final LiveData<List<CategoryEntity>> allCategories;
    
    private final MediatorLiveData<List<ItemEntity>> filteredItems = new MediatorLiveData<>();
    private final MediatorLiveData<List<ItemEntity>> paginatedItems = new MediatorLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> typeFilter = new MutableLiveData<>(InventoryConstants.TYPE_ALL);
    
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(1);

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public InventoryViewModel(@NonNull Application application) {
        this(application, new ItemRepository(AppDatabase.getInstance(application), new SessionManager(application)));
    }

    public InventoryViewModel(@NonNull Application application, ItemRepository repository) {
        super(application);
        this.repository = repository;

        allItems = repository.getAllItems();
        allCategories = repository.getAllCategories();

        // 1. Filter logic
        filteredItems.addSource(allItems, items -> {
            currentPage.setValue(1); // Reset page on data change
            applyFilters();
        });
        filteredItems.addSource(searchQuery, query -> {
            currentPage.setValue(1);
            applyFilters();
        });
        filteredItems.addSource(typeFilter, filter -> {
            currentPage.setValue(1);
            applyFilters();
        });

        // 2. Pagination logic
        paginatedItems.addSource(filteredItems, items -> applyPagination());
        paginatedItems.addSource(currentPage, page -> applyPagination());
    }

    public LiveData<List<ItemEntity>> getPaginatedItems() {
        return paginatedItems;
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return allCategories;
    }
    
    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }
    
    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query.trim());
    }

    public void setTypeFilter(String selectedType) {
        typeFilter.setValue((selectedType == null || selectedType.trim().isEmpty())
                ? InventoryConstants.TYPE_ALL
                : selectedType);
    }
    
    public void nextPage() {
        Integer current = currentPage.getValue();
        Integer total = totalPages.getValue();
        if (current != null && total != null && current < total) {
            currentPage.setValue(current + 1);
        }
    }
    
    public void previousPage() {
        Integer current = currentPage.getValue();
        if (current != null && current > 1) {
            currentPage.setValue(current - 1);
        }
    }

    private final MutableLiveData<Boolean> itemOperationSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getItemOperationSuccess() {
        return itemOperationSuccess;
    }

    public void addItem(String name, String type, int totalQuantity, int availableQuantity, String status) {
        isLoading.setValue(true);
        int categoryId = findCategoryIdByName(type);
        String backendStatus = mapStatusToBackend(status);
        
        CreateItemRequestDTO request = new CreateItemRequestDTO(name, categoryId, totalQuantity, availableQuantity, backendStatus);
        repository.createItem(request).observeForever(result -> {
            isLoading.postValue(false);
            if (result.isSuccess()) {
                itemOperationSuccess.postValue(true);
            } else {
                errorMessage.postValue(result.getError());
            }
        });
    }

    public void updateItem(long id, String name, String type, int totalQuantity, int availableQuantity, String status) {
        isLoading.setValue(true);
        int categoryId = findCategoryIdByName(type);
        String backendStatus = mapStatusToBackend(status);
        
        UpdateItemRequestDTO request = new UpdateItemRequestDTO(name, categoryId, totalQuantity, availableQuantity, backendStatus);
        repository.updateItem((int) id, request).observeForever(result -> {
            isLoading.postValue(false);
            if (result.isSuccess()) {
                itemOperationSuccess.postValue(true);
            } else {
                errorMessage.postValue(result.getError());
            }
        });
    }

    private String mapStatusToBackend(String uiStatus) {
        if (InventoryConstants.STATUS_MAINTENANCE.equalsIgnoreCase(uiStatus)) {
            return "maintenance";
        } else if (InventoryConstants.STATUS_ARCHIVED.equalsIgnoreCase(uiStatus)) {
            return "archived";
        } else {
            return "active"; // Default for Available/Borrowed
        }
    }

    public void deleteItem(long id) {
        isLoading.setValue(true);
        repository.deleteItem((int) id).observeForever(result -> {
            isLoading.postValue(false);
            if (result.isSuccess()) {
                itemOperationSuccess.postValue(true);
            } else {
                errorMessage.postValue(result.getError());
            }
        });
    }

    public void refresh() {
        repository.syncAllItems();
        repository.getAllCategories();
    }

    private void applyFilters() {
        List<ItemEntity> source = allItems.getValue();
        if (source == null) {
            filteredItems.setValue(new ArrayList<>());
            return;
        }

        String query = searchQuery.getValue();
        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.US);
        String filter = typeFilter.getValue();

        List<ItemEntity> filtered = new ArrayList<>();
        for (ItemEntity item : source) {
            boolean matchesSearch = item.name != null && item.name.toLowerCase(Locale.US).contains(normalizedQuery);
            boolean matchesType = InventoryConstants.TYPE_ALL.equalsIgnoreCase(filter)
                    || (item.type != null && item.type.equalsIgnoreCase(filter));

            if (matchesSearch && matchesType) {
                filtered.add(item);
            }
        }
        filteredItems.setValue(filtered);
    }

    private void applyPagination() {
        List<ItemEntity> filtered = filteredItems.getValue();
        if (filtered == null || filtered.isEmpty()) {
            paginatedItems.setValue(new ArrayList<>());
            totalPages.setValue(1);
            return;
        }

        int totalItems = filtered.size();
        int total = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        totalPages.setValue(total);

        Integer page = currentPage.getValue();
        if (page == null || page < 1) page = 1;
        if (page > total) page = total;

        int start = Math.max(0, (page - 1) * PAGE_SIZE);
        int end = Math.min(start + PAGE_SIZE, totalItems);

        if (start < totalItems) {
            paginatedItems.setValue(new ArrayList<>(filtered.subList(start, end)));
        } else {
            paginatedItems.setValue(new ArrayList<>());
        }
    }

    private int findCategoryIdByName(String categoryName) {
        List<CategoryEntity> categories = allCategories.getValue();
        if (categories != null) {
            for (CategoryEntity category : categories) {
                if (category.getName().equalsIgnoreCase(categoryName)) {
                    return category.getId();
                }
            }
        }
        return 0; 
    }

    private String calculateStatusForAvailability(int availableQuantity) {
        return availableQuantity == 0
                ? InventoryConstants.STATUS_BORROWED
                : InventoryConstants.STATUS_AVAILABLE;
    }
}
