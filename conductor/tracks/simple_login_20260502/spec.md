# Specification: Simple Login and Server List

## Overview
Implement a simplified entry point for Conflux where users can enter a username to "log in" and view a list of servers they belong to. This bypasses complex authentication for initial prototyping while establishing the core flow from login to main application state.

## Functional Requirements
- **Backend (Ktor)**:
    - `POST /api/login`: Accepts a `LoginRequest` (username).
        - Searches for a user with the given username.
        - If found, returns the `User` object.
        - If not found, creates a new `User` and returns it.
    - `GET /api/servers?userId={id}`: Returns a list of `Server` objects where the user is a member or owner.
- **Frontend (Compose Multiplatform)**:
    - **Login Screen**:
        - Username input field with validation (alphanumeric, 3-20 characters).
        - "Login" button.
        - Calls Backend Login API on submit.
        - Stores the resulting `User` in a global/shared state (Memory only).
        - Navigates to the Main Screen on success.
        - Displays basic alerts for network or validation errors.
    - **Main Screen**:
        - Fetches the server list using the stored `userId` upon entry.
        - Displays a Discord-style sidebar on the left containing the servers.
        - Placeholder for channel/message area (not in scope for this track).

## Non-Functional Requirements
- **Standard Validation**: Usernames must be alphanumeric and between 3-20 characters.
- **In-Memory State**: Login state does not need to persist across browser refreshes or app restarts for this track.

## Acceptance Criteria
- [ ] User can "log in" by entering a username.
- [ ] A new user is created in the database if the username is new.
- [ ] Existing users are retrieved correctly.
- [ ] After login, the user is redirected to a screen showing their servers.
- [ ] The server list is displayed in a left-side sidebar.
- [ ] Errors (network, empty input) are handled gracefully with basic alerts.

## Out of Scope
- Password-based authentication or real security.
- Channel management and message viewing.
- Profile editing (avatars, discriminators).
- Persistence of login state (refreshing the page will log the user out).
