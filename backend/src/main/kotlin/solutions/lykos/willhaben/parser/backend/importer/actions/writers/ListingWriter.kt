package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class ListingWriter() : Writer<Listing>(TableDefinitions.getTableName<Listing>()) {
    override val columnMappings: Map<String, String>
        get() = mapOf(
            "willhaben_id" to "?",
            "hash" to "?",
            "duplicate_hash" to "?",
            "raw" to "?::jsonb"
        )

    override fun initialize(transaction: Transaction) {
        preparedStatement =
            createPreparedInsertStatement<Listing>(columnMappings, transaction, ConflictType.DO_NOTHING)
    }

    override fun write(
        message: PipelineMessage.Payload<Listing>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setInt(colMappings.getOrError("willhaben_id"), entry.willhabenId)
            stmt.setString(colMappings.getOrError("hash"), entry.hash)
            stmt.setString(colMappings.getOrError("duplicate_hash"), entry.duplicateHash)
            stmt.setString(colMappings.getOrError("raw"), entry.raw.toJson())
        }

}
