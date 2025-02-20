package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Content
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class ContentWriter() : Writer<Content>(TableDefinitions.getTableName<Content>()) {
    override val columnMappings: Map<String, String>
        get() = mapOf(
            "id" to "?",
            "hash" to "?",
            "raw" to "?::jsonb"
        )

    override fun initialize(transaction: Transaction) {
        preparedStatement =
            createPreparedInsertStatement<Content>(columnMappings, transaction, ConflictType.DO_NOTHING)
    }

    override fun write(
        message: PipelineMessage.Payload<Content>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setInt(colMappings.getOrError("id"), entry.id)
            stmt.setString(colMappings.getOrError("hash"), entry.hash)
            stmt.setString(colMappings.getOrError("raw"), entry.raw.replace("'", "''"))
        }

}
