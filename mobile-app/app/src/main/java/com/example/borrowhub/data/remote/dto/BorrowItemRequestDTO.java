package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for an item in a borrow request.
 */
public class BorrowItemRequestDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("quantity")
    private int quantity;

    public BorrowItemRequestDTO(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
