# Plan: Role-Based Access Control (RBAC) Enforcement

## I. Overview
This plan addresses the unhandled authorization bugs where "Staff" users can currently see and potentially access Admin-only features. The goal is to enforce strict role-based visibility in the mobile UI and hard authorization on the backend API.

## II. Objectives
1.  **UI Redaction**: Hide management features from the mobile app's profile menu for Staff users.
2.  **API Hardening**: Protect Admin-only endpoints (Users, Students, and Global Logs) with middleware.
3.  **Data Partitioning**: Filter Activity and Transaction logs so Staff only see their own actions, while Admins see everything.

## III. Technical Implementation

### 1. Mobile Application (Android)
- **`MainActivity.java`**:
    - Update `showProfileDropdown()` logic.
    - Before showing the `PopupWindow`, check `currentUser.getRole()`.
    - Set `dropdownBinding.itemUserManagement.setVisibility(View.GONE)` and `dropdownBinding.itemStudentManagement.setVisibility(View.GONE)` if the user is not an "admin".
- **Safety**: Ensure that if a user manually tries to navigate via `navController` (though unlikely in this UI), the fragments check roles in `onViewCreated` or rely on the API's 403 response.

### 2. Backend API (Laravel)
- **`routes/api.php`**:
    - Move `LogController` routes out of the `role:admin` middleware group so Staff can access them.
    - Move `StudentController` routes (including `import`) **INTO** the `role:admin` middleware group.
- **`LogService.php`**:
    - Refactor `getLogs(string $type, array $filters = [])` to handle role-based partitioning.
    - **Staff Restriction (Activity Logs)**: If `Auth::user()->role !== 'admin'` and the requested `$type` is `'activity'`, apply a `where('actor_id', Auth::id())` filter.
    - **Staff Permission (Transaction Logs)**: If the requested `$type` is `'transaction'`, do NOT apply the `actor_id` filter for Staff. This allows all staff to see the complete history of item movements.
    - **Admin Access**: Maintain global visibility (no `actor_id` filter) for `admin` users across all log types.

### 3. Verification & Error Handling
- **Graceful Failures**: Ensure the mobile app handles `403 Forbidden` responses by showing a "Unauthorized" Toast and navigating back if necessary.
- **Log Source of Truth**: Verify that the `actor_id` filtering in `LogService` correctly partitions data for Staff without leaking Admin-level logs.

## IV. Verification Strategy
- **Admin Test**: Log in as an Admin; verify all menu items are visible and all logs (any staff) are shown.
- **Staff Test**: Log in as a Staff; verify User/Student management is hidden. Try to access `/api/v1/users` via Postman and verify a `403` response. Verify logs only show actions performed by that specific staff account.
