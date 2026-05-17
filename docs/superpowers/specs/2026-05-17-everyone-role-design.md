# Spec: @everyone Role System

## Overview
Introduce a permanent, immutable `@everyone` role for every server. This role represents the base permissions for all members (including the owner, though the owner retains override permissions). The legacy `Owner` and `User`/`Member` roles will be removed.

## Requirements
1.  **@everyone Role**:
    *   **Name**: Fixed as `@everyone`.
    *   **Priority**: Fixed at `-1` (lowest possible).
    *   **Assignment**: Implicitly assigned to every member. No entry in `member_roles` table.
    *   **Immutability**: Cannot be renamed, deleted, or have its priority changed. Only permissions (and optionally color) can be modified.
2.  **Removal of Legacy Roles**:
    *   New servers will no longer create "Owner" or "Member" roles.
    *   New members joining a server will not be assigned any default role (they rely on `@everyone`).
    *   Server owners will no longer be assigned an "Owner" role upon server creation.
3.  **Permission Calculation**:
    *   Total permissions = (Permissions from roles in `member_roles`) OR (Permissions from `@everyone` role).
    *   Owner override (ALL permissions) remains unchanged.

## Data Model Changes
No schema changes required. The `Roles` table will store the `@everyone` role with `priority_level = -1`.

## Logic Changes

### 1. Server Creation (`ExposedServerRepository.createServer`)
*   Remove logic that creates `DEFAULT_ROLE_NAME_OWNER` and `DEFAULT_ROLE_NAME_MEMBER`.
*   Remove logic that assigns the "Owner" role to the creator.
*   Add logic to create a role with:
    *   `name`: `@everyone`
    *   `priorityLevel`: `-1`
    *   `permissions`: Default base permissions (e.g., `MESSAGING`).

### 2. Member Joining (`ExposedServerRepository.joinServer`)
*   Remove logic that assigns `DEFAULT_ROLE_NAME_MEMBER` to the new member.

### 3. Permission Calculation (`ExposedServerRepository.getPermissionsForMember`)
*   Modify to fetch the `@everyone` role (where `priorityLevel = -1`) for the server.
*   Merge its permissions with the user's explicit roles.

### 4. Role Management Validation (`RoleController` / `ServerRoutes`)
*   **Update Role**:
    *   If `priorityLevel == -1`:
        *   Reject changes to `name`.
        *   Reject changes to `priorityLevel`.
*   **Delete Role**:
    *   Reject deletion if `priorityLevel == -1`.
*   **Create Role**:
    *   Ensure new roles have `priorityLevel >= 0`.

### 5. Role Query (`ExposedServerRepository.getRolesForMember`)
*   Ensure `@everyone` is included in the list of roles returned for a member, even if not explicitly in `member_roles`.

## Success Criteria
*   New servers have exactly one role: `@everyone`.
*   Owner of a new server has no assigned roles but has full permissions.
*   New members joining have no assigned roles but can perform actions allowed by `@everyone`.
*   API prevents changing `@everyone`'s name or priority.
*   API prevents deleting `@everyone`.
