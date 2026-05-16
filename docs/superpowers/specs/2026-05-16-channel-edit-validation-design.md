# Spec: Channel Edit Validation

## Background
Currently, the "Edit Channel" dialog (`ChannelSettingsDialog`) only checks if the name is not blank before allowing a save. The "Create Channel" dialog (`ChannelCreationDialog`) already uses a `ChannelValidator` to enforce more strict rules (length, characters). We need to align these two to ensure data consistency.

## Goals
- Enforce the same validation rules in `ChannelSettingsDialog` as in `ChannelCreationDialog`.
- Provide immediate visual feedback to the user when they enter an invalid name.
- Prevent submission of invalid channel names.

## Proposed Changes

### `ChannelSettingsDialog.kt`
1.  **Add State**:
    - Add `var validationError by remember { mutableStateOf<String?>(null) }`.
2.  **Update `TextField`**:
    - Update `onValueChange` to run validation using `ChannelValidator.validateName(it)`.
    - Set `isError = validationError != null`.
    - Add `supportingText` to display `validationError`.
3.  **Update `Button` (Save Changes)**:
    - Update `enabled` condition to: `name.isNotBlank() && name != channel.name && !isLoading && validationError == null`.

## Verification Plan
### Automated Tests
- Ensure `ChannelValidatorTest` covers all rules (it already does).
- If feasible, add a test case to verify `ChannelSettingsDialog`'s validation state, or trust the `ChannelValidator` integration.

### Manual Verification
1.  Open Channel Settings for any channel.
2.  Try to set the name to something invalid (e.g., empty, >32 chars, or special characters like `!`).
3.  Verify that an error message appears below the text field.
4.  Verify that the "Save Changes" button is disabled.
5.  Set the name to a valid value (e.g., `new-general`).
6.  Verify that the error message disappears and the button is enabled.
7.  Save and verify the update works.
