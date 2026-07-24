package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelType {
    TEXT, VOICE, ANNOUNCEMENT, CATEGORY, DM, GROUP_DM
}

@Serializable
data class Channel(
    val id: String,
    val serverId: String,
    val name: String,
    val type: ChannelType,
    val topic: String? = null,
    val categoryId: String? = null,
    val position: Int = 0,
    val metadata: Map<String, String> = emptyMap(),
    val canManage: Boolean = false,
    val recipients: List<User> = emptyList()
)
