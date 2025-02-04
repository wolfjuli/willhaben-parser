package solutions.lykos.willhaben.parser.backend.postgresql

import java.sql.Connection

/***
 * @author Julian Wolf
 *
 * A wrapper class to strongly specify, if a connection is already in a transaction
 * created by Connection.transaction (and thus useTransaction)
 */

class Transaction(
    val baseConnection: Connection
) : Connection by baseConnection
