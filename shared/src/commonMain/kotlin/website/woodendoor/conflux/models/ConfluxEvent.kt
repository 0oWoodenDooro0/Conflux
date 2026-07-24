package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ConfluxEvent {
    @Serializable
    data object Connected : ConfluxEvent()

    @Serializable
    data class NewMessage(val message: Message, val serverId: String) : ConfluxEvent()

    @Serializable
    data class NewDirectMessage(val message: Message, val channelId: String) : ConfluxEvent()
    
    @Serializable
    data class SubscriptionSuccess(val channelId: String) : ConfluxEvent()
    
    @Serializable
    data class ChannelCreated(val channel: Channel) : ConfluxEvent()

    @Serializable
    data class ChannelUpdated(val channel: Channel) : ConfluxEvent()

    @Serializable
    data class ChannelDeleted(val channelId: String, val serverId: String) : ConfluxEvent()

    @Serializable
    data class PermissionUpdate(val serverId: String, val roleId: String? = null, val userId: String? = null) : ConfluxEvent()
    
    @Serializable
    data class UserPresenceChanged(val userId: String, val isOnline: Boolean) : ConfluxEvent()

    @Serializable
    data class UserStatusChanged(val userId: String, val onlineStatus: OnlineStatus, val statusMessage: String? = null) : ConfluxEvent()

    @Serializable
    data class TypingStart(val channelId: String, val userId: String, val username: String) : ConfluxEvent()

    @Serializable
    data class TypingStop(val channelId: String, val userId: String) : ConfluxEvent()

    @Serializable
    data class FriendStatusUpdated(val friendship: Friendship) : ConfluxEvent()
    
    @Serializable
    data class Error(val message: String) : ConfluxEvent()
}
