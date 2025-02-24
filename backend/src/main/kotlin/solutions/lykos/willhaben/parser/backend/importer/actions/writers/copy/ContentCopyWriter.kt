package solutions.lykos.willhaben.parser.backend.importer.actions.writers.copy

import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing

class ContentCopyWriter : CopyWriter<Listing>("contents", true) {

    override val columns: Map<String, ValueDef<Listing>> = mapOf(
        "id" to PGInteger { willhabenId.toString() },
        "hash" to PGString { hash },
        "raw" to PGJsonb { raw.toJson() }
    )
}
