# Plan: Audit Log Data Integrity Refactoring

## I. Overview
This plan addresses the "Misleading Data" issue in the `activity_logs` table. By removing static name snapshots (`performed_by`, `target_user_name`) and relying on dynamic ID-based lookups (`actor_id`, `target_user_id`), we ensure the system always reflects the current names of users and students.

## II. Objectives
1.  **Normalize Data**: Remove redundant string columns that become stale when names are updated.
2.  **Dynamic Referencing**: Implement Eloquent relationships to fetch names in real-time.
3.  **Preserve History**: Ensure that even if a user/student is removed from active lists, their identity in logs remains traceable (using Soft Deletes).

## III. Technical Implementation

### 1. Database Migration (Laravel)
- **Schema Update**:
    - Create a new migration to `DROP` columns: `performed_by` and `target_user_name`.
    - Ensure `actor_id` (User) and `target_user_id` (Student/User) are properly indexed.
- **Data Preservation**:
    - Add `use SoftDeletes` to the `User` and `Student` models to prevent "Missing Reference" errors in logs.

### 2. Backend Logic (`LogService.php`)
- **Update Logging Logic**:
    - Modify `LogService::log()` to stop recording string names.
    - Ensure the `actor_id` is always captured from `Auth::id()`.
- **Relationship Definition**:
    - Define `belongsTo` relationships in the `ActivityLog` model:
        - `actor()` -> `User`
        - `target()` -> `User` or `Student` (may require polymorphic relationship or separate columns).

### 3. API Resources
- **`ActivityLogResource`**:
    - Update the resource to return `actor_name` and `target_name` by accessing the related model's `name` attribute dynamically.
    - Handle null/deleted cases: `optional($this->actor)->name ?? 'System/Unknown'`.

### 4. Mobile Integration (Android)
- **DTO Update**:
    - No significant UI changes needed, but ensure `ActivityLogDTO` continues to receive the `actor_name` and `target_name` fields as strings from the API.

## IV. Impact & Risk Mitigation
- **Risk**: Hard deletion of a user results in "Unknown" logs.
- **Mitigation**: Strictly enforce `SoftDeletes`. Update the `ActivityLog` model to use `withTrashed()` when fetching relationships to ensure name retrieval for deleted accounts.
