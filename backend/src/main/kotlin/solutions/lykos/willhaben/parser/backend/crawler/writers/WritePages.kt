package solutions.lykos.willhaben.parser.backend.crawler.writers

import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.actions.Communicator
import solutions.lykos.willhaben.parser.backend.importer.actions.ResolvingActions
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.AttributeResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.ListingResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.ListingAttributeMultiplier
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.PipeTo
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.AttributesWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.LastSeenWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ListingAttributesWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ListingWriter
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import solutions.lykos.willhaben.parser.backend.importer.pipelines.Pipeline
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.time.ZonedDateTime
import java.util.*


private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
private val resolveFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)

private val now = ZonedDateTime.now()


fun Sequence<WHAdvertSummary>.write(transaction: Transaction) {
    val attributeActions = ResolvingActions(
        AttributeResolver(),
        AttributesWriter()
    )

    val locationActions = ResolvingActions(
        AttributeResolver()
    )

    val listingAttributeActions =
        ActionSequence(
            ListingAttributeMultiplier(),
            Communicator(
                attributeActions,
                { attribute },
                { me, res -> me.also { it.attribute.id = res.id } },
                writeFlags
            ),
            ListingAttributesWriter()
        )

    val listingLocationActions =
        ActionSequence(
            Communicator(
                locationActions,
                { attribute },
                { me, res -> me.also { it.attribute.id = res.id } },
                writeFlags
            ),
            ListingAttributesWriter()
        )

    val listingActions = ResolvingActions(
        ListingResolver(),
        ListingWriter()
    )

    val listingPipeline = Pipeline(
        ActionSequence<Listing>(
            LastSeenWriter(),
            Communicator(
                listingActions,
                { this },
                { _, res -> res },
                writeFlags
            ),
            PipeTo(
                listingAttributeActions,
                { ListingAttribute(this, Attribute(""), emptyList()) }
            )
        )
    )

    listingPipeline.initialize(transaction)
    onEachIndexed { idx, _ ->
        if (idx % 500 == 0) logger.info("working on $idx")
    }
        .forEach { content ->
            listingPipeline.offer(content.toNode())
        }

    (0..10).forEach {
        if (listingPipeline.close() is PipelineMessage.Close) return@forEach
    }

    logger.info("Done")
}
