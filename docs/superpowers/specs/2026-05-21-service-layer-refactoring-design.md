# Design Spec: Service Layer Refactoring and Channel Permission Consolidation

Introduce a clean Service Layer between Controllers and Repositories in the Conflux backend. This refactoring decouples business logic from presentation (HTTP/Ktor/WebSockets) and data access layers. It also resolves duplicated `@everyone` permission override creation logic between channel creation and server creation.

---

## 1. Objectives

- **Introduce Service Layer**: Decouple controllers from repositories by adding high-level services that manage domain transactions, validation, and core business rules.
- **De-duplicate Channel Permissions Overrides**: Consolidate the `@everyone` role permission override logic into a dedicated permission service.
- **Ensure Separation of Concerns**: Keep controllers thin, dealing only with HTTP routing, Ktor calls, permission checks (authorization), and WebSocket broadcasting.

---

## 2. Proposed Architecture

We will introduce a new package `website.woodendoor.conflux.service` containing Service interfaces and their implementations.

### 2.1 Interface & Implementation Class Structure

```
website.woodendoor.conflux
├── service
│   ├── ChannelService.kt (Interface)
│   ├── ChannelPermissionService.kt (Interface)
│   ├── ServerService.kt (Interface)
│   ├── UserService.kt (Interface)
│   ├── RoleService.kt (Interface)
│   ├── ChatService.kt (Interface)
│   └── impl
│       ├── ChannelServiceImpl.kt
│       ├── ChannelPermissionServiceImpl.kt
│       ├── ServerServiceImpl.kt
│       ├── UserServiceImpl.kt
│       ├── RoleServiceImpl.kt
│       └── ChatServiceImpl.kt
```

### 2.2 Shared Repository Strategy
As designed, we will keep a single `ChannelRepository` for data persistence but split the business logic into two services:
- `ChannelService`: Handles Channel CRUD.
- `ChannelPermissionService`: Handles channel overrides, permissions checking, and `@everyone` initialization.

---

## 3. Detailed Service Definitions

### 3.1 `ChannelPermissionService` and `ChannelPermissionServiceImpl`
Responsible for all overrides and permission computation. It will encapsulate the creation of the default `@everyone` override.

```kotlin
package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.ChannelPermissionOverride
import website.woodendoor.conflux.models.UpsertOverrideRequest

interface ChannelPermissionService {
    suspend fun getOverrides(channelId: String): List<ChannelPermissionOverride>
    suspend fun upsertOverride(channelId: String, request: UpsertOverrideRequest): Boolean
    suspend fun deleteOverride(serverId: String, overrideId: String): Boolean
    suspend fun getEffectivePermissions(serverId: String, channelId: String, userId: String): Long
    suspend fun hasPermission(serverId: String, channelId: String, userId: String, permission: Long): Boolean
    suspend fun createEveryoneOverride(serverId: String, channelId: String): Boolean
}
```

**Implementation Highlights (`ChannelPermissionServiceImpl`)**:
- Uses `ChannelRepository` and `ServerRepository` to fetch channels/roles.
- `createEveryoneOverride`:
  1. Finds the `@everyone` role priority level (`DEFAULT_ROLE_PRIORITY_EVERYONE`) on the server.
  2. Creates and upserts a `ChannelPermissionOverride` with `allow = 0L` and `deny = 0L`.

### 3.2 `ChannelService` and `ChannelServiceImpl`
Handles basic channel operations and coordinates with `ChannelPermissionService`.

```kotlin
package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType

interface ChannelService {
    suspend fun createChannel(serverId: String, name: String, type: ChannelType, topic: String? = null): Channel?
    suspend fun getChannel(id: String): Channel?
    suspend fun getChannelsByServer(serverId: String): List<Channel>
    suspend fun updateChannel(channel: Channel): Boolean
    suspend fun deleteChannel(id: String): Boolean
}
```

**Implementation Highlights (`ChannelServiceImpl`)**:
- `createChannel`:
  1. Calls `channelRepository.createChannel(...)`.
  2. If successful, calls `channelPermissionService.createEveryoneOverride(serverId, createdChannel.id)`.
  3. Returns the created channel.

### 3.3 `ServerService` and `ServerServiceImpl`
Manages servers and default initialization.

```kotlin
package website.woodendoor.conflux.service

import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User

interface ServerService {
    suspend fun createServer(name: String, ownerId: String): Server?
    suspend fun getServer(id: String): Server?
    suspend fun getServersForUser(userId: String): List<Server>
    suspend fun getMembers(serverId: String): List<User>
    suspend fun getPermissionsForMember(serverId: String, userId: String): Long
    suspend fun joinServer(userId: String, serverId: String): Boolean
}
```

**Implementation Highlights (`ServerServiceImpl`)**:
- `createServer`:
  1. Discovers or creates the user (owner).
  2. Calls `serverRepository.createServer(...)`.
  3. Automatically calls `channelService.createChannel(serverId, "general", ChannelType.TEXT, "Welcome to $serverName!")` which transitively initializes the `@everyone` override.

### 3.4 `UserService`, `RoleService`, `ChatService`
These services mirror the remaining controllers' transactional operations:
- `UserService`: Register/get users.
- `RoleService`: CRUD server roles, priorities, and memberships.
- `ChatService`: Save messages and check basic constraints.

---

## 4. Controller Refactoring

All controllers will be updated to:
- Inject **Services** instead of **Repositories**.
- Handle request/response mappings.
- Perform authorization checks (using `ChannelPermissionService` or `RoleService`).
- Call the appropriate service method.
- Broadcast WebSocket updates on success.

---

## 5. Verification & Tests Plan

### 5.1 Automated Tests
Run Gradle test suites to verify that both API functionality and controller behavior remain correct:
- `./gradlew test` (specifically `ServerControllerTest`, `ChannelControllerTest`, `ChatControllerTest`, `RoleControllerTest`, `UserControllerTest`)

### 5.2 Manual Verification
We will verify that Ktor starts cleanly and databases are correctly initialized with no SQL errors or dependency injection circular issues.
