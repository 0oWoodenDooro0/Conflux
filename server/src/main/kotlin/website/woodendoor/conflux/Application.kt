package website.woodendoor.conflux

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import website.woodendoor.conflux.auth.WebSocketAuthTokenManager
import website.woodendoor.conflux.controller.*
import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import website.woodendoor.conflux.database.repositories.*
import website.woodendoor.conflux.routes.*
import website.woodendoor.conflux.service.*
import website.woodendoor.conflux.service.impl.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import website.woodendoor.conflux.di.serverModule


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
        SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers, MemberRoles, ChannelPermissionOverrides, Messages)
        
        // Create a default user if it doesn't exist
        if (Users.selectAll().where { Users.id eq "default-user" }.empty()) {
            Users.insert {
                it[id] = "default-user"
                it[username] = "DefaultUser"
                it[discriminator] = "0000"
            }
        }
    }
    
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }

    val serverController by inject<ServerController>()
    val channelController by inject<ChannelController>()
    val roleController by inject<RoleController>()
    val chatController by inject<ChatController>()
    val userController by inject<UserController>()
    val tokenManager by inject<WebSocketAuthTokenManager>()
    val connectionManager by inject<WebSocketConnectionManager>()
    val channelRepository by inject<ChannelRepository>()
    val serverRepository by inject<ServerRepository>()


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
        messageRoutes(chatController)
        roleRoutes(roleController)
        userRoutes(userController)
        webSocketRoutes(tokenManager, connectionManager, channelRepository, serverRepository)
    }
}
