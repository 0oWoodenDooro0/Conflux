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

## Phase 2: Shared API Client & Models [checkpoint: bd10531]
- [x] Task: Update Shared Models (if necessary) 2739a11
    - [x] Ensure `Channel` model includes necessary fields (ID, Name, Type, Description, Metadata)
- [x] Task: Implement `ServerApiClient.getChannels(serverId: String)` d814056
    - [x] Write failing test in `ServerApiClientTest`
    - [x] Implement fetching logic using Ktor Client
    - [x] Verify test passes
- [x] Task: Conductor - User Manual Verification 'Shared API Client & Models' (Protocol in workflow.md) d814056

## Phase 3: Frontend State & Logic [checkpoint: 5059e95]
- [x] Task: Update `MainState` to handle server selection and channel list 34e3ac9
    - [x] Write failing test for state transitions
    - [x] Create `MainState` (or update existing) with `selectedServer` and `channelList`
    - [x] Implement `selectServer(server)` action that triggers API call
    - [x] Verify test passes
- [x] Task: Implement loading and error states for channel fetching 34e3ac9
    - [x] Write failing test for loading/error transitions
    - [x] Add `isFetchingChannels` and `channelFetchError` states
    - [x] Verify test passes
- [x] Task: Conductor - User Manual Verification 'Frontend State & Logic' (Protocol in workflow.md) 34e3ac9

## Phase 4: UI Components & Integration [checkpoint: 0f2a054]
- [x] Task: Update `ServerSidebar` to trigger selection 0f2a054
    - [x] Add click listener to server icons
    - [x] Call `selectServer` on click
- [x] Task: Implement `ChannelSidebar` Component 0f2a054
    - [x] Display the server name and a "Create Channel" button
    - [x] Implement skeleton/shimmer UI for loading
    - [x] Render channel list from state
- [x] Task: Integrate `ChannelCreationDialog` 0f2a054
    - [x] Connect the existing dialog to the "Create Channel" button
    - [x] Ensure successful creation refreshes the list or appends to state
- [x] Task: Integrate Toast Notification for Errors 0f2a054
    - [x] Trigger toast when `channelFetchError` is set
- [x] Task: Conductor - User Manual Verification 'UI Components & Integration' (Protocol in workflow.md) 0f2a054
