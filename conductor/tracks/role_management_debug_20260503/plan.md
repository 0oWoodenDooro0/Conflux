# Implementation Plan: Server Role Management & Developer Debug Tools

## Phase 1: Navigation & State Management
- [x] Task: Create `ServerSettingsTab` enum and update `ServerSettingsDialog.kt` to include a vertical sidebar for tab selection. 9ea61f0
- [x] Task: Implement a content switcher in `ServerSettingsDialog.kt` that displays placeholders for "Overview", "Channel Management", and "Members". 68ac841
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Navigation & State Management' (Protocol in workflow.md)

## Phase 2: Role Management UI & Basic Operations
- [ ] Task: Define/Update `Role` related models and API client methods in the `shared` module for fetching and creating roles.
- [ ] Task: Implement the `RolesAndPermissions` tab layout with a two-column design.
- [ ] Task: Implement the Role List (left column) showing role names and colors, with an "Add Role" button.
- [ ] Task: Write TDD tests for role creation and selection logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Role Management UI & Basic Operations' (Protocol in workflow.md)

## Phase 3: Permissions Sub-tab
- [ ] Task: Implement the `Permissions` sub-tab in the Role Detail panel (right column).
- [ ] Task: Implement Toggle/Switch components for Messaging, Channel Management, Role Management, and Server Management permissions.
- [ ] Task: Implement bitmask manipulation logic for permission updates in the `shared` module.
- [ ] Task: Implement "Save Changes" and "Revert" functionality with TDD tests for state consistency.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Permissions Sub-tab' (Protocol in workflow.md)

## Phase 4: Member Assignment Sub-tab
- [ ] Task: Implement the `Members` sub-tab in the Role Detail panel (right column).
- [ ] Task: Implement a searchable dropdown for selecting server members to add to a role.
- [ ] Task: Implement the list of members currently in the role with a removal ("X") action.
- [ ] Task: Write TDD tests for adding/removing members from roles.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Member Assignment Sub-tab' (Protocol in workflow.md)

## Phase 5: Developer Debug Menu
- [ ] Task: Implement a reusable component or Modifier for right-click (PointerInput) context menus.
- [ ] Task: Integrate the debug menu into the Server Sidebar, Channel Sidebar, and User components to display relevant IDs.
- [ ] Task: Integrate the debug menu into Message and Role list items.
- [ ] Task: Implement the "Copy to Clipboard" functionality for IDs.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Developer Debug Menu' (Protocol in workflow.md)
