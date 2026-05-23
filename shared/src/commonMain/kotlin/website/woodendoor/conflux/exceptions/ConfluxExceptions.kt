package website.woodendoor.conflux.exceptions

sealed class ConfluxException(
    override val message: String,
    val statusCode: Int,
    val details: String? = null
) : RuntimeException(message)

class UserNotFoundException(message: String, details: String? = null) : 
    ConfluxException(message, 404, details)

class ServerNotFoundException(message: String, details: String? = null) : 
    ConfluxException(message, 404, details)

class ChannelNotFoundException(message: String, details: String? = null) : 
    ConfluxException(message, 404, details)

class RoleNotFoundException(message: String, details: String? = null) : 
    ConfluxException(message, 404, details)

class UnauthorizedException(message: String, details: String? = null) : 
    ConfluxException(message, 401, details)

class ForbiddenException(message: String, details: String? = null) : 
    ConfluxException(message, 403, details)

class BadRequestException(message: String, details: String? = null) : 
    ConfluxException(message, 400, details)

class ConflictException(message: String, details: String? = null) : 
    ConfluxException(message, 409, details)

class InternalServerErrorException(message: String, details: String? = null) : 
    ConfluxException(message, 500, details)
