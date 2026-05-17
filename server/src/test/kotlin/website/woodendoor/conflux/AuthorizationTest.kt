package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class AuthorizationTest {

    @Test
    fun testChannelCreationPermission() = testApplication {
        application {
            module()
        }

        // 1. Create a server as owner
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Auth Server", ownerId = "owner1")))
        }
        val server = Json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        val ownerId = server.ownerId

        // 2. Owner should be able to create channel
        val ownerCreateResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "admin-only")))
        }
        assertEquals(HttpStatusCode.Created, ownerCreateResponse.status, "Owner should have permission to create channel")

        // 3. Create another user who joins the server (gets Member role by default, no channel management)
        // Ensure user exists and get their resolved ID
        val dummyResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Dummy", ownerId = "member1")))
        }
        val dummyServer = Json.decodeFromString<Server>(dummyResponse.bodyAsText())
        val memberId = dummyServer.ownerId
        
        client.post("/api/servers/$serverId/join") {
            parameter("userId", memberId)
        }

        // 4. Member without permission should get 403 Forbidden
        val memberCreateResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", memberId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "member-channel")))
        }
        assertEquals(HttpStatusCode.Forbidden, memberCreateResponse.status, "Member without permission should be rejected with 403")
    }

    @Test
    fun testMessagingPermission() = testApplication {
        application {
            module()
        }

        // 1. Create a server as owner
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Msg Server", ownerId = "owner1")))
        }
        val server = Json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        val ownerId = server.ownerId

        // 2. Create a channel
        val createChannelResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "general")))
        }
        val channel = Json.decodeFromString<Channel>(createChannelResponse.bodyAsText())
        val channelId = channel.id

        // 3. Create a member who joins
        // Ensure user exists and get their resolved ID
        val dummyResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Dummy", ownerId = "member1")))
        }
        val dummyServer = Json.decodeFromString<Server>(dummyResponse.bodyAsText())
        val memberId = dummyServer.ownerId
        
        client.post("/api/servers/$serverId/join") {
            parameter("userId", memberId)
        }

        // 4. Member should have messaging permission by default
        val memberMsgResponse = client.post("/api/channels/$channelId/messages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(SendMessageRequest(senderId = memberId, content = "Hello")))
        }
        assertEquals(HttpStatusCode.Created, memberMsgResponse.status, "Member should have permission to send messages by default. Status: ${memberMsgResponse.status}, Body: ${memberMsgResponse.bodyAsText()}")

        // 5. Create another valid user who is NOT a member and they should get 403.
        val strangerDummyResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Stranger", ownerId = "stranger")))
        }
        val strangerServer = Json.decodeFromString<Server>(strangerDummyResponse.bodyAsText())
        val nonMemberId = strangerServer.ownerId

        val strangerMsgResponse = client.post("/api/channels/$channelId/messages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(SendMessageRequest(senderId = nonMemberId, content = "Attack")))
        }
        assertEquals(HttpStatusCode.Forbidden, strangerMsgResponse.status, "Non-member should be rejected with 403")
    }
}
