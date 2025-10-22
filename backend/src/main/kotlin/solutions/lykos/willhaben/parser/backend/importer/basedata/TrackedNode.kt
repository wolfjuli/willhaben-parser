package solutions.lykos.willhaben.parser.backend.importer.basedata

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.bufferedWriter


abstract class TrackedNode : Node {
    companion object {
        data class TrackEntry(
            var amount: Int,
            var memSize: Long
        ) {
            fun plus(other: TrackEntry): TrackEntry {
                amount += other.amount
                memSize += other.memSize

                return this
            }
        }

        val initializedObjects = ConcurrentHashMap<String, TrackEntry>()

        private val logger by lazy { LoggerFactory.getLogger("StatisticsWriterJob") }

        var finish = false

        val writerJob: Job = GlobalScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            val targetFile = kotlin.io.path.createTempFile("statistics", ".csv")
            logger.info("Writing statistics file to $targetFile")

            targetFile.bufferedWriter().use { writer ->
                writer.write("datetime,className,amount,memSize")
                writer.newLine()

                var counter = 0
                val start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

                while (true) {
                    delay(1000)
                    val now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start

                    initializedObjects.forEach { clazz, entry ->
                        writer.write("$now,$clazz,${entry.amount},${entry.memSize}")
                        writer.newLine()
                    }

                    if (++counter >= 5) {
                        counter = 0
                        writer.flush()
                    }

                    if (finish)
                        break
                }
            }

            logger.info("########## Finished writing statistics file to $targetFile")
        }
    }

    private val clazzName: String = this::class.java.simpleName
    private val memSize: Long = 0

    init {
        initializedObjects.merge(clazzName, TrackEntry(1, memSize), TrackEntry::plus)
        writerJob.start()
    }

    protected fun finalize() {
        initializedObjects.merge(clazzName, TrackEntry(-1, -memSize), TrackEntry::plus)
    }


}
