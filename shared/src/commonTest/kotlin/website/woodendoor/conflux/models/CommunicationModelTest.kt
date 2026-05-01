package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CommunicationModelTest {

    @Test
    fun testMessageSerialization() {
        val message = Message(
            id = "msg-001",
            channelId = "channel-001",
            authorId = "user-123",
            content = "Hello, Conflux!",
            timestamp = 1714564800000L,
            attachments = listOf("https://example.com/image.png")
        )
        val json = Json.encodeToString(message)
        val deserialized = Json.decodeFromString<Message>(json)
        assertEquals(message, deserialized)
    }
}
