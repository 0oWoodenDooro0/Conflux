# Specification: Server-Channel Interaction & Creation

## Overview
This track implements the reactive linkage between selecting a server and displaying its associated channels, as well as the ability to create new channels within the selected server.

## Functional Requirements
- **Backend API**:
    - `GET /api/servers/{serverId}/channels`: Fetch all channels for a server.
    - `POST /api/servers/{serverId}/channels`: Create a new channel in a server (Already partially implemented, verify and integrate).
- **Frontend State**:
    - Track `selectedServer` (Server object or ID) in the application state.
    - Track `channelList` (List of Channel objects) for the currently selected server.
    - Handle channel fetching state (loading, error).
    - Handle channel creation state (modal visibility, submission status).
- **Interaction**:
    - Clicking a server in the `ServerSidebar` updates the `selectedServer`.
    - Updating `selectedServer` triggers an API call to fetch channels.
    - While fetching, show a skeleton/shimmer loading state in the channel list area.
    - "Create Channel" button appears in the channel sidebar when a server is active.
    - Submitting the creation dialog adds the new channel to the list.
- **UI Components**:
    - Channel Sidebar: Displays the list of channels and the "Add" button.
    - Channel Creation Dialog: Modal for inputting new channel info (Integrate existing component).
    - Toast: Notification for errors.

## Acceptance Criteria
- Clicking a server icon in the sidebar highlights the server and triggers a fetch.
- The channel list is populated with correct channel names.
- [ ] If fetching fails, a toast notification is shown.
- [ ] "Create Channel" button is visible when a server is selected.
- [ ] Successfully created channel appears in the list immediately.
- [ ] No channel is automatically selected when switching servers.

## Out of Scope
- Detailed channel permissions.
- Deleting or editing channels.
- Real-time updates (WebSockets) for list changes.
