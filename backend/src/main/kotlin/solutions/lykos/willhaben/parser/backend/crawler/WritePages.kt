package solutions.lykos.willhaben.parser.backend.crawler

import Importer
import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSpecification
import solutions.lykos.willhaben.parser.backend.config.CrawlerConfiguration
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.asSequence


fun Stream<WHAdvertSpecification>.write(transaction: Transaction, configuration: CrawlerConfiguration, importer: Importer) = with(importer) {
    var max = 0
    val idx = AtomicInteger(0)
    (configuration.debugAmount?.let { limit(it.toLong()) } ?: this@write)
        .asSequence()
        .forEach { content ->
            max = idx.getAndIncrement()

            if (max > 0 && max % 500 == 0 ) {
                logger.info("parsed $max - flushing and committing")
                listingPipeline.flush()
                transaction.commit()
            }

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
