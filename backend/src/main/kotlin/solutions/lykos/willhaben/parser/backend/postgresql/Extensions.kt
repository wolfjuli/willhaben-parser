package solutions.lykos.willhaben.parser.backend.postgresql

import java.io.InputStream
import java.io.SequenceInputStream
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Supplier

private val sqlEscapeRegex = Regex("['\"\\\\]")

@JvmName("ListStringPgString")
fun List<String>.toPgString() = joinToString(separator = ",", "{", "}") { "\"${it.replace("\"", "\\\"")}\"" }
fun List<Int>.toPgString() = joinToString(separator = ",", "{", "}")

private class SequenceWrapper<E>(
    sequence: Sequence<E>
) : Enumeration<E> {
    private val iterator = sequence.iterator()

    override fun hasMoreElements(): Boolean = iterator.hasNext()

    override fun nextElement(): E = iterator.next()
}

fun <E> Sequence<E>.asEnumeration(): Enumeration<E> = SequenceWrapper(this)

fun inputStreamOf(vararg suppliers: Supplier<InputStream>): InputStream = inputStreamOf(suppliers.asSequence())

fun inputStreamOf(suppliers: Sequence<Supplier<InputStream>>): InputStream =
    SequenceInputStream(suppliers.map(Supplier<InputStream>::get).asEnumeration())

fun String.escapeSql(): String =
    sqlEscapeRegex.replace(this) { matchResult ->
        when (matchResult.value) {
            "'" -> "''"
            "\"" -> "\"\""
            "\\\\" -> ""
            else -> throw IllegalArgumentException("Unhandled character sequence to escape: ${matchResult.value}")
        }
    }


inline operator fun <reified T> ResultSet.get(
    columnName: String,
    timeZone: ZoneId? = null
): T =
    when (T::class) {
        Boolean::class -> getBoolean(columnName)
        String::class -> getString(columnName)
        Int::class -> getInt(columnName)
        Long::class -> getLong(columnName)
        Float::class -> getFloat(columnName)
        ByteArray::class -> getBytes(columnName)
        LocalDate::class -> LocalDate.parse(getString(columnName))
        LocalDateTime::class -> LocalDateTime.parse(getString(columnName).replace(" ", "T"))
        ZonedDateTime::class ->
            getTimestamp(columnName)
                .toInstant()
                .atZone(timeZone ?: ZoneId.systemDefault())

        else -> throw IllegalArgumentException("Unsupported column type ${T::class.qualifiedName}")
    } as T
