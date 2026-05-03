# Implementation Plan: Role and Permission System

## Phase 1: Database Layer [checkpoint: d6322aa]
- [x] Task: Create `Role` and `Permission` structures [6091918]
    - [x] Create test for database structures and queries.
    - [x] Add `Role` and `Permission` tables/models in `StructuralModels.kt`.
    - [x] Verify test passes.
- [x] Task: Implement Owner role assignment on Server creation [fd601e2]
    - [x] Create test for Owner role assignment.
    - [x] Update Server Repository to assign "Owner" role to the creator.
    - [x] Verify test passes.
- [x] Task: Implement Member role assignment on Server join [16d3e9e]
    - [x] Create test for default "Member" role assignment.
    - [x] Update joining logic to assign "Member" role.
    - [x] Verify test passes.
- [x] Task: Implement Permission query logic [d6bdecd]
    - [x] Create test for querying permissions by Server ID and User ID.
    - [x] Add Repository method for permission queries.
    - [x] Verify test passes.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database Layer' (Protocol in workflow.md) [753a9a5]

## Phase 2: Backend Authorization Layer [checkpoint: c5e397d]
- [x] Task: Implement Permission Interception [e91ab94]
    - [x] Create test for request interception based on permissions (e.g., channel creation).
    - [x] Implement middleware/check in Ktor routes to verify permissions and return 403.
    - [x] Verify test passes.
- [x] Task: Implement Management API (UC4) [7242f96]
    - [x] Create test for updating/assigning roles via API.
    - [x] Add route to update roles, protected by `roleManagementPrivilege`.
    - [x] Verify test passes.
- [~] Task: Conductor - User Manual Verification 'Phase 2: Backend Authorization Layer' (Protocol in workflow.md)

## Phase 3: Shared Layer [checkpoint: b3d39b3]
- [x] Task: Update Shared Models [95966dc]
    - [x] Create test for updated models serialization/deserialization.
    - [x] Add Role/Permission data to `Server` and `User` data classes.
    - [x] Verify test passes.
- [x] Task: Update API Client [95966dc]
    - [x] Create test for new API client methods.
    - [x] Add methods in the KMP API Client to call the Management API.
    - [x] Verify test passes.
- [~] Task: Conductor - User Manual Verification 'Phase 3: Shared Layer' (Protocol in workflow.md)

## Phase 4: Frontend State
- [x] Task: Manage `currentUserPermissions` [bd102a0]
    - [x] Create test for state update logic.
    - [x] Update `MainState.kt` to store `currentUserPermissions` when switching servers.
    - [x] Verify test passes.
- [~] Task: Conductor - User Manual Verification 'Phase 4: Frontend State' (Protocol in workflow.md)

## Phase 5: UI Layer
- [ ] Task: Channel Management UI Protection
    - [ ] Create test/composable preview for disabled channel button.
    - [ ] Update `ChannelSidebar.kt` to hide/disable "+ (New Channel)" button based on `channelManagementPrivilege`.
    - [ ] Verify test passes.
- [ ] Task: Messaging UI Protection
    - [ ] Create test/composable preview for disabled chat input.
    - [ ] Update `ChatRoom.kt` to disable input and show text based on `messagingPrivilege`.
    - [ ] Verify test passes.
- [ ] Task: Server Settings UI
    - [ ] Create test/composable preview for server settings UI.
    - [ ] Create UI for viewing members and adjusting roles (for users with `roleManagementPrivilege`).
    - [ ] Verify test passes.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: UI Layer' (Protocol in workflow.md)