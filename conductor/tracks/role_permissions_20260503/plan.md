# Implementation Plan: Role and Permission System

## Phase 1: Database Layer
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
- [~] Task: Implement Permission query logic
    - [ ] Create test for querying permissions by Server ID and User ID.
    - [ ] Add Repository method for permission queries.
    - [ ] Verify test passes.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Database Layer' (Protocol in workflow.md)

## Phase 2: Backend Authorization Layer
- [ ] Task: Implement Permission Interception
    - [ ] Create test for request interception based on permissions (e.g., channel creation).
    - [ ] Implement middleware/check in Ktor routes to verify permissions and return 403.
    - [ ] Verify test passes.
- [ ] Task: Implement Management API (UC4)
    - [ ] Create test for updating/assigning roles via API.
    - [ ] Add route to update roles, protected by `roleManagementPrivilege`.
    - [ ] Verify test passes.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Backend Authorization Layer' (Protocol in workflow.md)

## Phase 3: Shared Layer
- [ ] Task: Update Shared Models
    - [ ] Create test for updated models serialization/deserialization.
    - [ ] Add Role/Permission data to `Server` and `User` data classes.
    - [ ] Verify test passes.
- [ ] Task: Update API Client
    - [ ] Create test for new API client methods.
    - [ ] Add methods in the KMP API Client to call the Management API.
    - [ ] Verify test passes.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Shared Layer' (Protocol in workflow.md)

## Phase 4: Frontend State
- [ ] Task: Manage `currentUserPermissions`
    - [ ] Create test for state update logic.
    - [ ] Update `MainState.kt` to store `currentUserPermissions` when switching servers.
    - [ ] Verify test passes.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Frontend State' (Protocol in workflow.md)

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