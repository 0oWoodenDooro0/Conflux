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
    data class Error(val message: String) : ConfluxEvent()
}
