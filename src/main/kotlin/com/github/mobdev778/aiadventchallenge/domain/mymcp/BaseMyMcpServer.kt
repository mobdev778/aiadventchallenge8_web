package com.github.mobdev778.aiadventchallenge.domain.mymcp

import com.github.mobdev778.aiadventchallenge.domain.mymcp.model.MyMcpServerState
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * Абстрактная базовая реализация сервера MCP, использующая встроенный HTTP-сервер на основе Netty
 * с транспортом Server-Sent Events (SSE).
 * Предоставляет базовый жизненный цикл (запуск, остановка) и поток состояния сервера.
 *
 * Подклассы должны реализовать метод [createServer], определяющий логику MCP-сервера.
 *
 * @param name Название сервера.
 * @param description Описание сервера.
 * @param port Порт, на котором будет запущен HTTP-сервер.
 * @param launchAtStartup Флаг запуска сервера при старте приложения.
 */
abstract class BaseMyMcpServer(
    private val name: String,
    private val description: String,
    private val port: Int,
    val launchAtStartup: Boolean,
) : MyMcpServer {

    /**
     * Встроенный HTTP-сервер Netty. `null`, если сервер не запущен.
     */
    protected var engine: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    private val state = MutableStateFlow(
        MyMcpServerState(
            name = name,
            description = description,
            isRunning = launchAtStartup,
            url = "http://localhost:$port/sse",
            launchAtStartup = launchAtStartup,
        )
    )

    init {
        if (launchAtStartup) {
            runBlocking(kotlin.coroutines.EmptyCoroutineContext) {
                start()
            }
        }
    }

    /**
     * Возвращает поток [MyMcpServerState], отражающий текущее состояние сервера и его изменения.
     * Состояние обновляется при запуске и остановке.
     *
     * @return холодный [Flow] с текущим и последующими значениями состояния.
     */
    override fun observeState(): Flow<MyMcpServerState> {
        return state
    }

    /**
     * Запускает HTTP-сервер и MCP-обработчик, если сервер ещё не запущен.
     * После успешного старта обновляет состояние, помечая сервер как работающий.
     */
    override suspend fun start() {
        if (engine != null) return

        val newEngine = embeddedServer(Netty, port = port) {
            configureMyMcpServer()
        }
        newEngine.start(wait = false)
        engine = newEngine
        state.update {
            it.copy(isRunning = true)
        }
    }

    /**
     * Останавливает HTTP-сервер с заданными периодами ожидания.
     * Обновляет состояние, сбрасывая флаг работы и очищая ссылку на движок.
     */
    override suspend fun stop() {
        engine?.stop(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
        engine = null
        state.update {
            it.copy(isRunning = false)
        }
    }

    private fun Application.configureMyMcpServer() {
        install(ContentNegotiation) {
            json(McpJson)
        }
        install(SSE)

        val transports = ConcurrentHashMap<String, SseServerTransport>()

        routing {
            route("/sse") {
                sse {
                    val transport = SseServerTransport("/sse", this, 4L * 1024 * 1024)
                    transports[transport.sessionId] = transport

                    val server = createServer()
                    server.onClose {
                        transports.remove(transport.sessionId)
                    }
                    server.createSession(transport)

                    awaitCancellation()
                }

                post {
                    val sessionId = call.parameters["sessionId"]
                    if (sessionId == null) {
                        call.respond(HttpStatusCode.BadRequest, "sessionId query parameter is not provided")
                        return@post
                    }
                    val transport = transports[sessionId]
                    if (transport == null) {
                        call.respond(HttpStatusCode.NotFound, "Session not found")
                        return@post
                    }
                    transport.handlePostMessage(call)
                }
            }
        }
    }

    /**
     * Создаёт экземпляр MCP-сервера, который будет зарегистрирован на HTTP-маршруте.
     * Вызывается при конфигурации приложения.
     *
     * @return экземпляр [Server] MCP.
     */
    protected abstract fun createServer(): Server

    /**
     * Returns the MCP [Server] instance, creating it if necessary.
     * Intended for testing and internal use.
     */
    internal fun getServer(): Server = createServer()

    /**
     * Константы для конфигурации остановки сервера.
     */
    private companion object {
        /**
         * Время ожидания (в миллисекундах) перед принудительной остановкой после запроса на остановку.
         */
        const val STOP_GRACE_PERIOD_MS = 2000L

        /**
         * Максимальное время ожидания (в миллисекундах) завершения остановки.
         */
        const val STOP_TIMEOUT_MS = 3000L
    }
}
