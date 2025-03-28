package solutions.lykos.willhaben.parser.backend.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.postgresql.ds.PGSimpleDataSource
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
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

    val logger = LoggerFactory.getLogger(this.javaClass)
    val database = Database { dataSource.connection }

    //Special listing search endpoint
    data class SearchParams(
        val page: Int? = null,
        val searchString: String? = null,
        val attributes: List<String>? = null,
        val sortCol: String = "points",
        val sortDirection: SortDir = SortDir.DESC
    ) {
        fun toMap() = mapOf(
            "page" to page,
            "searchString" to (searchString?.trim()?.split(" ") ?: emptyList()),
            "attributes" to attributes,
            "sortCol" to sortCol,
            "sortDir" to sortDirection,
        )

        init {
            sortCol.trim().takeIf {
                it.contains("--") ||
                        it.contains("//") ||
                        it.contains("/*") ||
                        it.contains(" ")
            }?.let {
                error("Invalid sort column")
            }
        }
    }


    val templates = QueryTemplateProvider(
        this::class.java.getResource("/solutions/lykos/willhaben/parser/queries")
            ?: error("Base query url doesn't exist")
    )
    get("search") {
        val params: SearchParams =
            call.request.queryParameters.get("params")?.let { jsonMapper.readValue(it) } ?: SearchParams()

        val query = templates.getTemplate(
            "search",
            mapOf(
                "sortDir" to params.sortDirection.toString(),
                "limit" to 100,
                "offset" to ((params.page ?: 1) - 1) * 100,
            )
        )

        val list = try {
            dataSource.connection.useTransaction { transaction ->
                QueryBuilder(transaction)
                    .append(query)
                    .build(params.toMap())
                    .executeQuery()
                    .useAsSequence { seq ->
                        seq.map { it.toCamelCaseMap() }.toList()
                    }
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            return@get
        }
        call.respond(list)
    }

    //GET requests for tables and views
    database.tables.forEach { (tableName, tableDef) ->
        get(tableName) {
            logger.info("API Get $tableName")

            val params = call.request.queryParameters.toMap().toMutableMap()

            var offset: Int? = null
            var limit: Int? = null
            if (params.containsKey("page")) {
                limit = 100
                offset = ((params["page"]?.firstOrNull()?.orNull?.toInt() ?: 1) - 1) * 100
            }

            val list = dataSource.connection.useTransaction { transaction ->
                QueryBuilder(transaction)
                    .append(database.selectQuery(tableDef, limit, offset))
                    .build(params)
                    .executeQuery()
                    .useAsSequence { seq ->
                        seq.map { it.toCamelCaseMap() }.toList()
                    }
            }

            call.respond(list)
        }
    }

    //POST requests for tables only
    database.tables.filter { it.value.type == "BASE TABLE" }.forEach { (tableName, tableDef) ->
        post(tableName) {
            logger.info("API POST $tableName")

            val new: Map<String, Any?> = jsonMapper.readValue(call.receiveText())

            val obj: Map<String, Any?> = dataSource.connection.useTransaction { transaction ->
                val stmt = QueryBuilder(transaction)
                    .append(database.insertQuery(tableDef))
                    .build(new)
                try {
                    stmt
                        .executeQuery()
                        .useAsSequence { seq ->
                            seq.map { it.toCamelCaseMap() }.first()
                        }
                } catch (e: PSQLException) {
                    logger.error("Query: $stmt")
                    throw e
                }
            }

            call.respond(obj)
        }
    }

    //PUT requests for tables only
    database.tables.filter { it.value.type == "BASE TABLE" }.forEach { (tableName, tableDef) ->
        put(tableName) {
            logger.info("API PUT $tableName")

            val new: Map<String, Any?> = jsonMapper.readValue(call.receiveText())

            val obj: Map<String, Any?> = dataSource.connection.useTransaction { transaction ->
                val stmt = QueryBuilder(transaction)
                    .append(database.updateQuery(tableDef))
                    .build(new)
                try {
                    stmt.executeQuery()
                        .useAsSequence { seq ->
                            seq.map { it.toCamelCaseMap() }.first()
                    }
                } catch (e: PSQLException) {
                    logger.error("Query: $stmt")
                    throw e
                }
            }

            call.respond(obj)
        }
    }


    //DELETE requests for tables only
    database.tables.filter { it.value.type == "BASE TABLE" }.forEach { (tableName, tableDef) ->
        delete(tableName) {
            logger.info("API DELETE $tableName")

            val del: Map<String, Any?> = jsonMapper.readValue(call.receiveText())

            val obj: Int = dataSource.connection.useTransaction { transaction ->
                QueryBuilder(transaction)
                    .append(database.deleteQuery(tableDef))
                    .build(del)
                    .executeUpdate()
            }

            call.respond(obj)
        }
    }
}


