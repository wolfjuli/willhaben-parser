package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryBuilder
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.toPgString
import solutions.lykos.willhaben.parser.backend.database.postgresql.useAsSequence
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

    private val listingIds = mutableSetOf<Int>()
    override fun write(
        message: PipelineMessage.Payload<ListingAttribute>,
        transaction: Transaction
    ) =
        batchInsert(message) { entry, stmt, colMappings ->
            listingIds.add(entry.listingId.orNotResolved(entry))
            stmt.setInt(colMappings.getOrError("listing_id"), entry.listingId.orNotResolved(entry))
            stmt.setInt(colMappings.getOrError("attribute_id"), entry.attributeId.orNotResolved(entry))
            stmt.setString(colMappings.getOrError("values"), entry.values.toPgString())
        }

    override fun close(transaction: Transaction): PipelineMessage<ListingAttribute> {
        val ret = super.close(transaction)

        logger.info("Updating ${listingIds.size} normalized listings")
        QueryBuilder(transaction).append(
            """
            SELECT count(*) c FROM update_normalize_listings(listing_ids := ${'$'}{listingIds})
            """.trimIndent()
        )
            .build("listingIds" to listingIds.toList())
            .executeQuery().useAsSequence { logger.info("Updated ${it.first().getInt("c")} rows") }


        logger.info("Updating ${listingIds.size} listing points")
        QueryBuilder(transaction).append(
            """
            SELECT count(*) c FROM update_listing_points(listing_ids := ${'$'}{listingIds})
            """.trimIndent()
        )
            .build("listingIds" to listingIds.toList())
            .executeQuery().useAsSequence { logger.info("Updated ${it.first().getInt("c")} rows") }


        listingIds.clear()
        return ret
    }
}
