# UI Validation and Constraints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement real-time character validation for login and enforce single-line input on key text fields.

**Architecture:** Refactor shared validation logic to support partial (character-only) validation and update Compose UI components to use these validators and input constraints.

**Tech Stack:** Kotlin, Compose Multiplatform, Kotlin Test.

---

### Task 1: Refactor UsernameValidator

**Files:**
- Modify: `shared/src/commonMain/kotlin/website/woodendoor/conflux/validation/UsernameValidator.kt`
- Test: `shared/src/commonTest/kotlin/website/woodendoor/conflux/validation/UsernameValidatorTest.kt`

- [ ] **Step 1: Add validateCharacters and refactor validateUsername**

```kotlin
package website.woodendoor.conflux.validation

object UsernameValidator {
    fun validateCharacters(username: String): ValidationResult {
        val regex = Regex("^[a-zA-Z0-9]*$") // Use * to allow empty string during typing without character error
        if (!regex.matches(username)) {
            return ValidationResult.Error("Username can only contain alphanumeric characters")
        }
        return ValidationResult.Success
    }

    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult.Error("Username cannot be empty")
        }
        if (username.length < 3) {
            return ValidationResult.Error("Username must be at least 3 characters")
        }
        if (username.length > 20) {
            return ValidationResult.Error("Username must be 20 characters or less")
        }
        return validateCharacters(username)
    }
}
```

- [ ] **Step 2: Update tests in UsernameValidatorTest.kt**

Add test cases for `validateCharacters`.

```kotlin
    @Test
    fun testValidateCharacters() {
        assertIs<ValidationResult.Success>(UsernameValidator.validateCharacters("abc123"))
        assertIs<ValidationResult.Error>(UsernameValidator.validateCharacters("user!"))
        assertIs<ValidationResult.Success>(UsernameValidator.validateCharacters("")) // Empty should be fine for character check
    }
```

- [ ] **Step 3: Run tests to verify**

Run: `./gradlew :shared:cleanTest :shared:test`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/website/woodendoor/conflux/validation/UsernameValidator.kt shared/src/commonTest/kotlin/website/woodendoor/conflux/validation/UsernameValidatorTest.kt
git commit -m "refactor: add character-only validation to UsernameValidator"
```

---

### Task 2: Update LoginScreen UI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/LoginScreen.kt`

- [ ] **Step 1: Implement real-time character validation and singleLine**

```kotlin
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                errorMessage = when (val result = UsernameValidator.validateCharacters(it)) {
                    is ValidationResult.Error -> result.message
                    ValidationResult.Success -> null
                }
            },
            label = { Text("Username") },
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
```

- [ ] **Step 2: Update Button onClick to perform full validation**

```kotlin
        Button(
            onClick = {
                when (val result = UsernameValidator.validateUsername(username)) {
                    is ValidationResult.Error -> {
                        errorMessage = result.message
                    }
                    ValidationResult.Success -> {
                        // ... existing launch block
                    }
                }
            },
            // ...
        )
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/LoginScreen.kt
git commit -m "feat: implement real-time character validation and singleLine in LoginScreen"
```

---

### Task 3: Update Dialogs to enforce singleLine

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/CreateServerDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/JoinServerDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelCreationDialog.kt`

- [ ] **Step 1: Add singleLine to CreateServerDialog**

In `CreateServerDialog.kt`, update the `TextField`.

```kotlin
                TextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    placeholder = { Text("My Awesome Server") }
                )
```

- [ ] **Step 2: Add singleLine to JoinServerDialog**

In `JoinServerDialog.kt`, verify/ensure `singleLine = true` is present (it is already there based on research, but ensure consistency).

- [ ] **Step 3: Add singleLine to ChannelCreationDialog**

In `ChannelCreationDialog.kt`, update the `TextField`.

```kotlin
                TextField(
                    value = channelName,
                    onValueChange = {
                        // ...
                    },
                    label = { Text("Channel Name") },
                    isError = validationError != null,
                    supportingText = {
                        // ...
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
```

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/CreateServerDialog.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/JoinServerDialog.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelCreationDialog.kt
git commit -m "feat: enforce singleLine input in server and channel dialogs"
```
