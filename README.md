# BorrowHub Monorepo
### Borrowing Management System — Computer Studies Department

Welcome to the **BorrowHub** project! This is a monorepo that contains both the mobile application (Android) and the backend API service (Laravel).

---

## Project Structure

- **[mobile-app/](./mobile-app)**: Android Mobile Application (Java, Room, MVVM).
- **[backend-api/](./backend-api)**: Laravel Backend Service + PostgreSQL (PHP, MySQL, Service-Repository Pattern).
- **[docs/](./docs)**: Shared project documentation and contribution guidelines.

---

## Getting Started

Follow these steps to set up the project on your local machine.

### 1. Prerequisites
- **Git** installed on your system.
- Access to the BorrowHub GitHub repository.

### 2. Fork & Clone
1. **Fork the repository** to your own GitHub account.
2. **Clone your fork locally**:
   ```bash
   git clone https://github.com/your-username/BorrowHub.git
   cd BorrowHub
   ```
3. **Set the upstream remote** to keep your fork up to date:
   ```bash
   git remote add upstream https://github.com/original-repo-owner/BorrowHub.git
   ```

---

## Development Setup

Once the repository is cloned, follow the specific guide depending on your role.

### For Mobile (Frontend) Developers
The Android application is located in the `mobile-app/` directory.
1. **Prerequisites:** Android Studio, Android SDK (API 26+), JDK 11+.
2. **Setup:** Open the `mobile-app/` folder in Android Studio and sync Gradle.
3. **Full Guide:** See **[mobile-app/README.md](./mobile-app/README.md)** for detailed environment setup.

### For Backend Developers
The Laravel API is located in the `backend-api/` directory.
1. **Prerequisites:** PHP 8.1+, Composer, MySQL 8.0+.
2. **Setup:** Navigate to `backend-api/`, install dependencies, and configure your `.env` file.
3. **Full Guide:** See **[backend-api/README.md](./backend-api/README.md)** for detailed environment and database setup.

---

## Shared Resources

- **[CONTRIBUTING.md](./docs/CONTRIBUTING.md)**: Guidelines for contributing to either project.
- **[.github/](./.github)**: Standard Issue and Pull Request templates for the entire repository.

## Architecture Overview

BorrowHub uses a **Network-First (with Local Cache)** architecture. The mobile app communicates with the Laravel API to sync data with a central MySQL database, while using Room for offline support.

- **Mobile Architecture:** [MVVM + Repository Guide](./mobile-app/docs/ARCHITECTURE.md)
- **Backend Architecture:** [Service-Repository Guide](./backend-api/docs/ARCHITECTURE.md)

---
*BorrowHub — Making asset management simple and efficient.*
