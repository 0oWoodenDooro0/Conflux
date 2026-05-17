# Design: Channel Overrides Controller & Routes

## 1. Controller Methods in `ChannelController`

### Channel Management (Updated)
- `editChannel(channelId: String, userId: String, request: UpdateChannelRequest): OperationResult<Channel>`
  - Checks `CHANNEL_MANAGEMENT` permission for the specific channel using `channelRepository.getEffectivePermissions`.
- `deleteChannel(channelId: String, userId: String): OperationResult<Unit>`
  - Checks `CHANNEL_MANAGEMENT` permission for the specific channel using `channelRepository.getEffectivePermissions`.

### Override Management (New)
- `getOverrides(channelId: String, userId: String): OperationResult<List<ChannelPermissionOverride>>`
  - Checks `CHANNEL_MANAGEMENT` permission for the specific channel.
- `upsertOverride(channelId: String, userId: String, request: UpsertOverrideRequest): OperationResult<Unit>`
  - Checks `CHANNEL_MANAGEMENT` permission for the specific channel.
  - Converts `UpsertOverrideRequest` to `ChannelPermissionOverride`.
- `deleteOverride(channelId: String, overrideId: String, userId: String): OperationResult<Unit>`
  - Checks `CHANNEL_MANAGEMENT` permission for the specific channel.
  - Note: `channelId` is passed to simplify permission checking.

## 2. Permission Enforcement Updates

- The controller will now handle permission enforcement for channel-specific operations using the hierarchical logic (Server Roles + Channel Overrides).

## 3. New Routes in `ServerRoutes.kt`

In the `channelRoutes` block:
- `GET /api/servers/{serverId}/channels/{channelId}/overrides`
- `POST /api/servers/{serverId}/channels/{channelId}/overrides`
- `DELETE /api/servers/{serverId}/channels/{channelId}/overrides/{overrideId}`

The routes will:
- Extract `serverId`, `channelId`, `userId`.
- Call the corresponding `ChannelController` method.
- Pass `userId` to the controller.

## 4. Models
Using existing:
- `ChannelPermissionOverride`
- `UpsertOverrideRequest`
- `UpdateChannelRequest`

## Testing Strategy
- Add unit tests for `ChannelController` covering permission checks and override management.
- Verify routes with integration tests (or manual verification if needed, but TDD is preferred).
