# Implementation Plan: Database Infrastructure & Core Repositories

## Phase 1: Setup & Infrastructure [checkpoint: 41fdbf4]
- [x] Task: Add Exposed 1.0 and H2 dependencies to `libs.versions.toml` and configure `server/build.gradle.kts` e75c78f
- [x] Task: Implement `DatabaseFactory` to manage H2 in-memory connection and transaction context bc5524c
- [x] Task: Conductor - User Manual Verification 'Setup & Infrastructure' (Protocol in workflow.md) 41fdbf4

## Phase 2: Identity Persistence (User) [checkpoint: 41fdbf4]
- [x] Task: Define `Users` Exposed table in the `server` module 6e057dd
- [x] Task: Create `UserRepository` with CRUD operations ca394a4
- [x] Task: Write unit tests for `UserRepository` (TDD: Red Phase) 777fd24
- [x] Task: Implement repository logic to pass tests (Green Phase) f6a8c86
- [x] Task: Conductor - User Manual Verification 'Identity Persistence' (Protocol in workflow.md) 41fdbf4

## Phase 3: Structural Persistence (Server, Role, Channel) [checkpoint: 41fdbf4]
- [x] Task: Define `Servers`, `Roles`, and `Channels` Exposed tables 2a4ed5e
- [x] Task: Create `ServerRepository` and `ChannelRepository` 2a4ed5e
- [x] Task: Write unit tests for `ServerRepository` and `ChannelRepository` (TDD: Red Phase) a36ac26
- [x] Task: Implement repository logic to pass tests (Green Phase) f6a8c86
- [x] Task: Conductor - User Manual Verification 'Structural Persistence' (Protocol in workflow.md) 41fdbf4

## Phase 4: Integration & Initialization
- [ ] Task: Integrate `DatabaseFactory.init()` into the Ktor `module` in `Application.kt`
- [ ] Task: Implement a health check or smoke test to verify DB connectivity on startup
- [ ] Task: Conductor - User Manual Verification 'Integration & Initialization' (Protocol in workflow.md)
