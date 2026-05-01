# Specification: Database Infrastructure & Core Repositories

## Overview
Implement the persistence layer for Conflux using JetBrains Exposed 1.0 and H2. This track focuses on setting up the database infrastructure and implementing repositories for the core identity and structural models.

## Functional Requirements
- **Dependency Integration**: Add Exposed 1.0 and H2 dependencies to the project.
- **Database Configuration**: Set up an in-memory H2 database connection in the `server` module.
- **Schema Implementation**: Define Exposed Tables for the following models:
    - `User` (id, username, discriminator, avatar)
    - `Server` (id, name, ownerId)
    - `Role` (id, serverId, name, permissions, color)
    - `Channel` (id, serverId, name, type)
- **Repository Pattern**: Implement CRUD repositories in the `server` module:
    - `UserRepository`
    - `ServerRepository`
    - `ChannelRepository`
- **Initialization**: Implement logic to initialize the database schema on server startup.

## Non-Functional Requirements
- **Module Isolation**: All database-specific code and dependencies remain strictly within the `server` module.
- **Testing**: Follow TDD for repository implementations using JUnit.
- **Concurrency**: Use appropriate thread-safe handling for database transactions.

## Acceptance Criteria
- [ ] Database schema is automatically created on server start.
- [ ] `UserRepository` successfully persists and retrieves `User` data.
- [ ] `ServerRepository` successfully persists and retrieves `Server` data (including associations if applicable).
- [ ] `ChannelRepository` successfully persists and retrieves `Channel` data.
- [ ] All repository tests pass in the `server` module.

## Out of Scope
- Message persistence (Communication models).
- Complex database migrations (e.g., Flyway/Liquibase).
- Client-side persistence logic.
