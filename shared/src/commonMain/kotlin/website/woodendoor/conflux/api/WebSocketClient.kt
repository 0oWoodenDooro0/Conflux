package website.woodendoor.conflux.api

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import website.woodendoor.conflux.models.ConfluxEvent
import kotlin.math.min
import kotlin.math.pow

class WebSocketClient(
    private val client: HttpClient,
    private val baseUrl: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private var session: DefaultClientWebSocketSession? = null
    private val _events = MutableSharedFlow<ConfluxEvent>()
    val events: SharedFlow<ConfluxEvent> = _events.asSharedFlow()

    private var connectionJob: Job? = null
    private var isClosedManually = false
    private var lastToken: String? = null
    private val subscribedChannels = mutableSetOf<String>()
    private var subscribedServer: String? = null

    fun connect(token: String) {
        lastToken = token
        isClosedManually = false
        connectionJob?.cancel()
        connectionJob = scope.launch {
            var attempt = 0
            while (isActive && !isClosedManually) {
                try {
                    val wsUrl = baseUrl.replace("http", "ws") + "/ws?token=$token"
                    session = client.webSocketSession(wsUrl)
                    attempt = 0 // Reset attempt count on success
                    
                    _events.emit(ConfluxEvent.Connected)

                    // Re-subscribe
                    subscribedServer?.let { subscribeServer(it) }
                    subscribedChannels.forEach { subscribe(it) }

                    for (frame in session!!.incoming) {
                        if (frame is Frame.Text) {
                            val event = Json.decodeFromString<ConfluxEvent>(frame.readText())
                            _events.emit(event)
                        }
                    }
                } catch (e: Exception) {
                    if (!isClosedManually) {
                        _events.emit(ConfluxEvent.Error("Connection lost: ${e.message}. Retrying..."))
                    }
                } finally {
                    session = null
                }

                if (!isClosedManually) {
                    val delayMs = min(2.0.pow(attempt).toLong() * 1000, 30000L)
                    delay(delayMs)
                    attempt++
                }
            }
        }
    }

    suspend fun subscribe(channelId: String) {
        subscribedChannels.add(channelId)
        session?.send(Frame.Text("subscribe:$channelId"))
    }

    suspend fun subscribeServer(serverId: String) {
        subscribedServer = serverId
        session?.send(Frame.Text("subscribe_server:$serverId"))
    }

    fun close() {
        isClosedManually = true
        connectionJob?.cancel()
        scope.launch {
            session?.close()
            session = null
        }
    }
}
