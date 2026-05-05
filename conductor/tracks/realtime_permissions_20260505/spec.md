# Specification: Real-time Permissions & Auto-General Channel

## 1. Overview
This track implements real-time authorization updates and improves the server onboarding experience. It ensures that permission changes are immediately reflected across all online members via WebSockets and that every new server starts with a default #general channel.

## 2. Functional Requirements
- **Backend: Permission Broadcasting**
    - When a role's permissions or a member's roles are updated and saved, the backend must identify all online members of that server.
    - The backend will broadcast a WebSocket event (PERMISSION_UPDATE) containing the relevant ID (User ID or Role ID) that was changed.
- **Backend: Auto-Channel Creation**
    - During the "Create Server" flow, the backend must automatically generate a default channel named #general.
    - The #general channel must have a unique, system-generated ID.
    - The server creator should be automatically granted full access to this channel.
- **Frontend: Reactive Updates**
    - Listen for PERMISSION_UPDATE WebSocket events.
    - Upon receiving an event, the frontend will trigger a Reactive Re-fetch of the current user's permissions for the active server/channel context.
- **Frontend: UI Constraints**
    - Message Permission: If SEND_MESSAGES is revoked, the message input/send button must be disabled.
    - Channel Permission: If MANAGE_CHANNELS is revoked, the "Channel Settings" menu/option must be hidden.
    - Server Permission: If MANAGE_SERVER is revoked, the "Server Settings" menu/option must be hidden.

## 3. Non-Functional Requirements
- Consistency: Ensure the UI state remains synchronized with the backend authority even during network latency.
- Efficiency: Use ID-only notifications to keep WebSocket payloads small.

## 4. Acceptance Criteria
- [ ] Creating a new server automatically results in a #general channel appearing in the sidebar.
- [ ] Changing a user's role in one client causes another client (logged in as that user) to immediately disable/hide restricted UI elements without a manual refresh.
- [ ] Revoking SEND_MESSAGES disables the input field in real-time.
- [ ] Revoking MANAGE_SERVER hides the server settings button in real-time.

## 5. Out of Scope
- Complex permission inheritance UI (only basic bitmask updates are covered).
- Offline notification for permission changes (only online users are notified).
