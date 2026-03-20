package com.example.borrowhub.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.dao.UserDao;
import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.data.remote.api.ApiService;
import com.example.borrowhub.data.remote.dto.ApiResponseDTO;
import com.example.borrowhub.data.remote.dto.CreateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginRequestDTO;
import com.example.borrowhub.data.remote.dto.LoginResponseDTO;
import com.example.borrowhub.data.remote.dto.ResetPasswordRequestDTO;
import com.example.borrowhub.data.remote.dto.UpdateUserRequestDTO;
import com.example.borrowhub.data.remote.dto.UserDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Type;
import java.util.List;

public class UserRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ApiService mockApiService;
    @Mock
    private SessionManager mockSessionManager;
    @Mock
    private UserDao mockUserDao;
    @Mock
    private AppDatabase mockDatabase;
    @Mock
    private Call<LoginResponseDTO> mockCall;
    @Mock
    private Observer<Boolean> mockObserver;
    @Mock
    private Call<Void> mockLogoutCall;
    @Mock
    private Call<ApiResponseDTO<List<UserDTO>>> mockUsersCall;
    @Mock
    private Call<ApiResponseDTO<UserDTO>> mockCreateUserCall;
    @Mock
    private Call<ApiResponseDTO<UserDTO>> mockUpdateUserCall;
    @Mock
    private Call<ApiResponseDTO<Void>> mockDeleteUserCall;
    @Mock
    private Call<ApiResponseDTO<Void>> mockResetPasswordCall;

    private UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userRepository = new UserRepository(mockApiService, mockSessionManager, mockUserDao, mockDatabase);
    }

    @Test
    public void login_success_savesTokenAndUser() {
        // Arrange
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        LoginResponseDTO.Data mockData = new LoginResponseDTO.Data();
        mockData.setToken("fake_token");
        UserDTO userDto = new UserDTO();
        userDto.setId(1);
        userDto.setName("Test User");
        userDto.setUsername("testuser");
        userDto.setRole("student");
        mockData.setUser(userDto);
        mockResponse.setData(mockData);

        when(mockApiService.login(any(LoginRequestDTO.class))).thenReturn(mockCall);

        // Act
        LiveData<Boolean> result = userRepository.login("testuser", "password");
        result.observeForever(mockObserver);

        // Capture callback
        ArgumentCaptor<Callback<LoginResponseDTO>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());

        // Simulate success response
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        verify(mockSessionManager).saveAuthToken("Bearer fake_token");
        
        verify(mockUserDao, timeout(200)).insertUser(any(User.class));
        verify(mockObserver).onChanged(true);
    }

    @Test
    public void logout_clearsSessionAndDeletesUser() {
        // Arrange
        when(mockSessionManager.getAuthToken()).thenReturn("Bearer fake_token");
        when(mockApiService.logout("Bearer fake_token")).thenReturn(mockLogoutCall);

        // Act
        LiveData<Boolean> result = userRepository.logout();
        result.observeForever(mockObserver);

        ArgumentCaptor<Callback<Void>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockLogoutCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockLogoutCall, Response.success(null));

        // Assert
        verify(mockSessionManager).clearSession();
        
        verify(mockUserDao, timeout(200)).deleteAll();
        verify(mockApiService).logout("Bearer fake_token");
        verify(mockObserver).onChanged(true);
        result.removeObserver(mockObserver);
    }

    @Test
    public void getAllUsers_success_syncsAndReturnsDaoLiveData() {
        MutableLiveData<List<User>> cachedUsers = new MutableLiveData<>();
        when(mockSessionManager.getAuthToken()).thenReturn("fake_token");
        when(mockUserDao.getAllUsers()).thenReturn(cachedUsers);
        when(mockApiService.getUsers("Bearer fake_token")).thenReturn(mockUsersCall);

        LiveData<List<User>> result = userRepository.getAllUsers();

        verify(mockUsersCall).enqueue(any(Callback.class));
        verify(mockUserDao).getAllUsers();
    }

    @Test
    public void createUser_success_savesUserLocally() {
        when(mockSessionManager.getAuthToken()).thenReturn("fake_token");
        when(mockApiService.createUser(any(), any(CreateUserRequestDTO.class))).thenReturn(mockCreateUserCall);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(9);
        userDTO.setName("Admin");
        userDTO.setUsername("admin");
        userDTO.setRole("admin");
        ApiResponseDTO<UserDTO> responseBody = new ApiResponseDTO<>("success", "created", userDTO);

        LiveData<UserRepository.Result<User>> result = userRepository.createUser("Admin", "admin", "admin", "secret123");
        result.observeForever(r -> { });

        ArgumentCaptor<Callback<ApiResponseDTO<UserDTO>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCreateUserCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockCreateUserCall, Response.success(responseBody));

        verify(mockUserDao, timeout(200)).insertUser(any(User.class));
    }

    @Test
    public void updateUser_success_updatesUserLocally() {
        when(mockSessionManager.getAuthToken()).thenReturn("fake_token");
        when(mockApiService.updateUser(any(), anyInt(), any(UpdateUserRequestDTO.class))).thenReturn(mockUpdateUserCall);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(5);
        userDTO.setName("Editor");
        userDTO.setUsername("editor");
        userDTO.setRole("staff");
        ApiResponseDTO<UserDTO> responseBody = new ApiResponseDTO<>("success", "updated", userDTO);

        LiveData<UserRepository.Result<User>> result = userRepository.updateUser(5, "Editor", "editor", "staff");
        result.observeForever(r -> { });

        ArgumentCaptor<Callback<ApiResponseDTO<UserDTO>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockUpdateUserCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockUpdateUserCall, Response.success(responseBody));

        verify(mockUserDao, timeout(200)).updateUser(any(User.class));
    }

    @Test
    public void deleteUser_success_deletesUserLocally() {
        when(mockSessionManager.getAuthToken()).thenReturn("fake_token");
        when(mockApiService.deleteUser("Bearer fake_token", 7)).thenReturn(mockDeleteUserCall);

        LiveData<UserRepository.Result<Void>> result = userRepository.deleteUser(7);
        result.observeForever(r -> { });

        ArgumentCaptor<Callback<ApiResponseDTO<Void>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockDeleteUserCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockDeleteUserCall, Response.success(new ApiResponseDTO<>("success", "deleted", null)));

        verify(mockUserDao, timeout(200)).deleteById(7);
    }

    @Test
    public void resetPassword_success_callsApi() {
        when(mockSessionManager.getAuthToken()).thenReturn("fake_token");
        when(mockApiService.resetUserPassword(any(), anyInt(), any(ResetPasswordRequestDTO.class))).thenReturn(mockResetPasswordCall);

        LiveData<UserRepository.Result<Void>> result = userRepository.resetPassword(3, "newPass123", "newPass123");
        result.observeForever(r -> { });

        ArgumentCaptor<Callback<ApiResponseDTO<Void>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockResetPasswordCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockResetPasswordCall, Response.success(new ApiResponseDTO<>("success", "reset", null)));

        verify(mockApiService).resetUserPassword(any(), anyInt(), any(ResetPasswordRequestDTO.class));
    }

    @Test
    public void apiResponseParsing_userDto_mapsCoreFields() {
        String json = "{\"status\":\"success\",\"message\":\"ok\",\"data\":[{\"id\":12,\"name\":\"John Doe\",\"username\":\"jdoe\",\"role\":\"admin\"}]}";
        Type responseType = new TypeToken<ApiResponseDTO<List<UserDTO>>>() {}.getType();
        ApiResponseDTO<List<UserDTO>> response = new Gson().fromJson(json, responseType);

        org.junit.Assert.assertTrue(response.isSuccess());
        org.junit.Assert.assertEquals(1, response.getData().size());
        org.junit.Assert.assertEquals("John Doe", response.getData().get(0).getName());
        org.junit.Assert.assertEquals("jdoe", response.getData().get(0).getUsername());
        org.junit.Assert.assertEquals("admin", response.getData().get(0).getRole());
    }
}
