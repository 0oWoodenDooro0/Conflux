package website.woodendoor.conflux

import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import website.woodendoor.conflux.models.ConfluxEvent
import kotlin.test.*

import kotlinx.coroutines.runBlocking
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.repositories.ExposedChannelRepository
import website.woodendoor.conflux.database.repositories.ExposedServerRepository
import website.woodendoor.conflux.database.repositories.ExposedUserRepository
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User

class WebSocketRouteTest {

    @Test
    fun testWebSocketConnectionRequiresToken() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/ws") {
            val reason = closeReason.await()
            assertEquals(CloseReason.Codes.VIOLATED_POLICY.code, reason?.code)
        }
    }

    @Test
    fun testWebSocketConnectionWithValidToken() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        // Force application startup to create database schema
        client.get("/health")

        // Initialize DB and insert required test data
        DatabaseFactory.init()
        val userRepo = ExposedUserRepository()
        val serverRepo = ExposedServerRepository(userRepo)
        val channelRepo = ExposedChannelRepository()

        val user = User("user-123", "testuser", "0001")
        val server = Server("server-123", "Test Server", "user-123")
        val channel = Channel("channel-123", "server-123", "general", ChannelType.TEXT)

        runBlocking {
            userRepo.createUser(user)
            serverRepo.createServer(server)
            channelRepo.createChannel(channel)
        }

        // 1. Get a token
        val tokenResponse = client.post("/api/auth/ws-token") {
            setBody("user-123")
        }
        val tokenJson = Json.parseToJsonElement(tokenResponse.bodyAsText()).jsonObject
        val token = tokenJson["token"]?.jsonPrimitive?.content ?: ""
        
        assertTrue(token.isNotEmpty())

        // 2. Connect with the token
        client.webSocket("/ws?token=$token") {
            send("subscribe:channel-123")
            val subFrame = incoming.receive() as Frame.Text
            val subEvent = Json.decodeFromString<ConfluxEvent>(subFrame.readText())
            assertTrue(subEvent is ConfluxEvent.SubscriptionSuccess)
            assertEquals("channel-123", subEvent.channelId)
        }
    }

    @Test
    fun testWebSocketConnectionWithInvalidToken() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/ws?token=invalid") {
            val reason = closeReason.await()
            assertEquals(CloseReason.Codes.VIOLATED_POLICY.code, reason?.code)
        }
    }
}
