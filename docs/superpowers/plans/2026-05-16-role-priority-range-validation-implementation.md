# Role Priority Range Validation (0-100) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enforce a 0-100 range for role priority levels in both the client and server.

**Architecture:** 
- Client-side: Update `RolesAndPermissionsState` to validate priority range and update `RolesAndPermissionsTab` to show error states and disable saving.
- Server-side: Update `ServerRoutes` to reject `CreateRoleRequest` and `UpdateRoleRequest` if `priorityLevel` is outside the 0-100 range.

**Tech Stack:** Kotlin, Ktor, Compose Multiplatform.

---

### Task 1: Client State Validation

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt`
- Test: `composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsStateTest.kt`

- [ ] **Step 1: Write failing tests for priority validation**

```kotlin
// composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsStateTest.kt

@Test
fun testPriorityValidation() {
    val role = Role("r1", "s1", "Admin", 1L, priorityLevel = 50)
    val state = RolesAndPermissionsState(listOf(role))
    state.selectRole(role)
    
    assertTrue(state.isPriorityValid)
    
    state.updatePendingPriority(-1)
    assertTrue(!state.isPriorityValid)
    
    state.updatePendingPriority(101)
    assertTrue(!state.isPriorityValid)
    
    state.updatePendingPriority(100)
    assertTrue(state.isPriorityValid)
    
    state.updatePendingPriority(0)
    assertTrue(state.isPriorityValid)
}
```

- [ ] **Step 2: Run tests to verify failure**

Run: `./gradlew :composeApp:test --tests "website.woodendoor.conflux.ui.RolesAndPermissionsStateTest.testPriorityValidation"`
Expected: FAIL (isPriorityValid not defined)

- [ ] **Step 3: Implement `isPriorityValid` and update `hasChanges`**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt

// ... inside class RolesAndPermissionsState
    val isPriorityValid: Boolean
        get() = pendingPriority == null || pendingPriority in 0..100

    val hasChanges: Boolean
        get() = ((pendingPermissions != null && pendingPermissions != selectedRole?.permissions) ||
                (pendingPriority != null && pendingPriority != selectedRole?.priorityLevel)) &&
                isPriorityValid
// ...
```

- [ ] **Step 4: Run tests to verify success**

Run: `./gradlew :composeApp:test --tests "website.woodendoor.conflux.ui.RolesAndPermissionsStateTest.testPriorityValidation"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsState.kt composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsStateTest.kt
git commit -m "feat(ui): add priority range validation to state"
```

---

### Task 2: Client UI Feedback

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt`

- [ ] **Step 1: Update `RoleGeneralSettings` to show error state**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt

@Composable
fun RoleGeneralSettings(
    priority: Int,
    isError: Boolean, // Add this parameter
    onPriorityChange: (Int) -> Unit
) {
    var textValue by remember(priority) { mutableStateOf(priority.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue
            newValue.toIntOrNull()?.let { onPriorityChange(it) }
        },
        label = { Text("Priority Level") },
        isError = isError, // Use this
        supportingText = if (isError) {
            { Text("Priority must be between 0 and 100") }
        } else null,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
    )
}
```

- [ ] **Step 2: Pass `isError` to `RoleGeneralSettings` in `RolesAndPermissionsTab`**

```kotlin
// composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt

// Inside RolesAndPermissionsTab, find where RoleGeneralSettings is called:
RoleGeneralSettings(
    priority = state.pendingPriority ?: selectedRole.priorityLevel,
    isError = !state.isPriorityValid, // Pass validation state
    onPriorityChange = { state.updatePendingPriority(it) }
)
```

- [ ] **Step 3: Verify UI behavior**
(Manual verification after full implementation)

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/RolesAndPermissionsTab.kt
git commit -m "feat(ui): display priority validation errors in UI"
```

---

### Task 3: Server Route Validation

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt`
- Test: `server/src/test/kotlin/website/woodendoor/conflux/routes/ServerRoutesPriorityTest.kt` (New)

- [ ] **Step 1: Write failing integration tests for server validation**

```kotlin
// server/src/test/kotlin/website/woodendoor/conflux/routes/ServerRoutesPriorityTest.kt
// (Create new file, follow pattern from existing route tests like ServerListRouteTest.kt)

package website.woodendoor.conflux.routes

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import website.woodendoor.conflux.models.CreateRoleRequest
import website.woodendoor.conflux.models.UpdateRoleRequest
import website.woodendoor.conflux.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ServerRoutesPriorityTest {
    @Test
    fun testCreateRoleInvalidPriority() = testApplication {
        application { module() }
        val response = client.post("/servers/s1/roles?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateRoleRequest("Invalid", priorityLevel = 101)))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testUpdateRoleInvalidPriority() = testApplication {
        application { module() }
        val response = client.patch("/servers/s1/roles/r1?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(priorityLevel = -1)))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run: `./gradlew :server:test --tests "website.woodendoor.conflux.routes.ServerRoutesPriorityTest"`
Expected: FAIL (Returns 200 or 201 because validation is missing)

- [ ] **Step 3: Implement validation in `ServerRoutes.kt`**

```kotlin
// server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt

// In post route:
val request = call.receive<CreateRoleRequest>()
if (request.priorityLevel !in 0..100) {
    return@post call.respond(HttpStatusCode.BadRequest, "Priority level must be between 0 and 100")
}

// In patch route:
val request = call.receive<UpdateRoleRequest>()
if (request.priorityLevel != null && request.priorityLevel !in 0..100) {
    return@patch call.respond(HttpStatusCode.BadRequest, "Priority level must be between 0 and 100")
}
```

- [ ] **Step 4: Run tests to verify success**

Run: `./gradlew :server:test --tests "website.woodendoor.conflux.routes.ServerRoutesPriorityTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt server/src/test/kotlin/website/woodendoor/conflux/routes/ServerRoutesPriorityTest.kt
git commit -m "feat(server): validate role priority range in routes"
```

---

### Task 4: Verification

- [ ] **Step 1: Run full test suite**

Run: `./gradlew test`

- [ ] **Step 2: Manual Verification**
1. Open Server Settings > Roles.
2. Select a role.
3. Change priority to 101.
4. Verify error message appears and Save button is disabled.
5. Change priority to -1.
6. Verify error message appears and Save button is disabled.
7. Change priority to 50 and Save.
8. Verify change is persisted.
