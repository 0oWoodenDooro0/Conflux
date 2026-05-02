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
            // Read connection confirmation
            incoming.receive()
            
            // Subscribe to the channel
            send("subscribe:$channelId")
            incoming.receive() // Read subscription confirmation

            // 4. Send a message via HTTP POST (using a different "client" call or same one)
            client.post("/api/channels/$channelId/messages") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(SendMessageRequest(senderId = "default-user", content = "Broadcast test")))
            }

            // 5. Verify broadcast received over WebSocket
            val broadcastFrame = incoming.receive() as Frame.Text
            val broadcastText = broadcastFrame.readText()
            
            // For now, let's assume it sends the raw message JSON or a specific Event object
            assertTrue(broadcastText.contains("Broadcast test"), "Broadcast should contain message content")
            assertTrue(broadcastText.contains("default-user"), "Broadcast should contain sender ID")
        }
    }
}
