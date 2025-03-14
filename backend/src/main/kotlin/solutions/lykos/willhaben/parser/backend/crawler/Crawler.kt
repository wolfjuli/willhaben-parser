package solutions.lykos.willhaben.parser.backend.crawler

import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.config.WPConfiguration
import solutions.lykos.willhaben.parser.backend.crawler.writers.write
import solutions.lykos.willhaben.parser.backend.database.postgresql.DataSource
import solutions.lykos.willhaben.parser.backend.database.postgresql.useTransaction
import solutions.lykos.willhaben.parser.backend.parser.parse
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
                val data = run()
                //write(data)
                sleep()
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
            //jsonObjectMapper().readValue<List<WHAdvertSummary>>(File("/Users/jwolf/tmp/wh_all.json")).asSequence()
            .write(transaction)
        }
    }




    private fun sleep() {
        val wait = configuration.crawler.timeout * 1000L
        logger.info("Waiting for $wait ms")
        generateSequence(0) { (it + 500).takeIf { it < wait } }.forEach { _ ->
            Thread.sleep(500)
            if (stop)
                return@forEach
        }
        logger.info("Finished wait")

    }
}
