package solutions.lykos.willhaben.parser.backend.api.wh

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val zoneId = ZoneId.of("Europe/Vienna")
data class WHAttributes(
    val attribute: MutableList<AttributeEntry>
) {
    data class AttributeEntry(
        val name: String,
        val values: List<String?>?
    ) {
        fun toInt() = values?.firstOrNull()?.toInt()
        fun toLong() = values?.firstOrNull()?.toLong()
        fun toFloat() = values?.firstOrNull()?.toFloat()
        fun toDateTime(): ZonedDateTime? = values?.firstOrNull()?.let { v ->
            (try {
                v.toLong()
            } catch (e: Exception) {
                null
            })?.let { i ->
                ZonedDateTime.of(LocalDateTime.ofEpochSecond(i, 0, ZoneOffset.UTC), zoneId)
            } ?: ZonedDateTime.parse(values.first())
        }

    }

}
