# Technology Stack

## Core Language
- **Kotlin**: Used for both shared logic, server implementation, and UI development.

## Multiplatform & UI
- **Kotlin Multiplatform (KMP)**: Facilitates sharing logic across Web, JVM, and Server.
- **Compose Multiplatform**: Used for building the shared UI components.
    - **Targets**: Web (WasmJs/JS) and Desktop (JVM).

## Backend
- **Ktor**: Used as the server-side framework for handling client connections and messaging.
- **Coroutines**: Leveraged for high-concurrency message synchronization and async I/O.

## Build & Tooling
- **Gradle (Kotlin DSL)**: Unified build system for all modules.
- **Version Catalog**: Centralized dependency management via `libs.versions.toml`.
