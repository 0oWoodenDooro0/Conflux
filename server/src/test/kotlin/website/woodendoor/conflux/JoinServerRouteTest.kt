package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.*
import kotlin.test.*

class JoinServerRouteTest {

    @Test
    fun testJoinServerSuccess() = testApplication {
        application {
            module()
        }

        // 1. Create owner and server
        val loginOwner = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "owner")))
        }
        val owner = Json.decodeFromString<User>(loginOwner.bodyAsText())

        val createResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Joinable Server", ownerId = owner.id)))
        }
        val server = Json.decodeFromString<Server>(createResponse.bodyAsText())

        // 2. Create joiner
        val loginJoiner = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "joiner")))
        }
        val joiner = Json.decodeFromString<User>(loginJoiner.bodyAsText())

        // 3. Join server
        val joinResponse = client.post("/api/servers/${server.id}/join?userId=${joiner.id}")
        assertEquals(HttpStatusCode.Created, joinResponse.status)

        // 4. Verify membership (via GET servers)
        val listResponse = client.get("/api/servers?userId=${joiner.id}")
        val servers = Json.decodeFromString<List<Server>>(listResponse.bodyAsText())
        assertTrue(servers.any { it.id == server.id }, "User should have joined the server")
    }

    @Test
    fun testJoinServerNotFound() = testApplication {
        application {
            module()
        }

        val response = client.post("/api/servers/nonexistent/join?userId=someuser")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testJoinServerAlreadyMember() = testApplication {
        application {
            module()
        }

        // 1. Create owner and server
        val loginOwner = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "owner2")))
        }
        val owner = Json.decodeFromString<User>(loginOwner.bodyAsText())

        val createResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Double Join Server", ownerId = owner.id)))
        }
        val server = Json.decodeFromString<Server>(createResponse.bodyAsText())

        // 2. Create joiner
        val loginJoiner = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "joiner2")))
        }
        val joiner = Json.decodeFromString<User>(loginJoiner.bodyAsText())

        // 3. Join first time
        client.post("/api/servers/${server.id}/join?userId=${joiner.id}")

        // 4. Join second time
        val secondJoinResponse = client.post("/api/servers/${server.id}/join?userId=${joiner.id}")
        assertEquals(HttpStatusCode.Conflict, secondJoinResponse.status)
    }

    @Test
    fun testJoinServerAsOwner() = testApplication {
        application {
            module()
        }

        // 1. Create owner and server
        val loginOwner = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "owner3")))
        }
        val owner = Json.decodeFromString<User>(loginOwner.bodyAsText())

        val createResponse = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "Owner Join Server", ownerId = owner.id)))
        }
        val server = Json.decodeFromString<Server>(createResponse.bodyAsText())

        // 2. Owner tries to join
        val joinResponse = client.post("/api/servers/${server.id}/join?userId=${owner.id}")
        assertEquals(HttpStatusCode.Conflict, joinResponse.status)
    }
}
