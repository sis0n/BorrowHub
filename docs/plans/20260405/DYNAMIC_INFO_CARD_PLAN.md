# Plan: Dynamic & Synchronized Info Card

## I. Overview
This plan outlines the steps to standardize the "Info Card" across the Borrow and Return screens in the BorrowHub mobile application. It ensures that critical transaction metadata (date, time, and actor) is displayed consistently and dynamically.

## II. UI/UX Requirements
1.  **Consistency:** The Info Card must appear at the top of both `BorrowItemFragment` and `ReturnItemFragment`.
2.  **Relevance:** Remove the "Return by:" field as it is deemed unnecessary for these transaction types.
3.  **Real-time Data:** The "Date & Time:" field must update every second to reflect the current system time.
4.  **Identity:** The "Processed by:" field must display the full name of the logged-in staff member instead of a hardcoded placeholder.

## III. Technical Implementation

### 1. XML Layout Updates
- **`fragment_borrow_item.xml`**:
    - Remove the third `LinearLayout` (Return by) from the Info Card.
    - Assign `@+id/tvProcessedBy` to the "Processed by" value `TextView`.
- **`fragment_return_item.xml`**:
    - Wrap existing content in a `LinearLayout` or adjust `ConstraintLayout` to place the Info Card at the top.
    - Copy the updated Info Card structure from `fragment_borrow_item.xml`.

### 2. ViewModel & Logic (Java)
- **Live Clock**:
    - Add a `MutableLiveData<String> currentDateTime` to `TransactionViewModel`.
    - Implement a `Handler` and `Runnable` to update this LiveData every 1000ms using a standard format (e.g., `MMM dd, yyyy - hh:mm:ss a`).
- **Staff Info**:
    - Add `MutableLiveData<String> processedByName` to `TransactionViewModel`.
    - Populate this value from `SessionManager` (or `UserPreference`) upon ViewModel initialization.

### 3. View Integration
- Both `BorrowItemFragment` and `ReturnItemFragment` will:
    - Observe `currentDateTime` and update `tvCurrentDateTime`.
    - Observe `processedByName` and update `tvProcessedBy`.

## IV. Verification Strategy
- **Visual Check**: Verify the Info Card is present and identical in both tabs.
- **Dynamic Check**: Confirm the clock ticks every second.
- **Context Check**: Log in with different staff accounts to ensure the name updates correctly.
