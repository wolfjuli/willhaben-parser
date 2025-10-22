package solutions.lykos.willhaben.parser.backend.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.postgresql.ds.PGSimpleDataSource
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.api.routes.*
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.orNull


fun Route.apiRoutes(configuration: API.Configuration) {
    val dataSource = PGSimpleDataSource().apply {
        databaseName = configuration.database.name
        serverNames = arrayOf(configuration.database.host)
        portNumbers = intArrayOf(configuration.database.port)
        user = configuration.database.user
        password = configuration.database.password
    }

    val database = Database { dataSource.connection }
    val templates = QueryTemplateProvider(
        this::class.java.getResource("/solutions/lykos/willhaben/parser/queries")
            ?: error("Base query url doesn't exist")
    )

    configurations(database, templates)
    search(database, templates)
    listings(database, templates)
    attributes(database, templates)
    scripts(database, templates)
    functions(database, templates)
}


