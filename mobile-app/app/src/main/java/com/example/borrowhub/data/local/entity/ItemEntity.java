package com.example.borrowhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public String status;
    public int totalQuantity;
    public int availableQuantity;
    @ColumnInfo(name = "category_id")
    public int categoryId;
    @ColumnInfo(name = "created_at")
    public String createdAt;
    @ColumnInfo(name = "updated_at")
    public String updatedAt;

    public ItemEntity() {
    }

    @Ignore
    public ItemEntity(long id, String name, String type, String status, int totalQuantity, int availableQuantity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.categoryId = 0;
        this.createdAt = "";
        this.updatedAt = "";
    }

    @Ignore
    public ItemEntity(long id, String name, int categoryId, String categoryName,
                      int totalQuantity, int availableQuantity, String status,
                      String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.type = categoryName;
        this.status = status;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
