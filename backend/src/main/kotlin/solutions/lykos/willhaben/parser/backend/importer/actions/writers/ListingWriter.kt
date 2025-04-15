package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.useAsSequence
import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.getOrError
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage

class ListingWriter : Writer<Listing>(TableDefinitions.getTableName<Listing>()) {
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

    private val ids = mutableSetOf<Int>()
    override fun write(
        message: PipelineMessage.Payload<Listing>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            ids.add(entry.willhabenId)
            stmt.setInt(colMappings.getOrError("willhaben_id"), entry.willhabenId)
            stmt.setString(colMappings.getOrError("hash"), entry.hash)
            stmt.setString(colMappings.getOrError("duplicate_hash"), entry.duplicateHash)
            stmt.setString(colMappings.getOrError("raw"), entry.raw.toJson())
        }

    override fun close(transaction: Transaction): PipelineMessage<Listing> {
        val ret = super.close(transaction)
        val willhabenIds = ids.toList()

        if (willhabenIds.isNotEmpty()) {
            logger.info("Updating ${willhabenIds.size} normalized listing points")
            var amount = QueryBuilder(transaction).append(
                """
            SELECT count(*) as count
            FROM update_normalized_listings(willhabenIds := ${'$'}{willhabenIds})
        """.trimIndent()
            )
                .build("willhabenIds" to willhabenIds)
                .executeQuery().useAsSequence { seq -> seq.map { it.getInt("count") }.first() }

            logger.info("Updated $amount normalized listings")

            amount = QueryBuilder(transaction).append(
                """
            SELECT count(*) as count
            FROM update_listing_points(willhabenIds := ${'$'}{willhabenIds})
        """.trimIndent()
            )
                .build("willhabenIds" to willhabenIds)
                .executeQuery().useAsSequence { seq -> seq.map { it.getInt("count") }.first() }

            logger.info("Updated $amount listing points")

            ids.clear()
        }
        return ret
    }
}
