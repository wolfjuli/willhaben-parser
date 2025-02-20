package solutions.lykos.willhaben.parser.backend.importer.actions.writers

import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions.getTableName
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import java.sql.PreparedStatement
import java.sql.Types

enum class ConflictType {
    DO_UPDATE,
    DO_NOTHING_WITH_CHECK,
    DO_NOTHING
}

inline fun <reified T : Node> createPreparedInsertStatement(
    columnMapping: Map<String, String>,
    transaction: Transaction,
    conflictType: ConflictType? = null,
    onConflictColumns: Set<String>? = null,
    conflictedColumnsToUpdate: Set<String>? = null,
    tableName: String = getTableName<T>()
): PreparedStatement = createPreparedInsertStatement(
    columnMapping,
    transaction,
    tableName,
    conflictType,
    onConflictColumns,
    conflictedColumnsToUpdate
)

fun createPreparedInsertStatement(
    columnMapping: Map<String, String>,
    transaction: Transaction,
    tableName: String,
    conflictType: ConflictType? = null,
    onConflictColumns: Set<String>? = null,
    conflictedColumnsToUpdate: Set<String>? = null
): PreparedStatement = columnMapping.asIterable().let {
    "INSERT INTO $tableName"
        .plus(it.joinToString(prefix = "(", postfix = ")") { (colName, _) -> colName })
        .plus("\nVALUES ")
        .plus(it.joinToString(prefix = "(", postfix = ")") { (colName, transformation) ->
            if (!transformation.contains("?"))
                error("Transformation for column $colName has to contain at least the SQL placeholder '?'")
            else
                transformation
        })
        .plus(
            if (conflictType != null) generateOnConflictClause(
                conflictType,
                onConflictColumns,
                conflictedColumnsToUpdate
            ) else ""
        )
}.let { sql -> transaction.prepareStatement(sql) }

@Deprecated("Avoid usage with java.sql.preparedStatement due to inefficiency")
inline fun <reified T : Node> generateInsertIntoStatement(
    data: List<T>,
    columns: Set<String>,
    tableName: String = getTableName<T>(),
    noinline rowTransformation: (T) -> CharSequence,
): String =
    "INSERT INTO $tableName "
        .plus(columns.joinToString(prefix = "(", postfix = ")") { it })
        .plus("\nVALUES ")
        .plus(data.joinToString(separator = ",\n".appendPad(7), postfix = "\n", transform = rowTransformation))

fun generateOnConflictClause(
    conflictType: ConflictType,
    onConflictColumns: Set<String>? = null,
    conflictedColumnsToUpdate: Set<String>? = null
): String = "ON CONFLICT " + when (conflictType) {
    ConflictType.DO_UPDATE -> onConflictColumns!!.joinToString(prefix = "(", postfix = ") ") { it } +
            "DO UPDATE\nSET ${conflictedColumnsToUpdate?.joinToString(",\n\t") { "$it = excluded.$it" }}"

    ConflictType.DO_NOTHING_WITH_CHECK -> onConflictColumns?.joinToString(
        prefix = "(",
        postfix = ") "
    ) { it } + "DO NOTHING"

    ConflictType.DO_NOTHING -> "DO NOTHING"
}

fun String.appendPad(count: Int) = this + " ".repeat(count)

fun PreparedStatement.setIntOrNull(colIndex: Int, value: Int?) =
    if (value == null)
        setNull(colIndex, Types.NULL)
    else
        setInt(colIndex, value)

fun PreparedStatement.setStringOrNull(colIndex: Int, value: String?) =
    if (value.isNullOrEmpty())
        setNull(colIndex, Types.NULL)
    else
        setString(colIndex, value)

fun PreparedStatement.setFloatOrNull(colIndex: Int, value: Float?) =
    if (value == null)
        setNull(colIndex, Types.NULL)
    else
        setFloat(colIndex, value)
