package solutions.lykos.willhaben.parser.backend

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureSwagger() {
    routing {
        swaggerUI(path = "documentation")
    }
    routing {
        openAPI(path = "documentation")
    }
}
