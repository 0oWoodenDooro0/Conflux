package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import website.woodendoor.conflux.models.*

class ServerApiClient(
    private val client: HttpClient,
    private val baseUrl: String
) {
    constructor(baseUrl: String) : this(HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }, baseUrl)

    suspend fun login(username: String): User {
        return client.post("$baseUrl/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username))
        }.body()
    }

    suspend fun getServers(userId: String): List<Server> {
        return client.get("$baseUrl/api/servers") {
            parameter("userId", userId)
        }.body()
    }

    suspend fun createServer(name: String, iconUrl: String?, ownerId: String): Server {
        return client.post("$baseUrl/api/servers") {
            contentType(ContentType.Application.Json)
            setBody(CreateServerRequest(name, iconUrl, ownerId))
        }.body()
    }

    suspend fun joinServer(serverId: String, userId: String): Boolean {
        val response = client.post("$baseUrl/api/servers/$serverId/join") {
            parameter("userId", userId)
        }
        return response.status.isSuccess()
    }

    suspend fun createChannel(serverId: String, name: String, userId: String): Channel {
        return client.post("$baseUrl/api/servers/$serverId/channels") {
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
            setBody(CreateChannelRequest(name))
        }.body()
    }

    suspend fun getChannels(serverId: String): List<Channel> {
        return client.get("$baseUrl/api/servers/$serverId/channels").body()
    }

    suspend fun getMessages(channelId: String): List<Message> {
        return client.get("$baseUrl/api/channels/$channelId/messages").body()
    }

    suspend fun sendMessage(channelId: String, senderId: String, content: String): Message {
        return client.post("$baseUrl/api/channels/$channelId/messages") {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(senderId, content))
        }.body()
    }

    suspend fun getWsToken(userId: String): String {
        val response: Map<String, String> = client.post("$baseUrl/api/auth/ws-token") {
            setBody(userId)
        }.body()
        return response["token"] ?: throw Exception("Token not found in response")
    }

    // Role Management API
    suspend fun getRoles(serverId: String): List<Role> {
        return client.get("$baseUrl/api/servers/$serverId/roles").body()
    }

    suspend fun createRole(serverId: String, userId: String, name: String, permissions: Long = 0, color: Int? = null, priority: Int = 0): Role {
        return client.post("$baseUrl/api/servers/$serverId/roles") {
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
            setBody(CreateRoleRequest(name, permissions, color, priority))
        }.body()
    }

    suspend fun assignRole(serverId: String, adminUserId: String, targetUserId: String, roleId: String): Boolean {
        val response = client.post("$baseUrl/api/servers/$serverId/roles/assign") {
            parameter("userId", adminUserId)
            contentType(ContentType.Application.Json)
            setBody(AssignRoleRequest(targetUserId, roleId))
        }
        return response.status.isSuccess()
    }

    fun close() {
        client.close()
    }
}
