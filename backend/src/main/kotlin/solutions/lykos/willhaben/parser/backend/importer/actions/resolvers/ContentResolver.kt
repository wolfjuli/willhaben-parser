package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.HashMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.Content
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class ContentResolver : HashResolver<Content>() {
    override fun resolveEntry(entry: Content): Content? =
        entry.takeIf { objects.contains(entry.toIdentityObject().hash) }

    override fun updateResolving(transaction: Transaction): HashMapping =
        ImporterFetcher.fetchHashes<Content>(transaction)
}
