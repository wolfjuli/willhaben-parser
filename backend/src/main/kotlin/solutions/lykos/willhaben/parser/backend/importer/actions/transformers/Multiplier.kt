package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.importer.toBulk
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*

abstract class Multiplier<T : Node> : Transformer<T>() {

    override fun transformEntry(entry: T): T {
        error("Do not use this function here")
    }

    abstract fun multiply(entry: T, transaction: Transaction): Pair<T?, List<T?>>

    override fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T> {
        val (init, new) = multiply(message.payload, transaction).let {
            it.first?.let { message.copy(it) } to
                    it.second.mapNotNull {
                        it?.let {
                            PipelineMessage.Payload(
                                payload = it,
                                flags = message.flags.clone()
                                    .also { it.addAll(EnumSet.of(PipelineMessage.Flags.MULTIPLIED)) }
                            )
                        }
                    }
        }

        return (listOfNotNull(init) + new)
            .takeIf { it.isNotEmpty() }
            ?.toBulk()
            ?: PipelineMessage.Stop()
    }
}
