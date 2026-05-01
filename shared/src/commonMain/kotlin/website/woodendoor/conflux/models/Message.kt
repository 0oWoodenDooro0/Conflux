package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val channelId: String,
    val authorId: String,
    val content: String,
    val timestamp: Long,
    val attachments: List<String> = emptyList()
)
