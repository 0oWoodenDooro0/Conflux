package website.woodendoor.conflux.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.ErrorResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun testSuccessResult() = runTest {
        val result = safeApiCall { "Success Data" }
        assertTrue(result is Result.Success)
        assertEquals("Success Data", result.data)
    }

    @Test
    fun testNetworkErrorResult() = runTest {
        val result = safeApiCall {
            throw kotlinx.io.IOException("No connection")
        }
        assertTrue(result is Result.Failure.NetworkError)
        assertEquals("Network error: No connection", result.message)
    }

    @Test
    fun testServerErrorResult() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = Json.encodeToString(ErrorResponse("Resource not found", "Details about missing resource")),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            expectSuccess = true
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        val result = safeApiCall {
            client.get("https://example.com/api/test")
        }

        assertTrue(result is Result.Failure.ServerError)
        assertEquals(404, result.statusCode)
        assertEquals("Resource not found", result.message)
    }

    @Test
    fun testUnknownErrorResult() = runTest {
        val result = safeApiCall {
            throw IllegalStateException("Something went wrong internally")
        }
        assertTrue(result is Result.Failure.UnknownError)
        assertEquals("Something went wrong internally", result.message)
    }
}
