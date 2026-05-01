package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiModelsTest {
    @Test
    fun testCreateServerRequestSerialization() {
        val request = CreateServerRequest(name = "Test Server", iconUrl = "http://example.com/icon.png")
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<CreateServerRequest>(json)
        assertEquals(request, deserialized)
    }
}
