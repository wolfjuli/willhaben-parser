package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage

class LastSeenWriter(override val columnMappings: Map<String, String> = mapOf()) :
    Writer<Listing>(TableDefinitions.getTableName<Listing>()) {

    override fun write(message: PipelineMessage.Payload<Listing>, transaction: Transaction) {
        ids.add(message.payload.willhabenId)
    }


    private val ids = mutableSetOf<Int>()
    override fun close(transaction: Transaction): PipelineMessage<Listing> {
        val ret = super.close(transaction)

        val amount = QueryBuilder(transaction).append(
            """
            UPDATE listings SET last_seen = now() WHERE willhaben_id = ANY(${'$'}{ids}::INT[])
            """.trimIndent()
        )
            .build(mapOf("ids" to ids.toList()))
            .executeLargeUpdate()

        logger.info("Updated $amount last seen Listings")
        ids.clear()

        return ret
    }


}
