# BorrowHub

## Project Overview
BorrowHub is a mobile-based Borrowing Management System built for university use. It is designed to help University MIS and CSD Staff efficiently manage and monitor borrowing transactions within the Computer Studies Department. It tracks item status, return dates, and borrower records while offering role-based access for different staff members.

**Main Technologies:**
- **Platform:** Android (Mobile)
- **Language:** Java
- **Database:** Room (SQLite)
- **UI Framework:** XML Layouts with View Binding
- **Jetpack Components:** ViewModel, LiveData, Room, Navigation

**Architecture:**
BorrowHub strictly follows the **MVVM (Model-View-ViewModel)** architectural pattern combined with a **Repository pattern**.
- **Entity/Model Layer:** Room Entities act as the Domain Model, simplifying data flow.
- **DAO Layer:** Interfaces defining database operations, called only by the Repository.
- **Repository Layer:** Single source of truth. Calls DAO and returns `LiveData<Entity>` directly to the ViewModel.
- **ViewModel Layer:** Holds `LiveData` for UI state and contains presentation logic. No context or DB access.
- **View Layer (Activity/Fragment):** Observes `LiveData` from the ViewModel and renders UI. No business logic or direct data access.

## Building and Running
The project is built using Gradle. Typical tasks can be executed via the command line or Android Studio:

- **Build the project:** `./gradlew assembleDebug`
- **Clean the project:** `./gradlew clean`
- **Run Unit Tests:** `./gradlew test`
- **Run Instrumentation Tests:** `./gradlew connectedAndroidTest`

*To run the application, it is recommended to use Android Studio to build and deploy to a connected Android device or emulator.*

## Development Conventions

**Coding Standards:**
- Keep functions short and focused (single responsibility).
- Avoid hardcoded strings; use `strings.xml`.
- **Naming Conventions:**
  - Classes: `PascalCase` (e.g., `BorrowViewModel`)
  - Functions / Variables: `camelCase` (e.g., `fetchBorrowRecords()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_BORROW_DAYS`)
  - XML Layout Files: `snake_case` (e.g., `fragment_borrow_list.xml`)
  - XML IDs: `snake_case` using specific prefixes based on the view type:
    - `tv_` (TextView)
    - `btn_` (Button)
    - `et_` (EditText)
    - `rv_` (RecyclerView)
    - `iv_` (ImageView)
    - `pb_` (ProgressBar)

**Architecture Rules:**
1. The View must never touch a DAO directly.
2. The ViewModel must never touch a DAO directly.
3. DAOs return Entities.
4. Entities are passed up to the View for rendering.

**Branching & Commit Strategy:**
- **Branching:** `feature/<short-description>`, `fix/<short-description>`, `improve/<short-description>`, `hotfix/<short-description>`. Branch off from `master`.
- **Commits:** Follow conventional commits format: `<type>: <short summary>`.
  - Types include: `feat`, `fix`, `refactor`, `style`, `docs`, `test`, `chore`.

**Pull Requests:**
Ensure the project builds without errors before opening a PR. Use the provided Pull Request Template (`docs/PR_TEMPLATE.md`) and request a review before merging.
