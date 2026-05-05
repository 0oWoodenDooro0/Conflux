package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelSerializationTest {

    @Test
    fun testServerWithRolesSerialization() {
        val role = Role("r1", "s1", "Admin", ConfluxPermission.ALL, 0xFF0000, 100)
        val server = Server("s1", "Test", "owner1", null, emptyList(), listOf("r1"), listOf(role))
        
        val json = Json.encodeToString(server)
        val deserialized = Json.decodeFromString<Server>(json)
        
        assertEquals(1, deserialized.roles.size)
        assertEquals("Admin", deserialized.roles[0].name)
    }

    @Test
    fun testUserWithPermissionsSerialization() {
        val user = User("u1", "user", "0001", null, 0x1L)
        
        val json = Json.encodeToString(user)
        val deserialized = Json.decodeFromString<User>(json)
        
        assertEquals(0x1L, deserialized.permissions)
    }
}
