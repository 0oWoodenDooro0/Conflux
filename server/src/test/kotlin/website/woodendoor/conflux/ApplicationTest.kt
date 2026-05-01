package website.woodendoor.conflux

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }

    @Test
    fun testCreateServer() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/servers") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(CreateServerRequest(name = "New Server", iconUrl = "http://icon.com")))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val server = Json.decodeFromString<Server>(response.bodyAsText())
        assertEquals("New Server", server.name)
        assertEquals("http://icon.com", server.icon)
        assertNotNull(server.id)
    }
}