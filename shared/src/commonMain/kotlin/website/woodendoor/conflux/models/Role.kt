package website.woodendoor.conflux.models

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: String,
    val name: String,
    val permissions: Long,
    val color: Int? = null,
    val priorityLevel: Int = 0
)
