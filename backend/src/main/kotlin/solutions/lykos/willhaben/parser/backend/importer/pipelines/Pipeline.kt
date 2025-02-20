package solutions.lykos.willhaben.parser.backend.importer.pipelines

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.io.Writer
import java.util.*
import java.util.concurrent.CancellationException

class Pipeline<T : Node>(
    private val actions: ActionSequence<T>,
    val name: String
) {
    protected val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }
    protected fun debug(text: () -> String) {
        if (logger.isDebugEnabled) logger.debug(text())
    }

    companion object {
        inline operator fun <reified T : Node> invoke(actions: ActionSequence<T>) =
            Pipeline(actions, T::class.java.simpleName)
    }

    private lateinit var transaction: Transaction
    fun initialize(transaction: Transaction) {
        this.transaction = transaction
    }

    private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
    fun offer(obj: T?) = obj?.let {
        offer(PipelineMessage.Payload(it, flags = writeFlags.clone()))
    }

    fun offer(objects: List<T?>?) = objects?.forEach { obj -> offer(obj) }

    private fun offer(message: PipelineMessage.Payload<T>) {
        if (!this::transaction.isInitialized)
            error("Called pipeline offer() before initialize()")

        debug { "offer ${message.payload}" }

        actions(message, transaction)
    }

    fun close(cause: CancellationException? = null): PipelineMessage<T> {
        if (cause != null)
            logger.error("EXCEPTION OCCURRED - Initiate close of ${this::class.java.simpleName}<$name>")
        else
            logger.info("Initiate close of ${this::class.java.simpleName}<$name>")
        return actions.close(transaction)
    }

    fun report(writer: Writer) {
        actions(PipelineMessage.Report(writer), transaction)
    }

    fun flush(): PipelineMessage<T> {
        return actions(PipelineMessage.Update(EnumSet.of(PipelineMessage.Flags.FLUSH)), transaction)
    }

    fun flush(flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<T> {
        return actions(PipelineMessage.Update(flags.clone().also { it.add(PipelineMessage.Flags.FLUSH) }), transaction)
    }
}
