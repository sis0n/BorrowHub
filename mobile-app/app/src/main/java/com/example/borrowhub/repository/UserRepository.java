package com.example.borrowhub.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.UserDao;
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.data.remote.ApiClient;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.CreateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginResponseDTO;
import com.example.borrowhub.data.remote.dto.ResetPasswordRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final UserDao userDao;
    private final AppDatabase database;
    private final ExecutorService executorService;

    public UserRepository(Application application) {
        this.sessionManager = new SessionManager(application);
        this.apiService = ApiClient.getInstance(this.sessionManager).getApiService();
        this.database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public UserRepository(ApiService apiService, SessionManager sessionManager, UserDao userDao, AppDatabase database) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.userDao = userDao;
        this.database = database;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getUser() {
        return userDao.getUser();
    }

    public LiveData<List<User>> getAllUsers() {
        syncUsersFromApi();
        return userDao.getAllUsers();
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<Boolean> login(String username, String password) {
        MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
        
        LoginRequestDTO request = new LoginRequestDTO(username, password);
        apiService.login(request).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                Log.d(TAG, "Login Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponseDTO.Data data = response.body().getData();
                    sessionManager.saveAuthToken("Bearer " + data.getToken());
                    
                    UserDTO userDto = data.getUser();
                    if (userDto != null) {
                        User user = convertDtoToEntity(userDto);
                        executorService.execute(() -> userDao.insertUser(user));
                    }
                    loginResult.postValue(true);
                } else {
                    loginResult.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                Log.e(TAG, "Login Failure: " + t.getMessage(), t);
                loginResult.postValue(false);
            }
        });

        return loginResult;
    }

    public LiveData<Boolean> logout() {
        MutableLiveData<Boolean> logoutResult = new MutableLiveData<>();

        String token = sessionManager.getAuthToken();
        if (isValidToken(token)) {
            apiService.logout(token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    clearLocalSession(logoutResult);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    clearLocalSession(logoutResult);
                }
            });
        } else {
            clearLocalSession(logoutResult);
        }

        return logoutResult;
    }

    private void clearLocalSession(MutableLiveData<Boolean> logoutResult) {
        try {
            sessionManager.clearSession();
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to clear auth session", e);
            logoutResult.postValue(false);
            return;
        }

        executorService.execute(() -> {
            try {
                userDao.deleteAll();
                logoutResult.postValue(true);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to clear local session", e);
                logoutResult.postValue(false);
            }
        });
    }

    public boolean hasActiveSession() {
        String token = sessionManager.getAuthToken();
        return isValidToken(token);
    }

    public MutableLiveData<Result<User>> createUser(String name, String username, String role, String password) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        String token = getAuthHeader();
        if (!isValidToken(token)) {
            result.postValue(new Result<>(null, "No active session"));
            return result;
        }

        CreateUserRequestDTO request = new CreateUserRequestDTO(name, username, role, password);
        apiService.createUser(token, request).enqueue(new Callback<ApiResponseDTO<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<UserDTO>> call, Response<ApiResponseDTO<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = convertDtoToEntity(response.body().getData());
                    if (user == null) {
                        result.postValue(new Result<>(null, "Invalid user response"));
                        return;
                    }
                    executorService.execute(() -> userDao.insertUser(user));
                    result.postValue(new Result<>(user, null));
                } else {
                    result.postValue(new Result<>(null, "Failed to create user"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<UserDTO>> call, Throwable t) {
                result.postValue(new Result<>(null, t.getMessage()));
            }
        });

        return result;
    }

    public MutableLiveData<Result<User>> updateUser(int userId, String name, String username, String role) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        String token = getAuthHeader();
        if (!isValidToken(token)) {
            result.postValue(new Result<>(null, "No active session"));
            return result;
        }

        UpdateUserRequestDTO request = new UpdateUserRequestDTO(name, username, role);
        apiService.updateUser(token, userId, request).enqueue(new Callback<ApiResponseDTO<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<UserDTO>> call, Response<ApiResponseDTO<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = convertDtoToEntity(response.body().getData());
                    if (user == null) {
                        result.postValue(new Result<>(null, "Invalid user response"));
                        return;
                    }
                    executorService.execute(() -> userDao.updateUser(user));
                    result.postValue(new Result<>(user, null));
                } else {
                    result.postValue(new Result<>(null, "Failed to update user"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<UserDTO>> call, Throwable t) {
                result.postValue(new Result<>(null, t.getMessage()));
            }
        });

        return result;
    }

    public MutableLiveData<Result<Void>> deleteUser(int userId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        String token = getAuthHeader();
        if (!isValidToken(token)) {
            result.postValue(new Result<>(null, "No active session"));
            return result;
        }

        apiService.deleteUser(token, userId).enqueue(new Callback<ApiResponseDTO<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<Void>> call, Response<ApiResponseDTO<Void>> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> userDao.deleteById(userId));
                    result.postValue(new Result<>(null, null));
                } else {
                    result.postValue(new Result<>(null, "Failed to delete user"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<Void>> call, Throwable t) {
                result.postValue(new Result<>(null, t.getMessage()));
            }
        });

        return result;
    }

    public MutableLiveData<Result<Void>> resetPassword(int userId, String newPassword, String confirmPassword) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        String token = getAuthHeader();
        if (!isValidToken(token)) {
            result.postValue(new Result<>(null, "No active session"));
            return result;
        }

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(newPassword, confirmPassword);
        apiService.resetUserPassword(token, userId, request).enqueue(new Callback<ApiResponseDTO<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<Void>> call, Response<ApiResponseDTO<Void>> response) {
                if (response.isSuccessful()) {
                    result.postValue(new Result<>(null, null));
                } else {
                    result.postValue(new Result<>(null, "Failed to reset password"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<Void>> call, Throwable t) {
                result.postValue(new Result<>(null, t.getMessage()));
            }
        });

        return result;
    }

    private void syncUsersFromApi() {
        String token = getAuthHeader();
        if (!isValidToken(token)) {
            return;
        }

        apiService.getUsers(token).enqueue(new Callback<ApiResponseDTO<List<UserDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponseDTO<List<UserDTO>>> call,
                                   Response<ApiResponseDTO<List<UserDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserDTO> userDTOs = response.body().getData();
                    List<User> users = new ArrayList<>();
                    if (userDTOs != null) {
                        for (UserDTO userDTO : userDTOs) {
                            User user = convertDtoToEntity(userDTO);
                            if (user != null) {
                                users.add(user);
                            }
                        }
                    }
                    executorService.execute(() -> {
                        database.runInTransaction(() -> {
                            userDao.deleteAll();
                            userDao.insertAll(users);
                        });
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDTO<List<UserDTO>>> call, Throwable t) {
                Log.e(TAG, "Error syncing users from API", t);
            }
        });
    }

    private User convertDtoToEntity(UserDTO userDto) {
        if (userDto == null) {
            return null;
        }

        if (userDto.getName() == null || userDto.getUsername() == null || userDto.getRole() == null) {
            Log.w(TAG, "User DTO has null required fields for id=" + userDto.getId());
        }

        return new User(
                userDto.getId(),
                userDto.getName() == null ? "" : userDto.getName(),
                userDto.getUsername() == null ? "" : userDto.getUsername(),
                userDto.getRole() == null ? "" : userDto.getRole(),
                userDto.getCreatedAt() == null ? "" : userDto.getCreatedAt(),
                userDto.getUpdatedAt() == null ? "" : userDto.getUpdatedAt()
        );
    }

    private String getAuthHeader() {
        String token = sessionManager.getAuthToken();
        if (!isValidToken(token)) {
            return null;
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private boolean isValidToken(String token) {
        return token != null && !token.trim().isEmpty();
    }

    public static class Result<T> {
        private final T data;
        private final String error;

        public Result(T data, String error) {
            this.data = data;
            this.error = error;
        }

        public T getData() {
            return data;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null;
        }
    }
}
