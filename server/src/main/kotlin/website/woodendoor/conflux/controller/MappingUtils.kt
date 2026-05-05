package website.woodendoor.conflux.controller

import website.woodendoor.conflux.models.*

fun CreateServerRequest.toDomain(id: String) = Server(
    id = id,
    name = name,
    ownerId = ownerId,
    icon = iconUrl
)

fun CreateChannelRequest.toDomain(id: String, serverId: String) = Channel(
    id = id,
    serverId = serverId,
    name = name,
    type = type,
    topic = topic
)

fun CreateRoleRequest.toDomain(id: String, serverId: String) = Role(
    id = id,
    serverId = serverId,
    name = name,
    permissions = permissions,
    color = color,
    priorityLevel = priorityLevel
)
