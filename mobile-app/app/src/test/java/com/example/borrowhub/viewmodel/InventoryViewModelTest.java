package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.repository.ItemRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private ItemRepository repository;

    private InventoryViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(repository.getAllItems()).thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(repository.getAllCategories()).thenReturn(new MutableLiveData<>(Collections.emptyList()));

        viewModel = new InventoryViewModel(application, repository);
    }

    @Test
    public void filterBySearch_returnsMatchingItems() {
        // Arrange
        // Using constructor: ItemEntity(long id, String name, String type, String status, int totalQuantity, int availableQuantity)
        ItemEntity item1 = new ItemEntity(1L, "Laptop Acer", "Electronics", "active", 5, 5);
        ItemEntity item2 = new ItemEntity(2L, "Mouse Logitech", "Electronics", "active", 10, 10);
        MutableLiveData<List<ItemEntity>> liveData = new MutableLiveData<>(Arrays.asList(item1, item2));
        when(repository.getAllItems()).thenReturn(liveData);
        
        // Re-init so the MediatorLiveData picks up the data
        viewModel = new InventoryViewModel(application, repository);
        viewModel.getPaginatedItems().observeForever(items -> {});

        // Act
        viewModel.setSearchQuery("Laptop");

        // Assert
        List<ItemEntity> filtered = viewModel.getPaginatedItems().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Laptop Acer", filtered.get(0).name);
    }

    @Test
    public void addItem_callsRepository() {
        // Arrange
        when(repository.createItem(any())).thenReturn(new MutableLiveData<>(new ItemRepository.Result<>(new ItemEntity(), null)));

        // Act
        viewModel.addItem("Tablet", "Electronics", 5, 5, InventoryConstants.STATUS_AVAILABLE);

        // Assert
        verify(repository).createItem(any());
    }

    @Test
    public void updateItem_callsRepository() {
        // Arrange
        when(repository.updateItem(anyInt(), any())).thenReturn(new MutableLiveData<>(new ItemRepository.Result<>(new ItemEntity(), null)));

        // Act
        viewModel.updateItem(1L, "Tablet Updated", "Electronics", 7, 6, InventoryConstants.STATUS_AVAILABLE);

        // Assert
        verify(repository).updateItem(anyInt(), any());
    }

    @Test
    public void deleteItem_callsRepository() {
        // Arrange
        when(repository.deleteItem(anyInt())).thenReturn(new MutableLiveData<>(new ItemRepository.Result<>(null, null)));

        // Act
        viewModel.deleteItem(1L);

        // Assert
        verify(repository).deleteItem(anyInt());
    }
}
