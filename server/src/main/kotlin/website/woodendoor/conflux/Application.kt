package website.woodendoor.conflux

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.controller.ChannelController
import website.woodendoor.conflux.controller.ChatController
import website.woodendoor.conflux.controller.RoleController
import website.woodendoor.conflux.controller.ServerController
import website.woodendoor.conflux.controller.UserController
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.database.repositories.ExposedChannelRepository
import website.woodendoor.conflux.database.repositories.ExposedServerRepository
import website.woodendoor.conflux.database.repositories.ExposedUserRepository
import website.woodendoor.conflux.database.repositories.ExposedMessageRepository
import website.woodendoor.conflux.routes.channelRoutes
import website.woodendoor.conflux.routes.serverRoutes
import website.woodendoor.conflux.routes.roleRoutes
import website.woodendoor.conflux.routes.userRoutes
import website.woodendoor.conflux.routes.messageRoutes
import website.woodendoor.conflux.routes.webSocketRoutes
import website.woodendoor.conflux.auth.WebSocketAuthTokenManager
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets)
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        anyHost() // In production, replace with specific hosts
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
    DatabaseFactory.init()
    transaction {
        SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, Messages)
        
        // Create a default user if it doesn't exist
        if (Users.selectAll().where { Users.id eq "default-user" }.empty()) {
            Users.insert {
                it[id] = "default-user"
                it[username] = "DefaultUser"
                it[discriminator] = "0000"
            }
        }
    }
    
    val userRepository = ExposedUserRepository()
    val serverRepository = ExposedServerRepository(userRepository)
    val channelRepository = ExposedChannelRepository()
    val messageRepository = ExposedMessageRepository()
    val tokenManager = WebSocketAuthTokenManager()
    val connectionManager = WebSocketConnectionManager()

    val serverController = ServerController(serverRepository, userRepository, channelRepository)
    val channelController = ChannelController(channelRepository, serverRepository, connectionManager)
    val roleController = RoleController(serverRepository)
    val chatController = ChatController(messageRepository, channelRepository, roleController, connectionManager)
    val userController = UserController(userRepository)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        get("/health") {
            try {
                DatabaseFactory.dbQuery {
                    Users.selectAll().limit(1).count()
                }
                call.respondText("OK")
            } catch (e: Exception) {
                call.respondText("Database connection failed: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        serverRoutes(serverController)
        channelRoutes(channelController, roleController)
        roleRoutes(roleController)
        userRoutes(userController)
        messageRoutes(chatController)
        webSocketRoutes(tokenManager, connectionManager)
    }
}
