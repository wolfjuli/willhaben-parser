package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction


data class Ping(val data: Any?) : WSSingleMessage() {
    override fun respond(transaction: Transaction, templates: QueryTemplateProvider): Map<String, Any?> =
        mapOf("type" to "pong", "data" to data)
}