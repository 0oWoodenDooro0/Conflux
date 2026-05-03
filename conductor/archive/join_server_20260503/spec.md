# Specification: Join Server

## Overview
This track implements the functionality for a user to join an existing server using its Server ID. It spans from the database (Junction Table) to the Compose Multiplatform UI.

## Functional Requirements

### 1. Database & Repository (Backend)
- **Junction Table**: Verify and utilize the many-to-many relationship table (Junction Table) between Users and Servers in `StructuralModels.kt`.
- **Repository Logic**: In `ExposedServerRepository.kt`, implement a `joinServer(userId, serverId)` method.
- **Validations**:
  - Check if the target `serverId` exists in the database.
  - Check if the `userId` is already a member of the server (prevent duplicate entries).
  - If both checks pass, insert the relationship into the Junction Table.

### 2. Routes (Backend API)
- **Endpoint**: Expose a new POST endpoint `/servers/{id}/join` in `ServerRoutes.kt`.
- **Authentication**: Extract the `userId` from the authenticated session (e.g., JWT).
- **Parameters**: Extract the target `serverId` from the URL path.
- **Responses**:
  - `200 OK` or `201 Created` for successful join.
  - `404 Not Found` if the server doesn't exist.
  - `409 Conflict` (or similar) if the user is already a member.

### 3. API Client (Shared)
- **Method**: Add a `joinServer(serverId: String)` to `ServerApiClient.kt`.
- **Network Request**: Send a POST request to `/servers/{serverId}/join`.
- **Error Handling**: Catch backend errors and translate them into a state that the UI can interpret.

### 4. State Management (Frontend)
- **Action**: Add a function to handle the "join server" event in `MainState.kt`.
- **Syncing**: Upon a successful join, immediately trigger a refresh of the server list to sync the latest data.
- **Context Switch**: Upon success, automatically switch the UI context to the newly joined server (Auto-Switch).

### 5. UI (Compose Multiplatform)
- **Join Dialog**: Create a new Dialog component in the UI directory with a text input for the Server ID.
- **Trigger**: Add a "Join Server" button near the create server button in the sidebar.
- **Validation**: Validate that the input is a valid UUID before allowing submission.
- **Loading State**: Disable the "Join" button and show a circular spinner inside it while the request is processing.
- **Feedback**: Display transient Toast/Snackbar messages for success or backend errors (e.g., "Server Not Found", "Already Joined"), and close the dialog.

## Non-Functional Requirements
- **Performance**: The UI must remain responsive during the network request.
- **Error Messages**: Ensure clear and user-friendly error messages are provided via the Snackbar.

## Out of Scope
- Browsing or discovering public servers (joining is strictly via known ID).
- Sending an invitation link to users (this is manual ID entry).