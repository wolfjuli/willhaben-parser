package solutions.lykos.willhaben.parser.backend.importer.actions.checks

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage


sealed class Check<T : Node> : Action<T>() {

    abstract class CheckOnce<T : Node> : Check<T>() {
        open fun checkOnBeginning(transaction: Transaction) {}
        open fun checkBeforeClose(transaction: Transaction) {}

        final override fun run(
            message: PipelineMessage.Payload<T>,
            transaction: Transaction
        ): PipelineMessage<T> = message

        final override fun initialize(transaction: Transaction) = checkOnBeginning(transaction)

        final override fun close(transaction: Transaction): PipelineMessage<T> {
            checkBeforeClose(transaction)
            return super.close(transaction)
        }
    }

    abstract class CheckElements<T : Node> : Check<T>() {

        final override fun run(
            message: PipelineMessage.Payload<T>,
            transaction: Transaction
        ): PipelineMessage<T> {
            checkElement(message)
            return message
        }

        abstract fun checkElement(element: PipelineMessage.Payload<T>)
    }
}


