# Technology Stack

## Core Language
- **Kotlin**: Used for both shared logic, server implementation, and UI development.

## Multiplatform & UI
- **Kotlin Multiplatform (KMP)**: Facilitates sharing logic across Web, JVM, and Server.
- **Compose Multiplatform**: Used for building the shared UI components, including Material3 and Material Icons.
    - **Targets**: Web (WasmJs/JS) and Desktop (JVM).

## Backend
- **Ktor**: Used as the server-side framework (with WebSockets) and as the client-side library (Ktor Client) for KMP.
- **Kotlinx Serialization**: Standard library for JSON serialization across backend, shared, and frontend modules.
- **Coroutines**: Leveraged for high-concurrency message synchronization and async I/O.
- **JetBrains Exposed**: Type-safe SQL framework for database access.
- **H2 Database**: In-memory database used for initial development and testing.

## Build & Tooling
- **Gradle (Kotlin DSL)**: Unified build system for all modules.
- **Version Catalog**: Centralized dependency management via `libs.versions.toml`.
