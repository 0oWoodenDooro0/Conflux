# Implementation Plan: Real-time Permissions & Auto-General Channel

## Phase 1: Backend Infrastructure (Auto-Channel & WS Events)
- [x] Task: Update Server Creation logic to include default `#general` channel generation. f3d9f3b
- [x] Task: Define `PERMISSION_UPDATE` WebSocket event model in shared module. 581e924
- [ ] Task: Implement permission change detection in Role/Member controllers.
- [ ] Task: Implement WebSocket broadcasting logic in `WebSocketConnectionManager` to target specific server members.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Infrastructure' (Protocol in workflow.md)

## Phase 2: Frontend Reactive Logic
- [ ] Task: Update Frontend WebSocket listener to handle `PERMISSION_UPDATE` events.
- [ ] Task: Implement permission re-fetch logic in `PermissionState` or relevant state manager.
- [ ] Task: Bind UI element visibility and enabled states to the permission state (Message input, Settings buttons).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Reactive Logic' (Protocol in workflow.md)

## Phase 3: Integration & Validation
- [ ] Task: Verify end-to-end flow: Server Creation -> Auto-channel visibility.
- [ ] Task: Verify end-to-end flow: Permission Change -> Real-time UI Lock/Unlock.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration & Validation' (Protocol in workflow.md)
