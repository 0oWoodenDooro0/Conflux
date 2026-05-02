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
            iconUrl = "http://example.com/icon.png",
            ownerId = "test-user"
        )
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<CreateServerRequest>(json)
        assertEquals(request, deserialized)
        assertEquals("test-user", deserialized.ownerId)
    }
}
