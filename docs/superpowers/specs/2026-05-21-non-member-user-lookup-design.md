# Non-Member User Lookup & Caching Design Spec

Ensure usernames are correctly displayed in the chat interface even if the sender has left the server or is not in the server's cached member list.

## Goal

- Provide a server-side endpoint `GET /api/users/{userId}` to retrieve any registered user's profile.
- Implement the client-side API method in `ServerApiClient` to query user details by ID.
- Upgrade `ChatRoom.kt` to dynamically fetch, resolve, and cache names of users not present in the active server members list.
- Keep the user experience smooth and performant through non-blocking background fetching and memory caching.

## Proposed Changes

### Server-Side Changes (`server`)

#### [MODIFY] [UserController.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/controller/UserController.kt)
Add a function `getUser` to retrieve a user profile from the database:
```kotlin
suspend fun getUser(id: String): OperationResult<User> {
    val user = userService.getUser(id)
    return if (user != null) {
        OperationResult.Success(user)
    } else {
        OperationResult.Failure.NotFound("User with ID $id not found")
    }
}
```

#### [MODIFY] [UserRoutes.kt](file:///home/user/IdeaProjects/Conflux/server/src/main/kotlin/website/woodendoor/conflux/routes/UserRoutes.kt)
Register the `GET /api/users/{userId}` route:
```kotlin
get("/api/users/{userId}") {
    val userId = call.parameters["userId"]
    if (userId == null) {
        call.respond(HttpStatusCode.BadRequest, "Missing userId")
        return@get
    }
    val result = userController.getUser(userId)
    call.respond(result, HttpStatusCode.OK)
}
```

---

### Client-Side Changes (`shared` & `composeApp`)

#### [MODIFY] [ServerApiClient.kt](file:///home/user/IdeaProjects/Conflux/shared/src/commonMain/kotlin/website/woodendoor/conflux/api/ServerApiClient.kt)
Implement the `getUser` API function:
```kotlin
suspend fun getUser(userId: String): User {
    return client.get("$baseUrl/api/users/$userId").body()
}
```

#### [MODIFY] [ChatRoom.kt](file:///home/user/IdeaProjects/Conflux/composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChatRoom.kt)
- Introduce a memory cache map `fetchedUsers: Map<String, User>` and pending tracking list `pendingFetches: List<String>`.
- Read and combine cached members and individually fetched users: `val user = members.find { it.id == message.authorId } ?: fetchedUsers[message.authorId]`.
- Add a Compose `LaunchedEffect(messages, members)` to scan for unknown authors in background coroutines, fetch profiles asynchronously, and dynamically populate `fetchedUsers` to trigger UI updates seamlessly.

---

## Verification Plan

### Automated Tests
- Run gradle compilation and existing tests to ensure no regressions:
  ```bash
  ./gradlew check
  ```
