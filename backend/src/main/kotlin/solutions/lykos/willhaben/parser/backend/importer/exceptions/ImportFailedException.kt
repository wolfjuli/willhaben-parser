package solutions.lykos.willhaben.parser.backend.importer.exceptions

class ImportFailedException(customMessage: String? = null) : Exception() {
    override val message: String = customMessage
        ?: "Import failed due to an unexpected internal server error"
}
