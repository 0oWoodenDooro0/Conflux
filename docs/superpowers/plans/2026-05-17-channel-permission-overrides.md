# Channel Permission Overrides Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Discord-style channel permission overrides for Roles and Users.

**Architecture:** 
- Add `ChannelPermissionOverrides` table to store bitmasks for Allow/Deny.
- Implement a hierarchical permission calculation logic: Server Base -> @everyone -> Roles -> User.
- Update API endpoints and UI to manage these overrides.

**Tech Stack:** Kotlin, Ktor, Exposed, Compose Multiplatform.

---

### Task 1: Shared Models

**Files:**
- Create: `shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ChannelPermissionOverride.kt`

- [ ] **Step 1: Define OverrideType and ChannelPermissionOverride**

```kotlin
package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class OverrideType {
    ROLE, USER
}

@Serializable
data class ChannelPermissionOverride(
    val id: String,
    val channelId: String,
    val targetId: String, // Role ID or User ID
    val targetType: OverrideType,
    val allow: Long,
    val deny: Long
)

@Serializable
data class UpsertOverrideRequest(
    val targetId: String,
    val targetType: OverrideType,
    val allow: Long,
    val deny: Long
)
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ChannelPermissionOverride.kt
git commit -m "feat: add ChannelPermissionOverride shared models"
```

---

### Task 2: Database Table

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/models/StructuralModels.kt`

- [ ] **Step 1: Add ChannelPermissionOverrides table**

```kotlin
// Add to server/src/main/kotlin/website/woodendoor/conflux/database/models/StructuralModels.kt

object ChannelPermissionOverrides : Table("channel_permission_overrides") {
    val id = varchar("id", 36)
    val channelId = varchar("channel_id", 36).references(Channels.id)
    val targetId = varchar("target_id", 36)
    val targetType = enumerationByName("target_type", 10, website.woodendoor.conflux.models.OverrideType::class)
    val allow = long("allow")
    val deny = long("deny")

    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 2: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/models/StructuralModels.kt
git commit -m "feat: add ChannelPermissionOverrides database table"
```

---

### Task 3: Repository Methods

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ChannelRepository.kt`
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedChannelRepository.kt`

- [ ] **Step 1: Update ChannelRepository interface**

```kotlin
// In ChannelRepository.kt
suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride>
suspend fun upsertOverride(channelId: String, override: ChannelPermissionOverride): Boolean
suspend fun deleteOverride(overrideId: String): Boolean
suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long
```

- [ ] **Step 2: Implement stubs in ExposedChannelRepository**

- [ ] **Step 3: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ChannelRepository.kt server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedChannelRepository.kt
git commit -m "feat: add override methods to ChannelRepository"
```

---

### Task 4: Implement Permission Logic

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedChannelRepository.kt`

- [ ] **Step 1: Implement getEffectivePermissions logic**

```kotlin
// In ExposedChannelRepository.kt
// Use the logic from the design spec:
// 1. Owner Exemption
// 2. Server Base
// 3. @everyone override
// 4. Roles overrides union
// 5. User override
```

- [ ] **Step 2: Implement CRUD for overrides**

- [ ] **Step 3: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedChannelRepository.kt
git commit -m "feat: implement channel permission calculation and CRUD"
```

---

### Task 5: Controller & Routes

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt`
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt`

- [ ] **Step 1: Add override management to ChannelController**

- [ ] **Step 2: Update ChannelController to use getEffectivePermissions for edits/deletes**

- [ ] **Step 3: Add routes in ServerRoutes.kt**

```kotlin
// GET /api/servers/{serverId}/channels/{channelId}/overrides
// POST /api/servers/{serverId}/channels/{channelId}/overrides
// DELETE /api/servers/{serverId}/channels/{channelId}/overrides/{overrideId}
```

- [ ] **Step 4: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt
git commit -m "feat: add channel override controller methods and routes"
```

---

### Task 6: Chat Permission Integration

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/controller/ChatController.kt`

- [ ] **Step 1: Update sendMessage to check effective permissions**

```kotlin
// In ChatController.kt
val permissions = channelRepository.getEffectivePermissions(serverId, channelId, userId)
if (!ConfluxPermission.hasPermission(permissions, ConfluxPermission.MESSAGING)) {
    return OperationResult.Failure.Forbidden("No messaging permission in this channel")
}
```

- [ ] **Step 2: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/controller/ChatController.kt
git commit -m "feat: enforce channel permissions in ChatController"
```

---

### Task 7: UI State & API Client

**Files:**
- Modify: `shared/src/commonMain/kotlin/website/woodendoor/conflux/api/ConfluxApiClient.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt`

- [ ] **Step 1: Add override endpoints to ConfluxApiClient**

- [ ] **Step 2: Update MainState to fetch and store channel permissions**

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/website/woodendoor/conflux/api/ConfluxApiClient.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt
git commit -m "feat: update UI state and API client for channel permissions"
```

---

### Task 8: UI Implementation

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt`

- [ ] **Step 1: Add Permissions tab to ChannelSettingsDialog**

- [ ] **Step 2: Implement Override List and Editor (3-state toggles)**

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt
git commit -m "feat: add channel permissions UI"
```
