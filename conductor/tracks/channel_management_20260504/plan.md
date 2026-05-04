# Implementation Plan: Channel Management

## Phase 1: Backend API & Authorization
- [ ] Task: Implement backend authorization logic
    - [ ] Write failing test for missing `CHANNEL_MANAGEMENT` permission on Edit/Delete Channel routes.
    - [ ] Implement permission check in `ChannelController` or relevant routing layer.
- [ ] Task: Implement Edit Channel API
    - [ ] Write failing test for renaming a channel via API.
    - [ ] Implement Edit API in `ChannelController` (update DB, broadcast WebSocket event).
- [ ] Task: Implement Delete Channel API
    - [ ] Write failing test for deleting a channel via API.
    - [ ] Implement Delete API in `ChannelController` (remove from DB, broadcast WebSocket event).
- [ ] Task: Conductor - User Manual Verification 'Backend API & Authorization' (Protocol in workflow.md)

## Phase 2: Frontend State & WebSocket Handling
- [ ] Task: Handle Edit Channel WebSocket Event
    - [ ] Write unit test for frontend state update on channel renamed event.
    - [ ] Implement frontend state logic to update channel list upon receiving edit event.
- [ ] Task: Handle Delete Channel WebSocket Event & Redirection
    - [ ] Write unit test for frontend state update on channel deleted event.
    - [ ] Implement frontend state logic to remove channel from list.
    - [ ] Implement redirection logic: if active channel is deleted, switch to the first available channel in the server.
- [ ] Task: Conductor - User Manual Verification 'Frontend State & WebSocket Handling' (Protocol in workflow.md)

## Phase 3: UI Implementation
- [ ] Task: Implement ChannelSidebar Gear Icon
    - [ ] Write unit test/UI test for gear icon visibility based on `CHANNEL_MANAGEMENT` permission.
    - [ ] Implement UI for gear icon in `ChannelSidebar`.
- [ ] Task: Implement Channel Settings UI
    - [ ] Write UI test for opening the settings view (matches Server Settings layout).
    - [ ] Implement the Settings View container.
- [ ] Task: Implement Settings UI Components
    - [ ] Write UI test for Rename input (with Create Channel validation logic).
    - [ ] Write UI test for Delete button and confirmation dialog.
    - [ ] Implement Rename input and hook to Edit Channel API.
    - [ ] Implement Delete button, confirmation dialog, and hook to Delete Channel API.
- [ ] Task: Conductor - User Manual Verification 'UI Implementation' (Protocol in workflow.md)