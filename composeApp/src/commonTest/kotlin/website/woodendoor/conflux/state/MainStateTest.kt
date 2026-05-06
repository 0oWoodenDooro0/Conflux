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

    @Test
    fun testHandlePermissionUpdateEvent() = runTest {
        val server = Server("s1", "Server 1", "u1")
        MainState.selectedServer = server
        MainState.currentUserId = "u1"
        MainState.currentUserPermissions = 0L

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("/permissions")) {
                respond(
                    content = ByteReadChannel("7"), // 0b111
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

        MainState.handleWebSocketEvent(ConfluxEvent.PermissionUpdate("s1"), apiClient)

        assertEquals(7L, MainState.currentUserPermissions)
    }
}
