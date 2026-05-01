# Implementation Plan: Simple Login and Server List

## Phase 1: Shared Models and Validation [checkpoint: 4bea48b]
- [x] Task: Create `LoginRequest` model in `shared` module. fc5c774
    - [ ] Define `LoginRequest` data class with `@Serializable`.
- [x] Task: Implement `UsernameValidator` in `shared` module. a021c22
    - [ ] Write tests for username validation (Red).
    - [ ] Implement `validateUsername` function (Green).
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Shared Models and Validation' (Protocol in workflow.md)

## Phase 2: Backend Implementation
- [x] Task: Update `UserRepository` to support finding users by username. 24a4f10
    - [ ] Add `findByUsername` to `UserRepository` interface.
    - [ ] Implement `findByUsername` in `ExposedUserRepository`.
    - [ ] Write integration test for `findByUsername` (Red -> Green).
- [x] Task: Implement `POST /api/login` route. 35cb601
    - [ ] Write integration tests for login route (success, new user, validation fail) (Red).
    - [ ] Implement route logic in `ServerRoutes.kt` (Green).
- [ ] Task: Implement `GET /api/servers` route.
    - [ ] Write integration tests for fetching user's servers (Red).
    - [ ] Implement route logic in `ServerRoutes.kt` (Green).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Backend Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Login Screen
- [ ] Task: Create `LoginState` to manage current user in memory.
    - [ ] Define a shared state holder for `currentUser`.
- [ ] Task: Implement `LoginScreen` UI.
    - [ ] Create `LoginScreen.kt`.
    - [ ] Implement username TextField and Login Button.
    - [ ] Add validation logic call.
- [ ] Task: Connect `LoginScreen` to Backend Login API.
    - [ ] Add `login` method to `ServerApiClient`.
    - [ ] Update `LoginScreen` to call `ServerApiClient.login`.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Login Screen' (Protocol in workflow.md)

## Phase 4: Main Screen and Server List
- [ ] Task: Implement `MainScreen` scaffolding.
    - [ ] Create `MainScreen.kt`.
    - [ ] Add fetching logic for servers using `currentUser.id`.
- [ ] Task: Create `ServerSidebar` component.
    - [ ] Implement a vertical list for servers.
    - [ ] Apply Discord-style styling (narrow icons/labels).
- [ ] Task: Integrate `ServerSidebar` into `MainScreen`.
    - [ ] Combine sidebar with a main content placeholder.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Main Screen and Server List' (Protocol in workflow.md)
