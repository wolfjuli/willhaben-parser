package solutions.lykos.willhaben.parser.backend.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger

fun Route.attributes(database: Database, templates: QueryTemplateProvider) {
    route("attributes") {
        get {
            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("get/attributes"))
                        .build()
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
        get("{id}") {
            val id = call.request.pathVariables["id"]?.takeUnless { it.isBlank() }?.toIntOrNull()
                ?: error("missing id parameter or id was not numeric")

            logger.info("API Get attribute/id=$id")
            val attr = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("get/attributes/full"))
                        .build("id" to id)
                        .executeQuery()
                        .useAsSequence { seq ->
                            seq.map { it.toCamelCaseMap() }.firstOrNull()
                        }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
                return@get
            }
            if (attr == null) {
                call.respond(HttpStatusCode.NotFound, "Not matching attribute with id $id found")
                return@get
            }

            call.respond(attr)
        }

        contentType(ContentType.Application.Json) {
            post("{id}") {
                val id = call.request.pathVariables["id"]?.takeUnless { it.isBlank() }?.toIntOrNull()
                    ?: error("missing id parameter or id was not numeric")

                val attr: Map<String, Any> = call.receive()

                if(attr.containsKey("id") && attr["id"] != id)
                    return@post call.respond(HttpStatusCode.BadRequest, "Given id does not match the object id")

                if(!attr.containsKey("label"))
                    return@post call.respond(HttpStatusCode.BadRequest, "Given object misses a label field")
                if(!attr.containsKey("dataType"))
                    return@post call.respond(HttpStatusCode.BadRequest, "Given object misses a dataType field")

                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("post/attribute")
                        )
                        .build(mapOf("id" to id, "label" to attr["label"], "dataType" to attr["dataType"]))
                        .executeUpdate()
                }

                call.respond(200)
            }
        }
    }
}
