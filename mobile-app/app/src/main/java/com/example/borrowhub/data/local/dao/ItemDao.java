package com.example.borrowhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.borrowhub.data.local.entity.ItemEntity;

import java.util.List;

/**
 * Data Access Object for Item entity.
 * Provides methods to interact with the items table in the local database.
 */
@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);

    @Update
    void update(ItemEntity item);

    @Delete
    void delete(ItemEntity item);

    @Query("SELECT * FROM items ORDER BY name ASC")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM items WHERE id = :itemId")
    LiveData<ItemEntity> getItemById(int itemId);

    @Query("SELECT * FROM items WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    LiveData<List<ItemEntity>> searchItemsByName(String searchQuery);

    @Query("SELECT * FROM items WHERE category_id = :categoryId ORDER BY name ASC")
    LiveData<List<ItemEntity>> getItemsByCategory(int categoryId);

    @Query("SELECT * FROM items WHERE status = :status ORDER BY name ASC")
    LiveData<List<ItemEntity>> getItemsByStatus(String status);

    @Query("DELETE FROM items")
    void deleteAll();

    @Query("DELETE FROM items WHERE id = :itemId")
    void deleteById(int itemId);
}
