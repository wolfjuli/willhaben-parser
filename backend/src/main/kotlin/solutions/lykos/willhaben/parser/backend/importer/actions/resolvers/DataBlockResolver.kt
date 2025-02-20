package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.HashMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.DataBlock
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class DataBlockResolver : HashResolver<DataBlock>() {
    override fun resolveEntry(entry: DataBlock): DataBlock? =
        objects[entry.toIdentityObject().hash]?.let { id -> entry.also { it.id = id.identity.toInt() } }

    override fun updateResolving(transaction: Transaction): HashMapping =
        ImporterFetcher.fetchHashes<DataBlock>(transaction)
}
