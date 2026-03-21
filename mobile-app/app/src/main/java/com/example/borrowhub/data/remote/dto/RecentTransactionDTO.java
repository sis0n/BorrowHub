package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecentTransactionDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("status")
    private String status;

    @SerializedName("borrowed_at")
    private String borrowedAt;

    @SerializedName("items")
    private List<ItemDTO> items;

    @SerializedName("student")
    private StudentDTO student;

    // Getters
    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getBorrowedAt() {
        return borrowedAt;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public StudentDTO getStudent() {
        return student;
    }
}
