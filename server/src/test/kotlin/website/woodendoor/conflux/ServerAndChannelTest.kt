package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class ServerAndChannelTest {

    @Test
    fun testServerCreationInviteAndCategoryFlow() = testApplication {
        application {
            module()
        }

        // 1. Register Owner & Joiner
        val ownerReg = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("guildOwner", "password123")))
        }
        val owner = Json.decodeFromString<AuthResponse>(ownerReg.bodyAsText()).user

        val joinerReg = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("guildJoiner", "password123")))
        }
        val joiner = Json.decodeFromString<AuthResponse>(joinerReg.bodyAsText()).user

        // 2. Owner creates server with icon and description
        val createServerResp = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Gaming Guild", ownerId = owner.id, icon = "icon.png", description = "For gamers")))
        }
        assertEquals(HttpStatusCode.Created, createServerResp.status)
        val server = Json.decodeFromString<Server>(createServerResp.bodyAsText())
        assertEquals("Gaming Guild", server.name)
        assertNotNull(server.inviteCode)

        // 3. Check default categories & channels created
        val getChannelsResp = client.get("/api/servers/${server.id}/channels?userId=${owner.id}")
        assertEquals(HttpStatusCode.OK, getChannelsResp.status)
        val channels = Json.decodeFromString<List<Channel>>(getChannelsResp.bodyAsText())
        assertTrue(channels.any { it.type == ChannelType.CATEGORY && it.name == "TEXT CHANNELS" })
        assertTrue(channels.any { it.type == ChannelType.CATEGORY && it.name == "VOICE CHANNELS" })
        assertTrue(channels.any { it.type == ChannelType.TEXT && it.name == "general" })
        assertTrue(channels.any { it.type == ChannelType.VOICE && it.name == "General Voice" })

        // 4. Joiner joins server using invite code
        val joinInviteResp = client.post("/api/servers/invite/join?userId=${joiner.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(JoinByInviteRequest(inviteCode = server.inviteCode!!)))
        }
        assertEquals(HttpStatusCode.OK, joinInviteResp.status)
        val joinedServer = Json.decodeFromString<Server>(joinInviteResp.bodyAsText())
        assertEquals(server.id, joinedServer.id)

        // 5. Verify joiner can get server list
        val joinerServersResp = client.get("/api/servers?userId=${joiner.id}")
        assertEquals(HttpStatusCode.OK, joinerServersResp.status)
        val joinerServers = Json.decodeFromString<List<Server>>(joinerServersResp.bodyAsText())
        assertTrue(joinerServers.any { it.id == server.id })
    }
}
