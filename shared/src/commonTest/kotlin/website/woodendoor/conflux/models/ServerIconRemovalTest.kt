package website.woodendoor.conflux.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ServerIconRemovalTest {

    @Test
    fun testServerModelHasNoIcon() {
        val server = Server(
            id = "server-1",
            name = "Test Server",
            ownerId = "owner-1"
        )
        // This test is mostly to ensure we can create it without an icon
        assertEquals("Test Server", server.name)
    }

    @Test
    fun testCreateServerRequestHasNoIcon() {
        val request = CreateServerRequest(
            name = "New Server",
            ownerId = "owner-1"
        )
        assertEquals("New Server", request.name)
    }
}
