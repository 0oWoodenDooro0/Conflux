package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class ChannelRouteTest {

    @Test
    fun testGetChannelsForServer() = testApplication {
        application {
            module()
        }

        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "owner")))
        }
        val server = Json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id

        // 2. Verify auto-created default categories and channels
        val initialChannelsResponse = client.get("/api/servers/$serverId/channels")
        val initialChannels = Json.decodeFromString<List<Channel>>(initialChannelsResponse.bodyAsText())
        assertTrue(initialChannels.isNotEmpty())
        assertTrue(initialChannels.any { it.name == "general" && it.type == ChannelType.TEXT })

        // 3. Create another channel
        val createChannelResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", server.ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "announcements")))
        }
        assertEquals(HttpStatusCode.Created, createChannelResponse.status)

        // 4. Get channels for the server
        val response = client.get("/api/servers/$serverId/channels")
        assertEquals(HttpStatusCode.OK, response.status)
        val channels = Json.decodeFromString<List<Channel>>(response.bodyAsText())
        assertTrue(channels.any { it.name == "general" })
        assertTrue(channels.any { it.name == "announcements" })
    }

    @Test
    fun testGetChannelsForNonExistentServer() = testApplication {
        application {
            module()
        }

        val response = client.get("/api/servers/non-existent/channels")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
