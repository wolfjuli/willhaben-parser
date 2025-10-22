package solutions.lykos.willhaben.parser.backend.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger

fun Route.configurations(database: Database, templates: QueryTemplateProvider) {
    route("configuration") {
        get {
            val list = try {
                database.connection().useTransaction { transaction ->
                    QueryBuilder(transaction)
                        .append(templates.getTemplate("get/configuration"))
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
