package solutions.lykos.willhaben.parser.backend.api.messages.rcv

import solutions.lykos.willhaben.parser.backend.api.messages.snd.Pong
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction


data class Ping(val data: Any) : WSMessage<Pong>() {

    override fun respond(transaction: Transaction, templates: QueryTemplateProvider): Pong {
        return Pong(data)
    }
}