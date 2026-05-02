package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.Server
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerApiClientTest {
    @Test
    fun testCreateServer() = runTest {
        val mockServer = Server(
            id = "test-id",
            name = "Test Server",
            ownerId = "owner-id",
            icon = "http://icon.com"
        )
        
        val mockEngine = MockEngine { request ->
            assertEquals("localhost", request.url.host)
            assertEquals("/api/servers", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            
            // Verify body
            val body = (request.body as? io.ktor.http.content.TextContent)?.text
            val expectedBody = Json.encodeToString(
                website.woodendoor.conflux.models.CreateServerRequest(
                    name = "Test Server",
                    iconUrl = "http://icon.com",
                    ownerId = "owner-id"
                )
            )
            assertEquals(expectedBody, body)
            
            respond(
                content = ByteReadChannel(Json.encodeToString(mockServer)),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.createServer("Test Server", "http://icon.com", "owner-id")
        
        assertEquals(mockServer, result)
    }

    @Test
    fun testCreateChannel() = runTest {
        val mockChannel = website.woodendoor.conflux.models.Channel(
            id = "chan-id",
            serverId = "server-id",
            name = "general",
            type = website.woodendoor.conflux.models.ChannelType.TEXT
        )

        val mockEngine = MockEngine { request ->
            assertEquals("localhost", request.url.host)
            assertEquals("/api/servers/server-id/channels", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockChannel)),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.createChannel("server-id", "general")

        assertEquals(mockChannel, result)
    }

    @Test
    fun testLogin() = runTest {
        val mockUser = website.woodendoor.conflux.models.User(
            id = "user-id",
            username = "testuser",
            discriminator = "1234"
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/api/login", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockUser)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.login("testuser")

        assertEquals(mockUser, result)
    }

    @Test
    fun testGetServers() = runTest {
        val mockServers = listOf(
            Server(id = "s1", name = "Server 1", ownerId = "u1"),
            Server(id = "s2", name = "Server 2", ownerId = "u1")
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/api/servers", request.url.encodedPath)
            assertEquals("u1", request.url.parameters["userId"])
            assertEquals(HttpMethod.Get, request.method)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockServers)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.getServers("u1")

        assertEquals(mockServers, result)
    }

    @Test
    fun testGetChannels() = runTest {
        val mockChannels = listOf(
            website.woodendoor.conflux.models.Channel(id = "c1", serverId = "s1", name = "Channel 1", type = website.woodendoor.conflux.models.ChannelType.TEXT),
            website.woodendoor.conflux.models.Channel(id = "c2", serverId = "s1", name = "Channel 2", type = website.woodendoor.conflux.models.ChannelType.TEXT)
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/api/servers/s1/channels", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockChannels)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.getChannels("s1")

        assertEquals(mockChannels, result)
    }

    @Test
    fun testSendMessage() = runTest {
        val mockMessage = website.woodendoor.conflux.models.Message(
            id = "msg-id",
            channelId = "chan-id",
            authorId = "user-id",
            content = "Hello",
            timestamp = 123456789L
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/api/channels/chan-id/messages", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            
            val body = (request.body as? io.ktor.http.content.TextContent)?.text
            val expectedBody = Json.encodeToString(
                website.woodendoor.conflux.models.SendMessageRequest(
                    senderId = "user-id",
                    content = "Hello"
                )
            )
            assertEquals(expectedBody, body)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockMessage)),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.sendMessage("chan-id", "user-id", "Hello")

        assertEquals(mockMessage, result)
    }

    @Test
    fun testGetMessages() = runTest {
        val mockMessages = listOf(
            website.woodendoor.conflux.models.Message(id = "m1", channelId = "c1", authorId = "u1", content = "Msg 1", timestamp = 1L),
            website.woodendoor.conflux.models.Message(id = "m2", channelId = "c1", authorId = "u2", content = "Msg 2", timestamp = 2L)
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/api/channels/c1/messages", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)

            respond(
                content = ByteReadChannel(Json.encodeToString(mockMessages)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.getMessages("c1")

        assertEquals(mockMessages, result)
    }
}
