package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.DataBlock
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class DataBlockWriter() : Writer<DataBlock>(TableDefinitions.getTableName<DataBlock>()) {
    override val columnMappings: Map<String, String>
        get() = mapOf(
            "timestamp" to "?::TIMESTAMPTZ",
            "content_id" to "?"
        )

    override fun initialize(transaction: Transaction) {
        preparedStatement =
            createPreparedInsertStatement<DataBlock>(columnMappings, transaction, ConflictType.DO_NOTHING)
    }

    override fun write(
        message: PipelineMessage.Payload<DataBlock>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setString(colMappings.getOrError("timestamp"), entry.timestamp.toString().substringBefore("["))
            stmt.setInt(colMappings.getOrError("content_id"), entry.contentId)
        }
}
