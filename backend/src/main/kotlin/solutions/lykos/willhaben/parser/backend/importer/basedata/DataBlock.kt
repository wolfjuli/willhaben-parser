package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField
import java.time.ZonedDateTime

data class DataBlock(
    val listing: Listing,

    @HashField
    val timestamp: ZonedDateTime,

    @IdField
    var id: Int? = null,
) : Node {
    @HashField
    val listingId get() = listing.id
}
