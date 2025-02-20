package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.importer.basedata.ContentAttribute
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.orNotResolved
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.postgresql.toPgString

class ContentAttributesWriter : Writer<ContentAttribute>("") {
    override val columnMappings: Map<String, String>
        get() = mapOf(
            "data_block_id" to "?",
            "attribute_id" to "?",
            "values" to "?::TEXT[]"
        )

    override fun initialize(transaction: Transaction) {
        preparedStatement =
            createPreparedInsertStatement<ContentAttribute>(columnMappings, transaction, ConflictType.DO_NOTHING)
    }

    override fun write(
        message: PipelineMessage.Payload<ContentAttribute>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            stmt.setInt(colMappings.getOrError("data_block_id"), entry.dataBlockId.orNotResolved(entry))
            stmt.setInt(colMappings.getOrError("attribute_id"), entry.attributeId.orNotResolved(entry))
            stmt.setString(colMappings.getOrError("values"), entry.values.toPgString())
        }

}
