package solutions.lykos.willhaben.parser.backend

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tech.units.indriya.unit.Units
import javax.measure.Quantity
import javax.measure.quantity.Time
import kotlin.math.ceil

fun toSeconds(duration: Quantity<Time>): Long =
    duration.unit
        .getConverterTo(Units.SECOND)
        .convert(duration.value)
        .toLong()

val camelCaseRegex = "([a-z])([A-Z])".toRegex()
val snakeCaseRegex = "_([a-z])".toRegex()

fun String.snakeCase(): String =
    camelCaseRegex.replace(this) {
        "_" + it.value[1].lowercase()
    }

fun String.camelCase(): String =
    snakeCaseRegex.replace(this) {
        it.value[1].uppercase()
    }

fun Float.ceilToInt() = ceil(this).toInt()
fun Double.ceilToInt() = ceil(this).toInt()


fun jsonObjectMapper() = jacksonObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
