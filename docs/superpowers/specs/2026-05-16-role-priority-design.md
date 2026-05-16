# Design Spec: Role Priority Management

## Overview
This feature allows server administrators to manage role hierarchy by assigning a priority level to each role. The role list will be sorted based on these levels, ensuring higher priority roles appear at the top.

## Goals
- Allow users to edit `priorityLevel` for any role via a numeric input.
- Automatically sort the role list in descending order of `priorityLevel`.
- Ensure consistency between client-side state and server-side storage.

## Proposed Changes

### 1. Shared Module (Data Models)
- `Role`, `CreateRoleRequest`, and `UpdateRoleRequest` already contain `priorityLevel`. No changes needed to the models themselves.

### 2. Client (Compose Multiplatform)
- **State Management (`RolesAndPermissionsState.kt`)**:
    - Add `pendingPriority` to track unsaved priority changes.
    - Update `hasChanges` to check for priority changes.
    - Update `updateRoles` to sort roles by `priorityLevel` descending.
- **UI (`RolesAndPermissionsTab.kt`)**:
    - Add a "Priority" section in the role details (Permissions tab or a new General tab).
    - Implement a `TextField` with `KeyboardType.Number` to edit priority.
    - Pass the new priority to `onSaveChanges`.

### 3. Server (Ktor + Exposed)
- **Repository (`ExposedServerRepository.kt`)**:
    - Update `getRoles` to include `.orderBy(Roles.priorityLevel, SortOrder.DESC)`.
    - This ensures any client fetching roles gets them pre-sorted.

## User Interaction Flow
1. User opens Server Settings > Roles.
2. User selects a role from the left list (sorted 100 -> 0).
3. User changes the "Priority Level" number in the right panel.
4. "Save Changes" button becomes active.
5. User clicks "Save".
6. Role is updated on the server, and the list re-sorts itself if the priority changed relative to other roles.

## Success Criteria
- Roles with higher numbers appear first in the list.
- Modifying the priority and saving reflects immediately in the UI order.
- Refreshing the data maintains the correct sort order.
