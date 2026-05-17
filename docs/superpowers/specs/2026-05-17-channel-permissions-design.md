# Design Spec: Channel Permission Overrides

## Status
- **Date**: 2026-05-17
- **Status**: Draft
- **Authors**: Gemini CLI

## 1. Overview
The goal is to implement channel-specific permission overrides similar to Discord. These overrides allow server administrators to fine-tune permissions (like `MESSAGING` and `CHANNEL_MANAGEMENT`) for specific roles or users within a particular channel. 

Key constraints:
- Server owners are exempt from all restrictions and always have `ALL` permissions.
- Channel overrides follow a strict hierarchy: `@everyone` -> Roles -> Specific Users.
- An "Allow" in a channel can grant a permission that is denied at the server level (Expandable Discord Style).

## 2. Data Model

### 2.1 Shared Model
```kotlin
enum class OverrideType {
    ROLE, USER
}

data class ChannelPermissionOverride(
    val id: String,
    val channelId: String,
    val targetId: String, // Role ID or User ID
    val targetType: OverrideType,
    val allow: Long, // Bitmask of allowed permissions
    val deny: Long   // Bitmask of denied permissions
)
```

### 2.2 Database Schema (Exposed)
```kotlin
object ChannelPermissionOverrides : Table("channel_permission_overrides") {
    val id = varchar("id", 36)
    val channelId = varchar("channel_id", 36).references(Channels.id)
    val targetId = varchar("target_id", 36) // Can be Role.id or User.id
    val targetType = enumerationByName("target_type", 10, OverrideType::class)
    val allow = long("allow")
    val deny = long("deny")

    override val primaryKey = PrimaryKey(id)
}
```

## 3. Permission Calculation Logic

For a given user in a specific channel:

1. **Owner Exemption**: If `userId == server.ownerId`, return `ConfluxPermission.ALL`.
2. **Server Base**: Start with `ServerPerms` (union of all user's server roles).
3. **Channel Overrides**:
   - Fetch all overrides for the channel.
   - **@everyone**: Apply the `@everyone` role's override if it exists.
     - `Effective = (ServerBase & ~everyoneDeny) | everyoneAllow`
   - **Roles**: For each role the user has (excluding `@everyone`):
     - Calculate `rolesAllow = Union of all role allows`
     - Calculate `rolesDeny = Union of all role denies`
     - `Effective = (Effective & ~rolesDeny) | rolesAllow`
   - **User**: Apply the specific user's override if it exists.
     - `Effective = (Effective & ~userDeny) | userAllow`
4. **Final Result**: Return `Effective`.

*Note: Currently, only `MESSAGING` and `CHANNEL_MANAGEMENT` bits are expected to be overriden at the channel level.*

## 4. Backend Changes

### 4.1 Repositories
- **ChannelRepository**:
  - `getOverrides(channelId: String): List<ChannelPermissionOverride>`
  - `upsertOverride(override: ChannelPermissionOverride): Boolean`
  - `deleteOverride(id: String): Boolean`
  - `getEffectivePermissions(serverId: String, channelId: String, userId: String): Long`

### 4.2 Controllers & Routes
- **ChannelController**:
  - `GET /api/channels/{channelId}/overrides`
  - `POST /api/channels/{channelId}/overrides` (Upsert)
  - `DELETE /api/channels/{channelId}/overrides/{id}`
- **Permission Checks**:
  - Update `ChatController` (sending messages) and `ChannelController` (editing channels) to use `getEffectivePermissions` instead of server-level permissions.

## 5. UI Changes

### 5.1 Channel Settings
- New tab: **Permissions**.
- **Override List**: Displays roles/users with existing overrides.
- **Add Override**: Button to select a Role or User.
- **Permission Editor**:
  - When an override is selected, show a list of overridable permissions (`MESSAGING`, `CHANNEL_MANAGEMENT`).
  - Each permission has three states:
    - **Inherit (Default)**: Bit 0 in both `allow` and `deny`.
    - **Allow**: Bit 1 in `allow`, 0 in `deny`.
    - **Deny**: Bit 0 in `allow`, 1 in `deny`.

## 6. Testing Strategy
- **Unit Tests**: Test the hierarchical calculation logic with various combinations of server/channel permissions.
- **Integration Tests**: Verify CRUD operations for overrides and ensure permission checks correctly restrict/grant access in API calls.
- **Manual UI Tests**: Verify that the permission editor correctly updates the backend and that the UI responds immediately to permission changes.
