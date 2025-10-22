package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.ImporterConstants
import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

abstract class Writer<T : Node>(protected val tableName: String) : Action<T>() {

    protected data class ExecutionTiming(
        var amount: Int = 0,
        var averageExecutionMilliseconds: Long = 0
    ) {
        fun add(milliseconds: Long) {
            averageExecutionMilliseconds = (averageExecutionMilliseconds * amount + milliseconds) / (amount + 1)
            amount++
        }

        override fun toString(): String =
            "$amount x $averageExecutionMilliseconds ms"
    }

    protected open val batchSize = ImporterConstants.PIPELINE_BUFFER_SIZE
    private var currentBatchSize = AtomicInteger(0)

    private val executeBatchWarningRegex = "Executed [0-9]+ batches".toRegex()
    protected val writtenCounter = AtomicLong(0)

    abstract val columnMappings: Map<String, String>
    lateinit var preparedStatement: PreparedStatement

    protected val timing = ExecutionTiming()

    override fun initialize(transaction: Transaction) {
        preparedStatement = createPreparedInsertStatement(columnMappings, transaction, tableName)
    }

    override fun update(transaction: Transaction): PipelineMessage<T> {
        if (currentBatchSize.get() > 0 && this::preparedStatement.isInitialized) {
            debug { "UPDATE - Writing $currentBatchSize entries to $tableName" }
            executeBatch(preparedStatement)
            writtenCounter.addAndGet(currentBatchSize.get().toLong())
            currentBatchSize.set(0)
        }

        return super.update(transaction)
    }

    override fun close(transaction: Transaction): PipelineMessage<T> {
        if (this::preparedStatement.isInitialized) {
            update(transaction)
            // preparedStatement.close()
        }

        logger.info(
            "Wrote total of $writtenCounter entries into $tableName" +
                    " (avg writing time: ${timing.averageExecutionMilliseconds} ms)"
        )
        return super.close(transaction)
    }

    override fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T> {
        if (message.hasWriteFlag) {
            write(message, transaction)
        }
        return if (currentBatchSize.get() >= batchSize) {
            PipelineMessage.Bulk(listOf(message, update(transaction)))
        } else {
            message
        }
    }

    private val mapper = jacksonObjectMapper().findAndRegisterModules()

    abstract fun write(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    )

    protected fun batchInsert(
        message: PipelineMessage.Payload<T>,
        block: (T, PreparedStatement, Map<String, Int>) -> Unit
    ) {
        val columnMappings = getStatementColumnMappings()
        block(message.payload, preparedStatement, columnMappings)
        preparedStatement.addBatch()
        currentBatchSize.incrementAndGet()
        debug { "$tableName: added to batch (now $currentBatchSize)" }
    }

    protected fun logStatementMessages(statement: PreparedStatement) {
        statement.warnings?.asSequence()?.forEach { warning ->
            warning.message?.let { message ->
                if (executeBatchWarningRegex.matches(message)) {
                    debug { "[SQL]: ${warning.message}" }
                } else {
                    logger.warn("[SQL]: ${warning.message}")
                }
            }
        }
    }

    protected fun executeBatch(statement: PreparedStatement) {
        try {
            val ms = measureTimeMillis {
                statement.executeBatch()
            }

            timing.add(ms)
            debug { "Current exec: $ms ms, avg exec time: $timing" }

            logStatementMessages(statement)

            timing.amount
        } catch (e: SQLException) {
            statement.close()
            error("\n\nError during execute batch: ${e.message} - ${e.cause}")
        }
    }

    protected fun Any?.toJson() =
        mapper.writeValueAsString(this).replace("'", "''")

    @Deprecated("Causes potential memory leak when used for batch insert")
    protected fun Transaction.execute(
        sql: String
    ): Boolean =
        prepareStatement(sql).use { preparedStatement ->
            val res = preparedStatement.use { it.execute() }

            preparedStatement.warnings?.asSequence()?.forEach { warning ->
                logger.warn("[SQL]: ${warning.message}")
            }
            res
        }

    protected fun Set<String>.applyDefaultMapping(): Map<String, String> = associateWith { "?" }
    protected fun getStatementColumnMappings(): Map<String, Int> =
        columnMappings.asIterable().mapIndexed { ix, entry -> entry.key to ix + 1 }.toMap()
}
