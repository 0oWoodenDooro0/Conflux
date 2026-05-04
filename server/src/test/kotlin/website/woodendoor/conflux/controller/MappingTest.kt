package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MappingTest {

    @Test
    fun `test CreateServerRequest to Server domain model mapping`() {
        val request = CreateServerRequest(
            name = "Test Server",
            iconUrl = "http://icon.com",
            ownerId = "user-1"
        )
        val server = request.toDomain(id = "server-1")
        
        assertEquals("server-1", server.id)
        assertEquals("Test Server", server.name)
        assertEquals("user-1", server.ownerId)
        assertEquals("http://icon.com", server.icon)
    }

    @Test
    fun `test CreateChannelRequest to Channel domain model mapping`() {
        val request = CreateChannelRequest(
            name = "general",
            type = ChannelType.TEXT,
            topic = "General discussion"
        )
        val channel = request.toDomain(id = "channel-1", serverId = "server-1")
        
        assertEquals("channel-1", channel.id)
        assertEquals("server-1", channel.serverId)
        assertEquals("general", channel.name)
        assertEquals(ChannelType.TEXT, channel.type)
        assertEquals("General discussion", channel.topic)
    }

    @Test
    fun `test CreateRoleRequest to Role domain model mapping`() {
        val request = CreateRoleRequest(
            name = "Admin",
            permissions = 0b111,
            color = 0xFF0000,
            priorityLevel = 10
        )
        val role = request.toDomain(id = "role-1")
        
        assertEquals("role-1", role.id)
        assertEquals("Admin", role.name)
        assertEquals(0b111L, role.permissions)
        assertEquals(0xFF0000, role.color)
        assertEquals(10, role.priorityLevel)
    }
}
