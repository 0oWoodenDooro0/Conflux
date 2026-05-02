package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class MessageRouteTest {

    @Test
    fun testSendAndGetMessages() = testApplication {
        application {
            module()
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

        // 2. Send a message
        val sendResponse = client.post("/api/channels/$channelId/messages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(SendMessageRequest(senderId = "default-user", content = "Hello!")))
        }
        assertEquals(HttpStatusCode.Created, sendResponse.status)
        val message = Json.decodeFromString<Message>(sendResponse.bodyAsText())
        assertEquals("Hello!", message.content)
        assertEquals("default-user", message.authorId)

        // 3. Get messages
        val getResponse = client.get("/api/channels/$channelId/messages")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val messages = Json.decodeFromString<List<Message>>(getResponse.bodyAsText())
        assertEquals(1, messages.size)
        assertEquals("Hello!", messages[0].content)
    }

    @Test
    fun testGetMessagesForNonExistentChannel() = testApplication {
        application {
            module()
        }

        val response = client.get("/api/channels/non-existent/messages")
        assertEquals(HttpStatusCode.OK, response.status) // Should probably return empty list if channel doesn't exist but let's see. 
        // Actually, existing repositories usually return empty list if not found for list queries.
        val messages = Json.decodeFromString<List<Message>>(response.bodyAsText())
        assertTrue(messages.isEmpty())
    }
}
