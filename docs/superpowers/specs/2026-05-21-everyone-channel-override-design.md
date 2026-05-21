# Spec: Immutable @everyone Channel Permission Override

Ensure that every new channel automatically has a default `@everyone` permission override created, which cannot be removed, and defaults to all permissions being set to "Inherit" (`allow = 0L, deny = 0L`).

## Proposed Changes

### Backend

#### 1. Channel Creation Override Setup
Modify `ChannelController.createChannel` to automatically insert a default permission override for the server's `@everyone` role when a channel is created.
- Query all server roles via `serverRepository.getRoles(serverId)`.
- Find the `@everyone` role (where `priorityLevel == -1`).
- Call `channelRepository.upsertOverride` to insert a default `ChannelPermissionOverride` with:
  - `targetId = everyoneRole.id`
  - `targetType = OverrideType.ROLE`
  - `allow = 0`
  - `deny = 0`

#### 2. Protect `@everyone` Override from Deletion
- In `ExposedChannelRepository.deleteOverride`, reject deleting the override if its `targetId` matches the server's `@everyone` role ID.
- In `ChannelController.deleteOverride`, if the target is determined to be the `@everyone` override, return an `OperationResult.Failure.BadRequest("Cannot delete the @everyone override")`.

---

### Frontend

#### 1. Channel Settings Dialog
In [ChannelSettingsDialog.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt):
- In `ChannelPermissionsTab`, search the list of `roles` to identify the `@everyone` role:
  `val everyoneRole = roles.find { it.priorityLevel == website.woodendoor.conflux.DEFAULT_ROLE_PRIORITY_EVERYONE }` (or `it.priorityLevel == -1`).
- Conditionally render the delete override button only if the currently selected override is NOT the `@everyone` override:
  `val isEveryoneOverride = selectedOverride?.targetId == everyoneRole?.id && selectedOverride?.targetType == OverrideType.ROLE`
- Do not display the delete button when `isEveryoneOverride` is true.

---

## Verification Plan

### Automated Tests
- Update `ChannelControllerTest` to verify that `createChannel` inserts the `@everyone` override.
- Update `ChannelControllerTest` or create new tests to verify `deleteOverride` returns a `BadRequest` if the target is the `@everyone` override.
