# Design Doc: Enforce singleLine in Server Settings and Roles

## Context
Code review for Task 3 found missing fields that should also be `singleLine` for consistency. These fields are used for names, priorities, and search queries, where multi-line input is not appropriate.

## Goal
Enforce `singleLine = true` for specific `OutlinedTextField` components in the Server Settings and Roles UI.

## Scope

### `ServerSettingsDialog.kt`
- **Component:** `RoleCreationDialog`
- **Fields:**
    - "Role Name"
    - "Priority Level"

### `RolesAndPermissionsTab.kt`
- **Component:** `RoleGeneralSettings`
- **Fields:**
    - "Priority Level"
- **Component:** `MemberAssignmentView`
- **Fields:**
    - "Add Member" (Search field)

## Proposed Changes

### 1. `ServerSettingsDialog.kt`
Update `RoleCreationDialog` to set `singleLine = true` for both `OutlinedTextField` instances.

### 2. `RolesAndPermissionsTab.kt`
Update `RoleGeneralSettings` to set `singleLine = true` for the "Priority Level" `OutlinedTextField`.
Update `MemberAssignmentView` to set `singleLine = true` for the "Add Member" `OutlinedTextField`.

## Verification Plan
- **Manual Testing:** Run the application and verify that pressing "Enter" in these fields does not create a new line.
- **Visual Check:** Ensure no layout regressions.
