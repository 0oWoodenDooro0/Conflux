# Specification: Channel Management & Settings UI

## Overview
Implement channel management functionality, including a settings UI accessible via a gear icon next to the channel name in the `ChannelSidebar`. The settings page will allow users with appropriate permissions to rename or delete the channel.

## Functional Requirements

### 1. UI: Gear Icon
- Display a "gear" icon on the far right of the channel name in the `ChannelSidebar` when hovered or active.
- **Permission:** The gear icon is ONLY visible to users who have the `ConfluxPermission.CHANNEL_MANAGEMENT` permission in that channel/server.

### 2. UI: Channel Settings View
- **Interaction:** Clicking the gear icon opens a channel settings page.
- **Layout:** The layout and style must mirror the existing "Server Settings" view (full-screen overlay or modal, matching current application behavior).
- **Components:**
  - **Rename Input:** A text input field pre-filled with the current channel name. Validation rules must be identical to the existing "Create Channel" validation logic.
  - **Delete Button:** A button to delete the channel. Clicking it must trigger a secondary confirmation dialog to prevent accidental deletion.

### 3. Backend: ChannelController API
- Implement backend API endpoints for:
  - **Edit Channel:** Update the channel's name.
  - **Delete Channel:** Remove the channel from the database.
- **Authorization:** Both endpoints MUST verify the user holds the `ConfluxPermission.CHANNEL_MANAGEMENT` permission before executing the action.

### 4. Real-time Updates (WebSocket)
- Upon successful execution of Edit or Delete actions via the API, the backend must broadcast a WebSocket event.
- **Frontend Reaction:**
  - On receiving an update event, the frontend automatically re-renders the channel list to reflect the new name.
  - On receiving a delete event, the frontend removes the channel from the list.
  - **User Redirection:** If a user is currently viewing a channel that is deleted, they should be automatically redirected to the top/first available channel in the server.

## Non-Functional Requirements
- Maintain existing Kotlin Multiplatform / Compose UI paradigms.
- Ensure smooth WebSocket event handling without blocking the main UI thread.

## Acceptance Criteria
- [ ] Gear icon only appears for users with `CHANNEL_MANAGEMENT` permission.
- [ ] Clicking the gear icon opens the settings UI matching the Server Settings style.
- [ ] Renaming a channel successfully updates the name across all connected clients via WebSocket.
- [ ] Deleting a channel prompts for confirmation, then successfully removes it for all connected clients.
- [ ] Users viewing a deleted channel are redirected to the first channel in the server.
- [ ] Users without `CHANNEL_MANAGEMENT` cannot edit or delete channels via API directly (verified by backend auth).