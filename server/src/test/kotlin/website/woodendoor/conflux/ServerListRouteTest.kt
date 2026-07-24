package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class ServerListRouteTest {

    @Test
    fun testGetServersForUser() = testApplication {
        application {
            module()
        }
        
        // 1. Login to get a user
        val loginResponse = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(username = "serverUser", password = "password123")))
        }
        val user = Json.decodeFromString<AuthResponse>(loginResponse.bodyAsText()).user
        
        val createResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "User's Server", ownerId = "default-user")))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        
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
        val loginResponse = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(username = "realuser", password = "password123")))
        }
        val user = Json.decodeFromString<AuthResponse>(loginResponse.bodyAsText()).user
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
