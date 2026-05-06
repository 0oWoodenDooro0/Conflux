# Initial Concept
Conflux - A lightweight communication platform focused on textual precision and reliable history.

# Product Vision
To provide a core, pure communication space for personal users and professional teams. Conflux aims to be a high-performance, minimalist alternative to bloated modern platforms, focusing intensely on text clarity and permanent conversation logs.

# Target Audience
- **Communities**: Seeking stable, long-term discussion spaces.
- **Individual Users**: Who prefer a slim, distraction-free interface.
- **Dev Teams**: Needing precise history and reliable role management.

# Core Pillars
1. **Lightweight Architecture**: Keeping the system slim and fast across Web and Desktop.
2. **Textual Precision**: High focus on text formatting and accurate message representation.
3. **History Preservation**: Ensuring all dialogues are permanently recorded and easily retrievable.

# Key Features (In Progress)
- **Simplified User Onboarding**: Fast, username-based login flow for immediate access.
- **Server & Channel Management**: End-to-end flow for creating, joining, and managing servers and channels. New servers automatically include a default #general channel. Users can join existing communities using a Server ID. Includes reactive server-channel linkage, allowing users to switch contexts and create structured discussion spaces effortlessly.
- **Persistent Text Messaging**: Robust message delivery and history retrieval with character limits and automatic scrolling.
- **Real-time Messaging**: Low-latency, WebSocket-based delivery with robust synchronization and auto-reconnection logic.
- **Roles & Permissions**: Comprehensive, Discord-like identity management system. Supports custom roles with colors, bitmask-based permissions (messaging, channel, role, and server management), and a priority-based hierarchy for administrative order. Permission changes are broadcast in real-time, instantly updating the UI for affected users.
- **Developer Tools**: Built-in developer debug menus for quick access to internal system IDs (Servers, Channels, Users, Roles, Messages).
