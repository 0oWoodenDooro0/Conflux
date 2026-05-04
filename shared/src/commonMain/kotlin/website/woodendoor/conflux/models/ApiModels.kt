package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String
)

@Serializable
data class CreateServerRequest(
    val name: String,
    val iconUrl: String? = null,
    val ownerId: String
)

@Serializable
data class CreateChannelRequest(
    val name: String,
    val type: ChannelType = ChannelType.TEXT,
    val topic: String? = null
)

@Serializable
data class SendMessageRequest(
    val senderId: String,
    val content: String
)

@Serializable
data class CreateRoleRequest(
    val name: String,
    val permissions: Long = 0,
    val color: Int? = null,
    val priorityLevel: Int = 0
)

@Serializable
data class UpdateChannelRequest(
    val name: String? = null,
    val type: ChannelType? = null,
    val topic: String? = null
)

@Serializable
data class AssignRoleRequest(
    val userId: String,
    val roleId: String
)

@Serializable
data class UpdateRoleRequest(
    val name: String? = null,
    val permissions: Long? = null,
    val color: Int? = null,
    val priorityLevel: Int? = null
)
