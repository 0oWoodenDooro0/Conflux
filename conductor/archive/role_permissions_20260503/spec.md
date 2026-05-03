# Specification: Role and Permission System

## Overview
Implement a comprehensive Role and Permission system to manage user access and capabilities within Conflux servers. This system will control actions such as sending messages and managing channels, fulfilling the requirement for fine-grained access control.

## Functional Requirements
1. **Database Layer**
   - Create `Role` and `Permission` data structures in `StructuralModels.kt` (including `priorityLevel`, `messagingPrivilege`, `channelManagementPrivilege`, and a new `roleManagementPrivilege`).
   - Automatically assign the "Owner" role to the creator of a server.
   - Automatically provide a default "Member" role with basic messaging privileges for new users joining a server.
   - Implement Repository methods to query a user's permissions by Server ID and User ID.

2. **Backend Authorization Layer**
   - Intercept requests (e.g., adding/deleting channels) and verify permissions (`channelManagementPrivilege`).
   - Reject unauthorized requests with HTTP 403 Forbidden.
   - Create a Management API endpoint to update/assign roles, restricted to users with `roleManagementPrivilege`.

3. **Shared Layer**
   - Update `Server` and `User` data classes in shared models to include current `Role` or `Permission` data.
   - Add API client methods to call the role update endpoint.

4. **Frontend State**
   - Track `currentUserPermissions` in `MainState.kt` when switching servers.

5. **UI Layer**
   - **Channel Management:** Hide or disable the "+ (New Channel)" button if `channelManagementPrivilege` is false.
   - **Messaging:** Disable the chat input and display a clear text message indicating the lack of permission if `messagingPrivilege` is false.
   - **Server Settings:** Provide an interface for authorized users (with `roleManagementPrivilege`) to view members and adjust their roles.

## Non-Functional Requirements
- **Security:** "Defense in depth" - backend must independently verify all permissions regardless of UI state.
- **Performance:** Permission queries must be optimized for fast resolution during request interception.

## Acceptance Criteria
- [ ] Server creators are assigned the "Owner" role automatically.
- [ ] Users without `channelManagementPrivilege` receive a 403 error if they attempt to manage channels via API.
- [ ] The UI correctly disables the chat input and shows a message when a user lacks `messagingPrivilege`.
- [ ] Users with `roleManagementPrivilege` can successfully assign roles to other members via the Server Settings UI.

## Out of Scope
- Custom permission creation (users creating entirely new permissions beyond the predefined ones).