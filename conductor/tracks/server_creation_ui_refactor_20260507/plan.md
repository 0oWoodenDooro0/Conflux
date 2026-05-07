# Implementation Plan - Server Creation UI Refactor and Icon Removal

## Phase 1: Data Model and API Cleanup
Remove `icon_url` from the core data structures and server-side logic.

- [x] Task: Remove `icon_url` from Shared Models (63fe431)
    - [ ] Update `Server` data class in `shared` module.
    - [ ] Update `CreateServerRequest` or similar models in `shared`.
- [x] Task: Update Server-Side Database and Logic (1c21c51)
    - [ ] Remove `icon_url` column from `Servers` table in Exposed schema.
    - [ ] Update server-side repository/service to handle creation without `icon_url`.
    - [ ] Update server-side tests to reflect the model change.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Data Model and API Cleanup' (Protocol in workflow.md)

## Phase 2: Client-Side Logic and UI Cleanup
Remove `icon_url` references from the Compose Multiplatform UI.

- [ ] Task: Update Client API Calls
    - [ ] Update `ServerApiClient` to match the new request/response models.
- [ ] Task: Clean up existing UI components
    - [ ] Remove `icon_url` from Server Sidebar (ensure text initials are used).
    - [ ] Remove `icon_url` from Server Settings screen.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Client-Side Logic and UI Cleanup' (Protocol in workflow.md)

## Phase 3: Server Creation Dialog Implementation
Implement the new dialog-based server creation UI.

- [ ] Task: Implement `CreateServerDialog`
    - [ ] Create a new Material3 `AlertDialog` for server creation.
    - [ ] Include input field for server name only.
- [ ] Task: Integrate Dialog into Main UI
    - [ ] Update the action that previously triggered `createServerScreen` to show the dialog instead.
    - [ ] Remove the old `createServerScreen` implementation and navigation routes.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Server Creation Dialog Implementation' (Protocol in workflow.md)
