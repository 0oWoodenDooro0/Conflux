package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
enum class OnlineStatus {
    ONLINE,
    IDLE,
    DND,
    OFFLINE
}

@Serializable
data class User(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatar: String? = null,
    val permissions: Long = 0,
    val isOnline: Boolean = false,
    val statusMessage: String? = null,
    val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE
)
