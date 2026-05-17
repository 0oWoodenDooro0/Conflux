package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import website.woodendoor.conflux.models.*
import website.woodendoor.conflux.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class RoleProtectionRouteTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testEveryoneRoleProtection() = testApplication {
        application { module() }
        
        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "u1")))
        }
        val server = json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        
        // 2. Get the @everyone role (it should be the only role)
        val getRolesResponse = client.get("/api/servers/$serverId/roles")
        val roles = json.decodeFromString<List<Role>>(getRolesResponse.bodyAsText())
        val everyoneRole = roles.find { it.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE }!!
        val roleId = everyoneRole.id

        // 3. Try to rename @everyone
        val renameResponse = client.patch("/api/servers/$serverId/roles/$roleId?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(name = "Not Everyone")))
        }
        assertEquals(HttpStatusCode.BadRequest, renameResponse.status, "Should not be able to rename @everyone")

        // 4. Try to change @everyone priority
        val priorityResponse = client.patch("/api/servers/$serverId/roles/$roleId?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(priorityLevel = 10)))
        }
        assertEquals(HttpStatusCode.BadRequest, priorityResponse.status, "Should not be able to change @everyone priority")

        // 5. Try to delete @everyone
        val deleteResponse = client.delete("/api/servers/$serverId/roles/$roleId?userId=u1")
        assertEquals(HttpStatusCode.BadRequest, deleteResponse.status, "Should not be able to delete @everyone")
        
        // 6. Verify @everyone still exists and is unchanged
        val verifyResponse = client.get("/api/servers/$serverId/roles")
        val finalRoles = json.decodeFromString<List<Role>>(verifyResponse.bodyAsText())
        val finalEveryone = finalRoles.find { it.id == roleId }!!
        assertEquals(DEFAULT_ROLE_NAME_EVERYONE, finalEveryone.name)
        assertEquals(DEFAULT_ROLE_PRIORITY_EVERYONE, finalEveryone.priorityLevel)
    }

    @Test
    fun testCreateRoleProtection() = testApplication {
        application { module() }
        
        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "u1")))
        }
        val server = json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id

        // 2. Try to create role with priority -1
        val response = client.post("/api/servers/$serverId/roles?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateRoleRequest("Fake Everyone", priorityLevel = -1)))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status, "Should not be able to create role with priority -1")
    }

    @Test
    fun testUpdateEveryonePermissions() = testApplication {
        application { module() }
        
        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "u1")))
        }
        val server = json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        
        // 2. Get the @everyone role
        val getRolesResponse = client.get("/api/servers/$serverId/roles")
        val roles = json.decodeFromString<List<Role>>(getRolesResponse.bodyAsText())
        val everyoneRole = roles.find { it.priorityLevel == DEFAULT_ROLE_PRIORITY_EVERYONE }!!
        val roleId = everyoneRole.id

        // 3. Update @everyone permissions
        val newPermissions = ConfluxPermission.MESSAGING or ConfluxPermission.CHANNEL_MANAGEMENT
        val updateResponse = client.patch("/api/servers/$serverId/roles/$roleId?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(permissions = newPermissions)))
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status, "Should be able to update @everyone permissions. Body: ${updateResponse.bodyAsText()}")

        // 4. Verify update
        val verifyResponse = client.get("/api/servers/$serverId/roles")
        val finalRoles = json.decodeFromString<List<Role>>(verifyResponse.bodyAsText())
        val finalEveryone = finalRoles.find { it.id == roleId }!!
        assertEquals(newPermissions, finalEveryone.permissions)
    }
}
