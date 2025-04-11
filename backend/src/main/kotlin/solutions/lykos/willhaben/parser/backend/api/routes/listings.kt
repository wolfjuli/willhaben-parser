package solutions.lykos.willhaben.parser.backend.api.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger

fun Route.listings(database: Database, templates: QueryTemplateProvider) {
    route("listings") {
        get("sorting") {
            logger.info("API Get listings/sorting")
            val sortCol: String =
                call.request.queryParameters["sortCol"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "sortCol parameter is missing")

            val sortDir: String =
                call.request
                    .queryParameters["sortDir"]
                    ?.takeIf { it == "DESC" || it == "ASC" }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "sortDir parameter is missing")

            val query = templates.getTemplate(
                "get/listings/sorting",
                mapOf(
                    "sortDir" to sortDir
                )
            )

            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(query)
                        .build("sortCol" to sortCol)
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
        get("full") {
            logger.info("API Get listings/full")
            val knownMd5 = call.request.queryParameters["knownMd5"]?.takeUnless { it.isBlank() }
            val listingId = call.request.queryParameters["listingId"]

            val query = templates.getTemplate(
                "get/listings/full"
            )

            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(query)
                        .build("knownMd5" to knownMd5?.split(","), "listingId" to listingId)
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
}
