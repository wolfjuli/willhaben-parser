package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import solutions.lykos.willhaben.parser.backend.database.postgresql.*


val json = jacksonObjectMapper().registerKotlinModule()
data class CreateListing(val listing: Map<String, Any>) : WSListMessage() {
    override fun respond(transaction: Transaction, templates: QueryTemplateProvider): List<Map<String, Any?>> =
        QueryBuilder(transaction)
            .append(templates.getTemplate(type))
            .build(mapOf("listing" to json.writeValueAsString(listing)))
            .executeQuery()
            .useAsSequence { rs ->
                rs.map { it.toCamelCaseMap() }.toList()
            }
}