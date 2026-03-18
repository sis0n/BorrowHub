package com.example.borrowhub.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API Response wrapper used by the Laravel backend.
 * Wraps the actual data with status and message fields.
 */
public class ApiResponseDTO<T> {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }
}
