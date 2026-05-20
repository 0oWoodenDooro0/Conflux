# Channel Management Permissions Adjustment Design

This specification defines the implementation plan for the dynamic "Manage Channel" (`CHANNEL_MANAGEMENT`) permission adjustments within the Conflux platform. The goal is to ensure that a user who has either server-wide channel management permission or channel-specific permission overrides can display the frontend channel gear icon, enter the settings screen, modify the channel name, delete the channel, and configure that channel's permissions.

---

## Architectural Changes

### 1. Transient Permission Property (`shared` module)
We will introduce a new `canManage: Boolean = false` field to the shared `Channel` model.
- Defaults to `false` for backwards compatibility.
- Acts as a transient property computed dynamically by the server per-user.

### 2. Dynamically Setting `canManage` on the Server (`server` module)
In `ChannelController.kt`, we will calculate the `canManage` flag when:
- **Fetching channels** (`getChannelsByServer`):
  When a query `userId` is present, each channel in the returned list is mapped to set `canManage = hasPermission(serverId, channel.id, userId, ConfluxPermission.CHANNEL_MANAGEMENT)`.
- **Broadcasting channel creation/update events** (`broadcastChannelCreated` and `broadcastChannelUpdated`):
  Compute each subscriber-specific `canManage` value inside the Ktor WebSocket session broadcast loop, copying the channel model with the specific permission state before dispatching.

### 3. Frontend Integration (`composeApp` module)
- **`ChannelSidebar.kt` & `ChannelItem`**:
  - Update `ChannelSidebar` to pass `canManage = channel.canManage` to `ChannelItem` for all rendered channels.
  - Simplify `ChannelItem` to render the settings gear icon for any channel if `channel.canManage` is true, without restricting it to only the selected channel if desired (or keep it selected-only if preferred, but with override support).
- **`MainScreen.kt`**:
  - Clean up LaunchedEffects and reactive variables that track channel settings dialog state.
  - Rely directly on `selectedChannel?.canManage` to decide if settings can be opened or closed dynamically.

---

## Proposed Changes

### [shared]

#### [MODIFY] [Channel.kt](file:///home/user/IdeaProjects/Conflux/shared/src/commonMain/kotlin/website/woodendoor/conflux/models/Channel.kt)
- Add `val canManage: Boolean = false` to the `Channel` data class.

---

### [server]

#### [MODIFY] [ChannelController.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt)
- Update `getChannelsByServer` to return channels mapped with subscriber-specific `canManage`.
- Update `broadcastChannelCreated` and `broadcastChannelUpdated` to copy the channel with user-specific `canManage` permissions during the WebSocket broadcast loop.

---

### [composeApp]

#### [MODIFY] [ChannelSidebar.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSidebar.kt)
- Pass `canManage = channel.canManage` to `ChannelItem`.

#### [MODIFY] [MainScreen.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/MainScreen.kt)
- Update how `channelToEdit` and LaunchedEffect gates are handled based on `selectedChannel?.canManage`.

---

## Verification Plan

### Automated Tests
- Update or create tests inside `ChannelPermissionLogicTest.kt` and `MainStateTest.kt` to ensure `canManage` is correctly calculated and serialized.
- Run `./gradlew test` to ensure that all server and shared unit tests pass successfully.

### Manual Verification
- Deploy the Conflux server and composeApp.
- Assign channel-specific role/user overrides for `CHANNEL_MANAGEMENT` and verify the settings gear icon shows and disappears in real-time in the left sidebar.
