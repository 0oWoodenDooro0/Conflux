package website.woodendoor.conflux.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respond(result: OperationResult<*>, successStatus: HttpStatusCode = HttpStatusCode.OK) {
    when (result) {
        is OperationResult.Success -> {
            if (result.data == Unit) {
                respond(successStatus)
            } else {
                respond(successStatus, result.data!!)
            }
        }
        is OperationResult.Failure.NotFound -> respond(HttpStatusCode.NotFound, result.message)
        is OperationResult.Failure.Unauthorized -> respond(HttpStatusCode.Unauthorized, result.message)
        is OperationResult.Failure.Forbidden -> respond(HttpStatusCode.Forbidden, result.message)
        is OperationResult.Failure.BadRequest -> respond(HttpStatusCode.BadRequest, result.message)
        is OperationResult.Failure.Conflict -> respond(HttpStatusCode.Conflict, result.message)
        is OperationResult.Failure.InternalError -> respond(HttpStatusCode.InternalServerError, result.message)
    }
}
