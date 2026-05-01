package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentityModelTest {

    @Test
    fun testUserSerialization() {
        val user = User(
            id = "user-123",
            username = "testuser",
            discriminator = "0001",
            avatar = "https://example.com/avatar.png"
        )
        val json = Json.encodeToString(user)
        val deserialized = Json.decodeFromString<User>(json)
        assertEquals(user, deserialized)
    }

    @Test
    fun testRoleSerialization() {
        val role = Role(
            id = "role-456",
            name = "Admin",
            permissions = 0xFFFFFFFFL,
            color = 0xFF0000
        )
        val json = Json.encodeToString(role)
        val deserialized = Json.decodeFromString<Role>(json)
        assertEquals(role, deserialized)
    }
}
