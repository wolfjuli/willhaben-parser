package solutions.lykos.willhaben.parser.backend.api

import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.api.rest.configurations
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.*

fun Route.restRoutes(database: Database, templates: QueryTemplateProvider) {
   configurations(database, templates)
}