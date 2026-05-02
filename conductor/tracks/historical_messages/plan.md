# Implementation Plan: Static Historical Messages (HTTP-based)

## Phase 1: Backend Persistence Layer [checkpoint: a4352cf]
- [x] Task: Define `Messages` Table 46a6ec3
    - [x] Create `Messages.kt` with Exposed DSL.
    - [x] Register table in `DatabaseFactory`.
- [x] Task: Implement `MessageRepository` (TDD) f9aa5ed
    - [x] **Red**: Write failing unit tests for `saveMessage` and `getMessagesByChannel`.
    - [x] **Green**: Implement `MessageRepository` and `ExposedMessageRepository`.
    - [x] **Verify**: Ensure tests pass and coverage >80%.
- [x] Task: Conductor - User Manual Verification 'Backend Persistence Layer' (Protocol in workflow.md) a4352cf

## Phase 2: API & Shared Client [checkpoint: c7e2b93]
- [x] Task: Implement Message Routes (TDD) 034936f
    - [x] **Red**: Write failing integration tests for `POST` and `GET` message endpoints.
    - [x] **Green**: Implement Ktor routes and dependency injection for the repository.
    - [x] **Verify**: Ensure integration tests pass.
- [x] Task: Extend `ServerApiClient` in Shared Module 35d979c
    - [x] Define `Message` model in `shared/commonMain`.
    - [x] Implement `getMessages` and `sendMessage` in `ServerApiClient`.
    - [x] Write unit tests for the client methods using Ktor `MockEngine`.
- [x] Task: Conductor - User Manual Verification 'API & Shared Client' (Protocol in workflow.md) c7e2b93

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
