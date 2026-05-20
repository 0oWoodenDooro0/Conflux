# Channel Notification Red Dot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement real-time, lightweight channel read-state tracking and unread indicators (notification red dots) for channels and servers in Conflux.

**Architecture:** 
1. Add `serverId` to the `ConfluxEvent.NewMessage` event so the client instantly knows which server an unread message belongs to.
2. Broadcast the `NewMessage` event to both channel subscribers and all server subscribers who have the `VIEW_CHANNEL` permission.
3. Maintain reactive sets of `unreadChannels` and `unreadServerIds` in `MainState`, updating them on `NewMessage` and clearing them on `selectChannel`.
4. Render primary-colored dots next to unread channels and error-colored dots next to unread servers in Compose UI.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor WebSockets.

---

### Task 1: Update the Shared Models & Serialization Tests

**Files:**
- Modify: `shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ConfluxEvent.kt`
- Modify: `shared/src/commonTest/kotlin/website/woodendoor/conflux/models/ConfluxEventSerializationTest.kt`

- [ ] **Step 1.1: Modify `ConfluxEvent.NewMessage` class signature**
  Update the `NewMessage` declaration in `shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ConfluxEvent.kt` to accept a `serverId` parameter:
  ```kotlin
  @Serializable
  data class NewMessage(val message: Message, val serverId: String) : ConfluxEvent()
  ```

- [ ] **Step 1.2: Update the serialization test in `ConfluxEventSerializationTest.kt`**
  Update `shared/src/commonTest/kotlin/website/woodendoor/conflux/models/ConfluxEventSerializationTest.kt` around line 26:
  ```kotlin
  val event: ConfluxEvent = ConfluxEvent.NewMessage(message, "server-1")
  ```
  And add assertion check around line 35:
  ```kotlin
  assertEquals("server-1", decoded.serverId)
  ```

- [ ] **Step 1.3: Run the shared module serialization tests**
  Run: `./gradlew :shared:test`
  Expected: PASS

- [ ] **Step 1.4: Commit**
  ```bash
  git add shared/src/commonMain/kotlin/website/woodendoor/conflux/models/ConfluxEvent.kt shared/src/commonTest/kotlin/website/woodendoor/conflux/models/ConfluxEventSerializationTest.kt
  git commit -m "feat: add serverId to ConfluxEvent.NewMessage and update serialization test"
  ```

---

### Task 2: Implement Backend Message Broadcasting Logic

**Files:**
- Modify: `server/src/main/kotlin/website/woodendoor/conflux/controller/ChatController.kt`

- [ ] **Step 2.1: Update `broadcastMessage` to send messages to both channel and server subscribers**
  Modify `server/src/main/kotlin/website/woodendoor/conflux/controller/ChatController.kt` line 59-75:
  ```kotlin
  private suspend fun broadcastMessage(channelId: String, message: Message) {
      val channel = channelRepository.getChannel(channelId) ?: return
      val serverId = channel.serverId
      
      val event = ConfluxEvent.NewMessage(message, serverId)
      val eventJson = Json.encodeToString<ConfluxEvent>(event)
      
      val sessionsToSend = mutableSetOf<DefaultWebSocketServerSession>()
      
      // 1. Send to all channel subscribers
      sessionsToSend.addAll(connectionManager.getConnectionsForChannel(channelId))
      
      // 2. Send to all server subscribers who have VIEW_CHANNEL permission
      val serverSubscribers = connectionManager.getServerSubscribers(serverId)
      serverSubscribers.forEach { userId ->
          val perms = channelRepository.getEffectivePermissions(serverId, channelId, userId)
          if (ConfluxPermission.hasPermission(perms, ConfluxPermission.VIEW_CHANNEL)) {
              sessionsToSend.addAll(connectionManager.getUserSessions(userId))
          }
      }
      
      coroutineScope {
          sessionsToSend.forEach { session ->
              launch {
                  try {
                      session.send(Frame.Text(eventJson))
                  } catch (e: Exception) {
                      // Session might be closed
                  }
              }
          }
      }
  }
  ```

- [ ] **Step 2.2: Run backend tests to verify broadcasting works**
  Run: `./gradlew :server:test`
  Expected: PASS

- [ ] **Step 2.3: Commit**
  ```bash
  git add server/src/main/kotlin/website/woodendoor/conflux/controller/ChatController.kt
  git commit -m "feat: broadcast NewMessage to all server subscribers with view permissions"
  ```

---

### Task 3: Implement Client-Side State Management in `MainState`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt`

- [ ] **Step 3.1: Add `unreadChannels` and `unreadServerIds` properties to `MainState`**
  Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt` around line 25:
  ```kotlin
  var unreadChannels by mutableStateOf<Set<String>>(emptySet())
  var unreadServerIds by mutableStateOf<Set<String>>(emptySet())
  ```

- [ ] **Step 3.2: Update WebSocket event handling for `NewMessage` in `MainState.kt`**
  Modify `handleWebSocketEvent` around line 74:
  ```kotlin
          is ConfluxEvent.NewMessage -> {
              val isCurrentChannel = event.message.channelId == selectedChannel?.id
              if (isCurrentChannel) {
                  // Avoid duplicates
                  if (messages.none { it.id == event.message.id }) {
                      messages = messages + event.message
                  }
              } else {
                  if (event.message.authorId != currentUserId) {
                      unreadChannels = unreadChannels + event.message.channelId
                      unreadServerIds = unreadServerIds + event.serverId
                  }
              }
          }
  ```

- [ ] **Step 3.3: Update `selectChannel` to clear notifications**
  Modify `selectChannel` around line 172:
  ```kotlin
      suspend fun selectChannel(channel: Channel, apiClient: ServerApiClient) {
          selectedChannel = channel
          messages = emptyList()
          messageFetchError = null
          currentChannelPermissions = 0L
          
          // Clear notification
          unreadChannels = unreadChannels - channel.id
          selectedServer?.let { server ->
              val hasAnyUnreadOnServer = channelList.any { it.id in unreadChannels }
              if (!hasAnyUnreadOnServer) {
                  unreadServerIds = unreadServerIds - server.id
              }
          }
          
          try {
              messages = apiClient.getMessages(channel.id)
              webSocketClient?.subscribe(channel.id)
  
              // Fetch hierarchical permissions for the channel
              selectedServer?.let { server ->
                  currentUserId?.let { userId ->
                      currentChannelPermissions = apiClient.getChannelPermissions(server.id, channel.id, userId)
                  }
              }
          } catch (e: Exception) {
              messageFetchError = e.message ?: "Unknown error"
          }
      }
  ```

- [ ] **Step 3.4: Add server subscription helper and reset state**
  Add helper method `subscribeToAllServers` to `MainState`:
  ```kotlin
      fun subscribeToAllServers() {
          val ws = webSocketClient ?: return
          scope.launch {
              serverList.forEach { server ->
                  ws.subscribeServer(server.id)
              }
          }
      }
  ```
  And update `reset()` around line 226:
  ```kotlin
      fun reset() {
          serverList = emptyList()
          selectedServer = null
          channelList = emptyList()
          channelFetchError = null
          selectedChannel = null
          messages = emptyList()
          messageFetchError = null
          messageSendError = null
          unreadChannels = emptySet()
          unreadServerIds = emptySet()
          webSocketClient?.close()
          webSocketClient = null
      }
  ```

- [ ] **Step 3.5: Run existing client-side tests**
  Run: `./gradlew :composeApp:test`
  Expected: PASS

- [ ] **Step 3.6: Commit**
  ```bash
  git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/state/MainState.kt
  git commit -m "feat: track and clear unread channels and server IDs in MainState"
  ```

---

### Task 4: Subscribe to Servers on Load and Update UI Components

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/MainScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSidebar.kt`
- Modify: `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSidebar.kt`

- [ ] **Step 4.1: Add server subscription side-effect to `MainScreen.kt`**
  Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/MainScreen.kt` around line 77 (before Scaffold):
  ```kotlin
      LaunchedEffect(servers) {
          MainState.subscribeToAllServers()
      }
  ```

- [ ] **Step 4.2: Update ChannelSidebar to render unread notification dots**
  Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSidebar.kt` to update `ChannelSidebar` parameter list, add `unreadChannels: Set<String> = emptySet()`, and update calling of `ChannelItem`:
  ```kotlin
  @Composable
  fun ChannelSidebar(
      serverName: String,
      channels: List<Channel>,
      selectedChannelId: String? = null,
      unreadChannels: Set<String> = emptySet(), // Added
      canCreateChannel: Boolean = false,
      canManageServer: Boolean = false,
      onCreateChannelClick: () -> Unit,
      onSettingsClick: () -> Unit,
      onChannelClick: (Channel) -> Unit,
      onChannelSettingsClick: (Channel) -> Unit = {}
  ) {
  // ...
                      ChannelItem(
                          channel = channel,
                          isSelected = channel.id == selectedChannelId,
                          hasUnread = channel.id in unreadChannels, // Added
                          canManage = channel.canManage,
                          onClick = { onChannelClick(channel) },
                          onSettingsClick = { onChannelSettingsClick(channel) }
                      )
  // ...
  ```
  And update `ChannelItem` declaration around line 95 to style the text and add the primary-colored dot:
  ```kotlin
  @Composable
  fun ChannelItem(
      channel: Channel,
      isSelected: Boolean,
      hasUnread: Boolean, // Added
      canManage: Boolean = false,
      onClick: () -> Unit,
      onSettingsClick: () -> Unit = {}
  ) {
      Surface(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
          color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
          shape = RoundedCornerShape(4.dp)
      ) {
          Row(
              modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
          ) {
              Text(
                  text = "#",
                  style = MaterialTheme.typography.bodyLarge,
                  color = if (isSelected) MaterialTheme.colorScheme.primary 
                          else if (hasUnread) MaterialTheme.colorScheme.onSurface 
                          else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text = channel.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected || hasUnread) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer 
                          else if (hasUnread) MaterialTheme.colorScheme.onSurface 
                          else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.weight(1f)
              )
              if (hasUnread) {
                  Box(
                      modifier = Modifier
                          .padding(horizontal = 4.dp)
                          .size(8.dp)
                          .background(MaterialTheme.colorScheme.primary, CircleShape)
                  )
              }
              if (canManage && isSelected) {
                  IconButton(
                      onClick = onSettingsClick,
                      modifier = Modifier.size(24.dp)
                  ) {
                      Icon(
                          Icons.Default.Settings,
                          contentDescription = "Channel Settings",
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                  }
              }
          }
      }
  }
  ```

- [ ] **Step 4.3: Update ServerSidebar to render notification dots**
  Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSidebar.kt` to add `unreadServerIds: Set<String> = emptySet()` parameter:
  ```kotlin
  @Composable
  fun ServerSidebar(
      servers: List<Server>,
      unreadServerIds: Set<String> = emptySet(), // Added
      onServerClick: (Server) -> Unit,
      onHomeClick: () -> Unit,
      onCreateServerClick: () -> Unit,
      onJoinServerClick: () -> Unit
  ) {
  ```
  And wrap the list's `ServerIcon` inside `ServerSidebar` around line 65:
  ```kotlin
                      Box(modifier = Modifier.padding(vertical = 4.dp)) {
                          ServerIcon(
                              name = server.name.take(1).uppercase(),
                              onClick = { onServerClick(server) }
                          )
                          if (server.id in unreadServerIds) {
                              Box(
                                  modifier = Modifier
                                      .size(10.dp)
                                      .align(Alignment.TopEnd)
                                      .background(MaterialTheme.colorScheme.error, CircleShape)
                              )
                          }
                      }
  ```

- [ ] **Step 4.4: Pass notifications state in `MainScreen.kt`**
  Modify `composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/MainScreen.kt` around line 82 & 107 to pass these properties:
  ```kotlin
              ServerSidebar(
                  servers = servers,
                  unreadServerIds = MainState.unreadServerIds, // Added
                  onServerClick = { server ->
  // ...
              if (selectedServer != null && !isCreatingServer && !isJoiningServer) {
                  ChannelSidebar(
                      serverName = selectedServer.name,
                      channels = channels,
                      selectedChannelId = MainState.selectedChannel?.id,
                      unreadChannels = MainState.unreadChannels, // Added
                      canCreateChannel = canManageChannelsServerWide,
  ```

- [ ] **Step 4.5: Commit**
  ```bash
  git add composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/MainScreen.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ChannelSidebar.kt composeApp/src/commonMain/kotlin/website/woodendoor/conflux/ui/ServerSidebar.kt
  git commit -m "ui: render notification dots on unread channels and server icons in sidebars"
  ```

---

### Task 5: Add Unit Tests and Verify the Feature

**Files:**
- Modify: `composeApp/src/commonTest/kotlin/website/woodendoor/conflux/state/MainStateTest.kt`

- [ ] **Step 5.1: Add unit tests to `MainStateTest.kt`**
  Add these tests to the end of `composeApp/src/commonTest/kotlin/website/woodendoor/conflux/state/MainStateTest.kt`:
  ```kotlin
      @Test
      fun testNewMessageEventAddsToUnreadChannelsWhenNotSelected() = runTest {
          val originalChannel = Channel("c1", "s1", "general", ChannelType.TEXT)
          MainState.channelList = listOf(originalChannel)
          MainState.selectedChannel = Channel("c2", "s1", "other", ChannelType.TEXT)
          MainState.currentUserId = "u1"
  
          val message = Message("m1", "c1", "u2", "Hello", 1L)
          val event = ConfluxEvent.NewMessage(message, "s1")
          
          val mockEngine = MockEngine { request -> respond(content = ByteReadChannel(""), status = HttpStatusCode.OK) }
          val apiClient = ServerApiClient(HttpClient(mockEngine) { install(ContentNegotiation) { json() } }, "http://localhost")
  
          MainState.handleWebSocketEvent(event, apiClient)
  
          assertTrue(MainState.unreadChannels.contains("c1"))
          assertTrue(MainState.unreadServerIds.contains("s1"))
      }
  
      @Test
      fun testNewMessageEventDoesNotAddToUnreadChannelsWhenSelected() = runTest {
          val originalChannel = Channel("c1", "s1", "general", ChannelType.TEXT)
          MainState.channelList = listOf(originalChannel)
          MainState.selectedChannel = originalChannel
          MainState.currentUserId = "u1"
  
          val message = Message("m1", "c1", "u2", "Hello", 1L)
          val event = ConfluxEvent.NewMessage(message, "s1")
          
          val mockEngine = MockEngine { request -> respond(content = ByteReadChannel(""), status = HttpStatusCode.OK) }
          val apiClient = ServerApiClient(HttpClient(mockEngine) { install(ContentNegotiation) { json() } }, "http://localhost")
  
          MainState.handleWebSocketEvent(event, apiClient)
  
          assertFalse(MainState.unreadChannels.contains("c1"))
          assertFalse(MainState.unreadServerIds.contains("s1"))
      }
  
      @Test
      fun testSelectChannelClearsUnreadState() = runTest {
          val channel1 = Channel("c1", "s1", "general", ChannelType.TEXT)
          val channel2 = Channel("c2", "s1", "other", ChannelType.TEXT)
          MainState.channelList = listOf(channel1, channel2)
          MainState.selectedServer = Server("s1", "Server 1", "u1")
          
          MainState.unreadChannels = setOf("c1", "c2")
          MainState.unreadServerIds = setOf("s1")
  
          val mockEngine = MockEngine { request ->
              if (request.url.encodedPath.contains("/messages")) {
                  respond(content = ByteReadChannel("[]"), status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "application/json"))
              } else {
                  respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
              }
          }
          val apiClient = ServerApiClient(HttpClient(mockEngine) { install(ContentNegotiation) { json() } }, "http://localhost")
  
          MainState.selectChannel(channel1, apiClient)
  
          assertFalse(MainState.unreadChannels.contains("c1"))
          assertTrue(MainState.unreadChannels.contains("c2"))
          assertTrue(MainState.unreadServerIds.contains("s1")) // Still has c2 unread
          
          MainState.selectChannel(channel2, apiClient)
          
          assertFalse(MainState.unreadChannels.contains("c2"))
          assertFalse(MainState.unreadServerIds.contains("s1")) // All cleared
      }
  ```

- [ ] **Step 5.2: Run all workspace tests**
  Run: `./gradlew test`
  Expected: PASS

- [ ] **Step 5.3: Commit**
  ```bash
  git add composeApp/src/commonTest/kotlin/website/woodendoor/conflux/state/MainStateTest.kt
  git commit -m "test: add comprehensive unit tests for unread channel and server tracking"
  ```
