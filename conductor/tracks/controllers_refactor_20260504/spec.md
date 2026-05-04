# Specification: Application Layer Controllers

## Overview
This track introduces a formal Application Layer (Controllers) to the backend. Currently, business logic is scattered across Routes and Repositories. This refactor aligns the codebase with the Design Class Model (hw4), specifically implementing `ServerController`, `RoleController`, `ChannelController`, and `ChatController`.

## Functional Requirements
- **Layer Separation**: Create a `website.woodendoor.conflux.controller` package in the backend module.
- **ServerController**: Handle server creation, joining, and metadata management.
- **RoleController**: Implement role management and permission checks. This fills the current gap in Role/Permission API handling.
- **ChannelController**: Manage channel creation, deletion, and configuration within servers.
- **ChatController**: Handle message sending, history retrieval, and broadcast logic.
- **Authorization**: Move permission verification logic from Routes/Repositories into the Controllers.
- **DTO Transformation**: Controllers must map incoming request objects to domain models and outgoing domain models to DTOs.

## Non-Functional Requirements
- **Error Handling**: 
    - Use a `Result` type or `Sealed Class` for expected business logic errors (e.g., `InsufficientPermissions`, `ChannelNotFound`).
    - Use standard Exceptions for unexpected system failures (e.g., database connection issues), to be caught by `StatusPages`.
- **Testability**: Controllers must be easily unit-testable in isolation by mocking Repositories.
- **Consistency**: All existing routes must be refactored to delegate logic to these controllers.

## Acceptance Criteria
- [ ] `ServerController`, `RoleController`, `ChannelController`, and `ChatController` classes are implemented.
- [ ] Routes in `ServerRoutes`, `MessageRoutes`, etc., contain minimal logic (Authentication + delegation to Controller).
- [ ] Authorization checks are successfully integrated into the Controller layer.
- [ ] Unit tests for all controllers achieve >80% coverage.
- [ ] All existing features (Login, Server/Channel creation, Messaging) function correctly after refactoring.

## Out of Scope
- Major changes to the Database schema.
- Frontend UI changes.
