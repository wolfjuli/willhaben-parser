package solutions.lykos.willhaben.parser.backend.postgresql

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
