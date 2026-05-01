package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User
import kotlin.test.*

class LoginRouteTest {

    @Test
    fun testLoginExistingUser() = testApplication {
        application {
            module()
        }
        
        // 1. First login to create the user
        val firstResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "existingUser")))
        }
        assertEquals(HttpStatusCode.OK, firstResponse.status)
        val user1 = Json.decodeFromString<User>(firstResponse.bodyAsText())
        
        // 2. Second login with same username
        val secondResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "existingUser")))
        }
        assertEquals(HttpStatusCode.OK, secondResponse.status)
        val user2 = Json.decodeFromString<User>(secondResponse.bodyAsText())
        
        assertEquals(user1.id, user2.id)
        assertEquals("existingUser", user2.username)
    }

    @Test
    fun testLoginNewUser() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "newUser")))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val user = Json.decodeFromString<User>(response.bodyAsText())
        assertEquals("newUser", user.username)
        assertNotNull(user.id)
    }

    @Test
    fun testLoginValidationFailure() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(username = "ab"))) // Too short
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("at least 3 characters", ignoreCase = true))
    }
}
