package solutions.lykos.willhaben.parser.backend.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*

fun Route.listings(database: Database, templates: QueryTemplateProvider) {
    route("listings") {
        get {
            val knownMd5 = call.request.queryParameters["knownMd5"]?.takeUnless { it.isBlank() }
            val ids = call.request.queryParameters["ids"]?.takeUnless { it.isBlank() }

            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("get/listings"))
                        .build("knownMd5" to knownMd5?.split(","), "ids" to ids?.split(","))
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

        contentType(ContentType.Application.Json) {
            post("{listingId}/{attributeId}") {
                val listingId = call.request.pathVariables["listingId"]?.takeUnless { it.isBlank() } ?: error(
                    "listingId parameter is missing"
                )
                val attributeId = call.request.pathVariables["attributeId"]?.takeUnless { it.isBlank() } ?: error(
                    "attributeId parameter is missing"
                )
                val value = runBlocking { call.receiveText() }

                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(
                            """INSERT INTO listing_user_attributes (listing_id, attribute_id, values) VALUES (${'$'}{listingId}::INT, ${'$'}{attributeId}::INT, ${'$'}{value}::JSONB) 
                            |ON CONFLICT (listing_id, attribute_id) DO UPDATE SET values = excluded.values;
                        """.trimMargin()
                        )
                        .build(mapOf("listingId" to listingId, "attributeId" to attributeId, "value" to value))
                        .executeUpdate()
                }

                call.respond(200)
            }
        }
        delete("{listingId}/{attributeId}") {
            val listingId = call.request.pathVariables["listingId"]?.takeUnless { it.isBlank() } ?: error(
                "listingId parameter is missing"
            )
            val attributeId = call.request.pathVariables["attributeId"]?.takeUnless { it.isBlank() } ?: error(
                "attributeId parameter is missing"
            )

            database.connection().useTransaction { transaction ->
                QueryBuilder(transaction)
                    .append(
                        """
                            DELETE FROM listing_user_attributes where listing_id = ${'$'}{listingId}::INT AND attribute_id = ${'$'}{attributeId}::INT
                        """.trimMargin()
                    )
                    .build(mapOf("listingId" to listingId, "attributeId" to attributeId))
                    .executeUpdate()
            }

            call.respond(200)
        }

        get("sorting") {
            val sortCol: String =
                call.request.queryParameters["sortCol"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "sortCol parameter is missing")

            val sortDir: String =
                call.request
                    .queryParameters["sortDir"]
                    ?.takeIf { it == "DESC" || it == "ASC" }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "sortDir parameter is missing")

            val searchAttrs =
                call.request
                    .queryParameters["searchAttrs"]
                    ?.split(",") ?: emptyList()

            val searchString =
                call.request
                    .queryParameters["searchString"]?.takeUnless { it.isBlank() }

            val query = templates.getTemplate(
                "get/sorting",
                mapOf(
                    "sortDir" to sortDir
                )
            )

            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(query)
                        .build("sortCol" to sortCol, "searchAttrs" to searchAttrs, "searchString" to searchString)
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
