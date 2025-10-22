package solutions.lykos.willhaben.parser.backend.importer.parameters

import java.time.LocalDate

/**
 * @author Gunnar Schulze
 */
object Date : Parameter<LocalDate>("date") {

    private const val DATE = "date"
    private const val DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}"

    override val patterns: Map<String, String> = mapOf(
        DATE to DATE_PATTERN,
    )

    override fun extract(values: Map<String, String>): LocalDate? {
        return values[DATE]?.let(LocalDate::parse)
    }
}
