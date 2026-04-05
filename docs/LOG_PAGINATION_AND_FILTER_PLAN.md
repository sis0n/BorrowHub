# Plan: Log Pagination & Date Filtering

## I. Overview
This plan outlines the implementation of UI-driven pagination for Activity and Transaction logs, matching the Inventory module's behavior. It also introduces a dynamic date range filter to help staff find specific records across large datasets.

## II. Objectives
1.  **Consistency**: Port the pagination UI (Prev/Next buttons and page indicator) from Inventory to Logs.
2.  **Optimization**: Reduce initial load times by fetching logs in pages (default: 15 items).
3.  **Flexibility**: Implement a date period filter with predefined and custom options.

## III. Technical Implementation

### 1. Mobile UI Changes
- **Layout Updates (`fragment_activity_logs.xml` & `fragment_transactions_logs.xml`)**:
    - Add the `layoutPagination` block at the bottom (anchored to parent bottom).
    - Ensure the `RecyclerView` is constrained to the top of the pagination layout.
- **Filter Updates**:
    - Add a second `Spinner` (or a horizontal chip group) for **Date Period**.
    - **Options**: Today, Last 7 Days, Last 30 Days, This Month, Custom Range..., All Time.

### 2. Mobile Logic (Java)
- **ViewModels**:
    - Update `ActivityLogViewModel` and `TransactionViewModel` to track `currentPage`.
    - Add methods `nextPage()`, `previousPage()`, and `setDateFilter(startDate, endDate)`.
- **Fragments**:
    - Implement the pagination button logic.
    - Implement `MaterialDatePicker.Builder.dateRangePicker()` for the "Custom Range" option.

### 3. Backend (Laravel)
- **`LogService.php`**:
    - Update `getLogs()` to accept `start_date` and `end_date` parameters.
    - Apply `whereBetween('created_at', [$start, $end])` logic.
- **`LogController.php`**:
    - Validate date inputs (ISO 8601 format).

## IV. Date Filter Discussion (Proposed Options)
| Option | Logic |
| :--- | :--- |
| **Today** | `now()->startOfDay()` to `now()` |
| **Last 7 Days** | `now()->subDays(7)` to `now()` |
| **Last 30 Days** | `now()->subDays(30)` to `now()` |
| **This Month** | `now()->startOfMonth()` to `now()` |
| **Custom Range** | User-selected range via DatePicker |
| **All Time** | No date constraints applied |

## V. Verification Strategy
- **Pagination**: Navigate through 3+ pages and verify data changes correctly.
- **Date Filter**: Select "Today" and verify only today's logs appear.
- **Custom Range**: Select a specific week in the past and verify results match the database.
