package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val id: String,
    val name: String,
    val ownerId: String,
    val icon: String? = null,
    val memberIds: List<String> = emptyList(),
    val roleIds: List<String> = emptyList()
)
