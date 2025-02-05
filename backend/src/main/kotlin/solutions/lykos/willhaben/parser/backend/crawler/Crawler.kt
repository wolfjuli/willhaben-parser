package solutions.lykos.willhaben.parser.backend.crawler

import org.postgresql.ds.PGSimpleDataSource
import solutions.lykos.willhaben.parser.backend.config.WPConfiguration
import kotlin.concurrent.thread

class Crawler(
    private val configuration: WPConfiguration
) {
    private var currentThread: Thread? = null
    private var stop: Boolean = false

    private val dataSource = with(configuration) {
        PGSimpleDataSource().apply {
            serverNames = arrayOf(database.host)
            portNumbers = intArrayOf(database.port)
            databaseName = database.name
            user = database.user
            password = database.password
        }
    }

    fun start() {
        if (currentThread != null)
            error("Crawler.start called twice")

        stop = false

        currentThread = thread {
            while (!stop) {
                val data = check()
                write(data)
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

    private fun check(): CrawlerData {
        return CrawlerData(emptyList())
    }

    private fun write(data: CrawlerData) {

    }

    private fun sleep() {
        val durationStart = configuration.crawler.interval.first().toLong() * 60 * 1000
        val durationEnd = configuration.crawler.interval.last().toLong() * 60 * 1000

        val wait = durationStart.rangeTo(durationEnd).random()

        generateSequence(0) { (it + 500).takeIf { it < wait } }.forEach { _ ->
            Thread.sleep(wait)
            if (stop)
                return@forEach
        }
    }
}
