package solutions.lykos.willhaben.parser.backend.importer.actions

import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.Resolver
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*

class ResolvingActions<T : Node, K : MutableMap<*, *>>(
    private val resolver: Resolver<T, K>,
    vararg actions: Action<T>
) : ActionSequence<T>(*actions) {


    override fun run(
        message: PipelineMessage.Payload<T>,
        transaction: Transaction
    ): PipelineMessage<T> {
        val result: PipelineMessage<T> = resolver(message, transaction)
        //If resolver sends stop, it couldn't resolve. Add to our pipeline for writing
        if (result is PipelineMessage.Stop) {
            debug { "Resolve failed " }
            if (message.hasWriteFlag) {
                debug { "hasWrite - passing on to actions" }
                super.run(message, transaction)
            }
        } else
            debug { "Resolve OK" }

        return result
    }

    override fun update(transaction: Transaction, flags: EnumSet<PipelineMessage.Flags>): PipelineMessage<T> {
        return super.update(transaction, flags).also { resolver.update(transaction, flags) }
    }

    override fun close(transaction: Transaction): PipelineMessage<T> {
        if (resolver.close(transaction) is PipelineMessage.Stop) {
            debug { "Resolver cant close - cancel close at this point" }
            return PipelineMessage.Stop()
        }

        return super.close(transaction)
    }

}
