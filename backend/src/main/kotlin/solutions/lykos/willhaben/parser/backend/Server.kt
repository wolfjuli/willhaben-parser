package solutions.lykos.willhaben.parser.backend

import io.ktor.server.application.*

fun main(args: Array<String>) {

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSwagger()
    configureRouting()
}
