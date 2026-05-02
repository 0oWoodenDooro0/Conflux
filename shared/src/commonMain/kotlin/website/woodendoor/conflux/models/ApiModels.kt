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
    val name: String
)
