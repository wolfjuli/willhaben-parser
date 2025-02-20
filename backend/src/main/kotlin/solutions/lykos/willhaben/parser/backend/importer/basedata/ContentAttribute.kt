package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField

data class ContentAttribute(
    val dataBlock: DataBlock,
    val attribute: Attribute,
    val values: List<String>
) : Node {

    @HashField
    val dataBlockId get() = dataBlock.id

    @HashField
    val attributeId get() = attribute.id

}
