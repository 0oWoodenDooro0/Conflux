# Track Specification: WebSocket Real-time Messaging

## Overview
Upgrade the existing HTTP-based message delivery system to use WebSockets for real-time updates. This will provide a more responsive user experience by instantly pushing new messages to connected clients without requiring polling or manual refreshes.

## Functional Requirements
- **WebSocket Server Integration**: Implement a Ktor WebSocket server to manage persistent connections.
- **Connection Management**:
    - Track active connections and associate them with specific users and channels.
    - Handle client connection and disconnection gracefully.
- **Real-time Broadcast**:
    - When a message is sent to a channel (via HTTP POST), broadcast it to all users currently connected to that channel's WebSocket.
- **Client-side WebSocket Client**:
    - Implement a WebSocket client in the KMP module to maintain a persistent connection.
    - Automatically update the `MainState.messages` list when a new message event is received.
- **Authentication**:
    - Use a dedicated token-based handshake for WebSocket connections to ensure security.
- **Resilience**:
    - Implement automatic reconnection with exponential backoff on the client side.
- **History Synchronization**:
    - Automatically synchronize missed messages upon reconnection to ensure no messages are lost during brief network interruptions.

## Non-Functional Requirements
- **Performance**: Message broadcast latency should be less than 100ms.
- **Scalability**: The system should handle hundreds of concurrent WebSocket connections (within memory limits of the H2/Ktor setup).

## Acceptance Criteria
- [ ] Users receive messages in real-time without refreshing the page.
- [ ] Sending a message via the UI instantly updates the sender's and other recipients' message lists in the same channel.
- [ ] WebSocket connections are authenticated using a token.
- [ ] If the connection drops, the client automatically reconnects and fetches missed messages.
- [ ] The system logs connection/disconnection events for debugging.

## Out of Scope
- Typing indicators.
- User presence status (online/offline).
- Read receipts.
- File transfers over WebSockets.
