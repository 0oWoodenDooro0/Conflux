package website.woodendoor.conflux.routes

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

class ServerRoutesPriorityTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testCreateRoleInvalidPriority() = testApplication {
        application { module() }
        
        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "u1")))
        }
        val server = json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id

        // 2. Try to create role with invalid priority (too high)
        val responseHigh = client.post("/api/servers/$serverId/roles?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateRoleRequest("Invalid High", priorityLevel = 101)))
        }
        assertEquals(HttpStatusCode.BadRequest, responseHigh.status)

        // 3. Try to create role with invalid priority (too low)
        val responseLow = client.post("/api/servers/$serverId/roles?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateRoleRequest("Invalid Low", priorityLevel = -1)))
        }
        assertEquals(HttpStatusCode.BadRequest, responseLow.status)
    }

    @Test
    fun testUpdateRoleInvalidPriority() = testApplication {
        application { module() }
        
        // 1. Create a server
        val createServerResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateServerRequest(name = "Test Server", ownerId = "u1")))
        }
        val server = json.decodeFromString<Server>(createServerResponse.bodyAsText())
        val serverId = server.id
        
        // 2. Create a custom role
        val createRoleResponse = client.post("/api/servers/$serverId/roles?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateRoleRequest("Custom Role", priorityLevel = 10)))
        }
        val customRole = json.decodeFromString<Role>(createRoleResponse.bodyAsText())
        val roleId = customRole.id

        // 3. Try to update role with invalid priority (too high)
        val responseHigh = client.patch("/api/servers/$serverId/roles/$roleId?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(priorityLevel = 101)))
        }
        assertEquals(HttpStatusCode.BadRequest, responseHigh.status)

        // 4. Try to update role with invalid priority (too low)
        val responseLow = client.patch("/api/servers/$serverId/roles/$roleId?userId=u1") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(UpdateRoleRequest(priorityLevel = -1)))
        }
        assertEquals(HttpStatusCode.BadRequest, responseLow.status)
    }
}
