package solutions.lykos.willhaben.parser.backend.importer.exceptions

class InvalidImportStructureException(folderName: String) :
    Exception("Invalid import structure. Missing prefixed import order for folder '$folderName'") {

    override val message: String
        get() = super.message!!
}
