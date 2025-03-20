package solutions.lykos.willhaben.parser.backend.crawler

import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.config.WPConfiguration
import solutions.lykos.willhaben.parser.backend.crawler.writers.write
import solutions.lykos.willhaben.parser.backend.database.postgresql.DataSource
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.database.postgresql.useAsSequence
import solutions.lykos.willhaben.parser.backend.database.postgresql.useTransaction
import solutions.lykos.willhaben.parser.backend.parser.parse
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.thread

class Crawler(
    private val configuration: WPConfiguration
) {
    private var currentThread: Thread? = null
    private var stop: Boolean = false

    private val dataSource = DataSource(configuration.database)

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun start() {
        if (currentThread != null)
            error("Crawler.start called twice")

        stop = false

        currentThread = thread(name = "Crawler") {
            while (!stop) {
                sleep()
                run()
                //write(data)
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

        dataSource.connection.useTransaction { transaction ->
            dataSource.get.watchLists().parse()
                .write(transaction, configuration.crawler)
        }
    }

    private fun sleep() {
        val lastRun = dataSource.connection.useTransaction { transaction: Transaction ->
            transaction.prepareStatement("""SELECT max(last_seen) AS last FROM listings""").useAsSequence { seq ->
                seq.map { it.getTimestamp("last") }.first()
            }
        }

        val wait = lastRun?.let {
            (configuration.crawler.timeout * 1000L -
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
}
