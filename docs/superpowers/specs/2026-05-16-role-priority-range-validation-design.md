# Design Spec: Role Priority Range Validation (0-100)

## Overview
This specification defines the requirements and implementation details for restricting role priority levels to a range between 0 and 100 (inclusive). This ensures a consistent hierarchy and prevents unexpected behavior from arbitrary integer values.

## Goals
- Enforce a strict `0-100` range for `priorityLevel` in both the client UI and the server API.
- Provide immediate visual feedback to users when an invalid priority is entered.
- Ensure data integrity by validating input at the API layer.

## Proposed Changes

### 1. Client (Compose Multiplatform)

#### State Management (`RolesAndPermissionsState.kt`)
- Add a derived property `isPriorityValid: Boolean` which returns true if `pendingPriority` is null (no change) or within `0..100`.
- Update `hasChanges` to consider `isPriorityValid`.

#### UI (`RolesAndPermissionsTab.kt`)
- Update the priority `TextField`:
    - Set `isError = !state.isPriorityValid`.
    - Add a `supportingText` displaying "Priority must be between 0 and 100" when invalid.
- Disable the "Save Changes" button if `!state.isPriorityValid`.

### 2. Server (Ktor)

#### Route Validation (`ServerRoutes.kt`)
- In the `patch("/servers/{serverId}/roles/{roleId}")` (or equivalent update) route:
    - Extract `priorityLevel` from the request body.
    - If `priorityLevel` is provided and is not in the range `0..100`, return `HttpStatusCode.BadRequest` with a descriptive error message: `{"error": "Priority level must be between 0 and 100"}`.

### 3. Shared (Optional/Future Proofing)
- Consider adding a constant `MIN_PRIORITY = 0` and `MAX_PRIORITY = 100` in the `shared` module for consistency, though hardcoding in the validation logic is acceptable for this initial scope.

## User Interaction Flow
1. User enters "105" in the Priority Level field.
2. The text field turns red (error state).
3. A message appears below the field: "Priority must be between 0 and 100".
4. The "Save Changes" button is disabled.
5. User changes value to "50".
6. Error state clears, "Save Changes" becomes active.
7. User clicks "Save", and the change is persisted.

## Success Criteria
- Priority levels cannot be saved if they are outside the 0-100 range.
- The UI clearly communicates the range restriction.
- Direct API calls with invalid priorities are rejected by the server.
