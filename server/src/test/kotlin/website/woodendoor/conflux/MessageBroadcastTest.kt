package website.woodendoor.conflux

import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import website.woodendoor.conflux.models.*
import kotlin.test.*

class MessageBroadcastTest {

    @Test
    fun `sending a message should broadcast it to WebSocket subscribers`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        // 1. Setup: Create server and channel
        val serverResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "default-user")))
        }
        val server = Json.decodeFromString<Server>(serverResponse.bodyAsText())
        
        val channelResponse = client.post("/api/servers/${server.id}/channels") {
            parameter("userId", server.ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "general")))
        }
        val channel = Json.decodeFromString<Channel>(channelResponse.bodyAsText())
        val channelId = channel.id

        // 2. Get WebSocket token
        val tokenResponse = client.post("/api/auth/ws-token") {
            setBody("user-ws")
        }
        val tokenJson = Json.parseToJsonElement(tokenResponse.bodyAsText()).jsonObject
        val token = tokenJson["token"]?.jsonPrimitive?.content ?: ""

        // 3. Connect to WebSocket and subscribe
        client.webSocket("/ws?token=$token") {
            // Subscribe to the channel
            send("subscribe:$channelId")
            
            val subFrame = incoming.receive() as Frame.Text
            val subEvent = Json.decodeFromString<ConfluxEvent>(subFrame.readText())
            assertTrue(subEvent is ConfluxEvent.SubscriptionSuccess)
            assertEquals(channelId, subEvent.channelId)

            // 4. Send a message via HTTP POST
            client.post("/api/channels/$channelId/messages") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(SendMessageRequest(senderId = "default-user", content = "Broadcast test")))
            }

            // 5. Verify broadcast received over WebSocket
            val broadcastFrame = incoming.receive() as Frame.Text
            val event = Json.decodeFromString<ConfluxEvent>(broadcastFrame.readText())
            
            assertTrue(event is ConfluxEvent.NewMessage)
            assertEquals("Broadcast test", event.message.content)
            assertEquals("default-user", event.message.authorId)
        }
    }
}
