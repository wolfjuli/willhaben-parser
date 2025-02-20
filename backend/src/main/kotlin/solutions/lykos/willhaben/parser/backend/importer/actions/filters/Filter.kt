package solutions.lykos.willhaben.parser.backend.importer.actions.filters

import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*

sealed class Filter<T : Node>(val toggleFlags: EnumSet<PipelineMessage.Flags>) : Action<T>() {

    override fun initialize(transaction: Transaction) = initResolving(transaction)
    override fun update(transaction: Transaction): PipelineMessage<T> {
        updateResolving(transaction)
        return super.update(transaction)
    }

    abstract fun updateResolving(transaction: Transaction)

    open fun initResolving(transaction: Transaction) = updateResolving(transaction)

    abstract class FilterElements<T : Node>(toggleFlags: EnumSet<PipelineMessage.Flags>) : Filter<T>(toggleFlags) {
        final override fun run(
            message: PipelineMessage.Payload<T>,
            transaction: Transaction
        ): PipelineMessage<T> {
            checkElement(message)
            return message
        }

        abstract fun checkElement(message: PipelineMessage.Payload<T>)
    }
}
