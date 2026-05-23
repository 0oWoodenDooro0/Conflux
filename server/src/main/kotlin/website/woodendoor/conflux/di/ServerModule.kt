package website.woodendoor.conflux.di

import org.koin.dsl.module
import website.woodendoor.conflux.auth.WebSocketAuthTokenManager
import website.woodendoor.conflux.controller.*
import website.woodendoor.conflux.database.repositories.*
import website.woodendoor.conflux.service.*
import website.woodendoor.conflux.service.impl.*
import website.woodendoor.conflux.WebSocketConnectionManager

val serverModule = module {
    // Repositories
    single<UserRepository> { ExposedUserRepository() }
    single<ServerRepository> { ExposedServerRepository(get()) }
    single<ChannelRepository> { ExposedChannelRepository() }
    single<MessageRepository> { ExposedMessageRepository() }

    // Managers
    single { WebSocketAuthTokenManager() }
    single { WebSocketConnectionManager() }

    // Services
    single<ChannelPermissionService> { ChannelPermissionServiceImpl(get(), get()) }
    single<ChannelService> { ChannelServiceImpl(get(), get()) }
    single<ServerService> { ServerServiceImpl(get(), get(), get()) }
    single<UserService> { UserServiceImpl(get()) }
    single<RoleService> { RoleServiceImpl(get()) }
    single<ChatService> { ChatServiceImpl(get()) }

    // Controllers
    single { ServerController(get(), get(), get()) }
    single { ChannelController(get(), get(), get(), get()) }
    single { RoleController(get(), get()) }
    single { ChatController(get(), get(), get(), get(), get()) }
    single { UserController(get()) }
}
