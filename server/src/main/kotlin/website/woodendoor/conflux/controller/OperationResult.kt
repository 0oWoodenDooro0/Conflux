package website.woodendoor.conflux.controller

sealed class OperationResult<out T> {
    data class Success<out T>(val data: T) : OperationResult<T>()
    
    sealed class Failure(val message: String) : OperationResult<Nothing>() {
        class NotFound(message: String) : Failure(message)
        class Unauthorized(message: String) : Failure(message)
        class Forbidden(message: String) : Failure(message)
        class BadRequest(message: String) : Failure(message)
        class InternalError(message: String) : Failure(message)
    }
}
