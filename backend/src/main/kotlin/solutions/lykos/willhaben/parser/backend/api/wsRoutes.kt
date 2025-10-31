package solutions.lykos.willhaben.parser.backend.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import solutions.lykos.willhaben.parser.backend.api.websocket.WSMessage
import solutions.lykos.willhaben.parser.backend.api.websocket.snd.ErrorListMessage
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.useTransaction

val apiClients = mutableListOf<WebSocketServerSession>()

suspend fun WebSocketServerSession.wsRoutes(database: Database, templates: QueryTemplateProvider) {
    apiClients.add(this)
    val mapper = jacksonObjectMapper()
    for (frame in incoming) {
        val body = mapper.readValue<WSMessage>(frame.readBytes())
        database.connection().useTransaction { transaction: Transaction ->
            val res = try {
                body.respond(transaction, templates)
            } catch (e: Exception) {
                send(
                    mapper.writeValueAsString(
                        ErrorListMessage(
                            body.id,
                            e.message ?: e.localizedMessage,
                            body,
                            e.cause?.message ?: e.cause?.localizedMessage ?: ""
                        )
                    )
                )
                return@useTransaction
            }

            send(
                mapper.writeValueAsString(
                    mapOf(
                        "type" to body.type,
                        "id" to body.id,
                        "data" to res
                    )
                )
            )
        }
    }
}
