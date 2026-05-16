# Channel Edit Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the channel editing validation with the channel creation validation by using `ChannelValidator`.

**Architecture:** Update `ChannelSettingsDialog` to use `ChannelValidator` for its name input field, providing real-time feedback and disabling the save button on invalid input.

**Tech Stack:** Kotlin, Jetpack Compose Multiplatform.

---

### Task 1: Add Validation State and Logic to ChannelSettingsDialog

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt`

- [ ] **Step 1: Add validationError state**

```kotlin
// Inside ChannelSettingsDialog function
var name by remember { mutableStateOf(channel.name) }
var validationError by remember { mutableStateOf<String?>(null) } // Add this
var isDeleting by remember { mutableStateOf(false) }
```

- [ ] **Step 2: Update TextField onValueChange**

```kotlin
// In TextField
TextField(
    value = name,
    onValueChange = { 
        name = it 
        validationError = when (val result = website.woodendoor.conflux.validation.ChannelValidator.validateName(it)) {
            is website.woodendoor.conflux.validation.ValidationResult.Error -> result.message
            website.woodendoor.conflux.validation.ValidationResult.Success -> null
        }
    },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    placeholder = { Text("new-channel-name") },
    isError = validationError != null, // Add this
    supportingText = { // Add this
        if (validationError != null) {
            Text(validationError!!, color = MaterialTheme.colorScheme.error)
        }
    }
)
```

- [ ] **Step 3: Update Save Changes Button enabled state**

```kotlin
// In confirmButton -> Button
enabled = name.isNotBlank() && name != channel.name && !isLoading && validationError == null
```

- [ ] **Step 4: Commit changes**

```bash
git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt
git commit -m "feat(ui): Add validation to channel settings dialog"
```

### Task 2: Verify and Finalize

- [ ] **Step 1: Manual Verification**
    - Run the app (web/desktop).
    - Open Channel Settings.
    - Test invalid names: empty, >32 chars, special characters.
    - Verify error message and button state.
    - Test valid change: verify save works.

- [ ] **Step 2: Final Commit**
```bash
git commit --allow-empty -m "chore: Finalize channel edit validation"
```
