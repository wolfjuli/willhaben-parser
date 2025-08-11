package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.getJsonPaths
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class ListingAttributeMultiplier : Multiplier<ListingAttribute>() {


    override fun multiply(
        entry: ListingAttribute,
        transaction: Transaction
    ): Pair<ListingAttribute?, List<ListingAttribute?>> {
        return if (entry.attribute.attribute == "") {
            null to entry.listing.raw.getJsonPaths().map { field ->
                ListingAttribute(
                    entry.listing,
                    Attribute(field),
                    ""
                )
            }
        } else
            entry to emptyList()
    }

    override fun updateResolving(transaction: Transaction) {}
}
