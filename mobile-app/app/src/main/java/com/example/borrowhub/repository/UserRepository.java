package com.example.borrowhub.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.dao.UserDao;
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.LoginRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginResponseDTO;
import com.example.borrowhub.data.remote.dto.UserDTO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserRepository(ApiService apiService, SessionManager sessionManager, UserDao userDao) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.userDao = userDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getUser() {
        return userDao.getUser();
    }

    public LiveData<Boolean> login(String email, String password) {
        MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
        
        LoginRequestDTO request = new LoginRequestDTO(email, password);
        apiService.login(request).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponseDTO.Data data = response.body().getData();
                    sessionManager.saveAuthToken("Bearer " + data.getToken());
                    
                    UserDTO userDto = data.getUser();
                    if (userDto != null) {
                        User user = new User(userDto.getId(), userDto.getName(), userDto.getEmail(), userDto.getRole());
                        executorService.execute(() -> userDao.insertUser(user));
                    }
                    loginResult.postValue(true);
                } else {
                    loginResult.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                loginResult.postValue(false);
            }
        });

        return loginResult;
    }

    public void logout() {
        String token = sessionManager.getAuthToken();
        if (token != null) {
            apiService.logout(token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Ignored
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Ignored
                }
            });
        }
        
        sessionManager.clearSession();
        executorService.execute(userDao::deleteAll);
    }
}
