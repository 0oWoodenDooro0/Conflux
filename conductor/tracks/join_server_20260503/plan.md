# Implementation Plan: Join Server

## Phase 1: Database & Repository (Backend) [checkpoint: 2d5156e]
- [x] Task: Write Tests for Server Repository `joinServer` cb188a5
- [x] Task: Implement `joinServer` in `ExposedServerRepository.kt` a69a42a
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database & Repository (Backend)' (Protocol in workflow.md) 2d5156e

## Phase 2: Routes (Backend API)
- [ ] Task: Write Tests for POST `/servers/{id}/join` endpoint
- [ ] Task: Implement POST `/servers/{id}/join` endpoint in `ServerRoutes.kt`
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Routes (Backend API)' (Protocol in workflow.md)

## Phase 3: API Client (Shared)
- [ ] Task: Write Tests for `ServerApiClient.joinServer`
- [ ] Task: Implement `joinServer` in `ServerApiClient.kt`
- [ ] Task: Conductor - User Manual Verification 'Phase 3: API Client (Shared)' (Protocol in workflow.md)

## Phase 4: State Management & UI (Frontend)
- [ ] Task: Write Tests for `MainState` join server logic
- [ ] Task: Implement join server state handling and list refresh in `MainState.kt`
- [ ] Task: Write Tests for Join Server Dialog UI components
- [ ] Task: Implement Join Server Dialog and Trigger Button in Compose UI
- [ ] Task: Conductor - User Manual Verification 'Phase 4: State Management & UI (Frontend)' (Protocol in workflow.md)