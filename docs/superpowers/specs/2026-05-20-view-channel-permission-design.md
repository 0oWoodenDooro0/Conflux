# View Channel Permission Design

This specification defines the implementation plan for the "View Channel" (`VIEW_CHANNEL`) permission within the Conflux platform. The goal is to ensure that a user who does not have the `VIEW_CHANNEL` permission for a specific channel cannot see it in the left channel list (sidebar), subscribe to its WebSocket messages, or receive any metadata updates related to it.

## Architectural Changes

### 1. Permission Definition (`shared` module)
We will introduce `VIEW_CHANNEL` in `ConfluxPermission` as the 5th bit flags (`1L shl 4`).
- `ConfluxPermission.VIEW_CHANNEL = 1L shl 4` (16)
- Modify `ConfluxPermission.ALL` to include `VIEW_CHANNEL`.

### 2. Default Server Configurations (`server` module)
When a server is created, the `@everyone` role must have both `MESSAGING` and `VIEW_CHANNEL` enabled by default:
- `permissions = ConfluxPermission.MESSAGING or ConfluxPermission.VIEW_CHANNEL`

### 3. API Filtering (`server` module)
- `ChannelController.getChannelsByServer` will accept a nullable `userId` parameter. If `userId` is provided, it filters the channel list to only include channels for which the user has `VIEW_CHANNEL` permission.
- The route `/api/servers/{serverId}/channels` will fetch the query parameter `userId` and pass it to the controller.

### 4. WebSocket Subscription & Broadcast Security (`server` module)
- **Subscription Protection**: The websocket route in `WebSocketRoutes.kt` will resolve `channelRepository` and check if the user has `VIEW_CHANNEL` permission before allowing subscription to `subscribe:channelId`.
- **Broadcast Filtering**:
  - `WebSocketConnectionManager` will expose helper methods to get server subscribers (`getServerSubscribers`) and user sessions (`getUserSessions`).
  - `ChannelController` will filter the target websocket connections when broadcasting `ChannelCreated` and `ChannelUpdated` events so that they are only dispatched to users who have `VIEW_CHANNEL` permission for that channel.

### 5. Client Integration (`shared` & `composeApp` modules)
- Update `ServerApiClient.getChannels` to accept a nullable `userId` and send it as a query parameter.
- Update `MainState.selectServer` to pass the `currentUserId` when fetching channels.
- Update `MainState.handleWebSocketEvent` for `ConfluxEvent.PermissionUpdate` to re-fetch the channel list using `apiClient.getChannels(serverId, currentUserId)` to ensure UI immediately updates when permissions/overrides change.

---

## Proposed Changes

### [shared]
#### [MODIFY] [ConfluxPermission.kt](file:///home/user/IdeaProjects/Conflux/shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ConfluxPermission.kt)
- Add `const val VIEW_CHANNEL = 1L shl 4`
- Update `ALL` to include `VIEW_CHANNEL`

#### [MODIFY] [ServerApiClient.kt](file:///home/user/IdeaProjects/Conflux/shared/src/commonMain/kotlin/website/woodendoor/conflux/api/ServerApiClient.kt)
- Change signature of `getChannels(serverId: String)` to `getChannels(serverId: String, userId: String? = null)`
- Add `parameter("userId", userId)` to the HTTP GET builder if `userId` is not null.

### [server]
#### [MODIFY] [ExposedServerRepository.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt)
- Modify `@everyone` role creation to set permissions to `ConfluxPermission.MESSAGING or ConfluxPermission.VIEW_CHANNEL`.

#### [MODIFY] [ChannelController.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt)
- Update `getChannelsByServer(serverId: String)` to `getChannelsByServer(serverId: String, userId: String? = null)`.
- Implement `VIEW_CHANNEL` filtering logic inside `getChannelsByServer`.
- Update `broadcastChannelCreated` and `broadcastChannelUpdated` to filter WebSocket broadcasts by checking user permissions.

#### [MODIFY] [ServerRoutes.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt)
- Update channel GET route to extract `userId` query parameter and pass it to `channelController.getChannelsByServer`.

#### [MODIFY] [WebSocketConnectionManager.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/WebSocketConnectionManager.kt)
- Add `getServerSubscribers(serverId: String): Set<String>`
- Add `getUserSessions(userId: String): Set<DefaultWebSocketServerSession>`

#### [MODIFY] [WebSocketRoutes.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/routes/WebSocketRoutes.kt)
- Inject `channelRepository` into `webSocketRoutes`.
- In `subscribe:channelId` text frame handling, verify `VIEW_CHANNEL` permission before subscribing the user session.

#### [MODIFY] [Application.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/Application.kt)
- Pass `channelRepository` as a parameter to `webSocketRoutes(...)`.

### [composeApp]
#### [MODIFY] [MainState.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt)
- Pass `currentUserId` when calling `apiClient.getChannels(...)` in `selectServer(...)`.
- Update `ConfluxEvent.PermissionUpdate` handler to re-fetch channels using `apiClient.getChannels(...)`.

---

## Database Testing Issues (Pre-existing Fix)
We will fix the H2 `JdbcSQLSyntaxErrorException` (dropping `CHANNELS` fails due to `ChannelPermissionOverrides` dependency) in all database repository test suites by updating their `@BeforeTest setup` method to drop and create `ChannelPermissionOverrides` in correct dependency order:
- `Messages, ChannelPermissionOverrides, MemberRoles, ServerMembers, Channels, Roles, Servers, Users`

---

## Verification Plan

### Automated Tests
1. **Repository & Controller Tests**:
   - Verify `@everyone` has `VIEW_CHANNEL` by default.
   - Verify `getChannelsByServer` filters channels correctly.
   - Verify WebSocket route blocks subscription when `VIEW_CHANNEL` is absent.
2. Run `./gradlew test` to ensure all tests pass.

### Manual Verification
- Deploy and run the app locally, verify channels disappear/appear dynamically in the left sidebar as permission overrides are toggled.
