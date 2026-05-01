# Implementation Plan - Channel Creation

## Phase 1: Backend API Implementation
- [ ] Task: Backend - Implement `POST /api/servers/{serverId}/channels` Route
    - [ ] Task: Write failing integration tests for the new route in `ApplicationTest.kt`.
    - [ ] Task: Implement server existence check in the route.
    - [ ] Task: Implement channel creation logic calling `ChannelRepository.createChannel`.
    - [ ] Task: Verify tests pass (Green Phase).
- [ ] Task: Conductor - User Manual Verification 'Backend API Implementation' (Protocol in workflow.md)

## Phase 2: Shared API Client Implementation
- [ ] Task: Shared - Extend `ServerApiClient` with `createChannel`
    - [ ] Task: Write failing unit tests for `createChannel` in `ServerApiClientTest.kt`.
    - [ ] Task: Implement `createChannel(serverId: String, name: String): Channel` using Ktor Client.
    - [ ] Task: Verify tests pass (Green Phase).
- [ ] Task: Conductor - User Manual Verification 'Shared API Client Implementation' (Protocol in workflow.md)

## Phase 3: Compose UI Implementation
- [ ] Task: UI - Implement Channel Creation Modal
    - [ ] Task: Create a reusable `ChannelCreationDialog` component.
    - [ ] Task: Implement client-side validation logic (non-empty, length < 32, alphanumeric/hyphens).
    - [ ] Task: Implement success and generic error message feedback in the UI.
    - [ ] Task: Integrate the `createChannel` API call into the dialog.
    - [ ] Task: Add a "+" button to the server view to trigger the `ChannelCreationDialog`.
- [ ] Task: Conductor - User Manual Verification 'Compose UI Implementation' (Protocol in workflow.md)
