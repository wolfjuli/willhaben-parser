package solutions.lykos.willhaben.parser.backend.api.fetchers

import solutions.lykos.willhaben.parser.backend.postgresql.Transaction

interface Fetcher<T> {

    fun get(transaction: Transaction): List<T>

    fun post(transaction: Transaction, obj: T): T

    fun patch(transaction: Transaction, obj: T): T

    fun delete(transaction: Transaction, obj: T): Boolean
}
