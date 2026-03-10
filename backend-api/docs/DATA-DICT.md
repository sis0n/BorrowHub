# Data Dictionary — BorrowHub API

This document outlines the database schema for the BorrowHub backend, designed to support the requirements captured in the mobile prototype.

---

## 1. Core Entities

### `users`
Stores MIS/CSD Staff and Admin accounts.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `name` | string | No | | Full name of the staff |
| `username` | string | No | | Unique login identifier |
| `password` | string | No | | Hashed password |
| `role` | enum | No | 'staff' | `admin`, `staff` |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

### `courses`
Educational courses for student categorization.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `name` | string | No | | e.g., "BS Computer Science" |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

### `students`
Master list of authorized borrowers.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `student_number` | string | No | | Unique ID (e.g., 2024-12345) |
| `name` | string | No | | Full name of the student |
| `course_id` | bigint (FK) | No | | Reference to `courses.id` |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

---

## 2. Inventory Management

### `categories`
Groups items (e.g., Laptops, Projectors).
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `name` | string | No | | Category name |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

### `items`
Specific equipment available for borrowing.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `category_id` | bigint (FK) | No | | Reference to `categories.id` |
| `name` | string | No | | Name/Model of the item |
| `total_quantity` | integer | No | 0 | Total units in stock |
| `available_quantity`| integer | No | 0 | Units currently available to borrow |
| `status` | enum | No | 'active' | `active`, `maintenance`, `archived` |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

---

## 3. Transactions & Logs

### `borrow_records`
Core transaction table for borrowing events.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `student_id` | bigint (FK) | No | | Reference to `students.id` |
| `staff_id` | bigint (FK) | No | | User who processed the request |
| `collateral` | string | No | | Item left by student (e.g., ID) |
| `borrowed_at` | datetime | No | | Timestamp of borrowing |
| `due_at` | datetime | No | | Expected return (usually same day) |
| `returned_at` | datetime | Yes | | Timestamp when items were returned |
| `status` | enum | No | 'borrowed'| `borrowed`, `returned`, `overdue` |
| `created_at` | timestamp | Yes | | |
| `updated_at` | timestamp | Yes | | |

### `borrow_record_items`
Pivot table for many-to-many relationship between records and items.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `borrow_record_id` | bigint (FK) | No | | Reference to `borrow_records.id` |
| `item_id` | bigint (FK) | No | | Reference to `items.id` |
| `quantity` | integer | No | 1 | Number of units borrowed |

### `activity_logs`
Unified audit trail for both system activities and transaction history.
| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | bigint (PK) | No | | Unique identifier |
| `actor_id` | bigint (FK) | Yes | | Reference to `users.id` (Who performed it, e.g., Admin or Staff). Null if system generated. |
| `performed_by` | string | No | | Formatted string of the actor (e.g., "Staff (Maria Garcia)") |
| `target_user_id` | string | No | | ID of the target (e.g., Student ID 'STU3891', 'EMP2045', or 'SYSTEM') |
| `target_user_name` | string | No | | Name of the target (e.g., 'Lisa Thompson' or 'Admin') |
| `action` | string | No | | e.g., "Borrowed", "Returned", "Added", "Updated", "Deleted", "Created User", "Modified User" |
| `details` | text | No | | Detailed description (e.g., "Laptop - Dell XPS 15", "Status change: Portable Speaker -> Maintenance") |
| `type` | enum | No | | `transaction`, `activity` (used for tab filtering in the UI) |
| `created_at` | timestamp | No | CURRENT_TIMESTAMP | Timestamp of the event |


