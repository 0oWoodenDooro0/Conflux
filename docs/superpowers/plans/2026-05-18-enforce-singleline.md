# Enforce singleLine in Server Settings and Roles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enforce `singleLine = true` for role names, priority levels, and member search fields in the Server Settings UI to ensure consistent input behavior.

**Architecture:** Surgical updates to existing `OutlinedTextField` components in the Compose Multiplatform UI.

**Tech Stack:** Kotlin, Compose Multiplatform (Material3).

---

### Task 1: Update RoleCreationDialog in ServerSettingsDialog.kt

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt`

- [ ] **Step 1: Update "Role Name" and "Priority Level" fields**

Modify `RoleCreationDialog` to add `singleLine = true` to both `OutlinedTextField` instances.

```kotlin
// In ServerSettingsDialog.kt

@Composable
fun RoleCreationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long, Int) -> Unit
) {
    // ...
    AlertDialog(
        // ...
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true // Add this
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it },
                    label = { Text("Priority Level") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isPriorityValid,
                    supportingText = {
                        // ...
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true // Add this
                )
                // ...
            }
        },
        // ...
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSettingsDialog.kt
git commit -m "feat: enforce singleLine for Role Name and Priority in creation dialog"
```

---

### Task 2: Update RoleGeneralSettings in RolesAndPermissionsTab.kt

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt`

- [ ] **Step 1: Update "Priority Level" field**

Modify `RoleGeneralSettings` to add `singleLine = true` to the `OutlinedTextField`.

```kotlin
// In RolesAndPermissionsTab.kt

@Composable
fun RoleGeneralSettings(
    priorityText: String,
    isError: Boolean,
    onPriorityChange: (String) -> Unit
) {
    OutlinedTextField(
        value = priorityText,
        onValueChange = onPriorityChange,
        label = { Text("Priority Level") },
        isError = isError,
        supportingText = if (isError) {
            { Text("Priority must be between 0 and 100") }
        } else null,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        singleLine = true // Add this
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt
git commit -m "feat: enforce singleLine for Priority Level in general settings"
```

---

### Task 3: Update MemberAssignmentView in RolesAndPermissionsTab.kt

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt`

- [ ] **Step 1: Update "Add Member" search field**

Modify `MemberAssignmentView` to add `singleLine = true` to the search `OutlinedTextField`.

```kotlin
// In RolesAndPermissionsTab.kt

@Composable
fun MemberAssignmentView(
    allMembers: List<User>,
    roleMembers: List<User>,
    onAddMember: (User) -> Unit,
    onRemoveMember: (User) -> Unit
) {
    // ...
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Add
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    isDropdownExpanded = it.isNotEmpty()
                },
                label = { Text("Add Member") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; isDropdownExpanded = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true // Add this
            )
            // ...
        }
        // ...
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt
git commit -m "feat: enforce singleLine for Add Member search field"
```
