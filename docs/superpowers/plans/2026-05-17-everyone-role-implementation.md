# @everyone Role System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace legacy "Owner" and "Member" roles with a permanent, immutable `@everyone` role (priority -1) that applies to all server members.

**Architecture:** 
- Modify `ExposedServerRepository` to manage the lifecycle of the `@everyone` role.
- Update permission and role query logic to implicitly include `@everyone`.
- Add validation in `ServerRoutes` to protect `@everyone` from modification/deletion.

**Tech Stack:** Kotlin, Ktor, Exposed, JUnit.

---

### Task 1: Update Constants

**Files:**
- Modify: `shared/src/commonMain/kotlin/website/woodendoor/conflux/Constants.kt`

- [ ] **Step 1: Update constants**

```kotlin
package website.woodendoor.conflux

const val SERVER_PORT = 8080
const val DEFAULT_BASE_URL = "http://localhost:$SERVER_PORT"

const val DEFAULT_ROLE_NAME_EVERYONE = "@everyone"
const val DEFAULT_ROLE_PRIORITY_EVERYONE = -1
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/website/woodendoor/conflux/Constants.kt
git commit -m "refactor: add @everyone role constants and remove legacy role names"
```

---

### Task 2: Update Server Creation Logic

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt`
- Test: `server/src/test/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepositoryTest.kt`

- [ ] **Step 1: Write failing test for server creation**
Update `ExposedServerRepositoryTest.kt` to expect only `@everyone` role.

```kotlin
@Test
fun `createServer should only create @everyone role`() = runBlocking {
    val server = Server("test-id", "Test Server", "owner-id")
    repository.createServer(server)
    
    val roles = repository.getRoles("test-id")
    assertEquals(1, roles.size)
    assertEquals(DEFAULT_ROLE_NAME_EVERYONE, roles[0].name)
    assertEquals(DEFAULT_ROLE_PRIORITY_EVERYONE, roles[0].priorityLevel)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :server:test --tests "website.woodendoor.conflux.database.repositories.ExposedServerRepositoryTest"`
Expected: FAIL

- [ ] **Step 3: Update createServer implementation**

```kotlin
// In ExposedServerRepository.kt
override suspend fun createServer(server: Server): Server? = dbQuery {
    val insertStatement = Servers.insert {
        it[id] = server.id
        it[name] = server.name
        it[ownerId] = server.ownerId
    }
    val createdServer = insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToServer)
    
    if (createdServer != null) {
        ServerMembers.insert {
            it[this.serverId] = createdServer.id
            it[this.userId] = createdServer.ownerId
        }

        // Create @everyone role
        Roles.insert {
            it[id] = UUID.randomUUID().toString()
            it[this.serverId] = createdServer.id
            it[name] = DEFAULT_ROLE_NAME_EVERYONE
            it[permissions] = ConfluxPermission.MESSAGING
            it[color] = null
            it[priorityLevel] = DEFAULT_ROLE_PRIORITY_EVERYONE
        }
    }
    createdServer
}
```

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt server/src/test/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepositoryTest.kt
git commit -m "feat: update server creation to only create @everyone role"
```

---

### Task 3: Update Member Joining Logic

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt`
- Test: `server/src/test/kotlin/website/woodendoor/conflux/database/repositories/JoinServerTest.kt`

- [ ] **Step 1: Update JoinServerTest to expect no default role assignment**

```kotlin
@Test
fun `joinServer should not assign default role`() = runBlocking {
    val serverId = "test-server"
    val userId = "test-user"
    // ... setup server ...
    repository.joinServer(userId, serverId)
    
    val roles = repository.getRolesForMember(serverId, userId)
    // We expect only @everyone if getRolesForMember is updated, 
    // but for now check database state or current behavior.
    // If getRolesForMember isn't updated yet, this might be empty.
    assertTrue(roles.isEmpty() || roles.all { it.priorityLevel == -1 })
}
```

- [ ] **Step 2: Update joinServer implementation**
Remove the `memberRole` lookup and `MemberRoles.insert` block.

- [ ] **Step 3: Run tests**

- [ ] **Step 4: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt
git commit -m "feat: remove legacy role assignment on joining server"
```

---

### Task 4: Update Permission Calculation Logic

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt`

- [ ] **Step 1: Update getPermissionsForMember to include @everyone role**

```kotlin
override suspend fun getPermissionsForMember(serverId: String, userId: String): Long = dbQuery {
    // ... owner check ...

    val everyonePermissions = Roles.selectAll()
        .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq DEFAULT_ROLE_PRIORITY_EVERYONE) }
        .map { it[Roles.permissions] }
        .singleOrNull() ?: 0L

    val rolePermissions = (Roles innerJoin MemberRoles)
        .select(Roles.permissions)
        .where { 
            (MemberRoles.serverId eq serverId) and 
            (MemberRoles.userId eq resolvedUserId) and 
            (Roles.id eq MemberRoles.roleId) 
        }
        .map { it[Roles.permissions] }
        .fold(0L) { acc, p -> acc or p }

    rolePermissions or everyonePermissions
}
```

- [ ] **Step 2: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt
git commit -m "feat: include @everyone permissions in permission calculation"
```

---

### Task 5: Update Role Query for Member

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt`

- [ ] **Step 1: Update getRolesForMember to include @everyone**

```kotlin
override suspend fun getRolesForMember(serverId: String, userId: String): List<Role> = dbQuery {
    val everyoneRole = Roles.selectAll()
        .where { (Roles.serverId eq serverId) and (Roles.priorityLevel eq DEFAULT_ROLE_PRIORITY_EVERYONE) }
        .map(::resultRowToRole)
        .singleOrNull()

    val assignedRoles = (Roles innerJoin MemberRoles)
        .selectAll().where { 
            (MemberRoles.serverId eq serverId) and 
            (MemberRoles.userId eq userId) and 
            (Roles.id eq MemberRoles.roleId) 
        }
        .orderBy(Roles.priorityLevel, SortOrder.DESC)
        .map(::resultRowToRole)

    if (everyoneRole != null) {
        (assignedRoles + everyoneRole).distinctBy { it.id }.sortedByDescending { it.priorityLevel }
    } else {
        assignedRoles
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt
git commit -m "feat: include @everyone in member role list"
```

---

### Task 6: Implement Validation and Protection

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt`
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt` (to protect delete)

- [ ] **Step 1: Add validation in Role Update Route**
In `patch("/{roleId}")`, reject changes to `name` or `priorityLevel` if it's the `@everyone` role.

```kotlin
// In ServerRoutes.kt
if (existingRole.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE) {
    if (request.name != null && request.name != existingRole.name) {
        return@patch call.respond(HttpStatusCode.BadRequest, "Cannot rename @everyone role")
    }
    if (request.priorityLevel != null && request.priorityLevel != existingRole.priorityLevel) {
        return@patch call.respond(HttpStatusCode.BadRequest, "Cannot change @everyone role priority")
    }
}
```

- [ ] **Step 2: Add validation in Role Create Route**
Ensure `priorityLevel >= 0`.

```kotlin
if (request.priorityLevel < 0) {
    return@post call.respond(HttpStatusCode.BadRequest, "Priority level must be >= 0")
}
```

- [ ] **Step 3: Add protection in deleteRole implementation**

```kotlin
// In ExposedServerRepository.kt
override suspend fun deleteRole(roleId: String): Boolean = dbQuery {
    val role = Roles.selectAll().where { Roles.id eq roleId }.singleOrNull()
    if (role != null && role[Roles.priorityLevel] == DEFAULT_ROLE_PRIORITY_EVERYONE) {
        return@dbQuery false
    }
    Roles.deleteWhere { Roles.id eq roleId } > 0
}
```

- [ ] **Step 4: Run all tests to ensure no regressions**

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt
git commit -m "feat: add validation and protection for @everyone role"
```
