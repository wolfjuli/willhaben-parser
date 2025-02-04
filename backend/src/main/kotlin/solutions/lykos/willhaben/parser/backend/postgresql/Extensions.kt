package solutions.lykos.willhaben.parser.backend.postgresql

import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*
import java.util.function.Supplier

private val sqlEscapeRegex = Regex("['\"\\\\]")

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
