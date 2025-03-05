package solutions.lykos.willhaben.parser.backend.api

import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.postgresql.toCamelCaseMap
import solutions.lykos.willhaben.parser.backend.postgresql.useAsSequence
import solutions.lykos.willhaben.parser.backend.postgresql.useTransaction
import java.io.File


fun Route.apiRoutes(configuration: API.Configuration) {
    val dataSource = PGSimpleDataSource().apply {
        databaseName = configuration.database.name
        serverNames = arrayOf(configuration.database.host)
        portNumbers = intArrayOf(configuration.database.port)
        user = configuration.database.user
        password = configuration.database.password
    }

    fun <T : Any?> useTransaction(block: (Transaction) -> T): T = dataSource.connection.useTransaction(block)

    val logger = LoggerFactory.getLogger(this.javaClass)

    fun initGet(folder: File) {
        folder
            .walk()
            .filter { it.isFile }
            .filter { it.extension == "sql" }
            .forEach { file ->
                get(file.relativeTo(folder).path.substringBeforeLast(".")) {
                    logger.info("API Get ${file.name}")
                    val list = useTransaction { transaction ->
                        transaction
                            .prepareStatement(file.readText()).executeQuery().useAsSequence { seq ->
                                seq.map { it.toCamelCaseMap() }.toList()
                            }
                    }

                    call.respond(list)
                }
            }
    }

    val basePath = "/solutions/lykos/willhaben/parser/queries"
    File(this::class.java.getResource(basePath)?.toURI() ?: error("Resource not found: $basePath"))
        .listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                when (folder.name) {
                    "get" -> {
                        initGet(folder)
                    }
                }
            }
        }


}


