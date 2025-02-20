package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.KeyNotFoundException
import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*
import java.util.Collections.synchronizedMap

abstract class Resolver<T : Node, K : MutableMap<*, *>>(
) : Action<T>() {
    protected val objects: K = synchronizedMap(mutableMapOf<String, MutableMap<Any, Any?>>()) as K

    abstract fun resolveEntry(entry: T): T?
    abstract fun updateResolving(transaction: Transaction): K

    protected open fun flushedUpdate(transaction: Transaction) {
        update(transaction)
    }

    protected open fun simpleUpdate(transaction: Transaction) {
        update(transaction)
    }

    override fun initialize(transaction: Transaction) {
        objects.putAll(updateResolving(transaction) as Map<Nothing, Nothing>)
    }

    open fun resolveEntry(entry: T, flags: EnumSet<PipelineMessage.Flags>): T? = resolveEntry(entry)

    override fun run(message: PipelineMessage.Payload<T>, transaction: Transaction): PipelineMessage<T> =
        try {
            resolveEntry(message.payload, message.flags)?.let { resolved -> message.copy(resolved) }
        } catch (e: KeyNotFoundException) {
            null
        } ?: PipelineMessage.Stop<T>().also {
            debug { "Cache miss ${message.payload.javaClass.simpleName}" }
        }


    /**
     * Is called if an Update message arrives (mostly created by the writer)
     */
    override fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<T> {
        if (flags.contains(PipelineMessage.Flags.FLUSH))
            flushedUpdate(transaction)
        else
            simpleUpdate(transaction)

        return super.update(transaction)
    }

    override fun update(
        transaction: Transaction,
    ): PipelineMessage<T> {
        synchronized(objects) {
            objects.clear()
            objects.putAll(updateResolving(transaction) as Map<Nothing, Nothing>)
        }

        return super.update(transaction)
    }


}

