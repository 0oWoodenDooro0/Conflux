# Implementation Plan: Server Role Management & Developer Debug Tools

## Phase 1: Navigation & State Management [checkpoint: 2f3e2fe]
- [x] Task: Create `ServerSettingsTab` enum and update `ServerSettingsDialog.kt` to include a vertical sidebar for tab selection. 9ea61f0
- [x] Task: Implement a content switcher in `ServerSettingsDialog.kt` that displays placeholders for "Overview", "Channel Management", and "Members". 68ac841
- [x] Task: Conductor - User Manual Verification 'Phase 1: Navigation & State Management' (Protocol in workflow.md) 2f3e2fe

## Phase 2: Role Management UI & Basic Operations [checkpoint: 358f3e]
- [x] Task: Define/Update Role related models and API client methods in the shared module for fetching and creating roles. 5d55307
- [x] Task: Implement the `RolesAndPermissions` tab layout with a two-column design. cca0eea
- [x] Task: Implement the Role List (left column) showing role names and colors, with an "Add Role" button. cca0eea
- [x] Task: Write TDD tests for role creation and selection logic. cca0eea
- [x] Task: Conductor - User Manual Verification 'Phase 2: Role Management UI & Basic Operations' (Protocol in workflow.md) 358f3e

## Phase 3: Permissions Sub-tab [checkpoint: f52817]
- [x] Task: Implement the `Permissions` sub-tab in the Role Detail panel (right column). 3b463d
- [x] Task: Implement Toggle/Switch components for Messaging, Channel Management, Role Management, and Server Management permissions. 3b463d
- [x] Task: Implement bitmask manipulation logic for permission updates in the `shared` module. 3b463d
- [x] Task: Implement "Save Changes" and "Revert" functionality with TDD tests for state consistency. 3b463d
- [x] Task: Conductor - User Manual Verification 'Phase 3: Permissions Sub-tab' (Protocol in workflow.md) f52817

## Phase 4: Member Assignment Sub-tab [checkpoint: 0a0c35]
- [x] Task: Implement the `Members` sub-tab in the Role Detail panel (right column). 0a0c35
- [x] Task: Implement a searchable dropdown for selecting server members to add to a role. 0a0c35
- [x] Task: Implement the list of members currently in the role with a removal ("X") action. 0a0c35
- [x] Task: Write TDD tests for adding/removing members from roles. 0a0c35
- [x] Task: Conductor - User Manual Verification 'Phase 4: Member Assignment Sub-tab' (Protocol in workflow.md) 0a0c35

## Phase 5: Developer Debug Menu [checkpoint: 5e7581]
- [x] Task: Implement a reusable component or Modifier for right-click (PointerInput) context menus. e22cae1
- [x] Task: Integrate the debug menu into the Server Sidebar, Channel Sidebar, and User components to display relevant IDs. 2027a14
- [x] Task: Integrate the debug menu into Message and Role list items. 2027a14
- [x] Task: Implement the "Copy to Clipboard" functionality for IDs. 0d9d9a0
- [x] Task: Conductor - User Manual Verification 'Phase 5: Developer Debug Menu' (Protocol in workflow.md) 5e7581
