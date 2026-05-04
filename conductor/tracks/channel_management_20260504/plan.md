# Implementation Plan: Channel Management

## Phase 1: Backend API & Authorization [checkpoint: e953171]
- [x] Task: Implement backend authorization logic [0baed80]
    - [x] Write failing test for missing `CHANNEL_MANAGEMENT` permission on Edit/Delete Channel routes.
    - [x] Implement permission check in `ChannelController` or relevant routing layer.
- [x] Task: Implement Edit Channel API [cc288e4]
    - [x] Write failing test for renaming a channel via API.
    - [x] Implement Edit API in `ChannelController` (update DB, broadcast WebSocket event).
- [x] Task: Implement Delete Channel API [ca91230]
    - [x] Write failing test for deleting a channel via API.
    - [x] Implement Delete API in `ChannelController` (remove from DB, broadcast WebSocket event).
- [x] Task: Conductor - User Manual Verification 'Backend API & Authorization' (Protocol in workflow.md) [e953171]

## Phase 2: Frontend State & WebSocket Handling [checkpoint: 9273f75]
- [x] Task: Handle Edit Channel WebSocket Event [07e51eb]
    - [x] Write unit test for frontend state update on channel renamed event.
    - [x] Implement frontend state logic to update channel list upon receiving edit event.
- [x] Task: Handle Delete Channel WebSocket Event & Redirection [7c80691]
    - [x] Write unit test for frontend state update on channel deleted event.
    - [x] Implement frontend state logic to remove channel from list.
    - [x] Implement redirection logic: if active channel is deleted, switch to the first available channel in the server.
- [x] Task: Conductor - User Manual Verification 'Frontend State & WebSocket Handling' (Protocol in workflow.md) [9273f75]

## Phase 3: UI Implementation [checkpoint: 397506e]
- [x] Task: Implement ChannelSidebar Gear Icon [5efe2b7]
    - [x] Write unit test/UI test for gear icon visibility based on `CHANNEL_MANAGEMENT` permission.
    - [x] Implement UI for gear icon in `ChannelSidebar`.
- [x] Task: Implement Channel Settings UI [5efe2b7]
    - [x] Write UI test for opening the settings view (matches Server Settings layout).
    - [x] Implement the Settings View container.
- [x] Task: Implement Settings UI Components [5efe2b7]
    - [x] Write UI test for Rename input (with Create Channel validation logic).
    - [x] Write UI test for Delete button and confirmation dialog.
    - [x] Implement Rename input and hook to Edit Channel API.
    - [x] Implement Delete button, confirmation dialog, and hook to Delete Channel API.
- [x] Task: Conductor - User Manual Verification 'UI Implementation' (Protocol in workflow.md) [397506e]

## Phase: Review Fixes
- [x] Task: Apply review suggestions: Expand WebSocket broadcasting scope to server-level [cde8d43]