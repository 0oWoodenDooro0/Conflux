package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRequestTest {
    @Test
    fun testLoginRequestSerialization() {
        val request = LoginRequest(username = "testuser")
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<LoginRequest>(json)
        assertEquals(request, deserialized)
    }
}
