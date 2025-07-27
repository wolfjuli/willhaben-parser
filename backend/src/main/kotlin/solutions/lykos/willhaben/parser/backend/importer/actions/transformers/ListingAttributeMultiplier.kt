package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute

class ListingAttributeMultiplier : Multiplier<ListingAttribute>() {
    override fun multiply(
        entry: ListingAttribute,
        transaction: Transaction
    ): Pair<ListingAttribute?, List<ListingAttribute?>> {
        return if (entry.attribute.attribute == "")
            null to entry.listing.raw.attributeMap.mapNotNull {
                it.value?.let { vals ->
                    ListingAttribute(
                        entry.listing,
                        Attribute(it.key),
                        vals.filterNotNull()
                    )
                }
            }
        else
            entry to emptyList()
    }

    override fun updateResolving(transaction: Transaction) {}
}
