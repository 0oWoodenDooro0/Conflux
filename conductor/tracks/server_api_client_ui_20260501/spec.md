# Track Specification: Server Creation API, Client, and UI

## Overview
This track implements the end-to-end flow for creating a new server in Conflux. This includes the backend Ktor API, a shared Kotlin Multiplatform (KMP) client using Ktor-client, and a simple Compose Multiplatform UI for both Desktop and Web.

## Functional Requirements
- **Backend API**:
    - Endpoint: `POST /api/servers`
    - Request Body: JSON containing `name` (String) and `iconUrl` (String, optional).
    - Logic:
        - Generate a unique UUID for the server.
        - Set the current timestamp as `creationDate`.
        - Persist the server record using `ServerRepository` (H2 Database).
    - Response: JSON representation of the created `Server` object.
- **Shared API Client**:
    - Implementation: `ServerApiClient` class in `shared` module (`commonMain`).
    - Dependency: `ktor-client`.
    - Method: `suspend fun createServer(name: String, iconUrl: String?): Server`.
    - Responsibility: Serialize request, perform HTTP POST, and deserialize response.
- **Compose UI**:
    - Target: `composeApp` (Web and Desktop).
    - Components:
        - `TextField` for server name.
        - `Button` ("Create") to trigger the API call.
    - Behavior:
        - Start a Coroutine on button click.
        - Call `ServerApiClient.createServer`.
        - Show a success message (Toast/Snackbar) with the new Server ID on success.
        - Show an error message (Toast/Snackbar) on failure.

## Non-Functional Requirements
- **Type Safety**: Use Kotlin serialization for all JSON operations.
- **Asynchronous**: UI must remain responsive during the API call.
- **Code Sharing**: Maximize logic sharing in the `shared` module.

## Acceptance Criteria
- [ ] Backend API successfully saves a server to the H2 database and returns it.
- [ ] `ServerApiClient` correctly communicates with the backend in both JVM (Desktop) and WasmJs (Web) environments.
- [ ] The Compose UI allows users to create a server and displays feedback correctly.
- [ ] Automated tests cover the API endpoint and the client logic.

## Out of Scope
- Detailed server settings (roles, channels) beyond basic creation.
- Authentication (to be handled in a future track).
- Persistent frontend state/navigation.
