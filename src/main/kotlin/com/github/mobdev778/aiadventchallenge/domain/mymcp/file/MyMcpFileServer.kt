package com.github.mobdev778.aiadventchallenge.domain.mymcp.file

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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.stereotype.Component
import java.io.File

/**
 * Локальный MCP-сервер, предоставляющий AI-агенту инструменты для навигации по файловой
 * структуре проекта и чтения содержимого файлов в контексте текущего проекта IntelliJ IDEA.
 *
 * Сервер работает поверх протокола [Model Context Protocol](https://modelcontextprotocol.io)
 * и предоставляет два инструмента: "project_tree" и "read_file".
 * Каждый инструмент оперирует относительными путями (относительно корня проекта),
 *
 * Наследуется от [BaseMyMcpServer], используя общую инфраструктуру запуска и конфигурации
 * MCP-серверов.
 */
@Component
class MyMcpFileServer : BaseMyMcpServer(
    name = "MyMcpFileServer",
    description = "Локальный MCP-сервер для навигации по файловой структуре проекта и чтения файлов",
    port = 3008,
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
     * - Регистрируются два инструмента:
     *   - `project_tree` — возвращает дерево файлов проекта в виде JSON-структуры.
     *   - `read_file` — возвращает содержимое файла проекта в виде текста.
     * - Задаются инструкции для AI-ассистента.
     *
     * @return Сконфигурированный и готовый к запуску экземпляр [Server].
     */
    override fun createServer(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "my-mcp-project-server",
                version = "1.0.0",
            ),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = false),
                ),
            ),
            instructions = "Local MCP server for project file navigation and reading. " +
                    "CRITICAL RULES FOR THE ASSISTANT:\n" +
                    "1. Use 'project_tree' to explore the project file structure.\n" +
                    "2. Use 'read_file' to read the contents of a specific file.\n" +
                    "3. All paths are relative to the project root directory.\n" +
                    "4. 'project_tree' returns a JSON structure with 'name', 'type' (file/directory), " +
                    "and 'children' (for directories).",
        )

        addProjectTreeTool(server)
        addReadFileTool(server)

        return server
    }

    /**
     * Регистрирует инструмент `project_tree` — возвращает дерево файлов и директорий
     * в заданной точке проекта в виде JSON-структуры.
     *
     * JSON-структура узла:
     * - `name` (string) — имя файла или директории.
     * - `type` (string) — `"file"` или `"directory"`.
     * - `children` (array, только для директорий) — список дочерних узлов.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addProjectTreeTool(server: Server) {
        server.addTool(
            name = "project_tree",
            description = "Returns the file tree at a given relative path as a JSON structure. " +
                    "Each node has 'name' (string), 'type' ('file' or 'directory'), " +
                    "and 'children' (array of nodes, only for directories).",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("path", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive(
                                "Relative path from the project root. Use '.' or empty string for the root."
                            )
                        )
                    })
                },
                required = listOf("path"),
            ),
        ) { request: CallToolRequest ->
            val relativePath = request.params.arguments
                ?.get("path")
                ?.jsonPrimitive
                ?.content
                ?: "."
            runBlocking(Dispatchers.IO) {
                textResult(buildProjectTree(relativePath))
            }
        }
    }

    /**
     * Регистрирует инструмент `read_file` — возвращает содержимое файла проекта в виде текста.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addReadFileTool(server: Server) {
        server.addTool(
            name = "read_file",
            description = "Reads the content of a project file and returns it as plain text.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("path", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive("Relative path to the file from the project root.")
                        )
                    })
                },
                required = listOf("path"),
            ),
        ) { request: CallToolRequest ->
            val relativePath = request.params.arguments
                ?.get("path")
                ?.jsonPrimitive
                ?.content
                ?: return@addTool textResult("Error: 'path' parameter is required.")
            runBlocking(Dispatchers.IO) {
                textResult(readFileContent(relativePath))
            }
        }
    }

    /**
     * Возвращает абсолютный путь к корню проекта.
     *
     * @return Абсолютный путь к корню проекта или `null`, если проект недоступен.
     */
    private fun getProjectRootPath(): String? = gitProjectDir

    /**
     * Строит JSON-дерево файловой структуры, начиная с заданного относительного пути.
     *
     * Алгоритм:
     * 1. Получает базовый путь проекта через [ProjectContainer].
     * 2. Разрешает целевой путь относительно корня проекта.
     * 3. Рекурсивно обходит директории, строя JSON-представление.
     * 4. Возвращает результат в виде форматированной JSON-строки.
     *
     * @param relativePath Относительный путь от корня проекта.
     * @return JSON-строка с деревом файлов или сообщение об ошибке.
     */
    private fun buildProjectTree(relativePath: String): String {
        val projectPath = getProjectRootPath()
            ?: return "Error: No project is currently open. Unable to determine the project root directory."

        val targetDir = if (relativePath == "." || relativePath.isBlank()) {
            File(projectPath)
        } else {
            File(projectPath, relativePath)
        }

        if (!targetDir.exists()) {
            return "Error: Path '$relativePath' does not exist in the project."
        }

        if (!targetDir.isDirectory) {
            return "Error: Path '$relativePath' is not a directory. Use 'read_file' to read file contents."
        }

        return try {
            val treeJson = buildTreeNode(targetDir, projectPath)
            treeJson.toString()
        } catch (e: Exception) {
            "Error: Failed to build project tree for '$relativePath': ${e.message}"
        }
    }

    /**
     * Рекурсивно строит JSON-узел для заданного файла или директории.
     *
     * Для директорий список `children` включает все дочерние элементы,
     * отсортированные по алфавиту (директории перед файлами).
     * Максимальная глубина рекурсии ограничена 10 уровнями для предотвращения
     * зацикливания на симлинках и чрезмерно больших ответов.
     *
     * @param file Файл или директория для представления в виде JSON-узла.
     * @param projectRoot Абсолютный путь к корню проекта (для вычисления относительных путей).
     * @param depth Текущая глубина рекурсии (по умолчанию 0).
     * @return [JsonObject] — JSON-представление узла.
     */
    private fun buildTreeNode(file: File, projectRoot: String, depth: Int = 0): JsonObject {
        val maxDepth = 10

        return if (file.isDirectory) {
            val children = if (depth < maxDepth) {
                file.listFiles()
                    ?.sortedWith(compareBy<File> { if (it.isDirectory) 0 else 1 }.thenBy { it.name })
                    ?.map { child -> buildTreeNode(child, projectRoot, depth + 1) }
                    ?: emptyList()
            } else {
                emptyList()
            }

            buildJsonObject {
                put("name", JsonPrimitive(file.name))
                put("type", JsonPrimitive("directory"))
                put("children", JsonArray(children))
            }
        } else {
            buildJsonObject {
                put("name", JsonPrimitive(file.name))
                put("type", JsonPrimitive("file"))
            }
        }
    }

    /**
     * Читает содержимое файла проекта и возвращает его в виде текстовой строки.
     *
     * Алгоритм:
     * 1. Получает базовый путь проекта через [ProjectContainer].
     * 2. Разрешает целевой файл относительно корня проекта.
     * 3. Проверяет существование файла и что это не директория.
     * 4. Читает содержимое файла и возвращает его.
     *
     * @param relativePath Относительный путь к файлу от корня проекта.
     * @return Текстовое содержимое файла или сообщение об ошибке.
     */
    private fun readFileContent(relativePath: String): String {
        val projectPath = getProjectRootPath()
            ?: return "Error: No project is currently open. Unable to determine the project root directory."

        val file = File(projectPath, relativePath)

        if (!file.exists()) {
            return "Error: File '$relativePath' does not exist in the project."
        }

        if (file.isDirectory) {
            return "Error: '$relativePath' is a directory, not a file. Use 'project_tree' to explore directories."
        }

        return try {
            file.readText()
        } catch (e: Exception) {
            "Error: Failed to read file '$relativePath': ${e.message}"
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
