package solutions.lykos.willhaben.parser.backend.database.postgresql

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import solutions.lykos.willhaben.parser.backend.camelCase
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

object TransactionConstants {
    const val FETCH_SIZE: Int = 50000
}

inline fun <T> Connection.useTransaction(block: (transaction: Transaction) -> T): T =
    use {
        it.transaction(block)
    }

inline fun <T> Connection.transaction(block: (transaction: Transaction) -> T): T {
    autoCommit = false

    return try {
        block(Transaction(this)).also {
            commit()
        }
    } catch (e: Throwable) {
        runBlocking {
            try {
                withTimeout(30000) {
                    rollback()
                }
            } catch (e: Exception) {
                throw Exception(
                    "An error occurred, but ROLLBACK timed out (probably locked by CopyWriter), see cause for details",
                    e
                )
            }
        }
        throw e
    } finally {
        try {
            autoCommit = true
        } catch (_: Exception) {
        }
    }
}

fun <R : Any> PreparedStatement.useAsSequence(block: (Sequence<ResultSet>) -> R) =
    use {
        it.fetchSize = TransactionConstants.FETCH_SIZE
        it.executeQuery().useAsSequence(block)
    }

fun <R : Any> ResultSet.useAsSequence(block: (Sequence<ResultSet>) -> R) =
    use { block(generateSequence { it.takeIf { it.next() } }) }

val jsonMapper = jsonObjectMapper()
inline fun <reified R : Any> ResultSet.getTypedValue(idx: Int): R? =
    when (metaData.getColumnTypeName(idx)?.lowercase()) {
        "text" -> getString(idx) as R?
        "serial" -> getInt(idx) as R?
        "smallserial" -> getInt(idx) as R?
        "jsonb" -> jsonMapper.readValue<R?>(getString(idx))
        "int4" -> getInt(idx) as R?
        "int2" -> getInt(idx) as R?
        "float4" -> getFloat(idx) as R?
        "float8" -> getFloat(idx) as R?
        else -> error("Cant handle type '${metaData.getColumnTypeName(idx)?.lowercase()}'")
    }

fun ResultSet.toCamelCaseMap(): Map<String, Any?> =
    (1..metaData.columnCount).associate {
        metaData.getColumnName(it).camelCase() to getTypedValue(it)
    }

