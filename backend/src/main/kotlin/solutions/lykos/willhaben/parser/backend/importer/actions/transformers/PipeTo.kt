package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import solutions.lykos.willhaben.parser.backend.importer.actions.Action
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.matchType
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*

class PipeTo<F : Node, T : Node>(
    val targetActions: ActionSequence<T>,
    private val transform: F.() -> T? = { this as T },
    private val transformBack: (old: F, resolved: T) -> F? = { _, _ -> null },
    private val overrideFlags: EnumSet<PipelineMessage.Flags>? = null
) : Action<F>() {
    override fun run(message: PipelineMessage.Payload<F>, transaction: Transaction): PipelineMessage<F> {
        return transform(message.payload)?.let { transformed ->
            val newMessage = PipelineMessage.Payload(transformed, overrideFlags ?: message.flags)
            val result = targetActions(newMessage, transaction)
            result.matchType { transformBack(message.payload, this) ?: message.payload }
        } ?: message
    }
}
