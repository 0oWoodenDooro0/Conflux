# Implementation Plan: Application Layer Controllers

## Phase 1: Foundation & Result Types [checkpoint: 3c51e9d]
Establish the common error handling and result patterns used by all controllers.

- [x] Task: Define `OperationResult` sealed class for business logic errors. ec4acc1
- [x] Task: Implement base DTO mapping utilities. ddae0d4
- [x] Task: Conductor - User Manual Verification 'Phase 1: Foundation & Result Types' (Protocol in workflow.md) 3c51e9d

## Phase 2: RoleController & Permissions [checkpoint: e602138]
Focus on the new `RoleController` and moving permission logic.

- [x] Task: Write Unit Tests for `RoleController` (Red Phase). d1d1a81
- [x] Task: Implement `RoleController` with Role management and Permission checks (Green Phase). 418d3a8
- [x] Task: Refactor existing permission checks to use `RoleController` logic. f80b24b
- [x] Task: Conductor - User Manual Verification 'Phase 2: RoleController & Permissions' (Protocol in workflow.md) e602138

## Phase 3: Server & Channel Controllers [checkpoint: 056f1fd]
Refactor server and channel logic into their respective controllers.

- [x] Task: Write Unit Tests for `ServerController` and `ChannelController` (Red Phase). 2693f8a
- [x] Task: Implement `ServerController` and `ChannelController` (Green Phase). b05bff6
- [x] Task: Refactor `ServerRoutes` and `ChannelRoutes` to delegate to controllers. 3ef21f1
- [x] Task: Conductor - User Manual Verification 'Phase 3: Server & Channel Controllers' (Protocol in workflow.md) 056f1fd

## Phase 4: ChatController & WebSocket Integration
Refactor messaging and history retrieval.

- [x] Task: Write Unit Tests for `ChatController` (Red Phase). 1b99abd
- [x] Task: Implement `ChatController` handling messages and history (Green Phase). c02d642
- [~] Task: Refactor `MessageRoutes` and `WebSocket` logic to delegate to `ChatController`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: ChatController & WebSocket Integration' (Protocol in workflow.md)

## Phase 5: Final Integration & Cleanup
Ensure all routes are consistent and legacy logic is removed.

- [ ] Task: Audit all routes to ensure zero direct business logic.
- [ ] Task: Verify >80% coverage across all new controller logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Final Integration & Cleanup' (Protocol in workflow.md)
