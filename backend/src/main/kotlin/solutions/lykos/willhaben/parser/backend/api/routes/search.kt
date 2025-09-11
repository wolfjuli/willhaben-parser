package solutions.lykos.willhaben.parser.backend.api.routes

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.api.SearchParams
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger

fun Route.search(database: Database, templates: QueryTemplateProvider) =
    route("search") {
        get {
            val params: SearchParams =
                call.request.queryParameters.get("params")?.let { jsonMapper.readValue(it) } ?: SearchParams()

            val query = templates.getTemplate(
                "search",
                mapOf(
                    "sortDir" to params.sortDir.toString(),
                    "limit" to 100,
                    "offset" to ((params.page ?: 1) - 1) * 100,
                )
            )

            logger.info("search: $params")

            val list = try {
                database.connection().useTransaction { transaction ->
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
    }