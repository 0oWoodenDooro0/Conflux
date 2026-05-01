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

class ServerApiClientTest {
    @Test
    fun testCreateServer() = runTest {
        val mockServer = Server(
            id = "test-id",
            name = "Test Server",
            ownerId = "owner-id",
            icon = "http://icon.com"
        )
        
        val mockEngine = MockEngine { request ->
            assertEquals("localhost", request.url.host)
            assertEquals("/api/servers", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            
            respond(
                content = ByteReadChannel(Json.encodeToString(mockServer)),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val client = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }, "http://localhost:8080")
        val result = client.createServer("Test Server", "http://icon.com")
        
        assertEquals(mockServer, result)
    }
}
