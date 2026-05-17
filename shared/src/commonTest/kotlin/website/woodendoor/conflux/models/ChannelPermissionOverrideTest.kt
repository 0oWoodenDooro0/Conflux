package website.woodendoor.conflux.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelPermissionOverrideTest {

    @Test
    fun testChannelPermissionOverrideSerialization() {
        val override = ChannelPermissionOverride(
            id = "o1",
            channelId = "c1",
            targetId = "t1",
            targetType = OverrideType.ROLE,
            allow = 1L,
            deny = 2L
        )

        val json = Json.encodeToString(override)
        val deserialized = Json.decodeFromString<ChannelPermissionOverride>(json)

        assertEquals(override, deserialized)
    }

    @Test
    fun testUpsertOverrideRequestSerialization() {
        val request = UpsertOverrideRequest(
            targetId = "t1",
            targetType = OverrideType.USER,
            allow = 4L,
            deny = 8L
        )

        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<UpsertOverrideRequest>(json)

        assertEquals(request, deserialized)
    }
}
