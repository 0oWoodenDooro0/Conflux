package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StructuralModelTest {

    @Test
    fun testServerSerialization() {
        val server = Server(
            id = "server-789",
            name = "Conflux HQ",
            ownerId = "user-123",
            memberIds = listOf("user-123", "user-456"),
            roleIds = listOf("role-456")
        )
        val json = Json.encodeToString(server)
        val deserialized = Json.decodeFromString<Server>(json)
        assertEquals(server, deserialized)
    }

    @Test
    fun testChannelSerialization() {
        val channel = Channel(
            id = "channel-001",
            serverId = "server-789",
            name = "general",
            type = ChannelType.TEXT,
            topic = "General discussion"
        )
        val json = Json.encodeToString(channel)
        val deserialized = Json.decodeFromString<Channel>(json)
        assertEquals(channel, deserialized)
    }
}
