# Track Specification: Channel Creation

## Overview
This track implements the end-to-end flow for creating a new channel within a server in Conflux. This includes the backend Ktor API, extending the shared KMP client, and adding a UI component for channel creation.

## Functional Requirements
- **Backend API**:
    - Endpoint: `POST /api/servers/{serverId}/channels`
    - Request Body: JSON containing `name` (String).
    - Logic:
        - Validate that `serverId` exists.
        - Create a new channel using `ChannelRepository.createChannel`.
    - Response: JSON representation of the created `Channel` object.
- **Shared API Client**:
    - Implementation: Extend `ServerApiClient` in `shared` module.
    - Method: `suspend fun createChannel(serverId: String, name: String): Channel`.
    - Responsibility: Perform HTTP POST to the new endpoint.
- **Compose UI**:
    - Integration: A dialog/modal that opens when a "+" button is clicked (e.g., in the server view).
    - Validation:
        - Name must not be empty.
        - Name length limit (e.g., 32 characters).
        - Character restriction (alphanumeric and hyphens).
    - Behavior:
        - Show a success message upon creation.
        - Show a generic error message if creation fails.
        - Stay on the current page after creation.

## Non-Functional Requirements
- **Validation**: Ensure `serverId` is valid on the backend before attempting channel creation.
- **Type Safety**: Use Kotlin serialization for all API data.
- **Asynchronous**: UI must remain responsive during the network call.

## Acceptance Criteria
- [ ] Backend API correctly creates a channel for a given server and returns it.
- [ ] API client correctly calls the backend and handles the response.
- [ ] UI allows users to input a name in a modal and trigger channel creation.
- [ ] Client-side validation prevents invalid channel names.
- [ ] UI displays success or generic error messages correctly.
- [ ] Unit tests for API and Client logic pass.

## Out of Scope
- Detailed channel permissions.
- Deleting or editing channels.
- Real-time updates to the channel list (outside of the immediate creation feedback).
