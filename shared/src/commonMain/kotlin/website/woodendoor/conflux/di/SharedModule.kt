package website.woodendoor.conflux.di

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module
import website.woodendoor.conflux.api.ServerApiClient
import website.woodendoor.conflux.DEFAULT_BASE_URL

val sharedModule = module {
    single {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    single { ServerApiClient(get(), DEFAULT_BASE_URL) }
}
