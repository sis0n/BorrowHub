# BorrowHub — MVVM Architecture Guide
### Room (SQLite) + Retrofit (API) Implementation

---

## Overview

BorrowHub uses the **MVVM (Model-View-ViewModel)** pattern combined with a **Repository pattern**.

For this project, we have opted for a **streamlined approach** where the **Entity Layer also acts as the Domain Model**. This means the classes mapped to our Room database (`data/local/entity/`) are the exact same classes passed up to the ViewModel and View.

To support remote integration with the Laravel backend, we have introduced a **Remote Data Layer** using Retrofit. The Repository serves as the single source of truth, coordinating between local cache and remote API.

---

## Full Architecture Diagram

```
+---------------------------------------------------------------+
|                          UI LAYER                             |
|                                                               |
|   Activity / Fragment                                         |
|   - Observes LiveData                                         |
|   - Sends user events to ViewModel                            |
|   - Renders UI using Entity objects                           |
+----------------------------+----------------------------------+
                             |
                    calls methods on
                             |
+----------------------------v----------------------------------+
|                       VIEWMODEL LAYER                         |
|                                                               |
|   BorrowViewModel / InventoryViewModel / AuthViewModel        |
|   - Holds LiveData<T> for UI state                            |
|   - Calls Repository methods                                  |
|   - No Android Context, no DB access                          |
+----------------------------+----------------------------------+
                             |
                    calls methods on
                             |
+----------------------------v----------------------------------+
|                     REPOSITORY LAYER                          |
|                                                               |
|   BorrowRepository / ItemRepository / UserRepository          |
|   - Single source of truth                                    |
|   - Decides data source (Local cache or Remote API)           |
|   - Orchestrates Data Sync (Remote -> Local)                  |
+--------------+-----------------------------+------------------+
               |                             |
      calls queries on                calls requests on
               |                             |
+--------------v---------------+    +--------v------------------+
|      LOCAL DATA (Room)       |    |  REMOTE DATA (Retrofit)   |
|                              |    |                           |
|  +--------+   +-----------+  |    |  +-----------+            |
|  |  DAO   |   |  ENTITY   |  |    |  |ApiService |            |
|  +--------+   +-----------+  |    |  +-----------+            |
|                              |    |  +-----------+            |
|  AppDatabase (SQLite)        |    |  |   DTOs    |            |
+------------------------------+    +---------------------------+
```

---

## The Data Layer

BorrowHub uses a **Network-First with Local Cache** strategy:

1.  **Remote Data (`data/remote/`)**: Fetches data from the Laravel API using Retrofit. Data is returned in **DTO (Data Transfer Object)** format.
2.  **Local Data (`data/local/`)**: Acts as a persistent cache using Room. The Repository converts DTOs into **Entities** and saves them to the local database.
3.  **Synchronization**: The View always observes the **Local Database**. When the Repository fetches fresh data from the API and updates the local cache, the UI automatically refreshes via `LiveData`.

### Remote Layer Components
- **`ApiService`**: Interface defining Retrofit endpoints (GET, POST, etc.).
- **`DTO`**: Simple classes that match the JSON structure returned by the Laravel backend.
- **`ApiClient`**: Singleton that provides the Retrofit instance.

---

## Project Structure

```
java/com/example/borrowhub/
|
|-- data/
|   |-- local/                      <- Local persistence (Room)
|   |   |-- AppDatabase.java
|   |   |-- entity/
|   |   `-- dao/
|   |
|   `-- remote/                     <- Remote networking (Retrofit)
|       |-- api/
|       |   `-- ApiService.java     <- Retrofit endpoints
|       |-- dto/                    <- API Request/Response models
|       `-- ApiClient.java          <- Retrofit configuration
|
|-- repository/                     <- Data coordination
|   |-- UserRepository.java
|   |-- ItemRepository.javare
|   `-- BorrowRepository.java
...
```

---

## Summary: When to Use What

| Class Type | Location | Used By | Contains |
|---|---|---|---|
| `*Entity` | `data/local/entity/` | DAO, Repository, VM, View | Room annotations, DB column fields |
| `*DTO` | `data/remote/dto/` | ApiService, Repository | JSON mapping (GSON/Moshi) |
| `*Dao` | `data/local/dao/` | Repository only | @Query, @Insert, @Update, @Delete |
| `ApiService` | `data/remote/api/` | Repository only | Retrofit @GET, @POST annotations |
| `*Repository`| `repository/` | ViewModel only | Orchestrates Local & Remote data |
| `*ViewModel` | `viewmodel/` | View (Activity/Fragment) | LiveData, calls Repository |

---

## Key Rules

1.  The **View and ViewModel never touch the API directly** — always go through the Repository.
2.  The **Repository is the only layer** allowed to communicate with both the `AppDatabase` and `ApiService`.
3.  Always **save remote data to the local cache** before showing it in the UI (except for sensitive actions like Login).
4.  The UI should **observe LiveData from the local database** to ensure it works offline.
