package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField

data class ListingAttribute(
    val listing: Listing,
    val attribute: Attribute,
    val values: List<String>
) : Node {

    @HashField
    val listingId get() = listing.id

    @HashField
    val attributeId get() = attribute.id

}
