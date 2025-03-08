package solutions.lykos.willhaben.parser.backend.database.postgresql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

class QueryBuilder(
    private val connection: Connection
) {
    companion object {
        private val VARIABLE_REGEX = Regex("\\$\\{([^}]+)}")
    }

    private val builder = StringBuilder()
    private val parameterValues = ArrayList<Any?>()

    fun append(template: String): QueryBuilder {
        builder.append(template)
        return this
    }

    fun build(parameters: Map<String, Any?>): PreparedStatement {
        val sql =
            builder.toString().let { template ->
                VARIABLE_REGEX.replace(template) { matchResult ->
                    val key = matchResult.groupValues[1]
                    val path = key.split('.')
                    val value = getValue(parameters, path)

                    // Check for column reference
                    val range = matchResult.range
                    if (range.first > 0 &&
                        template[range.first - 1] == '"' &&
                        range.last < template.lastIndex &&
                        template[range.last + 1] == '"'
                    ) {
                        value.toString()
                    } else {
                        parameterValues.add(value)
                        "?"
                    }
                }
            }

        val statement = connection.prepareStatement(sql)
        val parameterMetaData = statement.parameterMetaData
        parameterValues.forEachIndexed { index, value ->
            if (value == null) {
                statement.setNull(index + 1, parameterMetaData.getParameterType(index + 1))
            } else {
                when (value) {
                    is List<*> -> {
                        if (parameterMetaData.getParameterType(index + 1) != Types.ARRAY) {
                            throw IllegalArgumentException("Parameter ${index + 1} is not an array type")
                        }
                        val elementType =
                            parameterMetaData.getParameterTypeName(index + 1).let { typeName ->
                                val startIndex = if (typeName[0] != '_') 0 else 1
                                val endIndex = typeName.indexOf('[').takeIf { it != -1 } ?: typeName.length
                                typeName.substring(startIndex, endIndex)
                            }
                        val array = connection.createArrayOf(elementType, value.toTypedArray())
                        statement.setObject(index + 1, array)
                    }

                    else -> {
                        statement.setObject(index + 1, value)
                    }
                }
            }
        }
        return statement
    }

    private fun getValue(
        parameters: Map<String, Any?>,
        path: List<String>
    ): Any? {
        var current = parameters
        for (i in 0 until path.lastIndex) {
            @Suppress("UNCHECKED_CAST")
            current = current[path[i]] as Map<String, Any>? ?: return null
        }
        return current[path.last()]
    }
}
