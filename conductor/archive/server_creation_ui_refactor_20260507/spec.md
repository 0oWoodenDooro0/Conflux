# Track Specification: Server Creation UI Refactor and Icon Removal

## Overview
This track focuses on refactoring the server creation process to improve UX by using a dialog instead of a full-screen view, and simplifying the server data model by removing the `icon_url` field entirely from the system.

## Functional Requirements
- **Server Creation UI**:
    - Replace the `createServerScreen` with a Material3 `AlertDialog`.
    - The dialog should appear when the user clicks to create a new server.
    - Remove the `icon_url` input field from the server creation form.
- **Data Model Simplification**:
    - Remove `icon_url` from the `Server` shared model.
    - Update the database schema (Exposed) to remove the `icon_url` column from the `Servers` table.
    - Remove `icon_url` from all server-related API responses and requests.
- **UI Cleanup**:
    - Remove `icon_url` references from the Server Settings screen.
    - Remove `icon_url` (or placeholder icons) from the Server Sidebar.
    - Ensure the UI still looks good with just the server name or a text-based initial as an icon.

## Non-Functional Requirements
- **Type Safety**: Ensure all Kotlin models are updated and no stale references to `icon_url` remain.
- **Database Schema**: Update the database schema to remove the `icon_url` column.

## Acceptance Criteria
- [ ] Server creation UI is a Material3 Dialog.
- [ ] `icon_url` is removed from `shared`, `server`, and `composeApp` modules.
- [ ] Server Sidebar displays servers correctly without an icon URL (e.g., using initials).
- [ ] Server Settings no longer has an icon URL field.
- [ ] All tests pass after refactoring.

## Out of Scope
- Adding new server icon types (e.g., local image uploads).
- Any other UI changes unrelated to the server creation dialog or icon removal.
