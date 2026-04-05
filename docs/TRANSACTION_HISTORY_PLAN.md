# Plan: Transaction History & Data Consistency Integration

## I. Overview
This plan addresses the inconsistency between the "Recent Transactions" in the Dashboard and the "Transaction Logs" in the System Logs. The goal is to establish a **Source of Truth** for transaction records by creating a dedicated **Transaction History** fragment that uses the `borrow_records` table, while keeping the `activity_logs` as an independent audit trail.

## II. Objectives
1.  **Establish Consistency:** Align the dashboard's "Recent Transactions" with a new dedicated History view.
2.  **Architectural Separation:** 
    -   **Transaction History:** State-based records (Source of Truth) from `borrow_records`.
    -   **System Logs:** Event-based audit trail from `activity_logs`.
3.  **UI Enhancement:** Add a third tab to the Transactions screen in the mobile app.

## III. Proposed Changes

### 1. Backend API (Laravel)
- **Endpoint Expansion:** Ensure `/api/v1/dashboard/recent-transactions` or a new `/api/v1/transactions/history` supports:
    - Pagination (standard for the project).
    - Basic filtering (search by student name/number).
    - Date range filtering (as requested by the client).
- **Data Model:** Continue using `BorrowRecord` with its relationships (`student`, `items`, `staff`).

### 2. Mobile Application (Android)
- **Resources (`res/values/strings.xml`):**
    - Add `transaction_tab_history` (Label: "History" or "Records").
- **Layouts (`res/layout/`):**
    - **`fragment_transaction_history.xml`**: Create a new layout with a `RecyclerView`, search bar, and empty state.
    - **`fragment_transaction.xml`**: Update `TabLayout` to accommodate the third tab.
- **Fragments (`view/fragment/`):**
    - **`TransactionHistoryFragment.java`**: New fragment to display the history list.
    - **`TransactionFragment.java`**: Update `showTab()` logic to include `TAB_HISTORY`.
- **ViewModel & Repository:**
    - **`TransactionViewModel.java`**: Add `fetchTransactionHistory()` and `getFilteredHistory()` LiveData.
    - **`TransactionRepository.java`**: Implement the API call for history/all transactions.
- **Adapters:**
    - Use or extend `TransactionAdapter` to handle both active and completed (returned) states with appropriate status chips/labels.

## IV. Technical Workflow

### Step 1: Resource Preparation
- Define new strings and colors for status indicators (e.g., Green for "Returned", Amber for "Borrowed").

### Step 2: Fragment Creation
- Implement `TransactionHistoryFragment` inheriting existing patterns from `TransactionLogsFragment` but mapping to `BorrowRecordDTO`.

### Step 3: Tab Integration
- Modify `TransactionFragment` to initialize three tabs:
    1. **Borrow** (Action)
    2. **Return** (Action)
    3. **Records** (History)

### Step 4: Data Integration
- Connect the "View All" button in `HomeFragment` (Dashboard) to navigate directly to the **Records** tab of the `TransactionFragment`.

## V. Consistency Rules
- **Dashboard Recent Transactions** == **Transaction History Fragment** (First 5 vs. All).
- **Logs > Transaction Logs** == Audit Trail of *actions* (borrowed, returned, deleted).
- **Transaction > Records** == Actual *state* of the hiram (who has what, what was returned).

## VI. Impact & Validation
- **Database:** No migrations required (uses existing tables).
- **Performance:** Pagination is mandatory to prevent UI lag on large history lists.
- **User Experience:** Provides a clear path for staff to verify previous transactions without digging through technical audit logs.
