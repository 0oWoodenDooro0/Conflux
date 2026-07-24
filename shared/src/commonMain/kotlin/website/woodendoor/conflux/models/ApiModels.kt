package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String = ""
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)

@Serializable
data class UpdateStatusRequest(
    val onlineStatus: OnlineStatus,
    val statusMessage: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val avatar: String? = null,
    val statusMessage: String? = null
)

@Serializable
data class CreateServerRequest(
    val name: String,
    val ownerId: String,
    val icon: String? = null,
    val description: String? = null
)

@Serializable
data class JoinByInviteRequest(
    val inviteCode: String
)

@Serializable
data class CreateChannelRequest(
    val name: String,
    val type: ChannelType = ChannelType.TEXT,
    val topic: String? = null,
    val categoryId: String? = null,
    val position: Int = 0
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
    val topic: String? = null,
    val categoryId: String? = null,
    val position: Int? = null
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

@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null
)
