package solutions.lykos.willhaben.parser.backend.importer.actions.filters

import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*

class BlacklistFilter<T : Node, R : Any?>(
    val selector: T.() -> R,
    val blackList: Set<R>
) : Filter.FilterElements<T>(EnumSet.of(PipelineMessage.Flags.WRITE)) {


    override fun updateResolving(transaction: Transaction) {
    }

    override fun checkElement(message: PipelineMessage.Payload<T>) {
        if (message.payload.selector() in blackList)
            message.flags.removeAll(toggleFlags)
    }
}
