# Specification: Static Historical Messages (HTTP-based)

## Overview
Implementation of a robust, HTTP-based system for retrieving and sending messages within channels. This track focuses on persistence and basic message flow without real-time (WebSocket) capabilities, emphasizing textual precision and reliable history.

## Functional Requirements

### Backend (Ktor)
- **Database Schema**:
    - Use JetBrains Exposed to define/update the `Messages` table.
    - Fields: `id` (UUID), `channelId` (FK), `senderId` (FK), `content` (Text, max 2000 chars), `timestamp` (Long/Instant).
- **Repository**:
    - Implement `MessageRepository` with:
        - `saveMessage(channelId: String, senderId: String, content: String): Message?`
        - `getMessagesByChannel(channelId: String): List<Message>` (ordered by timestamp).
- **API Endpoints**:
    - `POST /api/channels/{channelId}/messages`: Save a new message.
    - `GET /api/channels/{channelId}/messages`: Retrieve all messages for a channel.

### Shared Logic (KMP)
- **API Client**:
    - Extend `ServerApiClient` in the `shared` module with methods to call the new endpoints.
    - Add `Message` model to shared models if not already present.

### Frontend (Compose Multiplatform)
- **UI Components**:
    - **Chat Room UI**: Right-side panel for the selected channel.
    - **Message List**: `LazyColumn` displaying messages.
    - **Message Input**: Bottom text field with auto-focus upon entering the channel.
- **Interactions**:
    - Pressing Enter or the Send button triggers the `POST` API.
    - Successful send triggers a `GET` API call to refresh the message list.
    - **Auto-Scroll**: Automatically scroll to the bottom when new messages arrive.
    - **Scroll Button**: Show a "Scroll to Bottom" button when the user has scrolled up.
- **Constraints**:
    - Message limit: 2000 characters.
    - Error Handling: Inline error messages shown above the input field on failure.

## Acceptance Criteria
- [ ] Users can view historical messages when selecting a channel.
- [ ] Users can send messages; the list updates immediately (via refresh) upon success.
- [ ] Input field automatically focuses when a channel is opened.
- [ ] Messages are limited to 2000 characters.
- [ ] API failures show inline error messages.
- [ ] Data persists in H2 database and survives restarts.

## Out of Scope
- Real-time updates via WebSockets.
- Message editing or deletion.
- Rich text formatting (Markdown support).
