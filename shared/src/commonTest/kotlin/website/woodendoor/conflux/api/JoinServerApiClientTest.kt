package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class JoinServerApiClientTest {
    @Test
    fun testJoinServerSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/servers/s1/join", request.url.encodedPath)
            assertEquals("u1", request.url.parameters["userId"])
            assertEquals(HttpMethod.Post, request.method)
            
            respond(
                content = ByteReadChannel("Joined successfully"),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        
        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.joinServer("s1", "u1")
        
        assertTrue(result, "Should return true on success")
    }

    @Test
    fun testJoinServerFailure() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Not Found"),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        
        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.joinServer("s1", "u1")
        
        assertFalse(result, "Should return false on failure (404)")
    }

    @Test
    fun testJoinServerConflict() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Conflict"),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        
        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.joinServer("s1", "u1")
        
        assertFalse(result, "Should return false on failure (409)")
    }
}
