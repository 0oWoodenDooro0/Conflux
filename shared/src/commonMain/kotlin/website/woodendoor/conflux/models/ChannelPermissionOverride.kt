package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class OverrideType {
    ROLE, USER
}

@Serializable
data class ChannelPermissionOverride(
    val id: String,
    val channelId: String,
    val targetId: String, // Role ID or User ID
    val targetType: OverrideType,
    val allow: Long,
    val deny: Long
)

@Serializable
data class UpsertOverrideRequest(
    val targetId: String,
    val targetType: OverrideType,
    val allow: Long,
    val deny: Long
)
