# Design: Client UI Updates for Role Priority

Adding UI elements to allow users to view and edit role priority levels in the server settings.

## Architecture

### Components

1.  **RoleGeneralSettings (New Composable)**:
    -   A new composable function in `RolesAndPermissionsTab.kt`.
    -   Displays an `OutlinedTextField` for the "Priority Level".
    -   Handles numeric input validation.

2.  **RolesAndPermissionsTab**:
    -   Updated to include `RoleGeneralSettings`.
    -   Updated `onSaveChanges` signature to include priority.
    -   Passes `state.pendingPriority` to the save action.

3.  **ServerSettingsDialog**:
    -   Updated to handle the new `onSaveChanges` signature.
    -   Calls `apiClient.updateRole` with the new priority.

## Data Flow

1.  User selects a role in the `RolesAndPermissionsTab`.
2.  `RolesAndPermissionsState` is updated with the selected role's priority.
3.  User edits the priority in `RoleGeneralSettings`.
4.  `updatePendingPriority` is called in `RolesAndPermissionsState`.
5.  "Save Changes" button becomes enabled (if priority changed).
6.  User clicks "Save Changes".
7.  `onSaveChanges(role, permissions, priority)` is invoked.
8.  `ServerSettingsDialog` receives the callback and calls the API.

## Error Handling

-   Input validation: Only allow integers in the priority text field.
-   API failures: Handled by the existing try-catch block in `ServerSettingsDialog`.

## Testing

-   Manual verification of the UI element appearing.
-   Manual verification of entering different priority values.
-   Manual verification that saving updates the role and re-sorts the list (since `RolesAndPermissionsState` sorts roles by priority).
