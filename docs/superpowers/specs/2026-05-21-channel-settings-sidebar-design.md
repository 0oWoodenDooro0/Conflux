# Channel Settings Dialog Sidebar Design

This specification defines the implementation plan for replacing the top-level tab row in the Channel Settings dialog (`ChannelSettingsDialog`) with a vertical sidebar, mimicking the aesthetic and structure of the `ServerSettingsDialog`.

---

## Architectural Changes

### 1. New Navigation Enum (`ChannelSettingsTab`)
We will introduce a new `ChannelSettingsTab` enum class under `website.woodendoor.conflux.ui` to represent the settings screens:
* `Overview`
* `Permissions`

We will also add an accompanying unit test file `ChannelSettingsTabTest.kt` to verify that the enum entries are correct and consistent.

### 2. Refactoring the Channel Settings Dialog Layout
In `ChannelSettingsDialog.kt`, we will refactor `ChannelSettingsDialog` to lay out the sidebar on the left and the active tab content on the right:
* Use a horizontal `Row` within the `text` area of the `AlertDialog`.
* **Sidebar (Left)**: A `Column` of width `240.dp` with `NavigationDrawerItem` elements for each tab, with active tab tracking using `ChannelSettingsTab`.
* **Divider**: A `VerticalDivider` separating the sidebar from the content.
* **Content (Right)**: A `Box` taking up the remaining weight, rendering `ChannelOverviewTab` or `ChannelPermissionsTab` based on the selected tab.

---

## Proposed Changes

### [composeApp]

#### [NEW] [ChannelSettingsTab.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsTab.kt)
- Define `ChannelSettingsTab` enum class with `Overview` and `Permissions` values.

#### [NEW] [ChannelSettingsTabTest.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonTest/kotlin/website/woodendoor/conflux/ui/ChannelSettingsTabTest.kt)
- Create basic unit tests to verify the enum contains the correct tabs.

#### [MODIFY] [ChannelSettingsDialog.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSettingsDialog.kt)
- Import `ChannelSettingsTab`.
- Refactor state from `var selectedTab by remember { mutableStateOf(0) }` to `var selectedTab by remember { mutableStateOf(ChannelSettingsTab.Overview) }`.
- Remove the `TabRow` from the `title` element of the `AlertDialog`.
- Layout the text body of the `AlertDialog` as a `Row` containing the sidebar `NavigationDrawerItem` list, a `VerticalDivider`, and the corresponding tab contents on the right side.

---

## Verification Plan

### Automated Tests
- Run the new `ChannelSettingsTabTest` and all existing tests using:
  ```bash
  ./gradlew :composeApp:test
  ```

### Manual Verification
- Deploy/run the desktop/KMP Compose client.
- Click the Channel settings gear to open the `ChannelSettingsDialog`.
- Verify the sidebar displays correctly on the left, allows switching tabs smoothly, and displays each tab's contents correctly.
