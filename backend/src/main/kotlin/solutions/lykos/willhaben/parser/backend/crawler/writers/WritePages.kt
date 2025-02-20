package solutions.lykos.willhaben.parser.backend.crawler.writers

import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.actions.Communicator
import solutions.lykos.willhaben.parser.backend.importer.actions.ResolvingActions
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.AttributeResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.ContentResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.DataBlockResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.AttributesWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ContentAttributesWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ContentWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.DataBlockWriter
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.ContentAttribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.DataBlock
import solutions.lykos.willhaben.parser.backend.importer.pipelines.Pipeline
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.time.ZonedDateTime
import java.util.*


private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
private val resolveFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)

fun Sequence<WHAdvertSummary>.write(transaction: Transaction) {
    val now = ZonedDateTime.now()

    val contentActions = ResolvingActions(
        ContentResolver(),
        ContentWriter()
    )

    val dataBlockActions = ResolvingActions(
        DataBlockResolver(),
        Communicator(
            contentActions,
            { content },
            { me, _ -> me },
            writeFlags
        ),
        DataBlockWriter()
    )

    val attributeActions = ResolvingActions(
        AttributeResolver()
    )

    val contentAttributeActions = Pipeline(
        ActionSequence(
            Communicator(
                attributeActions,
                { attribute },
                { me, res -> me.also { it.attribute.id = res.id } },
                resolveFlags
            ),
            Communicator(
                dataBlockActions,
                { dataBlock },
                { me, res -> me.also { it.dataBlock.id = res.id } },
                writeFlags
            ),
            ContentAttributesWriter()
        )
    )

    val writeAttributes = Pipeline(
        ActionSequence(
            AttributesWriter()
        )
    )


    contentAttributeActions.initialize(transaction)
    onEachIndexed { idx, it ->
        logger.info("working on $idx ")
    }
        .chunked(100)
        .forEach { chunk ->
            val attributes = chunk.flatMap { it.attributes.attribute.map { it.name } }.toSet().map { Attribute(it) }
            writeAttributes.offer(attributes)

            chunk.forEach { content ->
                content.attributes.attribute.forEach { attribute ->
                    contentAttributeActions.offer(
                        ContentAttribute(
                            DataBlock(content.toNode(), now),
                            Attribute(attribute.name),
                            attribute.values?.filterNotNull() ?: emptyList()
                        )
                    )
                }
            }
        }
    contentAttributeActions.close()
    logger.info("Done")
}
