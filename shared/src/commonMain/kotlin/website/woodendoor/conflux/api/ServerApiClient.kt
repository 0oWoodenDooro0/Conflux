package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import website.woodendoor.conflux.models.CreateServerRequest
import website.woodendoor.conflux.models.Server

class ServerApiClient(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) {
    constructor(baseUrl: String = "http://localhost:8080") : this(HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }, baseUrl)

    suspend fun createServer(name: String, iconUrl: String?): Server {
        return client.post("$baseUrl/api/servers") {
            contentType(ContentType.Application.Json)
            setBody(CreateServerRequest(name, iconUrl))
        }.body()
    }
}
