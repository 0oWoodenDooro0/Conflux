# Implementation Plan: Database Infrastructure & Core Repositories

## Phase 1: Setup & Infrastructure
- [x] Task: Add Exposed 1.0 and H2 dependencies to `libs.versions.toml` and configure `server/build.gradle.kts` e75c78f
- [ ] Task: Implement `DatabaseFactory` to manage H2 in-memory connection and transaction context
- [ ] Task: Conductor - User Manual Verification 'Setup & Infrastructure' (Protocol in workflow.md)

## Phase 2: Identity Persistence (User)
- [ ] Task: Define `Users` Exposed table in the `server` module
- [ ] Task: Create `UserRepository` with CRUD operations
- [ ] Task: Write unit tests for `UserRepository` (TDD: Red Phase)
- [ ] Task: Implement repository logic to pass tests (Green Phase)
- [ ] Task: Conductor - User Manual Verification 'Identity Persistence' (Protocol in workflow.md)

## Phase 3: Structural Persistence (Server, Role, Channel)
- [ ] Task: Define `Servers`, `Roles`, and `Channels` Exposed tables
- [ ] Task: Create `ServerRepository` and `ChannelRepository`
- [ ] Task: Write unit tests for `ServerRepository` and `ChannelRepository` (TDD: Red Phase)
- [ ] Task: Implement repository logic to pass tests (Green Phase)
- [ ] Task: Conductor - User Manual Verification 'Structural Persistence' (Protocol in workflow.md)

## Phase 4: Integration & Initialization
- [ ] Task: Integrate `DatabaseFactory.init()` into the Ktor `module` in `Application.kt`
- [ ] Task: Implement a health check or smoke test to verify DB connectivity on startup
- [ ] Task: Conductor - User Manual Verification 'Integration & Initialization' (Protocol in workflow.md)
