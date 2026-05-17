# Channel Overrides Controller & Routes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement controller methods and routes for managing channel permission overrides and update permission enforcement to use channel-specific effective permissions.

**Architecture:** Add override management to `ChannelController`, update `ServerRoutes.kt` to use hierarchical permissions via `ChannelRepository.getEffectivePermissions`, and add new endpoints for overrides.

**Tech Stack:** Ktor, Kotlin, Exposed (via Repositories).

---

### Task 1: Update ChannelController with Override Management

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt`

- [ ] **Step 1: Add getOverrides method**

```kotlin
    suspend fun getOverrides(channelId: String): OperationResult<List<ChannelPermissionOverride>> {
        if (channelRepository.getChannel(channelId) == null) {
            return OperationResult.Failure.NotFound("Channel not found")
        }
        val overrides = channelRepository.getOverrides(channelId)
        return OperationResult.Success(overrides)
    }
```

- [ ] **Step 2: Add upsertOverride method**

```kotlin
    suspend fun upsertOverride(channelId: String, request: UpsertOverrideRequest): OperationResult<Unit> {
        if (channelRepository.getChannel(channelId) == null) {
            return OperationResult.Failure.NotFound("Channel not found")
        }
        
        val override = ChannelPermissionOverride(
            id = UUID.randomUUID().toString(), // Repository should handle ID if it's an update, but we pass one anyway
            channelId = channelId,
            targetId = request.targetId,
            targetType = request.targetType,
            allow = request.allow,
            deny = request.deny
        )
        
        val success = channelRepository.upsertOverride(channelId, override)
        return if (success) {
            // TODO: Broadcast update if needed
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.InternalError("Failed to upsert override")
        }
    }
```

- [ ] **Step 3: Add deleteOverride method**

```kotlin
    suspend fun deleteOverride(overrideId: String): OperationResult<Unit> {
        val success = channelRepository.deleteOverride(overrideId)
        return if (success) {
            OperationResult.Success(Unit)
        } else {
            OperationResult.Failure.NotFound("Override not found or failed to delete")
        }
    }
```

- [ ] **Step 4: Add hasPermission helper**

```kotlin
    suspend fun hasPermission(serverId: String, channelId: String, userId: String, permission: Long): Boolean {
        val effectivePermissions = channelRepository.getEffectivePermissions(serverId, channelId, userId)
        return ConfluxPermission.hasPermission(effectivePermissions, permission)
    }
```

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/controller/ChannelController.kt
git commit -m "feat: add override management to ChannelController"
```

### Task 2: Update ServerRoutes.kt with Hierarchical Permissions and New Routes

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt`

- [ ] **Step 1: Update patch("/{channelId}") to use hierarchical permissions**

```kotlin
        patch("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@patch call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }
            // ... rest of the method
```

- [ ] **Step 2: Update delete("/{channelId}") to use hierarchical permissions**

```kotlin
        delete("/{channelId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            
            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }
            // ... rest of the method
```

- [ ] **Step 3: Add GET /overrides route**

```kotlin
        get("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@get call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            val result = channelController.getOverrides(channelId)
            call.respond(result)
        }
```

- [ ] **Step 4: Add POST /overrides route**

```kotlin
        post("/{channelId}/overrides") {
            val serverId = call.parameters["serverId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing channelId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@post call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            try {
                val request = call.receive<UpsertOverrideRequest>()
                val result = channelController.upsertOverride(channelId, request)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
```

- [ ] **Step 5: Add DELETE /overrides/{overrideId} route**

```kotlin
        delete("/{channelId}/overrides/{overrideId}") {
            val serverId = call.parameters["serverId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing serverId")
            val userId = call.request.queryParameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")
            val channelId = call.parameters["channelId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing channelId")
            val overrideId = call.parameters["overrideId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing overrideId")

            if (!channelController.hasPermission(serverId, channelId, userId, ConfluxPermission.CHANNEL_MANAGEMENT)) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
            }

            val result = channelController.deleteOverride(overrideId)
            call.respond(result)
        }
```

- [ ] **Step 6: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt
git commit -m "feat: add channel override routes and update permission enforcement"
```

### Task 3: Verification

- [ ] **Step 1: Verify compilation**

Run: `./gradlew :server:build`
Expected: Success

- [ ] **Step 2: Commit any fixes**
