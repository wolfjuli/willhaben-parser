package solutions.lykos.willhaben.parser.backend.importer.actions

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.ImporterConstants
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.emptyBufferList
import solutions.lykos.willhaben.parser.backend.importer.matchType
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.importer.toBulk
import java.util.*

class Communicator<F : Node, T : Node, K : MutableMap<*, *>>(
    private val actions: ResolvingActions<T, K>,
    @Suppress("UNCHECKED_CAST")
    private val transform: F.() -> T? = { this as T },
    @Suppress("UNCHECKED_CAST")
    private val transformBack: (old: F, resolved: T) -> F = { _, r -> r as F },
    private val overrideFlags: EnumSet<PipelineMessage.Flags>? = null,
    private val allowFailedResolve: Boolean = true,
    fromClass: Class<F>,
    private val toClass: Class<T>
) : Action<F>() {

    companion object {
        inline operator fun <reified F : Node, reified T : Node, K : MutableMap<*, *>> invoke(
            actions: ResolvingActions<T, K>,
            noinline transform: F.() -> T? = { this as T },
            noinline transformBack: (old: F, resolved: T) -> F = { _, r -> r as F },
            overrideFlags: EnumSet<PipelineMessage.Flags>? = null,
            allowFailedResolve: Boolean = true,
        ): Communicator<F, T, K> {
            return Communicator(
                actions,
                transform,
                transformBack,
                overrideFlags,
                allowFailedResolve,
                F::class.java,
                T::class.java
            )
        }
    }

    private val bufferSize = ImporterConstants.PIPELINE_BUFFER_SIZE
    private val buffer = emptyBufferList<F>(bufferSize)
    private val header =
        "Resolver for ${fromClass.simpleName} -> ${toClass.simpleName} failed to resolve the following entries:"

    private fun counterOverflow(counter: Int) =
        counter >= bufferSize && counter % bufferSize == 0

    override fun run(message: PipelineMessage.Payload<F>, transaction: Transaction): PipelineMessage<F> =
        transformTo(message)?.let { newMessage ->
            removeFromBuffer(message)
            val result = actions(newMessage, transaction)

            result.matchType { transformBack(message.payload, this) }
                .takeIf { it is PipelineMessage.Payload || it is PipelineMessage.Bulk }
                ?.also { it.flags.clear(); it.flags.addAll(message.flags) }
                ?: (
                        if (allowFailedResolve) addToBuffer(message, transaction)
                        else error("$header ${newMessage.payload.prettify()}")
                        ).takeUnless { it is PipelineMessage.Update }
                ?: PipelineMessage.Stop()
        } ?: message

    private fun transformTo(message: PipelineMessage.Payload<F>): PipelineMessage.Payload<T>? =
        message.payload.transform()?.let { transformed ->
            PipelineMessage.Payload(
                message.uuid,
                transformed,
                (overrideFlags ?: message.flags).clone()
            )
        }

    /***
     * Returns :
     *  Bulk - buffer was not empty and resolve worked out. Always contains an update message as last message
     *  Stop - buffer was not empty but no resolve worked out
     *  Update - buffer was empty - no resend necessary
     */
    private fun resend(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<F> {

        return synchronized(buffer) {
            if (buffer.isEmpty())
                return PipelineMessage.Update(flags)

            val newBuffer = ArrayList<PipelineMessage.Payload<F>>(buffer.size)

            debug { "Checking ${buffer.size} for resend" }

            val oldBuffer = ArrayList(buffer)

            val newMessage = oldBuffer.mapNotNull { msg ->
                transformTo(msg)
                    ?.let { transformed ->
                        actions(transformed, transaction)
                            .matchType { transformBack(msg.payload, this) }
                            .also { it.flags.clear(); it.flags.addAll(msg.flags) }
                    }
                    .takeIf { it !is PipelineMessage.Stop }
                    ?: newBuffer.add(msg).let { null }
            }.toBulk()


            debug { "Resolved ${buffer.size - newBuffer.size} entries (${newBuffer.size} go back to buffer)" }

            buffer.clear()
            buffer.addAll(newBuffer)


            if (newMessage.messages.isNotEmpty())
                newMessage.add(PipelineMessage.Update(flags)).simplify()
            else
                PipelineMessage.Stop()
        }
    }

    /**
     * Registers the given payload [message] to the buffer
     *
     * Returns:
     *  Stop - added to buffer OR buffer was full but no resolve worked out
     *  Bulk - buffer full and resolve worked out. Always contains an update message as last message
     *  Update - buffer was empty - no resend necessary
     */
    private fun addToBuffer(message: PipelineMessage.Payload<F>, transaction: Transaction): PipelineMessage<F> {
        val currentSize = synchronized(buffer) {
            buffer.add(message)
            buffer.size
        }
        debug { "added to buffer - size now $currentSize" }

        return if (counterOverflow(currentSize)) {
            debug { "$currentSize objects in buffer - try update and resend" }
            update(transaction)
        } else {
            PipelineMessage.Stop()
        }
    }

    /**
     * If there are multiple buffers in series, messages get resent. This here avoid multiplication
     */
    private fun removeFromBuffer(message: PipelineMessage.Payload<F>) {
        synchronized(buffer) {
            buffer.remove(message)
        }
    }

    override fun update(transaction: Transaction): PipelineMessage<F> =
        update(transaction, emptyFlags)


    /***
     * Returns :.
     *  Bulk - buffer was not empty and resolve worked out. Always contains an update message as last message
     *  Stop - buffer was not empty but no resolve worked out
     *  Update - buffer was empty - no resend necessary
     */
    override fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<F> {
        debug { "Received UPDATE ${"with FLUSH".takeIf { flags.contains(PipelineMessage.Flags.FLUSH) } ?: ""}" }
        actions(PipelineMessage.Update(flags), transaction)
        return resend(transaction, flags)
    }

    override fun close(transaction: Transaction): PipelineMessage<F> {
        if (buffer.isNotEmpty()) {
            val flush = EnumSet.of(PipelineMessage.Flags.FLUSH)
            val update = PipelineMessage.Update<T>(flush)
            actions(update, transaction)
            return resend(transaction, flush).takeUnless { it is PipelineMessage.Update } ?: super.close(transaction)
        }

        debug { "Empty buffer - closing actions" }
        return when (val res = actions.close(transaction)) {
            is PipelineMessage.Close -> super.close(transaction)
            is PipelineMessage.Stop -> PipelineMessage.Stop()
            else -> error("Communicator can't handle message type ${res.javaClass.simpleName} on close")
        }
    }

    private fun Node.prettify(): String =
        try {
            mergeValues(false, ", ")
        } catch (e: Exception) {
            toString()
        }

    override fun report(message: PipelineMessage.Report<F>, transaction: Transaction): PipelineMessage<F> {
        val waiting: Map<String, Pair<Int, List<String>>> = synchronized(buffer) {
            buffer
                .asSequence()
                .mapNotNull { it.payload.transform()?.let { to -> to.prettify() to it.payload.prettify() } }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.size to it.value.distinct() }
        }

        if (waiting.isNotEmpty()) {

            message.writer.write(
                """
                    |$header
                    |  ${
                    waiting.entries.joinToString("\n  ") {
                        "These ${it.value.first} entries waiting on  ${toClass.simpleName}(${it.key}):\n    " + it.value.second.joinToString(
                            "\n    "
                        )
                    }
                }
                    |---------------------------------------------------------------------------------------------------
                    |
                """.trimMargin()
            )
        }

        val toReportMessage =
            message.matchType<F, T> { error("This should not be a payload message") } as PipelineMessage.Report

        actions.report(toReportMessage, transaction)

        return super.report(message, transaction)
    }
}
