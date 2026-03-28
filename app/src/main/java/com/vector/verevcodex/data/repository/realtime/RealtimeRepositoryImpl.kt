package com.vector.verevcodex.data.repository.realtime

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.vector.verevcodex.data.remote.auth.TokenStore
import com.vector.verevcodex.data.remote.core.BackendEndpoint
import com.vector.verevcodex.domain.model.realtime.MerchantRealtimeEvent
import com.vector.verevcodex.domain.model.realtime.RealtimeConnectionState
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import com.vector.verevcodex.domain.repository.realtime.RealtimeRepository
import com.vector.verevcodex.domain.repository.store.StoreRepository
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@Singleton
class RealtimeRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val tokenStore: TokenStore,
    private val gson: Gson,
    private val okHttpClient: OkHttpClient,
    backendEndpoint: BackendEndpoint,
) : RealtimeRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val eventFlow = MutableSharedFlow<MerchantRealtimeEvent>(extraBufferCapacity = 64)
    private val connectionState = MutableStateFlow<RealtimeConnectionState>(RealtimeConnectionState.Idle)
    private val websocketUrl = backendEndpoint.webSocketUrl
    private val stompHost = backendEndpoint.socketHost
    private val frameBuffer = StringBuilder()

    @Volatile
    private var activeContext: RealtimeSocketContext? = null

    @Volatile
    private var activeSocket: WebSocket? = null

    @Volatile
    private var activeSocketGeneration: Long = 0L

    private var reconnectJob: Job? = null

    init {
        combine(
            authRepository.observeSession(),
            storeRepository.observeSelectedStore().map { it?.id },
        ) { session, selectedStoreId ->
            val accessToken = tokenStore.getAccessToken()
            val organizationId = accessToken?.extractOrganizationId(gson)
            if (session == null || accessToken.isNullOrBlank()) {
                null
            } else {
                RealtimeSocketContext(
                    accessToken = accessToken,
                    userId = session.user.id,
                    organizationId = organizationId,
                    storeId = selectedStoreId,
                )
            }
        }.distinctUntilChanged()
            .onEach(::synchronizeContext)
            .launchIn(repositoryScope)
    }

    override fun observeEvents(): Flow<MerchantRealtimeEvent> = eventFlow

    override fun observeRefreshSignals(): Flow<Unit> = eventFlow.map { Unit }

    override fun observeConnectionState(): Flow<RealtimeConnectionState> = connectionState

    private suspend fun synchronizeContext(context: RealtimeSocketContext?) {
        if (context == activeContext) return
        reconnectJob?.cancel()
        activeContext = context
        if (context == null) {
            disconnect(expected = true)
            connectionState.value = RealtimeConnectionState.Idle
            return
        }
        openSocket(context)
    }

    private fun openSocket(context: RealtimeSocketContext) {
        disconnect(expected = true)
        frameBuffer.clear()
        connectionState.value = RealtimeConnectionState.Connecting
        val generation = ++activeSocketGeneration
        activeSocket = okHttpClient.newWebSocket(
            Request.Builder().url(websocketUrl).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (!isCurrent(generation, context)) {
                        webSocket.close(1000, "superseded")
                        return
                    }
                    sendConnectFrame(webSocket, context)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (!isCurrent(generation, context)) return
                    appendIncomingFrame(text, webSocket, context)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (!isCurrent(generation, context)) return
                    activeSocket = null
                    scheduleReconnect(context, "closed: $code $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (!isCurrent(generation, context)) return
                    activeSocket = null
                    scheduleReconnect(context, t.message ?: "connection_failed")
                }
            },
        )
    }

    private fun appendIncomingFrame(rawText: String, webSocket: WebSocket, context: RealtimeSocketContext) {
        frameBuffer.append(rawText)
        while (true) {
            val frameTerminator = frameBuffer.indexOf("\u0000")
            if (frameTerminator < 0) break
            val frame = frameBuffer.substring(0, frameTerminator)
            frameBuffer.delete(0, frameTerminator + 1)
            if (frame.isBlank()) continue
            handleFrame(frame, webSocket, context)
        }
    }

    private fun handleFrame(frame: String, webSocket: WebSocket, context: RealtimeSocketContext) {
        val normalized = frame.replace("\r\n", "\n")
        val separatorIndex = normalized.indexOf("\n\n")
        val headerBlock = if (separatorIndex >= 0) normalized.substring(0, separatorIndex) else normalized
        val body = if (separatorIndex >= 0) normalized.substring(separatorIndex + 2).trim() else ""
        val headerLines = headerBlock.lines().filter { it.isNotBlank() }
        if (headerLines.isEmpty()) return

        val command = headerLines.first()
        val headers = headerLines
            .drop(1)
            .mapNotNull { line ->
                val delimiter = line.indexOf(':')
                if (delimiter <= 0) null else line.substring(0, delimiter) to line.substring(delimiter + 1)
            }.toMap()

        when (command) {
            "CONNECTED" -> {
                connectionState.value = RealtimeConnectionState.Connected
                subscribeToTopics(webSocket, context)
            }
            "MESSAGE" -> {
                val event = runCatching { gson.fromJson(body, RealtimeEventDto::class.java) }.getOrNull() ?: return
                eventFlow.tryEmit(
                    MerchantRealtimeEvent(
                        eventId = event.eventId.orEmpty(),
                        eventType = event.eventType.orEmpty(),
                        scope = event.scope.orEmpty(),
                        payload = event.payload ?: emptyMap(),
                        createdAt = event.createdAt.orEmpty(),
                        destination = headers["destination"],
                    ),
                )
            }
            "ERROR" -> {
                scheduleReconnect(context, headers["message"] ?: body.ifBlank { "stomp_error" })
            }
        }
    }

    private fun subscribeToTopics(webSocket: WebSocket, context: RealtimeSocketContext) {
        val subscriptions = buildList {
            add("user-${context.userId}" to "/topic/user.${context.userId}")
            context.organizationId?.takeIf { it.isNotBlank() }?.let { organizationId ->
                add("org-$organizationId" to "/topic/org.$organizationId")
            }
            context.storeId?.takeIf { it.isNotBlank() }?.let { storeId ->
                add("store-$storeId" to "/topic/store.$storeId")
            }
        }
        subscriptions.forEach { (id, destination) ->
            sendFrame(
                webSocket = webSocket,
                command = "SUBSCRIBE",
                headers = mapOf(
                    "id" to id,
                    "destination" to destination,
                    "ack" to "auto",
                ),
            )
        }
    }

    private fun sendConnectFrame(webSocket: WebSocket, context: RealtimeSocketContext) {
        sendFrame(
            webSocket = webSocket,
            command = "CONNECT",
            headers = mapOf(
                "accept-version" to "1.2",
                "host" to stompHost,
                "Authorization" to "Bearer ${context.accessToken}",
                "heart-beat" to "0,0",
            ),
        )
    }

    private fun sendFrame(
        webSocket: WebSocket,
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
    ) {
        val frame = buildString {
            append(command).append('\n')
            headers.forEach { (key, value) ->
                append(key).append(':').append(value).append('\n')
            }
            append('\n')
            if (body.isNotEmpty()) append(body)
            append('\u0000')
        }
        webSocket.send(frame)
    }

    private fun scheduleReconnect(context: RealtimeSocketContext, reason: String) {
        reconnectJob?.cancel()
        connectionState.value = RealtimeConnectionState.Failed(reason)
        reconnectJob = repositoryScope.launch {
            connectionState.value = RealtimeConnectionState.Disconnected(retrying = true)
            delay(RECONNECT_DELAY_MS)
            if (activeContext == context) {
                openSocket(context)
            }
        }
    }

    private fun disconnect(expected: Boolean) {
        activeSocket?.close(1000, if (expected) "context_changed" else "disconnect")
        activeSocket = null
    }

    private fun isCurrent(generation: Long, context: RealtimeSocketContext): Boolean =
        activeSocketGeneration == generation && activeContext == context

    private data class RealtimeSocketContext(
        val accessToken: String,
        val userId: String,
        val organizationId: String?,
        val storeId: String?,
    )

    private data class RealtimeEventDto(
        @SerializedName("eventId") val eventId: String?,
        @SerializedName("eventType") val eventType: String?,
        @SerializedName("scope") val scope: String?,
        @SerializedName("payload") val payload: Map<String, Any?>?,
        @SerializedName("createdAt") val createdAt: String?,
    )

    private companion object {
        const val RECONNECT_DELAY_MS = 2_500L
    }
}

internal fun String.extractOrganizationId(gson: Gson): String? {
    val payloadSegment = split('.').getOrNull(1) ?: return null
    val payloadJson = runCatching {
        val decoded = Base64.getUrlDecoder().decode(payloadSegment)
        String(decoded, StandardCharsets.UTF_8)
    }.getOrNull() ?: return null
    return runCatching { gson.fromJson(payloadJson, JwtPayload::class.java)?.organizationId }.getOrNull()
}

private data class JwtPayload(
    @SerializedName("organization_id") val organizationId: String?,
)
