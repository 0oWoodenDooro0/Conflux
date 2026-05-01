package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateServerRequest(
    val name: String,
    val iconUrl: String? = null
)
