package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}

@Serializable
data class Friendship(
    val id: String,
    val userId: String,
    val friendId: String,
    val status: FriendshipStatus,
    val friendUser: User? = null
)

@Serializable
data class FriendRequestPayload(
    val targetUsername: String,
    val targetDiscriminator: String? = null
)

@Serializable
data class OpenDmRequest(
    val targetUserId: String
)
