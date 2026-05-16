# Client State Management Updates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Update `RolesAndPermissionsState` to support tracking and editing role priority levels, and ensure roles are always sorted by priority.

**Architecture:** Extend the existing `RolesAndPermissionsState` class with a `pendingPriority` state and update its methods to handle priority changes and sorting.

**Tech Stack:** Kotlin, Compose Runtime (mutableStateOf), Kotlin Test.

---

### Task 1: Add Sorting and Priority Tracking Tests

**Files:**
- Modify: `composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsStateTest.kt`

- [ ] **Step 1: Add failing tests for sorting and priority tracking**

```kotlin
    @Test
    fun testRolesSorting() {
        val role1 = Role("r1", "s1", "User", 0L, priorityLevel = 0)
        val role2 = Role("r2", "s1", "Admin", 1L, priorityLevel = 10)
        val state = RolesAndPermissionsState(listOf(role1, role2))
        
        assertEquals(role2, state.roles[0])
        assertEquals(role1, state.roles[1])
    }

    @Test
    fun testPendingPriority() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 5)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertEquals(5, state.pendingPriority)
        state.updatePendingPriority(10)
        assertEquals(10, state.pendingPriority)
        assertTrue(state.hasChanges)
        
        state.revertChanges()
        assertEquals(5, state.pendingPriority)
        assertEquals(5, state.selectedRole?.priorityLevel)
    }

    @Test
    fun testHasChangesWithPriority() {
        val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 5)
        val state = RolesAndPermissionsState(listOf(role))
        state.selectRole(role)
        
        assertTrue(!state.hasChanges)
        state.updatePendingPriority(6)
        assertTrue(state.hasChanges)
        
        state.updatePendingPriority(5) // Back to original
        assertTrue(!state.hasChanges)
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :composeApp:test`
Expected: FAIL (compilation errors for `pendingPriority` and `updatePendingPriority`, and `testRolesSorting` failure if it compiles)

- [ ] **Step 3: Commit tests**

```bash
git add composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsStateTest.kt
git commit -m "test(ui): add tests for role priority and sorting"
```

---

### Task 2: Implement Priority Tracking and Sorting

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt`

- [ ] **Step 1: Implement `pendingPriority` and sorting**

```kotlin
class RolesAndPermissionsState(
    initialRoles: List<Role> = emptyList()
) {
    var roles by mutableStateOf(initialRoles.sortedByDescending { it.priorityLevel })
    var selectedRole by mutableStateOf<Role?>(null)
        private set

    var pendingPermissions by mutableStateOf<Long?>(null)
        private set

    var pendingPriority by mutableStateOf<Int?>(null)
        private set

    var roleMembers by mutableStateOf<List<User>>(emptyList())
        private set

    val hasChanges: Boolean
        get() = (pendingPermissions != null && pendingPermissions != selectedRole?.permissions) ||
                (pendingPriority != null && pendingPriority != selectedRole?.priorityLevel)

    fun selectRole(role: Role?) {
        selectedRole = role
        pendingPermissions = role?.permissions
        pendingPriority = role?.priorityLevel
        roleMembers = emptyList()
    }
    
    fun updateRoleMembers(members: List<User>) {
        roleMembers = members
    }

    fun updateRoles(newRoles: List<Role>) {
        roles = newRoles.sortedByDescending { it.priorityLevel }
        // Maintain selection if possible
        selectedRole = roles.find { it.id == selectedRole?.id }
        if (pendingPermissions == null) {
            pendingPermissions = selectedRole?.permissions
        }
        if (pendingPriority == null) {
            pendingPriority = selectedRole?.priorityLevel
        }
    }

    fun updatePendingPermission(permission: Long, enabled: Boolean) {
        val current = pendingPermissions ?: selectedRole?.permissions ?: return
        pendingPermissions = ConfluxPermission.setPermission(current, permission, enabled)
    }

    fun updatePendingPriority(priority: Int) {
        pendingPriority = priority
    }

    fun revertChanges() {
        pendingPermissions = selectedRole?.permissions
        pendingPriority = selectedRole?.priorityLevel
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `./gradlew :composeApp:test`
Expected: PASS

- [ ] **Step 3: Commit implementation**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt
git commit -m "feat(ui): update RolesAndPermissionsState to track priority changes and sort roles"
```
