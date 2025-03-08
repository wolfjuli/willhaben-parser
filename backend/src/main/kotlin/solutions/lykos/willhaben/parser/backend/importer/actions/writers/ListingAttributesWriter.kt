package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.toPgString
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.orNotResolved
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage

class ListingAttributesWriter : Writer<ListingAttribute>("") {
    override val columnMappings: Map<String, String>
        get() = mapOf(
            "listing_id" to "?",
            "attribute_id" to "?",
            "values" to "?::TEXT[]"
        )

    override fun initialize(transaction: Transaction) {
        preparedStatement =
            createPreparedInsertStatement<ListingAttribute>(columnMappings, transaction, ConflictType.DO_NOTHING)
    }

    override fun write(
        message: PipelineMessage.Payload<ListingAttribute>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setInt(colMappings.getOrError("listing_id"), entry.listingId.orNotResolved(entry))
            stmt.setInt(colMappings.getOrError("attribute_id"), entry.attributeId.orNotResolved(entry))
            stmt.setString(colMappings.getOrError("values"), entry.values.toPgString())
        }

}
