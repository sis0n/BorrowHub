package com.example.borrowhub.data.remote.api;

import com.example.borrowhub.data.remote.dto.LoginRequest;
import com.example.borrowhub.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interface defining the API endpoints for the BorrowHub backend.
 */
public interface ApiService {

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // TODO: Add more endpoints (items, borrow requests, etc.)
}
