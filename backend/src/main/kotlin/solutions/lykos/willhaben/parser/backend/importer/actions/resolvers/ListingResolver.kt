package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.DefaultMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class ListingResolver : DefaultResolver<Listing>() {
    override fun resolveEntry(entry: Listing): Listing? =
        objects[entry.hash]?.let { id -> entry.also { it.id = id } }

    override fun updateResolving(transaction: Transaction): DefaultMapping =
        ImporterFetcher.fetchMapping<Listing>(transaction, columnName = "hash")

}
