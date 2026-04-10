# Tracking: Standardize Log Action Filters (Issue #110)

This document tracks the implementation of log action filter standardization across the BorrowHub monorepo.

## Parent Issue
[#110 – Standardize Log Action Filters for Activity and Transaction Logs](https://github.com/99lash/BorrowHub/issues/110)

## Sub-Issues / Sub-PRs

| # | Component | Title | Status |
|---|-----------|-------|--------|
| #111 | backend-api (Laravel) | Standardize log action filters in LogService and API endpoints | Pending |
| #112 | mobile-app (Android) | Update Spinner labels and ViewModel mappings for standardized log actions | Pending |

## Implementation Plan
See full strategy: [`LOG_FILTER_REFINEMENT_PLAN.md`](./LOG_FILTER_REFINEMENT_PLAN.md)

### Backend (#111)
- Update `LogService.php` constants to use `borrowed`, `returned`, `created`, `updated`, `deleted`
- Ensure `GET /api/v1/activity-logs` and `GET /api/v1/transaction-logs` accept standardized action query parameters

### Mobile (#112)
- Update `strings.xml` filter labels
- Update `TransactionLogsFragment` and `ActivityLogsFragment` Spinners
- Update `TransactionViewModel` and `ActivityLogViewModel` action mappings
