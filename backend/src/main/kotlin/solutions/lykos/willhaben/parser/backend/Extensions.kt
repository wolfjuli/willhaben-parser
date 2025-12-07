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
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

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
    first().lowercase() + snakeCaseRegex.replace(substring(1)) {
        it.value[1].uppercase()
    }

fun Float.ceilToInt() = ceil(this).toInt()
fun Double.ceilToInt() = ceil(this).toInt()


fun jsonObjectMapper() = jacksonObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())

private val specialCharsRegex = "[^a-zA-Z0-9]".toRegex()
private val multiSnakeRegex = "_[_]+".toRegex()
fun String.normalized() = this
    .lowercase()
    .replace(specialCharsRegex, "_")
    .replace(multiSnakeRegex, "_")
    .camelCase()


fun Any.getJsonPaths(): Set<String> {
    return getJsonPaths(this)
}

fun <T : Any> getJsonPaths(obj: T, prefix: String = ""): Set<String> {
    val paths = mutableSetOf<String>()
    paths.add(prefix)

    when (obj) {
        is String,
        is Int,
        is Long,
        is Double,
        is Float,
        is Boolean,
        is Char -> {
            //nothing to do here - prefix was already added
        }

        is Array<*>,
        is Collection<*> -> {
            val c = (obj as? Collection<*>) ?: (obj as? Array<*>)?.toList()
            paths.add("$prefix[*]")
            c?.filterNotNull()?.forEach { value ->
                paths += getJsonPaths(value, "$prefix[*]")
            }
        }

        is Map<*, *> -> {
            obj.keys.forEach { key ->
                val path = if (prefix.isNotEmpty()) "$prefix.$key" else key.toString()
                paths.add(path)

                obj[key]?.let { value ->
                    paths += getJsonPaths(value, path)
                }
            }
        }

        else -> {
            (obj::class as KClass<T>).memberProperties.filter { it.javaField != null }.forEach { property ->
                try {
                    val path = if (prefix.isNotEmpty()) "$prefix.${property.name}" else property.name
                    paths.add(path)

                    property.get(obj)?.let { value ->
                        paths += getJsonPaths(value, path)
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }

    return paths.filter { it.isNotEmpty() && !paths.contains("$it[*]") }.toSet()
}

