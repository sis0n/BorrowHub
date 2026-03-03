[# BorrowHub
### Borrowing Management System — Computer Studies Department

> An Android application designed to help **University MIS and CSD Staff** efficiently manage and monitor borrowing transactions within the Computer Studies Department.

---

## About the App

**BorrowHub** is a mobile-based Borrowing Management System built for university use. It streamlines the process of lending and tracking equipment, devices, and other assets managed by the Computer Studies Department (CSD), with oversight from the Management Information System (MIS) staff.

---

## Features

- **Borrow Request Management** — Submit, review, and approve borrowing requests
- **Item Tracking** — Monitor the status and availability of borrowable items in real time
- **Return Monitoring** — Track return dates and flag overdue items
- **Borrower Records** — Maintain a log of all borrowers and transaction history
- **Item Inventory** — Manage the list of available CSD assets and equipment
- **Notifications & Alerts** — Receive reminders for upcoming and overdue returns
- **Reports & Logs** — View and export borrowing history and summaries
- **Role-Based Access** — Separate access levels for MIS Staff and CSD Staff

---

## Tech Stack

| Component | Technology |
|---|---|
| Platform | Android (Mobile) |
| Architecture | MVVM (Model-View-ViewModel) |
| Language | Java |
| Database | Room (SQLite) |
| UI Framework | XML Layouts |
| Jetpack Components | ViewModel, LiveData, Room, View Binding, Navigation |

---

## Architecture

BorrowHub uses **MVVM** with a Repository pattern where the **Entity acts as the single Domain Model** across all layers to simplify data flow. For the complete architecture breakdown, see **[ARCHITECTURE.md](./docs/ARCHITECTURE.md)**.

---

## Getting Started (Mobile)

If you haven't cloned the repository yet, please follow the **[Root Getting Started Guide](../README.md#getting-started)** first.

### Prerequisites

- Android Studio (latest stable version)
- Android SDK (API Level 26 or higher)
- Java Development Kit (JDK 11+)

### Setup & Installation

1. **Open in Android Studio**
   - Launch Android Studio.
   - Select **Open an Existing Project**.
   - Navigate to the `mobile-app/` folder within the cloned repository.

2. **Configure dependencies**
   - Sync Gradle files when prompted.
   - Ensure all required SDKs and build tools are installed.

3. **Run the application**
   - Connect an Android device or start an emulator.
   - Click **Run** or press `Shift + F10`.

> Make sure the project builds and runs successfully before making any changes.

---

## Documentation

- [ARCHITECTURE.md](./docs/ARCHITECTURE.md) — Full MVVM architecture guide
- [CONTRIBUTING.md](../docs/CONTRIBUTING.md) — Shared development workflow and coding standards
- [PULL_REQUEST_TEMPLATE.md](../.github/PULL_REQUEST_TEMPLATE.md) — Standard PR template

---
*BorrowHub — Making asset management simple and efficient.*
