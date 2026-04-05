# Plan: Log Action Filter Refinement

## I. Overview
This plan outlines the standardization of action filters in the Activity and Transaction logs. The goal is to move from verbose or inconsistent labeling to concise, universal action terms ("Borrowed", "Returned", "Created", "Updated", "Deleted").

## II. Requirements
1.  **Transaction Logs**: Change "Items Borrowed" & "Items Returned" to "**Borrowed**" & "**Returned**".
2.  **Activity Logs**: Implement universal filters: "**Created**", "**Updated**", and "**Deleted**" to cover all CRUD-based system operations.
3.  **Synchronization**: Ensure the frontend labels correctly map to the backend's action categorization logic.

## III. Technical Implementation

### 1. Backend (Laravel)
- **Log Service Standardization**:
    - Verify `LogService.php` constants for system logs.
    - Ensure the `activity_logs` table's `action` column uses the strings: `created`, `updated`, `deleted`, `borrowed`, and `returned`.
- **API Filtering**:
    - Ensure `GET /api/v1/activity-logs` and `GET /api/v1/transaction-logs` accept these standardized action strings as query parameters.

### 2. Mobile (Android)
- **Resource Updates (`strings.xml`)**:
    - Update `logs_filter_borrowed`: "Borrowed"
    - Update `logs_filter_returned`: "Returned"
    - Add `logs_filter_created`: "Created"
    - Add `logs_filter_updated`: "Updated"
    - Add `logs_filter_deleted`: "Deleted"
- **UI Logic**:
    - **`TransactionLogsFragment.java`**: Update the `Spinner` adapter to use the refined transaction actions.
    - **`ActivityLogsFragment.java`**: Replace the current specific action filters with the universal CRUD actions.
- **ViewModel Mapping**:
    - Update `TransactionViewModel.java` and `ActivityLogViewModel.java` to send the correct, lowercase action identifiers to the `Repository` and `ApiService`.

## IV. Verification Strategy
- **Backend**: Test API endpoints with specific action filters using Postman (e.g., `/api/v1/activity-logs?action=created`).
- **Mobile UI**: Verify the `Spinner` displays the correct labels in both fragments.
- **End-to-End**: Select a filter in the app and verify the list correctly displays only logs matching that specific action.
