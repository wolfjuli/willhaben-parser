package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField

data class Attribute(

    @HashField
    val attribute: String,

    @IdField
    var id: Int? = null,
) : Node
