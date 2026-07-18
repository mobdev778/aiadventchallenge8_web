package com.github.mobdev778.aiadventchallenge.domain.mymcp

import com.github.mobdev778.aiadventchallenge.domain.mymcp.model.MyMcpServerState
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс, представляющий сервер MCP (MyMcpServer), позволяющий управлять его жизненным циклом
 * и отслеживать текущее состояние. Сервер может быть запущен и остановлен, а его состояние
 * транслируется через реактивный поток [observeState].
 */
interface MyMcpServer {

    /**
     * Возвращает холодный [Flow] с текущим и последующими значениями состояния сервера.
     *
     * @return поток состояний [MyMcpServerState], эмитирующий данные до тех пор, пока сборщик активен.
     */
    fun observeState(): Flow<MyMcpServerState>

    /**
     * Запускает сервер. Вызов является приостанавливаемым и возвращает управление
     * после успешного старта сервера. Может бросить исключение в случае ошибки инициализации.
     */
    suspend fun start()

    /**
     * Останавливает сервер. Вызов приостанавливается до полной остановки всех ресурсов
     * и компонентов сервера. Гарантирует корректное завершение работы.
     */
    suspend fun stop()
}
