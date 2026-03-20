package com.example.borrowhub.data.remote.api;

import com.example.borrowhub.data.remote.dto.LoginRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginResponseDTO;

import com.example.borrowhub.data.remote.dto.DashboardStatsDTO;
import com.example.borrowhub.data.remote.dto.RecentTransactionDTO;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.ItemDTO;
import com.example.borrowhub.data.remote.dto.CategoryDTO;
import com.example.borrowhub.data.remote.dto.CreateItemRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateItemRequestDTO;
import com.example.borrowhub.data.remote.dto.StudentDTO;
import com.example.borrowhub.data.remote.dto.CreateStudentRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateStudentRequestDTO;
import com.example.borrowhub.data.remote.dto.ImportStudentsRequestDTO;
import com.example.borrowhub.data.remote.dto.UserDTO;
import com.example.borrowhub.data.remote.dto.CreateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.ResetPasswordRequestDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Path;
import com.example.borrowhub.data.remote.dto.ActivityLogDTO;
import com.example.borrowhub.data.remote.dto.TransactionLogDTO;

/**
 * Interface defining the API endpoints for the BorrowHub backend.
 */
public interface ApiService {

    @POST("api/v1/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO request);

    @POST("api/v1/logout")
    Call<Void> logout(@Header("Authorization") String token);

    // User Management
    @GET("api/v1/users")
    Call<ApiResponseDTO<List<UserDTO>>> getUsers(@Header("Authorization") String token);

    @GET("api/v1/users/{id}")
    Call<ApiResponseDTO<UserDTO>> getUser(@Header("Authorization") String token, @Path("id") int userId);

    @POST("api/v1/users")
    Call<ApiResponseDTO<UserDTO>> createUser(@Header("Authorization") String token, @Body CreateUserRequestDTO request);

    @PUT("api/v1/users/{id}")
    Call<ApiResponseDTO<UserDTO>> updateUser(@Header("Authorization") String token, @Path("id") int userId, @Body UpdateUserRequestDTO request);

    @DELETE("api/v1/users/{id}")
    Call<ApiResponseDTO<Void>> deleteUser(@Header("Authorization") String token, @Path("id") int userId);

    @POST("api/v1/users/{id}/reset-password")
    Call<ApiResponseDTO<Void>> resetUserPassword(@Header("Authorization") String token, @Path("id") int userId, @Body ResetPasswordRequestDTO request);

    @GET("api/v1/dashboard/stats")
    Call<DashboardStatsDTO> getDashboardStats(@Header("Authorization") String token);

    @GET("api/v1/dashboard/recent-transactions")
    Call<List<RecentTransactionDTO>> getRecentTransactions(@Header("Authorization") String token);

    // Inventory - Categories
    @GET("api/v1/categories")
    Call<ApiResponseDTO<List<CategoryDTO>>> getCategories(@Header("Authorization") String token);

    // Inventory - Items
    @GET("api/v1/items")
    Call<ApiResponseDTO<List<ItemDTO>>> getItems(@Header("Authorization") String token);

    @GET("api/v1/items/{id}")
    Call<ApiResponseDTO<ItemDTO>> getItem(@Header("Authorization") String token, @Path("id") int itemId);

    @POST("api/v1/items")
    Call<ApiResponseDTO<ItemDTO>> createItem(@Header("Authorization") String token, @Body CreateItemRequestDTO request);

    @PUT("api/v1/items/{id}")
    Call<ApiResponseDTO<ItemDTO>> updateItem(@Header("Authorization") String token, @Path("id") int itemId, @Body UpdateItemRequestDTO request);

    @DELETE("api/v1/items/{id}")
    Call<ApiResponseDTO<Void>> deleteItem(@Header("Authorization") String token, @Path("id") int itemId);

    // Student Management
    @GET("api/v1/students")
    Call<ApiResponseDTO<List<StudentDTO>>> getStudents(@Header("Authorization") String token);

    @GET("api/v1/students/{id}")
    Call<ApiResponseDTO<StudentDTO>> getStudent(@Header("Authorization") String token, @Path("id") long studentId);

    @POST("api/v1/students")
    Call<ApiResponseDTO<StudentDTO>> createStudent(@Header("Authorization") String token, @Body CreateStudentRequestDTO request);

    @PUT("api/v1/students/{id}")
    Call<ApiResponseDTO<StudentDTO>> updateStudent(@Header("Authorization") String token, @Path("id") long studentId, @Body UpdateStudentRequestDTO request);

    @DELETE("api/v1/students/{id}")
    Call<ApiResponseDTO<Void>> deleteStudent(@Header("Authorization") String token, @Path("id") long studentId);

    @POST("api/v1/students/import")
    Call<ApiResponseDTO<Void>> importStudents(@Header("Authorization") String token, @Body ImportStudentsRequestDTO request);

    // System Logs
    @GET("api/v1/activity-logs")
    Call<ApiResponseDTO<List<ActivityLogDTO>>> getActivityLogs(
            @Header("Authorization") String token,
            @Query("action") String action,
            @Query("target_user_id") String targetUserId,
            @Query("performed_by") String performedBy
    );

    @GET("api/v1/transaction-logs")
    Call<ApiResponseDTO<List<TransactionLogDTO>>> getTransactionLogs(
            @Header("Authorization") String token,
            @Query("action") String action,
            @Query("target_user_id") String targetUserId,
            @Query("performed_by") String performedBy
    );
}
