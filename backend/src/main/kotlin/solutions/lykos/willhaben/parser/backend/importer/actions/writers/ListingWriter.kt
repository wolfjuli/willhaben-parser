package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
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
        val amount = QueryBuilder(transaction).append(
            """
           INSERT INTO listing_points (listing_id, attribute_id, script_id, points)
            SELECT la.listing_id,
                   la.attribute_id,
                   s.id,
                   run_script(s.id, la.attribute_id::SMALLINT, la.values[1], nl.listing)
            FROM listing_attributes la
                     JOIN normalized_listings nl
                          ON nl.listing_id = la.listing_id
                     JOIN scripts s
                          ON s.attribute_id = la.attribute_id
            WHERE la.listing_id IN (SELECT id FROM listings WHERE willhaben_id = ANY(${'$'}{ids}::INT[]))
            ON CONFLICT(listing_id, attribute_id, script_id) DO UPDATE SET points = excluded.points
            """.trimIndent()
        )
            .build(mapOf("ids" to ids.toList()))
            .executeLargeUpdate()

        logger.info("Updated $amount listing points")
        ids.clear()
        return ret
    }

}
