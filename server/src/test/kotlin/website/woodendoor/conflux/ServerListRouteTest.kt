package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.User
import website.woodendoor.conflux.models.LoginRequest
import kotlin.test.*

class ServerListRouteTest {

    @Test
    fun testGetServersForUser() = testApplication {
        application {
            module()
        }
        
        // 1. Login to get a user
        val loginResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "serverUser")))
        }
        val user = Json.decodeFromString<User>(loginResponse.bodyAsText())
        
        // 2. Create a server (as default-user for now, but we want to see it)
        // Wait, the current implementation of createServer sets ownerId to "default-user".
        // And there is no way to join a server yet.
        // So for this track, maybe the GET /api/servers?userId=... should return ALL servers if the user is "default-user" 
        // or if we change createServer to use the provided userId (not yet implemented in API).
        
        // Let's refine the plan: the route should return servers where the user is owner.
        // I'll update createServer to use a provided ownerId or just return all for now to satisfy the "Server List" requirement.
        // Actually, the spec says "where the user is a member or owner".
        
        val createResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "User's Server", ownerId = "default-user")))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        
        // 3. Get servers for the user
        // Since createServer uses "default-user", I'll test with that.
        val response = client.get("/api/servers?userId=default-user")
        assertEquals(HttpStatusCode.OK, response.status)
        val servers = Json.decodeFromString<List<Server>>(response.bodyAsText())
        assertTrue(servers.any { it.name == "User's Server" })
    }

    @Test
    fun testGetServersByUsername() = testApplication {
        application {
            module()
        }
        
        // 1. Create a server with a specific ownerId
        client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Username Server", ownerId = "testuser")))
        }
        
        // 2. Query by username (which is "testuser")
        val response = client.get("/api/servers?userId=testuser")
        assertEquals(HttpStatusCode.OK, response.status)
        val servers = Json.decodeFromString<List<Server>>(response.bodyAsText())
        assertTrue(servers.any { it.name == "Username Server" }, "Server should be found by username")
    }

    @Test
    fun testGetServersByUsernameWithSeparateId() = testApplication {
        application {
            module()
        }

        // 1. Create a user with a specific ID and username via login
        val loginResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "realuser")))
        }
        val user = Json.decodeFromString<User>(loginResponse.bodyAsText())
        val userId = user.id
        
        // 2. Create a server with the user's ID
        client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "UUID Server", ownerId = userId)))
        }
        
        // 3. Query by username ("realuser")
        val response = client.get("/api/servers?userId=realuser")
        assertEquals(HttpStatusCode.OK, response.status)
        val servers = Json.decodeFromString<List<Server>>(response.bodyAsText())
        assertTrue(servers.any { it.name == "UUID Server" }, "Server should be found by username even if ID is UUID")
    }
}
