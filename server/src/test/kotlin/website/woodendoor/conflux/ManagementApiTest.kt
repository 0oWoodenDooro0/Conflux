package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class ManagementApiTest {

    @Test
    fun testRoleManagementPermissions() = testApplication {
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

        // 2. Owner should be able to create a role
        val createRoleResponse = client.post("/api/servers/$serverId/roles") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateRoleRequest(name = "Moderator", permissions = ConfluxPermission.MESSAGING or ConfluxPermission.CHANNEL_MANAGEMENT)))
        }
        assertEquals(HttpStatusCode.Created, createRoleResponse.status, "Owner should have permission to create role")
        val moderatorRole = Json.decodeFromString<Role>(createRoleResponse.bodyAsText())

        // 3. Create a member
        val dummyResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Dummy", ownerId = "member1")))
        }
        val memberId = Json.decodeFromString<Server>(dummyResponse.bodyAsText()).ownerId
        client.post("/api/servers/$serverId/join") {
            parameter("userId", memberId)
        }

        // 4. Member without permission should get 403 when creating role
        val memberCreateRoleResponse = client.post("/api/servers/$serverId/roles") {
            parameter("userId", memberId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateRoleRequest(name = "Rebel")))
        }
        assertEquals(HttpStatusCode.Forbidden, memberCreateRoleResponse.status, "Member without permission should be rejected")

        // 5. Owner assigns Moderator role to member
        val assignRoleResponse = client.post("/api/servers/$serverId/roles/assign") {
            parameter("userId", ownerId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(AssignRoleRequest(userId = memberId, roleId = moderatorRole.id)))
        }
        assertEquals(HttpStatusCode.OK, assignRoleResponse.status, "Owner should be able to assign role")

        // 6. Verify member now has the permissions
        val memberPermsResponse = client.post("/api/servers/$serverId/channels") {
            parameter("userId", memberId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateChannelRequest(name = "mod-channel")))
        }
        assertEquals(HttpStatusCode.Created, memberPermsResponse.status, "Member with Moderator role should now have channel management permission")
    }
}
