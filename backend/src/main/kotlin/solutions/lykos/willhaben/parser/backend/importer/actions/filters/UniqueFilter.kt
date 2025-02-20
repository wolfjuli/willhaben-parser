package solutions.lykos.willhaben.parser.backend.importer.actions.filters

import solutions.lykos.willhaben.parser.backend.importer.Hash
import solutions.lykos.willhaben.parser.backend.importer.HashMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.HashedObject
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.util.*
import java.util.Collections.synchronizedMap

open class UniqueFilter<T : Node>(
    val fetchHashes: ImporterFetcher.(Transaction) -> MutableMap<Hash, HashedObject>,
    private val hashObjects: HashMapping,
    toggleFlags: EnumSet<PipelineMessage.Flags> = EnumSet.of(PipelineMessage.Flags.WRITE)
) : Filter.FilterElements<T>(toggleFlags) {

    companion object {
        inline operator fun <reified T : Node> invoke() =
            UniqueFilter<T>({ fetchHashes<T>(it) }, getList())

        inline operator fun <reified T : Node> invoke(noinline fetchHashes: ImporterFetcher.(Transaction) -> MutableMap<Hash, HashedObject>) =
            UniqueFilter<T>(fetchHashes, getList())

        fun getList(): HashMapping = synchronizedMap(mutableMapOf())
    }


    final override fun initResolving(transaction: Transaction) {
        synchronized(hashObjects) {
            hashObjects.clear()
            hashObjects.putAll(fetchHashes(ImporterFetcher, transaction))
        }
    }

    override fun updateResolving(transaction: Transaction) {}

    final override fun checkElement(message: PipelineMessage.Payload<T>) {
        with(message) {
            payload.toIdentityObject().also { id ->
                synchronized(hashObjects) {
                    if (id.hash !in hashObjects) {
                        if (flags.containsAll(toggleFlags)) {
                            hashObjects[id.hash] = id
                        }
                    } else {
                        debug { "Object known - removing flags" }
                        flags.removeAll(toggleFlags)
                    }
                }
            }
        }
    }

}
