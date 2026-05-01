# Specification: Shared Domain Models

## Goal
Establish a robust, shared domain model layer in the `shared` module that can be used by both the Ktor server and the Compose Multiplatform clients (Web/JVM). The models will follow a Discord-like structure to support servers, channels, and real-time messaging.

## Scope
- Define core entities: `User`, `Role`, `Server`, `Channel`, and `Message`.
- Ensure all models are serializable using `kotlinx.serialization`.
- Place models in `shared/src/commonMain/kotlin/website/woodendoor/conflux/models`.

## Data Models

### User
- `id`: String (UUID)
- `username`: String
- `discriminator`: String (e.g., "0001")
- `avatar`: String? (URL or resource path)

### Role
- `id`: String (UUID)
- `name`: String
- `permissions`: Long (Bitmask for Discord-style permissions)
- `color`: Int? (HEX color code)

### Server
- `id`: String (UUID)
- `name`: String
- `ownerId`: String
- `icon`: String?
- `memberIds`: List<String>
- `roleIds`: List<String>

### Channel
- `id`: String (UUID)
- `serverId`: String
- `name`: String
- `type`: ChannelType (Enum: TEXT, VOICE, etc.)
- `topic`: String?

### Message
- `id`: String (UUID)
- `channelId`: String
- `authorId`: String
- `content`: String
- `timestamp`: Long (Epoch milliseconds)
- `attachments`: List<String>

## Constraints
- Models must be immutable (`data class` with `val`).
- Must support Kotlin Multiplatform (Common code).
