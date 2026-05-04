package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.controller.OperationResult
import website.woodendoor.conflux.models.*
import kotlin.test.*

class ChannelManagementApiTest {

    @Test
    fun testEditDeleteChannelPermissions() = testApplication {
        application {
            module()
        }

        // 1. Create a server as owner
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Mgmt Server", ownerId = "owner1")))
        }
        val server = Json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        val ownerId = server.ownerId

        // 2. Create a channel as owner
        val createChannelResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "test-channel")))
        }
        assertEquals(HttpStatusCode.Created, createChannelResponse.status)
        val channel = Json.decodeFromString<Channel>(createChannelResponse.bodyAsText())
        val channelId = channel.id

        // 3. Create a normal member (dummy server trick to register user)
        val dummyResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Dummy", ownerId = "member1")))
        }
        val memberId = Json.decodeFromString<Server>(dummyResponse.bodyAsText()).ownerId
        client.post("/api/servers/$serverId/join") {
            parameter("userId", memberId)
        }

        // 4. Member without permission should get 403 when trying to edit channel
        val memberEditChannelResponse = client.patch("/api/servers/$serverId/channels/$channelId") {
            parameter("userId", memberId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateChannelRequest(name = "renamed-channel")))
        }
        assertEquals(HttpStatusCode.Forbidden, memberEditChannelResponse.status, "Member without permission should be rejected on edit")

        // 5. Member without permission should get 403 when trying to delete channel
        val memberDeleteChannelResponse = client.delete("/api/servers/$serverId/channels/$channelId") {
            parameter("userId", memberId)
        }
        assertEquals(HttpStatusCode.Forbidden, memberDeleteChannelResponse.status, "Member without permission should be rejected on delete")

        // 6. Owner should be able to edit channel
        val ownerEditChannelResponse = client.patch("/api/servers/$serverId/channels/$channelId") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateChannelRequest(name = "owner-renamed-channel")))
        }
        assertEquals(HttpStatusCode.OK, ownerEditChannelResponse.status, "Owner should be able to edit channel")
        val updatedChannel = Json.decodeFromString<Channel>(ownerEditChannelResponse.bodyAsText())
        assertEquals("owner-renamed-channel", updatedChannel.name)
    }
}
