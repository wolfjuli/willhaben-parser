package solutions.lykos.willhaben.parser.backend.api.rest

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*

fun Route.scripts(database: Database, templates: QueryTemplateProvider) {
    route("scripts") {
        get {

            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("get/scripts"))
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
    }
}
