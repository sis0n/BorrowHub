package com.example.borrowhub.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.data.local.entity.User;
import com.example.borrowhub.repository.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class UserManagementViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private UserRepository userRepository;

    private final MutableLiveData<java.util.List<User>> usersLiveData = new MutableLiveData<>();

    private UserManagementViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userRepository.getAllUsers()).thenReturn(usersLiveData);
        viewModel = new UserManagementViewModel(application, userRepository);

        usersLiveData.setValue(Arrays.asList(
                new User(1, "Administrator", "admin", "admin"),
                new User(2, "Staff User", "staff1", "staff")
        ));
    }

    @Test
    public void init_requestsUsersFromRepository() {
        verify(userRepository).getAllUsers();
    }

    @Test
    public void deleteUser_protectedAdmin_doesNotCallRepositoryAndPostsError() {
        User protectedAdmin = new User(1, "Administrator", "admin", "admin");

        viewModel.deleteUser(protectedAdmin);

        verify(userRepository, never()).deleteUser(anyInt());
        assertEquals("Cannot delete the admin user", viewModel.getOperationError().getValue());
    }

    @Test
    public void resetPasswordToDefault_callsRepositoryWithDefaultPassword() {
        User staff = new User(2, "Staff User", "staff1", "staff");
        MutableLiveData<UserRepository.Result<Void>> resetResult = new MutableLiveData<>();
        when(userRepository.resetPassword(eq(2), eq("borrowhub123"), eq("borrowhub123"))).thenReturn(resetResult);

        viewModel.resetPasswordToDefault(staff);
        verify(userRepository).resetPassword(2, "borrowhub123", "borrowhub123");

        resetResult.setValue(new UserRepository.Result<>(null, null));
        assertEquals(Boolean.TRUE, viewModel.getOperationSuccess().getValue());
    }

    @Test
    public void addUser_trimsInputAndPostsSuccess() {
        MutableLiveData<UserRepository.Result<User>> createResult = new MutableLiveData<>();
        when(userRepository.createUser(eq("New User"), eq("newuser"), eq("staff"), eq(UserManagementViewModel.DEFAULT_PASSWORD)))
                .thenReturn(createResult);

        viewModel.addUser("  New User  ", "  newuser  ", "  staff  ");
        verify(userRepository).createUser("New User", "newuser", "staff", UserManagementViewModel.DEFAULT_PASSWORD);
        assertNull(viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());

        createResult.setValue(new UserRepository.Result<>(new User(3, "New User", "newuser", "staff"), null));
        assertEquals(Boolean.TRUE, viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());
    }

    @Test
    public void updateUser_failureWithEmptyError_postsDefaultError() {
        User existing = new User(2, "Staff User", "staff1", "staff");
        MutableLiveData<UserRepository.Result<User>> updateResult = new MutableLiveData<>();
        when(userRepository.updateUser(eq(2), eq("Updated Name"), eq("staff1"), eq("staff")))
                .thenReturn(updateResult);

        viewModel.updateUser(existing, " Updated Name ", " staff1 ", " staff ");
        verify(userRepository).updateUser(2, "Updated Name", "staff1", "staff");
        assertNull(viewModel.getOperationSuccess().getValue());
        assertNull(viewModel.getOperationError().getValue());

        updateResult.setValue(new UserRepository.Result<>(null, "   "));
        assertNull(viewModel.getOperationSuccess().getValue());
        assertEquals("Failed to update user", viewModel.getOperationError().getValue());
    }

    @Test
    public void deleteUser_withNullUser_doesNotCallRepositoryAndPostsDefaultError() {
        viewModel.deleteUser(null);
        verify(userRepository, never()).deleteUser(anyInt());
        assertEquals("Failed to delete user", viewModel.getOperationError().getValue());
    }

    @Test
    public void setSearchQuery_filtersByNameAndUsernameIgnoringCaseAndSpaces() {
        usersLiveData.setValue(Arrays.asList(
                new User(1, "Administrator", "admin", "admin"),
                new User(2, "Staff User", "staff1", "staff")
        ));

        viewModel.setSearchQuery("  STAFF  ");
        List<User> filtered = viewModel.getFilteredUsers().getValue();
        assertEquals(1, filtered.size());
        assertEquals("staff1", filtered.get(0).getUsername());
        assertEquals("Staff User", filtered.get(0).getName());
    }

    @Test
    public void clearOperationStates_setsOperationLiveDataToNull() {
        viewModel.deleteUser(null);
        assertEquals("Failed to delete user", viewModel.getOperationError().getValue());

        viewModel.clearOperationStates();
        assertNull(viewModel.getOperationError().getValue());
        assertNull(viewModel.getOperationSuccess().getValue());
    }
}
