package solutions.lykos.willhaben.parser.backend.crawler.writers

import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSpecification
import solutions.lykos.willhaben.parser.backend.config.CrawlerConfiguration
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.actions.Communicator
import solutions.lykos.willhaben.parser.backend.importer.actions.ResolvingActions
import solutions.lykos.willhaben.parser.backend.importer.actions.filters.UniqueFilter
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.AttributeResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.ListingResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.ListingAttributeMultiplier
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.PipeTo
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.*
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import solutions.lykos.willhaben.parser.backend.importer.pipelines.Pipeline
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.time.ZonedDateTime
import java.util.*


private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
private val resolveFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)

private val now = ZonedDateTime.now()


fun Sequence<WHAdvertSpecification>.write(transaction: Transaction, configuration: CrawlerConfiguration) {
    val attributeActions = ActionSequence(
        ListingAttributeMultiplier(),
        AttributesWriter()
    )

    val listingPointActions = ResolvingActions(
        ListingResolver(),
        ListingPointWriter()
    )

    val listingActions = ResolvingActions(
        ListingResolver(),
        ListingWriter(),
        PipeTo(
            attributeActions,
            { ListingAttribute(this, Attribute(""), "") }
        ),
        Communicator(
            listingPointActions,
            { this },
            {me, _ -> me},
            writeFlags
        )
    )

    val listingPipeline = Pipeline(
        ActionSequence(
            LastSeenWriter(),
            UniqueFilter(),
            Communicator(
                listingActions,
                { this },
                { me, _ -> me },
                writeFlags
            )
        )
    )

    var max = 0
    listingPipeline.initialize(transaction)
    (configuration.debugAmount?.let { this.take(it) } ?: this)
        .onEachIndexed { idx, _ ->
            if (idx % 500 == 0 ) {
                logger.info("parsed $idx - flushing and committing")
                listingPipeline.flush()
                transaction.commit()
            }

            max = idx
        }
        .forEach { content ->
            listingPipeline.offer(content.toNode())
        }
    logger.info("Parsed ${max + 1} entries")

    generateSequence(1) {
        val ret = listingPipeline.close()
        if (ret.javaClass.simpleName == PipelineMessage.Close::class.java.simpleName)
            null
        else {
            (it + 1)
                .takeIf { it <= 10 }
                ?.also {
                    logger.info("Trying to close again... (attempt $it / 10)")
                }
        }
    }.toList()



    logger.info("Done")
}
