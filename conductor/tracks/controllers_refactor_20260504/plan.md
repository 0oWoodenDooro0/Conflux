# Implementation Plan: Application Layer Controllers

## Phase 1: Foundation & Result Types [checkpoint: 3c51e9d]
Establish the common error handling and result patterns used by all controllers.

- [x] Task: Define `OperationResult` sealed class for business logic errors. ec4acc1
- [x] Task: Implement base DTO mapping utilities. ddae0d4
- [x] Task: Conductor - User Manual Verification 'Phase 1: Foundation & Result Types' (Protocol in workflow.md) 3c51e9d

## Phase 2: RoleController & Permissions
Focus on the new `RoleController` and moving permission logic.

- [x] Task: Write Unit Tests for `RoleController` (Red Phase). d1d1a81
- [ ] Task: Implement `RoleController` with Role management and Permission checks (Green Phase).
- [ ] Task: Refactor existing permission checks to use `RoleController` logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: RoleController & Permissions' (Protocol in workflow.md)

## Phase 3: Server & Channel Controllers
Refactor server and channel logic into their respective controllers.

- [ ] Task: Write Unit Tests for `ServerController` and `ChannelController` (Red Phase).
- [ ] Task: Implement `ServerController` and `ChannelController` (Green Phase).
- [ ] Task: Refactor `ServerRoutes` and `ChannelRoutes` to delegate to controllers.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Server & Channel Controllers' (Protocol in workflow.md)

## Phase 4: ChatController & WebSocket Integration
Refactor messaging and history retrieval.

- [ ] Task: Write Unit Tests for `ChatController` (Red Phase).
- [ ] Task: Implement `ChatController` handling messages and history (Green Phase).
- [ ] Task: Refactor `MessageRoutes` and `WebSocket` logic to delegate to `ChatController`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: ChatController & WebSocket Integration' (Protocol in workflow.md)

## Phase 5: Final Integration & Cleanup
Ensure all routes are consistent and legacy logic is removed.

- [ ] Task: Audit all routes to ensure zero direct business logic.
- [ ] Task: Verify >80% coverage across all new controller logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Final Integration & Cleanup' (Protocol in workflow.md)
