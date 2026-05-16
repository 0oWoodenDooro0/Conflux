# Role Priority Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement role priority levels that allow admins to set a hierarchy and ensure the role list is sorted by priority (descending).

**Architecture:** 
- Server-side: Update Exposed repositories to sort roles by `priority_level` DESC.
- Client-side State: Update `RolesAndPermissionsState` to track unsaved priority changes and sort the local role list.
- Client-side UI: Add a numeric input field in the role settings to edit priority.

**Tech Stack:** Kotlin, Ktor, Exposed, Compose Multiplatform.

---

### Task 1: Server-side Repository Updates

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt`
- Test: `server/src/test/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepositoryTest.kt` (if exists, or verify via existing server tests)

- [ ] **Step 1: Update `getRoles` to sort by priority**

```kotlin
// server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt

override suspend fun getRoles(serverId: String): List<Role> = dbQuery {
    Roles.selectAll()
        .where { Roles.serverId eq serverId }
        .orderBy(Roles.priorityLevel, SortOrder.DESC) // Add this line
        .map(::resultRowToRole)
}
```

- [ ] **Step 2: Update `getRolesForMember` to sort by priority**

```kotlin
// server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt

override suspend fun getRolesForMember(serverId: String, userId: String): List<Role> = dbQuery {
    (Roles innerJoin MemberRoles)
        .selectAll().where { 
            (MemberRoles.serverId eq serverId) and 
            (MemberRoles.userId eq userId) and 
            (Roles.id eq MemberRoles.roleId) 
        }
        .orderBy(Roles.priorityLevel, SortOrder.DESC) // Add this line
        .map(::resultRowToRole)
}
```

- [ ] **Step 3: Verify server-side sorting**
Run existing server tests to ensure no regressions.
Run: `./gradlew :server:test`

- [ ] **Step 4: Commit**
```bash
git add server/src/main/kotlin/website/woodendoor/conflux/database/repositories/ExposedServerRepository.kt
git commit -m "feat(server): sort roles by priority level descending"
```

---

### Task 2: Client State Management Updates

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt`

- [ ] **Step 1: Add `pendingPriority` and update `hasChanges`**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt

class RolesAndPermissionsState(
    initialRoles: List<Role> = emptyList()
) {
    var roles by mutableStateOf(initialRoles.sortedByDescending { it.priorityLevel }) // Sort initial
    var selectedRole by mutableStateOf<Role?>(null)
        private set

    var pendingPermissions by mutableStateOf<Long?>(null)
        private set

    var pendingPriority by mutableStateOf<Int?>(null) // Add this
        private set

    var roleMembers by mutableStateOf<List<User>>(emptyList())
        private set

    val hasChanges: Boolean
        get() = (pendingPermissions != null && pendingPermissions != selectedRole?.permissions) ||
                (pendingPriority != null && pendingPriority != selectedRole?.priorityLevel) // Update this

    fun selectRole(role: Role?) {
        selectedRole = role
        pendingPermissions = role?.permissions
        pendingPriority = role?.priorityLevel // Add this
        roleMembers = emptyList()
    }
    
    // ... updateRoles should also sort
    fun updateRoles(newRoles: List<Role>) {
        roles = newRoles.sortedByDescending { it.priorityLevel } // Sort here
        selectedRole = roles.find { it.id == selectedRole?.id }
        if (pendingPermissions == null) {
            pendingPermissions = selectedRole?.permissions
        }
        if (pendingPriority == null) {
            pendingPriority = selectedRole?.priorityLevel
        }
    }

    fun updatePendingPriority(priority: Int) {
        pendingPriority = priority
    }

    fun revertChanges() {
        pendingPermissions = selectedRole?.permissions
        pendingPriority = selectedRole?.priorityLevel // Add this
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt
git commit -m "feat(ui): update RolesAndPermissionsState to track priority changes and sort roles"
```

---

### Task 3: Client UI Updates

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt` (to pass updated priority to API)

- [ ] **Step 1: Add Priority input to `RolesAndPermissionsTab`**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt

// Inside RolesAndPermissionsTab, update the Save button logic:
Button(onClick = { 
    val perms = state.pendingPermissions ?: selectedRole.permissions
    val priority = state.pendingPriority ?: selectedRole.priorityLevel
    onSaveChanges(selectedRole, perms, priority) // Add priority parameter
}) {
    Text("Save Changes")
}

// Add UI for Priority editing (e.g., above or inside PermissionList)
@Composable
fun RoleGeneralSettings(
    priority: Int,
    onPriorityChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = priority.toString(),
        onValueChange = { newValue ->
            newValue.toIntOrNull()?.let { onPriorityChange(it) }
        },
        label = { Text("Priority Level") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
```

- [ ] **Step 2: Update `onSaveChanges` signature in `RolesAndPermissionsTab` and its callers**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt

fun RolesAndPermissionsTab(
    state: RolesAndPermissionsState,
    allServerMembers: List<User>,
    onAddRole: () -> Unit,
    onSaveChanges: (Role, Long, Int) -> Unit, // Add Int priority
    // ...
)
```

- [ ] **Step 3: Update `ServerSettingsDialog.kt` to call API with new priority**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt

onSaveChanges = { role, permissions, priority ->
    scope.launch {
        apiClient.updateRole(
            serverId = server.id,
            userId = currentUser.id,
            roleId = role.id,
            permissions = permissions,
            priority = priority // Pass priority here
        )
        // Refresh roles after update
        val updatedRoles = apiClient.getRoles(server.id)
        rolesState.updateRoles(updatedRoles)
    }
}
```

- [ ] **Step 4: Commit**
```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt
git commit -m "feat(ui): add priority level input and update save logic"
```

---

### Task 4: Verification

- [ ] **Step 1: Run the application**
Run: `./gradlew :composeApp:run` (or equivalent for JVM/Web)

- [ ] **Step 2: Manual Test Case**
1. Open Server Settings > Roles.
2. Create two roles: "Role A" (Priority 10) and "Role B" (Priority 20).
3. Verify "Role B" is above "Role A" in the list.
4. Select "Role A", change priority to 30, and Save.
5. Verify "Role A" moves to the top of the list.
6. Close and reopen the dialog to ensure the order is persisted from the server.
