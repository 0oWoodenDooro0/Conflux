package website.woodendoor.conflux.util

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import website.woodendoor.conflux.models.ErrorResponse

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    
    sealed interface Failure : Result<Nothing> {
        val message: String
        val throwable: Throwable?

        data class NetworkError(
            override val message: String,
            override val throwable: Throwable? = null
        ) : Failure

        data class ServerError(
            val statusCode: Int,
            override val message: String,
            override val throwable: Throwable? = null
        ) : Failure

        data class UnknownError(
            override val message: String,
            override val throwable: Throwable? = null
        ) : Failure
    }
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

inline fun <T> Result<T>.onFailure(action: (Result.Failure) -> Unit): Result<T> {
    if (this is Result.Failure) {
        action(this)
    }
    return this
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: ResponseException) {
        val errorResponse = try {
            e.response.body<ErrorResponse>()
        } catch (ignored: Exception) {
            null
        }
        val msg = errorResponse?.error ?: e.message ?: "Server returned error status code ${e.response.status.value}"
        Result.Failure.ServerError(e.response.status.value, msg, e)
    } catch (e: kotlinx.io.IOException) {
        Result.Failure.NetworkError("Network error: ${e.message ?: "Connection failed"}", e)
    } catch (e: Exception) {
        Result.Failure.UnknownError(e.message ?: "An unexpected error occurred", e)
    }
}
