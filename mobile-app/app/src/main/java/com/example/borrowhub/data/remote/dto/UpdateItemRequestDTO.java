package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for updating an Item via API.
 * Used as request body for PUT /api/v1/items/{id}
 */
public class UpdateItemRequestDTO {
    @SerializedName("name")
    private String name;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("total_quantity")
    private int totalQuantity;

    @SerializedName("available_quantity")
    private int availableQuantity;

    @SerializedName("status")
    private String status;

    public UpdateItemRequestDTO() {
    }

    public UpdateItemRequestDTO(String name, int categoryId, int totalQuantity,
                                int availableQuantity, String status) {
        this.name = name;
        this.categoryId = categoryId;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
