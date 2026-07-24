package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.AuthResponse
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.RegisterRequest
import kotlin.test.*

class LoginRouteTest {

    @Test
    fun testLoginExistingUser() = testApplication {
        application {
            module()
        }
        
        // 1. First register the user
        val firstResponse = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("existingUser", "password123")))
        }
        assertEquals(HttpStatusCode.OK, firstResponse.status)
        val auth1 = Json.decodeFromString<AuthResponse>(firstResponse.bodyAsText())
        
        // 2. Second login with correct password
        val secondResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest("existingUser", "password123")))
        }
        assertEquals(HttpStatusCode.OK, secondResponse.status)
        val auth2 = Json.decodeFromString<AuthResponse>(secondResponse.bodyAsText())
        
        assertEquals(auth1.user.id, auth2.user.id)
        assertEquals("existingUser", auth2.user.username)
        assertNotNull(auth2.token)

        // 3. Login with incorrect password
        val thirdResponse = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest("existingUser", "wrongpassword")))
        }
        assertEquals(HttpStatusCode.Unauthorized, thirdResponse.status)
    }

    @Test
    fun testRegisterNewUser() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest("newUser", "password123")))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val auth = Json.decodeFromString<AuthResponse>(response.bodyAsText())
        assertEquals("newUser", auth.user.username)
        assertNotNull(auth.user.id)
        assertNotNull(auth.token)
    }

    @Test
    fun testLoginValidationFailure() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest("ab", "password"))) // Too short
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("at least 3 characters", ignoreCase = true))
    }
}
