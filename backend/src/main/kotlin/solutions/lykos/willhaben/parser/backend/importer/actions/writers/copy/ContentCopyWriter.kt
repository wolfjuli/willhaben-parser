package solutions.lykos.willhaben.parser.backend.importer.actions.writers.copy

import solutions.lykos.willhaben.parser.backend.importer.basedata.Content
import solutions.lykos.willhaben.parser.backend.postgresql.escapeSql

class ContentCopyWriter : CopyWriter<Content>("contents", true) {

    override val columns: Map<String, ValueDef<Content>> = mapOf(
        "id" to PGInteger { id.toString() },
        "hash" to PGString { hash },
        "raw" to PGJsonb { raw.escapeSql() }
    )
}
