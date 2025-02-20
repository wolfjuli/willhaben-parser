package solutions.lykos.willhaben.parser.backend.importer.actions.resolvers

import solutions.lykos.willhaben.parser.backend.importer.DefaultMapping
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

class AttributeResolver : DefaultResolver<Attribute>() {
    override fun resolveEntry(entry: Attribute): Attribute? =
        objects[entry.attribute]?.let { id -> entry.also { it.id = id } }

    override fun updateResolving(transaction: Transaction): DefaultMapping =
        ImporterFetcher.fetchMapping<Attribute>(transaction, columnName = "attribute")
}
