package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiModelsTest {
    @Test
    fun testCreateServerRequestWithDetails() {
        val request = CreateServerRequest(
            name = "Full Server",
            ownerId = "test-user"
        )
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<CreateServerRequest>(json)
        assertEquals(request, deserialized)
        assertEquals("test-user", deserialized.ownerId)
    }

    @Test
    fun testUpdateRoleRequestSerialization() {
        val request = UpdateRoleRequest(
            name = "Updated Name",
            permissions = 100L,
            color = 0xFF0000,
            priorityLevel = 5
        )
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<UpdateRoleRequest>(json)
        assertEquals(request, deserialized)
    }

    @Test
    fun testUpdateRoleRequestPartialSerialization() {
        val request = UpdateRoleRequest(
            name = "Only Name"
        )
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<UpdateRoleRequest>(json)
        assertEquals("Only Name", deserialized.name)
        assertEquals(null, deserialized.permissions)
    }
}
