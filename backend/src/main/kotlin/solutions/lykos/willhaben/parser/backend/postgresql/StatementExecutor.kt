package solutions.lykos.willhaben.parser.backend.postgresql

import java.sql.Connection
import java.sql.ResultSet

class StatementExecutor(
    basePath: String = "/io/invenium/lamar/backend/postgresql"
) {
    private val templateProvider =
        QueryTemplateProvider(javaClass.getResource(basePath)!!)

    fun <T> execute(
        connection: Connection,
        templatePath: String,
        templateParameters: Map<String, Any> = emptyMap(),
        queryParameters: Map<String, Any?> = emptyMap(),
        transform: (ResultSet) -> T
    ): List<T> {
        val template = templateProvider.getTemplate(templatePath, templateParameters)
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

    fun runStatement(
        transaction: Transaction,
        templatePath: String,
        templateParameters: Map<String, Any> = emptyMap(),
        queryParameters: Map<String, Any?> = emptyMap()
    ): Boolean {
        val template = templateProvider.getTemplate(templatePath, templateParameters)
        return try {
            QueryBuilder(transaction)
                .append(template)
                .build(queryParameters)
                .use { preparedStatement ->
                    preparedStatement.execute()
                }
        } catch (e: Exception) {
            System.err.println(template)
            throw e
        }
    }
}
