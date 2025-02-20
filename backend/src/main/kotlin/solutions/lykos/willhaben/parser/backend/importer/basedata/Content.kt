package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField

data class Content(
    @IdField
    val id: Int,
    @HashField
    val hash: String,
    val url: String,
    val raw: String
) : Node {
    override fun equals(other: Any?): Boolean =
        other is Content &&
                other.hash == hash

    override fun hashCode(): Int {
        return hash.hashCode()
    }

}
