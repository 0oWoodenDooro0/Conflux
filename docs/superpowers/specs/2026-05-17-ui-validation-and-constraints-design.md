# Design Spec - UI Validation and Input Constraints

Targeting a more responsive login experience and preventing accidental multi-line input in critical UI components.

## Goals
- Prevent multi-line input in login, server creation, server joining, and channel creation text fields.
- Implement differentiated validation timing for login:
    - **Real-time**: Immediate feedback for illegal characters.
    - **On-Action**: Feedback for length requirements only when the user attempts to submit.

## Proposed Changes

### 1. Shared Validation Logic (`shared`)
- Refactor `UsernameValidator` to expose character-only validation.

#### `website.woodendoor.conflux.validation.UsernameValidator`
- Add `validateCharacters(username: String): ValidationResult` to check only against alphanumeric regex.
- Keep `validateUsername(username: String): ValidationResult` for full validation (length + characters).

### 2. Login Screen (`composeApp`)
- Update `LoginScreen.kt`:
    - `OutlinedTextField`: Add `singleLine = true`.
    - `onValueChange`: Call `UsernameValidator.validateCharacters`. If it fails, show error immediately. If it passes, clear the error (even if length is still invalid).
    - `Button.onClick`: Call `UsernameValidator.validateUsername` for full validation before proceeding.

### 3. Dialog Constraints (`composeApp`)
- Update the following files to add `singleLine = true` to their primary input fields:
    - `CreateServerDialog.kt` (Server Name)
    - `JoinServerDialog.kt` (Server ID)
    - `ChannelCreationDialog.kt` (Channel Name)

## Verification Plan

### Automated Tests
- Update/Add unit tests for `UsernameValidator` to ensure `validateCharacters` and `validateUsername` behave as expected.

### Manual Verification
1. **Login Screen**:
    - Type a special character (e.g., `@`). Error "Username can only contain alphanumeric characters" should appear immediately.
    - Type a valid but short name (e.g., "ab"). No error should appear while typing.
    - Click "Login" with "ab". Error "Username must be at least 3 characters" should appear.
    - Verify that pressing "Enter" does not create a new line.
2. **Dialogs**:
    - Open Create Server, Join Server, and Create Channel dialogs.
    - Verify that text fields do not allow new lines (pressing Enter should trigger the action or do nothing, but not wrap).
