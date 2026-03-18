package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.entity.ItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class InventoryViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ItemEntity>> inventoryItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ItemEntity>> filteredItems = new MutableLiveData<>(new ArrayList<>());
    private final AtomicLong nextId = new AtomicLong(1000L);

    private String searchQuery = "";
    private String normalizedSearchQuery = "";
    private String typeFilter = InventoryConstants.TYPE_ALL;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        inventoryItems.setValue(seedInventory());
        applyFilters();
    }

    public LiveData<List<ItemEntity>> getInventoryItems() {
        return inventoryItems;
    }

    public LiveData<List<ItemEntity>> getFilteredItems() {
        return filteredItems;
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim();
        normalizedSearchQuery = searchQuery.toLowerCase(Locale.US);
        applyFilters();
    }

    public void setTypeFilter(String selectedType) {
        typeFilter = (selectedType == null || selectedType.trim().isEmpty())
                ? InventoryConstants.TYPE_ALL
                : selectedType;
        applyFilters();
    }

    public void addItem(String name, String type, int totalQuantity, int availableQuantity) {
        List<ItemEntity> current = safeList(inventoryItems.getValue());
        ItemEntity newItem = new ItemEntity(
                nextId.incrementAndGet(),
                name,
                type,
                calculateStatusForAvailability(availableQuantity),
                totalQuantity,
                availableQuantity
        );
        current.add(newItem);
        inventoryItems.setValue(current);
        applyFilters();
    }

    public void updateItem(long id, String name, String type, int totalQuantity, int availableQuantity) {
        List<ItemEntity> current = safeList(inventoryItems.getValue());
        for (int i = 0; i < current.size(); i++) {
            ItemEntity item = current.get(i);
            if (item.id == id) {
                String updatedStatus = item.status;
                if (!InventoryConstants.STATUS_MAINTENANCE.equalsIgnoreCase(updatedStatus)) {
                    updatedStatus = calculateStatusForAvailability(availableQuantity);
                }
                current.set(i, new ItemEntity(id, name, type, updatedStatus, totalQuantity, availableQuantity));
                break;
            }
        }
        inventoryItems.setValue(current);
        applyFilters();
    }

    public void deleteItem(long id) {
        List<ItemEntity> current = safeList(inventoryItems.getValue());
        List<ItemEntity> updated = new ArrayList<>();
        for (ItemEntity item : current) {
            if (item.id != id) {
                updated.add(item);
            }
        }
        inventoryItems.setValue(updated);
        applyFilters();
    }

    private void applyFilters() {
        List<ItemEntity> source = safeList(inventoryItems.getValue());
        List<ItemEntity> filtered = new ArrayList<>();

        for (ItemEntity item : source) {
            boolean matchesSearch = item.name != null && item.name.toLowerCase(Locale.US).contains(normalizedSearchQuery);
            boolean matchesType = InventoryConstants.TYPE_ALL.equalsIgnoreCase(typeFilter)
                    || (item.type != null && item.type.equalsIgnoreCase(typeFilter));

            if (matchesSearch && matchesType) {
                filtered.add(item);
            }
        }

        filteredItems.setValue(filtered);
    }

    private List<ItemEntity> safeList(List<ItemEntity> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    private List<ItemEntity> seedInventory() {
        List<ItemEntity> seed = new ArrayList<>();
        seed.add(new ItemEntity(1, "Projector - Epson EB-X41", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 12, 9));
        seed.add(new ItemEntity(2, "Laptop - Dell XPS 15", InventoryConstants.TYPE_LAPTOP, InventoryConstants.STATUS_BORROWED, 20, 14));
        seed.add(new ItemEntity(3, "Camera - Canon EOS R6", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 5, 3));
        seed.add(new ItemEntity(4, "Laptop - MacBook Pro 16", InventoryConstants.TYPE_LAPTOP, InventoryConstants.STATUS_AVAILABLE, 10, 10));
        seed.add(new ItemEntity(5, "Conference Microphone", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_BORROWED, 4, 0));
        seed.add(new ItemEntity(6, "Extension Cable 10m", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 30, 28));
        seed.add(new ItemEntity(7, "Wireless Presenter", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 8, 6));
        seed.add(new ItemEntity(8, "Portable Speaker", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_MAINTENANCE, 4, 2));
        seed.add(new ItemEntity(9, "Tripod Stand", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 6, 5));
        seed.add(new ItemEntity(10, "HDMI Cable 5m", InventoryConstants.TYPE_EQUIPMENT, InventoryConstants.STATUS_AVAILABLE, 25, 25));
        return seed;
    }

    private String calculateStatusForAvailability(int availableQuantity) {
        return availableQuantity == 0
                ? InventoryConstants.STATUS_BORROWED
                : InventoryConstants.STATUS_AVAILABLE;
    }
}
