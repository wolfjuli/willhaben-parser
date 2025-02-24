package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField

data class Listing(
    val willhabenId: Int,
    @HashField
    val hash: String,
    val duplicateHash: String,
    val url: String,
    val raw: WHAdvertSummary
) : Node {
    override fun equals(other: Any?): Boolean =
        other is Listing &&
                other.hash == hash

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    @IdField
    var id: Int? = null
}
