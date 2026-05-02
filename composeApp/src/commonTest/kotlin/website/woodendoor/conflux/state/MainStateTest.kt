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

class MainStateTest {

    @BeforeTest
    fun setup() {
        MainState.reset()
    }

    @Test
    fun testInitialState() {
        assertNull(MainState.selectedServer)
        assertTrue(MainState.channelList.isEmpty())
    }

    @Test
    fun testSelectServer() {
        val server = Server("s1", "Server 1", "u1")
        MainState.selectedServer = server
        assertEquals(server, MainState.selectedServer)
    }

    @Test
    fun testSetChannels() {
        val channels = listOf(
            Channel("c1", "s1", "general", ChannelType.TEXT)
        )
        MainState.channelList = channels
        assertEquals(channels, MainState.channelList)
    }

    @Test
    fun testErrorState() {
        assertNull(MainState.channelFetchError)
        MainState.channelFetchError = "Error"
        assertEquals("Error", MainState.channelFetchError)
    }

    @Test
    fun testSelectServerAction() = runTest {
        val server = Server("s1", "Server 1", "u1")
        val channels = listOf(
            Channel("c1", "s1", "general", ChannelType.TEXT)
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(Json.encodeToString(channels)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.selectServer(server, apiClient)

        assertEquals(server, MainState.selectedServer)
        assertEquals(channels, MainState.channelList)
        assertNull(MainState.channelFetchError)
    }

    @Test
    fun testSelectServerActionError() = runTest {
        val server = Server("s1", "Server 1", "u1")

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Error"),
                status = HttpStatusCode.InternalServerError
            )
        }
        val apiClient = ServerApiClient(HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }, "http://localhost")

        MainState.selectServer(server, apiClient)

        assertEquals(server, MainState.selectedServer)
        assertTrue(MainState.channelList.isEmpty())
        assertNotNull(MainState.channelFetchError)
    }
}
