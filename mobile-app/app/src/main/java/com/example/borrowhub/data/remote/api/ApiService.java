package com.example.borrowhub.data.remote.api;

import com.example.borrowhub.data.remote.dto.LoginRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

/**
 * Interface defining the API endpoints for the BorrowHub backend.
 */
public interface ApiService {

    @POST("api/v1/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO request);

    @POST("api/v1/logout")
    Call<Void> logout(@Header("Authorization") String token);

    // TODO: Add more endpoints (items, borrow requests, etc.)
}
