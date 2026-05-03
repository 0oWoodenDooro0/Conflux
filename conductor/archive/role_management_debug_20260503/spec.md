# Specification: Server Role Management & Developer Debug Tools

## Overview
This track implements a comprehensive server role management system within the `ServerSettingsDialog`. It introduces a multi-tabbed interface for server management and a developer-focused debug menu to facilitate backend interaction and data verification.

## Functional Requirements

### 1. Server Settings Navigation
- **Navigation Menu**: Add a vertical tab menu on the left side of `ServerSettingsDialog`.
- **Tabs**:
    - Overview (Placeholder)
    - Channel Management (Placeholder)
    - Roles & Permissions (Primary implementation)
    - Members (Placeholder)
- **State Management**: Clicking a tab switches the content in the main (right) panel.

### 2. Role Management Interface
- **Layout**: Two-column design within the "Roles & Permissions" tab.
- **Left Column (Role List)**:
    - Display all server roles including the default `@everyone`.
    - "Add Role" button at the top.
    - Display role name and associated color.
- **Right Column (Role Details)**:
    - Displayed when a role is selected from the left column.
    - Organized into sub-tabs: **Permissions** and **Members**.

### 3. Permissions Control (Sub-tab)
- **UI**: Use Toggle/Switch or Checkbox for each permission.
- **Permissions to Implement**:
    - Messaging (messagingPrivilege)
    - Channel Management (channelManagementPrivilege)
    - Role Management (roleManagementPrivilege)
    - Server Management (serverManagementPrivilege)
- **Actions**: "Save Changes" and "Revert" buttons at the bottom.

### 4. Member Assignment (Sub-tab)
- **Member List**: Display all users currently assigned to the selected role.
- **Add Member**: A searchable dropdown menu to select a server member and add them to the role.
- **Remove Member**: An "X" button next to each member in the list to remove them from the role.

### 5. Developer Debug Menu
- **Trigger**: Right-click (Pointer Event) on specific UI elements.
- **Target Elements & Information**:
    - **Server Icon/Sidebar**: Display `server_id` and current `user_id`.
    - **Channel Name/Sidebar**: Display `channel_id` and its `server_id`.
    - **User Avatar/Name**: Display `user_id`.
    - **Messages**: Display `message_id`.
    - **Roles**: Display `role_id`.
- **UI**: Dropdown Menu / Context Menu.
- **Utility**: "Copy" icon/button next to each ID to copy it to the clipboard.

## Non-Functional Requirements
- **Performance**: Role list and permission toggles should be reactive and performant using Compose Multiplatform states.
- **Reliability**: Use bitmask-based permissions as defined in the Tech Stack.

## Acceptance Criteria
- [ ] `ServerSettingsDialog` has a working vertical tab menu.
- [ ] Users can create new roles and edit their names and colors.
- [ ] Permissions can be toggled and saved for each role.
- [ ] Members can be added to and removed from roles via the UI.
- [ ] Right-clicking on servers, channels, users, messages, and roles opens a debug menu with the correct IDs.
- [ ] Clicking "Copy" in the debug menu successfully copies the ID to the clipboard.

## Out of Scope
- Implementation of the "Overview", "Channel Management", and "Members" (general list) tabs beyond placeholders.
- Complex permission enforcement logic (this track focuses on management UI and data persistence).
