package solutions.lykos.willhaben.parser.backend.crawler

import solutions.lykos.willhaben.parser.backend.api.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import solutions.lykos.willhaben.parser.backend.postgresql.useTransaction
import java.sql.Connection


fun Sequence<WHAdvertSummary>.write(connection: Connection) {
    val mapper = jsonObjectMapper()

    val values = joinToString(",\n") {
        """(${it.id}, '${it.advertStatus.id}', '${mapper.writeValueAsString(it)}')"""
    }

    connection.useTransaction {

    }


}
