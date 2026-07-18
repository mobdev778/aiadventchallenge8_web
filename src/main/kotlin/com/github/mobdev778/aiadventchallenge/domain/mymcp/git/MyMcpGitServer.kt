package com.github.mobdev778.aiadventchallenge.domain.mymcp.git

import com.github.mobdev778.aiadventchallenge.domain.mymcp.BaseMyMcpServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.stereotype.Component
import java.io.File

/**
 * Локальный MCP-сервер, предоставляющий AI-агенту инструменты для работы с Git
 * в контексте текущего проекта IntelliJ IDEA.
 *
 * Сервер работает поверх протокола [Model Context Protocol](https://modelcontextprotocol.io)
 * и предоставляет пять инструментов: "git status", "git diff", "git log", "git branch",
 * "git checkout".
 * Каждый инструмент выполняет соответствующую shell-команду `git` в корневом каталоге
 * проекта
 *
 * Наследуется от [BaseMyMcpServer], используя общую инфраструктуру запуска и конфигурации
 * MCP-серверов.
 *
 * @property gitProjectDir путь к текущему проекту
 */
@Component
class MyMcpGitServer : BaseMyMcpServer(
    name = "MyMcpGitServer",
    description = "Локальный MCP-сервер для выполнения Git-команд в корне проекта",
    port = 3007,
    launchAtStartup = true,
) {

    private val gitProjectDir: String = System.getenv("GIT_PROJECT_DIRECTORY")
        ?: "/home/ruslan/AI/AIAdventChallenge8/GitHub/aiadventchallenge8"

    /**
     * Создаёт и конфигурирует экземпляр [Server] — ядро MCP-сервера.
     *
     * В процессе создания:
     * - Устанавливаются метаданные сервера (название, версия).
     * - Объявляются поддерживаемые возможности (tools).
     * - Регистрируются пять инструментов:
     *   - `git status` — статус рабочего дерева.
     *   - `git diff` — различия между рабочей директорией и индексом.
     *   - `git log` — история коммитов.
     *   - `git branch` — список веток.
     *   - `git checkout` — переключение на ветку или коммит.
     * - Задаются инструкции для AI-ассистента.
     *
     * @return Сконфигурированный и готовый к запуску экземпляр [Server].
     */
    override fun createServer(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "my-mcp-git-server",
                version = "1.0.0",
            ),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = false),
                ),
            ),
            instructions = "Local MCP server for Git operations in the current project. " +
                    "CRITICAL RULES FOR THE ASSISTANT:\n" +
                    "1. Use these tools to inspect the Git state of the current project.\n" +
                    "2. The tools run in the project root directory automatically.\n" +
                    "3. Results are returned as plain text (stdout and stderr combined).",
        )

        addGitStatusTool(server)
        addGitDiffTool(server)
        addGitLogTool(server)
        addGitBranchTool(server)
        addGitCheckoutTool(server)

        return server
    }

    /**
     * Регистрирует инструмент `git status` — отображает состояние рабочего дерева.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGitStatusTool(server: Server) {
        server.addTool(
            name = "git_status",
            description = "Shows the working tree status: staged, unstaged, and untracked files.",
            inputSchema = ToolSchema(
                properties = buildJsonObject { },
                required = emptyList(),
            ),
        ) { request: CallToolRequest ->
            runBlocking(Dispatchers.IO) {
                textResult(executeGitCommand("status"))
            }
        }
    }

    /**
     * Регистрирует инструмент `git diff` — показывает различия между рабочей директорией и индексом.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGitDiffTool(server: Server) {
        server.addTool(
            name = "git_diff",
            description = "Shows changes between commits, branches, working directory, etc. " +
                    "By default (no args) shows unstaged changes in the working tree. " +
                    "Use the 'args' parameter to pass additional git diff options and arguments. " +
                    "Examples of args values:\n" +
                    "- 'day_32 day_29' — diff between two branches: day_32 and day_29",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("args", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive(
                                "Additional git diff arguments (space-separated). " +
                                        "Examples: '--stat', '--name-status', " +
                                        "'--stat day_28..day_31', '--cached', 'HEAD~3..HEAD'. " +
                                        "Leave empty to show unstaged changes."
                            )
                        )
                    })
                },
                required = emptyList(),
            ),
        ) { request: CallToolRequest ->
            val args = request.params.arguments
                ?.get("args")
                ?.jsonPrimitive
                ?.content
                ?.takeIf { it.isNotBlank() }
            println("!!! args: $args")

            var filteredArgs = args?.split(" ")
                ?.filter { it.isNotBlank() }
                ?.filter { it != "--branch" } // спец-хак для Qwen 3 1.7B, которая делает неправильную команду
                .orEmpty()
            println("!!! filteredArgs: $filteredArgs")

            // спецхак для глючной Qwen 3 1.7B:
            if (filteredArgs.size == 1 && filteredArgs[0].contains("..")) {
                filteredArgs = listOf(
                    filteredArgs[0].substringBefore(".."),
                    filteredArgs[0].substringAfter("..")
                )
            }

            val commandArgs = if (filteredArgs.isNotEmpty()) {
                listOf("diff") + filteredArgs
            } else {
                listOf("diff")
            }
            println("!!! commandArgs: $commandArgs")

            runBlocking(Dispatchers.IO) {
                textResult(executeGitCommand(*commandArgs.toTypedArray()))
            }
        }
    }

    /**
     * Регистрирует инструмент `git log` — показывает историю коммитов.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGitLogTool(server: Server) {
        server.addTool(
            name = "git_log",
            description = "Shows the commit logs. Returns the last 20 commits by default.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("count", buildJsonObject {
                        put("type", JsonPrimitive("integer"))
                        put(
                            "description",
                            JsonPrimitive("Number of recent commits to show (default: 20)")
                        )
                    })
                },
                required = emptyList(),
            ),
        ) { request: CallToolRequest ->
            val count = request.params.arguments
                ?.get("count")
                ?.jsonPrimitive
                ?.content
                ?.toIntOrNull()
                ?: 20
            runBlocking(Dispatchers.IO) {
                textResult(executeGitCommand("log", "-n", count.toString(), "--oneline"))
            }
        }
    }

    /**
     * Регистрирует инструмент `git branch` — показывает список локальных веток.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGitBranchTool(server: Server) {
        server.addTool(
            name = "git_branch",
            description = "Lists all local branches. The current branch is marked with an asterisk.",
            inputSchema = ToolSchema(
                properties = buildJsonObject { },
                required = emptyList(),
            ),
        ) { request: CallToolRequest ->
            runBlocking(Dispatchers.IO) {
                textResult(executeGitCommand("branch"))
            }
        }
    }

    /**
     * Регистрирует инструмент `git checkout` — переключается на указанную ветку или коммит.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGitCheckoutTool(server: Server) {
        server.addTool(
            name = "git_checkout",
            description = "Switches to the specified branch or commit (by name or hash). " +
                    "WARNING: This modifies the working tree. " +
                    "Use 'git_status' before and after to verify the result.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("ref", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive(
                                "Branch name or commit hash to checkout. " +
                                        "Examples: 'main', 'feature-branch', 'abc1234'"
                            )
                        )
                    })
                },
                required = listOf("ref"),
            ),
        ) { request: CallToolRequest ->
            val ref = request.params.arguments
                ?.get("ref")
                ?.jsonPrimitive
                ?.content
                ?: return@addTool textResult("Error: 'ref' parameter is required.")
            runBlocking(Dispatchers.IO) {
                textResult(executeGitCommand("checkout", ref))
            }
        }
    }

    /**
     * Выполняет заданную Git-команду в корневом каталоге текущего проекта.
     *
     * Алгоритм работы:
     * 1. Получает базовый путь проекта из [ProjectContainer].
     *    Если проект недоступен, возвращает сообщение об ошибке.
     * 2. Запускает процесс `git` с переданными аргументами через [ProcessBuilder].
     * 3. Читает stdout и stderr процесса.
     * 4. Возвращает объединённый вывод (stdout + stderr) в виде текстовой строки.
     *
     * @param args Аргументы Git-команды (например, "status", "diff", "log -n 10 --oneline").
     * @return Текстовый вывод выполненной команды или сообщение об ошибке.
     */
    private fun executeGitCommand(vararg args: String): String {
        val projectPath = gitProjectDir

        println("!!! MyMcpGitServer: executing 'git ${args.joinToString(" ")}' in '$projectPath'")

        return try {
            val process = ProcessBuilder()
                .command(listOf("git") + args.toList())
                .directory(File(projectPath))
                .redirectErrorStream(false)
                .start()

            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            val result = buildString {
                if (stdout.isNotBlank()) {
                    append(stdout.trimEnd())
                }
                if (stderr.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("[stderr]\n")
                    append(stderr.trimEnd())
                }
            }

            if (result.isEmpty()) {
                "(exit code: $exitCode, no output)"
            } else {
                result
            }
        } catch (e: Exception) {
            println("!!! MyMcpGitServer: Error executing git ${args.joinToString(" ")}: ${e.message}")
            "Error: Failed to execute 'git ${args.joinToString(" ")}': ${e.message}"
        }
    }

    /**
     * Оборачивает текстовую строку в [CallToolResult], пригодный для возврата MCP-клиенту.
     *
     * @param text Текстовое содержимое результата.
     * @return Объект [CallToolResult] с единственным элементом [TextContent] и флагом `isError = false`.
     */
    private fun textResult(text: String): CallToolResult =
        CallToolResult(
            content = listOf(TextContent(text = text)),
            isError = false,
        )
}
