package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.borrowhub.data.local.entity.ItemEntity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InventoryViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    private InventoryViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new InventoryViewModel(application);
    }

    @Test
    public void initialData_isSeeded() {
        List<ItemEntity> items = viewModel.getInventoryItems().getValue();
        assertNotNull(items);
        assertEquals(10, items.size());
    }

    @Test
    public void filterBySearch_returnsMatchingItems() {
        viewModel.setSearchQuery("Laptop");

        List<ItemEntity> filtered = viewModel.getFilteredItems().getValue();
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
    }

    @Test
    public void addUpdateDeleteItem_updatesInventoryList() {
        List<ItemEntity> initialItems = viewModel.getInventoryItems().getValue();
        int initialSize = initialItems == null ? 0 : initialItems.size();

        viewModel.addItem("Tablet - Samsung", "Equipment", 5, 5);
        List<ItemEntity> afterAdd = viewModel.getInventoryItems().getValue();
        assertNotNull(afterAdd);
        assertEquals(initialSize + 1, afterAdd.size());

        ItemEntity added = afterAdd.get(afterAdd.size() - 1);
        viewModel.updateItem(added.id, "Tablet - Samsung Updated", "Equipment", 7, 6);
        List<ItemEntity> afterUpdate = viewModel.getInventoryItems().getValue();
        assertNotNull(afterUpdate);
        assertEquals("Tablet - Samsung Updated", afterUpdate.get(afterUpdate.size() - 1).name);

        viewModel.deleteItem(added.id);
        List<ItemEntity> afterDelete = viewModel.getInventoryItems().getValue();
        assertNotNull(afterDelete);
        assertEquals(initialSize, afterDelete.size());
    }
}
