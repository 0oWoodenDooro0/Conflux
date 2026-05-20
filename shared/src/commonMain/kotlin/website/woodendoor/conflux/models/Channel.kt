package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelType {
    TEXT, VOICE, ANNOUNCEMENT
}

@Serializable
data class Channel(
    val id: String,
    val serverId: String,
    val name: String,
    val type: ChannelType,
    val topic: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val canManage: Boolean = false
)
