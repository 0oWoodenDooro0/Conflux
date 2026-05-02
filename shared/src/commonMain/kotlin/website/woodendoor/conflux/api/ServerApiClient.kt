package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import website.woodendoor.conflux.models.Channel
import website.woodendoor.conflux.models.CreateChannelRequest
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server
import website.woodendoor.conflux.models.LoginRequest
import website.woodendoor.conflux.models.User

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

    suspend fun createChannel(serverId: String, name: String): Channel {
        return client.post("$baseUrl/api/servers/$serverId/channels") {
            contentType(ContentType.Application.Json)
            setBody(CreateChannelRequest(name))
        }.body()
    }

    suspend fun getChannels(serverId: String): List<Channel> {
        return client.get("$baseUrl/api/servers/$serverId/channels").body()
    }

    fun close() {
        client.close()
    }
}
