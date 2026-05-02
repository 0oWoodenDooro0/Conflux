# Implementation Plan: Static Historical Messages (HTTP-based)

## Phase 1: Backend Persistence Layer
- [x] Task: Define `Messages` Table 46a6ec3
    - [ ] Create `Messages.kt` with Exposed DSL.
    - [ ] Register table in `DatabaseFactory`.
- [x] Task: Implement `MessageRepository` (TDD) f9aa5ed
    - [ ] **Red**: Write failing unit tests for `saveMessage` and `getMessagesByChannel`.
    - [ ] **Green**: Implement `MessageRepository` and `ExposedMessageRepository`.
    - [ ] **Verify**: Ensure tests pass and coverage >80%.
- [ ] Task: Conductor - User Manual Verification 'Backend Persistence Layer' (Protocol in workflow.md)

## Phase 2: API & Shared Client
- [ ] Task: Implement Message Routes (TDD)
    - [ ] **Red**: Write failing integration tests for `POST` and `GET` message endpoints.
    - [ ] **Green**: Implement Ktor routes and dependency injection for the repository.
    - [ ] **Verify**: Ensure integration tests pass.
- [ ] Task: Extend `ServerApiClient` in Shared Module
    - [ ] Define `Message` model in `shared/commonMain`.
    - [ ] Implement `getMessages` and `sendMessage` in `ServerApiClient`.
    - [ ] Write unit tests for the client methods using Ktor `MockEngine`.
- [ ] Task: Conductor - User Manual Verification 'API & Shared Client' (Protocol in workflow.md)

## Phase 3: Frontend State & UI
- [ ] Task: Update `MainState` for Message Management (TDD)
    - [ ] **Red**: Write tests for `MainState` message fetching and sending logic.
    - [ ] **Green**: Implement state updates, error handling, and API integration.
- [ ] Task: Implement Chat Room UI
    - [ ] Create `MessageList` with `LazyColumn`.
    - [ ] Create `MessageInput` with auto-focus and character limit (2000).
    - [ ] Implement Auto-Scroll and "Scroll to Bottom" button.
- [ ] Task: Conductor - User Manual Verification 'Frontend State & UI' (Protocol in workflow.md)

## Phase 4: Final Validation
- [ ] Task: End-to-End Testing
    - [ ] Verify full flow: Login -> Select Server -> Select Channel -> Send Message -> Refresh/View History.
- [ ] Task: Performance and Edge Case Verification
    - [ ] Verify character limit enforcement.
    - [ ] Verify persistence after server restart.
- [ ] Task: Conductor - User Manual Verification 'Final Validation' (Protocol in workflow.md)
