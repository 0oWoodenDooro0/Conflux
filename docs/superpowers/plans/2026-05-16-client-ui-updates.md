# Client UI Updates for Role Priority Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add priority level input to the Roles and Permissions UI and update the save logic to send the priority to the server.

**Architecture:** Update `RolesAndPermissionsTab` to handle priority input and update `ServerSettingsDialog` to pass this priority to the API.

**Tech Stack:** Compose Multiplatform (Kotlin), Ktor Client.

---

### Task 1: Add Priority Input to RolesAndPermissionsTab

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt`

- [ ] **Step 1: Update `onSaveChanges` signature in `RolesAndPermissionsTab`**

```kotlin
// RolesAndPermissionsTab.kt

// Update the parameter type of RolesAndPermissionsTab
fun RolesAndPermissionsTab(
    state: RolesAndPermissionsState,
    allServerMembers: List<User>,
    onAddRole: () -> Unit,
    onSaveChanges: (Role, Long, Int) -> Unit, // Changed from (Role, Long) -> Unit
    onAssignRole: (Role, User) -> Unit,
    onRemoveRole: (Role, User) -> Unit
)
```

- [ ] **Step 2: Add `RoleGeneralSettings` composable**

```kotlin
// RolesAndPermissionsTab.kt (Add near PermissionList)

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
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
}
```

- [ ] **Step 3: Update `RolesAndPermissionsTab` to include `RoleGeneralSettings` and update Save button**

```kotlin
// RolesAndPermissionsTab.kt

// Inside RolesAndPermissionsTab, near the Headline:
// ...
                    if (state.hasChanges) {
                        Row {
                            TextButton(onClick = { state.revertChanges() }) {
                                Text("Revert")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { 
                                val perms = state.pendingPermissions ?: selectedRole.permissions
                                val priority = state.pendingPriority ?: selectedRole.priorityLevel
                                onSaveChanges(selectedRole, perms, priority)
                            }) {
                                Text("Save Changes")
                            }
                        }
                    }
// ...

// Inside when (detailTabIndex) case 0:
                when (detailTabIndex) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            RoleGeneralSettings(
                                priority = state.pendingPriority ?: selectedRole.priorityLevel,
                                onPriorityChange = { state.updatePendingPriority(it) }
                            )
                            
                            PermissionList(
                                permissions = state.pendingPermissions ?: selectedRole.permissions,
                                onPermissionChange = { permission, enabled ->
                                    state.updatePendingPermission(permission, enabled)
                                }
                            )
                        }
                    }
// ...
```

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt
git commit -m "feat(ui): add priority level input and update save signature in RolesAndPermissionsTab"
```

### Task 2: Update ServerSettingsDialog to pass Priority

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt`

- [ ] **Step 1: Update `onSaveChanges` lambda in `ServerSettingsDialog`**

```kotlin
// ServerSettingsDialog.kt

// Inside RolesAndPermissionsTab call:
                                    onSaveChanges = { role, newPermissions, newPriority ->
                                        scope.launch {
                                            try {
                                                val adminId = MainState.currentUserId ?: return@launch
                                                apiClient.updateRole(
                                                    serverId = server.id,
                                                    userId = adminId,
                                                    roleId = role.id,
                                                    permissions = newPermissions,
                                                    priority = newPriority
                                                )
                                                refreshData()
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to save changes: ${e.message}"
                                            }
                                        }
                                    },
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt
git commit -m "feat(ui): pass priority level to API in ServerSettingsDialog"
```

### Task 3: Verification

- [ ] **Step 1: Build the project**

Run: `./gradlew :composeApp:build`
Expected: SUCCESS

- [ ] **Step 2: Manual Verification (Instructions)**
1. Open Server Settings -> Roles.
2. Select a role.
3. Change the Priority Level.
4. Click "Save Changes".
5. Verify that the role list is re-sorted if the priority changed significantly (roles are sorted by priority descending).
