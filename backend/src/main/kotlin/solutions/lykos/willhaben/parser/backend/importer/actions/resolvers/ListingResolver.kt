package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.DefaultMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing

class ListingResolver : DefaultResolver<Listing>() {
    override fun resolveEntry(entry: Listing): Listing? =
        objects[entry.hash]?.let { id -> entry.also { it.id = id } }

    override fun updateResolving(transaction: Transaction): DefaultMapping =
        ImporterFetcher.fetchMapping<Listing>(transaction, columnName = "hash")

}
