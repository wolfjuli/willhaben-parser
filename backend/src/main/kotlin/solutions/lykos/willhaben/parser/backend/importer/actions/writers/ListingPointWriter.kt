package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage

class ListingPointWriter : Writer<Listing>(TableDefinitions.getTableName<Listing>()) {

    override val columnMappings: Map<String, String>
        get() = mapOf("willhaben_id" to "?")

    override fun initialize(transaction: Transaction) {
        preparedStatement = transaction.prepareStatement(
        """
            WITH inp(willhaben_id) AS (VALUES (?))
            SELECT update_listing_points(array_agg(willhaben_id))
            FROM inp;
        """.trimIndent()
        )
    }


    override fun write(
        message: PipelineMessage.Payload<Listing>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setInt(colMappings.getOrError("willhaben_id"), entry.willhabenId)
        }
}
