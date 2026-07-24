package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class FriendAndDmTest {

    @Test
    fun testFriendRequestAndDmChannelFlow() = testApplication {
        application {
            module()
        }

        // 1. Register Alice & Bob
        val regAlice = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("aliceUser", "password123")))
        }
        val alice = Json.decodeFromString<AuthResponse>(regAlice.bodyAsText()).user

        val regBob = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("bobUser", "password123")))
        }
        val bob = Json.decodeFromString<AuthResponse>(regBob.bodyAsText()).user

        // 2. Alice sends friend request to Bob
        val reqResponse = client.post("/api/friends/request?userId=${alice.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(FriendRequestPayload("bobUser")))
        }
        assertEquals(HttpStatusCode.Created, reqResponse.status)
        val friendship = Json.decodeFromString<Friendship>(reqResponse.bodyAsText())
        assertEquals(FriendshipStatus.PENDING, friendship.status)

        // 3. Bob accepts friend request
        val acceptResponse = client.post("/api/friends/${friendship.id}/accept?userId=${bob.id}")
        assertEquals(HttpStatusCode.OK, acceptResponse.status)

        // 4. Check friends list for Alice
        val friendsListResp = client.get("/api/friends?userId=${alice.id}")
        assertEquals(HttpStatusCode.OK, friendsListResp.status)
        val friends = Json.decodeFromString<List<Friendship>>(friendsListResp.bodyAsText())
        assertTrue(friends.any { it.friendUser?.username == "bobUser" && it.status == FriendshipStatus.ACCEPTED })

        // 5. Open DM channel between Alice & Bob
        val openDmResp = client.post("/api/dm/open?userId=${alice.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(OpenDmRequest(bob.id)))
        }
        assertEquals(HttpStatusCode.OK, openDmResp.status)
        val dmChannel = Json.decodeFromString<Channel>(openDmResp.bodyAsText())
        assertEquals(ChannelType.DM, dmChannel.type)

        // 6. Get DM channels for Bob
        val getDmsResp = client.get("/api/dm/channels?userId=${bob.id}")
        assertEquals(HttpStatusCode.OK, getDmsResp.status)
        val bobDms = Json.decodeFromString<List<Channel>>(getDmsResp.bodyAsText())
        assertTrue(bobDms.any { it.id == dmChannel.id })
    }
}
