package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction


abstract class Transformer<T : Node> : Action<T>() {
    override fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T> {
        return transformEntry(message)
    }

    abstract fun transformEntry(entry: T): T

    open fun transformEntry(message: PipelineMessage.Payload<T>): PipelineMessage.Payload<T> =
        message.copy(transformEntry(message.payload))

    abstract fun updateResolving(transaction: Transaction)

    open fun initResolving(transaction: Transaction) = updateResolving(transaction)

    override fun initialize(transaction: Transaction) {
        initResolving(transaction)
        super.initialize(transaction)
    }

    override fun update(transaction: Transaction): PipelineMessage<T> {
        updateResolving(transaction)
        return super.update(transaction)
    }
}


