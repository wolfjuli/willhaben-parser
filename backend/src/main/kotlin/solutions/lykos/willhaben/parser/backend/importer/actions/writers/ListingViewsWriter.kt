package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage

class ListingViewsWriter : Writer<Listing>(TableDefinitions.getTableName<Listing>()) {
    override val columnMappings: Map<String, String>
        get() = mapOf()

    override fun initialize(transaction: Transaction) {
        preparedStatement = transaction.prepareStatement(
            """
                SELECT FROM update_normalize_listings(willhaben_ids := ARRAY[?::INT]);
            """.trimIndent()
        )
    }

    override fun write(
        message: PipelineMessage.Payload<Listing>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, _ ->
            stmt.setInt(0, entry.willhabenId)
            stmt.setInt(1, entry.willhabenId)
            stmt.setInt(2, entry.willhabenId)
        }

}
