package solutions.lykos.willhaben.parser.backend.api.messages.rcv

import solutions.lykos.willhaben.parser.backend.api.SortDir
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction


data class GetSorting(
    val sortCol: String?,
    val sortDir: SortDir?,
    val searchString: String?,
    val searchAttributes: List<String>?,
    val viewAttributes: List<String>?
) : WSMessage<List<Map<String, Any?>>>() {
    override fun respond(transaction: Transaction, templates: QueryTemplateProvider): List<Map<String, Any?>> {
        return respond(
            QueryBuilder(transaction)
                .append(templates.getTemplate(type, mapOf("sortDir" to sortDir.toString())))
        )

    }
}