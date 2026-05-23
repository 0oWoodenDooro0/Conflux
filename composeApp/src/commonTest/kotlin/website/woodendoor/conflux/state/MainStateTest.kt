package website.woodendoor.conflux.state

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.Message
import website.woodendoor.conflux.models.ConfluxEvent
import kotlin.test.*

class MainStateTest {

    @BeforeTest
    fun setup() {
        MainState.reset()
    }

    @Test
    fun testHandleChannelUpdatedEvent() = runTest {
        val originalChannel = Channel("c1", "s1", "general", ChannelType.TEXT)
        MainState.channelList = listOf(originalChannel)
        MainState.selectedChannel = originalChannel

        val updatedChannel = originalChannel.copy(name = "renamed-general")
        val mockEngine = MockEngine { request -> respond(content = ByteReadChannel(""), status = HttpStatusCode.OK) }
        val apiClient = ServerApiClient(HttpClient(mockEngine) { install(ContentNegotiation) { json() } }, "http://localhost")

        MainState.handleWebSocketEvent(ConfluxEvent.ChannelUpdated(updatedChannel), apiClient)

        assertEquals("renamed-general", MainState.channelList.first().name)
        assertEquals("renamed-general", MainState.selectedChannel?.name)
    }

    @Test
    fun testHandleChannelDeletedEvent() = runTest {
        val c1 = Channel("c1", "s1", "general", ChannelType.TEXT)
        val c2 = Channel("c2", "s1", "other", ChannelType.TEXT)
        MainState.channelList = listOf(c1, c2)
        MainState.selectedChannel = c1

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("/messages")) {
                respond(content = ByteReadChannel("[]"), status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
            }
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) { install(ContentNegotiation) { json() } }, "http://localhost")

        MainState.handleWebSocketEvent(ConfluxEvent.ChannelDeleted("c1", "s1"), apiClient)

        assertEquals(1, MainState.channelList.size)
        assertEquals("c2", MainState.channelList.first().id)
        assertEquals("c2", MainState.selectedChannel?.id, "Should redirect to the next available channel")
    }

    @Test
    fun testInitialState() {
        assertNull(MainState.selectedServer)
        assertTrue(MainState.channelList.isEmpty())
    }

    @Test
    fun testSelectServer() {
        val server = Server("s1", "Server 1", "u1")
        MainState.selectedServer = server
        assertEquals(server, MainState.selectedServer)
    }

    @Test
    fun testSetChannels() {
        val channels = listOf(
            Channel("c1", "s1", "general", ChannelType.TEXT)
        )
        MainState.channelList = channels
        assertEquals(channels, MainState.channelList)
    }

    @Test
    fun testErrorState() {
        assertNull(MainState.channelFetchError)
        MainState.channelFetchError = "Error"
        assertEquals("Error", MainState.channelFetchError)
    }

    @Test
    fun testSelectServerAction() = runTest {
        val server = Server("s1", "Server 1", "u1")
        val channels = listOf(
            Channel("c1", "s1", "general", ChannelType.TEXT)
        )
        MainState.currentUserId = "u1"

        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/channels") -> {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(channels)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.endsWith("/members") -> {
                    respond(
                        content = ByteReadChannel("[]"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.endsWith("/permissions") -> {
                    respond(
                        content = ByteReadChannel("7"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
            }
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.selectServer(server, apiClient)

        assertEquals(server, MainState.selectedServer)
        assertEquals(channels, MainState.channelList)
        assertEquals(7L, MainState.currentUserPermissions)
        assertNull(MainState.channelFetchError)
    }

    @Test
    fun testSelectServerActionError() = runTest {
        val server = Server("s1", "Server 1", "u1")
        MainState.currentUserId = "u1"

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Error"),
                status = HttpStatusCode.InternalServerError
            )
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.selectServer(server, apiClient)

        assertEquals(server, MainState.selectedServer)
        assertTrue(MainState.channelList.isEmpty())
        assertNotNull(MainState.channelFetchError)
    }

    @Test
    fun testSelectChannel() = runTest {
        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        val messages = listOf(
            Message("m1", "c1", "u1", "Hello", 1L)
        )

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("/messages")) {
                respond(
                    content = ByteReadChannel(Json.encodeToString(messages)),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
            }
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.selectChannel(channel, apiClient)

        assertEquals(channel, MainState.selectedChannel)
        assertEquals(messages, MainState.messages)
        assertNull(MainState.messageFetchError)
    }

    @Test
    fun testSendMessage() = runTest {
        val channel = Channel("c1", "s1", "general", ChannelType.TEXT)
        MainState.selectedChannel = channel
        val newMessage = Message("m2", "c1", "u1", "New Message", 2L)

        val mockEngine = MockEngine { request ->
            if (request.method == HttpMethod.Post) {
                respond(
                    content = ByteReadChannel(Json.encodeToString(newMessage)),
                    status = HttpStatusCode.Created,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                // GET messages (after send)
                respond(
                    content = ByteReadChannel(Json.encodeToString(listOf(newMessage))),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.sendMessage("u1", "New Message", apiClient)

        assertEquals(listOf(newMessage), MainState.messages)
        assertNull(MainState.messageSendError)
    }

    private fun createMockApiClient(
        channels: List<Channel> = emptyList(),
        messages: List<Message> = emptyList(),
        permissions: Long = 0L
    ): ServerApiClient {
        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/channels") -> {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(channels)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/messages") -> {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(messages)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/permissions") -> {
                    respond(
                        content = ByteReadChannel(permissions.toString()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/members") -> {
                    respond(
                        content = ByteReadChannel("[]"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
            }
        }
        return ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")
    }

    @Test
    fun testHandlePermissionUpdateEvent() = runTest {
        val server = Server("s1", "Server 1", "u1")
        MainState.selectedServer = server
        MainState.currentUserId = "u1"
        MainState.currentUserPermissions = 0L

        val apiClient = createMockApiClient(permissions = 7L)

        MainState.handleWebSocketEvent(ConfluxEvent.PermissionUpdate("s1"), apiClient)

        assertEquals(7L, MainState.currentUserPermissions)
    }

    @Test
    fun testNewMessageEventUnreadTracking() = runTest {
        MainState.currentUserId = "current-user"
        val activeChannel = Channel("c1", "s1", "general", ChannelType.TEXT)
        MainState.selectedChannel = activeChannel

        val apiClient = createMockApiClient()

        // 1. Message in current channel -> should not track as unread
        val msg1 = Message("m1", "c1", "other-user", "Hello", 1L)
        MainState.handleWebSocketEvent(ConfluxEvent.NewMessage(msg1, "s1"), apiClient)
        assertTrue(MainState.unreadChannels.isEmpty())
        assertTrue(MainState.unreadServerIds.isEmpty())

        // 2. Message in other channel by other user -> should track as unread
        val msg2 = Message("m2", "c2", "other-user", "Hi", 2L)
        MainState.handleWebSocketEvent(ConfluxEvent.NewMessage(msg2, "s1"), apiClient)
        assertEquals(setOf("c2"), MainState.unreadChannels)
        assertEquals(setOf("s1"), MainState.unreadServerIds)

        // 3. Message in other channel by current user -> should not track as unread
        val msg3 = Message("m3", "c3", "current-user", "My message", 3L)
        MainState.handleWebSocketEvent(ConfluxEvent.NewMessage(msg3, "s1"), apiClient)
        // unread channels should still only contain c2
        assertEquals(setOf("c2"), MainState.unreadChannels)
    }

    @Test
    fun testSelectChannelClearsUnread() = runTest {
        val server = Server("s1", "Server 1", "u1")
        val c1 = Channel("c1", "s1", "general", ChannelType.TEXT)
        val c2 = Channel("c2", "s1", "random", ChannelType.TEXT)
        MainState.selectedServer = server
        MainState.channelList = listOf(c1, c2)
        
        MainState.unreadChannels = setOf("c1", "c2")
        MainState.unreadServerIds = setOf("s1")

        val apiClient = createMockApiClient()

        // Select c1 -> should clear c1 from unreads, but server s1 is still unread because c2 is unread
        MainState.selectChannel(c1, apiClient)
        assertEquals(setOf("c2"), MainState.unreadChannels)
        assertEquals(setOf("s1"), MainState.unreadServerIds)

        // Select c2 -> should clear c2, and since no more unreads remain on s1, s1 should clear too
        MainState.selectChannel(c2, apiClient)
        assertTrue(MainState.unreadChannels.isEmpty())
        assertTrue(MainState.unreadServerIds.isEmpty())
    }

    @Test
    fun testChannelDeletedClearsUnread() = runTest {
        val server = Server("s1", "Server 1", "u1")
        val c1 = Channel("c1", "s1", "general", ChannelType.TEXT)
        MainState.selectedServer = server
        MainState.channelList = listOf(c1)
        
        MainState.unreadChannels = setOf("c1")
        MainState.unreadServerIds = setOf("s1")

        val apiClient = createMockApiClient()

        MainState.handleWebSocketEvent(ConfluxEvent.ChannelDeleted("c1", "s1"), apiClient)
        assertTrue(MainState.unreadChannels.isEmpty())
        assertTrue(MainState.unreadServerIds.isEmpty())
    }

    @Test
    fun testPermissionUpdateClearsUnread() = runTest {
        val server = Server("s1", "Server 1", "u1")
        val c1 = Channel("c1", "s1", "general", ChannelType.TEXT)
        val c2 = Channel("c2", "s1", "random", ChannelType.TEXT)
        MainState.selectedServer = server
        MainState.currentUserId = "u1"
        MainState.channelList = listOf(c1, c2)

        MainState.unreadChannels = setOf("c1", "c2")
        MainState.unreadServerIds = setOf("s1")

        // In permission update, user only has access to c2 (c1 is removed)
        val apiClient = createMockApiClient(
            channels = listOf(c2),
            permissions = 7L
        )

        MainState.handleWebSocketEvent(ConfluxEvent.PermissionUpdate("s1"), apiClient)

        // c1 should be cleared because user no longer has permission to view it
        assertEquals(setOf("c2"), MainState.unreadChannels)
        assertEquals(setOf("s1"), MainState.unreadServerIds)
    }

    @Test
    fun testResetClearsUnread() {
        MainState.unreadChannels = setOf("c1")
        MainState.unreadServerIds = setOf("s1")
        
        MainState.reset()
        
        assertTrue(MainState.unreadChannels.isEmpty())
        assertTrue(MainState.unreadServerIds.isEmpty())
    }
}
