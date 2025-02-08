package solutions.lykos.willhaben.parser.backend.postgresql

import solutions.lykos.willhaben.parser.backend.snakeCase
import java.sql.Connection
import java.sql.ResultSet

object DefaultStatementExecutor {

    inline fun <reified T> select(
        connection: Connection,
        whereConditions: Map<String, String> = emptyMap(),
        queryParameters: Map<String, Any?> = emptyMap(),
        crossinline transform: (ResultSet) -> T
    ): List<T> {
        val columnList = T::class.java.declaredFields.joinToString(",")
        val tableName = T::class.java.simpleName.snakeCase()

        val whereList = whereConditions.entries.joinToString(" AND ") {
            it.key + " = " + it.value
        }

        val template = """
            SELECT $columnList FROM $tableName WHERE $whereList
        """.trimIndent()

        return try {
            QueryBuilder(connection)
                .append(template)
                .build(queryParameters)
                .use { preparedStatement ->
                    preparedStatement.executeQuery().use { resultSet ->
                        generateSequence { resultSet.takeIf { it.next() } }.map { transform(it) }.toList()
                    }
                }
        } catch (e: Exception) {
            System.err.println(template)
            throw e
        }
    }

    inline fun <reified T> upsert(
        connection: Connection,
        objects: List<T>
    ): List<T> {
        if (objects.isEmpty()) return emptyList()
        val tableName = T::class.java.simpleName.snakeCase()
        val mapping = T::class.java.fields.associate {
            it.name.snakeCase() to { obj: T -> it.get(obj) }
        }

        val template = """
            INSERT INTO $tableName (
        """.trimIndent()

        return try {
            QueryBuilder(connection)
                .append(template)
                .build(emptyMap())
                .use { preparedStatement ->
                    preparedStatement.executeQuery().use { resultSet ->
                        generateSequence { resultSet.takeIf { it.next() } }.map { it as T }.toList()
                    }
                }
        } catch (e: Exception) {
            System.err.println(template)
            throw e
        }
    }
}
