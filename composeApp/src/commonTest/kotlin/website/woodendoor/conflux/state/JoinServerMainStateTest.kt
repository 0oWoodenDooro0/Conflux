package website.woodendoor.conflux.state

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.ChannelType
import website.woodendoor.conflux.models.Server
import kotlin.test.*

class JoinServerMainStateTest {

    @BeforeTest
    fun setup() {
        MainState.reset()
    }

    @Test
    fun testJoinServerActionSuccess() = runTest {
        val server = Server("s1", "Joined Server", "owner-id")
        val channels = listOf(Channel("c1", "s1", "general", ChannelType.TEXT))

        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/join") -> {
                    respond(content = ByteReadChannel("OK"), status = HttpStatusCode.Created)
                }
                request.url.encodedPath == "/api/servers" && request.method == HttpMethod.Get -> {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(listOf(server))),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath == "/api/servers/s1/channels" -> {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(channels)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
            }
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        val result = MainState.joinServer("s1", "u1", apiClient)

        assertTrue(result, "joinServer should return true on success")
        assertTrue(MainState.serverList.any { it.id == "s1" }, "serverList should be updated")
        assertEquals(server, MainState.selectedServer, "Should automatically select joined server")
        assertEquals(channels, MainState.channelList, "Should load channels for joined server")
    }

    @Test
    fun testJoinServerActionFailure() = runTest {
        val mockEngine = MockEngine { request ->
            respond(content = ByteReadChannel("Not Found"), status = HttpStatusCode.NotFound)
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        val result = MainState.joinServer("s1", "u1", apiClient)

        assertFalse(result, "joinServer should return false on failure")
        assertNull(MainState.selectedServer, "Should not select server on failure")
    }
}
