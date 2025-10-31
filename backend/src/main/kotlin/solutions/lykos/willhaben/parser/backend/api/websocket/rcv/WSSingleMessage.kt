package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

import solutions.lykos.willhaben.parser.backend.api.websocket.WSMessage
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import kotlin.reflect.full.memberProperties

abstract class WSSingleMessage : WSMessage() {

    protected fun asMap() = this::class.memberProperties.map { it.name to it.call(this) }.toMap()

    override fun respond(transaction: Transaction, templates: QueryTemplateProvider) =
        respond(QueryBuilder(transaction).append(templates.getTemplate(type)))

    open fun respond(queryBuilder: QueryBuilder) =
        queryBuilder
            .build(asMap())
            .executeQuery()
            .useAsSequence { rs ->
                rs.map { it.toCamelCaseMap() }.firstOrNull()
            }

}