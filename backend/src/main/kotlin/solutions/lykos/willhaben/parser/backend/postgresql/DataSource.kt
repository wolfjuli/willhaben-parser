package solutions.lykos.willhaben.parser.backend.postgresql

import org.postgresql.ds.PGSimpleDataSource
import solutions.lykos.willhaben.parser.backend.api.WatchList
import solutions.lykos.willhaben.parser.backend.api.insert.NWatchList
import solutions.lykos.willhaben.parser.backend.config.DatabaseConfiguration

class DataSource(config: DatabaseConfiguration) {

    private val dataSource = PGSimpleDataSource().apply {
        serverNames = arrayOf(config.host)
        portNumbers = intArrayOf(config.port)
        databaseName = config.name
        user = config.user
        password = config.password
    }

    val connection
        get() = dataSource.connection!!

    abstract class Get {
        abstract fun watchLists(): List<WatchList>
    }

    abstract class Set {
        abstract fun watchLists(values: List<NWatchList>): List<WatchList>
    }

    val get = object : Get() {
        override fun watchLists(): List<WatchList> =
            dataSource.connection.useTransaction { transaction ->
                StatementExecutor().execute(
                    transaction,
                    "watch_lists"
                ) {
                    WatchList(
                        it["id"],
                        it["url"]
                    )
                }
            }
    }

    val set = object : Set() {
        override fun watchLists(values: List<NWatchList>): List<WatchList> =
            dataSource.connection.useTransaction { transaction ->
                StatementExecutor().execute(
                    transaction,
                    "upsert/watchLists"
                ) {
                    WatchList(
                        it["id"],
                        it["url"]
                    )
                }
            }
    }


}
