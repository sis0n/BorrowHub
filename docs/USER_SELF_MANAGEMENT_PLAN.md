# Plan: User Self-Management Protection and UI Indicator

## I. Overview
This plan addresses the requirement to distinguish the currently logged-in user within the User Management list. It ensures that users cannot accidentally edit or delete their own accounts while providing a clear visual indicator of their identity in the list.

## II. Objectives
1.  **Identity Awareness**: Add a "(You)" suffix to the name of the currently logged-in user in the user list.
2.  **Account Protection**: Disable the "Edit", "Delete", and "Reset Password" buttons for the current user's own entry to prevent self-lockout or accidental modification.

## III. Technical Implementation

### 1. Mobile - ViewModel & Repository
- **`UserManagementViewModel.java`**:
    - Add a `LiveData<User> getCurrentUser()` method that calls `repository.getUser()`.
    - This ensures the fragment always knows who is currently logged in.

### 2. Mobile - Adapter (`UserAdapter.java`)
- **State Management**:
    - Add a private field `int currentUserId` to the adapter.
    - Create a method `setCurrentUserId(int id)` to update this value and trigger `notifyDataSetChanged()`.
- **UI Logic (`bind` method)**:
    - Compare `user.getId()` with `currentUserId`.
    - If they match:
        - Append " (You)" to the `tvUserName` text.
        - Set `isEnabled(false)` and `setAlpha(0.4f)` for `btnEdit`, `btnDelete`, and `btnResetPassword`.
        - Optionally, highlight the item with a slightly different background or stroke.

### 3. Mobile - Fragment (`UserManagementFragment.java`)
- **Observation**:
    - Observe `viewModel.getCurrentUser()` in `onViewCreated`.
    - When the current user data is received, call `userAdapter.setCurrentUserId(currentUser.getId())`.

### 4. Backend (Security Layer)
- **`UserController.php`**:
    - Add a server-side check in `update()` and `destroy()` methods.
    - If `Auth::id() === $userToModify->id`, return a `403 Forbidden` response with a message: "You cannot modify your own account from the User Management module. Please use Account Settings."

## IV. Verification Strategy
- **Visual Check**: Log in as an Admin and navigate to User Management. Verify your own name appears as "Admin Name (You)".
- **Interaction Check**: Verify that the Edit, Delete, and Reset Password icons for your own entry are dimmed and non-clickable.
- **Security Check**: Verify that icons for other users remain active and functional.
