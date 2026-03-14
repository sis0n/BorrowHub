# BorrowHub Feature Documentation

This document provides a comprehensive list of features and functional deliverables for the BorrowHub prototype, organized by module.

## 1. Authentication & Landing (Login Module) `@src/app/screens/LoginScreen.tsx`
The entry point of the application, ensuring secure access to the management system.
*   **Default Credential Initialization:** Automatically sets up default `admin` and `staff` accounts in local storage on first load.
*   **Secure Sign-In:** Validates credentials against stored user data with real-time feedback via toast notifications.
*   **Role-Based Access:** Identifies user roles (Admin vs. Staff) to control permissions across the application.
*   **Session Management:** Persists the current session in local storage for seamless navigation.

## 2. Dashboard & Analytics `@src/app/screens/DashboardScreen.tsx`
Provides a high-level overview of the facility's resource status.
*   **Premium Stats Grid:** Real-time summary cards for:
    *   **Total Items:** Complete count of assets in the registry.
    *   **Currently Borrowed:** Count of items currently out with users.
    *   **Available Now:** Real-time stock availability.
    *   **Due Today:** Critical monitoring for items expected back.
*   **Recent Transactions Feed:** A chronological list of the latest borrowing and returning activities.
*   **Quick Actions:** Direct navigation buttons to the most common workflows (Borrow, Return, Add Item).

## 3. Inventory Management `@src/app/screens/InventoryScreen.tsx`
A centralized directory for tracking and managing physical assets.
*   **Asset Categorization:** Organizes items into logical groups like Laptops and Equipment.
*   **Real-Time Status Tracking:** Visual indicators for item availability (`Available`, `Borrowed`, `Maintenance`).
*   **Stock Monitoring:** Tracks total quantity vs. units currently available for issuance.
*   **Inventory CRUD:** Complete management interface to Add, Edit, and Delete assets.
*   **Search & Filtering:** Quickly locate items by name or type within the inventory list.

## 4. Transaction Management (Borrow & Return) `@src/app/screens/TransactionScreen.tsx`
The core workflow engine for asset lifecycle management. Supports operations across `@src/app/screens/BorrowItemScreen.tsx` and `@src/app/screens/ReturnItemScreen.tsx`.
*   **Unified Transaction Hub:** Single interface for both borrowing and returning operations.
*   **Borrowing Workflow:**
    *   **Student Auto-Fill:** Automatically retrieves student details when a valid Student Number is entered.
    *   **Multi-Item Borrowing:** Ability to assign multiple items (e.g., Laptop + HDMI Cable) to a single borrower in one transaction.
    *   **Collateral Tracking:** Records the specific security deposit (e.g., Student ID, License) held by the facility.
    *   **Policy Enforcement:** Automated "Same-Day Return" reminders and due date calculations.
*   **Returning Workflow:**
    *   **Active Borrow Search:** Locate outstanding items by student name, ID, or item name.
    *   **Return Verification:** Detailed view of the original transaction to ensure all items and collateral are correctly returned.
    *   **Instant Inventory Sync:** Automatically updates item status to `Available` upon successful return processing.

## 5. Student Management `@src/app/screens/StudentManagementScreen.tsx`
Management of the university's master student list.
*   **Student Registry:** A searchable list of all students eligible for borrowing.
*   **Course Mapping:** Links students to their respective academic programs.
*   **Bulk Import (CSV):** Feature for importing large lists of students via CSV data pasting to avoid manual entry.
*   **Registration Management:** Add, edit, or remove student profiles from the master list.

## 6. User Management (Administrative) `@src/app/screens/UserManagementScreen.tsx`
Controls system access for facility personnel.
*   **Role Management:** Differentiation between `MIS/CSD Admin` and `MIS/CSD Staff`.
*   **User Provisioning:** Create and manage administrative accounts.
*   **Security Controls:**
    *   **Password Reset:** Ability for admins to reset user passwords to a system default.
    *   **Admin Protection:** Prevents accidental deletion of the primary administrator account.

## 7. System Logs & Audit Trail `@src/app/screens/TransactionLogsScreen.tsx`
A comprehensive history of all actions performed within the system.
*   **Transaction Logs:** Dedicated audit trail for asset movements (Borrow/Return).
*   **Activity Logs:** Tracking of administrative changes (Inventory updates, User modifications, Student registration).
*   **Detailed Attribution:** Every log entry records the date, time, action taken, and the specific personnel who performed it.
*   **Advanced Filtering:** Capability to filter logs by action type or search for specific keywords.

## 8. Account & Security Settings `@src/app/screens/AccountSettingsScreen.tsx`
Self-service tools for the logged-in user.
*   **Profile Personalization:** Update full name and username.
*   **Password Management:** Secure workflow for changing account passwords with current password validation.
*   **Role Transparency:** Clear visibility of the user's assigned role and permissions.

## 9. System Branding & Visual Identity
Ensures consistent branding and professional visual presence across the application.
*   **Unified Application Logo:** Centralized use of `@src/assets/logo.png` across all entry points and UI components.
*   **App Icon/Favicon:** Implementation of the system logo as the browser favicon and application icon.
*   **Login & Header Branding:** Prominent branding on the login screen and header as established in `@src/app/screens/LoginScreen.tsx` and `@src/app/components/MainLayout.tsx`.
*   **Entry Point Branding:** Branding integration within `@src/main.tsx` for visual consistency from the root level.
