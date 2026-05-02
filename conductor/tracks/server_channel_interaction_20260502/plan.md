# Implementation Plan: Server-Channel Interaction & Creation

## Phase 1: Backend Implementation (API & Repository) [checkpoint: 0c6df14]
- [x] Task: Implement `ChannelRepository.getChannelsByServer` (Already exists)
    - [x] Write failing test for repository method (Verified)
    - [x] Implement repository logic in `ExposedChannelRepository` (Verified)
    - [x] Verify test passes (Verified)
- [x] Task: Create `GET /api/servers/{serverId}/channels` Route a30f75e
    - [x] Write failing integration test for the route
    - [x] Implement route in `ServerRoutes.kt`
    - [x] Verify test passes
- [x] Task: Verify `POST /api/servers/{serverId}/channels` Route a30f75e
    - [x] Ensure the existing implementation matches the current needs
    - [x] Run existing tests to confirm functionality
- [x] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md) a30f75e

## Phase 2: Shared API Client & Models
- [ ] Task: Update Shared Models (if necessary)
    - [ ] Ensure `Channel` model includes necessary fields (ID, Name, Type, Description, Metadata)
- [ ] Task: Implement `ServerApiClient.getChannels(serverId: String)`
    - [ ] Write failing test in `ServerApiClientTest`
    - [ ] Implement fetching logic using Ktor Client
    - [ ] Verify test passes
- [ ] Task: Conductor - User Manual Verification 'Shared API Client & Models' (Protocol in workflow.md)

## Phase 3: Frontend State & Logic
- [ ] Task: Update `MainState` to handle server selection and channel list
    - [ ] Write failing test for state transitions
    - [ ] Add `selectedServer` and `channelList` to the state management
    - [ ] Implement `selectServer(server)` action that triggers API call
    - [ ] Verify test passes
- [ ] Task: Implement loading and error states for channel fetching
    - [ ] Write failing test for loading/error transitions
    - [ ] Add `isFetchingChannels` and `channelFetchError` states
    - [ ] Verify test passes
- [ ] Task: Conductor - User Manual Verification 'Frontend State & Logic' (Protocol in workflow.md)

## Phase 4: UI Components & Integration
- [ ] Task: Update `ServerSidebar` to trigger selection
    - [ ] Add click listener to server icons
    - [ ] Call `selectServer` on click
- [ ] Task: Implement `ChannelSidebar` Component
    - [ ] Display the server name and a "Create Channel" button
    - [ ] Implement skeleton/shimmer UI for loading
    - [ ] Render channel list from state
- [ ] Task: Integrate `ChannelCreationDialog`
    - [ ] Connect the existing dialog to the "Create Channel" button
    - [ ] Ensure successful creation refreshes the list or appends to state
- [ ] Task: Integrate Toast Notification for Errors
    - [ ] Trigger toast when `channelFetchError` is set
- [ ] Task: Conductor - User Manual Verification 'UI Components & Integration' (Protocol in workflow.md)
