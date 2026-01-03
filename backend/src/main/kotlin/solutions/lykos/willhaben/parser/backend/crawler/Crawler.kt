package solutions.lykos.willhaben.parser.backend.crawler

import Importer
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSpecification
import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.api.wh.WHAttributes
import solutions.lykos.willhaben.parser.backend.config.WPConfiguration
import solutions.lykos.willhaben.parser.backend.database.postgresql.DataSource
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.useAsSequence
import solutions.lykos.willhaben.parser.backend.database.postgresql.useTransaction
import solutions.lykos.willhaben.parser.backend.parser.detailed
import solutions.lykos.willhaben.parser.backend.parser.parse
import java.time.Duration
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.concurrent.thread
import kotlin.streams.asStream

object Crawler {
    private var currentThread: Thread? = null
    private var stop: Boolean = false

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var  dataSource: DataSource
    private lateinit var configuration: WPConfiguration

    fun init(configuration: WPConfiguration) {
        this.configuration = configuration
        this.dataSource =  DataSource(configuration.database)
    }

    fun start() {
        if (configuration.crawler.timeout == null) return
        if (currentThread != null)
            error("Crawler.start called twice")

        stop = false

        currentThread = thread(name = "Crawler") {
            while (!stop) {
                sleep()
                run()
            }
        }
    }

    fun stop() {
        if (currentThread == null) return

        stop = true
        currentThread?.join(30000)

        stop = false
        currentThread = null
    }

    private fun run() {
        try {
            dataSource.connection.useTransaction { transaction ->
                val importer = Importer(transaction)
                dataSource.get.watchLists()
                    .parse()
                    .detailed(configuration.crawler)
                    .write(transaction, configuration.crawler, importer)
            }
        } catch (ex: Exception) {
            logger.error("Something went wrong during crawling/insert", ex)
        }
    }

    private fun sleep() {
        val lastRun = dataSource.connection.useTransaction { transaction: Transaction ->
            transaction.prepareStatement("""SELECT max(last_seen) AS last FROM listings""").useAsSequence { seq ->
                seq.map { it.getTimestamp("last") }.first()
            }
        }

        val wait = lastRun?.let {
            (configuration.crawler.timeout!! * 1000L -
                    Duration.between(it.toLocalDateTime()!!, LocalDateTime.now()).toMillis()).coerceAtLeast(0)
        } ?: 0

        logger.info("Crawler waiting for $wait ms")
        generateSequence(0) { (it + 500).takeIf { it < wait } }.forEach { _ ->
            Thread.sleep(500)
            if (stop)
                return@forEach
        }
        logger.info("Finished wait")
    }

    fun crawlSingle(url: String) : List<Int> {
        if(!url.startsWith(configuration.crawler.listingBaseUrl))
            throw Exception("Not allowed url $url")

        val allIds = mutableSetOf<Int>()
        dataSource.connection.useTransaction { transaction ->
            val importer = Importer(transaction)
            sequenceOf<WHAdvertSpecification>(
                WHAdvertSummary.fromUrl(
                    url.replace(
                        configuration.crawler.listingBaseUrl,
                        ""
                    )
                )
            )
                .asStream()
                .detailed(configuration.crawler)
                .map { allIds.add(it.id); it }
                .write(transaction, configuration.crawler, importer)
        }

        return allIds.toList()
    }
}
