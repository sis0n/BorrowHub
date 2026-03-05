# BorrowHub Development Plan

## 🎯 Minimum Viable Product (MVP) Scope

For the MVP, we will focus on the core flow: **managing inventory and tracking who borrowed what.**

1. **Authentication & Authorization**
   - Login functionality.
   - Basic Roles: **Admin** (System-wide access) and **Staff/Librarian** (Manage inventory and handle transactions).
2. **Inventory Management**
   - View all inventory items with their available counts.
   - Add, Update, and Delete items.
3. **Borrowing & Returning Flow**
   - **Borrow:** Staff selects a user/student, selects items, and confirms the transaction. Reduces available inventory.
   - **Return:** Staff processes a return for an active transaction. Restores available inventory.
4. **Basic Transaction Logs**
   - A read-only audit trail of borrowing and returning actions.

---

## 📊 Entity-Relationship Diagram (ERD)

This is the database structure that will be implemented in MySQL (via Laravel Migrations).

**1. `users` (System Users & Borrowers)**

- `id` (PK)
- `name` (String)
- `username` (String, Unique)
- `password` (String, Hashed)
- `role` (Enum: 'admin', 'staff', 'student')
- `timestamps`

**2. `inventory_items`**

- `id` (PK)
- `name` (String)
- `type` (String - e.g., 'Equipment', 'Laptop')
- `total_quantity` (Integer)
- `available_quantity` (Integer)
- `status` (Enum: 'Available', 'Maintenance')
- `timestamps`

**3. `transactions`**

- `id` (PK)
- `borrower_id` (FK -> `students.id`)
- `processed_by_id` (FK -> `users.id`)
- `status` (Enum: 'Active', 'Returned', 'Overdue')
- `borrowed_at` (Datetime)
- `expected_return_at` (Datetime, Nullable)
- `returned_at` (Datetime, Nullable)
- `timestamps`

**4. `transaction_items` (Pivot Table for Many-to-Many)**

- `id` (PK)
- `transaction_id` (FK -> `transactions.id`)
- `inventory_item_id` (FK -> `inventory_items.id`)
- `quantity` (Integer)

**5. `students`**

- `student_id` (PK)
- `course_id` (FK -> `courses.id`)
- `Full Name` (String)

**6. `courses`**

- `id` (PK)
- `name` (String)

**7. `system_logs` (Optional for MVP, good for Audit)**

- `id` (PK)
- `user_id` (FK -> `users.id`)
- `action` (String - e.g., 'Borrowed', 'Updated Item')
- `description` (Text)
- `created_at` (Datetime)

---

## 📋 Development Tasks (Backlog)

Since BorrowHub is a **Network-First (with Local Caching)** architecture, development should follow a Backend-first approach for each feature.

### Phase 1: Backend Foundation (Laravel)

- [ ] **Task 1.1:** Initialize Laravel project and configure MySQL connection.
- [ ] **Task 1.2:** Create database migrations based on the ERD above.
- [ ] **Task 1.3:** Set up Laravel Models with relationships (Users, InventoryItem, Transaction).
- [ ] **Task 1.4:** Implement Authentication using Laravel Sanctum for API tokens.
- [ ] **Task 1.5:** Create Form Requests and API Resources to standardize JSON validation and responses.

### Phase 2: Backend API Endpoints (Service-Repository Pattern)

- [ ] **Task 2.1:** Inventory API (`GET`, `POST`, `PUT`, `DELETE` for `/api/inventory`).
- [ ] **Task 2.2:** Transactions API (`POST /api/transactions/borrow`, `POST /api/transactions/return`). Include logic to auto-decrement/increment `available_quantity`.
- [ ] **Task 2.3:** Users API (`GET` for searching borrowers).
- [ ] **Task 2.4:** Logs API (`GET /api/logs`).

### Phase 3: Mobile App Foundation (Android/Java)

- [ ] **Task 3.1:** Set up Android Project with MVVM architecture.
- [ ] **Task 3.2:** Configure Retrofit & OkHttp for network calls (add token interceptors).
- [ ] **Task 3.3:** Set up Room Database for local caching of Inventory and Transactions.
- [ ] **Task 3.4:** Implement Login Screen and securely store the auth token locally.

### Phase 4: Mobile App Feature Integration

- [ ] **Task 4.1:** **Inventory Screen:** Fetch items from the API, display in a RecyclerView, and implement the Add/Edit item dialogs mapping to the API.
- [ ] **Task 4.2:** **Borrow Screen:** Implement the flow to search a user, select items, and submit a borrow request to the API.
- [ ] **Task 4.3:** **Return Screen:** Fetch active transactions and implement the return submission.
- [ ] **Task 4.4:** **Dashboard & Logs:** Connect the dashboard stats and log views to their respective endpoints.

### Phase 5: Caching & Polish

- [ ] **Task 5.1:** Update the Android Repositories to save API responses into Room.
- [ ] **Task 5.2:** Implement Network-First fallback (if API fetch fails, load from Room DB so the app is still usable offline).
