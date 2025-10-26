package solutions.lykos.willhaben.parser.backend.api.messages.rcv

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.serialization.Serializable
import solutions.lykos.willhaben.parser.backend.camelCase
import solutions.lykos.willhaben.parser.backend.database.postgresql.*
import solutions.lykos.willhaben.parser.backend.importer.toSnakeCase


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(GetAttributes::class, name = "getAttributes"),
    JsonSubTypes.Type(Ping::class, name = "ping"),
)
abstract class WSMessage<T : Any> {
    var type: String = this::class.java.simpleName.camelCase()

    open fun respond(transaction: Transaction, templates: QueryTemplateProvider): T =
        respond(QueryBuilder(transaction).append(templates.getTemplate(type)))

    open fun respond(queryBuilder: QueryBuilder): T =
        queryBuilder
            .build()
            .executeQuery()
            .useAsSequence { rs ->
                rs.map { it.toCamelCaseMap() }
            }.toList() as T

}