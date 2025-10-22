package solutions.lykos.willhaben.parser.backend.importer.parameters

/**
 * @author Gunnar Schulze
 */
object Country : Parameter<String>("country") {

    private const val COUNTRY = "country"

    override val patterns: Map<String, String>
        get() = mapOf(COUNTRY to "[A-Z]{2}")

    override fun extract(values: Map<String, String>): String? {
        return values[COUNTRY]
    }
}
