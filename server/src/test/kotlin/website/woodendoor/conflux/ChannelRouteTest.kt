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

        // 2. Create a channel
        val createChannelResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", server.ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "general")))
        }
        assertEquals(HttpStatusCode.Created, createChannelResponse.status)

        // 3. Get channels for the server
        val response = client.get("/api/servers/$serverId/channels")
        assertEquals(HttpStatusCode.OK, response.status)
        val channels = Json.decodeFromString<List<Channel>>(response.bodyAsText())
        assertEquals(1, channels.size)
        assertEquals("general", channels[0].name)
        assertEquals(serverId, channels[0].serverId)
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
