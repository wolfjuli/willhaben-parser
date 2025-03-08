package solutions.lykos.willhaben.parser.backend.importer.actions

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.util.*

open class ActionSequence<T : Node>(vararg actions: Action<T>) : Action<T>() {

    protected val actions: MutableList<Action<T>> = actions.toMutableList()

    override fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T> {
        var result: PipelineMessage<T> = message
        for (action in actions) {
            result = action(result, transaction)

            if (result is PipelineMessage.Stop<*>) {
                debug { "STOP" }
                break
            }
        }

        return result
    }

    override fun close(transaction: Transaction): PipelineMessage<T> {
        var result = super.close(transaction)
        for (action in actions) {
            result = action(result, transaction)
            when (result) {
                is PipelineMessage.Stop -> return result.also {
                    debug { "Intermediate action is not finished - cancel close at this point" }
                }

                else -> continue
            }
        }

        return result.takeIf { it is PipelineMessage.Close } ?: PipelineMessage.Stop()
    }

    override fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<T> {
        var result = super.update(transaction, flags)
        for (action in actions) {
            result = action(result, transaction)

            when (result) {
                is PipelineMessage.Stop -> return result.also {
                    debug { "Intermediate action is not finished - cancel update at this point" }
                }

                else -> continue
            }
        }

        return result
    }

    fun registerAction(vararg actions: Action<T>) = this.actions.addAll(actions.toList())

    fun registerAction(idx: Int, vararg actions: Action<T>) {
        this.actions.addAll(idx, actions.toList())
    }

    override fun report(message: PipelineMessage.Report<T>, transaction: Transaction): PipelineMessage<T> {
        return actions.fold(message as PipelineMessage<T>) { msg, action ->
            if (msg !is PipelineMessage.Stop)
                action(msg, transaction)
            else
                msg
        }
    }
}
