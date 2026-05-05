package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ConfluxEventSerializationTest {

    @Test
    fun `test Connected serialization`() {
        val event: ConfluxEvent = ConfluxEvent.Connected
        val json = Json.encodeToString(event)
        val decoded = Json.decodeFromString<ConfluxEvent>(json)
        assertEquals(ConfluxEvent.Connected, decoded)
    }

    @Test
    fun `test NewMessage serialization`() {
        val message = Message(
            id = "msg-1",
            channelId = "chan-1",
            authorId = "user-1",
            content = "Hello",
            timestamp = 123456789L
        )
        val event: ConfluxEvent = ConfluxEvent.NewMessage(message)
        val json = Json.encodeToString(event)
        
        assertTrue(json.contains("website.woodendoor.conflux.models.ConfluxEvent.NewMessage"))
        assertTrue(json.contains("Hello"))
        
        val decoded = Json.decodeFromString<ConfluxEvent>(json)
        assertTrue(decoded is ConfluxEvent.NewMessage)
        assertEquals(message, decoded.message)
    }

    @Test
    fun `test SubscriptionSuccess serialization`() {
        val event: ConfluxEvent = ConfluxEvent.SubscriptionSuccess("chan-1")
        val json = Json.encodeToString(event)
        
        val decoded = Json.decodeFromString<ConfluxEvent>(json)
        assertTrue(decoded is ConfluxEvent.SubscriptionSuccess)
        assertEquals("chan-1", decoded.channelId)
    }

    @Test
    fun `test PermissionUpdate serialization`() {
        val event: ConfluxEvent = ConfluxEvent.PermissionUpdate("server-1", roleId = "role-1")
        val json = Json.encodeToString(event)
        
        val decoded = Json.decodeFromString<ConfluxEvent>(json)
        assertTrue(decoded is ConfluxEvent.PermissionUpdate)
        assertEquals("server-1", decoded.serverId)
        assertEquals("role-1", decoded.roleId)
        assertNull(decoded.userId)
    }
}
