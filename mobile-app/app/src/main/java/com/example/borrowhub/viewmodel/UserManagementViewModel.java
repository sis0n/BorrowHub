package com.example.borrowhub.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserManagementViewModel extends AndroidViewModel {

    public static final String DEFAULT_PASSWORD = "borrowhub123";
    public static final String PROTECTED_ADMIN_USERNAME = "admin";

    private final UserRepository repository;
    private final LiveData<List<User>> usersLiveData;
    private final MutableLiveData<List<User>> filteredUsers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();
    private final Observer<List<User>> usersObserver;

    private List<User> allUsers = new ArrayList<>();
    private String normalizedSearchQuery = "";

    public UserManagementViewModel(@NonNull Application application) {
        this(application, new UserRepository(application));
    }

    public UserManagementViewModel(@NonNull Application application, @NonNull UserRepository userRepository) {
        super(application);
        this.repository = userRepository;
        this.usersLiveData = repository.getAllUsers();
        this.usersObserver = users -> {
            allUsers = users == null ? new ArrayList<>() : users;
            applyFilters();
        };
        observeUsers();
    }

    public LiveData<List<User>> getFilteredUsers() {
        return filteredUsers;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }

    public int getTotalUserCount() {
        return allUsers.size();
    }

    public void clearOperationStates() {
        operationSuccess.setValue(null);
        operationError.setValue(null);
    }

    public void setSearchQuery(String query) {
        normalizedSearchQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);
        applyFilters();
    }

    public void addUser(String name, String username, String role) {
        clearOperationStates();
        observeResult(repository.createUser(safeTrim(name), safeTrim(username), safeTrim(role), DEFAULT_PASSWORD),
                "Failed to create user");
    }

    public void updateUser(User existingUser, String name, String username, String role) {
        clearOperationStates();
        if (existingUser == null) {
            operationError.setValue("Failed to update user");
            return;
        }
        observeResult(repository.updateUser(existingUser.getId(), safeTrim(name), safeTrim(username), safeTrim(role)),
                "Failed to update user");
    }

    public void deleteUser(User user) {
        clearOperationStates();
        if (user == null) {
            operationError.setValue("Failed to delete user");
            return;
        }
        if (isProtectedAdmin(user)) {
            operationError.setValue("Cannot delete the admin user");
            return;
        }
        observeResult(repository.deleteUser(user.getId()), "Failed to delete user");
    }

    public void resetPasswordToDefault(User user) {
        clearOperationStates();
        if (user == null) {
            operationError.setValue("Failed to reset password");
            return;
        }
        observeResult(repository.resetPassword(user.getId(), DEFAULT_PASSWORD, DEFAULT_PASSWORD),
                "Failed to reset password");
    }

    public boolean isProtectedAdmin(User user) {
        return user != null
                && user.getUsername() != null
                && PROTECTED_ADMIN_USERNAME.equalsIgnoreCase(user.getUsername().trim());
    }

    private <T> void observeResult(LiveData<UserRepository.Result<T>> liveData, String defaultError) {
        Observer<UserRepository.Result<T>> observer = new Observer<UserRepository.Result<T>>() {
            @Override
            public void onChanged(UserRepository.Result<T> result) {
                liveData.removeObserver(this);
                if (result != null && result.isSuccess()) {
                    operationError.setValue(null);
                    operationSuccess.setValue(true);
                } else {
                    String error = (result == null || result.getError() == null || result.getError().trim().isEmpty())
                            ? defaultError
                            : result.getError();
                    operationSuccess.setValue(null);
                    operationError.setValue(error);
                }
            }
        };
        liveData.observeForever(observer);
    }

    private void observeUsers() {
        usersLiveData.observeForever(usersObserver);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void applyFilters() {
        if (normalizedSearchQuery.isEmpty()) {
            filteredUsers.setValue(new ArrayList<>(allUsers));
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            boolean matchesName = user.getName() != null
                    && user.getName().toLowerCase(Locale.US).contains(normalizedSearchQuery);
            boolean matchesUsername = user.getUsername() != null
                    && user.getUsername().toLowerCase(Locale.US).contains(normalizedSearchQuery);
            if (matchesName || matchesUsername) {
                filtered.add(user);
            }
        }
        filteredUsers.setValue(filtered);
    }

    @Override
    protected void onCleared() {
        usersLiveData.removeObserver(usersObserver);
        super.onCleared();
    }
}
