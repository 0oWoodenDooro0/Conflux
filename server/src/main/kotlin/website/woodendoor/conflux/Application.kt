package website.woodendoor.conflux

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import website.woodendoor.conflux.database.DatabaseFactory
import website.woodendoor.conflux.database.models.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    transaction {
        SchemaUtils.create(Users, Servers, Roles, Channels, ServerMembers)
    }
    
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}