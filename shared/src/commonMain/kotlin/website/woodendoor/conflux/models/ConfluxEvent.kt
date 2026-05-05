package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ConfluxEvent {
    @Serializable
    data object Connected : ConfluxEvent()

    @Serializable
    data class NewMessage(val message: Message) : ConfluxEvent()
    
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
    data class Error(val message: String) : ConfluxEvent()
}
