# Implementation Plan: Join Server

## Phase 1: Database & Repository (Backend) [checkpoint: 2d5156e]
- [x] Task: Write Tests for Server Repository `joinServer` cb188a5
- [x] Task: Implement `joinServer` in `ExposedServerRepository.kt` a69a42a
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database & Repository (Backend)' (Protocol in workflow.md) 2d5156e

## Phase 2: Routes (Backend API) [checkpoint: f1c24d1]
- [x] Task: Write Tests for POST `/servers/{id}/join` endpoint d314523
- [x] Task: Implement POST `/servers/{id}/join` endpoint in `ServerRoutes.kt` bfe9611
- [x] Task: Conductor - User Manual Verification 'Phase 2: Routes (Backend API)' (Protocol in workflow.md) f1c24d1

## Phase 3: API Client (Shared) [checkpoint: b068239]
- [x] Task: Write Tests for `ServerApiClient.joinServer` 9dbe51b
- [x] Task: Implement `joinServer` in `ServerApiClient.kt` 8bb0db0
- [x] Task: Conductor - User Manual Verification 'Phase 3: API Client (Shared)' (Protocol in workflow.md) b068239

## Phase 4: State Management & UI (Frontend) [checkpoint: c284985]
- [x] Task: Write Tests for `MainState` join server logic e0692fe
- [x] Task: Implement join server state handling and list refresh in `MainState.kt` b476799
- [x] Task: Write Tests for Join Server Dialog UI components afdbd08
- [x] Task: Implement Join Server Dialog and Trigger Button in Compose UI 5fabd64
- [x] Task: Conductor - User Manual Verification 'Phase 4: State Management & UI (Frontend)' (Protocol in workflow.md) c284985

## Phase: Review Fixes
- [x] Task: Apply review suggestions 93ac8e1