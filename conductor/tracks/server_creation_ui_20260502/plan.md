# Implementation Plan: Server Creation UI and Owner Support

**Phase 1: Shared Models and Client Updates** [checkpoint: 6da4eee]
- [x] Task: Update `CreateServerRequest` model to include `ownerId` field. 4f0315c
    - [x] Add `ownerId` to `CreateServerRequest` in `shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ApiModels.kt`.
    - [x] Update `ApiModelsTest.kt` to verify serialization with the new field.
- [x] Task: Update `ServerApiClient` to pass `ownerId` when creating a server. c4640c6
    - [x] Update `createServer` signature in `ServerApiClient.kt`.
    - [x] Update `testCreateServer` in `ServerApiClientTest.kt` to assert `ownerId` is sent in the request body.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Shared Models and Client Updates' (Protocol in workflow.md) 6da4eee

**Phase 2: Server-Side Implementation**
- [x] Task: Update Server route to use `ownerId` from the POST request. 1107d72
    - [x] Modify `serverRoutes` in `server/src/main/kotlin/website/woodendoor/conflux/routes/ServerRoutes.kt`.
    - [x] Remove the hardcoded `"default-user"` owner.
- [x] Task: Update server list query to be robust (handle username and ID). 4715d5d
    - [x] Modify `getServersForUser` in `ExposedServerRepository.kt` to resolve username to ID if necessary.
    - [x] Ensure the query correctly returns servers where the user is owner OR member.
- [~] Task: Verify server-side server creation and querying with tests.
    - [ ] Add/Update tests in `server/src/test/kotlin/website/woodendoor/conflux/ServerListRouteTest.kt` to check if `ownerId` is correctly processed and queried.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Server-Side Implementation' (Protocol in workflow.md)

**Phase 3: UI Integration**
- [ ] Task: Add "Create Server" button to the server sidebar.
    - [ ] Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSidebar.kt`.
    - [ ] Add a "+" button at the bottom of the server list.
- [ ] Task: Implement navigation from Sidebar to `CreateServerScreen`.
    - [ ] Update `MainScreen.kt` to manage state for showing the `CreateServerScreen`.
- [ ] Task: Connect `CreateServerScreen` with the current user context.
    - [ ] Ensure `CreateServerScreen` uses the logged-in user's username for the `ownerId`.
    - [ ] Update `CreateServerScreen.kt` to use the updated `apiClient.createServer` method.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: UI Integration' (Protocol in workflow.md)
