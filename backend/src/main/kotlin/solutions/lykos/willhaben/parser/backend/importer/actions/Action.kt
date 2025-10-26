package solutions.lykos.willhaben.parser.backend.importer.actions

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.importer.toBulk
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class Action<T : Node> {
    protected val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }
    protected val emptyFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)

    private val isDebugEnabled: Boolean by lazy { logger.isDebugEnabled }
    private var initializeMutex = Mutex(false)
    private var initialized = false

    protected fun debug(text: () -> String) {
        if (isDebugEnabled) logger.debug(text())
    }

    operator fun invoke(message: PipelineMessage<T>, transaction: Transaction): PipelineMessage<T> {
        if (message !is PipelineMessage.Close)
            if (!initialized) {
                runBlocking {
                    initializeMutex.withLock {
                        //We are one of many waiting on the lock, but we were too late
                        if (initialized) { return@withLock}
                        debug { "initialize action" }
                        initialize(transaction)
                        initialized = true
                    }
                }
            }

        return when (message) {
            is PipelineMessage.Stop<T> -> {
                error("A Stop message has been passed on - this should not happen")
            }

            is PipelineMessage.Payload<T> -> {
                run(message, transaction)
            }

            is PipelineMessage.Update<T> -> {
                debug { "update" }
                update(transaction, message.flags)
            }

            is PipelineMessage.Close -> {
                debug { "close" }
                close(transaction)
            }

            is PipelineMessage.Report<T> -> {
                debug { "report" }
                report(message, transaction)
            }

            is PipelineMessage.Bulk<T> -> {
                debug { "bulk (${message.messages.size} messages)" }
                message.messages.mapNotNull {
                    this@Action.invoke(it, transaction).takeUnless { it is PipelineMessage.Stop }
                }.toBulk().simplify()
            }

            else -> error("Unknown message type: ${javaClass.simpleName}")
        }
    }

    protected abstract fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T>

    open fun initialize(transaction: Transaction) {}

    private val updateMessage = PipelineMessage.Update<T>()

    open fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>) =
        update(transaction).also { it.flags.addAll(flags) }

    open fun update(transaction: Transaction): PipelineMessage<T> = updateMessage

    private val closeMessage = PipelineMessage.Close<T>()
    open fun close(transaction: Transaction): PipelineMessage<T> = closeMessage
    open fun report(message: PipelineMessage.Report<T>, transaction: Transaction): PipelineMessage<T> = message
}
