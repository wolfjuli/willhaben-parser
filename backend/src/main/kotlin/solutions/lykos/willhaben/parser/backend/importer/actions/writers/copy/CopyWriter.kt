package solutions.lykos.willhaben.parser.backend.importer.actions.writers.copy

import org.postgresql.copy.CopyIn
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.Writer
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.nio.CharBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

@Suppress("unused")
abstract class CopyWriter<T : Node>(
    tableName: String,
    val simpleCopy: Boolean = false,
    val debugKeepTmpTable: Boolean = false
) : Writer<T>(tableName) {

    final override val columnMappings: Map<String, String>
        get() = error("Do not use columnMappings, use columns")

    abstract val columns: Map<String, ValueDef<T>>

    open val tmpTableName = "tmp_${tableName.substring(0..(tableName.length - 1).coerceAtMost(60))}"
    protected open val targetColumns by lazy { columns.entries.filter { it.value.copy } }
    protected open val tempTableCols by lazy { columns.entries.joinToString { (col, def) -> "$col ${def.sqlType}" } }
    protected open val colString by lazy { targetColumns.joinToString { it.key } }
    protected open val selectString by lazy { "n.${targetColumns.joinToString(", n.") { it.key }}" }
    protected open val checkColumn by lazy { targetColumns.first().key }
    protected open val joinString by lazy { targetColumns.joinToString(" AND ") { "n.${it.key} = o.${it.key}" } }
    protected open val sql by lazy {
        if (simpleCopy) {
            """
                INSERT INTO $tableName($colString)
                SELECT $selectString
                FROM $tmpTableName n
            """
        } else {
            """
                INSERT INTO $tableName($colString)
                SELECT DISTINCT $selectString
                FROM $tmpTableName n
                LEFT JOIN $tableName o
                ON $joinString
                WHERE o.$checkColumn IS NULL
            """
        }
    }

    protected var writer: StringBuilder? = null
    protected val insertCounter = AtomicLong(0)
    override val batchSize: Int = super.batchSize * 500
    protected val newLine: Char = '\n'
    protected val textLimiter: Char = '"'
    protected val columnLimiter: Char = ','
    protected val charset = Charsets.UTF_8

    protected fun StringBuilder.writeTo(copyIn: CopyIn) {
        val chars = CharArray(1024)
        val bytes = ByteArray(chars.size * 4)
        for (start in indices step chars.size) {
            val end = (start + chars.size).coerceAtMost(length)
            getChars(start, end, chars, 0)
            val buffer = charset.encode(CharBuffer.wrap(chars, 0, end - start))
            val sizeInBytes = buffer.remaining()
            buffer.get(bytes, 0, sizeInBytes)
            copyIn.writeToCopy(bytes, 0, sizeInBytes)
        }
    }

    protected fun String.writeTo(copyIn: CopyIn) {
        val buffer = charset.encode(this)
        val sizeInBytes = buffer.remaining()
        copyIn.writeToCopy(buffer.array(), 0, sizeInBytes)
    }

    override fun initialize(transaction: Transaction) {
        (
                if (debugKeepTmpTable) {
                    "CREATE TABLE $tmpTableName ($tempTableCols)"
                } else {
                    "CREATE TEMP TABLE $tmpTableName ($tempTableCols) ON COMMIT DROP"
                }
                ).let { sql ->
                transaction.prepareStatement(sql).use { it.execute() }
            }

        writer = StringBuilder()
    }

    override fun run(message: PipelineMessage.Payload<T>, transaction: Transaction): PipelineMessage<T> {
        if (message.hasWriteFlag) {
            columns.entries.forEachIndexed { index, (_, def) ->
                with(writer!!) {
                    def.extractor(message.payload)?.let { v ->
                        append(textLimiter)
                        append(v.toString())
                        append(textLimiter)
                    }
                    if (index < columns.entries.size - 1) {
                        append(columnLimiter)
                    }
                }
            }

            writer!!.append(newLine)
            writtenCounter.incrementAndGet()

            flush(transaction)
        }

        return message
    }

    protected open fun flush(transaction: Transaction, force: Boolean = false): Boolean {
        if (!force && writtenCounter.get() % batchSize != 0L) {
            return false
        }

        logger.info("Flushing CopyWriter: ${writtenCounter.get()}")
        val copyIn = CopyManager(transaction.baseConnection as BaseConnection)
            .copyIn("COPY $tmpTableName FROM STDIN DELIMITER '$columnLimiter' CSV header")

        var copied: Long
        val ms = measureTimeMillis {
            columns.keys.joinToString(columnLimiter.toString()).writeTo(copyIn)
            newLine.toString().writeTo(copyIn)

            writer!!.writeTo(copyIn)

            copied = copyIn.endCopy()
            writer = StringBuilder()
            System.gc()
        }

        timing.add(ms)
        debug { "Current exec: $ms ms, avg exec time: $timing" }

        insertCounter.addAndGet(copied)

        return true
    }

    override fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<T> {
        if (flags.contains(PipelineMessage.Flags.FLUSH)) {
            debug { "Received FLUSH update" }
            close(transaction)
        }

        return PipelineMessage.Update(flags)
    }

    override fun close(transaction: Transaction): PipelineMessage<T> {
        if (writer?.isNotEmpty() == true) {
            flush(transaction, true)
            var ins: Long
            val ms = measureTimeMillis {
                transaction.prepareStatement("analyze $tableName").use { it.execute() }
                transaction.prepareStatement("analyze $tmpTableName").use { it.execute() }

                ins = transaction
                    .prepareStatement(sql).use { stmt ->
                        val lines = stmt.executeLargeUpdate()
                            .takeIf { !simpleCopy }
                            ?: insertCounter.get()

                        logStatementMessages(stmt)

                        lines
                    }

                transaction.prepareStatement("analyze $tableName").use { it.execute() }
                transaction.prepareStatement("analyze $tmpTableName").use { it.execute() }
            }

            logger.info(
                "Copied $writtenCounter entries (avg: ${timing.averageExecutionMilliseconds} ms), " +
                        "inserted $ins into $tableName (writing time: $ms ms)"
            )
        }
        return PipelineMessage.Close()
    }

    override fun write(message: PipelineMessage.Payload<T>, transaction: Transaction) {
        error("Do not use write()")
    }
}
